package com.example.cameralist;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by JesusManuel on 07/03/16.
 */
public class PicturesAdapter extends RecyclerView.Adapter<PicturesAdapter.PicturesViewHolder> {

    private Context context;
    private List<Uri> picturesPaths;

    public PicturesAdapter(Context context, List<Uri> picturesPaths) {
        this.context = context;
        this.picturesPaths = picturesPaths;
    }

    @Override
    public PicturesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.image_layout_item, parent, false);
        return new PicturesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PicturesViewHolder holder, int position) {
        Picasso.with(context).load(picturesPaths.get(position)).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return picturesPaths.size();
    }

    public static class PicturesViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public PicturesViewHolder(View itemView) {
            super(itemView);
            this.imageView = (ImageView) itemView.findViewById(R.id.imageView);
        }
    }
}
