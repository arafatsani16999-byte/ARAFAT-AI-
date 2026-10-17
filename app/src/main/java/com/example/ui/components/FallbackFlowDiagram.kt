package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GeminiBluePrimary
import com.example.ui.theme.StatusNeutral
import com.example.ui.theme.StatusSuccess

@Composable
fun FallbackFlowDiagram(
    k1Configured: Boolean,
    k2Configured: Boolean,
    k3Configured: Boolean,
    modifier: Modifier = Modifier
) {
    val configuredCount = listOf(k1Configured, k2Configured, k3Configured).count { it }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = GeminiBluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "3-Key Fallback Architecture",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (configuredCount > 0) StatusSuccess.copy(alpha = 0.15f)
                    else StatusNeutral.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$configuredCount of 3 Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (configuredCount > 0) StatusSuccess else StatusNeutral,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3-step pipeline flow
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                KeyNode(
                    slot = 1,
                    title = "Key 1",
                    role = "Primary",
                    isConfigured = k1Configured,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(16.dp)
                        .padding(horizontal = 2.dp)
                )

                KeyNode(
                    slot = 2,
                    title = "Key 2",
                    role = "Fallback 1",
                    isConfigured = k2Configured,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(16.dp)
                        .padding(horizontal = 2.dp)
                )

                KeyNode(
                    slot = 3,
                    title = "Key 3",
                    role = "Fallback 2",
                    isConfigured = k3Configured,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic rule explanation
            val policyDescription = when (configuredCount) {
                0 -> "No keys configured. Gemini features will show: \"Gemini API key is not configured.\""
                1 -> "1 key configured. The app will use this key for all Gemini API requests."
                2 -> "2 keys configured. If the first configured key encounters rate limits or errors, the app automatically fails over to the second."
                else -> "All 3 keys configured! Maximum resilience with automatic three-tier failover."
            }

            Text(
                text = policyDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun KeyNode(
    slot: Int,
    title: String,
    role: String,
    isConfigured: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .border(
                1.dp,
                if (isConfigured) StatusSuccess.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(10.dp)
            ),
        shape = RoundedCornerShape(10.dp),
        color = if (isConfigured) StatusSuccess.copy(alpha = 0.08f)
        else MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        if (isConfigured) StatusSuccess else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isConfigured) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isConfigured) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = if (isConfigured) role else "Not config",
                fontSize = 10.sp,
                color = if (isConfigured) StatusSuccess else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
