package com.example.act22.ui.pages.main

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.act22.R
import com.example.act22.activity.Screen
import com.example.act22.ui.theme.getLogoResource
import com.example.act22.viewmodel.AuthenticationViewModel

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: Painter
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScaffold(
    navController: NavController,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = { CustomTopBar(navController) },
        bottomBar = { CustomBottomBar(navController) }
    ) {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 42.dp, bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ){
            content()
        }
    }
}

@Composable
fun CustomTopBar(navController: NavController) {
    Box(
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .height(74.dp)
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomEnd = 25.dp,
                        bottomStart = 25.dp
                    )
                )
                .background(MaterialTheme.colorScheme.primary)
                .padding(top = 24.dp, start = 8.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ){
                Image(
                    painter = painterResource(getLogoResource()),
                    contentDescription = "Logo",
                    modifier = Modifier.size(35.dp),
                    contentScale = ContentScale.Fit
                )
                Text("Curve",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimary)
            }

            Icon(
                painter = painterResource(R.drawable.icon_logout),
                contentDescription = "Exit",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .size(25.dp)
                    .clickable { navController.navigate(Screen.StartPage.route) }
                )
        }
    }
}


// Bottom bar navigation with icons
@Composable
fun CustomBottomBar(navController: NavController) {
    val items = listOf(
        BottomNavItem(
            title = "All assets",
            route = Screen.MainPage.route,
            icon = painterResource(R.drawable.icon_chart)
        ),
        BottomNavItem(
            title = "User Portfolio",
            route = Screen.Portfolio.route,
            icon = painterResource(R.drawable.icon_donut)
        ),
        BottomNavItem(
            title = "Account",
            route = Screen.Profile.route,
            icon = painterResource(R.drawable.icon_person)
        ),
        BottomNavItem(
            title = "Options",
            route = Screen.Options.route,
            icon = painterResource(R.drawable.icon_settings)
        )
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: Screen.MainPage.route

    Box(
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = 25.dp,
                        topEnd = 25.dp,
                        bottomEnd = 0.dp,
                        bottomStart = 0.dp
                    )
                )
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                CustomNavItem(
                    icon = item.icon,
                    label = item.title,
                    isSelected = currentRoute == item.route,
                    onClick = { navController.navigate(item.route) }
                )
            }
        }
    }
}

@Composable
fun CustomNavItem(
    icon: Painter,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Icon(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(32.dp))
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(
                    color = MaterialTheme.colorScheme.tertiary,
                    radius = 50.dp,
                    bounded = true
                )
            )
            .padding(8.dp),
        painter = icon,
        contentDescription = label,
        tint =  if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
    )
}