/*
홈화면 액티비티
 */

package com.example.weingweing

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

import java.text.SimpleDateFormat
import java.util.*

var isStart = false
var isFirstRun = true

class MainActivity : AppCompatActivity() {
    companion object {
        var pauseTime: Long = 0L
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val helper = SqliteHelper(this, "memo.db", 1)
        checkPermission()
//        val memo = Memo("19990101 0100","19990101 0101","0101","0100","0101","0101","11","없음","없음","없음","없음")
//        helper.insertMemo(memo)
        if(!isStart) {
            btn_stop_tracking.text = "위치 기록 시작하기"
        }
        else{
            btn_stop_tracking.text = "위치 기록 멈추기"
        }


        // '위치 기록 보러가기' 버튼
        btn_moveToLocation.setOnClickListener {
            val intent = Intent(this, LocationPage::class.java)
            val mess = Date(System.currentTimeMillis())
            intent.putExtra("currentTime", System.currentTimeMillis())
            startActivity(intent)
            finish()
        }

        // '알림 문자 보러가기' 버튼
        btn_moveToText.setOnClickListener {
            val intent = Intent(this, TextPage::class.java)
            startActivity(intent)
        }

        btn_stop_tracking.setOnClickListener {
            if(isFirstRun){
                start()
                isFirstRun = false //다시는 true가 되지 않음.
            }
            if(!isStart) {
                isStart = true
                Toast.makeText(this, "위치기록을 시작합니다", Toast.LENGTH_SHORT).show()
                btn_stop_tracking.text = "위치 기록 멈추기"
            }
            else {
                isStart = false
                Toast.makeText(this, "위치기록을 정지합니다", Toast.LENGTH_SHORT).show()
                pauseTime = System.currentTimeMillis()
                btn_stop_tracking.text = "위치 기록 시작하기"
                pauseTime = System.currentTimeMillis()
            }
        }


    }

    override fun onStart() {
        super.onStart()
        //gps off 푸시 알림 통해 들어온 경우
        val intent : Intent = getIntent()
        if (intent.hasExtra("day_start")){
            val day_start = intent.getStringExtra("day_start").toString()
            val day_end = intent.getStringExtra("day_end").toString()
            val time_start = intent.getStringExtra("start").toString()
            val time_end = intent.getStringExtra("end").toString()
            gpsOffPopUp(day_start, day_end, time_start, time_end)
        }
    }

    //권한처리
    val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    //    val PERM_LOCATION = 99
    fun checkPermission() {
        var permitted_all = true
        for (permission in permissions) {
            val result = ContextCompat.checkSelfPermission(this, permission)
            if (result != PackageManager.PERMISSION_GRANTED) {
                permitted_all = false
                requestPermission()
                break
            }
        }
        if (permitted_all) {
        }
    }
    fun requestPermission() {
        ActivityCompat.requestPermissions(this, permissions, 99)
    }
    fun confirmAgain() {
        AlertDialog.Builder(this)
            .setTitle("권한 승인 확인")
            .setMessage("위치 관련 권한을 모두 승인하셔야 앱을 사용할 수 있습니다. 권한 승인을 다시 하시겠습니까?")
            .setPositiveButton("네",{ _, _ ->
                requestPermission()
            }).setNegativeButton("아니요", { _, _ ->
                finish()
            }).create()
            .show()
    }
    override fun onRequestPermissionsResult(requestCode: Int
                                            , permissions: Array<out String>
                                            , grantResults: IntArray) {
        when (requestCode) {
            99 -> {
                var granted_all = true
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        granted_all = false
                        break
                    }
                }
                if (granted_all) {
                } else {
                    confirmAgain()
                }
            }
        }
    }
    fun start(){
        val intent = Intent(this, Foreground::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    fun stop(){
        val intent = Intent(this, Foreground::class.java)
        stopService(intent)
    }

    /*
GPS가 장시간 꺼져있을 경우 띄워줄 팝업 알림. 푸쉬 알림 통해서 들어왔을 때 띄워 줘야 함
pre: 날짜, 시작시간과 끝시간을 String으로 넘겨줌 (String db에 맞춰 수정가능)
post: 취소 시 위치기록으로(해당 날짜 해당 시간으로 보낼 수 있나?), 추가시 추가 화면으로
*/
    fun gpsOffPopUp(day_start: String, day_end: String, start: String, end: String) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate (R.layout.gps_off_pop_up, null)
        val tv : TextView = view.findViewById(R.id.tv_GpsOff)
        if(day_start == day_end) {
            tv.text = "위치 기능이 꺼져있어 위치 정보를 자동 기록하지 못 했습니다. ${day_start.substring(4,6).toInt()}월 ${day_start.substring(6).toInt()}일 ${start.substring(0,2)}:${start.substring(2)}~${end.substring(0,2)}:${end.substring(2)} 동안의 위치 정보를 추가해 주세요."
        }
        else { //시작날과 끝날 다를 때
            tv.text = "위치 기능이 꺼져있어 위치 정보를 자동 기록하지 못 했습니다. ${day_start.substring(4,6).toInt()}월 ${day_start.substring(6).toInt()}일 ${start.substring(0,2)}:${start.substring(2)}부터 ${day_end.substring(4,6).toInt()}월 ${day_end.substring(6).toInt()}일 ${end.substring(0,2)}:${end.substring(2)} 동안의 위치 정보를 추가해 주세요."
        }

        val alertDialog = AlertDialog.Builder(this)
            .create()

        val btn_cancel : Button = view.findViewById(R.id.btn_cancel)
        btn_cancel.setOnClickListener {
            alertDialog.dismiss()
        }

        val btn_addLocation : Button = view.findViewById(R.id.btn_addLocation)
        btn_addLocation.setOnClickListener {
            val intent = Intent(this, AddNewLocation::class.java)
            intent.putExtra("day_start", day_start)
            intent.putExtra("day_end", day_end)
            intent.putExtra("start", start)
            intent.putExtra("end", end)
            startActivity(intent)
            alertDialog.dismiss()
            finish()
        }

        alertDialog.setView(view)
        alertDialog.show()
    }
    override fun onBackPressed() {
        finishAffinity()
    }
}