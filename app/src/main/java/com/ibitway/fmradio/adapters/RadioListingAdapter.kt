package com.ibitway.fmradio.adapters

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.ibitway.fmradio.Config
import com.ibitway.fmradio.R
import com.ibitway.fmradio.activities.RadioActivity
import com.ibitway.fmradio.models.SocialObject
import com.ibitway.fmradio.utilities.*
import com.squareup.picasso.Picasso

class RadioListingAdapter(
    private val context: Activity,
    items: List<SocialObject>?
) : RecyclerView.Adapter<RadioListingAdapter.ViewHolder>() {
    private var mDocumentSnapshots: List<SocialObject>?
    private val mInflater: LayoutInflater
    private var mClickListener: ItemClickListener? = null
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): ViewHolder {
        val view = mInflater.inflate(R.layout.item_social_view, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val (name, image) = getItem(i)

        if (name!!.isEmpty()) {
            viewHolder.stationTitle.text = "Station " + i + 1
        } else {
            viewHolder.stationTitle.text = name
        }
        Picasso.with(context).load(image).fit()
            .placeholder(R.drawable.logo)
            .into(viewHolder.stationImage)
    }

    override fun getItemCount(): Int {
        return if (mDocumentSnapshots == null) 0 else mDocumentSnapshots!!.size
    }

    // allows clicks events to be caught
    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    // convenience method for getting data at click position
    fun getItem(id: Int): SocialObject {
        return mDocumentSnapshots!![id]
    }

    fun update(documentSnapshots: List<SocialObject>) {
        mDocumentSnapshots = documentSnapshots
        notifyDataSetChanged()
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var stationTitle: TextView
        var stationImage: ImageView
        override fun onClick(view: View) {
            val item = getItem(adapterPosition)
            //MainActivity.Companion.setType("1");

            if (item.isRadio) {
                Config.RADIO_STREAM_URL = item.url
                Config.CURRENT_STATION = item.name
                if (item.imageBitmap != null) {
                    Tools.BACKGROUND_IMAGE_ID = item.imageBitmap
                }
                context.startActivity(Intent(context, RadioActivity::class.java))
            }
            else {

                val url : String = item.url ?: "https://www.google.com"
                val name : String = item.name ?: "somename"

                when {

                    isWp(name) -> { openWhatsApp(url) }
                    isViber(name) -> { openViberApp(url) }

                    url.isEmailValid() -> context.launchEmailIntent(url)
                    url.isValidCellPhone() -> context.launchPhoneIntent(url)

                    else -> context.launchWebIntent(url)
                }
            }
        }

        init {
            stationTitle = itemView.findViewById(R.id.title)
            stationImage = itemView.findViewById(R.id.imageView)
            itemView.setOnClickListener(this)
        }

        private fun isAbout(name:String):Boolean{
            return when(name){
                "About us","About US","About Us","about","about us","about Us","about US" -> true
                else -> false
            }
        }

        private fun isWp(name: String):Boolean{
            return when(name){
                "whatsapp","Whatsapp","whatsApp","WhatsApp", "WHATSAPP" -> true
                else -> false
            }
        }

        private fun isViber(name: String):Boolean{
            return when(name){
                "Viber","viber","viber call","Viber", "VIBER" -> true
                else -> false
            }
        }

        private fun openWhatsApp(phone:String){
            val url = "https://api.whatsapp.com/send?phone=${phone}"
                try {
                    val packageManager = context.packageManager
                    packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
                    val i = Intent(Intent.ACTION_VIEW);
                    i.data = Uri.parse(url);
                    context.startActivity(i)
                } catch (e:PackageManager.NameNotFoundException) {
                    Toast.makeText(context, "Whatsapp is not installed in your phone.", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
        }

        private fun openViberApp(phone:String){

            val viberPackageName = "com.viber.voip"

            try {
                context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("viber://add?number=$phone")))
            } catch (ex: ActivityNotFoundException) {
                try {
                    context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$viberPackageName")))
                } catch (ex: ActivityNotFoundException) {
                    context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$viberPackageName")))
                }
            }
        }
    }

    // data is passed into the constructor
    init {
        mInflater = LayoutInflater.from(context)
        mDocumentSnapshots = items
    }
}