package com.gamenetmehdi.app

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamenetmehdi.app.ui.VenueColors

@Composable
fun VenuesScreen(repository: AppRepository, navigator: AppNavigator) {
    var filter by remember { mutableStateOf(VenueFilter()) }
    val venues = repository.venues(filter)
    fun update(next: VenueFilter) { filter = next }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(VenueColors.Slate950),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { BrandHeader("فهرست گیمنت‌ها") }
        item {
            SectionHeading("گیمنت‌ها", "مراکز بازی و خدمات فنی")
            VerticalGap()
            OutlinedTextField(
                value = filter.query,
                onValueChange = { update(filter.copy(query = it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("جست‌وجوی نام، شهر یا آدرس") },
            )
        }
        item {
            Column(modifier = Modifier.fillMaxWidth().panel().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterOptions("استان", repository.provinces(), filter.province, onSelect = { update(filter.copy(province = it)) })
                FilterOptions("شهر", repository.cities(), filter.city, onSelect = { update(filter.copy(city = it)) })
                FilterOptions("خدمت", repository.venueServices(), filter.service, onSelect = { update(filter.copy(service = it)) })
                FilterOptions("امکانات", repository.amenities(), filter.amenity, onSelect = { update(filter.copy(amenity = it)) })
                OutlinedButton(onClick = { update(VenueFilter()) }) { Text("پاک کردن همه فیلترها") }
            }
        }
        item { Text("${venues.size} گیمنت پیدا شد", color = VenueColors.Slate400, fontSize = 12.sp) }
        if (venues.isEmpty()) {
            item { EmptyState("نام یا گزینه‌های فیلتر را تغییر دهید.") { update(VenueFilter()) } }
        } else {
            items(venues, key = { it.id }) { venue ->
                VenueSummaryCard(venue) { navigator.open(AppRoute.VenueDetail(venue.id)) }
            }
        }
    }
}

@Composable
fun VenueDetailScreen(repository: AppRepository, venueId: String, navigator: AppNavigator) {
    val venue = repository.venue(venueId)
    if (venue == null) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) { DetailHeader("گیمنت پیدا نشد", navigator::back) }
        return
    }
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(VenueColors.Slate950),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { DetailHeader(venue.name, navigator::back) }
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().panel(24.dp).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TagList(listOf(venue.badge, venue.location, "${venue.activeDeviceCount} دستگاه فعال"))
                Text(venue.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 29.sp)
                Text(venue.address, color = VenueColors.Slate300)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { openExternalUri(context, "tel:${venue.phone}", Intent.ACTION_DIAL) },
                        modifier = Modifier.weight(1f),
                    ) { Text("تماس") }
                    OutlinedButton(onClick = { openVenueMap(context, venue) }, modifier = Modifier.weight(1f)) { Text("مسیریابی") }
                }
            }
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().panel().padding(16.dp)) {
                SectionHeading("اطلاعات مجموعه")
                InfoLine("تلفن", venue.phone)
                InfoLine("استان و شهر", venue.location)
                InfoLine("اتاق‌ها", venue.roomCount.toString())
                InfoLine("دستگاه‌ها", venue.deviceCount.toString())
            }
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().panel().padding(16.dp)) {
                SectionHeading("خدمات و امکانات")
                VerticalGap()
                Text("خدمات", color = VenueColors.Slate400, fontSize = 12.sp)
                VerticalGap(5)
                TagList(venue.services)
                VerticalGap(12)
                Text("امکانات رفاهی", color = VenueColors.Slate400, fontSize = 12.sp)
                VerticalGap(5)
                TagList(venue.amenities, VenueColors.Cyan400)
            }
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeading("اتاق‌ها و دستگاه‌ها", "وضعیت ثبت‌شده در داده نمونه")
                SampleNotice("وضعیت آزاد یا درحال‌استفاده زنده نیست و فقط برای نمایش رابط کاربری است.")
            }
        }
        items(venue.rooms, key = { it.name }) { room -> RoomDetailCard(room) }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().panel().padding(16.dp)) {
                SectionHeading("بازی‌های ویژه مجموعه")
                VerticalGap()
                if (venue.uniqueGames.isEmpty()) Text("بازی ویژه‌ای ثبت نشده است.", color = VenueColors.Slate400)
                else TagList(venue.uniqueGames, VenueColors.Violet500)
            }
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().panel().padding(16.dp)) {
                SectionHeading("تعرفه دستگاه‌ها")
                VerticalGap()
                SampleNotice("مبالغ نمونه و غیرقابل استناد هستند. قیمت نهایی را از مجموعه بپرسید.")
                VerticalGap()
                venue.tariffs.forEach { tariff -> InfoLine(tariff.deviceType, tariff.hourlyPrice) }
            }
        }
    }
}

@Composable
private fun RoomDetailCard(room: Room) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().panel().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Text(room.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text("${room.devices.size} دستگاه، ${room.activeDeviceCount} فعال", color = VenueColors.Slate400, fontSize = 12.sp)
        room.devices.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { device -> DeviceStatus(device, Modifier.weight(1f)) }
                if (pair.size == 1) androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DeviceStatus(device: Device, modifier: Modifier) {
    val accent = if (device.inUse) VenueColors.Red500 else VenueColors.Emerald500
    Column(modifier = modifier.panel(13.dp).background(accent.copy(alpha = .1f)).padding(10.dp)) {
        Text(device.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(device.type, color = VenueColors.Slate400, fontSize = 11.sp)
        Text(if (device.inUse) "در حال استفاده" else "آزاد", color = accent, fontSize = 11.sp)
    }
}
