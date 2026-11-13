package com.iotabuild.zippyhand.feature.booking.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.iotabuild.zippyhand.feature.booking.domain.MyOrderResponse
import java.math.BigDecimal

@Composable
fun OrderInfoCard(
    order: MyOrderResponse,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = order.productImage ?: "https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=500",
            contentDescription = order.productTitle ?: "Service Image",
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = order.productTitle ?: "Home Service",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Qty: ${order.quantity}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "₹${order.totalAmount.toInt()}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ActiveProviderCard(
    name: String,
    title: String,
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF1F1F1))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun BookingBillDetails(
    itemTotal: BigDecimal,
    discount: BigDecimal = BigDecimal.ZERO,
    serviceTax: BigDecimal = BigDecimal.ZERO,
    basePrice: BigDecimal = BigDecimal.ZERO,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        BillItem(title = "Item Total", value = "₹${basePrice.toInt()}")
        
        if (discount > BigDecimal.ZERO) {
            Spacer(modifier = Modifier.height(8.dp))
            BillItem(
                title = "Discount", 
                value = "-₹${discount.toInt()}", 
                valueColor = Color(0xFF4CAF50)
            )
        }
        
        if (serviceTax > BigDecimal.ZERO) {
            Spacer(modifier = Modifier.height(8.dp))
            BillItem(title = "Taxes & Fee", value = "₹${serviceTax.toInt()}")
        }
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F1F1))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total Paid",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "₹${itemTotal.toInt()}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun BillItem(
    title: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
fun OtpVerificationCard(
    otp: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFF8E1),
        border = BorderStroke(1.dp, Color(0xFFFFE082))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Service Start OTP",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF57F17)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                otp.forEach { char ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = char.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Safeguard your service. Only share this code once the job is finished to your complete satisfaction.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

data class OrderProgressStep(
    val title: String,
    val date: String?,
    val description: String,
    val subData: String? = null,
    val isCompleted: Boolean,
    val isActive: Boolean
)

@Composable
fun BookingTimeline(
    status: String,
    createdAt: String,
    completedAt: String? = null,
    modifier: Modifier = Modifier
) {
    val upperStatus = status.uppercase()
    val isCancelled = upperStatus == "CANCELLED"
    
    val currentIndex = when {
        isCancelled -> 0
        upperStatus == "COMPLETED" -> 3
        upperStatus == "IN_PROGRESS" -> 2
        upperStatus == "ASSIGNED" || upperStatus == "CONFIRMED" -> 1
        else -> 0
    }

    val orderSteps = remember(status, createdAt, completedAt) {
        listOf(
            OrderProgressStep(
                title = "Booking Placed",
                date = createdAt.split("T").firstOrNull(),
                description = "Your booking request has been received and confirmed.",
                subData = if (isCancelled) "Status: Cancelled" else "Request Confirmed",
                isCompleted = !isCancelled,
                isActive = currentIndex == 0 && !isCancelled
            ),
            OrderProgressStep(
                title = if (currentIndex >= 1) "Professional Assigned" else "Assigning Professional",
                date = null,
                description = if (currentIndex >= 1)
                    "A certified service expert has been assigned to your order."
                else
                    "We're finding the best professional near your location.",
                subData = null,
                isCompleted = currentIndex > 1 || (currentIndex == 1 && upperStatus == "COMPLETED"),
                isActive = currentIndex == 1 && !isCancelled
            ),
            OrderProgressStep(
                title = "Service in Progress",
                date = null,
                description = "Our expert is currently performing your service at your address.",
                subData = null,
                isCompleted = currentIndex > 2 || upperStatus == "COMPLETED",
                isActive = currentIndex == 2 && !isCancelled
            ),
            OrderProgressStep(
                title = "Service Completed",
                date = completedAt?.split("T")?.firstOrNull(),
                description = "Your service has been completed successfully.",
                subData = completedAt,
                isCompleted = upperStatus == "COMPLETED",
                isActive = currentIndex == 3 && !isCancelled
            )
        )
    }

    val nodeYPositions = remember { mutableStateMapOf<Int, Float>() }

    var animationTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animationTriggered = true
    }

    val animProgress by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "orderTimelineProgress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "activeStepPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val successColor = Color(0xFF4CAF50)
    val activeColor = if (upperStatus == "COMPLETED") successColor else primaryColor
    val density = LocalDensity.current

    val nodeXPx = with(density) { 18.dp.toPx() }
    val nodeRadiusPx = with(density) { 12.dp.toPx() }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Canvas(
            modifier = Modifier.matchParentSize()
        ) {
            if (nodeYPositions.size >= orderSteps.size) {
                val minY = nodeYPositions[0] ?: 0f
                val maxY = nodeYPositions[orderSteps.size - 1] ?: 0f

                if (orderSteps.size > 1 && maxY > minY) {
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.35f),
                        start = Offset(nodeXPx, minY),
                        end = Offset(nodeXPx, maxY),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    val activeTargetY = nodeYPositions[currentIndex] ?: minY
                    val activeLineEndY = minY + (activeTargetY - minY) * animProgress

                    if (activeLineEndY > minY) {
                        drawLine(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    activeColor,
                                    activeColor.copy(alpha = 0.75f)
                                ),
                                startY = minY,
                                endY = maxY
                            ),
                            start = Offset(nodeXPx, minY),
                            end = Offset(nodeXPx, activeLineEndY),
                            strokeWidth = 3.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                orderSteps.indices.forEach { index ->
                    val nodeY = nodeYPositions[index] ?: return@forEach
                    val step = orderSteps[index]

                    if (step.isActive && !isCancelled) {
                        drawCircle(
                            color = activeColor.copy(alpha = pulseAlpha),
                            radius = nodeRadiusPx * pulseScale,
                            center = Offset(nodeXPx, nodeY)
                        )
                    }

                    val nodeFillColor = when {
                        step.isCompleted -> activeColor
                        step.isActive -> activeColor
                        else -> Color(0xFFCFD8DC)
                    }

                    drawCircle(
                        color = nodeFillColor,
                        radius = nodeRadiusPx,
                        center = Offset(nodeXPx, nodeY)
                    )

                    drawCircle(
                        color = Color.White,
                        radius = nodeRadiusPx,
                        center = Offset(nodeXPx, nodeY),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    if (step.isCompleted) {
                        val checkPath = Path().apply {
                            moveTo(nodeXPx - 4.dp.toPx(), nodeY)
                            lineTo(nodeXPx - 1.dp.toPx(), nodeY + 3.dp.toPx())
                            lineTo(nodeXPx + 4.dp.toPx(), nodeY - 3.dp.toPx())
                        }
                        drawPath(
                            path = checkPath,
                            color = Color.White,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    } else if (step.isActive) {
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = Offset(nodeXPx, nodeY)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            orderSteps.forEachIndexed { index, step ->
                val isLast = index == orderSteps.size - 1

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (isLast) 0.dp else 24.dp)
                        .onGloballyPositioned { coordinates ->
                            val nodeCenterOffsetY = with(density) { 10.dp.toPx() }
                            nodeYPositions[index] = coordinates.positionInParent().y + nodeCenterOffsetY
                        }
                ) {
                    Spacer(modifier = Modifier.width(38.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = step.title,
                                style = if (step.isActive) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                                fontWeight = if (step.isActive || step.isCompleted) FontWeight.Bold else FontWeight.Medium,
                                color = if (step.isActive || step.isCompleted) Color.Black else Color.Gray
                            )

                            if (step.date != null) {
                                Text(
                                    text = step.date,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = step.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (step.isActive || step.isCompleted) Color.DarkGray else Color.Gray.copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        )

                        if (!step.subData.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = step.subData,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancellationReasonSheet(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var selectedReason by remember { mutableStateOf("") }
    var customReason by remember { mutableStateOf("") }

    val reasons = listOf(
        "Changed my mind",
        "Found another service provider",
        "Scheduled for wrong time/date",
        "Price is too high",
        "Other"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            Text(
                text = "Cancel Booking",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please select a reason for cancellation",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            reasons.forEach { reason ->
                val isSelected = selectedReason == reason
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedReason = reason },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color(0xFFF8F9FA),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (selectedReason == "Other") {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = customReason,
                    onValueChange = { customReason = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Please specify your reason...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val finalReason = if (selectedReason == "Other") customReason else selectedReason
                    if (finalReason.isNotBlank()) {
                        onConfirm(finalReason)
                    }
                },
                enabled = selectedReason.isNotBlank() && (selectedReason != "Other" || customReason.isNotBlank()),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F)
                )
            ) {
                Text("Confirm Cancellation", fontWeight = FontWeight.Bold)
            }
        }
    }
}
