package com.bytetwins.hei.mode

import android.content.Context
import java.io.File

private const val ID_CARD_FILE_NAME = "hei_id_card.txt"

data class IdCardData(
    val name: String = "",
    val role: String = "",
    val dataLink: String = ""
)

object IdCardStorage {

    fun save(context: Context, data: IdCardData) {
        val file = File(context.filesDir, ID_CARD_FILE_NAME)
        // 简单用 \n 分隔三行：name, role, dataLink
        file.writeText(listOf(data.name, data.role, data.dataLink).joinToString("\n"))
    }

    fun load(context: Context): IdCardData {
        val file = File(context.filesDir, ID_CARD_FILE_NAME)
        if (!file.exists()) return IdCardData()
        return try {
            val lines = file.readLines()
            val name = lines.getOrNull(0) ?: ""
            val role = lines.getOrNull(1) ?: ""
            val dataLink = lines.getOrNull(2) ?: ""
            IdCardData(name, role, dataLink)
        } catch (_: Exception) {
            IdCardData()
        }
    }
}

