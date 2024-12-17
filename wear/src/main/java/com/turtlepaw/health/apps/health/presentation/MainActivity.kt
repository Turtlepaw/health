/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.turtlepaw.health.apps.health.presentation

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.intl.Locale
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.turtlepaw.health.apps.health.presentation.pages.WearHome
import com.turtlepaw.health.apps.health.presentation.pages.coaching.StartCoaching
import com.turtlepaw.health.apps.health.presentation.pages.settings.WearSettings
import com.turtlepaw.health.apps.health.presentation.theme.HealthTheme
import com.turtlepaw.health.apps.sunlight.presentation.pages.StatePicker
import com.turtlepaw.shared.Settings
import com.turtlepaw.shared.SettingsBasics
import com.turtlepaw.shared.database.AppDatabase
import com.turtlepaw.shared.getDefaultSharedSettings
import java.text.NumberFormat


enum class Routes(private val route: String) {
    HOME("/home"),
    SETTINGS("/settings"),
    GOAL_PICKER("/goal-picker"),
    SUN_PICKER("/sun-picker"),
    HISTORY("/history"),
    CLOCKWORK("/clockwork-toolkit"),
    NOTICES("/notices"),
    STATS("/stats"),
    START_COACHING("/start-coaching");

    fun getRoute(query: String? = null): String {
        return if(query != null){
            "$route/$query"
        } else route
    }
}

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SettingsBasics.HISTORY_STORAGE_BASE.getKey())

class MainActivity : ComponentActivity() {
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
                this,
                sharedPreferences,
                AppDatabase.getDatabase(applicationContext),
            )
        }
    }
}

@Composable
fun WearPages(
    context: ComponentActivity,
    sharedPreferences: SharedPreferences,
    database: AppDatabase
){
    HealthTheme {
        // Creates a navigation controller for our pages
        val navController = rememberSwipeDismissableNavController()

        SwipeDismissableNavHost(
            navController = navController,
            startDestination = Routes.HOME.getRoute()
        ) {
            composable(Routes.HOME.getRoute()) {
                WearHome(context, database, navController)
            }
            composable(Routes.START_COACHING.getRoute()) {
                StartCoaching(database)
            }
            composable(Routes.SETTINGS.getRoute()) {
                var goal by remember { mutableStateOf<Int?>(null) }
                LaunchedEffect(Unit) {
                    goal = context.getDefaultSharedSettings()
                        .getInt(Settings.STEP_GOAL.getKey(), Settings.STEP_GOAL.getDefaultAsInt())
                }

                WearSettings(goal, navController::navigate)
            }
            composable(Routes.GOAL_PICKER.getRoute()) {
                var goal by remember { mutableStateOf<Int?>(null) }
                LaunchedEffect(Unit) {
                    goal = context.getDefaultSharedSettings()
                        .getInt(Settings.STEP_GOAL.getKey(), Settings.STEP_GOAL.getDefaultAsInt())
                }

                StatePicker(
                    List(10) {
                        it.times(1000).plus(1000)
                    },
                    unitOfMeasurement = "",
                    goal ?: Settings.STEP_GOAL.getDefaultAsInt(),
                    recommendedItem = null,
                    renderItem = {
                        NumberFormat.getInstance(Locale.current.platformLocale).format(it)
                    }
                ) {
                    sharedPreferences.edit().putInt(Settings.STEP_GOAL.getKey(), it).apply()
                    navController.popBackStack()
                }
            }
        }
    }
}