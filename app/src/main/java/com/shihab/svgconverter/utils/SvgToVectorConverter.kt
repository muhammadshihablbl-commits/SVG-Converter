package com.shihab.svgconverter.utils

import java.util.Locale

object SvgToVectorConverter {

    fun convertSvgToXml(
        svgString: String,
        force24dp: Boolean = false,
        minify: Boolean = false,
        includeXmlDeclaration: Boolean = true,
        removeColors: Boolean = false
    ): String {
        if (svgString.isBlank()) return ""

        // ১. ViewBox এবং ডাইমেনশন (width, height, minX, minY) পার্স করা
        val viewBoxMatch = Regex("""viewBox=["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(svgString)?.groupValues?.get(1)
        val widthMatch = Regex("""width=["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(svgString)?.groupValues?.get(1)
        val heightMatch = Regex("""height=["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(svgString)?.groupValues?.get(1)

        var minX = 0f
        var minY = 0f
        var viewportWidth = "24"
        var viewportHeight = "24"

        if (viewBoxMatch != null) {
            // স্পেস বা কমা দিয়ে ভাগ করা
            val parts = viewBoxMatch.trim().split(Regex("""[\s,]+""")).filter { it.isNotEmpty() }
            if (parts.size >= 4) {
                minX = parts[0].toFloatOrNull() ?: 0f
                minY = parts[1].toFloatOrNull() ?: 0f
                viewportWidth = parts[2].replace(Regex("[^0-9.]"), "").ifEmpty { "24" }
                viewportHeight = parts[3].replace(Regex("[^0-9.]"), "").ifEmpty { "24" }
            }
        } else {
            viewportWidth = widthMatch?.replace(Regex("[^0-9.]"), "")?.ifEmpty { "24" } ?: "24"
            viewportHeight = heightMatch?.replace(Regex("[^0-9.]"), "")?.ifEmpty { "24" } ?: "24"
        }

        val rawWidthNum = widthMatch?.replace(Regex("[^0-9.]"), "")?.ifEmpty { viewportWidth } ?: viewportWidth
        val rawHeightNum = heightMatch?.replace(Regex("[^0-9.]"), "")?.ifEmpty { viewportHeight } ?: viewportHeight

        val finalWidth = if (force24dp) "24dp" else "${rawWidthNum}dp"
        val finalHeight = if (force24dp) "24dp" else "${rawHeightNum}dp"

        // ২. মূল <svg> ট্যাগ থেকে গ্লোবাল অ্যাট্রিবিউট রিড করা
        val svgTagMatch = Regex("""<svg\b([^>]*)>""", RegexOption.IGNORE_CASE).find(svgString)?.groupValues?.get(1) ?: ""
        val globalStroke = extractAttribute(svgTagMatch, "stroke")
        val globalFill = extractAttribute(svgTagMatch, "fill")
        val globalStrokeWidth = extractAttribute(svgTagMatch, "stroke-width")
        val globalStrokeCap = extractAttribute(svgTagMatch, "stroke-linecap")
        val globalStrokeJoin = extractAttribute(svgTagMatch, "stroke-linejoin")

        val sb = StringBuilder()

        if (includeXmlDeclaration) {
            sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        }

        sb.append("<vector xmlns:android=\"http://schemas.android.com/apk/res/android\"\n")
        sb.append("    android:width=\"$finalWidth\"\n")
        sb.append("    android:height=\"$finalHeight\"\n")
        sb.append("    android:viewportWidth=\"$viewportWidth\"\n")
        sb.append("    android:viewportHeight=\"$viewportHeight\">\n")

        // ৩. Google Material Symbols নেগেটিভ কোঅর্ডিনেট হ্যান্ডলিং (translate)
        val needsTranslation = (minX != 0f || minY != 0f)
        if (needsTranslation) {
            val transX = formatFloat(-minX)
            val transY = formatFloat(-minY)
            sb.append("    <group")
            if (minX != 0f) sb.append(" android:translateX=\"$transX\"")
            if (minY != 0f) sb.append(" android:translateY=\"$transY\"")
            sb.append(">\n")
        }

        val indent = if (needsTranslation) "        " else "    "

        // ৪. সমস্ত শেপ (<path>, <circle>, <rect>, <line>, <polyline>, <polygon>) খুঁজে বের করা
        val elementRegex = Regex("""<(path|circle|rect|line|polyline|polygon)\b([^>]*)/?>""", RegexOption.IGNORE_CASE)
        val elements = elementRegex.findAll(svgString)

        for (match in elements) {
            val tagName = match.groupValues[1].lowercase(Locale.ROOT)
            val attributes = match.groupValues[2]

            val pathData: String = when (tagName) {
                "path" -> extractAttribute(attributes, "d") ?: continue
                "circle" -> {
                    val cx = extractFloat(attributes, "cx", 0f)
                    val cy = extractFloat(attributes, "cy", 0f)
                    val r = extractFloat(attributes, "r", 0f)
                    if (r <= 0f) continue
                    "M ${cx - r},$cy A $r,$r 0 1,0 ${cx + r},$cy A $r,$r 0 1,0 ${cx - r},$cy Z"
                }
                "rect" -> {
                    val x = extractFloat(attributes, "x", 0f)
                    val y = extractFloat(attributes, "y", 0f)
                    val w = extractFloat(attributes, "width", 0f)
                    val h = extractFloat(attributes, "height", 0f)
                    val rx = extractFloat(attributes, "rx", 0f)
                    val ry = extractFloat(attributes, "ry", rx)
                    if (w <= 0f || h <= 0f) continue
                    if (rx > 0f || ry > 0f) {
                        "M ${x + rx},$y H ${x + w - rx} A $rx,$ry 0 0,1 ${x + w},${y + ry} V ${y + h - ry} A $rx,$ry 0 0,1 ${x + w - rx},${y + h} H ${x + rx} A $rx,$ry 0 0,1 $x,${y + h - ry} V ${y + ry} A $rx,$ry 0 0,1 ${x + rx},$y Z"
                    } else {
                        "M $x,$y H ${x + w} V ${y + h} H $x Z"
                    }
                }
                "line" -> {
                    val x1 = extractFloat(attributes, "x1", 0f)
                    val y1 = extractFloat(attributes, "y1", 0f)
                    val x2 = extractFloat(attributes, "x2", 0f)
                    val y2 = extractFloat(attributes, "y2", 0f)
                    "M $x1,$y1 L $x2,$y2"
                }
                "polyline", "polygon" -> {
                    val points = extractAttribute(attributes, "points") ?: continue
                    val coords = points.trim().split(Regex("""[\s,]+""")).filter { it.isNotEmpty() }
                    if (coords.size < 4) continue
                    val pathBuilder = StringBuilder()
                    for (i in coords.indices step 2) {
                        if (i + 1 < coords.size) {
                            if (i == 0) pathBuilder.append("M ${coords[i]},${coords[i + 1]} ")
                            else pathBuilder.append("L ${coords[i]},${coords[i + 1]} ")
                        }
                    }
                    if (tagName == "polygon") pathBuilder.append("Z")
                    pathBuilder.toString().trim()
                }
                else -> continue
            }

            // কালার ও স্ট্রোক নির্ধারণ
            var fill = extractAttribute(attributes, "fill") ?: globalFill
            var stroke = extractAttribute(attributes, "stroke") ?: globalStroke
            val strokeWidth = extractAttribute(attributes, "stroke-width") ?: globalStrokeWidth ?: "2"
            val strokeLineCap = extractAttribute(attributes, "stroke-linecap") ?: globalStrokeCap
            val strokeLineJoin = extractAttribute(attributes, "stroke-linejoin") ?: globalStrokeJoin
            val fillRule = extractAttribute(attributes, "fill-rule")

            // currentColor ও রঙ ঠিক করা
            fill = sanitizeColor(fill)
            stroke = sanitizeColor(stroke)

            if (removeColors) {
                if (fill != null && fill != "none") fill = "#FF000000"
                if (stroke != null && stroke != "none") stroke = "#FF000000"
            }

            sb.append("$indent<path\n")
            sb.append("$indent    android:pathData=\"$pathData\"")

            if (fillRule?.equals("evenodd", ignoreCase = true) == true) {
                sb.append("\n$indent    android:fillType=\"evenOdd\"")
            }

            // ফিল কালার
            if (fill != null && fill != "none") {
                sb.append("\n$indent    android:fillColor=\"$fill\"")
            } else if (fill == null && (stroke == null || stroke == "none")) {
                sb.append("\n$indent    android:fillColor=\"#FF000000\"")
            }

            // স্ট্রোক কালার ও উইথ
            if (stroke != null && stroke != "none") {
                sb.append("\n$indent    android:strokeColor=\"$stroke\"")
                val widthVal = strokeWidth.replace(Regex("[^0-9.]"), "").ifEmpty { "2" }
                sb.append("\n$indent    android:strokeWidth=\"$widthVal\"")

                if (!strokeLineCap.isNullOrBlank()) {
                    sb.append("\n$indent    android:strokeLineCap=\"$strokeLineCap\"")
                }
                if (!strokeLineJoin.isNullOrBlank()) {
                    sb.append("\n$indent    android:strokeLineJoin=\"$strokeLineJoin\"")
                }
            }

            sb.append(" />\n")
        }

        if (needsTranslation) {
            sb.append("    </group>\n")
        }

        sb.append("</vector>")

        var result = sb.toString()
        if (minify) {
            result = result.lines().joinToString("") { it.trim() }
        }

        return result
    }

    private fun extractAttribute(tagContent: String, attributeName: String): String? {
        val regex = Regex("""\b$attributeName=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        return regex.find(tagContent)?.groupValues?.get(1)
    }

    private fun extractFloat(tagContent: String, attributeName: String, defaultVal: Float): Float {
        val str = extractAttribute(tagContent, attributeName) ?: return defaultVal
        return str.replace(Regex("[^0-9.-]"), "").toFloatOrNull() ?: defaultVal
    }

    private fun formatFloat(value: Float): String {
        return if (value % 1.0f == 0.0f) {
            value.toInt().toString()
        } else {
            value.toString()
        }
    }

    // currentColor এবং শর্ট হেক্স কোড হ্যান্ডেল করা
    private fun sanitizeColor(color: String?): String? {
        if (color == null) return null
        val trimmed = color.trim().lowercase(Locale.ROOT)
        if (trimmed == "none") return "none"
        if (trimmed == "currentcolor") return "#FF000000"

        // #RGB -> #FFRRGGBB
        if (trimmed.startsWith("#") && trimmed.length == 4) {
            val r = trimmed[1]
            val g = trimmed[2]
            val b = trimmed[3]
            return "#FF$r$r$g$g$b$b".uppercase(Locale.ROOT)
        }

        // #RRGGBB -> #FFRRGGBB
        if (trimmed.startsWith("#") && trimmed.length == 7) {
            return "#FF" + trimmed.substring(1).uppercase(Locale.ROOT)
        }

        return color
    }
}