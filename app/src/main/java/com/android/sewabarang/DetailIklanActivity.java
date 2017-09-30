package com.android.sewabarang;

import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import com.android.sewabarang.Adapter.SlideAdapter;
import com.android.sewabarang.Utility.Helper;
import com.android.sewabarang.Utility.RequestServer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator;

public class DetailIklanActivity extends AppCompatActivity {
    private static ViewPager mPager;
    private static int currentPage = 0;
    private ArrayList<String> imgsarray = new ArrayList<String>();
    private TextView tvHarga,tvUserName,tvUserCreated,tvJudul,tvDeskripsi;
    private ImageView ivUser;

    private String id_iklan,title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id_iklan = getIntent().getStringExtra("id_iklan");
        setContentView(R.layout.activity_detail_iklan);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        collapsingToolbarLayout.setTitle(" ");
        final FrameLayout frUser = (FrameLayout) findViewById(R.id.frUser);

        title = " ";
        tvHarga = (TextView) findViewById(R.id.tvHarga);
        tvUserName = (TextView) findViewById(R.id.tvUserName);
        tvUserCreated = (TextView) findViewById(R.id.tvUserCreated);
        tvJudul = (TextView) findViewById(R.id.tvJudul);
        tvDeskripsi = (TextView) findViewById(R.id.tvDeskripsi);
        ivUser = (ImageView) findViewById(R.id.ivUser);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(title);
                    frUser.setVisibility(View.GONE);
                    isShow = true;
                } else if(isShow) {
                    collapsingToolbarLayout.setTitle(" ");//carefull there should a space between double quote otherwise it wont work
                    frUser.setVisibility(View.VISIBLE);
                    isShow = false;
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mPager = (ViewPager) findViewById(R.id.pager);
        loadData();
    }

    private void loadData(){
        String url = new RequestServer().getServer_url() + "detailIklan";
        JsonObject jsonReq = new JsonObject();
        jsonReq.addProperty("id_iklan", id_iklan);

        Log.d("url",">"+url);

        Ion.with(DetailIklanActivity.this)
                .load(url)
                .setJsonObjectBody(jsonReq)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        JsonObject data = result.get("data").getAsJsonObject();
                        final JsonArray imgs = data.get("gambar_iklan").getAsJsonArray();
                        Log.d("data",">"+data);
                        title = data.get("judul").getAsString();
                        JsonObject user = data.get("user").getAsJsonObject();
                        tvHarga.setText(new Helper().formatNumber(Integer.valueOf(data.get("harga").getAsString()))+"/"+data.get("satuan").getAsString());
                        tvUserName.setText(user.get("name").getAsString());
                        tvUserCreated.setText("Member sejak "+user.get("created_at").getAsString());
                        tvJudul.setText(data.get("judul").getAsString());
                        tvDeskripsi.setText(data.get("deskripsi").getAsString());
                        Ion.with(DetailIklanActivity.this)
                                .load(new RequestServer().getImg_url()+"profile/"+user.get("img").getAsString())
                                .withBitmap()
                                .placeholder(R.drawable.guest)
                                .error(R.drawable.guest)
                                .intoImageView(ivUser);
                        if(!imgs.isJsonNull()){
                            for (int i=0; i<imgs.size(); i++){
                                JsonObject img = imgs.get(i).getAsJsonObject();
                                imgsarray.add(new RequestServer().getImg_url()+"iklan/"+data.get("id").getAsString()+"/"+img.get("img").getAsString());
                            }
                            mPager.setAdapter(new SlideAdapter(DetailIklanActivity.this,imgsarray));
                            CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
                            indicator.setViewPager(mPager);
                            // Auto start of viewpager
                            final Handler handler = new Handler();
                            final Runnable Update = new Runnable() {
                                public void run() {
                                    if (currentPage == imgs.size()) {
                                        currentPage = 0;
                                    }
                                    mPager.setCurrentItem(currentPage++, true);
                                }
                            };
                            Timer swipeTimer = new Timer();
                            swipeTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    handler.post(Update);
                                }
                            }, 5000, 5000);
                        }
                    }
                });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
