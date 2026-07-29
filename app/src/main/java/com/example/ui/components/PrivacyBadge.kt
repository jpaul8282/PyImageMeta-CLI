package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PrivacyStatus
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoseRed
import com.example.ui.theme.Slate800

@Composable
fun PrivacyBadge(
    status: PrivacyStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, textColor, label, icon) = when (status) {
        PrivacyStatus.EXPOSED_GPS_TAGGED -> Quintuple(
            RoseRed.copy(alpha = 0.2f),
            RoseRed,
            RoseRed,
            "GPS EMBEDDED",
            Icons.Default.GpsFixed
        )
        PrivacyStatus.MODIFIED_SANITIZED -> Quintuple(
            Slate800,
            Color(0xFF38BDF8),
            Color(0xFF38BDF8),
            "TAGS MODIFIED",
            Icons.Default.Shield
        )
        PrivacyStatus.SECURE_NO_GPS -> Quintuple(
            EmeraldGreen.copy(alpha = 0.2f),
            EmeraldGreen,
            EmeraldGreen,
            "SECURE (NO GPS)",
            Icons.Default.Lock
        )
    }

    Row(
        modifier = modifier
            .testTag("privacy_badge_${status.name}")
            .background(bgColor, RoundedCornerShape(20.dp))
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A, val second: B, val third: C, val fourth: D, val fifth: E
)
