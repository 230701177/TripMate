package com.tripmate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tripmate.app.navigation.NavGraph
import com.tripmate.app.navigation.Screen
import com.tripmate.app.ui.theme.TripMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TripMateTheme(darkTheme = com.tripmate.app.data.MockDataProvider.isDarkMode) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val primaryColor = MaterialTheme.colorScheme.primary
                val onPrimaryContainerColor = MaterialTheme.colorScheme.onPrimaryContainer
                val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
                val errorColor = MaterialTheme.colorScheme.error
                val errorContainerColor = MaterialTheme.colorScheme.errorContainer
                val onErrorContainerColor = MaterialTheme.colorScheme.onErrorContainer

                val showBottomBar = currentDestination?.route !in listOf(
                    Screen.Splash.route,
                    Screen.Login.route,
                    Screen.Signup.route
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        containerColor = Color.Transparent
                    ) { innerPadding ->
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            NavGraph(navController = navController)
                        }
                    }

                    // MINIMAL FLOATING NAVBAR
                    Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                        AnimatedVisibility(
                            visible = showBottomBar,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                        ) {
                            Surface(
                                modifier = Modifier
                                    .padding(horizontal = 60.dp, vertical = 24.dp)
                                    .fillMaxWidth()
                                    .height(60.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                                shape = CircleShape,
                                shadowElevation = 8.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val items = listOf(
                                        NavigationItem("Home", Screen.Home.route, Icons.Default.Home),
                                        NavigationItem("Explore", Screen.Explore.route, Icons.Default.Explore),
                                        NavigationItem("Community", Screen.Community.route, Icons.Default.Groups),
                                        NavigationItem("Profile", Screen.Profile.route, Icons.Default.Person)
                                    )
                                    items.forEach { item ->
                                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                                        
                                        IconButton(
                                            onClick = {
                                                if (!selected) {
                                                    navController.navigate(item.route) {
                                                        popUpTo(navController.graph.findStartDestination().id) {
                                                            saveState = true
                                                        }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(
                                                    if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) 
                                                    else Color.Transparent, 
                                                    CircleShape
                                                )
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.title,
                                                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // NOTIFICATION OVERLAY
                    val notification = com.tripmate.app.data.MockDataProvider.activeNotification
                    val isError = com.tripmate.app.data.MockDataProvider.isErrorNotification
                    AnimatedVisibility(
                        visible = notification != null,
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                        modifier = Modifier.padding(top = 40.dp, start = 16.dp, end = 16.dp)
                    ) {
                        if (notification != null) {
                            Surface(
                                color = if (isError) errorContainerColor else primaryContainerColor,
                                contentColor = if (isError) onErrorContainerColor else onPrimaryContainerColor,
                                shape = RoundedCornerShape(24.dp),
                                shadowElevation = 8.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isError) Icons.Default.Cancel else Icons.Default.CheckCircle, 
                                        contentDescription = null, 
                                        tint = if (isError) errorColor else primaryColor
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Text(
                                        notification, 
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class NavigationItem(val title: String, val route: String, val icon: ImageVector)
