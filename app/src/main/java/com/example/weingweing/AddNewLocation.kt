package com.example.weingweing

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_new_location.*
import java.text.SimpleDateFormat
import java.util.*


class AddNewLocation : AppCompatActivity() {
    companion object{
        lateinit var location_tuple: String
        lateinit var dong: String
        lateinit var si: String
        lateinit var gu: String
    }

    var day_start = ""
    var day_end = ""
    var time_start = ""
    var time_end = ""
    val helper = SqliteHelper(this, "memo.db", 1)

    val cal = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_location)

        val intent = getIntent()
        location_tuple = ""
        /*
        기본값으로 day_start는 무조건 주어진다.
        추가하기 버튼으로 들어온 경우: day_start 만 intent에 있음. day_end 값은 자동으로 start와 같은 날짜로 맞춘다
        푸쉬로 들어온 경우: day_start, end, time 다 intent 값 받아서 해주기
         */
        day_start = intent.getStringExtra("day_start").toString() //yyyymmdd 이거는 무조건 값이 있다
        if(intent.hasExtra("day_end")){ //푸쉬로 들어온 경우
            day_end = intent.getStringExtra("day_end").toString()
            time_start = intent.getStringExtra("start").toString() //hhmm
            time_end = intent.getStringExtra("end").toString() // hhmm

            btn_time_start.text = "${time_start.substring(0,2)} : ${time_start.substring(2)}"
            btn_time_end.text = "${time_end.substring(0,2)} : ${time_end.substring(2)}"
        }
        else { // 버튼으로 들어온 경우 day_end == day_start
            day_end = intent.getStringExtra("day_start").toString()
        }
        // 두 경우 모두 해당되는 코드
        btn_date_end.text = "${day_end.substring(4,6).toInt()}월 ${day_end.substring(6).toInt()}일"
        btn_date_start.text = "${day_start.substring(4,6).toInt()}월 ${day_start.substring(6).toInt()}일"


        // 날짜 선택 버튼
        btn_date_start.setOnClickListener {
            showDatePicker(btn_date_start, day_start, "start")
        }

        // 시작 시간 선택 버튼
        btn_time_start.setOnClickListener {
            showTimePicker(btn_time_start, time_start, "start")
        }

        // 끝 날짜 선택
        btn_date_end.setOnClickListener {
            showDatePicker(btn_date_end, day_end, "end")
        }
        // 끝 시간 선택 버튼
        btn_time_end.setOnClickListener {
            showTimePicker(btn_time_end, time_end, "end")
        }

        // 지도

        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.frame_map, MapsFragment().apply{
            arguments = Bundle().apply {
                putString("addr", "")
            }
        }).commit()

        val et_addr = findViewById<EditText>(R.id.et_address)

        btn_address.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(et_address.windowToken, 0)
            if(et_addr.text.isNotEmpty()) {
                val ft = supportFragmentManager.beginTransaction()
                ft.replace(R.id.frame_map, MapsFragment().apply{
                    arguments = Bundle().apply {
                        Log.d("editText", et_addr.text.toString())
                        putString("addr", et_addr.text.toString())
                    }
                }).commit()
            }
        }



        //'입력하기' 버튼 누를 시 '내 위치 기록'으로 이동
        //★할 수 있다면 추가한 날의 기록으로 이동하면 좋겠다!★
        btn_input.setOnClickListener {
            val time = System.currentTimeMillis()

            var dateFormat = SimpleDateFormat("yyyyMMdd")
            var a =day_start.toInt()
            var b =day_end.toInt()

            println(((SimpleDateFormat("yyyyMMddHHmm").format(Date(time))).toLong() < (day_end+time_end).toLong()))
            println(((day_end+time_end).toLong()<(day_start+time_start).toLong()))
            if (day_start=="" || day_end=="" || time_start=="" || time_end=="" || location_tuple=="" || dong =="" || si=="" || gu==""){
                Toast.makeText(this, "입력되지 않은 값이 있습니다.\n다시 확인해주세요", Toast.LENGTH_SHORT).show()
            }
            else if ((day_start.toInt() > (dateFormat.format(Date(time)).toInt())) ||
                (day_end.toInt() > (dateFormat.format(Date(time)).toInt())) ||
                (day_start.toInt() > day_end.toInt())) {
                Toast.makeText(this, "날짜를 확인해 주세요!", Toast.LENGTH_SHORT).show()
            }else if (((SimpleDateFormat("yyyyMMddHHmm").format(Date(time))).toLong() < (day_end+time_end).toLong())||((day_end+time_end).toLong()<(day_start+time_start).toLong())) { //같은 날 일때는 시간 순서 고려

                Toast.makeText(this, "시간을 확인해 주세요!", Toast.LENGTH_SHORT).show()
            }else if(!helper.checkToInsert(day_start+" "+time_start, day_end+" "+time_end)){
                Toast.makeText(this, "지정한 시간 사이에 이미 저장된 위치기록이 있습니다.", Toast.LENGTH_LONG).show()
            }else{
                //여기서 DB에 저장
                val storeDB = Memo(day_start+" "+time_start, day_end+" "+time_end, day_start, time_start, day_end, time_end, location_tuple, si+" "+gu+" "+dong, si, gu, dong)
                helper.insertMemo(storeDB)
                val intent = Intent(this, LocationPage::class.java)
                val input = SimpleDateFormat("yyyyMMdd").parse(day_start).time
                intent.putExtra("currentTime",input)
                startActivity(intent)
                finish()
            }
        }
    }

    /*
    날짜 고르는 함수
    day는 global 변수
    day_Strign은 "start"거나 "end"
     */
    private fun showDatePicker(btn : Button, day: String, day_String: String) {
        if(day_String == "start") {
            cal.set(Integer.parseInt(day.substring(0,4)), Integer.parseInt(day.substring(4,6))-1, Integer.parseInt(day.substring(6)))
            DatePickerDialog(this, DatePickerDialog.OnDateSetListener { datePicker, year_, month_, date_ ->
                btn.text = "${month_+1}월 ${date_}일"  //month는 0부터 시작
                day_start = year_.toString() + "%02d".format(month_ +1) + "%02d".format(date_)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE)).show()
        }
        else { //end 버튼일 때
            cal.set(Integer.parseInt(day.substring(0,4)), Integer.parseInt(day.substring(4,6))-1, Integer.parseInt(day.substring(6)))
            DatePickerDialog(this, DatePickerDialog.OnDateSetListener { datePicker, year_, month_, date_ ->
                btn.text = "${month_+1}월 ${date_}일"  //month는 0부터 시작
                day_end = year_.toString() + "%02d".format(month_ +1) + "%02d".format(date_)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE)).show()
        }
    }

    private fun showTimePicker(btn : Button, time : String, time_String: String) {
        if (time != "") {
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.substring(0,2)))
            cal.set(Calendar.MINUTE, Integer.parseInt(time.substring(2)))
        }
        TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { timePicker, hr_, min_->
            btn.text = "%02d".format(hr_) + " : " + "%02d".format(min_)
            if (time_String == "start") {
                time_start = "%02d".format(hr_) + "%02d".format(min_)
            }
            else {
                time_end = "%02d".format(hr_) + "%02d".format(min_)
            }
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show() //24시간제 할지 12시간제 할지

    }
    override fun onBackPressed() {
        intent = Intent(this, LocationPage::class.java)
        cal.set(Integer.parseInt(day_start.substring(0,4)), Integer.parseInt(day_start.substring(4,6))-1, Integer.parseInt(day_start.substring(6,8)))
        intent.putExtra("currentTime", cal.timeInMillis)
        startActivity(intent)
        finishAffinity()
    }
}