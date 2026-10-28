package com.fr3on.obbtest

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.util.Locale

class MainActivity : Activity() {

    companion object {
        // استبدل الرابط برابط APK الحقيقي للعبة.
        private const val APK_URL =
            "https://example.com/game.apk"

        // لو اللعبة تحتاج OBB، ضع رابطه هنا.
        // اتركه فارغًا إذا كانت اللعبة لا تحتاج ملف OBB منفصلًا.
        private const val OBB_URL = ""

        private const val BG = 0xFF101114.toInt()
        private const val CARD = 0xFF1C1F24.toInt()
        private const val RED = 0xFFE53945.toInt()
        private const val WHITE = 0xFFFFFFFF.toInt()
        private const val MUTED = 0xFFB8BEC8.toInt()
        private const val GREEN = 0xFF45D483.toInt()

        private const val DOWNLOAD_NONE = 0
        private const val DOWNLOAD_APK = 1
        private const val DOWNLOAD_OBB = 2
    }

    private lateinit var manager: DownloadManager
    private lateinit var status: TextView
    private lateinit var progress: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var downloadButton: Button
    private lateinit var playButton: Button

    private val handler = Handler(Looper.getMainLooper())

    private var apkDownloadId = -1L
    private var obbDownloadId = -1L
    private var activeDownloadId = -1L
    private var downloadStage = DOWNLOAD_NONE
    private var downloading = false

    private lateinit var apkFile: File
    private lateinit var obbFile: File

    private val poll = object : Runnable {
        override fun run() {
            if (downloading) {
                checkDownload()
                handler.postDelayed(this, 700)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        manager = getSystemService(
            Context.DOWNLOAD_SERVICE
        ) as DownloadManager

        val directory = getExternalFilesDir(
            Environment.DIRECTORY_DOWNLOADS
        ) ?: filesDir

        apkFile = File(directory, "fr3on-game.apk")
        obbFile = File(directory, "fr3on-game.obb")

        buildInterface()
        restoreState()
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
            setPadding(dp(20), dp(30), dp(20), dp(24))
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
            setPadding(0, dp(12), 0, dp(28))
        }

        val gameCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(26), dp(20), dp(26))

            background = GradientDrawable().apply {
                setColor(CARD)
                cornerRadius = dp(18).toFloat()
                setStroke(dp(1), 0xFF30343B.toInt())
            }
        }

        val gameIcon = TextView(this).apply {
            text = "🎮"
            textSize = 64f
            gravity = Gravity.CENTER
        }

