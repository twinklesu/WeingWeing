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

        val adapter = HistoryRecyclerAdapter()
        val view: View = findViewById(R.id.recyclerMemo)
        delete14()
        adapter.helper = helper
        adapter.listData.addAll(helper.selectMemo())
        recyclerMemo.adapter = adapter
        recyclerMemo.layoutManager = LinearLayoutManager(this)

        val memo = Memo2(
            "20201011 1117", "201", "20201011","20200923", "1000", "20200923", "1100", "서울특별시 노원구 공릉동",
            "기타사항", "서울특별시", "노원구", "공릉동", "20201011", "1020", "20200923", "1040", "(37.65,127.384)"
        )
        helper.insertMemo(memo)
        val memo1 = Memo2(
            "20201011 1110", "201", "20201011","20200923", "1000", "20200923", "1100", "서울특별시 노원구 하계동",
            "기타사항", "서울특별시", "노원구", "하계동", "20201011", "1020", "20200923", "1040", "(37.65,127.384)"
        )
        helper.insertMemo(memo1)
    }


//
//        var pri = helper.getMemo("dong").distinct()
//
//        var ref = helper.compareMemo("gu","노원구")
    //Log.d("Cheo", "$ref")

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