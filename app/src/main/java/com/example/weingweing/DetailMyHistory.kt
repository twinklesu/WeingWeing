package com.example.weingweing


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_text_page.*
import kotlinx.android.synthetic.main.activity_detail_myhistory.*
import java.util.*
import kotlin.properties.Delegates

class DetailMyHistory : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private var fromNoti = false


    var infecteed_overlap_location = ArrayList<String>()
    var myoverlap_location = ArrayList<String>()

    val helper = SqliteHelper(this, "memo.db", 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_myhistory)
        database = Firebase.database.reference

        val intent = getIntent()
        fromNoti = intent.getBooleanExtra("noti", false)
        Log.d("noti intent", fromNoti.toString())
        detail_location.text = intent.getStringExtra("si")+" "+intent.getStringExtra("gu")+" "+intent.getStringExtra("dong")
        overlap_Month.text = "${intent.getStringExtra("coronaST")!!.substring(4,6).toInt()}월 ${intent.getStringExtra("coronaST")!!.substring(6).toInt()}일"
        overlap_time.text = "${intent.getStringExtra("coronaSH")!!.substring(0,2)}:${intent.getStringExtra("coronaSH")!!.substring(2)}~${intent.getStringExtra("coronaEH")!!.substring(0,2)}:${intent.getStringExtra("coronaEH")!!.substring(2)}"
        infected_num.text = intent.getStringExtra("num")
        infected_date.text = intent.getStringExtra("infectedDate").toString().replace("(","")
        infected_location.text = intent.getStringExtra("address")
        infected_etc.text = intent.getStringExtra("ETC")
        my_time.text = "${intent.getStringExtra("userSH")!!.substring(0,2)}:${intent.getStringExtra("userSH")!!.substring(2)}"
        my_dong.text = "${intent.getStringExtra("userEH")!!.substring(0,2)}:${intent.getStringExtra("userEH")!!.substring(2)}"
        val gps = intent.getStringExtra("userGPS")
        val gpsArray = gps!!.split("@")
        val LatLng_array = arrayListOf<String>()
        for (gps in gpsArray){
            LatLng_array.add(gps)
        }

        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.frame2, HistoryMap().apply{
            arguments = Bundle().apply {
                putStringArrayList("LatLng", LatLng_array)
            }
        }).commit()

        go_to_site.setOnClickListener {
            Log.w("클릭은 반응","클릭반응")
            go_site()
        }


    }

    private fun go_site() {
        val intent = getIntent()
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val url: String =
                    dataSnapshot.child(intent.getStringExtra("num").toString().substring(0,3)).getValue()
                        .toString()
                // TODO: handle the post
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.sdm.go.kr/news/corona19/coronaInfo.do?mode=view&sdmBoardSeq=236109"))
                startActivity(intent)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    override fun onBackPressed() {
        intent = Intent(this, TextPage::class.java)
        startActivity(intent)
        finishAffinity()
    }



    private fun startToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }


}