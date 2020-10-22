package com.example.weingweing

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_location_page.*
import java.text.SimpleDateFormat
import java.util.*

class LocationPage : AppCompatActivity() {
    val adapter = RecyclerAdapter()
    val helper = SqliteHelper(this, "memo.db", 1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_page)

        var realTime= intent.getLongExtra("currentTime",0)
        delete14()
        var getTime = intent.getLongExtra("currentTime",0)
        searchDate.text= SimpleDateFormat("yyyy-MM-dd").format(Date(getTime))
        @SuppressLint("MissingPermission")
        if(getTime+86400000>System.currentTimeMillis()){
            buttonPlus.visibility= View.INVISIBLE
        }
        adapter.helper = helper

        adapter.listData.addAll(helper.selectMemo(searchDate.text.subSequence(0,4).toString()+searchDate.text.subSequence(5,7).toString()+searchDate.text.subSequence(8,10).toString()))
        recyclerMemo.adapter = adapter
        recyclerMemo.layoutManager = LinearLayoutManager(this)
        buttonMinus.setOnClickListener {
            if(getTime-172800000<System.currentTimeMillis()-1296000000){
                Toast.makeText(this, "14일 이전의 기록은 삭제됩니다.", Toast.LENGTH_SHORT).show()
                buttonMinus.visibility= View.INVISIBLE
            }
            if(getTime-86400000>=System.currentTimeMillis()-1296000000){
                getTime -= 86400000
                searchDate.text= SimpleDateFormat("yyyy-MM-dd").format(Date(getTime))
                adapter.listData.clear()
                adapter.listData.addAll(helper.selectMemo(searchDate.text.subSequence(0,4).toString()+searchDate.text.subSequence(5,7).toString()+searchDate.text.subSequence(8,10).toString()))
                adapter.notifyDataSetChanged()
                buttonPlus.visibility= View.VISIBLE
            }}
        buttonPlus.setOnClickListener {
            if(getTime+172800000>=System.currentTimeMillis()){
                buttonPlus.visibility= View.INVISIBLE
            }
            if(getTime+86400000<System.currentTimeMillis()){
                getTime += 86400000
                searchDate.text= SimpleDateFormat("yyyy-MM-dd").format(Date(getTime))
                adapter.listData.clear()
                adapter.listData.addAll(helper.selectMemo(searchDate.text.subSequence(0,4).toString()+searchDate.text.subSequence(5,7).toString()+searchDate.text.subSequence(8,10).toString()))
                adapter.notifyDataSetChanged()
                buttonMinus.visibility= View.VISIBLE
            }}
        buttonSave.setOnClickListener {
            val intent = Intent(this, AddNewLocation::class.java)
            val day = searchDate.text.subSequence(0,4).toString()+searchDate.text.subSequence(5,7).toString()+searchDate.text.subSequence(8,10).toString()
            intent.putExtra("day_start", day)
            startActivity(intent)
            finish()

        }
    }
    fun delete14() {
        val time = System.currentTimeMillis()
        var dateFormat = SimpleDateFormat("yyyyMMdd")
        val realTime = dateFormat.format(Date(time-1209600000)).toString()
        helper.deleteDate(realTime)
    }
    override fun onBackPressed() {
        intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }
}