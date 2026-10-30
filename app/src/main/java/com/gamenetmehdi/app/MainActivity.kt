package com.gamenetmehdi.app

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamenetmehdi.app.ui.VenueColors
import com.gamenetmehdi.app.ui.VenueTheme

const val APP_NAME = "استعداد بازی"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContent {
            VenueApp(golbargVenue)
        }
    }
}

@Composable
private fun VenueApp(venue: Venue) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        VenueTheme {
            VenueScreen(venue)
        }
    }
}

@Composable
private fun VenueScreen(venue: Venue) {
    val bottomInset = WindowInsets.safeDrawing
        .only(WindowInsetsSides.Bottom)
        .asPaddingValues()
        .calculateBottomPadding()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VenueColors.Slate950)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
            ),
        contentPadding = PaddingValues(bottom = 32.dp + bottomInset),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { BrandBar() }
        item { HeroHeader(venue) }
        item { StatsGrid(venue) }
        item { SectionTitle("اطلاعات، خدمات و امکانات مجموعه") }
        item { ContactCard(venue) }
        item { ChipsCard(title = "خدمات مجموعه", chips = venue.services) }
        item { ChipsCard(title = "امکانات رفاهی", chips = venue.amenities) }
        item {
            SectionTitle(
                title = "اتاق\u200cها و دستگاه\u200cها",
                subtitle = "نمایش دستگاه\u200cهای هر اتاق به همراه وضعیت فعالیت آن\u200cها.",
                caption = "وضعیت دستگاه\u200cها یک تصویر لحظه\u200cای است و زنده به\u200cروزرسانی نمی\u200cشود.",
            )
        }
        items(venue.rooms) { room -> RoomCard(room) }
    }
}

