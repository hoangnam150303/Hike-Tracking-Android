package com.example.android_coursework;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import java.util.List;

public class HikeAdapter extends RecyclerView.Adapter<HikeAdapter.HikeViewHolder> {

    private final List<HikeModel> hikeList; // create List with datatype is HikeModel
    private final Context context; // use for load image and open another activity

    // create constructor
    public HikeAdapter(Context context, List<HikeModel> hikeList) {
        this.context = context;
        this.hikeList = hikeList;
    }

    @NonNull
    @Override  // create view for each item
    public HikeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hike_card, parent, false);
        return new HikeViewHolder(view);
    }

    @Override // set data for item
    public void onBindViewHolder(@NonNull HikeViewHolder holder, int position) {

        HikeModel hike = hikeList.get(position);

        Log.d("HikeAdapter", "🖼 Loading image for " + hike.getTitle() + ": " + hike.getImageUri());

        // Load image
        if (hike.getImageUri() != null && !hike.getImageUri().isEmpty()) {
            // use library Glide to display image, this library i implemented in build.gradle
            Glide.with(context)
                    .load(Uri.parse(hike.getImageUri()))
                    .centerCrop()
                    .placeholder(R.drawable.hero1)
                    .error(R.drawable.hero2)
                    .into(holder.hikeImage);
        } else {
            holder.hikeImage.setImageResource(R.drawable.hero1);
        }

        // display name and length
        holder.hikeTitle.setText(hike.getTitle());
        holder.hikeLength.setText(String.format("%.1f km", hike.getLength()));

        // click card and paginate to detail item, put data into intent, it will use in other page
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, HikeDetailActivity.class);

            intent.putExtra("hike_id", hike.getId());
            intent.putExtra("image_uri", hike.getImageUri());
            intent.putExtra("hike_name", hike.getTitle());
            intent.putExtra("location", hike.getLocation());
            intent.putExtra("date", hike.getDate());
            intent.putExtra("parking", hike.getParking());
            intent.putExtra("length", hike.getLength());
            intent.putExtra("difficulty", hike.getDifficulty());
            intent.putExtra("description", hike.getDescription());
            intent.putExtra("weather", hike.getWeather());
            intent.putExtra("companions", hike.getCompanions());

            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return hikeList.size();
    }

    public static class HikeViewHolder extends RecyclerView.ViewHolder {
        ImageView hikeImage;
        TextView hikeTitle, hikeLength;

        public HikeViewHolder(@NonNull View itemView) {
            super(itemView);
            hikeImage = itemView.findViewById(R.id.hikeImage);
            hikeTitle = itemView.findViewById(R.id.hikeTitle);
            hikeLength = itemView.findViewById(R.id.hikeLength);
        }
    }
}
