package com.furkan.kilertakipp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KilerAppNavigator()


        }
    }
}



@Composable

fun KilerAppNavigator() {
    val primaryColor = Color(0xFF4CAF50)
    var currentScreen by remember { mutableStateOf("login") }
    var userName by remember { mutableStateOf("Kullanıcı") }

    when (currentScreen) {
        "login" -> LoginView(
            primaryColor = primaryColor,
            onRegisterClick = { currentScreen = "register" },
            onLoginSuccess = { name ->
                userName = name
                currentScreen = "home"
            }
        )
        "register" -> RegisterView(
            primaryColor = primaryColor,
            onBackToLogin = { currentScreen = "login" }
        )
        "home" -> HomeView(
            userName = userName,
            primaryColor = primaryColor,
            onLogout = { currentScreen = "login" }
        )
    }
}