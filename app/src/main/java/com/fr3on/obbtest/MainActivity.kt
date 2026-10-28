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
        text = "اضغط الزر لفحص ملف البيانات"
        textSize = 17f
        gravity = Gravity.CENTER
        setPadding(0, 30, 0, 30)
    }

    val button = Button(this).apply {
        text = "فحص ملف OBB"
        setOnClickListener {
            val obbDir = File(
                android.os.Environment.getExternalStorageDirectory(),
                "Android/obb/$packageName"
            )

            val files = obbDir.listFiles()
            status.text = if (files != null && files.any { it.extension == "obb" }) {
                "تم العثور على ملف OBB"
            } else {
                "لم يتم العثور على ملف OBB في المسار المتوقع"
            }
        }
    }

    layout.addView(title)
    layout.addView(status)
    layout.addView(
        button,
        ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    )

    setContentView(layout)
}

}
