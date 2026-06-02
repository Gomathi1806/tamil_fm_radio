package com.ibitway.fmradio.utilities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ibitway.fmradio.Config
import com.ibitway.fmradio.activities.RequestActivity


fun String.isEmailValid(): Boolean {
    return !TextUtils.isEmpty(this) && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isContainsContact(): Boolean {
    return this == "Contact US" || this == "contact us" || this == "Contact Us" || this == "Contact" || this == "contact"
}

fun String.isValidCellPhone(): Boolean {
    return Patterns.PHONE.matcher(this).matches()
}

fun AppCompatActivity.launchWebIntent(url:String){
    Config.SOCIAL_URL = url
    val i = Intent(Intent.ACTION_VIEW)
    i.data = Uri.parse(url)
    startActivity(i)
}

fun AppCompatActivity.launchPhoneIntent(phone: String){
    val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
    startActivity(intent)
}

fun AppCompatActivity.launchEmailIntent(email: String){
    Config.REQUEST = email
    val emailIntent = Intent(
        Intent.ACTION_SENDTO, Uri.fromParts(
            "mailto", email, null
        )
    )
    if (emailIntent.resolveActivity(packageManager) != null) {
        startActivity(emailIntent)
    } else {
        Toast.makeText(
            this,
            "Email app not installed in your phone",
            Toast.LENGTH_SHORT
        ).show()
    }

    //startActivity(Intent(this,RequestActivity::class.java))
}

fun Context.launchWebIntent(url:String){
    try {
        Config.SOCIAL_URL = url
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }
    catch (e:Exception){}

}

fun Context.launchPhoneIntent(phone: String){
    val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
    startActivity(intent)
}

fun Context.launchEmailIntent(email: String){
    Config.REQUEST = email
    val emailIntent = Intent(
        Intent.ACTION_SENDTO, Uri.fromParts(
            "mailto", email, null
        )
    )
    if (emailIntent.resolveActivity(packageManager) != null) {
        startActivity(emailIntent)
    } else {
        Toast.makeText(
            this,
            "Email app not installed in your phone",
            Toast.LENGTH_SHORT
        ).show()
    }

    //startActivity(Intent(this,RequestActivity::class.java))
}