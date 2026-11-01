package com.gamenetmehdi.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamenetmehdi.app.ui.VenueColors
import kotlin.math.ceil

private enum class CatalogLayout { LIST, GRID }

@Composable
fun GamesScreen(repository: AppRepository, navigator: AppNavigator) {
    var filter by remember { mutableStateOf(GameFilter(query = navigator.initialGameQuery)) }
    var layout by remember { mutableStateOf(CatalogLayout.LIST) }
    var page by remember { mutableIntStateOf(0) }
    val results = repository.games(filter)
    val pageSize = 3
    val pageCount = maxOf(1, ceil(results.size / pageSize.toDouble()).toInt())
    if (page >= pageCount) page = pageCount - 1
    val visibleGames = results.drop(page * pageSize).take(pageSize)
    fun update(next: GameFilter) { filter = next; page = 0 }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(VenueColors.Slate950),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { BrandHeader("فهرست بازی‌ها") }
        item {
            SectionHeading("بازی‌ها", "جست‌وجو و فیلتر روی داده‌های نمونه")
            VerticalGap()
            OutlinedTextField(
                value = filter.query,
                onValueChange = { update(filter.copy(query = it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("نام فارسی یا انگلیسی بازی") },
            )
        }
        item {
            Column(modifier = Modifier.fillMaxWidth().panel().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterOptions("پلتفرم", repository.platforms(), filter.platform, onSelect = { update(filter.copy(platform = it)) })
                FilterOptions("سبک", repository.genres(), filter.genre, onSelect = { update(filter.copy(genre = it)) })
                FilterOptions("سال انتشار", repository.years(), filter.year, onSelect = { update(filter.copy(year = it)) })
                FilterOptions(
                    "حداقل امتیاز",
                    listOf(4.0, 4.5),
                    filter.minimumRating,
                    valueLabel = { "$it به بالا" },
                    onSelect = { update(filter.copy(minimumRating = it)) },
                )
                FilterOptions(
                    "مرتب‌سازی",
                    GameSort.entries,
                    filter.sort,
                    valueLabel = { it.label },
                    onSelect = { update(filter.copy(sort = it ?: GameSort.RECOMMENDED)) },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { layout = CatalogLayout.LIST }) { Text(if (layout == CatalogLayout.LIST) "✓ فهرست" else "فهرست") }
                    OutlinedButton(onClick = { layout = CatalogLayout.GRID }) { Text(if (layout == CatalogLayout.GRID) "✓ شبکه" else "شبکه") }
                    OutlinedButton(onClick = { update(GameFilter()) }) { Text("بازنشانی") }
                }
            }
        }
        item { Text("${results.size} بازی پیدا شد", color = VenueColors.Slate400, fontSize = 12.sp) }
        if (results.isEmpty()) {
            item { EmptyState("ترکیب جست‌وجو و فیلترها را تغییر دهید.") { update(GameFilter()) } }
        } else if (layout == CatalogLayout.LIST) {
            items(visibleGames, key = { it.id }) { game ->
                GameSummaryCard(game) { navigator.open(AppRoute.GameDetail(game.id)) }
            }
        } else {
            items(visibleGames.chunked(2)) { rowGames ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowGames.forEach { game ->
                        CompactGameCard(game, Modifier.weight(1f)) { navigator.open(AppRoute.GameDetail(game.id)) }
                    }
                    if (rowGames.size == 1) Box(Modifier.weight(1f))
                }
            }
        }
        if (results.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(onClick = { if (page > 0) page-- }, enabled = page > 0) { Text("قبلی") }
                    Text("صفحه ${page + 1} از $pageCount", color = VenueColors.Slate300)
                    OutlinedButton(onClick = { if (page + 1 < pageCount) page++ }, enabled = page + 1 < pageCount) { Text("بعدی") }
                }
            }
        }
    }
}

@Composable
private fun CompactGameCard(game: Game, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.height(180.dp).panel().clickable(onClick = onClick).padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(70.dp).background(
                Brush.linearGradient(listOf(VenueColors.Violet500.copy(alpha = .6f), VenueColors.Cyan400.copy(alpha = .25f))),
                RoundedCornerShape(12.dp),
            ),
            contentAlignment = Alignment.Center,
        ) { Text("🎮", fontSize = 30.sp) }
        Text(game.title, color = Color.White, fontWeight = FontWeight.Black)
        Text("★ ${game.rating}  •  ${game.releaseYear}", color = VenueColors.Amber400, fontSize = 11.sp)
    }
}

@Composable
fun GameDetailScreen(repository: AppRepository, gameId: String, navigator: AppNavigator) {
    val game = repository.game(gameId)
    if (game == null) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            DetailHeader("بازی پیدا نشد", navigator::back)
        }
        return
    }
    val related = game.relatedGameIds.mapNotNull(repository::game)
    val venues = game.venueIds.mapNotNull(repository::venue)
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(VenueColors.Slate950),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { DetailHeader("جزئیات بازی", navigator::back) }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().panel(24.dp).padding(18.dp)) {
                Text(game.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 30.sp)
                Text(game.latinTitle, color = VenueColors.Slate400)
                VerticalGap(12)
                TagList(game.platforms + game.genres, VenueColors.Violet500)
                VerticalGap(12)
                InfoLine("سال انتشار", game.releaseYear.toString())
                InfoLine("رده سنی", game.ageRating)
                InfoLine("امتیاز نمونه", "${game.rating} از ۵")
            }
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeading("گالری")
                VerticalGap()
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    game.galleryLabels.forEachIndexed { index, label ->
                        Box(
                            modifier = Modifier.width(210.dp).height(120.dp).background(
                                Brush.linearGradient(
                                    if (index % 2 == 0) listOf(VenueColors.Violet500, VenueColors.Slate800)
                                    else listOf(VenueColors.Cyan400.copy(alpha = .65f), VenueColors.Slate800),
                                ),
                                RoundedCornerShape(16.dp),
                            ).padding(14.dp),
                            contentAlignment = Alignment.BottomStart,
                        ) { Text(label, color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().panel().padding(16.dp)) {
                SectionHeading("درباره بازی")
                VerticalGap()
                Text(game.description, color = VenueColors.Slate300, lineHeight = 24.sp)
                if (game.officialUrl != null) {
                    VerticalGap(12)
                    OutlinedButton(onClick = { openExternalUri(context, game.officialUrl) }) { Text("باز کردن وب‌سایت رسمی") }
                }
            }
        }
        if (venues.isNotEmpty()) {
            item { SectionHeading("محل نصب و دستگاه‌ها", modifierSubtitle(venues.size)) }
            items(venues, key = { it.id }) { venue ->
                VenueSummaryCard(venue, Modifier.padding(horizontal = 16.dp)) {
                    navigator.open(AppRoute.VenueDetail(venue.id))
                }
            }
        }
        if (related.isNotEmpty()) {
            item { SectionHeading("بازی‌های مرتبط", "پیشنهادهای نمونه") }
            items(related, key = { it.id }) { relatedGame ->
                GameSummaryCard(relatedGame, Modifier.padding(horizontal = 16.dp)) {
                    navigator.open(AppRoute.GameDetail(relatedGame.id))
                }
            }
        }
    }
}

private fun modifierSubtitle(count: Int): String = "$count گیمنت دارای این بازی است"
