/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.turtlepaw.health.apps.reflections.presentation

import ReflectAsCircularIcons
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.turtlepaw.health.HealthActivity
import com.turtlepaw.health.apps.reflections.presentation.pages.History
import com.turtlepaw.health.apps.reflections.presentation.pages.Reflect
import com.turtlepaw.health.apps.reflections.presentation.pages.WearHome
import com.turtlepaw.health.apps.reflections.presentation.theme.ReflectionTheme
import com.turtlepaw.shared.SettingsBasics
import com.turtlepaw.shared.database.AppDatabase
import com.turtlepaw.shared.database.reflect.Reflection
import com.turtlepaw.shared.database.reflect.ReflectionType
import kotlinx.coroutines.launch
import java.time.LocalDateTime


enum class Routes(private val route: String) {
    HOME("/home"),
    REFLECT("/reflect"),
    REFLECT_LIST("/reflect_list"),
    HISTORY("/history");

    fun getRoute(query: String? = null): String {
        return if (query != null) {
            "$route/$query"
        } else route
    }
}

class MainActivity : HealthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)
        val sharedPreferences = getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        )

        setContent {
            WearPages(
                sharedPreferences,
                AppDatabase.getDatabase(applicationContext),
                this
            )
        }
    }
}

@Composable
fun WearPages(
    sharedPreferences: SharedPreferences,
    database: AppDatabase,
    context: Context
) {
    ReflectionTheme {
        // Creates a navigation controller for our pages
        val navController = rememberSwipeDismissableNavController()
        val coroutineScope = rememberCoroutineScope()
        val onConfirm = { reflection: ReflectionType ->
            coroutineScope.launch {
                database.reflectionDao().insertReflection(
                    Reflection(
                        date = LocalDateTime.now(),
                        value = reflection
                    )
                )

                navController.popBackStack()

                if (navController.currentDestination?.route == Routes.REFLECT.getRoute()) {
                    navController.popBackStack()
                }
            }
        }

        SwipeDismissableNavHost(
            navController = navController,
            startDestination = Routes.HOME.getRoute()
        ) {
            composable(Routes.HOME.getRoute()) {
                WearHome(database, onOpenHistory = {
                    navController.navigate(Routes.HISTORY.getRoute())
                }) {
                    navController.navigate(Routes.REFLECT.getRoute())
                }
            }
            composable(Routes.REFLECT.getRoute()) {
                Reflect(context, { onConfirm(it) }) {
                    navController.navigate(Routes.REFLECT_LIST.getRoute())
                }
            }
            composable(Routes.REFLECT_LIST.getRoute()) {
                ReflectAsCircularIcons(context) {
                    onConfirm(it)
                }
            }
            composable(Routes.HISTORY.getRoute()) {
                History(database)
            }
        }
    }
}