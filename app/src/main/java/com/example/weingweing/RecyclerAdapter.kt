package com.example.weingweing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_recycler.view.*

class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.Holder>() {
    var helper:SqliteHelper? = null
    var listData = mutableListOf<Memo>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recycler, parent, false)
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
        var mMemo:Memo? = null
        init {
            itemView.buttonDelete.setOnClickListener {
                helper?.deleteMemo(mMemo!!)
                listData.remove(mMemo)
                notifyDataSetChanged()
            }
        }
        fun setMemo(memo:Memo) {
            itemView.userSH.text = memo.startTime.subSequence(0,4).toString()+"-"+memo.startTime.subSequence(4,6).toString()+"-"+memo.startTime.subSequence(6,8).toString()+" "+memo.startHour.subSequence(0,2).toString()+":"+memo.startHour.subSequence(2,4).toString()
            itemView.userEH.text = memo.endTime.subSequence(0,4).toString()+"-"+memo.endTime.subSequence(4,6).toString()+"-"+memo.endTime.subSequence(6,8).toString()+" "+memo.endHour.subSequence(0,2).toString()+":"+memo.endHour.subSequence(2,4).toString()
            //itemView.textGps.text = memo.gps
            itemView.textDong.text = memo.address
            this.mMemo = memo
        }
    }
}

