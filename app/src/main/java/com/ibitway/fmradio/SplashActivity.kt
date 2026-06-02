package com.ibitway.fmradio

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.google.android.gms.ads.MobileAds
import com.google.firebase.firestore.FirebaseFirestore
import com.ibitway.fmradio.activities.HomeActivity
import com.ibitway.fmradio.activities.MainActivity
import com.ibitway.fmradio.models.SocialObject
import com.ibitway.fmradio.utilities.isContainsContact
import com.ibitway.fmradio.utilities.isEmailValid
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        MobileAds.initialize(this)
        fetchData()
    }

    private fun fetchLogo(){

        FirebaseFirestore.getInstance().collection("advertisment").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val querySnapshot = task.result
                Log.e("DATA", "--====>" + querySnapshot!!.size())

                if (!querySnapshot.isEmpty) {

                    Config.banners.clear()

                    querySnapshot.documents.forEach { item ->
                        Config.banners.add(SocialObject(
                            name = "",
                            image = item.getString("image"),
                            url = item.getString("url"),
                            isRadio = false
                        ))
                    }

                    fetchData()
                }
            }
            else{

                fetchData()
            }

        }.addOnFailureListener { e -> Log.e("Error", e.message.toString())
            //IntentCall.hidepDialog()
            fetchData()
        }
    }

    private fun fetchData(){

        FirebaseFirestore.getInstance().collection("app_data").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val querySnapshot = task.result
                Log.e("DATA", "--====>" + querySnapshot!!.size())

                if (!querySnapshot.isEmpty) {

                    Config.socialList.clear()

                    querySnapshot.documents.forEachIndexed { index, it ->
                        Log.i("NAVD", "--====> size $index")

                        if(it.getBoolean("isRadio") == true){
                            Config.RADIO_STREAM_URL = it.getString("stream")
                        }

                        if (it.getString("stream")!!.isEmailValid()){
                            Config.EMAIL = it.getString("stream")
                        }

                        val item = SocialObject(
                        it.getString("name"),
                        it.getString("image"),
                        it.getString("stream"),
                        it.getBoolean("isRadio") ?: false
                        )

                        //item.prepareBitmap(this@SplashActivity)

                        Config.socialList.add(item)
                    }

                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
            }
            else{

                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }

        }.addOnFailureListener { e -> Log.e("Error", e.message.toString())
            //IntentCall.hidepDialog()
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
}