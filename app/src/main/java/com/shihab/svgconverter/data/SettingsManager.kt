package com.shihab.svgconverter.data

import android.content.Context
import android.content.SharedPreferences
import com.shihab.svgconverter.ui.theme.AppTheme

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    fun getForce24dp(): Boolean = prefs.getBoolean("force_24dp", false)
    fun setForce24dp(value: Boolean) = prefs.edit().putBoolean("force_24dp", value).apply()

    fun getMinifyXml(): Boolean = prefs.getBoolean("minify_xml", false)
    fun setMinifyXml(value: Boolean) = prefs.edit().putBoolean("minify_xml", value).apply()

    fun getIncludeXmlDeclaration(): Boolean = prefs.getBoolean("include_xml_decl", true)
    fun setIncludeXmlDeclaration(value: Boolean) = prefs.edit().putBoolean("include_xml_decl", value).apply()

    fun getAutoCopy(): Boolean = prefs.getBoolean("auto_copy", false)
    fun setAutoCopy(value: Boolean) = prefs.edit().putBoolean("auto_copy", value).apply()

    fun getRemoveColors(): Boolean = prefs.getBoolean("remove_colors", false)
    fun setRemoveColors(value: Boolean) = prefs.edit().putBoolean("remove_colors", value).apply()

    fun getAppTheme(): AppTheme {
        val name = prefs.getString("app_theme", AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
        return try { AppTheme.valueOf(name) } catch (e: Exception) { AppTheme.SYSTEM }
    }
    fun setAppTheme(theme: AppTheme) = prefs.edit().putString("app_theme", theme.name).apply()
}
