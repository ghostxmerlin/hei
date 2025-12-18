package com.bytetwins.hei.mode

import android.content.Context
import java.io.File

private const val MODE_FILE_NAME = "hei_mode.txt"

/**
 * 简单的模式持久化：将当前模式写入 app 私有目录下的文件，并设置世界可读权限，方便其他应用访问。
 */
object ModeStorage {
    fun saveMode(context: Context, mode: HeiMode) {
        val file = File(context.filesDir, MODE_FILE_NAME)
        file.writeText(mode.id)
        // 尝试放宽权限：world-readable（注意：在新版本 Android 上可能被限制）
        @Suppress("DEPRECATION")
        file.setReadable(true, false)
    }

    fun loadMode(context: Context): HeiMode {
        val file = File(context.filesDir, MODE_FILE_NAME)
        if (!file.exists()) return HeiMode.LOGIC
        return try {
            val id = file.readText().trim()
            HeiMode.fromId(id)
        } catch (e: Exception) {
            HeiMode.LOGIC
        }
    }

    /**
     * 暴露模式文件的绝对路径，方便其他应用通过文件系统直接读取。
     */
    fun modeFilePath(context: Context): String = File(context.filesDir, MODE_FILE_NAME).absolutePath
}
