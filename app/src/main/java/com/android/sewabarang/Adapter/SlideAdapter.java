package com.android.sewabarang.Adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.sewabarang.R;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

/**
 * Created by Karen on 9/29/2017.
 */

public class SlideAdapter extends PagerAdapter {
    private ArrayList<String> images;
    private LayoutInflater inflater;
    private Context context;

    public SlideAdapter(Context context, ArrayList<String> images) {
        this.context = context;
        this.images=images;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        View myImageLayout = inflater.inflate(R.layout.slide, view, false);
        ImageView myImage = (ImageView) myImageLayout
                .findViewById(R.id.image);
        //myImage.setImageResource(images.get(position));
        Ion.with(context)
                .load(images.get(position))
                .withBitmap()
                .placeholder(R.drawable.noimage)
                .error(R.drawable.noimage)
                .intoImageView(myImage);
        view.addView(myImageLayout, 0);
        return myImageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }
}
