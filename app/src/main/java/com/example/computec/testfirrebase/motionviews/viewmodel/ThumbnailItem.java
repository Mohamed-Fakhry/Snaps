package com.example.computec.testfirrebase.motionviews.viewmodel;

import android.graphics.Bitmap;

import com.zomato.photofilters.imageprocessors.Filter;

/**
 * Created by Mohamed Fakhry on 06/11/2017.
 */

public class ThumbnailItem {

    private Bitmap image;
    private Filter filter;
    private static int number = 0;
    private int num;

    public ThumbnailItem(Bitmap image, Filter filter) {
        this.filter = filter;
        if (filter != null) {
            this.image = filter.processFilter(image);
        } else
            this.image = image;
        num = ++number;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public static int getNumber() {
        return number;
    }

    public static void setNumber(int number) {
        ThumbnailItem.number = number;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return "ThumbnailItem{" +
                "image=" + image +
                ", filter=" + filter +
                ", num=" + num +
                '}';
    }
}
