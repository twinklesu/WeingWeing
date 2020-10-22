package com.example.weingweing

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_location_page.*
import kotlinx.android.synthetic.main.text_page_recycler.view.*

class HistoryRecyclerAdapter: RecyclerView.Adapter<HistoryRecyclerAdapter.Holder>() {
    var state = 0
    var helper:SqliteHelper2? = null
    var listData = mutableListOf<Memo2>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.text_page_recycler, parent, false)
        return Holder(view)
    }
    override fun getItemCount(): Int {
        return listData.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val memo = listData.get(position)
        holder.setMemo(memo)
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context = itemView.context
        var mMemo:Memo2? = null
        init {
            itemView.buttonDetail.setOnClickListener {
                val intent = Intent(context, DetailMyHistory::class.java)
                intent.putExtra("mainKey", mMemo?.mainKey)
                intent.putExtra("num", mMemo?.num)
                intent.putExtra("infectedDate", mMemo?.infectedDate)
                intent.putExtra("coronaST", mMemo?.coronaST)
                intent.putExtra("coronaSH", mMemo?.coronaSH)
                intent.putExtra("coronaET", mMemo?.coronaET)
                intent.putExtra("coronaEH", mMemo?.coronaEH)
                intent.putExtra("address", mMemo?.address)
                intent.putExtra("ETC", mMemo?.etc)
                intent.putExtra("si", mMemo?.si)
                intent.putExtra("gu", mMemo?.gu)
                intent.putExtra("dong", mMemo?.dong)
                intent.putExtra("userST", mMemo?.userST)
                intent.putExtra("userSH", mMemo?.userSH)
                intent.putExtra("userET", mMemo?.userET)
                intent.putExtra("userEH", mMemo?.userEH)
                intent.putExtra("userGPS", mMemo?.userGps)
                intent.putExtra("noti", false)
                context.startActivity(intent)
            }
        }

        fun setMemo(memo:Memo2) {
            itemView.userST.text = "날짜  :  "+memo.userST.subSequence(0,4).toString()+"-"+memo.userST.subSequence(4,6).toString()+"-"+memo.userST.subSequence(6,8).toString()
            itemView.userEH.text = memo.userEH.subSequence(0,2).toString()+":"+memo.userEH.subSequence(2,4).toString()
            itemView.userSH.text = memo.userSH.subSequence(0,2).toString()+":"+memo.userSH.subSequence(2,4).toString()
            itemView.coronaSH.text = memo.coronaSH.subSequence(0,2).toString()+":"+memo.coronaSH.subSequence(2,4).toString()
            itemView.coronaEH.text = memo.coronaEH.subSequence(0,2).toString()+":"+memo.coronaEH.subSequence(2,4).toString()
            itemView.address.text = memo.address
            this.mMemo = memo
        }
    }

}