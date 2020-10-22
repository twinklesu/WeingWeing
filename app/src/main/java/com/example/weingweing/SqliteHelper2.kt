package com.example.weingweing

import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SqliteHelper2(context: Context, name: String, version: Int) : SQLiteOpenHelper(context, name, null, version) {
    override fun onCreate(db: SQLiteDatabase?) {
        val create = "create table memo2 (" +
                "mainKey text primary key," + "num text,"+"infectedDate text,"+
                "coronaST text, " + "coronaSH text, " + "coronaET text, " + "coronaEH text, " +
                "address text, " + "etc text, " + "si text, " + "gu text, " + "dong text, " +
                "userST text, " + "userSH text, " + "userET text, " + "userEH text, " +
                "userGps text" +
                ")"
        db?.execSQL(create)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    fun insertMemo(memo: Memo2) {
        val values = ContentValues()
        values.put("mainKey", memo.mainKey)
        values.put("num", memo.num)
        values.put("infectedDate", memo.infectedDate)
        values.put("coronaST", memo.coronaST)
        values.put("coronaSH", memo.coronaSH)
        values.put("coronaET", memo.coronaET)
        values.put("coronaEH", memo.coronaEH)
        values.put("address", memo.address)
        values.put("etc", memo.etc)
        values.put("si", memo.si)
        values.put("gu", memo.gu)
        values.put("dong", memo.dong)
        values.put("userST", memo.userST)
        values.put("userSH", memo.userSH)
        values.put("userET", memo.userET)
        values.put("userEH", memo.userEH)
        values.put("userGps", memo.userGps)
        val wd = writableDatabase
        wd.insert("memo2", null, values)
        wd.close()
    }

    fun selectMemo(): MutableList<Memo2> {
        val list = mutableListOf<Memo2>()
        val select = "select * from memo2"
        val rd = readableDatabase
        try{
            val cursor = rd.rawQuery(select, null)
            while (cursor.moveToNext()) {
                val mainKey = cursor.getString(cursor.getColumnIndex("mainKey"))
                val num = cursor.getString(cursor.getColumnIndex("num"))
                val infectedDate = cursor.getString(cursor.getColumnIndex("infectedDate"))
                val coronaST = cursor.getString(cursor.getColumnIndex("coronaST"))
                val coronaSH = cursor.getString(cursor.getColumnIndex("coronaSH"))
                val coronaET = cursor.getString(cursor.getColumnIndex("coronaET"))
                val coronaEH = cursor.getString(cursor.getColumnIndex("coronaEH"))
                val address = cursor.getString(cursor.getColumnIndex("address"))
                val etc = cursor.getString(cursor.getColumnIndex("etc"))
                val userST = cursor.getString(cursor.getColumnIndex("userST"))
                val userSH = cursor.getString(cursor.getColumnIndex("userSH"))
                val userET = cursor.getString(cursor.getColumnIndex("userET"))
                val userEH = cursor.getString(cursor.getColumnIndex("userEH"))
                val userGps = cursor.getString(cursor.getColumnIndex("userGps"))
                val si = cursor.getString(cursor.getColumnIndex("si"))
                val gu = cursor.getString(cursor.getColumnIndex("gu"))
                val dong = cursor.getString(cursor.getColumnIndex("dong"))
                list.add(Memo2(mainKey, num, infectedDate, coronaST, coronaSH, coronaET, coronaEH, address, etc, si, gu, dong, userST, userSH, userET, userEH, userGps))
            }
            cursor.close()
        }
        finally {
            rd.close()
        }
        return list
    }

    fun compareMemo(col1:String,col2:String, criteria1:String,criteria2:String) :  MutableList<MutableList<String>>{
        var list = mutableListOf<String>()
        var result = mutableListOf<MutableList<String>>()
        val select = "select * from memo2 where $col1 = '$criteria1' and $col2 = '$criteria2'"
        val rd = readableDatabase
        try {
            val cursor = rd.rawQuery(select, null)
            while (cursor.moveToNext()) {
                val mainKey = cursor.getString(cursor.getColumnIndex("mainKey"))
                val endTime = cursor.getString(cursor.getColumnIndex("coronaEH"))
                list = mutableListOf(mainKey, endTime)
                result.add(list)
            }
            cursor.close()
        }
        finally {
            rd.close()
        }
        return result
    }

    fun deleteDate(date: String) {
        val deleData = "delete from memo2 where userST < '$date'"
        val db = writableDatabase
        db.execSQL(deleData)
        db.close()
    }

    fun deleteMemo(memo:Memo2) {
        val delete = "delete from memo2 where mainKey = '${memo.mainKey}'"
        val db = writableDatabase
        db.execSQL(delete)
        db.close()
    }
}


data class Memo2(var mainKey:String, var num:String, var infectedDate:String, var coronaST:String, var coronaSH:String, var coronaET:String, var coronaEH:String, var address:String, var etc:String, var si:String, var gu:String, var dong:String,
                 var userST:String, var userSH:String, var userET:String, var userEH:String, var userGps:String)