package com.lizardnest.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lizardnest.app.ui.screen.FullScreenVideoScreen
import com.lizardnest.app.ui.screen.HomeScreen
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * 导航路由定义
 */
object Routes {
    const val HOME = "home"
    const val FULL_SCREEN_VIDEO = "full_screen_video/{streamUrl}"

    fun fullScreenVideo(streamUrl: String): String {
        val encoded = URLEncoder.encode(streamUrl, "UTF-8")
        return "full_screen_video/$encoded"
    }
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToFullScreen = { streamUrl ->
                    navController.navigate(Routes.fullScreenVideo(streamUrl))
                }
            )
        }

        composable(
            route = Routes.FULL_SCREEN_VIDEO,
            arguments = listOf(
                navArgument("streamUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("streamUrl") ?: ""
            val streamUrl = try {
                URLDecoder.decode(encoded, "UTF-8")
            } catch (e: Exception) {
                encoded
            }
            FullScreenVideoScreen(
                streamUrl = streamUrl,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
