package com.bytetwins.hei.mode

import android.content.Context
import java.io.File

private const val MODE_FILE_NAME = "hei_mode.txt"

/**
 * 简单的模式持久化：将当前模式写入 app 私有目录下的文件，并设置世界可读权限，方便其他应用访问。
 */
object ModeStorage {

    fun saveModeId(context: Context, modeId: String) {
        val file = File(context.filesDir, MODE_FILE_NAME)
        file.writeText(modeId)
        @Suppress("DEPRECATION")
        file.setReadable(true, false)
    }

    fun loadModeId(context: Context): String? {
        val file = File(context.filesDir, MODE_FILE_NAME)
        if (!file.exists()) return null
        return try {
            file.readText().trim()
        } catch (_: Exception) {
            null
        }
    }

    fun modeFilePath(context: Context): String =
        File(context.filesDir, MODE_FILE_NAME).absolutePath
}
