package com.fr3on.obbtest

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import java.io.File
import java.io.InputStream
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var cancelButton: Button
    private lateinit var scanButton: Button

    private val mainHandler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()

    private var scanTask: Future<*>? = null
    private var cancelFlag: AtomicBoolean? = null
    private var scanning = false

    companion object {
        private const val PICK_FILE_REQUEST = 1001

        private const val BG = 0xFF101114.toInt()
        private const val CARD = 0xFF1C1F24.toInt()
        private const val RED = 0xFFE53945.toInt()
        private const val WHITE = 0xFFFFFFFF.toInt()
        private const val MUTED = 0xFFB8BEC8.toInt()
        private const val GREEN = 0xFF45D483.toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            setPadding(dp(20), dp(24), dp(20), dp(28))
        }

        val brand = TextView(this).apply {
            text = "FR3ON"
            textSize = 34f
            setTextColor(RED)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "GAME INSTALLER"
            textSize = 16f
            setTextColor(WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        val description = TextView(this).apply {
            text = "أداة فحص وتجهيز ملفات الألعاب"
            textSize = 15f
            setTextColor(MUTED)
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(24))
        }

        val statusCard = card()

        val statusTitle = TextView(this).apply {
            text = "حالة التطبيق"
            textSize = 17f
            setTextColor(WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }

        status = TextView(this).apply {
            text = "جاهز للعمل\nاختار عملية من الخيارات التالية."
            textSize = 14f
            setTextColor(MUTED)
            setPadding(0, dp(12), 0, dp(12))
        }

        progressBar = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            max = 100
            progress = 0
            visibility = View.GONE
        }

        progressText = TextView(this).apply {
            text = ""
            textSize = 13f
            setTextColor(GREEN)
            setPadding(0, dp(8), 0, 0)
            visibility = View.GONE
        }

        statusCard.addView(statusTitle)
        statusCard.addView(status)
        statusCard.addView(progressBar)
        statusCard.addView(progressText)

        val createButton = makeButton(
            "إنشاء ملف OBB تجريبي",
            RED
        ) {
            createTestFile()
        }

        val checkButton = makeButton(
            "فحص ملف OBB التجريبي",
            0xFF30343B.toInt()
        ) {
            checkTestFile()
        }

        scanButton = makeButton(
            "اختيار وفحص ملف من الموبايل",
            0xFF30343B.toInt()
        ) {
            pickFile()
        }

        cancelButton = makeButton(
            "إلغاء الفحص",
            0xFF7B252B.toInt()
        ) {
            cancelScan()
        }.apply {
            visibility = View.GONE
        }

        val note = TextView(this).apply {
            text = "ملاحظة: فحص الملف لا ينقله ولا يثبّت اللعبة. " +
                "ملفات OBB المحمية قد لا تظهر في منتقي الملفات على أندرويد الحديث."
            textSize = 12f
            setTextColor(MUTED)
            setPadding(dp(4), dp(18), dp(4), 0)
            gravity = Gravity.CENTER
        }

        content.addView(brand)
        content.addView(subtitle)
        content.addView(description)
        content.addView(statusCard)
        content.addView(space(16))
        content.addView(createButton, fullWidth())
        content.addView(space(10))
        content.addView(checkButton, fullWidth())
        content.addView(space(10))
        content.addView(scanButton, fullWidth())
        content.addView(space(10))
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
            textSize = 12f
            setPadding(0, dp(12), 0, dp(12))
        }

        root.addView(footer)
        setContentView(root)
    }

    private fun card(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = GradientDrawable().apply {
                setColor(CARD)
                cornerRadius = dp(16).toFloat()
                setStroke(dp(1), 0xFF30343B.toInt())
            }
        }
    }

    private fun makeButton(
        label: String,
        color: Int,
        action: () -> Unit
    ): Button {
        return Button(this).apply {
            text = label
            textSize = 14f
            isAllCaps = false
            setTextColor(WHITE)
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

    private fun space(height: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                1,
                dp(height)
            )
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun createTestFile() {
        if (scanning) {
            toast("انتظر حتى ينتهي الفحص الحالي.")
            return
        }

        try {
            val testDir = File(filesDir, "obb-test")

            if (!testDir.exists() && !testDir.mkdirs()) {
                throw Exception("تعذر إنشاء مجلد الاختبار.")
            }

            val testFile = File(
                testDir,
                "main.1.com.fr3on.obbtest.obb"
            )

            testFile.writeText(
                "FR3ON OBB TEST\nTest data file\n"
            )

            status.text =
                "تم إنشاء الملف التجريبي بنجاح.\n\n" +
                "الاسم: ${testFile.name}\n" +
                "الحجم: ${formatBytes(testFile.length())}\n" +
                "المسار داخل التطبيق:\n${testFile.absolutePath}"

            progressBar.visibility = View.GONE
            progressText.visibility = View.GONE

        } catch (e: Exception) {
            status.text = "فشل إنشاء الملف:\n${e.message}"
        }
    }

    private fun checkTestFile() {
        if (scanning) {
            toast("انتظر حتى ينتهي الفحص الحالي.")
            return
        }

        try {
            val testFile = File(
                filesDir,
                "obb-test/main.1.com.fr3on.obbtest.obb"
            )

            if (!testFile.isFile) {
                status.text =
                    "الملف التجريبي غير موجود.\n" +
                    "اضغط إنشاء ملف OBB تجريبي أولًا."
                return
            }

            val content = testFile.readText()

            status.text =
                "نجح فحص الملف التجريبي.\n\n" +
                "الاسم: ${testFile.name}\n" +
                "الحجم: ${formatBytes(testFile.length())}\n\n" +
                "المحتوى:\n$content"

            progressBar.visibility = View.GONE
            progressText.visibility = View.GONE

        } catch (e: Exception) {
            status.text = "فشل فحص الملف:\n${e.message}"
        }
    }

    private fun pickFile() {
        if (scanning) {
            toast("يوجد فحص جارٍ بالفعل.")
            return
        }

        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }

            startActivityForResult(intent, PICK_FILE_REQUEST)

        } catch (e: Exception) {
            status.text =
                "تعذر فتح أداة اختيار الملفات:\n${e.message}"
        }
    }

    @Deprecated("Deprecated in Android API")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != PICK_FILE_REQUEST) return

        if (resultCode != RESULT_OK) {
            status.text = "لم يتم اختيار ملف."
            return
        }

        val uri = data?.data

        if (uri == null) {
            status.text = "لم يتم استلام الملف المحدد."
            return
        }

        val fileName = getFileName(uri)

        startScan(uri, fileName)
    }

    private fun startScan(uri: Uri, fileName: String) {
        cancelFlag = AtomicBoolean(false)
        val flag = cancelFlag!!

        scanning = true
        scanButton.isEnabled = false
        cancelButton.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        progressBar.isIndeterminate = true
        progressText.visibility = View.VISIBLE
        progressText.text = "جارٍ فتح الملف..."
        progressBar.progress = 0

        status.text =
            "جارٍ فحص الملف المحدد.\n\n" +
            "الاسم: $fileName"

        scanTask = executor.submit {
            var totalBytes = 0L
            var expectedBytes = -1L
            var input: InputStream? = null

            try {
                contentResolver.query(
                    uri,
                    arrayOf(OpenableColumns.SIZE),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(
                            OpenableColumns.SIZE
                        )

                        if (index >= 0 && !cursor.isNull(index)) {
                            expectedBytes = cursor.getLong(index)
                        }
                    }
                }

                input = contentResolver.openInputStream(uri)
                    ?: throw Exception("تعذر فتح الملف.")

                val buffer = ByteArray(128 * 1024)

                while (true) {
                    if (flag.get() || Thread.currentThread().isInterrupted) {
                        throw InterruptedException("تم إلغاء الفحص.")
                    }

                    val count = input.read(buffer)

                    if (count == -1) break

                    totalBytes += count

                    val bytes = totalBytes
                    val expected = expectedBytes

                    mainHandler.post {
                        if (!scanning || flag.get()) return@post

                        if (expected > 0) {
                            val percent = (
                                bytes * 100L / expected
                            ).coerceIn(0L, 100L).toInt()

                            progressBar.isIndeterminate = false
                            progressBar.progress = percent

                            progressText.text =
                                "$percent% — ${formatBytes(bytes)} / " +
                                formatBytes(expected)
                        } else {
                            progressBar.isIndeterminate = true
                            progressText.text =
                                "تمت قراءة ${formatBytes(bytes)}"
                        }
                    }
                }

                if (expectedBytes >= 0 && totalBytes != expectedBytes) {
                    throw Exception(
                        "الحجم المقروء لا يطابق الحجم المتوقع."
                    )
                }

                mainHandler.post {
                    if (flag.get()) return@post

                    finishScan()

                    status.text =
                        "اكتمل فحص الملف بنجاح.\n\n" +
                        "الاسم: $fileName\n" +
                        "الحجم المقروء: ${formatBytes(totalBytes)}\n\n" +
                        "لم يتم نقل الملف أو تعديله أو تثبيته."

                    progressBar.isIndeterminate = false
                    progressBar.progress = 100
                    progressText.text = "اكتمل الفحص — 100%"
                }

            } catch (e: InterruptedException) {
                mainHandler.post {
                    finishScan()
                    status.text = "تم إلغاء فحص الملف."
                    progressText.text = "تم الإلغاء."
                }

            } catch (e: Exception) {
                mainHandler.post {
                    finishScan()
                    status.text =
                        "تعذر فحص الملف.\n\n" +
                        (e.message ?: "حدث خطأ غير معروف.")
                    progressText.text = "فشل الفحص."
                }

            } finally {
                try {
                    input?.close()
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun cancelScan() {
        if (!scanning) return

        cancelFlag?.set(true)
        scanTask?.cancel(true)

        finishScan()

        status.text = "تم طلب إلغاء الفحص."
        progressText.text = "تم الإلغاء."
    }

    private fun finishScan() {
        scanning = false
        scanButton.isEnabled = true
        cancelButton.visibility = View.GONE
    }

    private fun getFileName(uri: Uri): String {
        var name = uri.lastPathSegment ?: "ملف غير معروف"

        try {
            contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                val index = cursor.getColumnIndex(
                    OpenableColumns.DISPLAY_NAME
                )

                if (cursor.moveToFirst() && index >= 0) {
                    name = cursor.getString(index) ?: name
                }
            }
        } catch (_: Exception) {
        }

        return name
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes بايت"

        val kb = bytes / 1024.0
        if (kb < 1024) return "%.2f KB".format(kb)

        val mb = kb / 1024.0
        if (mb < 1024) return "%.2f MB".format(mb)

        val gb = mb / 1024.0
        return "%.2f GB".format(gb)
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        cancelFlag?.set(true)
        scanTask?.cancel(true)
        executor.shutdownNow()
        mainHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
