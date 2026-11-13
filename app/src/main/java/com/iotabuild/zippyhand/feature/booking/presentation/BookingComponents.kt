package com.iotabuild.zippyhand.feature.booking.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iotabuild.zippyhand.feature.booking.domain.MyOrderResponse

@Composable
fun StatusBadge(status: String) {
    val (backgroundColor, textColor, text) = when (status) {
        "COMPLETED" -> Triple(
            Color(0xFFE3F2FD),
            Color(0xFF2196F3),
            "COMPLETED"
        )
        "CANCELLED" -> Triple(
            Color(0xFFFFEBEE),
            Color(0xFFF44336),
            "CANCELLED"
        )
        "PENDING" -> Triple(
            Color(0xFFFFF3E0),
            Color(0xFFFF9800),
            "PENDING"
        )
        "CREATED" -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF4CAF50),
            "CREATED"
        )
        "CONFIRMED", "ASSIGNED" -> Triple(
            Color(0xFFE1F5FE),
            Color(0xFF00BCD4),
            "CONFIRMED"
        )
        "IN_PROGRESS" -> Triple(
            Color(0xFFF3E5F5),
            Color(0xFF9C27B0),
            "IN PROGRESS"
        )
        else -> Triple(
            Color(0xFFF5F5F5),
            Color.Gray,
            status.replace("_", " ")
        )
    }

    Box(
        modifier = Modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BookingCard(
    booking: MyOrderResponse,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = {
            onCardClick()
        },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = booking.status)
                PaymentStatusBadge(paymentStatus = booking.paymentStatus)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                 Column(modifier = Modifier.weight(1f)) {
                     Text(
                        text = booking.productTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                     Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = booking.orderId,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = booking.scheduledAt ?: "Date not scheduled",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                 }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${booking.totalAmount}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            booking.serviceAddress?.let { address ->
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${address.addressLine1}, ${address.city}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        maxLines = 1
                    )
                }
            }

        }
    }
}

@Composable
fun PaymentStatusBadge(paymentStatus: String) {
    val (backgroundColor, textColor, text) = when (paymentStatus) {
        "PAID" -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF4CAF50),
            "PAID"
        )
        "PENDING" -> Triple(
            Color(0xFFFFF3E0),
            Color(0xFFFF9800),
            "PENDING"
        )
        "FAILED" -> Triple(
            Color(0xFFFFEBEE),
            Color(0xFFF44336),
            "FAILED"
        )
        else -> Triple(
            Color(0xFFF5F5F5),
            Color.Gray,
            paymentStatus
        )
    }

    Box(
        modifier = Modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