        val gameName = TextView(this).apply {
            text = "لعبتك المفضلة"
            textSize = 23f
            gravity = Gravity.CENTER
            setTextColor(WHITE)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(12), 0, dp(8))
        }

        val gameDescription = TextView(this).apply {
            text = "تحميل ملفات اللعبة وتجهيزها للتثبيت"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(MUTED)
        }

        gameCard.addView(gameIcon)
        gameCard.addView(gameName)
        gameCard.addView(gameDescription)

        val statusCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))

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
        statusCard.addView(space(14))
        statusCard.addView(progress)
        statusCard.addView(progressText)

        downloadButton = makeButton(
            "تحميل اللعبة",
            RED,
            WHITE
        ) {
            startGameDownload()
        }

        playButton = makeButton(
            "فتح اللعبة",
            GREEN,
            BG
        ) {
            openInstalledGame()
        }

        val note = TextView(this).apply {
            text = "سيتم تنزيل الملفات وتجهيزها. " +
                "قد يطلب أندرويد تأكيد تثبيت اللعبة."
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(MUTED)
            setPadding(0, dp(20), 0, 0)
        }

        content.addView(logo)
        content.addView(subtitle)
        content.addView(description)
        content.addView(gameCard)
        content.addView(space(20))
        content.addView(statusCard)
        content.addView(space(20))
        content.addView(downloadButton, fullWidth())
        content.addView(space(10))
        content.addView(playButton, fullWidth())
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

        playButton.visibility = View.GONE
    }

    private fun startGameDownload() {
        if (downloading) {
            toast("التحميل جارٍ بالفعل.")
            return
        }

        if (!validHttpsUrl(APK_URL)) {
            status.text = "رابط اللعبة غير مجهز بعد."
            toast("يجب وضع رابط APK حقيقي في الكود.")
            return
        }

        downloading = true
        downloadButton.isEnabled = false
        progress.progress = 0

        startDownload(APK_URL, DOWNLOAD_APK)
    }

    private fun startDownload(url: String, stage: Int) {
        try {
            val uri = Uri.parse(url)

            val fileName = if (stage == DOWNLOAD_APK) {
                "fr3on-game.apk"
            } else {
                "fr3on-game.obb"
            }

            val request = DownloadManager.Request(uri)
                .setTitle("FR3ON - تحميل اللعبة")
                .setDescription("جارٍ تنزيل ملفات اللعبة")
                .setNotificationVisibility(
                    DownloadManager.Request
                        .VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)
                .setDestinationInExternalFilesDir(
                    this,
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName
                )

            activeDownloadId = manager.enqueue(request)
            downloadStage = stage

            if (stage == DOWNLOAD_APK) {
                apkDownloadId = activeDownloadId
            } else {
                obbDownloadId = activeDownloadId
            }

            status.text = if (stage == DOWNLOAD_APK) {
                "جارٍ تحميل اللعبة..."
            } else {
                "جارٍ تجهيز ملفات اللعبة..."
            }

            handler.removeCallbacks(poll)
            handler.post(poll)

        } catch (e: Exception) {
            finishWithError(
                "تعذر بدء التحميل: ${e.message}"
            )
        }
    }

    private fun checkDownload() {
        if (!downloading || activeDownloadId < 0L) return

        val cursor = manager.query(
            DownloadManager.Query().setFilterById(
                activeDownloadId
            )
        ) ?: return

        cursor.use {
            if (!it.moveToFirst()) {
                finishWithError("تعذر العثور على مهمة التحميل.")
                return
            }

            val state = it.getInt(
                it.getColumnIndexOrThrow(
                    DownloadManager.COLUMN_STATUS
                )
            )

            val downloaded = it.getLong(
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
                    downloaded * 100L / total
                ).coerceIn(0L, 100L).toInt()

                progress.progress = percent

                progressText.text =
                    "$percent% — ${formatBytes(downloaded)} / " +
                    formatBytes(total)
            } else {
                progressText.text =
                    "تم تنزيل ${formatBytes(downloaded)}"
            }

            when (state) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    if (downloadStage == DOWNLOAD_APK) {
                        if (!apkFile.isFile || apkFile.length() == 0L) {
                            finishWithError(
                                "ملف اللعبة غير موجود أو فارغ."
                            )
                            return
                        }

                        if (OBB_URL.isNotBlank()) {
                            if (!validHttpsUrl(OBB_URL)) {
                                finishWithError(
                                    "رابط ملفات اللعبة غير صالح."
                                )
                                return
                            }

                            status.text =
                                "اكتمل تنزيل اللعبة. " +
                                "جارٍ تنزيل ملفاتها الإضافية..."

                            startDownload(
                                OBB_URL,
                                DOWNLOAD_OBB
                            )
                        } else {
                            downloading = false
                            handler.removeCallbacks(poll)
                            downloadButton.isEnabled = true
                            status.text =
                                "اكتمل تنزيل اللعبة. جاهز للتثبيت."
                            progress.progress = 100
                            progressText.text = "اكتمل التنزيل"
                            installApk()
                        }
                    } else {
                        downloading = false
                        handler.removeCallbacks(poll)
                        downloadButton.isEnabled = true

                        status.text =
                            "اكتمل تنزيل ملفات اللعبة. جاهز للتثبيت."

                        progress.progress = 100
                        progressText.text = "اكتمل التنزيل"

                        installApk()
                    }
                }

                DownloadManager.STATUS_FAILED -> {
                    val reason = it.getInt(
                        it.getColumnIndexOrThrow(
                            DownloadManager.COLUMN_REASON
                        )
                    )

                    finishWithError(
                        "فشل التحميل. رمز الخطأ: $reason"
                    )
                }
            }
        }
    }

    private fun installApk() {
        if (!apkFile.isFile || apkFile.length() == 0L) {
            status.text = "ملف اللعبة غير جاهز للتثبيت."
            return
        }

        if (Build.VERSION.SDK_INT >= 26 &&
            !packageManager.canRequestPackageInstalls()
        ) {
            status.text =
                "اسمح للتطبيق بتثبيت الملفات من إعدادات أندرويد."

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

        val uri = manager.getUriForDownloadedFile(apkDownloadId)

        if (uri == null) {
            status.text =
                "تعذر فتح ملف التثبيت. أعد تنزيل اللعبة."
            return
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    uri,
                    "application/vnd.android.package-archive"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(intent)

            status.text =
                "اتبع تعليمات أندرويد لإكمال تثبيت اللعبة."

        } catch (e: Exception) {
            status.text =
                "تعذر فتح شاشة التثبيت: ${e.message}"
        }
    }

    private fun openInstalledGame() {
        toast("سيظهر زر اللعب بعد إضافة دعم التحقق من تثبيت اللعبة.")
    }

    private fun restoreState() {
        if (apkFile.isFile && apkFile.length() > 0L) {
            status.text = "يوجد ملف لعبة محفوظ. يمكنك بدء تنزيل نسخة جديدة."
        }
    }

    private fun finishWithError(message: String) {
        downloading = false
        handler.removeCallbacks(poll)
        downloadButton.isEnabled = true
        status.text = message
        toast(message)
    }

    private fun validHttpsUrl(value: String): Boolean {
        return try {
            val uri = Uri.parse(value)
            uri.scheme == "https" && !uri.host.isNullOrBlank() &&
                !value.contains("example.com")
        } catch (_: Exception) {
            false
        }
    }

    private fun makeButton(
        label: String,
        color: Int,
        textColor: Int,
        action: () -> Unit
    ) = Button(this).apply {
        text = label
        textSize = 16f
        isAllCaps = false
        setTextColor(textColor)
        background = GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(14).toFloat()
        }
        setPadding(dp(12), dp(12),
