package com.furkan.kilertakipp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

@Composable
fun PantryCard(
    item: PantryItem,
    onDelete: () -> Unit
) {
    val days = daysLeft(item.expiryDate)

    val cardColor = when {
        days < 0 -> Color(0xFFFFEBEE)   // kırmızımsı
        days <= 3 -> Color(0xFFFFF8E1)  // sarı
        else -> Color.White
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "SKT: ${item.expiryDate}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = expiryText(days),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        days < 0 -> Color.Red
                        days <= 3 -> Color(0xFFF57C00)
                        else -> Color(0xFF2E7D32)
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                AssistChip(
                    onClick = {},
                    label = { Text(item.category) },
                    leadingIcon = {
                        Icon(Icons.Default.Category, null)
                    }
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    null,
                    tint = Color.Red
                )
            }
        }
    }
}


fun daysLeft(date: String): Int {
    return try {
        val parts = date.split("/")
        val day = parts[0].toInt()
        val month = parts[1].toInt() - 1
        val year = parts[2].toInt()

        val expiry = Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
        }

        val today = Calendar.getInstance()

        ((expiry.timeInMillis - today.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
    } catch (e: Exception) {
        9999
    }
}

fun expiryText(days: Int): String =
    when {
        days < 0 -> "Süresi geçti"
        days == 0 -> "Bugün bitiyor"
        else -> "$days gün kaldı"
    }



