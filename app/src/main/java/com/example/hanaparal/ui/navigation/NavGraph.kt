package com.example.hanaparal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hanaparal.ui.screens.CreateGroupScreen
import com.example.hanaparal.ui.screens.GroupDetailScreen
import com.example.hanaparal.ui.screens.GroupsScreen
import com.example.hanaparal.ui.screens.LoginScreen
import com.example.hanaparal.ui.screens.ProfileScreen
import com.example.hanaparal.ui.screens.SuperuserScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Profile : Screen("profile")
    object Groups : Screen("groups")
    object CreateGroup : Screen("create_group")
    object Superuser : Screen("superuser")
    object GroupDetail : Screen("group_detail/{groupId}") {
        fun createRoute(groupId: String) = "group_detail/$groupId"
    }
}

@Composable
fun HanapAralNavGraph(
    isLoggedIn: Boolean,
    onLogout: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val startDestination = if (isLoggedIn) Screen.Profile.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Screen.Profile.route) { popUpTo(Screen.Login.route) { inclusive = true } } }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onProfileComplete = { navController.navigate(Screen.Groups.route) { popUpTo(Screen.Profile.route) { inclusive = true } } },
                onSuperuser = { navController.navigate(Screen.Superuser.route) }
            )
        }
        composable(Screen.Groups.route) {
            GroupsScreen(
                onCreateGroup = { navController.navigate(Screen.CreateGroup.route) },
                onGroupClick = { groupId -> navController.navigate(Screen.GroupDetail.createRoute(groupId)) },
                onSignOut = {
                    onLogout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            )
        }
        composable(Screen.CreateGroup.route) {
            CreateGroupScreen(
                onGroupCreated = { groupId ->
                    navController.popBackStack()
                    navController.navigate(Screen.GroupDetail.createRoute(groupId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.GroupDetail.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            GroupDetailScreen(
                groupId = groupId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Superuser.route) {
            SuperuserScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
