package com.threestrandscattle.app.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.threestrandscattle.app.models.*
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.util.Date

class SaleStore(private val context: Context) : ViewModel() {

    companion object {
        private const val TAG = "SaleStore"
    }

    private val gson = Gson()
    private val prefs: SharedPreferences = context.getSharedPreferences("sale_store", Context.MODE_PRIVATE)
    private val apiService = ApiService.getInstance(context)
    private val squareService = SquareService.getInstance(context)

    private val _sales = MutableStateFlow<List<FlashSale>>(emptyList())
    val sales: StateFlow<List<FlashSale>> = _sales

    private val _popUpSales = MutableStateFlow<List<PopUpSale>>(emptyList())
    val popUpSales: StateFlow<List<PopUpSale>> = _popUpSales

    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements

    private val _events = MutableStateFlow<List<CattleEvent>>(emptyList())
    val events: StateFlow<List<CattleEvent>> = _events

    private val _inboxItems = MutableStateFlow<List<InboxItem>>(emptyList())
    val inboxItems: StateFlow<List<InboxItem>> = _inboxItems

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _notificationPrefs = MutableStateFlow(NotificationPreferences())
    val notificationPrefs: StateFlow<NotificationPreferences> = _notificationPrefs

    private val _dismissedFromHome = MutableStateFlow<Set<String>>(emptySet())

    // Storage keys
    private val inboxKey = "notification_inbox"
    private val dismissedKey = "dismissed_from_home"
    private val seenAnnouncementIDsKey = "seen_announcement_ids"

    init {
        loadPreferences()
        loadInbox()
        loadDismissed()
        startPolling()
    }

    // Computed properties
    val homeNotifications: List<InboxItem>
        get() = _inboxItems.value.filter { !_dismissedFromHome.value.contains(it.id) }

    val activeSales: List<FlashSale>
        get() = _sales.value
            .filter { it.isActive && !it.isExpired }
            .sortedBy { it.expiresAt }

    val expiredSales: List<FlashSale>
        get() = _sales.value.filter { it.isExpired || !it.isActive }

    val unreadCount: Int
        get() = _inboxItems.value.count { !it.isRead }

    // Preferences
    private fun loadPreferences() {
        val json = prefs.getString(NotificationPreferences.STORAGE_KEY, null)
        if (json != null) {
            try {
                _notificationPrefs.value = gson.fromJson(json, NotificationPreferences::class.java)
            } catch (_: Exception) { }
        }
    }

    fun savePreferences() {
        prefs.edit().putString(
            NotificationPreferences.STORAGE_KEY,
            gson.toJson(_notificationPrefs.value)
        ).apply()
    }

    fun updatePreferences(update: (NotificationPreferences) -> NotificationPreferences) {
        _notificationPrefs.value = update(_notificationPrefs.value)
        savePreferences()
    }

    // Inbox management
    private fun loadInbox() {
        val json = prefs.getString(inboxKey, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<InboxItem>>() {}.type
                _inboxItems.value = gson.fromJson(json, type)
            } catch (_: Exception) { }
        }
    }

    private fun loadDismissed() {
        val set = prefs.getStringSet(dismissedKey, emptySet()) ?: emptySet()
        _dismissedFromHome.value = set
    }

    fun addInboxItem(title: String, body: String) {
        val item = InboxItem(title = title, body = body)
        _inboxItems.value = listOf(item) + _inboxItems.value
        saveInbox()
    }

    fun markAsRead(id: String) {
        _inboxItems.value = _inboxItems.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        saveInbox()
    }

    fun dismissFromHome(id: String) {
        _dismissedFromHome.value = _dismissedFromHome.value + id
        saveDismissed()
    }

    fun removeInboxItem(id: String) {
        _inboxItems.value = _inboxItems.value.filter { it.id != id }
        _dismissedFromHome.value = _dismissedFromHome.value - id
        saveInbox()
        saveDismissed()
    }

    fun clearInbox() {
        _inboxItems.value = emptyList()
        _dismissedFromHome.value = emptySet()
        saveInbox()
        saveDismissed()
    }

    private fun saveInbox() {
        prefs.edit().putString(inboxKey, gson.toJson(_inboxItems.value)).apply()
    }

    private fun saveDismissed() {
        prefs.edit().putStringSet(dismissedKey, _dismissedFromHome.value).apply()
    }

    // Sync announcements to inbox
    private fun syncAnnouncementsToInbox() {
        val seenIDs = prefs.getStringSet(seenAnnouncementIDsKey, emptySet())?.toMutableSet() ?: mutableSetOf()
        var added = false

        for (announcement in _announcements.value) {
            if (!announcement.is_active) continue
            val key = "announcement-${announcement.id}-${announcement.created_at ?: ""}"
            if (!seenIDs.contains(key)) {
                val item = InboxItem(title = announcement.title, body = announcement.message)
                _inboxItems.value = listOf(item) + _inboxItems.value
                seenIDs.add(key)
                added = true
            }
        }

        if (added) {
            saveInbox()
            prefs.edit().putStringSet(seenAnnouncementIDsKey, seenIDs).apply()
        }
    }

    // Data refresh
    fun refreshSales() {
        viewModelScope.launch {
            if (_sales.value.isEmpty()) _isLoading.value = true

            try {
                // supervisorScope prevents child async failures from cancelling the parent
                supervisorScope {
                    val salesDeferred = async { apiService.fetchFlashSales() }
                    val popUpsDeferred = async { apiService.fetchPopUpSales() }
                    val announcementsDeferred = async { apiService.fetchAnnouncements() }
                    val eventsDeferred = async { apiService.fetchEvents() }

                    // Catch each individually so one failure doesn't block others
                    try { _sales.value = salesDeferred.await() } catch (e: Exception) { Log.e(TAG, "Failed to fetch flash sales", e) }
                    try { _popUpSales.value = popUpsDeferred.await() } catch (e: Exception) { Log.e(TAG, "Failed to fetch pop-up markets", e) }
                    try { _announcements.value = announcementsDeferred.await() } catch (e: Exception) { Log.e(TAG, "Failed to fetch announcements", e) }
                    try { _events.value = eventsDeferred.await() } catch (e: Exception) { Log.e(TAG, "Failed to fetch events", e) }
                }
            } catch (e: Exception) { Log.e(TAG, "Failed to refresh data", e) }

            _isLoading.value = false
            // Reload inbox from SharedPreferences to pick up items added by FCMService
            loadInbox()
            syncAnnouncementsToInbox()
        }
    }

    // Poll every 30 seconds
    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                refreshSales()
                delay(30_000)
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SaleStore(context.applicationContext) as T
        }
    }
}
