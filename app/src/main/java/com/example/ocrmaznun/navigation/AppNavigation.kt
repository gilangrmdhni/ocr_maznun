package com.example.ocrmaznun.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ocrmaznun.ui.home.HomeScreen
import com.example.ocrmaznun.ui.result.ResultScreen
import com.example.ocrmaznun.ui.scan.ScanScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("scan") { ScanScreen(navController) }
        composable("result/{ocrResult}") { backStackEntry ->
            val result = backStackEntry.arguments?.getString("ocrResult") ?: "No Data"
            ResultScreen(result, navController)
        }
    }
}



//package com.example.ocrmaznun.ui.navigation
//
//import androidx.compose.runtime.Composable
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.rememberNavController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import com.example.ocrmaznun.ui.home.HomeScreen
//import com.example.ocrmaznun.ui.scan.ScanScreen
//import com.example.ocrmaznun.ui.result.ResultScreen
//
//@Composable
//fun Navigation(navController: NavHostController) {
//    val navController = rememberNavController()
//    NavHost(navController = navController, startDestination = "home") {
//        composable("home") { HomeScreen(navController) }
//        composable("scan") { ScanScreen(navController) }
//        composable("result/{ocrResult}") { backStackEntry ->
//            val result = backStackEntry.arguments?.getString("result")
//            ResultScreen(result, navController)
//        }
//    }
//}
