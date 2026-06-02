package com.ibitway.fmradio.activities

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.ibitway.fmradio.Config
import com.ibitway.fmradio.R
import com.ibitway.fmradio.utilities.Tools
import kotlinx.android.synthetic.main.activity_about2.*


class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about2)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (Tools.htmlValidate(Config.ABOUT_DATA)){
            loadHTml(Config.ABOUT_DATA)
        }
        else{
            loadText(Config.ABOUT_DATA)
        }
    }

    private fun loadText(text:String){
        aboutTv.visibility = View.VISIBLE
        webView.visibility = View.GONE
        aboutTv.text = text
    }

    private fun loadHTml(htmlString:String){
        aboutTv.visibility = View.GONE
        webView.visibility = View.VISIBLE

        webView.settings.loadsImagesAutomatically = true
        webView.settings.javaScriptEnabled = true
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        // Configure the client to use when opening URLs
        // Configure the client to use when opening URLs
        webView.webViewClient = WebViewClient()

        webView.loadDataWithBaseURL(null,htmlString,"text/html", "utf-8", null)
    }
}