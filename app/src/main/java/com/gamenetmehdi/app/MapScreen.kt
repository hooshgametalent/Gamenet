package com.gamenetmehdi.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamenetmehdi.app.ui.VenueColors

@Composable
fun MapScreen(repository: AppRepository, navigator: AppNavigator) {
    val province = navigator.mapProvince
    val layer = navigator.mapLayer
    val venues = repository.venues(VenueFilter(province = province))
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(VenueColors.Slate950),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { BrandHeader("نقشه آفلاین") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeading("نقشه گیمنت‌ها", "نشانگرها از داده‌های نمونه بارگذاری شده‌اند")
                SampleNotice("موقعیت زنده در حالت نمونه در دسترس نیست و هیچ مجوز مکانی درخواست نمی‌شود.")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repository.provinces().forEach { item ->
                        FilterChip(
                            selected = province == item,
                            onClick = { navigator.mapProvince = if (province == item) null else item },
                            label = { Text(item) },
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ساده", "تیره").forEach { item ->
                        FilterChip(selected = layer == item, onClick = { navigator.mapLayer = item }, label = { Text("لایه $item") })
                    }
                }
            }
        }
        item {
            Box(
                modifier = Modifier.fillMaxWidth().height(280.dp).panel(22.dp).background(
                    if (layer == "تیره") Brush.linearGradient(listOf(VenueColors.Slate800, VenueColors.Slate950))
                    else Brush.linearGradient(listOf(Color(0xFF164E63), Color(0xFF14532D))),
                ),
            ) {
                Text("نمای شماتیک آفلاین", modifier = Modifier.align(Alignment.TopCenter).padding(14.dp), color = VenueColors.Slate300)
                venues.forEachIndexed { index, venue ->
                    Column(
                        modifier = Modifier.align(if (index % 2 == 0) Alignment.Center else Alignment.BottomEnd)
                            .offset(x = (-index * 18).dp, y = (-index * 28).dp)
                            .clickable { navigator.open(AppRoute.VenueDetail(venue.id)) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(modifier = Modifier.background(VenueColors.Emerald500, CircleShape).padding(10.dp)) { Text("⌖", color = VenueColors.Slate950) }
                        Text(venue.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
        item {
            OutlinedButton(onClick = { }, enabled = false, modifier = Modifier.fillMaxWidth()) {
                Text("موقعیت فعلی در نسخه نمونه غیرفعال است")
            }
        }
        item { SectionHeading("گیمنت‌های روی نقشه", "${venues.size} نشانگر نمونه") }
        items(venues, key = { it.id }) { venue ->
            VenueSummaryCard(venue) { navigator.open(AppRoute.VenueDetail(venue.id)) }
        }
    }
}
