package com.gamenetmehdi.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamenetmehdi.app.ui.VenueColors

@Composable
fun HomeScreen(repository: AppRepository, navigator: AppNavigator) {
    val content = repository.home()
    val venue = repository.venue(content.featuredVenueId)
    val services = content.serviceIds.mapNotNull(repository::service)
    val games = content.popularGameIds.mapNotNull(repository::game)
    val search = navigator.homeGameQuery

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(VenueColors.Slate950),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { BrandHeader("حالت نمونه آفلاین") }
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().panel(24.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(VenueColors.Emerald500.copy(alpha = 0.28f), VenueColors.Violet500.copy(alpha = 0.15f)),
                        ),
                    ).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(content.headline, color = Color.White, fontWeight = FontWeight.Black, fontSize = 28.sp, lineHeight = 38.sp)
                Text(content.subtitle, color = VenueColors.Slate300)
                OutlinedTextField(
                    value = search,
                    onValueChange = { navigator.homeGameQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("جست‌وجوی بازی") },
                    placeholder = { Text("مثلاً تکن ۸") },
                )
                PrimaryAction("مشاهده بازی‌ها") { navigator.openGames(search) }
            }
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeading("آمار پلتفرم‌ها", "نمونه‌ای از بازی‌های ثبت‌شده")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    content.platformStatistics.forEach { statistic ->
                        Column(modifier = Modifier.weight(1f).panel().padding(12.dp)) {
                            Text(statistic.gameCount.toString(), color = VenueColors.Emerald300, fontWeight = FontWeight.Black, fontSize = 22.sp)
                            Text(statistic.platform, color = VenueColors.Slate300, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        if (venue != null) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeading("گیمنت منتخب", action = "همه گیمنت‌ها") { navigator.openMain(MainDestination.VENUES) }
                    VenueSummaryCard(venue) { navigator.open(AppRoute.VenueDetail(venue.id)) }
                }
            }
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeading("خدمات فنی", "خدمات قابل ارائه در مراکز", "همه خدمات") {
                    navigator.open(AppRoute.Services)
                }
            }
        }
        items(services, key = { it.id }) { service ->
            Column(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().panel()
                    .clickable { navigator.open(AppRoute.ServiceDetail(service.id)) }.padding(16.dp),
            ) {
                Text(service.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp)
                VerticalGap(5)
                Text(service.shortDescription, color = VenueColors.Slate400, fontSize = 13.sp)
                TextButton(onClick = { navigator.open(AppRoute.ServiceDetail(service.id)) }) { Text("جزئیات خدمت") }
            }
        }
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().panel()
                    .background(VenueColors.Cyan400.copy(alpha = 0.08f)).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("گیمنت‌های اطراف روی نقشه", color = Color.White, fontWeight = FontWeight.Black, fontSize = 19.sp)
                Text("در نسخه نمونه، نقشه و نشانگرها کاملاً آفلاین نمایش داده می‌شوند.", color = VenueColors.Slate300)
                PrimaryAction("باز کردن نقشه نمونه") { navigator.openMain(MainDestination.MAP) }
            }
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeading("بازی‌های محبوب", action = "فهرست بازی‌ها") { navigator.openMain(MainDestination.GAMES) }
            }
        }
        items(games, key = { it.id }) { game ->
            GameSummaryCard(game, Modifier.padding(horizontal = 16.dp)) {
                navigator.open(AppRoute.GameDetail(game.id))
            }
        }
    }
}

@Composable
fun GameSummaryCard(game: Game, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth().panel().clickable(onClick = onClick).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(game.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text(game.latinTitle, color = VenueColors.Slate400, fontSize = 12.sp)
            }
            Text("★ ${game.rating}", color = VenueColors.Amber400, fontWeight = FontWeight.Bold)
        }
        TagList(game.platforms + game.genres, VenueColors.Violet500)
        Text("${game.releaseYear}  •  رده سنی ${game.ageRating}", color = VenueColors.Slate400, fontSize = 12.sp)
    }
}

@Composable
fun VenueSummaryCard(venue: Venue, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth().panel().clickable(onClick = onClick).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(venue.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text(venue.badge, color = VenueColors.Emerald300, fontSize = 12.sp)
        }
        Text(venue.location, color = VenueColors.Slate300)
        Text(venue.address, color = VenueColors.Slate400, fontSize = 12.sp)
        TagList(listOf("${venue.roomCount} اتاق", "${venue.deviceCount} دستگاه") + venue.deviceTypes)
    }
}
