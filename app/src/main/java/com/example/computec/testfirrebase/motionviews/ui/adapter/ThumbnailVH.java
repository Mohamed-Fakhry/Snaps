package com.example.computec.testfirrebase.motionviews.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.computec.testfirrebase.R;
import com.example.computec.testfirrebase.motionviews.ui.ThumbnailCallback;
import com.example.computec.testfirrebase.motionviews.viewmodel.ThumbnailItem;


/**
 * Created by Mohamed Fakhry on 06/11/2017
 */

class ThumbnailVH extends RecyclerView.ViewHolder {

    private ImageView imageView;

    ThumbnailVH(View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.filterIV);
    }

    void onBind(final ThumbnailItem thumbnailItem, final ThumbnailCallback thumbnailCallback) {

        if (thumbnailItem.getFilter() != null){
            Log.d("test VH with filer", thumbnailItem.toString());
            imageView.setImageBitmap(thumbnailItem.getImage());
        } else {
            Log.d("test VH without filter", thumbnailItem.toString());
            imageView.setImageBitmap(thumbnailItem.getImage());
        }

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thumbnailCallback.onThumbnailClick(thumbnailItem.getImage());
            }
        });
    }
}
