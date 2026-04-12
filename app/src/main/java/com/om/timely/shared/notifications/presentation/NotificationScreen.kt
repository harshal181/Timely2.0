package com.om.timely.shared.notifications.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.timely.shared.notifications.presentation.components.NotificationCard
import com.om.timely.shared.notifications.presentation.components.NotificationFilterRow
import com.om.timely.shared.notifications.presentation.components.SendNotificationBottomSheet
import com.om.timely.ui.theme.BrandColors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    state: NotificationScreenState,
    onAction: (NotificationAction) -> Unit,
    onNavigateBack: () -> Unit,
    eventFlow: Flow<NotificationEvent>,
    snackbarHostState: SnackbarHostState
) {

    val context = LocalContext.current

    LaunchedEffect(true) {
        eventFlow.collectLatest { event ->
            when(event) {
                is NotificationEvent.ShowError -> snackbarHostState.showSnackbar(event.error.asString(context))
                is NotificationEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message.asString(context))
            }
        }
    }

    Scaffold(
        containerColor = if (state.isFaculty) BrandColors.BgOffWhite else BrandColors.BgStudent,
        floatingActionButton = {
            if (state.isFaculty) {
                FloatingActionButton(
                    onClick = { onAction(NotificationAction.OnToggleSendDialog(true)) },
                    containerColor = BrandColors.PrimaryPurple,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Announcement",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "New",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth().padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Announcements",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandColors.TextDark
                )

                // Mark all read button
                val unreadCount = state.notifications.count { !it.isRead }
                if (unreadCount > 0) {
                    Surface(
                        color = BrandColors.CardPurpleLight,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.clickable { onAction(NotificationAction.OnMarkAllAsReadClicked) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Mark all read",
                                tint = BrandColors.PrimaryPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Mark all read",
                                style = MaterialTheme.typography.labelMedium,
                                color = BrandColors.PrimaryPurple,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            val unreadCount = state.notifications.count { !it.isRead }
            if (unreadCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(BrandColors.PrimaryPurple, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$unreadCount unread",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrandColors.PrimaryPurple,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Filters — build list based on user role
            val activeFilters: List<NotificationFilter> = if (state.isFaculty) {
                FacultyNotificationFilter.values().toList()
            } else {
                StudentNotificationFilter.values().toList()
            }
            NotificationFilterRow(
                filters = activeFilters,
                activeFilter = state.activeFilter,
                onFilterSelected = { onAction(NotificationAction.OnFilterSelected(it)) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            )

            // Content
            if (state.isLoading && state.notifications.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.filteredNotifications.isEmpty()) {
                // Empty State
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Empty",
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "All caught up!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No unread notifications",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp) // Extra space for the FAB
                ) {
                    items(state.filteredNotifications, key = { it.id }) { notification ->
                        NotificationCard(
                            notification = notification,
                            modifier = Modifier.fillMaxWidth(),
                            onMarkAsRead = {}
                        )
                    }
                }
            }
        }
    }

    if (state.isSendDialogVisible) {
        SendNotificationBottomSheet(
            formState = state.sendFormState,
            onAction = onAction
        )
    }
}