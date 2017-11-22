package com.example.computec.testfirrebase.motionviews.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.computec.testfirrebase.R;
import com.example.computec.testfirrebase.motionviews.ui.ThumbnailCallback;
import com.example.computec.testfirrebase.motionviews.viewmodel.ThumbnailItem;

import java.util.ArrayList;


/**
 * Created by Mohamed Fakhry on 06/11/2017
 */

public class ThumbnailsAdapter extends RecyclerView.Adapter<ThumbnailVH> {

    private ArrayList<ThumbnailItem> thumbnailItems = new ArrayList<>();
    private ThumbnailCallback thumbnailCallback;

    public ThumbnailsAdapter(ArrayList<ThumbnailItem> thumbnailItems
            , ThumbnailCallback thumbnailCallback) {
        this.thumbnailItems = thumbnailItems;
        this.thumbnailCallback = thumbnailCallback;
    }

    @Override
    public ThumbnailVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ThumbnailVH(LayoutInflater
                .from(parent.getContext()).inflate(R.layout.item_thumbnail, null));
    }

    @Override
    public void onBindViewHolder(ThumbnailVH holder, int position) {
        holder.onBind(thumbnailItems.get(position), thumbnailCallback);
    }

    @Override
    public int getItemCount() {
        return thumbnailItems.size();
    }
}
