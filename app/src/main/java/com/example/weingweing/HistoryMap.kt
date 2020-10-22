package com.example.weingweing

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class HistoryMap : Fragment() {

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        val arr = requireArguments().getStringArrayList("LatLng")
        val options = MarkerOptions()
        val polylineOptions = PolylineOptions()


        for ((index, point) in arr!!.withIndex()) {
            val comma = point.indexOf(',')
            Log.d("point", point)
            val pin = LatLng(point.substring(1,comma).toDouble(), point.substring(comma+1,point.length-1).toDouble())
            options.position(pin)
            options.title((index+1).toString())
            polylineOptions.add(pin)
            googleMap.addMarker(options)
        }
        googleMap.addPolyline(polylineOptions)
        //첫번째 핀에 카메라 초점
        val first_point = arr[0]
        val comma = first_point.indexOf(',')
        Log.d("point", first_point)
        val pin = LatLng(first_point.substring(1,comma).toDouble(), first_point.substring(comma+1,first_point.length-1).toDouble())
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pin, 14f))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}