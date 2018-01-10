package com.android.sewabarang;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import com.android.sewabarang.Adapter.SlideAdapter;
import com.android.sewabarang.Utility.Helper;
import com.android.sewabarang.Utility.RequestServer;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
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
    private TextView tvHarga,tvUserName,tvUserCreated,tvJudul,tvDeskripsi,tvLokasi,tvDilihat,tvDihubungi,tvDipasang,tvStock,tvLastLogin;
    private ImageView ivUser;

    private String id_iklan,title,no_hp,location;

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
        //final View vSpace = (View) findViewById(R.id.vSpace);
        final NestedScrollView content_detail_iklan = (NestedScrollView) findViewById(R.id.content_detail_iklan);

        title = " ";
        tvHarga = (TextView) findViewById(R.id.tvHarga);
        tvUserName = (TextView) findViewById(R.id.tvUserName);
        tvUserCreated = (TextView) findViewById(R.id.tvUserCreated);
        tvLastLogin = (TextView) findViewById(R.id.tvLastLogin);
        tvJudul = (TextView) findViewById(R.id.tvJudul);
        tvDeskripsi = (TextView) findViewById(R.id.tvDeskripsi);
        tvLokasi = (TextView) findViewById(R.id.tvLokasi);
        tvDilihat = (TextView) findViewById(R.id.tvDilihat);
        tvDihubungi = (TextView) findViewById(R.id.tvDihubungi);
        tvDipasang = (TextView) findViewById(R.id.tvDipasang);
        tvStock = (TextView) findViewById(R.id.tvStock);
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
                    //vSpace.setVisibility(View.GONE);
                    isShow = true;
                } else if(isShow) {
                    collapsingToolbarLayout.setTitle(" ");//carefull there should a space between double quote otherwise it wont work
                    //vSpace.setVisibility(View.VISIBLE);
                    isShow = false;
                }
            }
        });

        final FloatingActionsMenu menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
        menuMultipleActions.setVisibility(View.GONE);
        
        final com.getbase.floatingactionbutton.FloatingActionButton actionA = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_a);
        actionA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = new RequestServer().getServer_url() + "addDihubungi";
                JsonObject jsonReq = new JsonObject();
                jsonReq.addProperty("id_iklan", id_iklan);
                Ion.with(DetailIklanActivity.this)
                        .load(url)
                        .setJsonObjectBody(jsonReq)
                        .asString()
                        .setCallback(new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result) {
                                Log.d("Result",">"+result);
                            }
                        });
                sendSMSMessage(no_hp);
            }
        });

        final com.getbase.floatingactionbutton.FloatingActionButton actionB = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_b);
        actionB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = new RequestServer().getServer_url() + "addDihubungi";
                JsonObject jsonReq = new JsonObject();
                jsonReq.addProperty("id_iklan", id_iklan);
                Ion.with(DetailIklanActivity.this)
                        .load(url)
                        .setJsonObjectBody(jsonReq)
                        .asString()
                        .setCallback(new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result) {
                                Log.d("Result",">"+result);
                            }
                        });
                launchDialer(no_hp);
            }
        });

        CardView cvLokasi = (CardView) findViewById(R.id.cvLokasi);
        cvLokasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMap(location);
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
                        tvUserCreated.setText("Member sejak "+data.get("memberSejak").getAsString());
                        tvLastLogin.setText("Last Login "+data.get("lastLogin").getAsString());
                        tvJudul.setText(data.get("judul").getAsString());
                        tvDeskripsi.setText(data.get("deskripsi").getAsString());
                        tvDipasang.setText(data.get("dipasang").getAsString());
                        tvStock.setText(data.get("stock").getAsString());
                        tvDilihat.setText(data.get("dilihat").getAsString());
                        tvDihubungi.setText(data.get("dihubungi").getAsString());
                        tvLokasi.setText(user.get("address").getAsString());
                        Ion.with(DetailIklanActivity.this)
                                .load(new RequestServer().getImg_url()+"profile/"+user.get("img").getAsString())
                                .withBitmap()
                                .placeholder(R.drawable.guest)
                                .error(R.drawable.guest)
                                .intoImageView(ivUser);
                        no_hp = user.get("phone").getAsString();
                        location = user.get("address").getAsString();
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
                                    Log.d("currentPage",">"+currentPage);
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

    private void launchDialer(String no_hp)
    {
        // No permisison needed
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:"+no_hp));
        startActivity(callIntent);
    }

    private void sendSMSMessage(String no_hp) {
        try {
            Uri uri = Uri.parse("smsto:"+no_hp);
            // No permisison needed
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO, uri);
            // Set the message to be sent
            //smsIntent.putExtra("sms_body", "SMS application launched from stackandroid.com example");
            startActivity(smsIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openMap(String location)
    {
        Uri gmmIntentUri = Uri.parse("geo:-8.4095178,115.18891600000006?q="+location);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    @Override
    public void onBackPressed() {
        finish();
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
