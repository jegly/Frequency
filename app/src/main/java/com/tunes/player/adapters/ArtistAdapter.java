package com.tunes.player.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tunes.player.R;
import com.tunes.player.interfaces.ItemClickListener;
import com.tunes.player.model.ArtistModel;

import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistSVH> {

    private List<ArtistModel> data;
    private ItemClickListener.SingleEvent mListener;
    private LayoutInflater mInflater;

    private static final String[] PASTEL_COLORS = {
            "#FFB3BA", "#FFDFBA", "#FFFFBA", "#BAFFC9", "#BAE1FF", "#E0BBE4", "#D4F0F0", "#FEC8D8", "#FFD1DC", "#ECEAE4",
            "#F0E68C", "#E6E6FA", "#FFF0F5", "#F0FFF0", "#F5FFFA", "#F0FFFF", "#F0F8FF", "#E0FFFF", "#AFEEEE", "#7FFFD4",
            "#B0E0E6", "#5F9EA0", "#4682B4", "#6495ED", "#00BFFF", "#1E90FF", "#ADD8E6", "#87CEEB", "#87CEFA", "#B0C4DE"
    };

    public ArtistAdapter(List<ArtistModel> list, LayoutInflater mInflater, ItemClickListener.SingleEvent mListener) {
        this.mInflater = mInflater;
        this.mListener = mListener;
        this.data = list;
    }

    @NonNull
    @Override
    public ArtistAdapter.ArtistSVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ArtistSVH(mInflater.inflate(R.layout.rv_grid_item_artist, parent, false), mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistSVH holder, int position) {
        holder.setData(data.get(position), position);
    }

    @Override
    public int getItemCount() {
        if (null != data)
            return data.size();
        return 0;
    }

    static class ArtistSVH extends RecyclerView.ViewHolder {

        private TextView title;
        private ImageView thumbnail;

        ArtistSVH(@NonNull View itemView, ItemClickListener.SingleEvent mListener) {
            super(itemView);
            title = itemView.findViewById(R.id.grid_item_artist_tv);
            thumbnail = itemView.findViewById(R.id.grid_item_artist_iv);
            itemView.setOnClickListener(v -> mListener.onClickItem(getAdapterPosition()));
        }

        void setData(ArtistModel am, int position) {
            title.setText(am.getArtistName());
            
            // Use position-based logic to ensure adjacent colors are different
            // and include artist name hash to maintain consistency for the same list
            int colorIndex = (position + Math.abs(am.getArtistName().hashCode())) % PASTEL_COLORS.length;
            
            // Simple check to avoid same color as previous item
            if (position > 0 && colorIndex == (position - 1 + Math.abs(am.getArtistName().hashCode())) % PASTEL_COLORS.length) {
                colorIndex = (colorIndex + 1) % PASTEL_COLORS.length;
            }

            int color = Color.parseColor(PASTEL_COLORS[colorIndex]);
            
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setColor(color);
            shape.setCornerRadius(16);
            
            thumbnail.setImageDrawable(null);
            thumbnail.setBackground(shape);
        }
    }
}
