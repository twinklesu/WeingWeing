package com.example.weingweing


import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.weingweing.MainActivity.Companion.pauseTime
import com.google.android.gms.location.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.thread
import kotlin.time.ExperimentalTime


class Foreground : Service() {
    val cri= mutableListOf<String>("1","2","3","4","5","6","7","8","9","0",",","본")
    val checkDong = Dong()
    var bh = checkDong.bh
    var hb = checkDong.hb


    val helper = SqliteHelper(this, "memo.db", 1)
    var helper2 = SqliteHelper2(this, "memo2.db", 1)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    var locationManager: LocationManager? = null
    val interval: Long = 300010
    var countNoGPS = -1
    var noGPSST = ""
    var noGPSET = ""
    var lastLati: Double? = 0.0
    var lastLongti: Double? = 0.0
    var latitude: Double? = null
    var longitude: Double? = null
    var delGPS = ""
    var lastLoc = ""
    var setTime = System.currentTimeMillis()
    private lateinit var firedatabase: DatabaseReference

    companion object {
        val CHANNEL_ID = "FGS"
    }

    fun createNotifiacationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel =
                NotificationChannel(CHANNEL_ID, "Foreground", NotificationManager.IMPORTANCE_NONE)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    @ExperimentalTime
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotifiacationChannel()

        val i = Intent(this, NotificationBroadcastReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 1, i, PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("알림을 눌러 이 안내 메시지를 끌 수 있습니다.")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(99, notification)

        runBackground()

        return super.onStartCommand(intent, flags, startId)
    }

    /*
    백그라운드에서 실행 되야 하는 함수를 이 위치에 적음
     */
    @ExperimentalTime
    fun runBackground() {
        thread(start = true, name = "Weing") {
            while (true) {
                if (isStart) {
                    getCurrentLoc()
                } else if ((!isStart) && (System.currentTimeMillis() - pauseTime >= 60000)) { //
                    recordOffPush()
                }
//                getFirebase()
                Thread.sleep(interval) //1초마다 로그 찍음
            }
        }
    }


