package com.fr3on.obbtest

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.io.File

class MainActivity : Activity() {

    private lateinit var status: TextView

    companion object {
        private const val PICK_FILE_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "FR3ON OBB TEST"
            textSize = 25f
            gravity = Gravity.CENTER
        }

        status = TextView(this).apply {
            text = "جاهز لاختبار ملفات البيانات"
            textSize = 17f
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 24)
        }

        val createButton = Button(this).apply {
            text = "إنشاء ملف OBB تجريبي"

            setOnClickListener {
                try {
                    val testDir = File(filesDir, "obb-test")

                    if (!testDir.exists() && !testDir.mkdirs()) {
                        throw Exception("تعذر إنشاء مجلد الاختبار")
                    }

                    val testFile = File(
                        testDir,
                        "main.1.com.fr3on.obbtest.obb"
                    )

                    testFile.writeText(
                        "FR3ON OBB TEST\nTest data file\n"
                    )

                    status.text =
                        "تم إنشاء الملف التجريبي بنجاح\n" +
                        "الحجم: ${testFile.length()} بايت"

                } catch (e: Exception) {
                    status.text =
                        "فشل إنشاء الملف:\n${e.message}"
                }
            }
        }

        val checkButton = Button(this).apply {
            text = "فحص الملف التجريبي"

            setOnClickListener {
                try {
                    val testFile = File(
                        filesDir,
                        "obb-test/main.1.com.fr3on.obbtest.obb"
                    )

                    status.text = if (testFile.isFile) {
                        val content = testFile.readText()

                        "الملف موجود ويمكن قراءته بنجاح\n\n" +
                        "الحجم: ${testFile.length()} بايت\n\n" +
                        "المحتوى:\n$content"
                    } else {
                        "الملف غير موجود.\n" +
                        "اضغط إنشاء ملف OBB تجريبي أولًا."
                    }

                } catch (e: Exception) {
                    status.text =
                        "فشل فحص الملف:\n${e.message}"
                }
            }
        }

        val pickButton = Button(this).apply {
            text = "اختيار ملف من الموبايل"

            setOnClickListener {
                try {
                    val intent = Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                    ).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "*/*"

                        putExtra(
                            Intent.EXTRA_MIME_TYPES,
                            arrayOf(
                                "application/octet-stream",
                                "application/vnd.android.obb",
                                "*/*"
                            )
                        )
                    }

                    startActivityForResult(
                        intent,
                        PICK_FILE_REQUEST
                    )

                } catch (e: Exception) {
                    status.text =
                        "تعذر فتح أداة اختيار الملفات:\n${e.message}"
                }
            }
        }

        layout.addView(title)
        layout.addView(status)

        listOf(
            createButton,
            checkButton,
            pickButton
        ).forEach { button ->
            layout.addView(
                button,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        setContentView(layout)
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

        val uri: Uri = data?.data ?: run {
            status.text = "لم يتم استلام الملف المحدد."
            return
        }

        try {
            val fileName = getFileName(uri)

            status.text = "جارٍ فحص الملف...\n$fileName"

            Thread {
                try {
                    var totalBytes = 0L

                    contentResolver.openInputStream(uri)?.use { input ->
                        val buffer = ByteArray(64 * 1024)

                        while (true) {
                            val count = input.read(buffer)

                            if (count == -1) break

                            totalBytes += count
                        }
                    } ?: throw Exception("تعذر فتح الملف المحدد.")

                    runOnUiThread {
                        status.text =
                            "تم فتح الملف وقراءته بنجاح\n\n" +
                            "الاسم: $fileName\n\n" +
                            "الحجم المقروء: $totalBytes بايت\n\n" +
                            "لم يتم نقل الملف أو تعديله."
                    }

                } catch (e: Exception) {
                    runOnUiThread {
                        status.text =
                            "تعذرت قراءة الملف:\n${e.message}"
                    }
                }
            }.start()

        } catch (e: Exception) {
            status.text =
                "حدث خطأ أثناء فحص الملف:\n${e.message}"
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = uri.lastPathSegment ?: "ملف غير معروف"

        try {
            contentResolver.query(
                uri,
                null,
                null,
                null,
                null
            )?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(
                    android.provider.OpenableColumns.DISPLAY_NAME
                )

                if (cursor.moveToFirst() && nameIndex >= 0) {
                    name = cursor.getString(nameIndex) ?: name
                }
            }
        } catch (_: Exception) {
            // استخدام الاسم البديل إذا تعذر الحصول على الاسم الأصلي.
        }

        return name
    }
}
