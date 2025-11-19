package com.example.emojiguess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.emojiguess.ui.screens.GameScreen
import com.example.emojiguess.ui.screens.LobbyScreen
import com.example.emojiguess.ui.screens.LoginScreen
import com.example.emojiguess.ui.theme.EmojiGuessTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EmojiGuessTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen(onLoginSuccess = { navController.navigate("lobby") })
                        }
                        composable("lobby") {
                            LobbyScreen(onCreateGame = { gameId -> navController.navigate("game/$gameId") })
                        }
                        composable(
                            "game/{gameId}",
                            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            GameScreen(gameId = backStackEntry.arguments?.getString("gameId"))
                        }
                    }
                }
            }
        }
    }
}
