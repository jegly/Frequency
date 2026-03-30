package com.tunes.player.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tunes.player.R;
import com.tunes.player.interfaces.ItemClickListener;
import com.tunes.player.model.AlbumModel;

import java.util.List;
import java.util.Random;

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.AlbumsSVH> {

    private List<AlbumModel> data;
    private ItemClickListener.SingleEvent mListener;
    private LayoutInflater mInflater;
    private final int[] colors = {
            Color.parseColor("#E91E63"), // Pink
            Color.parseColor("#9C27B0"), // Purple
            Color.parseColor("#673AB7"), // Deep Purple
            Color.parseColor("#3F51B5"), // Indigo
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#009688"), // Teal
            Color.parseColor("#FF9800"), // Orange
            Color.parseColor("#795548")  // Brown
    };

    public AlbumsAdapter(List<AlbumModel> list, LayoutInflater mInflater, ItemClickListener.SingleEvent mListener) {
        this.mInflater = mInflater;
        this.mListener = mListener;
        this.data = list;
    }

    @NonNull
    @Override
    public AlbumsSVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AlbumsSVH(mInflater.inflate(R.layout.rv_grid_item, parent, false), mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumsSVH holder, int position) {
        holder.setData(data.get(position), colors[new Random(data.get(position).getAlbumName().hashCode()).nextInt(colors.length)]);
    }

    @Override
    public int getItemCount() {
        if (null != data)
            return data.size();
        return 0;
    }

    static class AlbumsSVH extends RecyclerView.ViewHolder {

        private TextView title;
        private ImageView art;

        AlbumsSVH(@NonNull View itemView, ItemClickListener.SingleEvent mListener) {
            super(itemView);
            art = itemView.findViewById(R.id.grid_item_iv);
            title = itemView.findViewById(R.id.grid_item_tv);
            itemView.setOnClickListener(v -> mListener.onClickItem(getAdapterPosition()));
        }

        void setData(AlbumModel am, int color) {
            art.setImageDrawable(null);
            art.setBackgroundColor(color);
            title.setText(am.getAlbumName());
        }
    }
}
