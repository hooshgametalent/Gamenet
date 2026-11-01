package com.gamenetmehdi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
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
        setContent { GameTalentApp(InMemoryMockRepository()) }
    }
}

@Composable
fun GameTalentApp(repository: AppRepository) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        VenueTheme {
            val navigator = rememberAppNavigator()
            BackHandler(enabled = navigator.route !is AppRoute.Main) { navigator.back() }
            Scaffold(
                containerColor = VenueColors.Slate950,
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier.background(VenueColors.Slate900),
                        containerColor = VenueColors.Slate900,
                        contentColor = Color.White,
                    ) {
                        MainDestination.entries.forEach { destination ->
                            NavigationBarItem(
                                selected = navigator.selectedDestination == destination,
                                onClick = { navigator.openMain(destination) },
                                icon = {
                                    Text(
                                        destination.symbol,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                },
                                label = { Text(destination.label, fontSize = 10.sp) },
                            )
                        }
                    }
                },
            ) { contentPadding ->
                Box(
                    modifier = Modifier
                        .padding(contentPadding)
                        .safeDrawingPadding(),
                ) {
                    AppRouteHost(repository, navigator)
                }
            }
        }
    }
}

@Composable
private fun AppRouteHost(repository: AppRepository, navigator: AppNavigator) {
    when (val route = navigator.route) {
        is AppRoute.Main -> when (route.destination) {
            MainDestination.HOME -> HomeScreen(repository, navigator)
            MainDestination.GAMES -> GamesScreen(repository, navigator)
            MainDestination.VENUES -> VenuesScreen(repository, navigator)
            MainDestination.MAP -> MapScreen(repository, navigator)
            MainDestination.ACCOUNT -> AccountScreen(repository)
        }
        is AppRoute.GameDetail -> GameDetailScreen(repository, route.id, navigator)
        is AppRoute.VenueDetail -> VenueDetailScreen(repository, route.id, navigator)
        AppRoute.Services -> ServicesScreen(repository, navigator)
        is AppRoute.ServiceDetail -> ServiceDetailScreen(repository, route.id, navigator)
    }
}
