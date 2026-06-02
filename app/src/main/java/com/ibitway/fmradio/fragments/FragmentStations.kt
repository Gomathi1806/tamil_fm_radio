package com.ibitway.fmradio.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.ibitway.fmradio.BuildConfig
import com.ibitway.fmradio.Config
import com.ibitway.fmradio.R
import com.ibitway.fmradio.activities.MainActivity
import com.ibitway.fmradio.adapters.RadioListingAdapter
import com.ibitway.fmradio.models.StationObject
import kotlinx.android.synthetic.main.fragment_stations.*

class FragmentStations : Fragment() {

    var mainActivity: MainActivity? = null

    private var relativeLayout: RelativeLayout? = null

    var stationsList: ArrayList<StationObject> = ArrayList()

    var radioListingAdapter: RadioListingAdapter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        relativeLayout = inflater.inflate(R.layout.fragment_stations, container, false) as RelativeLayout

        return relativeLayout
    }

    override fun onResume() {
        super.onResume()

        initUI()
    }

    private fun initUI() {

        radioListingAdapter = activity?.let { RadioListingAdapter(it,Config.socialList) }

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.isSmoothScrollbarEnabled = true
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))
        recyclerView.adapter = radioListingAdapter

        menu_toggle.setOnClickListener {
            ((activity) as MainActivity).toggleMenu()
        }

        Log.i("LIFE","init")

        refreshData()
    }

    private fun refreshData() {

        Log.i("LIFE","refresh")

        progressBar.visibility = View.VISIBLE
        FirebaseFirestore.getInstance().collection("stations").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {

                progressBar.visibility = View.GONE

                val querySnapshot = task.result
                Log.e("DATA", "--====>" + querySnapshot!!.size())

                stationsList.clear()

                if (!querySnapshot.isEmpty) {
                    querySnapshot.documents.forEach {
                        stationsList.add(StationObject(it.getString("name"),it.getString("image"),it.getString("stream_url")))

                        stationsList.reverse()
                        //radioListingAdapter!!.update(stationsList)
                    }
                }
            }

        }.addOnFailureListener { e -> Log.e("Error", e.message.toString())
            progressBar.visibility = View.GONE
        }
    }
}