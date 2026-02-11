package com.threestrandscattle.app.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.threestrandscattle.app.models.CattleEvent
import com.threestrandscattle.app.services.LocationService
import com.threestrandscattle.app.services.SaleStore
import com.threestrandscattle.app.ui.theme.ThemeColors
import com.threestrandscattle.app.ui.theme.ThemeDimens
import com.threestrandscattle.app.ui.theme.ThemeTypography
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(store: SaleStore) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val events by store.events.collectAsState()

    var selectedDate by remember { mutableStateOf(Date()) }
    var displayedMonth by remember { mutableStateOf(Date()) }
    val calendar = remember { Calendar.getInstance() }

    // Location permission state
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        hasLocationPermission = granted
        LocationService.getInstance(context).setAuthorized(granted)
    }

    // Request location permission when screen appears if not already granted
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Camera position for the map
    val cameraPositionState = rememberCameraPositionState {
        val defaultPos = if (events.isNotEmpty()) {
            LatLng(events.first().latitude, events.first().longitude)
        } else {
            LatLng(27.0, -80.2) // Central Florida
        }
        position = CameraPosition.fromLatLngZoom(defaultPos, 8f)
    }

    // Build bounds from all events for recentering
    val eventBounds = remember(events) {
        if (events.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            events.forEach { event ->
                boundsBuilder.include(LatLng(event.latitude, event.longitude))
            }
            boundsBuilder.build()
        } else null
    }

    // Fit map to show all event markers on first load
    LaunchedEffect(events) {
        if (events.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            events.forEach { event ->
                boundsBuilder.include(LatLng(event.latitude, event.longitude))
            }
            val bounds = boundsBuilder.build()
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 80))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Events", color = ThemeColors.Primary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeColors.Background)
            )
        },
        containerColor = ThemeColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Map section header
            Row(
                modifier = Modifier.padding(horizontal = ThemeDimens.ScreenPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Map,
                    contentDescription = null,
                    tint = ThemeColors.BronzeGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Event Locations",
                    style = ThemeTypography.HeadingFont,
                    color = ThemeColors.Primary
                )
            }

            // Google Maps with event markers
            Box(
                modifier = Modifier
                    .padding(horizontal = ThemeDimens.ScreenPadding)
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(ThemeDimens.CornerRadius))
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        mapType = MapType.NORMAL,
                        isMyLocationEnabled = hasLocationPermission
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = false
                    )
                ) {
                    events.forEach { event ->
                        Marker(
                            state = MarkerState(position = LatLng(event.latitude, event.longitude)),
                            title = event.title,
                            snippet = event.location
                        )
                    }
                }

                // Recenter button overlay (bottom-right of map)
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            eventBounds?.let { bounds ->
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngBounds(bounds, 80)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ThemeColors.CardBackground.copy(alpha = 0.9f))
                ) {
                    Icon(
                        Icons.Filled.MyLocation,
                        contentDescription = "Recenter map",
                        tint = ThemeColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Calendar
            CalendarView(
                displayedMonth = displayedMonth,
                selectedDate = selectedDate,
                events = events,
                onDateSelected = { selectedDate = it },
                onMonthChanged = { displayedMonth = it }
            )

            // Events for selected date
            EventsForDate(
                selectedDate = selectedDate,
                events = events
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun CalendarView(
    displayedMonth: Date,
    selectedDate: Date,
    events: List<CattleEvent>,
    onDateSelected: (Date) -> Unit,
    onMonthChanged: (Date) -> Unit
) {
    val calendar = remember { Calendar.getInstance() }
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.US) }

    Column(
        modifier = Modifier
            .padding(horizontal = ThemeDimens.ScreenPadding)
            .clip(RoundedCornerShape(ThemeDimens.CornerRadius))
            .background(ThemeColors.CardBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                calendar.time = displayedMonth
                calendar.add(Calendar.MONTH, -1)
                onMonthChanged(calendar.time)
            }) {
                Icon(
                    Icons.Filled.ChevronLeft,
                    contentDescription = "Previous month",
                    tint = ThemeColors.Primary
                )
            }

            Text(
                text = monthFormat.format(displayedMonth),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif,
                color = ThemeColors.Primary
            )

            IconButton(onClick = {
                calendar.time = displayedMonth
                calendar.add(Calendar.MONTH, 1)
                onMonthChanged(calendar.time)
            }) {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = "Next month",
                    tint = ThemeColors.Primary
                )
            }
        }

        // Day-of-week headers
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeColors.TextSecondary,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Day grid
        val days = remember(displayedMonth) { getDaysInMonth(displayedMonth) }
        val today = remember { Calendar.getInstance() }

        Column {
            days.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { date ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (date != null) {
                                val cal = Calendar.getInstance().apply { time = date }
                                val isSelected = isSameDay(date, selectedDate)
                                val isToday = isSameDay(date, today.time)
                                val hasEvent = events.any { isSameDay(it.date, date) }

                                Column(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .then(
                                            if (isSelected) Modifier.background(ThemeColors.Primary)
                                            else Modifier
                                        )
                                        .clickable { onDateSelected(date) }
                                        .padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${cal.get(Calendar.DAY_OF_MONTH)}",
                                        fontSize = 15.sp,
                                        fontWeight = when {
                                            isSelected -> FontWeight.Bold
                                            isToday -> FontWeight.SemiBold
                                            else -> FontWeight.Normal
                                        },
                                        color = when {
                                            isSelected -> Color.White
                                            isToday -> ThemeColors.Primary
                                            else -> ThemeColors.TextPrimary
                                        }
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    hasEvent && isSelected -> Color.White
                                                    hasEvent -> ThemeColors.Bronze
                                                    else -> Color.Transparent
                                                }
                                            )
                                    )
                                }
                            }
                        }
                    }
                    // Fill remaining cells if week has less than 7 days
                    repeat(7 - week.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun EventsForDate(selectedDate: Date, events: List<CattleEvent>) {
    val context = LocalContext.current
    val dayEvents = events.filter { isSameDay(it.date, selectedDate) }
    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d", Locale.US) }
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.US) }

    Column(
        modifier = Modifier.padding(horizontal = ThemeDimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = dateFormat.format(selectedDate),
            style = ThemeTypography.HeadingFont,
            color = ThemeColors.Primary
        )

        if (dayEvents.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Filled.EventBusy,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = ThemeColors.TextSecondary.copy(alpha = 0.4f)
                )
                Text(
                    "No events on this day",
                    style = ThemeTypography.BodyFont,
                    color = ThemeColors.TextSecondary
                )
            }
        } else {
            dayEvents.forEach { event ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(ThemeDimens.CornerRadius),
                    colors = CardDefaults.cardColors(containerColor = ThemeColors.CardBackground)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ThemeColors.Bronze.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Eco,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = ThemeColors.Bronze
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                event.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ThemeColors.Primary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(11.dp),
                                    tint = ThemeColors.TextSecondary
                                )
                                val timeStr = buildString {
                                    append(timeFormat.format(event.date))
                                    event.endDate?.let {
                                        append(" – ")
                                        append(timeFormat.format(it))
                                    }
                                }
                                Text(
                                    timeStr,
                                    style = ThemeTypography.CaptionFont,
                                    color = ThemeColors.TextSecondary
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(11.dp),
                                    tint = ThemeColors.TextSecondary
                                )
                                Text(
                                    event.location,
                                    style = ThemeTypography.CaptionFont,
                                    color = ThemeColors.TextSecondary
                                )
                            }
                        }

                        // Directions button
                        IconButton(
                            onClick = {
                                val uri = Uri.parse("google.navigation:q=${event.latitude},${event.longitude}")
                                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                } else {
                                    val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${event.latitude},${event.longitude}")
                                    context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ThemeColors.Bronze)
                        ) {
                            Icon(
                                Icons.Filled.Directions,
                                contentDescription = "Directions",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper functions
private fun getDaysInMonth(month: Date): List<Date?> {
    val calendar = Calendar.getInstance()
    calendar.time = month
    calendar.set(Calendar.DAY_OF_MONTH, 1)

    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val days = mutableListOf<Date?>()

    // Add null for empty cells before first day
    repeat(firstDayOfWeek) { days.add(null) }

    // Add actual days
    for (day in 1..daysInMonth) {
        calendar.set(Calendar.DAY_OF_MONTH, day)
        days.add(calendar.time)
    }

    return days
}

private fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
