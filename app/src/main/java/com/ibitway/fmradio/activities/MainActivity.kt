package com.ibitway.fmradio.activities

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.ibitway.fmradio.Config
import com.ibitway.fmradio.Config.socialList
import com.ibitway.fmradio.R
import com.ibitway.fmradio.fragments.FragmentRadio
import com.ibitway.fmradio.fragments.FragmentStations
import com.ibitway.fmradio.models.StationObject
import com.ibitway.fmradio.utilities.*
import com.mikepenz.materialdrawer.holder.ImageHolder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.iconRes
import com.mikepenz.materialdrawer.model.interfaces.iconUrl
import com.mikepenz.materialdrawer.model.interfaces.nameText
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.mikepenz.materialdrawer.util.addItems
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    //private lateinit var navigationView: MaterialDrawerSliderView

    private var drawerLayout: DrawerLayout? = null
    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null

    private lateinit var slider: MaterialDrawerSliderView

    lateinit var progressdialog: ProgressDialog

    private var isAlreadySetup = false
    private var isRadioScreenOpen = false

    companion object {
        const val AUDIO_PERMISSION_REQUEST_CODE = 102
        val WRITE_EXTERNAL_STORAGE_PERMS = arrayOf(
            Manifest.permission.RECORD_AUDIO
        )


        lateinit var data : StationObject

        private const val COLLAPSING_TOOLBAR_FRAGMENT_TAG = "collapsing_toolbar"
        private const val SELECTED_TAG = "selected_index"
        private const val COLLAPSING_TOOLBAR = 0
        private var selectedIndex = 0
        var FRAGMENT_DATA = "transaction_data"
        var FRAGMENT_CLASS = "transation_target"
    }

    val states = arrayOf(
        intArrayOf(android.R.attr.state_enabled),
        intArrayOf(-android.R.attr.state_enabled),
        intArrayOf(
            -android.R.attr.state_checked
        ),
        intArrayOf(android.R.attr.state_pressed)
    )

    val colors = intArrayOf(
        Color.GRAY,
        Color.GRAY,
        Color.GRAY,
        Color.GRAY
    )

    val myList = ColorStateList(states, colors)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //navigationView = findViewById<View>(R.id.slider) as MaterialDrawerSliderView

        drawerLayout = findViewById<View>(R.id.drawer_layout) as DrawerLayout

        slider = findViewById(R.id.slider)

        progressdialog = ProgressDialog(this)
        progressdialog.setTitle("Loading...")
        progressdialog.hide()

        FirebaseApp.initializeApp(this)

        //initialize and create the image loader logic
        DrawerImageLoader.init(object : AbstractDrawerImageLoader() {
            override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable, tag: String?) {
                Picasso.with(this@MainActivity).load(uri).placeholder(placeholder).into(imageView)
            }

            override fun cancel(imageView: ImageView) {
                Picasso.with(this@MainActivity).cancelRequest(imageView)
            }
        })

        selectedIndex = COLLAPSING_TOOLBAR
        supportFragmentManager.beginTransaction().add(
            R.id.containerView,
            FragmentRadio(),
            COLLAPSING_TOOLBAR_FRAGMENT_TAG
        ).commit()

        // specify a click listener
        slider.onDrawerItemClickListener = { v, _, position ->
            // do something with the clicked item :D

            if (position == 1){
                slider.drawerLayout?.close()

                if (isRadioScreenOpen) {
                    showRadioFragment()
                }
            }
            else {

                val url = socialList[position-2].url

                if (!url.isNullOrEmpty()) {

                    when {
                        url.isEmailValid() -> launchEmailIntent(url)
                        url.isValidCellPhone() -> launchPhoneIntent(url)
                        else -> launchWebIntent(url)
                    }
                }
            }

            false
        }

        slider.apply {
            addItems(
                PrimaryDrawerItem().apply {
                    textColor = myList; nameText =
                    "Now Playing"; iconRes = R.drawable.ic_baseline_play_circle_filled_24  ; isSelectable = false;
                    identifier = 2001
                }
            )
        }

        refreshData()

        buildHeader(false,null)

        //setupNavigationDrawer()

        //slider.itemIconTintList = null

        //slider.setNavigationItemSelectedListener(this)
    }

    override fun onResume() {
        super.onResume()
        progressdialog.hide()
    }

    override fun onPause() {
        super.onPause()
        progressdialog.hide()
    }

    private fun refreshData() {

        populateNavDrawer()
    }

    private fun populateNavDrawer(){

        if (!isAlreadySetup) {

            buildHeader(false,null)

            if (socialList.isNotEmpty()) {

                Log.i("NAVD", "--====> size " + socialList.size)

                socialList.forEachIndexed { index, socialObject ->

                    Log.i("NAVD", "items adding --====> launch")
                    slider.apply {
                        addItems(
                            PrimaryDrawerItem().apply {
                                textColor = myList; nameText =
                                socialObject.name.toString(); iconUrl =
                                socialObject.image.toString(); isSelectable = false;
                                identifier =
                                index.toLong()
                            }
                        )
                    }
                }
            }

            isAlreadySetup = true
        }
    }

    private fun buildHeader(compact: Boolean, savedInstanceState: Bundle?) {
        // Create the AccountHeader
        val headerView = AccountHeaderView(this, compact = compact).apply {
            attachToSliderView(slider)
            headerBackground = ImageHolder(R.drawable.logo)
            onAccountHeaderListener = { _, _, _ ->
                //false if you have not consumed the event and it should close the drawer
                false
            }
            withSavedInstance(savedInstanceState)
        }
    }

    fun sliderToggle() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            drawer.openDrawer(GravityCompat.START)
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.drawr_home -> {
                drawerLayout!!.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.drawer_www -> {
                Config.SOCIAL_URL = "https://funlandradio.com/"
                startActivity(Intent(applicationContext, WebviewActivity::class.java))
                return true
            }
            R.id.drawer_fb -> {
                Config.SOCIAL_URL = "https://www.facebook.com/FunLand-Radio-102399438069721/"
                startActivity(Intent(applicationContext, WebviewActivity::class.java))
                return true
            }
            R.id.drawer_ins -> {
                Config.SOCIAL_URL = "https://instagram.com/funland_radio?igshid=1m21dv0o7zbe2"
                startActivity(Intent(applicationContext, WebviewActivity::class.java))
                return true
            }
        }
        return false
    }

    fun setupNavigationDrawer(toolbar: Toolbar?) {
        actionBarDrawerToggle = object : ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.drawer_open, R.string.drawer_close
        ) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
            }
        }

        /*getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(false);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(false);
        actionBarDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_custom_drawer_icon);*/supportActionBar!!.hide()
    }

    interface OnBackClickListener {
        fun onBackClick(): Boolean
    }

    private var onBackClickListener: OnBackClickListener? = null
    fun setOnBackClickListener(onBackClickListener: OnBackClickListener?) {
        this.onBackClickListener = onBackClickListener
    }

    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            if (onBackClickListener != null && onBackClickListener!!.onBackClick()) {
                return
            }
            super.onBackPressed()
        }
    }

    fun toggleMenu() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            drawer.openDrawer(GravityCompat.START)
        }
    }

    fun showRadioFragment(){
        supportFragmentManager.beginTransaction().add(
            R.id.containerView,
            FragmentRadio(),
            COLLAPSING_TOOLBAR_FRAGMENT_TAG
        ).commit()

        isRadioScreenOpen = true
    }

    fun showStationFragment(){
        supportFragmentManager.beginTransaction().add(
            R.id.containerView,
            FragmentStations(),
            COLLAPSING_TOOLBAR_FRAGMENT_TAG
        ).commit()

        isRadioScreenOpen = false
    }
}