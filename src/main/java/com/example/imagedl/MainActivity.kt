package com.example.imagedl

import android.app.DownloadManager
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var input: EditText
    private lateinit var status: TextView
    private var lastUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pad = (24 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad * 2, pad, pad)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        input = EditText(this).apply {
            hint = "لینک عکس را اینجا پیست کنید"
            setSingleLine(true)
            layoutDirection = View.LAYOUT_DIRECTION_LTR
            gravity = Gravity.START
        }

        val pasteBtn = Button(this).apply {
            text = "پیست و دانلود"
            setOnClickListener { pasteFromClipboard() }
        }

        status = TextView(this).apply {
            setPadding(0, pad / 2, 0, 0)
        }

        root.addView(input)
        root.addView(pasteBtn)
        root.addView(status)
        setContentView(root)

        // اگر کاربر لینک را دستی پیست کرد (long-press → Paste) هم خودکار دانلود شود
        input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString()?.trim().orEmpty()
                if (isUrl(text) && text != lastUrl) download(text)
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })
    }

    private fun pasteFromClipboard() {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = cm.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString()?.trim().orEmpty()
        if (text.isEmpty()) {
            toast("کلیپ‌بورد خالی است")
            return
        }
        input.setText(text) // TextWatcher دانلود را شروع می‌کند
        if (!isUrl(text)) toast("این یک لینک معتبر نیست")
    }

    private fun isUrl(s: String) = s.startsWith("http://") || s.startsWith("https://")

    private fun download(url: String) {
        lastUrl = url
        try {
            val uri = Uri.parse(url)
            var name = uri.lastPathSegment?.substringBefore('?').orEmpty()
            if (name.isBlank() || !name.contains('.')) {
                name = "image_${System.currentTimeMillis()}.jpg"
            }

            val request = DownloadManager.Request(uri)
                .setTitle(name)
                .setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                .setAllowedOverMetered(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "ImageDL/$name")
                .allowScanningByMediaScanner()

            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)

            status.text = "دانلود شروع شد: $name\nمحل ذخیره: Pictures/ImageDL"
            toast("دانلود شروع شد")
        } catch (e: Exception) {
            status.text = "خطا: ${e.message}"
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
