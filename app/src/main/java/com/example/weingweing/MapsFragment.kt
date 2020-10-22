package com.example.weingweing

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.CameraUpdateFactory.newLatLng
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_add_new_location.*
import java.io.IOException
import java.util.*


class MapsFragment : Fragment() {

    private val callback = OnMapReadyCallback { googleMap ->
//        var si=""
//        var gu=""
//        var dong=""
        var dong_1=""
        val geocoder = Geocoder(this.context, Locale.KOREA)
        val str = requireArguments().getString("addr")
        Log.d("bundle", str.toString())

        if(str == "") {
            val seoul = LatLng(37.574, 126.97458) //광화문 광장
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 15f))
        }
        else {
            var addressList: List<Address>? = null
            try {
                addressList = geocoder!!.getFromLocationName(str, 10)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (addressList.isNullOrEmpty()) {
                Toast.makeText(this.context, "장소를 정확히 입력해주세요", Toast.LENGTH_SHORT).show()
                val seoul = LatLng(37.574, 126.97458) //광화문 광장
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 15f))
            }
            else {
                Log.d("주소주소", "첫주소 : " + addressList[0].toString())
                val splitStr = addressList!![0].toString().split(",".toRegex()).toTypedArray()
                var latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1) // 위도
                var longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1) // 경도
                var sigudong = address(latitude.toDouble(), longitude.toDouble())
                AddNewLocation.location_tuple = "(${latitude}, ${longitude})" //DB에 저장
                AddNewLocation.dong = sigudong.third //DB에 저장
                AddNewLocation.si = sigudong.first
                AddNewLocation.gu = sigudong.second


                val point = LatLng(latitude.toDouble(), longitude.toDouble()) // 좌표(위도, 경도) 생성
                val mOptions2 = MarkerOptions() // 마커 생성
                mOptions2.title("search result")
                mOptions2.position(point) // 마커 추가
                mOptions2.draggable(true)
                googleMap!!.addMarker(mOptions2) // 해당 좌표로 화면 줌
                googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 16f))
                googleMap.setOnMarkerDragListener(object : OnMarkerDragListener {
                    override fun onMarkerDragStart(arg0: Marker) {
                    }
                    override fun onMarkerDragEnd(arg0: Marker) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLng(arg0.position))
                        val position = arg0.position.toString()
                        Log.d("markerLoc", position)
                        latitude =
                            position.substring(position.indexOf("(")+1, position.indexOf(","))
                        longitude =
                            position.substring(position.indexOf(",") + 1, position.indexOf(")"))
                        var sigudong = address(latitude.toDouble(), longitude.toDouble())
                        AddNewLocation.dong = sigudong.third //DB에 저장
                        AddNewLocation.si = sigudong.first
                        AddNewLocation.gu = sigudong.second
                        AddNewLocation.location_tuple = "(${latitude}, ${longitude})" //DB에 저장
                    }
                    override fun onMarkerDrag(arg0: Marker) {
                    }
                })

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
    private fun address(lati:Double, longi:Double):Triple<String,String,String>{
        val geocoder = Geocoder(this.context, Locale.KOREA)
        var si=""
        var gu=""
        var dong=""
        var dong_1=""
        var addressList: List<Address>? = null
        try {
            addressList = geocoder!!.getFromLocation(lati, longi, 1)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Log.d("주소주소", "첫주소 : "+ addressList!![0].toString())
        val splitStr = addressList!![0].toString().split(",".toRegex()).toTypedArray()
        val latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1) // 위도
        val longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1) // 경도
        var addr = splitStr[0].substring(splitStr[0].indexOf(":") + 2, splitStr[0].lastIndexOf("]") - 1)
        if (addr[addr.length-1].toString()=="동"){
            dong_1 = addr.substring(addr.lastIndexOf(" "), addr.lastIndexOf("동")+1).trim()
            addr = addr.substring(0, addr.lastIndexOf(" ")+1)
            //Log.d("addr", "현재 주소 값1: $addr, $si, $gu, $dong_1")
        }else if ((addr[addr.length-1].toString()!="동") && addr.contains("동 ")){
            dong_1 = addr.substring(addr.lastIndexOf(" ",addr.lastIndexOf(" ",addr.lastIndexOf("동 ")-1)), addr.lastIndexOf("동 ")+1).trim()
            addr = addr.substring(0, addr.lastIndexOf(" ",addr.lastIndexOf(" ",addr.lastIndexOf("동 ")-1))+1)
            //Log.d("addr", "현재 주소 값2: $addr, $si, $gu, $dong_1")
        }
        else{if((addr[addr.length-1].toString()!="동") && (addr.contains("동 "))!=true){
            dong_1 = "없음"
            addr = addr.substring(0, addr.lastIndexOf(" ")+1)
            //Log.d("addr", "현재 주소 값3: $addr, $si, $gu, $dong_1")
        }}

        //좌표를 다시 주소로 변환
        var mResultList: List<Address>? = null
        try {
            mResultList =
                geocoder!!.getFromLocationName(dong_1, 10)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (mResultList != null) {
            val address = mResultList[0]
            val addressStringBuilder = StringBuilder()
            for (i in 0..address.maxAddressLineIndex) {
                addressStringBuilder.append(address.getAddressLine(i))
                if (i < address.maxAddressLineIndex) addressStringBuilder.append("\n")
            }
            var addr = addressStringBuilder.toString()
            Log.d("주소주소", "첫주소 : "+ mResultList[0].toString())
            if (addr[addr.length - 1].toString() == "동") {
                dong =
                    addr.substring(addr.lastIndexOf(" "), addr.lastIndexOf("동") + 1).trim()
                addr = addr.substring(0, addr.lastIndexOf(" ") + 1)
                //Log.d("addr", "현재 주소 값1: $addr, $si, $gu, $dong")
            } else if ((addr[addr.length - 1].toString() != "동") && addr.contains("동 ")) {
                dong = addr.substring(
                    addr.lastIndexOf(
                        " ",
                        addr.lastIndexOf(" ", addr.lastIndexOf("동 ") - 1)
                    ), addr.lastIndexOf("동 ") + 1
                ).trim()
                addr = addr.substring(
                    0,
                    addr.lastIndexOf(
                        " ",
                        addr.lastIndexOf(" ", addr.lastIndexOf("동 ") - 1)
                    ) + 1
                )
                //Log.d("addr", "현재 주소 값2: $addr, $si, $gu, $dong")
            } else {
                if ((addr[addr.length - 1].toString() != "동") && (addr.contains("동 ")) != true) {
                    dong = "없음"
                    addr = addr.substring(0, addr.lastIndexOf(" ") + 1)
                    //Log.d("addr", "현재 주소 값3: $addr, $si, $gu, $dong")
                }
            }
            if (addr[addr.length - 1].toString() == "구") {
                gu = addr.substring(
                    addr.lastIndexOf(" ", addr.lastIndexOf("구") + 1),
                    addr.lastIndexOf("구") + 1
                ).trim()
                si = addr.substring(addr.indexOf(" "), addr.indexOf("시 ") + 1).trim()
                //Log.d("addr", "현재 주소 값4: $addr, $si, $gu, $dong")
            } else if (addr.contains("구 ") && addr.contains("시 ")) {
                gu = addr.substring(addr.indexOf("시 ") + 2, addr.indexOf("구 ") + 1).trim()
                si = addr.substring(addr.indexOf(" ") + 1, addr.indexOf("시 ") + 1).trim()
                //Log.d("addr", "현재 주소 값5: $addr, $si, $gu, $dong")
            } else if ((!addr.contains("구 ") && addr.contains("시 "))) {
                gu = "없음"
                si = addr.substring(addr.indexOf(" "), addr.indexOf("시 ") + 1).trim()
                // Log.d("addr", "현재 주소 값6: $addr, $si, $gu, $dong")
            }
            Log.d("addr최종", "현재 주소 값: $addr, $si, $gu, $dong")
            val point = LatLng(latitude.toDouble(), longitude.toDouble()) // 좌표(위도, 경도) 생성
        }
        return Triple(si, gu, dong)
    }
}