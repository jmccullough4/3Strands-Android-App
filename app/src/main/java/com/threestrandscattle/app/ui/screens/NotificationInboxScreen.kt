package com.threestrandscattle.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.threestrandscattle.app.models.InboxItem
import com.threestrandscattle.app.services.SaleStore
import com.threestrandscattle.app.ui.theme.ThemeColors
import com.threestrandscattle.app.ui.theme.ThemeDimens
import com.threestrandscattle.app.ui.theme.ThemeTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationInboxScreen(store: SaleStore) {
    val inboxItems by store.inboxItems.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = ThemeColors.Primary) },
                actions = {
                    if (inboxItems.isNotEmpty()) {
                        TextButton(onClick = { store.clearInbox() }) {
                            Text(
                                "Clear All",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = ThemeColors.Primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeColors.Background)
            )
        },
        containerColor = ThemeColors.Background
    ) { padding ->
        if (inboxItems.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.Inbox,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = ThemeColors.Primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No Notifications",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ThemeColors.Primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Push notifications from 3 Strands\nwill appear here.",
                    fontSize = 14.sp,
                    color = ThemeColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(inboxItems, key = { it.id }) { item ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                store.removeInboxItem(item.id)
                                true
                            } else false
                        }
                    )

                    // Mark as read when visible
                    LaunchedEffect(item.id) {
                        if (!item.isRead) {
                            store.markAsRead(item.id)
                        }
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.error)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onError
                                )
                            }
                        },
                        enableDismissFromStartToEnd = false
                    ) {
                        InboxItemRow(item = item)
                    }

                    HorizontalDivider(color = ThemeColors.CardBackground)
                }
            }
        }
    }
}

@Composable
private fun InboxItemRow(item: InboxItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ThemeColors.Background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val icon = getIconForTitle(item.title)
        val iconColor = if (item.isRead) ThemeColors.TextSecondary else ThemeColors.BronzeGold

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = iconColor
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    item.title,
                    fontSize = 15.sp,
                    fontWeight = if (item.isRead) FontWeight.Medium else FontWeight.Bold,
                    color = ThemeColors.Primary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    item.timeAgo,
                    fontSize = 12.sp,
                    color = ThemeColors.TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                item.body,
                fontSize = 14.sp,
                color = ThemeColors.TextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun getIconForTitle(title: String): ImageVector {
    val lower = title.lowercase()
    return when {
        lower.contains("flash sale") -> Icons.Filled.Bolt
        lower.contains("pop-up") || lower.contains("coming to") -> Icons.Filled.LocationOn
        lower.contains("test") -> Icons.Filled.Send
        else -> Icons.Filled.Campaign
    }
}
