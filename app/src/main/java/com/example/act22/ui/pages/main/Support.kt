package com.example.act22.ui.pages.main

import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.act22.activity.Screen

data class SupportTab(
    val title: String,
    val imageVector: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun SupportPage(navController: NavController) {
    MainScaffold(navController) {
        SupportTabs(navController)
    }
}

@Composable
fun SupportTabs(navController: NavController){
    val tabs = listOf(
        SupportTab(
            title = "CALL SUPPORT",
            imageVector = Icons.Default.Phone,
            onClick = {
                //TODO check of a time
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:+1234567890")
                }
                navController.context.startActivity(intent)
            }
        ), SupportTab(
            title = "CHAT WITH SUPPORT" ,
            imageVector = Icons.Default.Face,
            onClick = {
                //TODO chatbot
            }
        ), SupportTab(
            title = "EMAIL SUPPORT",
            imageVector = Icons.Default.Email,
            onClick = {
                navController.navigate(Screen.SupportEmail.route)
            }
        )
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(top = 40.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { (title, icon, onClick) ->
            SupportVerticalTabItem(
                icon = icon,
                label = title,
                onClick = { onClick() }
            )
        }
    }
}

@Composable
fun SupportVerticalTabItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

