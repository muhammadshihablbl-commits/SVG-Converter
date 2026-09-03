package com.shihab.svgconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.shihab.svgconverter.data.SettingsManager
import com.shihab.svgconverter.ui.screens.ConverterScreen
import com.shihab.svgconverter.ui.screens.SettingsScreen
import com.shihab.svgconverter.ui.theme.AppTheme
import com.shihab.svgconverter.ui.theme.SvgConvertTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val settingsManager = remember { SettingsManager(context) }
            var appTheme by remember { mutableStateOf(settingsManager.getAppTheme()) }

            SvgConvertTheme(appTheme = appTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        settingsManager = settingsManager,
                        appTheme = appTheme,
                        onAppThemeChange = { newTheme: AppTheme -> // Explicit type নির্দিষ্ট করা হয়েছে
                            appTheme = newTheme
                            settingsManager.setAppTheme(newTheme)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    settingsManager: SettingsManager,
    appTheme: AppTheme,
    onAppThemeChange: (AppTheme) -> Unit
) {
    var currentScreen by remember { mutableStateOf("converter") }

    var force24dp by remember { mutableStateOf(settingsManager.getForce24dp()) }
    var minifyXml by remember { mutableStateOf(settingsManager.getMinifyXml()) }
    var includeXmlDeclaration by remember { mutableStateOf(settingsManager.getIncludeXmlDeclaration()) }
    var autoCopy by remember { mutableStateOf(settingsManager.getAutoCopy()) }
    var removeColors by remember { mutableStateOf(settingsManager.getRemoveColors()) }

    if (currentScreen == "settings") {
        BackHandler { currentScreen = "converter" }
        SettingsScreen(
            onBack = { currentScreen = "converter" },
            force24dp = force24dp,
            onForce24dpChange = {
                force24dp = it
                settingsManager.setForce24dp(it)
            },
            minifyXml = minifyXml,
            onMinifyXmlChange = {
                minifyXml = it
                settingsManager.setMinifyXml(it)
            },
            includeXmlDeclaration = includeXmlDeclaration,
            onIncludeXmlDeclarationChange = {
                includeXmlDeclaration = it
                settingsManager.setIncludeXmlDeclaration(it)
            },
            autoCopy = autoCopy,
            onAutoCopyChange = {
                autoCopy = it
                settingsManager.setAutoCopy(it)
            },
            removeColors = removeColors,
            onRemoveColorsChange = {
                removeColors = it
                settingsManager.setRemoveColors(it)
            },
            appTheme = appTheme,
            onAppThemeChange = onAppThemeChange
        )
    } else {
        ConverterScreen(
            onNavigateSettings = { currentScreen = "settings" },
            force24dp = force24dp,
            minifyXml = minifyXml,
            includeXmlDeclaration = includeXmlDeclaration,
            autoCopy = autoCopy,
            removeColors = removeColors
        )
    }
}
