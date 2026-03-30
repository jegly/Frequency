package com.tunes.player.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tunes.player.R;
import com.tunes.player.interfaces.ItemClickListener;
import com.tunes.player.model.AlbumModel;

import java.util.List;

public class HomeAdapterAlbum extends RecyclerView.Adapter<HomeAdapterAlbum.AdapterSVH> {

    private final LayoutInflater mInflater;
    private final List<AlbumModel> mList;
    private final ItemClickListener.Simple mListener;

    public HomeAdapterAlbum(LayoutInflater inflater, List<AlbumModel> list, ItemClickListener.Simple listener) {
        this.mInflater = inflater;
        this.mList = list;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public HomeAdapterAlbum.AdapterSVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HomeAdapterAlbum.AdapterSVH(mInflater.inflate(R.layout.rv_album_card_item, parent, false), mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeAdapterAlbum.AdapterSVH holder, int position) {
        holder.updateData(mList.get(position));
    }

    @Override
    public int getItemCount() {
        if (null != mList)
            return mList.size();
        return 0;
    }

    static class AdapterSVH extends RecyclerView.ViewHolder {

        private final TextView tv;

        AdapterSVH(@NonNull View itemView, ItemClickListener.Simple listener) {
            super(itemView);
            tv = itemView.findViewById(R.id.rv_item_title);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onOptionsClick(v, position);
                }
            });
        }

        void updateData(AlbumModel am) {
            if (tv != null) tv.setText(am.getAlbumName());
        }
    }
}
