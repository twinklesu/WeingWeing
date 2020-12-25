package com.example.weingweing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_location_page.*
import kotlinx.android.synthetic.main.text_page_recycler.*
import kotlinx.android.synthetic.main.text_page_recycler.view.*
import java.text.SimpleDateFormat
import java.util.*


class TextPage : AppCompatActivity() {
    val helper = SqliteHelper2(this, "memo2.db", 1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_page)
//data class Memo2(var mainKey:String, var num:String, var infectedDate:String, var coronaST:String, var coronaSH:String,
// var coronaET:String, var coronaEH:String, var address:String, var etc:String, var si:String, var gu:String, var dong:String,
//                 var userST:String, var userSH:String, var userET:String, var userEH:String, var userGps:String)
        val adapter = HistoryRecyclerAdapter()
        val view: View = findViewById(R.id.recyclerMemo)
        adapter.helper = helper
        delete14()
        val initMemo = Memo2(SimpleDateFormat("yyyyMMdd").format(Date(System.currentTimeMillis())).toString(), "예시데이터",SimpleDateFormat("yyyyMMdd").format(Date(System.currentTimeMillis()-172800000)).toString(),SimpleDateFormat("yyyyMMdd").format(Date(System.currentTimeMillis()-86400000)).toString(),"1310",SimpleDateFormat("yyyyMMdd").format(Date(System.currentTimeMillis()-86400000)).toString(),"1320","서울특별시 서대문구 연희동","마스크착용","서울특별시","서대문구","연희동",SimpleDateFormat("yyyyMMdd").format(Date(System.currentTimeMillis()-86400000)).toString(),"1300",SimpleDateFormat("yyyyMMdd").format(Date(System.currentTimeMillis()-86400000)).toString(),"1330","(37.571797499999995, 126.93252539999999)")
        helper.insertMemo(initMemo)
        adapter.listData.addAll(helper.selectMemo())
        recyclerMemo.adapter = adapter
        recyclerMemo.layoutManager = LinearLayoutManager(this)
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