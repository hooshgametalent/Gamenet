package com.gamenetmehdi.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamenetmehdi.app.ui.VenueColors

fun Modifier.panel(radius: Dp = 18.dp): Modifier {
    val shape = RoundedCornerShape(radius)
    return clip(shape)
        .background(VenueColors.Slate900)
        .border(1.dp, VenueColors.Slate800, shape)
}

@Composable
fun BrandHeader(subtitle: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(VenueColors.Emerald500).padding(7.dp),
            contentAlignment = Alignment.Center,
        ) { Text("🎮", fontSize = 16.sp) }
        Spacer(Modifier.padding(5.dp))
        Column {
            Text(APP_NAME, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            if (subtitle != null) Text(subtitle, color = VenueColors.Slate400, fontSize = 11.sp)
        }
    }
}

@Composable
fun DetailHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OutlinedButton(
            onClick = onBack,
            shape = CircleShape,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            border = BorderStroke(1.dp, VenueColors.Slate700),
        ) { Text("→", fontSize = 18.sp) }
        Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
    }
}

@Composable
fun SectionHeading(title: String, subtitle: String? = null, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            if (subtitle != null) Text(subtitle, color = VenueColors.Slate400, fontSize = 12.sp)
        }
        if (action != null && onAction != null) {
            Text(
                text = action,
                color = VenueColors.Emerald300,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clip(CircleShape).clickable(onClick = onAction).padding(8.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagList(tags: List<String>, accent: Color = VenueColors.Emerald500) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        tags.forEach { tag ->
            Box(
                modifier = Modifier.clip(CircleShape)
                    .background(accent.copy(alpha = 0.14f))
                    .border(1.dp, accent.copy(alpha = 0.35f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(tag, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> FilterOptions(
    label: String,
    values: List<T>,
    selected: T?,
    valueLabel: (T) -> String = { it.toString() },
    onSelect: (T?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, color = VenueColors.Slate300, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("همه") })
            values.forEach { value ->
                FilterChip(
                    selected = value == selected,
                    onClick = { onSelect(if (value == selected) null else value) },
                    label = { Text(valueLabel(value)) },
                )
            }
        }
    }
}

@Composable
fun PrimaryAction(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VenueColors.Emerald500, contentColor = VenueColors.Slate950),
    ) { Text(text, fontWeight = FontWeight.Black) }
}

@Composable
fun EmptyState(message: String, onReset: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().panel().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("نتیجه‌ای پیدا نشد", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text(message, color = VenueColors.Slate400)
        OutlinedButton(onClick = onReset) { Text("پاک کردن فیلترها") }
    }
}

@Composable
fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = VenueColors.Slate400, fontSize = 13.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun SampleNotice(text: String = "اطلاعات این بخش نمونه است و ممکن است با وضعیت لحظه‌ای تفاوت داشته باشد.") {
    Text(
        text = text,
        color = VenueColors.Amber400,
        fontSize = 11.sp,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(VenueColors.Amber400.copy(alpha = 0.1f)).padding(10.dp),
    )
}

@Composable
fun VerticalGap(height: Int = 8) = Spacer(Modifier.height(height.dp))
