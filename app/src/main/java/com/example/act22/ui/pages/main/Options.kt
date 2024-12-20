package com.example.act22.ui.pages.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.act22.activity.Screen

enum class SettingsTab {
    SUPPORT, FEEDBACK, SETTINGS
}

@Composable
fun Options(
    navController: NavController
){
    MainScaffold(navController) {
        OptionsTabs(navController)
    }
}
@Composable
fun OptionsTabs(navController: NavController) {
    val tabs = listOf(
        SettingsTab.SUPPORT to Screen.Support.route,
        SettingsTab.FEEDBACK to Screen.Feedback.route,
        SettingsTab.SETTINGS to Screen.Settings.route
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(top = 40.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { (tab, route) ->
            VerticalTabItem(
                label = tab.name,
                onClick = { navController.navigate(route) }
            )
        }
    }
}


