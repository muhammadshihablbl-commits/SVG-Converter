package com.shihab.svgconverter.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shihab.svgconverter.R
import com.shihab.svgconverter.utils.SvgToVectorConverter
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    onNavigateSettings: () -> Unit,
    force24dp: Boolean,
    minifyXml: Boolean,
    includeXmlDeclaration: Boolean,
    autoCopy: Boolean,
    removeColors: Boolean
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var svgInput by remember { mutableStateOf("") }
    var xmlOutput by remember { mutableStateOf("") }
    var fileName by remember { mutableStateOf("ic_vector") }

    // Batch Convert এর জন্য লিস্ট
    var batchConvertedFiles by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    LaunchedEffect(svgInput, force24dp, minifyXml, includeXmlDeclaration, removeColors) {
        if (svgInput.isNotBlank()) {
            xmlOutput = SvgToVectorConverter.convertSvgToXml(
                svgString = svgInput,
                force24dp = force24dp,
                minify = minifyXml,
                includeXmlDeclaration = includeXmlDeclaration,
                removeColors = removeColors
            )
            if (autoCopy && xmlOutput.isNotBlank()) {
                clipboardManager.setText(AnnotatedString(xmlOutput))
            }
        }
    }

    // একটি বা একাধিক ফাইল সিলেক্ট করার জন্য পিকার
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            if (uris.size == 1) {
                // একটি ফাইল সিলেক্ট করলে সিঙ্গেল মোডে কাজ করবে
                batchConvertedFiles = emptyList()
                val uri = uris.first()
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    svgInput = inputStream?.bufferedReader()?.use { r -> r.readText() } ?: ""

                    val originalName = getFileNameFromUri(context, uri)
                    val rawName = originalName.substringBeforeLast(".")
                        .lowercase().replace(Regex("[^a-z0-9_]"), "_")
                    fileName = if (rawName.startsWith("ic_")) rawName else "ic_$rawName"

                    xmlOutput = SvgToVectorConverter.convertSvgToXml(
                        svgString = svgInput,
                        force24dp = force24dp,
                        minify = minifyXml,
                        includeXmlDeclaration = includeXmlDeclaration,
                        removeColors = removeColors
                    )
                    if (autoCopy && xmlOutput.isNotBlank()) {
                        clipboardManager.setText(AnnotatedString(xmlOutput))
                        Toast.makeText(context, "SVG Converted & Copied!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "SVG Converted!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to read SVG file", Toast.LENGTH_SHORT).show()
                }
            } else {
                // একাধিক ফাইল সিলেক্ট করলে Batch Mode এ কাজ করবে
                svgInput = ""
                xmlOutput = ""
                val convertedList = mutableListOf<Pair<String, String>>()

                uris.forEachIndexed { index, uri ->
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val content = inputStream?.bufferedReader()?.use { r -> r.readText() } ?: ""

                        val originalName = getFileNameFromUri(context, uri)
                        var rawName = originalName.substringBeforeLast(".")
                            .lowercase().replace(Regex("[^a-z0-9_]"), "_")
                        if (!rawName.startsWith("ic_")) rawName = "ic_$rawName"

                        val xml = SvgToVectorConverter.convertSvgToXml(
                            svgString = content,
                            force24dp = force24dp,
                            minify = minifyXml,
                            includeXmlDeclaration = includeXmlDeclaration,
                            removeColors = removeColors
                        )

                        if (xml.isNotBlank()) {
                            convertedList.add(Pair("$rawName.xml", xml))
                        }
                    } catch (_: Exception) {}
                }

                batchConvertedFiles = convertedList
                Toast.makeText(context, "${convertedList.size} Files Converted!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // সিঙ্গেল XML ফাইল সেভ বাটন
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/xml")
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { stream: OutputStream ->
                    stream.write(xmlOutput.toByteArray())
                }
                Toast.makeText(context, "Saved to Storage!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Batch ZIP ফাইল সেভ বাটন
    val saveZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    ZipOutputStream(outputStream).use { zipOut ->
                        batchConvertedFiles.forEach { (fileName, content) ->
                            val entry = ZipEntry(fileName)
                            zipOut.putNextEntry(entry)
                            zipOut.write(content.toByteArray())
                            zipOut.closeEntry()
                        }
                    }
                }
                Toast.makeText(context, "ZIP File Saved!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save ZIP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_transform),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Vector Studio",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "SVG to Android XML",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateSettings) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                onClick = { filePickerLauncher.launch("*/*") },
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_upload),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Choose SVG File(s)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Select one or multiple SVGs for batch convert",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            OutlinedTextField(
                value = svgInput,
                onValueChange = {
                    svgInput = it
                    if (it.isBlank()) {
                        xmlOutput = ""
                    }
                },
                placeholder = {
                    Text(
                        "Paste raw <svg> code here...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (svgInput.isNotEmpty()) {
                        IconButton(onClick = {
                            svgInput = ""
                            xmlOutput = ""
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = "Clear text",
                                tint = MaterialTheme.colorScheme.error, // Material 3 এর ডিফল্ট লাল কালার
                                modifier = Modifier.size(20.dp)
                            )

                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                shape = RoundedCornerShape(20.dp)
            )

            Button(
                onClick = {
                    if (svgInput.isNotBlank()) {
                        batchConvertedFiles = emptyList()
                        xmlOutput = SvgToVectorConverter.convertSvgToXml(
                            svgString = svgInput,
                            force24dp = force24dp,
                            minify = minifyXml,
                            includeXmlDeclaration = includeXmlDeclaration,
                            removeColors = removeColors
                        )
                        if (autoCopy) {
                            clipboardManager.setText(AnnotatedString(xmlOutput))
                            Toast.makeText(context, "Converted & Copied!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Converted!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Please select or paste an SVG first", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_transform),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Convert to Vector XML",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Batch Conversion Output
            if (batchConvertedFiles.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Batch Result (${batchConvertedFiles.size} files)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            batchConvertedFiles.forEach { (name, _) ->
                                Text(
                                    text = "• $name",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { saveZipLauncher.launch("vector_icons.zip") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_download),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Download All as ZIP")
                        }
                    }
                }
            }

            // Single Conversion Output
            if (xmlOutput.isNotBlank() && batchConvertedFiles.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Vector Output",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${xmlOutput.length} chars",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = xmlOutput,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.verticalScroll(rememberScrollState())
                            )
                        }

                        OutlinedTextField(
                            value = fileName,
                            onValueChange = { fileName = it },
                            label = { Text("Resource Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(xmlOutput))
                                    Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_copy),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Copy", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }

                            FilledTonalButton(
                                onClick = {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        putExtra(Intent.EXTRA_TEXT, xmlOutput)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Share Vector XML")
                                    context.startActivity(shareIntent)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_share),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Share", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    val safeName = if (fileName.startsWith("ic_")) "$fileName.xml" else "ic_$fileName.xml"
                                    saveFileLauncher.launch(safeName)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_download),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Save", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// URI থেকে আসল ফাইলের নাম বের করার হেলপার ফাংশন
private fun getFileNameFromUri(context: Context, uri: Uri): String {
    var fileName = ""
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
    }
    if (fileName.isEmpty()) {
        fileName = uri.lastPathSegment ?: "vector"
    }
    return fileName
}
