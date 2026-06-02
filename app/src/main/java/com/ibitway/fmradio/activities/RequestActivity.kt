package com.ibitway.fmradio.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ibitway.fmradio.Config
import com.ibitway.fmradio.R
import kotlinx.android.synthetic.main.activity_request.*


class RequestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        submitBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/html"
            intent.putExtra(Intent.EXTRA_EMAIL, Config.REQUEST)
            intent.putExtra(Intent.EXTRA_SUBJECT, "Song Request")
            intent.putExtra(Intent.EXTRA_TEXT, "${nameEtv.text}\n${songEtv.text.toString()}")

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(Intent.createChooser(intent, "Send Email"))
            } else {
                Toast.makeText(
                    this,
                    "Email app not installed in your phone",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}