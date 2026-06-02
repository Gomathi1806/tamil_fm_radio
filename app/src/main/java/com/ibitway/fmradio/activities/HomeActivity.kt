package com.ibitway.fmradio.activities

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.gms.ads.AdRequest
import com.ibitway.fmradio.Config
import com.ibitway.fmradio.R
import com.ibitway.fmradio.adapters.RadioListingAdapter
import com.ibitway.fmradio.services.RadioManager
import com.ibitway.fmradio.utilities.Log
import com.ibitway.fmradio.utilities.Tools
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity : AppCompatActivity() {

    private var adIndex = 0
    var adHandler = Handler()
    lateinit var runnable: Runnable
    var delay = 10 * 1000 //Delay for 15 seconds.  One second = 1000 milliseconds.


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        Tools.initBitmap(this)

        recyclerView.adapter = RadioListingAdapter(this, Config.socialList)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        val adRequest = AdRequest.Builder().build()
        admobView.loadAd(adRequest)

        //loadAd()
    }

    override fun onBackPressed() {
        Log.d("CDA", "onBackPressed Called")
        exitDialog()
    }

    private fun exitDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setIcon(R.mipmap.ic_launcher)
        dialog.setTitle(R.string.app_name)
        dialog.setMessage(resources.getString(R.string.message))
        dialog.setPositiveButton(
            resources.getString(R.string.quit)
        ) { dialogInterface: DialogInterface?, i: Int ->
            RadioManager.with().stopServices()
            finish()
        }
        dialog.setNegativeButton(
            resources.getString(R.string.minimize)
        ) { dialogInterface: DialogInterface?, i: Int -> minimizeApp() }
        dialog.setNeutralButton(
            resources.getString(R.string.cancel)
        ) { dialogInterface: DialogInterface?, i: Int -> }
        dialog.show()
    }

    private fun minimizeApp() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }


    private fun loadAd() {
        adHandler.postDelayed(object : Runnable {
            override fun run() {
                refreshAdView()
                runnable = this
                adHandler.postDelayed(runnable, delay.toLong())
            }
        }, delay.toLong())
    }

    private fun refreshAdView() {

        adIndex = Tools.randInt(0, Config.banners.size)

        if (adIndex >= Config.banners.size) {
            adIndex -= 1
        }

        if (Config.banners[adIndex].image != null) {

            Picasso.with(this).load(Config.banners[adIndex].image)
                .fit()
                .into(adView)

            adView.setOnClickListener {
                startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(Config.banners[adIndex].url))
                )
            }
        }
    }
}