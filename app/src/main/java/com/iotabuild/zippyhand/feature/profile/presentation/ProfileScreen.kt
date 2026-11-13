package com.iotabuild.zippyhand.feature.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iotabuild.zippyhand.ui.theme.ZippyhandTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    onProfileDetailsClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onAboutUsClick: () -> Unit,
    onAddressesClick: () -> Unit,
    onChangeCityClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDeleteAccount: () -> Unit,
    onScrollChanged: (Boolean) -> Unit = {},
    authViewModel: AuthViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(scrollState) {
        var lastOffset = 0
        snapshotFlow { scrollState.value }
            .map { offset ->
                val isScrollingDown = offset > lastOffset
                lastOffset = offset
                !isScrollingDown
            }
            .distinctUntilChanged()
            .collect { isVisible ->
                if (scrollState.value > 50) {
                    onScrollChanged(isVisible)
                } else {
                    onScrollChanged(true)
                }
            }
    }
    val userProfile by authViewModel.userProfile.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            onConfirm = {
                showDeleteDialog = false
                onDeleteAccount()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 100.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        ProfileHeader(
            name = userProfile?.name ?: "User",
            phoneNumber = userProfile?.phoneNumber ?: "No phone",
            location = userProfile?.email ?: ""
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Personal",
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                ProfileOptionItem(
                    icon = Icons.Default.Person,
                    title = "Profile Details",
                    subtitle = "View and edit your personal details",
                    onClick = onProfileDetailsClick
                )

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 12.dp))

                ProfileOptionItem(
                    icon = Icons.Default.LocationOn,
                    title = "My Addresses",
                    subtitle = "Manage your delivery locations",
                    onClick = onAddressesClick
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Support & Info",
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                ProfileOptionItem(
                    icon = Icons.Default.Info,
                    title = "About Us",
                    subtitle = "Learn more about FixKro",
                    onClick = onAboutUsClick
                )
                
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 12.dp))
                
                ProfileOptionItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = "How we handle your data",
                    onClick = onPrivacyClick
                )
                
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 12.dp))
                
                ProfileOptionItem(
                    icon = Icons.Default.Description,
                    title = "Terms & Conditions",
                    subtitle = "Rules for using our service",
                    onClick = onTermsClick
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                ProfileOptionItem(
                    icon = Icons.Default.DeleteOutline,
                    title = "Delete Account",
                    subtitle = "Permanently remove your account",
                    isDestructive = true,
                    onClick = { showDeleteDialog = true }
                )
                
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 12.dp))
                
                ProfileOptionItem(
                    icon = Icons.Default.ExitToApp,
                    title = "Logout",
                    subtitle = "Sign out of your session",
                    isDestructive = true,
                    onClick = onLogoutClick
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(48.dp))

        BrandingSection(version = "1.0.1")
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ZippyhandTheme {
        ProfileScreen(
            onProfileDetailsClick = {},
            onTermsClick = {}, onPrivacyClick = {},
            onAboutUsClick = {},
            onAddressesClick = {},
            onChangeCityClick = {},
            onLogoutClick = {},
            onDeleteAccount = {},
        )
    }
}