@Composable
private fun BrandBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(VenueColors.Slate900.copy(alpha = 0.6f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(VenueColors.Emerald500),
            contentAlignment = Alignment.Center,
        ) {
            Text("🎮", fontSize = 16.sp)
        }
        Spacer(Modifier.size(10.dp))
        Text(
            text = APP_NAME,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeroHeader(venue: Venue) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(VenueColors.Slate900)
            .background(
                Brush.radialGradient(
                    colors = listOf(VenueColors.Emerald500.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = 900f,
                ),
            )
            .border(1.dp, VenueColors.Slate800, RoundedCornerShape(24.dp))
            .padding(20.dp),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Pill(
                text = venue.badge,
                background = VenueColors.Emerald500.copy(alpha = 0.15f),
                textColor = VenueColors.Emerald200,
                borderColor = VenueColors.Emerald400.copy(alpha = 0.3f),
            )
            Pill(
                text = venue.location,
                background = Color.White.copy(alpha = 0.05f),
                textColor = VenueColors.Slate300,
                borderColor = VenueColors.Slate700,
            )
            Pill(
                text = "${venue.activeDeviceCount} دستگاه فعال",
                background = Color.White.copy(alpha = 0.05f),
                textColor = VenueColors.Slate300,
                borderColor = VenueColors.Slate700,
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = venue.name,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 32.sp,
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { openUri(context, "tel:${venue.phone}", Intent.ACTION_DIAL) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VenueColors.Emerald500,
                contentColor = VenueColors.Slate950,
            ),
        ) {
            Text("تماس با مجموعه", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = {
                val geo = "geo:${venue.latitude},${venue.longitude}?q=${venue.latitude},${venue.longitude}"
                if (!openUri(context, geo, Intent.ACTION_VIEW, showError = false)) {
                    openUri(
                        context,
                        "https://www.google.com/maps/search/?api=1&query=${venue.latitude},${venue.longitude}",
                        Intent.ACTION_VIEW,
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, VenueColors.Cyan400.copy(alpha = 0.4f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VenueColors.Cyan400),
        ) {
            Text("مشاهده مسیر روی نقشه", fontWeight = FontWeight.Bold)
        }
    }
}

private fun openUri(context: Context, uri: String, action: String, showError: Boolean = true): Boolean {
    return try {
        context.startActivity(Intent(action, Uri.parse(uri)))
        true
    } catch (e: ActivityNotFoundException) {
        if (showError) {
            Toast.makeText(context, "برنامه\u200cای برای این کار پیدا نشد", Toast.LENGTH_SHORT).show()
        }
        false
    }
}

@Composable
private fun StatsGrid(venue: Venue) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("اتاق\u200cها", venue.roomCount.toString(), Modifier.weight(1f))
            StatCard("دستگاه\u200cها", venue.deviceCount.toString(), Modifier.weight(1f))
        }
        StatCard("آدرس", venue.address, Modifier.fillMaxWidth(), valueSize = 16)
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier, valueSize: Int = 26) {
    Column(
        modifier = modifier
            .cardStyle()
            .padding(16.dp),
    ) {
        Text(label, color = VenueColors.Slate400, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Black, fontSize = valueSize.sp)
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String? = null, caption: String? = null) {
    Column(modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp)) {
        Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = VenueColors.Slate400, fontSize = 13.sp)
        }
        if (caption != null) {
            Spacer(Modifier.height(6.dp))
            Text(caption, color = VenueColors.Slate400.copy(alpha = 0.8f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun ContactCard(venue: Venue) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .cardStyle()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CardTitle("جزئیات تماس و موقعیت")
        InfoRow("تلفن", venue.phone)
        InfoRow("شهر/استان", venue.location)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VenueColors.Slate950.copy(alpha = 0.6f))
            .border(1.dp, VenueColors.Slate800, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = VenueColors.Slate400, fontSize = 13.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipsCard(title: String, chips: List<String>) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .cardStyle()
            .padding(16.dp),
    ) {
        CardTitle(title)
        Spacer(Modifier.height(12.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            chips.forEach { chip ->
                Pill(
                    text = chip,
                    background = VenueColors.Emerald500.copy(alpha = 0.10f),
                    textColor = VenueColors.Emerald200,
                    borderColor = VenueColors.Emerald400.copy(alpha = 0.25f),
                )
            }
        }
    }
}

@Composable
private fun CardTitle(text: String) {
    Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoomCard(room: Room) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .cardStyle()
            .padding(16.dp),
    ) {
        Text(room.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${room.devices.size} دستگاه، ${room.activeDeviceCount} فعال",
            color = VenueColors.Slate400,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(10.dp))
        Pill(
            text = "${room.exclusiveGames} بازی اختصاصی در این اتاق",
            background = VenueColors.Emerald500.copy(alpha = 0.15f),
            textColor = VenueColors.Emerald200,
            borderColor = VenueColors.Emerald400.copy(alpha = 0.3f),
        )
        Spacer(Modifier.height(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            room.devices.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    pair.forEach { device ->
                        DeviceCard(device, Modifier.weight(1f))
                    }
                    if (pair.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DeviceCard(device: Device, modifier: Modifier = Modifier) {
    val accent = if (device.inUse) VenueColors.Red500 else VenueColors.Emerald500
    val borderColor = if (device.inUse) VenueColors.Red300 else VenueColors.Emerald300
    val pillText = if (device.inUse) VenueColors.Red100 else VenueColors.Emerald100
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(accent.copy(alpha = 0.15f))
            .border(1.dp, borderColor.copy(alpha = 0.3f), shape)
            .padding(12.dp),
    ) {
        Text(device.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(Modifier.height(2.dp))
        Text(device.type, color = Color.White.copy(alpha = 0.65f), fontSize = 12.sp)
        Spacer(Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (device.activeInSystem) {
                Pill(
                    text = "فعال در سیستم",
                    background = Color.White.copy(alpha = 0.10f),
                    textColor = Color.White.copy(alpha = 0.70f),
                    fontSize = 11,
                )
            }
            Pill(
                text = if (device.inUse) "در حال استفاده" else "آزاد برای استفاده",
                background = accent.copy(alpha = 0.20f),
                textColor = pillText,
                fontSize = 11,
            )
        }
    }
}

@Composable
private fun Pill(
    text: String,
    background: Color,
    textColor: Color,
    borderColor: Color? = null,
    fontSize: Int = 12,
) {
    var modifier = Modifier
        .clip(CircleShape)
        .background(background)
    if (borderColor != null) {
        modifier = modifier.border(1.dp, borderColor, CircleShape)
    }
    Box(modifier = modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(text, color = textColor, fontSize = fontSize.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun Modifier.cardStyle(radius: Dp = 16.dp): Modifier {
    val shape = RoundedCornerShape(radius)
    return this
        .clip(shape)
        .background(VenueColors.Slate900.copy(alpha = 0.8f))
        .border(1.dp, VenueColors.Slate800, shape)
}

@Preview(showBackground = true, backgroundColor = 0xFF020617, heightDp = 2000)
@Composable
private fun VenuePreview() {
    VenueApp(golbargVenue)
}