    private fun getCurrentLoc() {
//        Log.d("compare", "---------------------------------------------")
        var dong = "빈칸"
        var gu = "빈칸"
        var si = "빈칸"
        var dong_1=""

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 10000
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) { locationResult ?: return }}
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location == null) {
                countNoGPS+=1
                if (countNoGPS == 0){
                    noGPSST=SimpleDateFormat("yyyyMMdd HHmm").format(Date(System.currentTimeMillis()))
                    Toast.makeText(this, "GPS를 켜주세요.", Toast.LENGTH_SHORT).show()
                    Log.d("Nogps", "현재 주소 값: $countNoGPS")
                    countNoGPS+=1
                }else if(countNoGPS == 12){
                    noGPSET=SimpleDateFormat("yyyymmddhhmmss").format(Date(System.currentTimeMillis()))
                    Log.d("Nogps", "현재 주소 값: $countNoGPS")
                    countNoGPS=1
                    Log.d("Nogps", "$noGPSST: $noGPSET")
                    gpsOffPush(noGPSST.substring(0,8), noGPSET.substring(0,8), noGPSST.substring(9,13), noGPSET.substring(9,13))
                    //Toast.makeText(this, "PUSH 알림 발사.", Toast.LENGTH_SHORT).show()
                }else {
                    Log.d("Nogps", "현재 주소 값: $countNoGPS")
                    //oast.makeText(this, "GPS를 켜주세요 $countNoGPS.", Toast.LENGTH_SHORT).show()
                }
            } else {
                countNoGPS=0
                latitude = location.latitude
                longitude = location.longitude

                var startPoint = Location("locationA")
                startPoint.setLatitude(lastLati!!)
                startPoint.setLongitude(lastLongti!!)
                val endPoint = Location("locationA")
                endPoint.setLatitude(latitude!!)
                endPoint.setLongitude(longitude!!)

                val distance: Float = startPoint.distanceTo(endPoint)
                lastLati=latitude
                lastLongti=longitude
                if (distance <=1250){
                    Log.d("Speed", "속도 정상, 거리 : $distance")
                    Log.d("CheckCurrentLocation", "현재 내 위치 값: $latitude, $longitude")
                    var geocoder = Geocoder(applicationContext, Locale.KOREAN)
                    var addressList: List<Address>? = null
                    try {
                        do {
                            addressList = geocoder.getFromLocation(latitude!!, longitude!!, 1)
                        } while (addressList!!.size==0)
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
                        if (addr[addr.length - 1].toString() == "동") {
                            dong = addr.substring(addr.lastIndexOf(" "), addr.lastIndexOf("동") + 1)
                                .trim()
                            addr = addr.substring(0, addr.lastIndexOf(" ") + 1)
                            Log.d("addr", "현재 주소 값1: $addr, $si, $gu, $dong")
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
                            Log.d("addr", "현재 주소 값2: $addr, $si, $gu, $dong")
                        } else {
                            if ((addr[addr.length - 1].toString() != "동") && (addr.contains("동 ")) != true) {
                                dong = "없음"
                                addr = addr.substring(0, addr.lastIndexOf(" ") + 1)
                                Log.d("addr", "현재 주소 값3: $addr, $si, $gu, $dong")
                            }
                        }
                        if (addr[addr.length - 1].toString() == "구") {
                            gu = addr.substring(
                                addr.lastIndexOf(" ", addr.lastIndexOf("구") + 1),
                                addr.lastIndexOf("구") + 1
                            ).trim()
                            si = addr.substring(addr.indexOf(" "), addr.indexOf("시 ") + 1).trim()
                            Log.d("addr", "현재 주소 값4: $addr, $si, $gu, $dong")
                        } else if (addr.contains("구 ") && addr.contains("시 ")) {
                            gu = addr.substring(addr.indexOf("시 ") + 2, addr.indexOf("구 ") + 1)
                                .trim()
                            si =
                                addr.substring(addr.indexOf(" ") + 1, addr.indexOf("시 ") + 1).trim()
                            Log.d("addr", "현재 주소 값5: $addr, $si, $gu, $dong")
                        } else if ((!addr.contains("구 ") && addr.contains("시 "))) {
                            gu = "없음"
                            si = addr.substring(addr.indexOf(" "), addr.indexOf("시 ") + 1).trim()
                            Log.d("addr", "현재 주소 값6: $addr, $si, $gu, $dong")
                        }
                        Log.d("addr최종", "현재 주소 값: $addr, $si, $gu, $dong")
                        var compareDong = dong
                        var compareLast = lastLoc
                        for (out in cri) {
                            if (out in compareLast) {
                                compareLast = compareLast.replace(out, "")
                            }
                        }
                        for (out in cri) {
                            if (out in compareDong) {
                                compareDong = compareDong.replace(out, "")
                            }
                        }

                        var answer = (dong == lastLoc || dong == compareLast || compareDong == lastLoc || compareDong == compareLast)
                        Log.d("Compare", "dong : $dong, lastLoc : $lastLoc, compareDong : $compareDong, compareLast : $compareLast, boolean : $answer")

                        if (dong == lastLoc || dong == compareLast || compareDong == lastLoc || compareDong == compareLast) {
                            var time = System.currentTimeMillis()
                            var dateFormat = SimpleDateFormat("yyyyMMdd HHmm")
                            val endTime =
                                dateFormat.format(Date(time)).subSequence(0, 8)
                                    .toString()
                            var endHour =
                                dateFormat.format(Date(time)).subSequence(9, 13)
                                    .toString()
                            time = setTime
                            var startTime =
                                dateFormat.format(Date(time-(interval))).subSequence(0, 8).toString()
                            var startHour =
                                dateFormat.format(Date(time-(interval))).subSequence(9, 13).toString()
                            delGPS = delGPS + "@" + "($latitude, $longitude)"
                            val delmemo = Memo(
                                startTime + " " + startHour,
                                endTime + " " + endHour,
                                startTime,
                                startHour,
                                endTime,
                                endHour,
                                "($latitude, $longitude)",
                                si + " " + gu + " " + dong,
                                si,
                                gu,
                                dong
                            )
                            helper.deleteMemo(delmemo)
                            val memo = Memo(
                                startTime + " " + startHour,
                                endTime + " " + endHour,
                                startTime,
                                startHour,
                                endTime,
                                endHour,
                                delGPS,
                                si + " " + gu + " " + dong,
                                si,
                                gu,
                                dong
                            )
                            helper.insertMemo(memo)
                            Log.d("변화X 최종", "위치가 같을때 if 문 $memo")
                            Log.d("delGPS", "$delGPS")
                        } else {
                            //위치가 바뀌었을때
                            Log.d("변화", "$lastLoc 에서 $dong 으로")
                            val time = System.currentTimeMillis()
                            var dateFormat = SimpleDateFormat("yyyyMMdd HHmm")
                            var startTime = dateFormat.format(Date(time- (interval))).subSequence(0, 8).toString()
                            var startHour = dateFormat.format(Date(time- (interval))).subSequence(9, 13).toString()
                            val endTime =
                                dateFormat.format(Date(time)).subSequence(0, 8).toString()
                            var endHour =
                                dateFormat.format(Date(time)).subSequence(9, 13).toString()
                            val memo = Memo(
                                startTime + " " + startHour,
                                endTime + " " + endHour,
                                startTime,
                                startHour,
                                endTime,
                                endHour,
                                "($latitude, $longitude)",
                                si + " " + gu + " " + dong,
                                si,
                                gu,
                                dong
                            )
                            helper.insertMemo(memo)
                            lastLoc = dong
                            setTime = time
                            delGPS = "($latitude, $longitude)"
                            Log.d("변화O 최종", "위치가 바뀌었을때 else문 $memo")
                            Log.d("delGPS", "$delGPS")
                        }
                    }
                }else{ if (distance <=1000_000){
                    Log.d("Speed", "속도 너무 빠름, 속도 : 거리 : $distance")
                    val time = System.currentTimeMillis()
                    var dateFormat = SimpleDateFormat("yyyyMMdd HHmm")
                    var startTime = dateFormat.format(Date(time-(interval))).subSequence(0,8).toString()
                    var startHour = dateFormat.format(Date(time-(interval))).subSequence(9,13).toString()
                    val endTime = dateFormat.format(Date(time)).subSequence(0,8).toString()
                    var endHour = dateFormat.format(Date(time)).subSequence(9,13).toString()
                    val memo = Memo(startTime+" "+startHour, endTime+" "+endHour, startTime, startHour, endTime, endHour, "($latitude, $longitude)", "시속30km 이상으로 삭제", "", "", "")
                    helper.insertMemo(memo)
                    lastLoc = ""
                    setTime = System.currentTimeMillis()
                    delGPS = ""
                }}
            }
        }
    }

    private fun recordOffPush() {
        val NOTIFICATION_ID = 10 //같으면 알림 새로 안뜬다는데..?

        createNotificationChannel(
            this,
            NotificationManagerCompat.IMPORTANCE_HIGH,
            false,
            getString(R.string.app_name),
            "App notification channel"
        )   // 1

        val channelId = "$packageName-${getString(R.string.app_name)}"
        val title = "장시간 위치 기록이 꺼져있었습니다"
        val content = "감염병 예방을 위해 위치기록 시작하기 버튼을 눌러주세요!!"
        val intent = Intent(baseContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val fullScreenPendingIntent = PendingIntent.getActivity(
            baseContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )    // 2

        val builder = NotificationCompat.Builder(this, channelId)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle(title)
        builder.priority = NotificationCompat.PRIORITY_HIGH
        builder.setAutoCancel(true)
        builder.setDefaults(Notification.DEFAULT_SOUND)
        builder.setFullScreenIntent(fullScreenPendingIntent, true)

        val style = NotificationCompat.BigTextStyle(builder)
        style.bigText(content)

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    override fun onBind(intent: Intent?): IBinder? {
        return Binder()
    }

    /*
    GPS가 장시간 꺼져있을 경우 띄워줄 푸시 알림.
    pre: 날짜, 시작시간과 끝시간을 String으로 넘겨준다. (String db에 맞춰 수정가능)
    post: 눌러서 어플에 접속했을 때, 홈 액티비티 위로 gpsOffPush가 실행 된다.
    */
    fun gpsOffPush(day_start: String, day_end: String, time_start: String, time_end: String) {
        val NOTIFICATION_ID = 100 //같으면 알림 새로 안뜬다는데..?

        createNotificationChannel(
            this,
            NotificationManagerCompat.IMPORTANCE_HIGH,
            false,
            getString(R.string.app_name),
            "App notification channel"
        )   // 1

        val channelId = "$packageName-${getString(R.string.app_name)}"
        val title = "장시간 위치 기능이 꺼져있었습니다"
        var content = ""
        if (day_start == day_end) {
            content = "${day_start.substring(4, 6).toInt()}월 ${day_start.substring(6)
                .toInt()}일 ${time_start.substring(
                0,
                2
            )}:${time_start.substring(2)}~${time_end.substring(
                0,
                2
            )}:${time_end.substring(2)} 동안의 위치 직접 입력하기"
        } else { //시작날과 끝날 다를 때
            content = "${day_start.substring(4, 6).toInt()}월 ${day_start.substring(6)
                .toInt()}일 ${time_start.substring(
                0,
                2
            )}:${time_start.substring(2)}부터 ${day_end.substring(4, 6).toInt()}월 ${day_end.substring(
                6
            ).toInt()}일 ${time_end.substring(0, 2)}:${time_end.substring(2)} 동안의 위치 직접 입력하기"
        }

        val intent = Intent(baseContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("day_start", day_start) //yyyymmdd
        intent.putExtra("day_end", day_end)
        intent.putExtra("start", time_start) //hhmm
        intent.putExtra("end", time_end) //hhmm
        val fullScreenPendingIntent = PendingIntent.getActivity(
            baseContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )    // 2

        val builder = NotificationCompat.Builder(this, channelId)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle(title)
        builder.priority = NotificationCompat.PRIORITY_HIGH
        builder.setAutoCancel(true)
        builder.setDefaults(Notification.DEFAULT_SOUND)
        builder.setFullScreenIntent(fullScreenPendingIntent, true)

        val style = NotificationCompat.BigTextStyle(builder)
        style.bigText(content)

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    // Push 알림 위한 설정
    fun createNotificationChannel(
        context: Context,
        importance: Int,
        showBadge: Boolean,
        name: String,
        description: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "${context.packageName}-$name"
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            channel.setShowBadge(showBadge)

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getFirebase() {
        Log.d("Firebase", "getFirebase()실행")
        var myDong = helper.getMemo("dong").distinct()
        var myGu = helper.getMemo("gu").distinct()
        var mySi = helper.getMemo("si").distinct()
        firedatabase = Firebase.database.reference
        firedatabase.addValueEventListener(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                val formatted = current.format(formatter).toInt()
                for (i in formatted downTo (formatted - 14)) {
                    for (si in mySi) {
                        if (dataSnapshot.child(i.toString()).hasChild(si)) {
                            for (gu in myGu) {
                                if (dataSnapshot.child(i.toString()).child(si).hasChild(gu)) {
                                    for (dong_1 in myDong) {
                                        var new_dong: ArrayList<String> = arrayListOf(dong_1)
                                        if (dong_1 in bh.keys) {
                                            for (t in 0 until bh[dong_1]!!.size) {
                                                new_dong.add((bh[dong_1])!![t])
                                            }
                                        } else if (dong_1 in hb.keys) {
                                            for (t in 0 until hb[dong_1]!!.size) {
                                                new_dong.add((hb[dong_1])!![t])
                                            }
                                        }
                                        var dbDong: String = ""
                                        for (dong in new_dong) {
                                            if (dataSnapshot.child(i.toString()).child(si).child(gu).hasChild(dong)) {
                                                dbDong = dong
                                            }
                                        }
                                        for (dong in new_dong) {
                                            var ref = helper.compareMemo("si", "gu", "dong", si, gu, dong)
                                            if (dbDong != "") {
                                                for (myTime in ref) {
                                                    if (dataSnapshot.child(i.toString()).child(si).child(gu).child(dbDong).hasChild(myTime[0])) {
                                                        for (time in dataSnapshot.child(i.toString()).child(si).child(gu).child(dbDong).child(myTime[0]).children) {
                                                            var infected_start_string = time.key.toString().substring(0, 4)
                                                            var infected_end_string = time.key.toString().substring(5, 9)
                                                            Log.d("infected_end_string",infected_end_string)
                                                            var infected_start_minus = ""
                                                            var infected_end_plus = ""
                                                            if (infected_start_string.substring(2, 4).toInt() < 30) {
                                                                if (infected_start_string.substring(0, 2).toInt().equals(0)) {
                                                                    //00시 10분이면
                                                                    infected_start_minus = (-(60 - (30 - infected_start_string.substring(2, 4).toInt()))).toString()
                                                                } else {
                                                                    infected_start_minus = (infected_start_string.substring(0, 2).toInt() - 1).toString() + (60 - (30 - infected_start_string.substring(2,4).toInt())).toString()
                                                                }
                                                            } else {
                                                                infected_start_minus = infected_start_string.substring(0, 2) + ((infected_start_string.substring(2, 4).toInt() - 30)).toString()
                                                            }
                                                            if (infected_end_string.substring(2, 4).toInt() > 30) {
                                                                infected_end_plus = (infected_end_string.substring(0, 2).toInt() + 1).toString() + (-60 + (30 + infected_end_string.substring(2, 4).toInt())).toString()
                                                            } else {
                                                                infected_end_plus = infected_end_string.substring(0, 2) + (infected_end_string.substring(2, 4).toInt() + 30).toString()
                                                            }
                                                            Log.d("infected edn plus", infected_end_plus)
                                                            if ((((infected_start_minus).toInt() <= myTime[1].toInt()) && (myTime[1].toInt() <= (infected_end_plus.toInt()))) || (((infected_start_minus).toInt() <= myTime[3].toInt()) && (myTime[3].toInt() <= (infected_end_plus.toInt()))) || (((infected_start_minus).toInt() >= myTime[1].toInt()) && (myTime[3].toInt() >= (infected_end_plus.toInt())))) {
                                                                //push알람

                                                                var mainKey = myTime[0] + " " + myTime[1]
                                                                var coronaST = myTime[0]
                                                                var coronaSH = infected_start_string
                                                                var infected_ent = myTime[0]
                                                                var coronaET: String
                                                                if (infected_end_string.substring(0, 2).toInt() >= 24) {
                                                                    var end_format = SimpleDateFormat("yyyyMMdd")
                                                                    var endTime = end_format.parse(time.key)
                                                                    coronaET = endTime.time.plus(1).toString()
                                                                } else {
                                                                    coronaET = myTime[0]
                                                                }
                                                                var coronaEH = infected_end_string

                                                                Log.d("코로나끝나는시간",coronaEH)
                                                                var corona_num = mutableListOf<String>()
                                                                for (z in time.children) {
                                                                    corona_num.add(z.key.toString())
                                                                }
                                                                var address = mutableListOf<String>()
                                                                var ETC = mutableListOf<String>()
                                                                var c = 0
                                                                for (z in corona_num) {
                                                                    var length_z = z.length + 3
                                                                    if (corona_num.size == c) {
                                                                        address.add(z + " - " + ((time.child(z).getValue().toString().split("="))[0]).substring(1, (time.child(z).getValue().toString().split("=")[0]).length))
                                                                        ETC.add(z + " - " + time.child(z).child(address[c].toString().substring(length_z)).getValue().toString())
                                                                        c += 1
                                                                    } else {
                                                                        address.add(z + " - " + ((time.child(z).getValue().toString().split("="))[0]).substring(1, (time.child(z).getValue().toString().split("=")[0]).length))
                                                                        ETC.add(z + " - " + time.child(z).child(address[c].toString().substring(length_z)).getValue().toString())
                                                                        c += 1
                                                                    }
                                                                }
                                                                var userST = myTime[0]
                                                                var userSH = myTime[1]
                                                                var userET = myTime[2]
                                                                var userEH = myTime[3]
                                                                var userGPS: String = myTime[4]
                                                                var infected_date_raw = i.toString()
                                                                var check_helper2 = coronaST+coronaSH+userSH
                                                                var infected_date = "${infected_date_raw.substring(4, 6).toInt()}월 ${infected_date_raw.substring(6).toInt()}일"
                                                                var corona_num_1: String = corona_num.joinToString(",", "", "")
                                                                var check = helper2.compareMemo("mainKey", "coronaEH", check_helper2, coronaEH)
                                                                var checkNum = 0
                                                                if (check.isEmpty()) {
                                                                    Log.d("del address", "$address")
                                                                    Log.d("del ETC", "$ETC")
                                                                    val memo = Memo2(check_helper2, corona_num_1, infected_date, coronaST, coronaSH, coronaET, coronaEH, address.toString().substring(1, address.toString().length - 1), ETC.toString().substring(1, ETC.toString().length - 1), si, gu, dbDong, userST, userSH, userET, userEH, userGPS)
                                                                    Log.d("memo",memo.toString())
                                                                    helper2.insertMemo(memo)
                                                                    overlapPush(memo)
                                                                } else {
                                                                    for (b in 0 until check.size) {
                                                                        if (checkNum != 3) {
                                                                            if (!(check[b][0] == check_helper2 && check[b][1] == coronaEH && check[b][2] == corona_num_1)) {
                                                                                if (check[b][0] == check_helper2 && check[b][1] == coronaEH && check[b][2] != corona_num_1) {
                                                                                    checkNum = 1
                                                                                } else {
                                                                                    if (checkNum != 1){
                                                                                        checkNum = 2
                                                                                    }
                                                                                }
                                                                            } else {
                                                                                checkNum = 3
                                                                            }
                                                                        }
                                                                    }
                                                                    if (checkNum == 1) {
                                                                        //시간은 다 같은데
                                                                        val memo = Memo2(check_helper2, corona_num_1, infected_date, coronaST, coronaSH, coronaET, coronaEH, address.toString().substring(1, address.toString().length - 1), ETC.toString().substring(1, ETC.toString().length - 1), si, gu, dbDong, userST, userSH, userET, userEH, userGPS)
                                                                        helper2.deleteMemo(memo)
                                                                        helper2.insertMemo(memo)
                                                                        overlapPush2(memo)
                                                                    } else if (checkNum == 2) {
                                                                        val memo = Memo2(check_helper2, corona_num_1, infected_date, coronaST, coronaSH, coronaET, coronaEH, address.toString().substring(1, address.toString().length - 1), ETC.toString().substring(1, ETC.toString().length - 1), si, gu, dbDong, userST, userSH, userET, userEH, userGPS)
                                                                        helper2.insertMemo(memo)
                                                                        overlapPush(memo)
                                                                    }

                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun startToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    fun overlapPush(memo: Memo2) {
        val NOTIFICATION_ID = System.currentTimeMillis().toInt()

        createNotificationChannel(
            this,
            NotificationManagerCompat.IMPORTANCE_HIGH,
            false,
            getString(R.string.app_name),
            "App overlap notification channel"
        )

        val channelId = "$packageName-${getString(R.string.app_name)}"
        val title = "${memo.userST.substring(4, 6).toInt()}월 ${memo.userST.substring(6)
            .toInt()}일 ${memo.dong}"
        val content = "${memo.userST.substring(4, 6).toInt()}월 ${memo.userST.substring(6)
            .toInt()}일 ${memo.dong} 방문 기록과 확진자 동선이 인접합니다"

        val intent = Intent(baseContext, DetailMyHistory::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("mainKey", memo.mainKey)
        intent.putExtra("num", memo.num)
        intent.putExtra("infectedDate", memo.infectedDate)
        intent.putExtra("coronaST", memo.coronaST)
        intent.putExtra("coronaSH", memo.coronaSH)
        intent.putExtra("coronaET", memo.coronaET)
        intent.putExtra("coronaEH", memo.coronaEH)
        intent.putExtra("address", memo.address)
        intent.putExtra("ETC", memo.etc)
        intent.putExtra("si", memo.si)
        intent.putExtra("gu", memo.gu)
        intent.putExtra("dong", memo.dong)
        intent.putExtra("userST", memo.userST)
        intent.putExtra("userSH", memo.userSH)
        intent.putExtra("userET", memo.userET)
        intent.putExtra("userEH", memo.userEH)
        intent.putExtra("userGPS", memo.userGps)
        intent.putExtra("noti", true)
        val fullScreenPendingIntent =
            PendingIntent.getActivity(baseContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this, channelId)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle(title)
        builder.priority = NotificationCompat.PRIORITY_HIGH
        builder.setAutoCancel(true)
        builder.setDefaults(Notification.DEFAULT_SOUND)
        builder.setContentIntent(fullScreenPendingIntent) //안되면 아랫줄
        //builder.setFullScreenIntent(fullScreenPendingIntent, true)

        val style = NotificationCompat.BigTextStyle(builder)
        style.bigText(content)

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun overlapPush2(memo: Memo2) {
        val NOTIFICATION_ID = System.currentTimeMillis().toInt()

        createNotificationChannel(
            this,
            NotificationManagerCompat.IMPORTANCE_HIGH,
            false,
            getString(R.string.app_name),
            "App overlap notification channel"
        )

        val channelId = "$packageName-${getString(R.string.app_name)}"
        val title = "${memo.userST.substring(4, 6).toInt()}월 ${memo.userST.substring(6)
            .toInt()}일 ${memo.dong}"
        val content = "${memo.userST.substring(4, 6).toInt()}월 ${memo.userST.substring(6)
            .toInt()}일 ${memo.dong} 방문 기록에 새로운 확진자 추가되었습니다"

        val intent = Intent(baseContext, DetailMyHistory::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("mainKey", memo.mainKey)
        intent.putExtra("num", memo.num)
        intent.putExtra("infectedDate", memo.infectedDate)
        intent.putExtra("coronaST", memo.coronaST)
        intent.putExtra("coronaSH", memo.coronaSH)
        intent.putExtra("coronaET", memo.coronaET)
        intent.putExtra("coronaEH", memo.coronaEH)
        intent.putExtra("address", memo.address)
        intent.putExtra("ETC", memo.etc)
        intent.putExtra("si", memo.si)
        intent.putExtra("gu", memo.gu)
        intent.putExtra("dong", memo.dong)
        intent.putExtra("userST", memo.userST)
        intent.putExtra("userSH", memo.userSH)
        intent.putExtra("userET", memo.userET)
        intent.putExtra("userEH", memo.userEH)
        intent.putExtra("userGPS", memo.userGps)
        intent.putExtra("noti", true)
        val fullScreenPendingIntent =
            PendingIntent.getActivity(baseContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this, channelId)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle(title)
        builder.priority = NotificationCompat.PRIORITY_HIGH
        builder.setAutoCancel(true)
        builder.setDefaults(Notification.DEFAULT_SOUND)
        builder.setContentIntent(fullScreenPendingIntent) //안되면 아랫줄
        //builder.setFullScreenIntent(fullScreenPendingIntent, true)

        val style = NotificationCompat.BigTextStyle(builder)
        style.bigText(content)

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }


}