package com.intas.metrolog.util

import android.util.Xml
import com.intas.metrolog.R
import io.github.tonnyl.whatsnew.item.WhatsNewItem
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

object ChangeLogXmlParser {
    private val ns: String? = null

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): List<WhatsNewItem> {
        inputStream.use { iStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(iStream, null)
            parser.nextTag()
            return readChangeLog(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readChangeLog(parser: XmlPullParser): List<WhatsNewItem> {
        val entries = mutableListOf<WhatsNewItem>()

        parser.require(XmlPullParser.START_TAG, ns, "changelog")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            if (parser.name == "entry") {
                entries.add(readEntry(parser))
            } else {
                skip(parser)
            }
        }
        return entries
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readEntry(parser: XmlPullParser): WhatsNewItem {
        parser.require(XmlPullParser.START_TAG, ns, "entry")
        var title = ""
        var content = ""
        var imageRes = 0
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "title" -> {
                    title = readTitle(parser)
                    imageRes = setImageRes(title)
                }
                "summary" -> content = readSummary(parser).replace("    ", " ")
                else -> skip(parser)
            }
        }
        return WhatsNewItem(title, content, imageRes)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTitle(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "title")
        val title = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "title")
        return title
    }


    private fun setImageRes(title: String): Int {
        return when(title) {
            "Изменения" -> R.drawable.ic_baseline_change_circle_red_24
            "Исправления" -> R.drawable.ic_baseline_bug_report_red_24
            "Новые функции" -> R.drawable.ic_baseline_new_releases_red_24
            else -> WhatsNewItem.NO_IMAGE_RES_ID
        }

    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readSummary(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "summary")
        val summary = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "summary")
        return summary
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}