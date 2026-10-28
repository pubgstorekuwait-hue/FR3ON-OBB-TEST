package com.fr3on.obbtest

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.io.File

class MainActivity : Activity() {

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

    val status = TextView(this).apply {
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
                "الملف غير موجود.\nاضغط إنشاء ملف OBB تجريبي أولًا."
            }
        }
    }

    layout.addView(title)
    layout.addView(status)

    layout.addView(
        createButton,
        ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    )

    layout.addView(
        checkButton,
        ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    )

    setContentView(layout)
}

}
