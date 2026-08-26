package com.jegly.frequency.interfaces

import androidx.recyclerview.widget.RecyclerView

interface RecyclerViewGestures {
    interface GestureCallback {
        fun onItemSwiped(position: Int)
        fun onItemMove(fromPosition: Int, toPosition: Int)
        fun onItemMoved(fromPosition: Int, toPosition: Int)
        fun onClearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder)
    }
}
