package com.iotabuild.zippyhand.location.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iotabuild.zippyhand.ui.theme.BackgroundColor
import com.iotabuild.zippyhand.ui.theme.BorderColor
import com.iotabuild.zippyhand.ui.theme.DarkText
import com.iotabuild.zippyhand.ui.theme.LightBlue
import com.iotabuild.zippyhand.ui.theme.MainBlue
import com.iotabuild.zippyhand.ui.theme.MediumText
import org.koin.androidx.compose.koinViewModel

@Composable
fun LocationGateScreen(
    onLocationGranted: () -> Unit = {},
    onManualSelectCity: () -> Unit = {},
    viewModel: LocationViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onPermissionResult(fineGranted || coarseGranted)
    }

    LaunchedEffect(Unit) {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        viewModel.onPermissionResult(fineLocationGranted || coarseLocationGranted)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Blinkit Map Container with Floating Status Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
        ) {
            BlinkitMapCanvas(
                modifier = Modifier.fillMaxSize(),
                isLocating = uiState.isFetchingLocation
            )

            // Floating Service Status Badge on Top of Map
            if (uiState.hasPermission && uiState.hasLocationData && !uiState.isFetchingLocation) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (uiState.isServiceable) Color(0xFF2E7D32) else Color(0xFFE65100)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isServiceable) "Serviceable Area" else "Currently Unavailable",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.isServiceable) Color(0xFF2E7D32) else Color(0xFFE65100)
                        )
                    }
                }
            }

            // "Locate Me" Floating Action Button on Map
            FloatingActionButton(
                onClick = { viewModel.fetchLocation() },
                shape = CircleShape,
                containerColor = Color.White,
                contentColor = MainBlue,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(48.dp)
            ) {
                if (uiState.isFetchingLocation) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MainBlue,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Locate Me",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Location Sheet Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Location Header Row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (uiState.hasPermission && uiState.hasLocationData) {
                                    if (uiState.isServiceable) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                } else LightBlue
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (uiState.hasPermission && uiState.hasLocationData) {
                                if (uiState.isServiceable) Icons.Default.Verified else Icons.Default.ErrorOutline
                            } else Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (uiState.hasPermission && uiState.hasLocationData) {
                                if (uiState.isServiceable) Color(0xFF2E7D32) else Color(0xFFE65100)
                            } else MainBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (uiState.hasPermission) uiState.cityName else "Enable Location Access",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            // Status Chip
                            if (uiState.hasPermission && uiState.hasLocationData && !uiState.isFetchingLocation) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (uiState.isServiceable) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                ) {
                                    Text(
                                        text = if (uiState.isServiceable) "AVAILABLE" else "UNAVAILABLE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (uiState.isServiceable) Color(0xFF2E7D32) else Color(0xFFE65100),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (uiState.hasPermission) uiState.locationDetail else "Live location detection requires location permission.",
                            fontSize = 13.sp,
                            color = MediumText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Service Availability Information Banner
                if (uiState.hasPermission && uiState.hasLocationData && !uiState.isFetchingLocation) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (uiState.isServiceable) Color(0xFFF1F8E9) else Color(0xFFFFF8E1),
                        border = BorderStroke(
                            1.dp,
                            if (uiState.isServiceable) Color(0xFFC8E6C9) else Color(0xFFFFE0B2)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (uiState.isServiceable) Color(0xFFA5D6A7) else Color(0xFFFFCC80)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (uiState.isServiceable) Icons.Default.CheckCircle else Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (uiState.isServiceable) Color(0xFF1B5E20) else Color(0xFFE65100),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = if (uiState.isServiceable) "We're available in your area!" else "Service not available here yet",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isServiceable) Color(0xFF1B5E20) else Color(0xFFBF360C)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (uiState.isServiceable)
                                        "Doorstep service & instant booking are ready at this location."
                                    else
                                        "Fixkro is expanding fast! Try selecting another city or pincode.",
                                    fontSize = 12.sp,
                                    color = if (uiState.isServiceable) Color(0xFF2E7D32) else Color(0xFFE65100),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Action Button
                Button(
                    onClick = {
                        if (!uiState.hasPermission) {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else if (uiState.isServiceable) {
                            onLocationGranted()
                        } else {
                            onManualSelectCity()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!uiState.hasPermission || uiState.isServiceable) MainBlue else Color(0xFFE65100)
                    ),
                    enabled = !uiState.isFetchingLocation
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (uiState.isFetchingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Checking Availability...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else if (!uiState.hasPermission) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Locate Me",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else if (uiState.isServiceable) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Confirm Location & Proceed",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Explore",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun BlinkitMapCanvas(
    modifier: Modifier = Modifier,
    isLocating: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "map_pulse")

    val wave1Scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1_scale"
    )
    val wave1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1_alpha"
    )

    val wave2Scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, delayMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2_scale"
    )
    val wave2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, delayMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2_alpha"
    )

    val pinOffsetY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pin_bounce"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
            .background(Color(0xFFE8ECEF)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Background
            drawRect(color = Color(0xFFF0F4F8))

            // Parks / Green Zones
            drawCircle(
                color = Color(0xFFE1F2DF),
                radius = width * 0.22f,
                center = Offset(width * 0.25f, height * 0.3f)
            )
            drawCircle(
                color = Color(0xFFE1F2DF),
                radius = width * 0.18f,
                center = Offset(width * 0.8f, height * 0.7f)
            )

            // Water River / Lake
            drawPath(
                path = Path().apply {
                    moveTo(width * 0.85f, 0f)
                    cubicTo(width * 0.75f, height * 0.3f, width * 0.9f, height * 0.65f, width * 0.8f, height)
                    lineTo(width, height)
                    lineTo(width, 0f)
                    close()
                },
                color = Color(0xFFD3E8F8)
            )

            // Roads
            val roadColor = Color.White
            val roadOutline = Color(0xFFCBD5E1)

            // Main Horizontal Highway
            drawLine(
                color = roadOutline,
                start = Offset(0f, height * 0.5f),
                end = Offset(width, height * 0.5f),
                strokeWidth = 36f
            )
            drawLine(
                color = roadColor,
                start = Offset(0f, height * 0.5f),
                end = Offset(width, height * 0.5f),
                strokeWidth = 28f
            )

            // Main Vertical Boulevard
            drawLine(
                color = roadOutline,
                start = Offset(width * 0.5f, 0f),
                end = Offset(width * 0.5f, height),
                strokeWidth = 36f
            )
            drawLine(
                color = roadColor,
                start = Offset(width * 0.5f, 0f),
                end = Offset(width * 0.5f, height),
                strokeWidth = 28f
            )

            // Diagonal Street
            drawLine(
                color = roadColor,
                start = Offset(0f, height * 0.2f),
                end = Offset(width * 0.8f, height),
                strokeWidth = 18f
            )

            // Building Blocks
            val buildingColor = Color(0xFFE2E8F0)
            drawRoundRect(
                color = buildingColor,
                topLeft = Offset(width * 0.08f, height * 0.12f),
                size = Size(width * 0.3f, height * 0.22f),
                cornerRadius = CornerRadius(12f, 12f)
            )
            drawRoundRect(
                color = buildingColor,
                topLeft = Offset(width * 0.58f, height * 0.12f),
                size = Size(width * 0.22f, height * 0.25f),
                cornerRadius = CornerRadius(12f, 12f)
            )
            drawRoundRect(
                color = buildingColor,
                topLeft = Offset(width * 0.12f, height * 0.62f),
                size = Size(width * 0.28f, height * 0.26f),
                cornerRadius = CornerRadius(12f, 12f)
            )
            drawRoundRect(
                color = buildingColor,
                topLeft = Offset(width * 0.58f, height * 0.62f),
                size = Size(width * 0.18f, height * 0.25f),
                cornerRadius = CornerRadius(12f, 12f)
            )
        }

        // Animated Blinkit Radar Pulsing Marker
        Box(contentAlignment = Alignment.Center) {
            // Ripple 1
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .graphicsLayer {
                        scaleX = wave1Scale
                        scaleY = wave1Scale
                        alpha = wave1Alpha
                    }
                    .background(MainBlue.copy(alpha = 0.35f), CircleShape)
            )
            // Ripple 2
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .graphicsLayer {
                        scaleX = wave2Scale
                        scaleY = wave2Scale
                        alpha = wave2Alpha
                    }
                    .background(MainBlue.copy(alpha = 0.25f), CircleShape)
            )

            // Ground Pin Shadow
            Box(
                modifier = Modifier
                    .offset(y = 22.dp)
                    .size(28.dp, 8.dp)
                    .background(Color.Black.copy(alpha = 0.25f), CircleShape)
            )

            // Pin Pinball Marker with Navigation Arrow
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = pinOffsetY.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MainBlue,
                    shadowElevation = 10.dp,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Current Location Marker",
                            tint = Color.White,
                            modifier = Modifier
                                .size(28.dp)
                                .rotate(-45f)
                        )
                    }
                }
            }
        }
    }
}
