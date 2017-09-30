package com.android.sewabarang.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.sewabarang.Models.Iklan;
import com.android.sewabarang.R;
import com.android.sewabarang.Utility.Helper;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Karen on 9/25/2017.
 */

public class HomeAdapter extends ArrayAdapter<Iklan> {
    //private final ColorMatrixColorFilter grayscaleFilter;
    private Context mContext;
    private int layoutResourceId;
    private List<Iklan> listItems;

    public HomeAdapter(Context mContext, int layoutResourceId, List<Iklan> listItems) {
        super(mContext, layoutResourceId, listItems);
        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.listItems = listItems;
    }


    /**
     * Updates grid data and refresh grid items.
     *
     * @param listItems
     */
    public void setGridData(List<Iklan> listItems) {
        this.listItems = listItems;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;
        Helper helper = new Helper();

        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) row.findViewById(R.id.grid_item_title);
            holder.price = (TextView) row.findViewById(R.id.grid_item_price);
            holder.imageView = (ImageView) row.findViewById(R.id.grid_item_image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        Iklan item = listItems.get(position);
        holder.title.setText(item.title);
        holder.price.setText(helper.formatNumber(Integer.valueOf(item.price))+"/"+item.satuan);

        if(item.image.equals("")){
            holder.imageView.setImageResource(R.drawable.noimage);
        }else{
            Ion.with(mContext)
                    .load(item.image)
                    .withBitmap()
                    .placeholder(R.drawable.noimage)
                    .error(R.drawable.noimage)
                    .intoImageView(holder.imageView);
        }
        return row;
    }

    static class ViewHolder {
        TextView title;
        TextView price;
        ImageView imageView;
    }
}
