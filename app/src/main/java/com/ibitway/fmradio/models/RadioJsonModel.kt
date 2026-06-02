package com.ibitway.fmradio.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.google.gson.annotations.SerializedName
import com.ibitway.fmradio.R
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

data class RadioJsonModel(

	@field:SerializedName("station")
	val stations: List<StationObject?>? = null,

	@field:SerializedName("main")
	val main: Main? = null
)

data class Main(

	@field:SerializedName("app_name")
	val appName: String? = null,

	@field:SerializedName("bg_color")
	val bgColor: String? = null,

	@field:SerializedName("nav_color")
	val navColor: String? = null
)

data class StationObject(

	val name: String? = "",
	val imageURL: String? = "null",
	val streamURL: String? = "",
	val isRadio : Boolean = false
)

data class SocialObject(val name:String?,val image:String?, val url:String?, val isRadio: Boolean = false){
	var imageBitmap: Bitmap? = null

	fun prepareBitmap(context: Context){

		Picasso.with(context)
			.load(image)
			.placeholder(R.drawable.logo)
			.into(object : Target {
				override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
					imageBitmap = bitmap
				}

				override fun onBitmapFailed(errorDrawable: Drawable) {

				}

				override fun onPrepareLoad(placeHolderDrawable: Drawable) {}
			})

	}
}