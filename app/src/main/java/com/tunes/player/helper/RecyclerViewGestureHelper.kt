package com.tunes.player.helper

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.tunes.player.interfaces.RecyclerViewGestures

class RecyclerViewGestureHelper(private val mCompletionCallback: RecyclerViewGestures.GestureCallback) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        mCompletionCallback.onItemSwiped(viewHolder.adapterPosition)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        mCompletionCallback.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onMoved(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, fromPos: Int, target: RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {
        mCompletionCallback.onItemMoved(fromPos, toPos)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        mCompletionCallback.onClearView(recyclerView, viewHolder)
        super.clearView(recyclerView, viewHolder)
    }
}
