package com.example.act22.ui.pages.main

import androidx.compose.runtime.Composable
import androidx.navigation.NavController


@Composable
fun SettingsPage(navController: NavController) {
    MainScaffold(navController) { EmptyPage("Settings page", "back") { }}

}
