package com.android.sewabarang.Models;

/**
 * Created by Karen on 9/25/2017.
 */

public class Iklan {
    public String id_iklan;
    public String image;
    public String title;
    public String price;
    public String satuan;

    public Iklan(String id_iklan, String image, String title, String price, String satuan)
    {
        this.id_iklan = id_iklan;
        this.image = image;
        this.title = title;
        this.price = price;
        this.satuan = satuan;
    }

}
