package com.fr3on.obbtest

import android.app.Activity
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import java.io.File
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var apkUrl: EditText
    private lateinit var obbUrl: EditText
    private lateinit var status: TextView
    private lateinit var progress: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var apkButton: Button
    private lateinit var obbButton: Button
    private lateinit var installButton: Button
    private lateinit var cancelButton: Button

    private lateinit var downloadManager: DownloadManager

    private val handler = Handler(Looper.getMainLooper())

    private var apkDownloadId = -1L
    private var obbDownloadId = -1L

    private var apkFile: File? = null
    private var obbFile: File? = null

    private var activeDownloadId = -1L
    private var activeType = ""

    private var downloading = false

    private val pollTask = object : Runnable {
        override fun run() {
            if (!downloading) return

            updateDownloadProgress()
            handler.postDelayed(this, 500)
        }
    }

    companion object {
        private const val BG = 0xFF101114.toInt()
        private const val CARD = 0xFF1C1F24.toInt()
        private const val RED = 0xFFE53945.toInt()
        private const val WHITE = 0xFFFFFFFF.toInt()
        private const val MUTED = 0xFFB8BEC8.toInt()
        private const val GREEN = 0xFF45D483.toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        downloadManager =
            getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        buildInterface()
        restoreDownloads()
    }

    private fun buildInterface() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(BG)
        }

        val scroll = ScrollView(this)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(28))
        }

        val brand = TextView(this).apply {
            text = "FR3ON"
            textSize = 36f
            gravity = Gravity.CENTER
            setTextColor(RED)
            typeface = Typeface.DEFAULT_BOLD
        }

        val subtitle = TextView(this).apply {
            text = "GAME INSTALLER"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }

        val description = TextView(this).apply {
            text = "تحميل الألعاب وتجهيز ملفات البيانات"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(MUTED)
            setPadding(0, dp(8), 0, dp(22))
        }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))

            background = GradientDrawable().apply {
                setColor(CARD)
                cornerRadius = dp(16).toFloat()
                setStroke(dp(1), 0xFF30343B.toInt())
            }
        }

        val cardTitle = TextView(this).apply {
            text = "حالة التحميل"
            textSize = 18f
            setTextColor(WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }

        status = TextView(this).apply {
            text = "جاهز لتحميل ملفات اللعبة."
            textSize = 14f
            setTextColor(MUTED)
            setPadding(0, dp(12), 0, dp(12))
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
            setTextColor(GREEN)
            setPadding(0, dp(8), 0, dp(8))
        }

        card.addView(cardTitle)
        card.addView(status)
        card.addView(progress)
        card.addView(progressText)

        val apkTitle = label("رابط ملف APK")

        apkUrl = editText(
            "https://example.com/game.apk"
        )

        apkButton = button("تحميل APK", RED) {
            startDownload(apkUrl.text.toString(), "apk")
        }

        val obbTitle = label("رابط ملف OBB")

        obbUrl = editText(
            "https://example.com/main.1.package.obb"
        )

        obbButton = button("تحميل OBB", 0xFF30343B.toInt()) {
            startDownload(obbUrl.text.toString(), "obb")
        }

        installButton = button(
            "تثبيت ملف APK",
            GREEN,
            0xFF101114.toInt()
        ) {
            installApk()
        }

        cancelButton = button(
            "إلغاء التحميل",
            0xFF7B252B.toInt()
        ) {
            cancelDownload()
        }

        val note = TextView(this).apply {
            text =
                "مهم: استخدم روابط HTTPS مباشرة لملفات تملكها " +
                "أو لديك إذن بتنزيلها.\n\n" +
                "ملف OBB يُحفظ في مجلد التطبيق أولًا. " +
                "نقله إلى مجلد لعبة أخرى يحتاج معالجة منفصلة، " +
                "وقد تمنعه قيود أندرويد."
            textSize = 12f
            setTextColor(MUTED)
            setPadding(0, dp(18), 0, 0)
            gravity = Gravity.CENTER
        }

        content.addView(brand)
        content.addView(subtitle)
        content.addView(description)
        content.addView(card)
        content.addView(space(20))
        content.addView(apkTitle)
        content.addView(apkUrl, fullWidth())
        content.addView(space(8))
        content.addView(apkButton, fullWidth())
        content.addView(space(20))
        content.addView(obbTitle)
        content.addView(obbUrl, fullWidth())
        content.addView(space(8))
        content.addView(obbButton, fullWidth())
        content.addView(space(12))
        content.addView(installButton, fullWidth())
        content.addView(space(8))
        content.addView(cancelButton, fullWidth())
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

        cancelButton.isEnabled = false
        installButton.isEnabled = false
    }

    private fun startDownload(rawUrl: String, type: String) {
        if (downloading) {
            toast("يوجد تحميل جارٍ بالفعل.")
            return
        }

        val url = rawUrl.trim()

        if (url.isEmpty() ||
            url.startsWith("https://example.com")
        ) {
            toast("ضع رابط الملف الحقيقي أولًا.")
            return
        }

        val uri = try {
            Uri.parse(url)
        } catch (_: Exception) {
            toast("الرابط غير صالح.")
            return
        }

        if (uri.scheme != "https" || uri.host.isNullOrBlank()) {
            toast("استخدم رابط HTTPS مباشرًا وآمنًا.")
            return
        }

        val directory = getExternalFilesDir(
            Environment.DIRECTORY_DOWNLOADS
        ) ?: run {
            toast("تعذر الوصول إلى مجلد التنزيلات.")
            return
        }

        if (!directory.exists() && !directory.mkdirs()) {
            toast("تعذر إنشاء مجلد التنزيلات.")
            return
        }

        val filename = if (type == "apk") {
            "fr3on-game.apk"
        } else {
            "fr3on-game.obb"
        }

        val destination = File(directory, filename)

        if (destination.exists() && !destination.delete()) {
            toast("تعذر استبدال الملف القديم.")
            return
        }

        try {
            val request = DownloadManager.Request(uri)
                .setTitle(
                    if (type == "apk") {
                        "FR3ON - تنزيل اللعبة"
                    } else {
                        "FR3ON - تنزيل بيانات اللعبة"
                    }
                )
                .setDescription("جارٍ تنزيل الملف...")
                .setNotificationVisibility(
                    DownloadManager.Request
                        .VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)
                .setDestinationInExternalFilesDir(
                    this,
                    Environment.DIRECTORY_DOWNLOADS,
                    filename
                )

            activeDownloadId = downloadManager.enqueue(request)
            activeType = type
            downloading = true

            if (type == "apk") {
                apkDownloadId = activeDownloadId
                apkFile = destination
            } else {
                obbDownloadId = activeDownloadId
                obbFile = destination
            }

            apkButton.isEnabled = false
            obbButton.isEnabled = false
            cancelButton.isEnabled = true
            installButton.isEnabled = false

            progress.progress = 0
            progressText.text = "جارٍ بدء التحميل..."
            status.text = "بدأ تنزيل ${if (type == "apk") "APK" else "OBB"}"

            handler.removeCallbacks(pollTask)
            handler.post(pollTask)

        } catch (e: Exception) {
            downloading = false
            toast("تعذر بدء التحميل: ${e.message}")
        }
    }

    private fun updateDownloadProgress() {
        if (!downloading || activeDownloadId < 0) return

        var cursor: android.database.Cursor? = null

        try {
            cursor = downloadManager.query(
                DownloadManager.Query().setFilterById(activeDownloadId)
            )

            if (cursor == null || !cursor.moveToFirst()) {
                finishDownload(false, "لم يتم العثور على مهمة التحميل.")
                return
            }

            val statusIndex = cursor.getColumnIndex(
                DownloadManager.COLUMN_STATUS
            )

            val reasonIndex = cursor.getColumnIndex(
                DownloadManager.COLUMN_REASON
            )

            val downloadedIndex = cursor.getColumnIndex(
                DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR
            )

            val totalIndex = cursor.getColumnIndex(
                DownloadManager.COLUMN_TOTAL_SIZE_BYTES
            )

            if (statusIndex < 0) {
                finishDownload(false, "تعذر قراءة حالة التحميل.")
                return
            }

            val downloadStatus = cursor.getInt(statusIndex)

            val downloaded = if (downloadedIndex >= 0) {
                cursor.getLong(downloadedIndex)
            } else {
                0L
            }

            val total = if (totalIndex >= 0) {
                cursor.getLong(totalIndex)
            } else {
                -1L
            }

            when (downloadStatus) {
                DownloadManager.STATUS_PENDING,
                DownloadManager.STATUS_RUNNING -> {
                    if (total > 0) {
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

                    status.text = "جارٍ تنزيل الملف..."
                }

                DownloadManager.STATUS_SUCCESSFUL -> {
                    finishDownload(
                        true,
                        "اكتمل تنزيل الملف بنجاح."
                    )
                }

                DownloadManager.STATUS_FAILED -> {
                    val reason = if (reasonIndex >= 0) {
                        cursor.getInt(reasonIndex)
                    } else {
                        -1
                    }

                    finishDownload(
                        false,
                        "فشل التحميل. رمز الخطأ: $reason"
                    )
                }
            }

        } catch (e: Exception) {
            finishDownload(
                false,
                "حدث خطأ أثناء متابعة التحميل: ${e.message}"
            )
        } finally {
            cursor?.close()
        }
    }

    private fun finishDownload(success: Boolean, message: String) {
        if (!downloading) return

        downloading = false
        handler.removeCallbacks(pollTask)

        apkButton.isEnabled = true
        obbButton.isEnabled = true
        cancelButton.isEnabled = false

        if (success) {
            progress.progress = 100
            progressText.text = "100% — اكتمل التنزيل"

            if (activeType == "apk") {
                val file = apkFile

                if (file != null && file.isFile && file.length() > 0L) {
                    installButton.isEnabled = true
                } else {
                    status.text = "اكتمل التنزيل، لكن ملف APK غير صالح."
                    return
                }
            } else {
                val file = obbFile

                if (file != null && file.isFile && file.length() > 0L) {
                    status.text =
                        "$message\n\n" +
                        "المسار:\n${file.absolutePath}"
                } else {
                    status.text = "اكتمل التنزيل، لكن ملف OBB غير موجود."
                    return
                }
            }
        } else {
            installButton.isEnabled =
                apkFile?.isFile == true &&
                (apkFile?.length() ?: 0L) > 0L
        }

        status.text = message
    }

    private fun cancelDownload() {
        if (!downloading) return

        val id = activeDownloadId

        handler.removeCallbacks(pollTask)

        try {
            downloadManager.remove(id)
        } catch (_: Exception) {
        }

        downloading = false

        apkButton.isEnabled = true
        obbButton.isEnabled = true
        cancelButton.isEnabled = false

        progressText.text = "تم إلغاء التحميل."
        status.text = "تم إلغاء مهمة التحميل."

        toast("تم إلغاء التحميل.")
    }

    private fun restoreDownloads() {
        val directory = getExternalFilesDir(
            Environment.DIRECTORY_DOWNLOADS
        ) ?: return

        val apk = File(directory, "fr3on-game.apk")
        val obb = File(directory, "fr3on-game.obb")

        if (apk.isFile && apk.length() > 0L) {
            apkFile = apk
            installButton.isEnabled = true
        }

        if (obb.isFile && obb.length() > 0L) {
            obbFile = obb
        }
    }

    private fun installApk() {
        val file = apkFile

        if (file == null || !file.isFile || file.length() <= 0L) {
            toast("نزّل ملف APK صالحًا أولًا.")
            return
        }

        if (android.os.Build.VERSION.SDK_INT >= 26 &&
            !packageManager.canRequestPackageInstalls()
        ) {
            status.text =
                "اسمح للتطبيق بتثبيت ملفات APK من إعدادات أندرويد."

            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:$packageName")
            )

            try {
                startActivity(intent)
            } catch (_: Exception) {
                toast("افتح إعدادات تثبيت التطبيقات يدويًا.")
            }

            return
        }

        try {
            val uri = downloadManager.getUriForDownloadedFile(
                apkDownloadId
            )

            if (uri != null) {
                openInstaller(uri)
                return
            }

            toast(
                "لم يُعثر على رابط التنزيل. أعد تنزيل ملف APK."
            )

        } catch (e: Exception) {
            status.text =
                "تعذر فتح المثبّت:\n${e.message}"
        }
    }

    private fun openInstaller(uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    uri,
                    "application/vnd.android.package-archive"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(intent)

        } catch (e: Exception) {
            status.text =
                "تعذر تشغيل مثبّت أندرويد.\n${e.message}"
        }
    }

    private fun label(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(WHITE)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, dp(8))
        }
    }

    private fun editText(hintText: String): EditText {
        return EditText(this).apply {
            hint = hintText
            textSize = 13f
            setSingleLine(true)
            setTextColor(WHITE)
            setHintTextColor(MUTED)
            setPadding(dp(12), dp(10), dp(12), dp(10))

            background = GradientDrawable().apply {
                setColor(CARD)
                cornerRadius = dp(10).toFloat()
                setStroke(dp(1), 0xFF454A53.toInt())
            }
        }
    }

    private fun button(
        textValue: String,
        color: Int,
        textColor: Int = WHITE,
        action: () -> Unit
    ): Button {
        return Button(this).apply {
            text = textValue
            textSize = 14f
            isAllCaps = false
            setTextColor(textColor)

            background = GradientDrawable().apply {
                setColor(color)
                cornerRadius = dp(12).toFloat()
            }

            setPadding(dp(12), dp(8), dp(12), dp(8))
            setOnClickListener { action() }
        }
    }

    private fun fullWidth(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun space(height: Int): android.view.View {
        return android.view.View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                1,
                dp(height)
            )
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024L) return "$bytes بايت"

        val kb = bytes / 1024.0
        if (kb < 1024.0) {
            return String.format(Locale.US, "%.2f KB", kb)
        }

        val mb = kb / 1024.0
        if (mb < 1024.0) {
            return String.format(Locale.US, "%.2f MB", mb)
        }

        return String.format(Locale.US, "%.2f GB", mb / 1024.0)
    }

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
