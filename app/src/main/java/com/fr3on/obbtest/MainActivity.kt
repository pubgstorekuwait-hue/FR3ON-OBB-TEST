package com.fr3on.obbtest

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.content.Intent
import android.net.Uri
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
                status.text = "فشل إنشاء الملف:\n${e.message}"
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
                status.text = "فشل فحص الملف:\n${e.message}"
            }
        }
    }

    val pickButton = Button(this).apply {
        text = "اختيار ملف من الموبايل"

        setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }

            startActivityForResult(intent, PICK_FILE_REQUEST)
        }
    }

    layout.addView(title)
    layout.addView(status)

    listOf(createButton, checkButton, pickButton).forEach { button ->
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

    if (requestCode == PICK_FILE_REQUEST &&
        resultCode == RESULT_OK
    ) {
        val uri: Uri = data?.data ?: return

        try {
            val fileName = uri.lastPathSegment ?: "ملف مختار"

            contentResolver.openInputStream(uri)?.use { input ->
                val buffer = ByteArray(8192)
                var totalBytes = 0L

                while (true) {
                    val count = input.read(buffer)
                    if (count == -1) break
                    totalBytes += count
                }

                status.text =
                    "تم فتح الملف وقراءته بنجاح\n\n" +
                    "الاسم: $fileName\n" +
                    "الحجم المقروء: $totalBytes بايت"
            } ?: run {
                status.text = "تعذر فتح الملف المحدد."
            }
        } catch (e: Exception) {
            status.text = "حدث خطأ أثناء قراءة الملف:\n${e.message}"
        }
    }
}

}
