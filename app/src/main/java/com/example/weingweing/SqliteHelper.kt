package com.example.weingweing

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class SqliteHelper(context: Context, name: String, version: Int) :
    SQLiteOpenHelper(context, name, null, version) {

    override fun onCreate(db: SQLiteDatabase?) {
        val create = "create table memo (" +
                "mainKey text primary key," +
                "endKey text, " +
                "startTime text, " +
                "startHour text, " +
                "endTime text, " +
                "endHour text, " +
                "gps text, " +
                "address text, " +
                "si text, " +
                "gu text, " +
                "dong text" +
                ")"
        db?.execSQL(create)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    fun insertMemo(memo:Memo) {
        val wd = writableDatabase
        if (memo.startTime == memo.endTime){
            val values = ContentValues()
            values.put("mainKey", memo.mainKey)
            values.put("endKey", memo.endKey)
            values.put("startTIme", memo.startTime)
            values.put("startHour", memo.startHour)
            values.put("endTime", memo.endTime)
            values.put("endHour", memo.endHour)
            values.put("gps", memo.gps)
            values.put("address", memo.address)
            values.put("dong", memo.dong)
            values.put("gu", memo.gu)
            values.put("si", memo.si)
            wd.insert("memo", null, values)

        }else{
            val interval = memo.endTime.toInt() - memo.startTime.toInt() // 16-15=
            Log.d("values", "interval: $interval")
            for (i in 1 until interval){
                val values = ContentValues()
                values.put("mainKey", (memo.startTime.toInt() + i).toString() + " 0000")
                values.put("endKey", (memo.startTime.toInt() + i).toString()+" 2359")
                values.put("startTIme", (memo.startTime.toInt() + i).toString())
                values.put("startHour", "0000")
                values.put("endTime", (memo.startTime.toInt() + i).toString())
                values.put("endHour", "2359")
                values.put("gps", memo.gps)
                values.put("address", memo.address)
                values.put("dong", memo.dong)
                values.put("gu", memo.gu)
                values.put("si", memo.si)
                wd.insert("memo", null, values)
                Log.d("values", "현재 memo: $values")
            }
            val values1 = ContentValues()
            val values2 = ContentValues()
            values1.put("mainKey", memo.mainKey)
            values1.put("endKey", memo.startTime+" 2359")
            values1.put("startTIme", memo.startTime)
            values1.put("startHour", memo.startHour)
            values1.put("endTime", memo.startTime)
            values1.put("endHour", "2359")
            values1.put("gps", memo.gps)
            values1.put("address", memo.address)
            values1.put("dong", memo.dong)
            values1.put("gu", memo.gu)
            values1.put("si", memo.si)

            values2.put("mainKey", memo.endTime + " 0000")
            values2.put("endKey", memo.endTime + memo.endHour)
            values2.put("startTIme", memo.endTime)
            values2.put("startHour", "0000")
            values2.put("endTime", memo.endTime)
            values2.put("endHour", memo.endHour)
            values2.put("gps", memo.gps)
            values2.put("address", memo.address)
            values2.put("dong", memo.dong)
            values2.put("gu", memo.gu)
            values2.put("si", memo.si)
            Log.d("values1", "현재 memo: $values1")
            Log.d("values2", "현재 memo: $values2")
            wd.insert("memo", null, values1)
            wd.insert("memo", null, values2)
        }
        wd.close()
    }

    fun selectMemo(date:String): MutableList<Memo> {
        val list = mutableListOf<Memo>()
        val select = "select * from memo where startTime=$date order by mainKey"
        val rd = readableDatabase
        try {
            val cursor = rd.rawQuery(select, null)
            while (cursor.moveToNext()) {
                val mainKey = cursor.getString(cursor.getColumnIndex("mainKey"))
                val endKey = cursor.getString(cursor.getColumnIndex("endKey"))
                val startTime = cursor.getString(cursor.getColumnIndex("startTime"))
                val startHour = cursor.getString(cursor.getColumnIndex("startHour"))
                val endTime = cursor.getString(cursor.getColumnIndex("endTime"))
                val endHour = cursor.getString(cursor.getColumnIndex("endHour"))
                val gps = cursor.getString(cursor.getColumnIndex("gps"))
                val address = cursor.getString(cursor.getColumnIndex("address"))
                val si = cursor.getString(cursor.getColumnIndex("si"))
                val gu = cursor.getString(cursor.getColumnIndex("gu"))
                val dong = cursor.getString(cursor.getColumnIndex("dong"))
                list.add(
                    Memo(
                        mainKey,
                        endKey,
                        startTime,
                        startHour,
                        endTime,
                        endHour,
                        gps,
                        address,
                        si,
                        gu,
                        dong
                    )
                )
            }
            cursor.close()
        }
        finally {
            rd.close()
        }
        return list
    }

    fun getGPS(mainKey:String): String {
        var get = ""
        val select = "select * from memo where mainKey = '$mainKey'"
        val rd = readableDatabase
        try {
            val cursor = rd.rawQuery(select, null)
            while (cursor.moveToNext()) {
                val gps = cursor.getString(cursor.getColumnIndex("gps"))
                get = gps
            }
            cursor.close()
        }
        finally{
            rd.close()
        }
        return get
    }

    fun compareMemo(col1:String,col2:String,col3:String, criteria1:String,criteria2:String,criteria3:String): MutableList<MutableList<String>> {
        var list = mutableListOf<String>()
        var result = mutableListOf<MutableList<String>>()
        val select = "select * from memo where $col1 = '$criteria1' and $col2 = '$criteria2' and $col3 = '$criteria3'"
        val rd = readableDatabase
        try{
            val cursor = rd.rawQuery(select, null)
            while (cursor.moveToNext()) {
                val startTime = cursor.getString(cursor.getColumnIndex("startTime"))
                val startHour = cursor.getString(cursor.getColumnIndex("startHour"))
                val endTime = cursor.getString(cursor.getColumnIndex("endTime"))
                val endHour = cursor.getString(cursor.getColumnIndex("endHour"))
                val gpsPoint = cursor.getString(cursor.getColumnIndex("gps"))
                list=mutableListOf(startTime,startHour,endTime,endHour, gpsPoint)
                result.add(list)
            }
            cursor.close()
        }
        finally{
            rd.close()

        }
        return result
    }

    fun getMemo(item:String): MutableList<String> {
        val list = mutableListOf<String>()
        val select = "select * from memo"
        val rd = readableDatabase
        try {
            val cursor = rd.rawQuery(select, null)
            while (cursor.moveToNext()) {
                val contents = cursor.getString(cursor.getColumnIndex("$item"))
                list.add(contents)
            }
            cursor.close()
        }
        finally{
            rd.close()
        }
        return list
    }

    fun checkToInsert(start:String, end:String): Boolean {
        var list = mutableListOf<String>()
        val rd = readableDatabase
        var result = mutableListOf<MutableList<String>>()
        var iter = mutableListOf<String>("select * from memo where mainKey < '$end' and endKey > '$end' ",
            "select * from memo where mainKey < '$start' and endKey > '$end' ",
            "select * from memo where mainKey < '$start' and endKey > '$start' ",
            "select * from memo where mainKey > '$start' and endKey < '$end' ")
        for (select in iter){
            val cursor = rd.rawQuery(select, null)
            while (cursor.moveToNext()) {
                val mainKey = cursor.getString(cursor.getColumnIndex("mainKey"))
                list=mutableListOf(mainKey)
                result.add(list)
            }}
        rd.close()
        Log.d("checkToInsert", "result: $result")
        val tf = result.isEmpty()
        return tf
    }


//    fun updateMemo(memo:Memo) {
//        val values = ContentValues()
//        values.put("content", memo.content)
//        values.put("datetime", memo.datetime)
//
//        val wd = writableDatabase
//        wd.update("memo", values, "no = ${memo.startTime}", null)
//        wd.close()
//    }

    fun deleteMemo(memo:Memo) {
        val delete = "delete from memo where mainKey = '${memo.mainKey}'"
        val db = writableDatabase
        db.execSQL(delete)
        db.close()
    }
    fun deleteDate(date:String){
        val deleData = "delete from memo where startTime < '$date'"
        val db = writableDatabase
        db.execSQL(deleData)
        db.close()
    }
}

data class Memo(var mainKey:String, var endKey:String, var startTime:String, var startHour:String, var endTime:String, var endHour:String, var gps:String, var address:String, var si:String, var gu:String, var dong:String)