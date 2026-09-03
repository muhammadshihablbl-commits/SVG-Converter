package com.shihab.svgconverter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shihab.svgconverter.ui.theme.AppTheme
import com.shihab.svgconverter.R
import com.shihab.svgconverter.ui.components.SettingsItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    force24dp: Boolean,
    onForce24dpChange: (Boolean) -> Unit,
    minifyXml: Boolean,
    onMinifyXmlChange: (Boolean) -> Unit,
    includeXmlDeclaration: Boolean,
    onIncludeXmlDeclarationChange: (Boolean) -> Unit,
    autoCopy: Boolean,
    onAutoCopyChange: (Boolean) -> Unit,
    removeColors: Boolean,
    onRemoveColorsChange: (Boolean) -> Unit,
    appTheme: AppTheme,
    onAppThemeChange: (AppTheme) -> Unit
) {
    var showThemeDialog by remember { mutableStateOf(false) }

    val topCardShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
    val middleCardShape = RoundedCornerShape(4.dp)
    val bottomCardShape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 28.dp, bottomEnd = 28.dp)

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    ThemeOptionRow(
                        title = "System Default",
                        selected = appTheme == AppTheme.SYSTEM,
                        onClick = {
                            onAppThemeChange(AppTheme.SYSTEM)
                            showThemeDialog = false
                        }
                    )
                    ThemeOptionRow(
                        title = "Light Theme",
                        selected = appTheme == AppTheme.LIGHT,
                        onClick = {
                            onAppThemeChange(AppTheme.LIGHT)
                            showThemeDialog = false
                        }
                    )
                    ThemeOptionRow(
                        title = "Dark Theme",
                        selected = appTheme == AppTheme.DARK,
                        onClick = {
                            onAppThemeChange(AppTheme.DARK)
                            showThemeDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 36.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Optimization & Output preferences",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // ১. App Theme (ক্লিক করলে থিম পিকার আসবে)
                ThemeSelectorCard(
                    currentTheme = when (appTheme) {
                        AppTheme.SYSTEM -> "System Default"
                        AppTheme.LIGHT -> "Light Theme"
                        AppTheme.DARK -> "Dark Theme"
                    },
                    onClick = { showThemeDialog = true },
                    shape = topCardShape
                )

                // ২. Force 24dp
                SettingsItemCard(
                    title = "Force 24dp Dimensions",
                    subtitle = "Override SVG dimensions to standard 24x24dp",
                    iconRes = R.drawable.ic_transform,
                    checked = force24dp,
                    onCheckedChange = onForce24dpChange,
                    shape = middleCardShape
                )

                // ৩. Include XML Declaration
                SettingsItemCard(
                    title = "Include XML Declaration",
                    subtitle = "Add <?xml version=\"1.0\" encoding=\"utf-8\"?> header",
                    iconRes = R.drawable.ic_xml,
                    checked = includeXmlDeclaration,
                    onCheckedChange = onIncludeXmlDeclarationChange,
                    shape = middleCardShape
                )

                // ৪. Minify XML
                SettingsItemCard(
                    title = "Minify XML Output",
                    subtitle = "Remove whitespace to reduce app resource size",
                    iconRes = R.drawable.ic_minify,
                    checked = minifyXml,
                    onCheckedChange = onMinifyXmlChange,
                    shape = middleCardShape
                )

                // ৫. Auto Copy
                SettingsItemCard(
                    title = "Auto-Copy Output",
                    subtitle = "Automatically copy XML code to clipboard on convert",
                    iconRes = R.drawable.ic_copy,
                    checked = autoCopy,
                    onCheckedChange = onAutoCopyChange,
                    shape = middleCardShape
                )

                // ৬. Remove Fixed Colors
                SettingsItemCard(
                    title = "Remove Fixed Colors",
                    subtitle = "Replace hardcoded SVG hex colors with monochrome tint",
                    iconRes = R.drawable.ic_invert_colors,
                    checked = removeColors,
                    onCheckedChange = onRemoveColorsChange,
                    shape = bottomCardShape
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ThemeSelectorCard(
    currentTheme: String,
    onClick: () -> Unit,
    shape: Shape
) {
    Surface(
        onClick = onClick,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_palette),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "App Theme",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = currentTheme,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
