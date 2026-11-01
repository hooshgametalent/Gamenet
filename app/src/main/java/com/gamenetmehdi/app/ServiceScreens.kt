package com.gamenetmehdi.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamenetmehdi.app.ui.VenueColors

@Composable
fun ServicesScreen(repository: AppRepository, navigator: AppNavigator) {
    val services = repository.services()
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(VenueColors.Slate950),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { DetailHeader("خدمات فنی", navigator::back) }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeading("خدمات قابل ارائه", "جزئیات نمونه و مراکز ارائه‌دهنده")
            }
        }
        items(services, key = { it.id }) { service ->
            Column(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().panel()
                    .clickable { navigator.open(AppRoute.ServiceDetail(service.id)) }.padding(16.dp),
            ) {
                Text(service.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                VerticalGap(5)
                Text(service.shortDescription, color = VenueColors.Slate400)
                VerticalGap()
                TagList(listOf("${service.venueIds.size} مرکز ارائه‌دهنده"))
            }
        }
    }
}

@Composable
fun ServiceDetailScreen(repository: AppRepository, serviceId: String, navigator: AppNavigator) {
    val service = repository.service(serviceId)
    if (service == null) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) { DetailHeader("خدمت پیدا نشد", navigator::back) }
        return
    }
    val venues = service.venueIds.mapNotNull(repository::venue)
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(VenueColors.Slate950),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { DetailHeader(service.title, navigator::back) }
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().panel(24.dp).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(service.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 26.sp)
                Text(service.description, color = VenueColors.Slate300, lineHeight = 24.sp)
                SampleNotice("شرح، هزینه و زمان انجام خدمت در این نسخه نمونه است و تعهد خدماتی ایجاد نمی‌کند.")
            }
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().panel().padding(16.dp)) {
                SectionHeading("مراحل دریافت خدمت")
                VerticalGap()
                service.steps.forEachIndexed { index, step ->
                    Text("${index + 1}. $step", color = Color.White, modifier = Modifier.padding(vertical = 6.dp))
                }
            }
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeading("مراکز ارائه‌دهنده", "برای دیدن اطلاعات تماس، مرکز را انتخاب کنید")
            }
        }
        items(venues, key = { it.id }) { venue ->
            VenueSummaryCard(venue, Modifier.padding(horizontal = 16.dp)) {
                navigator.open(AppRoute.VenueDetail(venue.id))
            }
        }
    }
}
