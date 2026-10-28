package com.fr3on.obbtest

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

class MainActivity : Activity() {

    companion object {
        private const val GAME_URL =
            "https://github.com/Promastergame/tinyapk-lab/raw/refs/heads/main/Tetris/build/LowBlocks-release.apk"

        private const val BG = 0xFF101114.toInt()
        private const val CARD = 0xFF1C1F24.toInt()
        private const val RED = 0xFFE53945.toInt()
        private const val WHITE = 0xFFFFFFFF.toInt()
        private const val MUTED = 0xFFB8BEC8.toInt()
        private const val GREEN = 0xFF45D483.toInt()
    }

    private lateinit var manager: DownloadManager
    private lateinit var status: TextView
    private lateinit var progress: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var downloadButton: Button

    private val handler = Handler(Looper.getMainLooper())

    private var downloadId = -1L
    private var downloading = false

    private val apkName = "fr3on-test-game.apk"

    private val poll = object : Runnable {
        override fun run() {
            if (downloading) {
                checkDownload()
                handler.postDelayed(this, 500)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        manager = getSystemService(
            Context.DOWNLOAD_SERVICE
        ) as DownloadManager

        buildInterface()
    }

    private fun buildInterface() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(BG)
        }

        val scroll = ScrollView(this)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(22), dp(32), dp(22), dp(28))
        }

        val logo = TextView(this).apply {
            text = "FR3ON"
            textSize = 38f
            gravity = Gravity.CENTER
            setTextColor(RED)
            typeface = Typeface.DEFAULT_BOLD
        }

        val subtitle = TextView(this).apply {
            text = "GAME INSTALLER"
            textSize = 19f
            gravity = Gravity.CENTER
            setTextColor(WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }

        val description = TextView(this).apply {
            text = "حمّل لعبتك واستعد للعب"
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(MUTED)
            setPadding(0, dp(12), 0, dp(30))
        }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(28), dp(20), dp(28))

            background = GradientDrawable().apply {
                setColor(CARD)
                cornerRadius = dp(18).toFloat()
                setStroke(dp(1), 0xFF30343B.toInt())
            }
        }

        val icon = TextView(this).apply {
            text = "🎮"
            textSize = 64f
            gravity = Gravity.CENTER
        }

        val gameName = TextView(this).apply {
            text = "Tetris"
            textSize = 26f
            gravity = Gravity.CENTER
            setTextColor(WHITE)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(12), 0, dp(8))
        }

        val gameInfo = TextView(this).apply {
            text = "لعبة تجريبية صغيرة"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(MUTED)
        }

        card.addView(icon)
        card.addView(gameName)
        card.addView(gameInfo)

        val statusCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(18), dp(16), dp(18))

            background = GradientDrawable().apply {
                setColor(CARD)
                cornerRadius = dp(14).toFloat()
            }
        }

        status = TextView(this).apply {
            text = "جاهز لتحميل اللعبة"
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(WHITE)
        }

        progress = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            max = 100
            progress = 0
        }

        progressText = TextView(this).apply {
            text = "0%"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(GREEN)
            setPadding(0, dp(10), 0, 0)
        }

        statusCard.addView(status)
        statusCard.addView(space(16))
        statusCard.addView(progress)
        statusCard.addView(progressText)

        downloadButton = Button(this).apply {
            text = "تحميل اللعبة"
            textSize = 17f
            isAllCaps = false
            setTextColor(WHITE)

            background = GradientDrawable().apply {
                setColor(RED)
                cornerRadius = dp(14).toFloat()
            }

            setPadding(dp(12), dp(12), dp(12), dp(12))

            setOnClickListener {
                startDownload()
            }
        }

        val note = TextView(this).apply {
            text = "سيتم تنزيل اللعبة، ثم فتح شاشة التثبيت."
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(MUTED)
            setPadding(0, dp(20), 0, 0)
        }

        content.addView(logo)
        content.addView(subtitle)
        content.addView(description)
        content.addView(card)
        content.addView(space(20))
        content.addView(statusCard)
        content.addView(space(20))
        content.addView(downloadButton, fullWidth())
        content.addView(note)

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        val footer = TextView(this).apply {
            text = "FR3ON © 2026"
            gravity = Gravity.CENTER
            setTextColor(MUTED)
            setPadding(0, dp(12), 0, dp(12))
        }

        root.addView(footer)
        setContentView(root)
    }

    private fun startDownload() {
        if (downloading) {
            toast("التحميل جارٍ بالفعل.")
            return
        }

        try {
            val directory = getExternalFilesDir(
                Environment.DIRECTORY_DOWNLOADS
            ) ?: throw Exception("مجلد التنزيل غير متاح.")

            val oldFile = java.io.File(directory, apkName)

            if (oldFile.exists() && !oldFile.delete()) {
                throw Exception("تعذر حذف الملف القديم.")
            }

            val request = DownloadManager.Request(
                Uri.parse(GAME_URL)
            )
                .setTitle("FR3ON - Tetris")
                .setDescription("جارٍ تنزيل اللعبة")
                .setMimeType("application/vnd.android.package-archive")
                .setNotificationVisibility(
                    DownloadManager.Request
                        .VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)
                .setDestinationInExternalFilesDir(
                    this,
                    Environment.DIRECTORY_DOWNLOADS,
                    apkName
                )

            downloadId = manager.enqueue(request)
            downloading = true

            downloadButton.isEnabled = false
            progress.progress = 0
            status.text = "جارٍ تنزيل اللعبة..."

            handler.removeCallbacks(poll)
            handler.post(poll)

        } catch (e: Exception) {
            downloading = false
            downloadButton.isEnabled = true
            status.text = "تعذر بدء التنزيل."
            toast(e.message ?: "حدث خطأ.")
        }
    }

    private fun checkDownload() {
        if (!downloading || downloadId < 0L) return

        val cursor = manager.query(
            DownloadManager.Query().setFilterById(downloadId)
        ) ?: return

        cursor.use {
            if (!it.moveToFirst()) {
                finishError("تعذر العثور على مهمة التنزيل.")
                return
            }

            val state = it.getInt(
                it.getColumnIndexOrThrow(
                    DownloadManager.COLUMN_STATUS
                )
            )

            val received = it.getLong(
                it.getColumnIndexOrThrow(
                    DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR
                )
            )

            val total = it.getLong(
                it.getColumnIndexOrThrow(
                    DownloadManager.COLUMN_TOTAL_SIZE_BYTES
                )
            )

            if (total > 0L) {
                val percent = (
                    received * 100L / total
                ).coerceIn(0L, 100L).toInt()

                progress.progress = percent
                progressText.text =
                    "$percent% — ${formatBytes(received)} / ${formatBytes(total)}"
            } else {
                progressText.text =
                    "تم تنزيل ${formatBytes(received)}"
            }

            when (state) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    downloading = false
                    handler.removeCallbacks(poll)
                    downloadButton.isEnabled = true

                    progress.progress = 100
                    progressText.text = "اكتمل التنزيل"

                    status.text = "تم تنزيل اللعبة. جارٍ تجهيز التثبيت..."

                    openInstaller()
                }

                DownloadManager.STATUS_FAILED -> {
                    val reason = it.getInt(
                        it.getColumnIndexOrThrow(
                            DownloadManager.COLUMN_REASON
                        )
                    )

                    finishError("فشل التنزيل. رمز الخطأ: $reason")
                }
            }
        }
    }

    private fun openInstaller() {
        if (Build.VERSION.SDK_INT >= 26 &&
            !packageManager.canRequestPackageInstalls()
        ) {
            status.text = "اسمح بتثبيت التطبيقات من هذا المصدر."

            try {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:$packageName")
                    )
                )
            } catch (_: Exception) {
                toast("افتح إعدادات تثبيت التطبيقات.")
            }

            return
        }

        try {
            val uri = manager.getUriForDownloadedFile(downloadId)
                ?: throw Exception("تعذر العثور على الملف.")

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    uri,
                    "application/vnd.android.package-archive"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(intent)

            status.text = "أكمل التثبيت من شاشة أندرويد."

        } catch (e: Exception) {
            status.text = "تعذر فتح التثبيت: ${e.message}"
        }
    }

    private fun finishError(message: String) {
        downloading = false
        handler.removeCallbacks(poll)
        downloadButton.isEnabled = true
        status.text = message
        toast(message)
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024L) return "$bytes بايت"

        val kb = bytes / 1024.0

        if (kb < 1024.0) {
            return String.format(Locale.US, "%.2f KB", kb)
        }

        return String.format(
            Locale.US,
            "%.2f MB",
            kb / 1024.0
        )
    }

    private fun makeSpace(height: Int) = space(height)

    private fun space(height: Int) =
        android.view.View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                1,
                dp(height)
            )
        }

    private fun fullWidth() =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

    private fun dp(value: Int) =
        (value * resources.displayMetrics.density).toInt()

    private fun toast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
