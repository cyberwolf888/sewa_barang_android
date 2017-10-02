package com.android.sewabarang;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.sewabarang.Utility.Helper;
import com.android.sewabarang.Utility.RequestServer;
import com.android.sewabarang.Utility.Session;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.HashMap;

public class KelolaIklanActivity extends AppCompatActivity {

    private LinearLayout content_kelola_iklan;
    private ListView lvKelolaIklan;
    private TextView tvKosong;
    private Session session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new Session(this);
        setContentView(R.layout.activity_kelola_iklan);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        content_kelola_iklan = (LinearLayout) findViewById(R.id.content_kelola_iklan);
        lvKelolaIklan = (ListView) findViewById(R.id.lvKelolaIklan);
        tvKosong = (TextView) findViewById(R.id.tvKosong);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(KelolaIklanActivity.this, TambahIklanActivity.class);
                startActivity(i);
            }
        });
    }

    private void loadData(){
        String url = new RequestServer().getServer_url() + "getIklan";
        JsonObject jsonReq = new JsonObject();
        jsonReq.addProperty("user_id", session.getUserId());

        Log.d("url",">"+url);

        Ion.with(this)
                .load(url)
                .setJsonObjectBody(jsonReq)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        final JsonArray mData = result.getAsJsonArray("data");
                        if(!mData.isJsonNull()){
                            tvKosong.setVisibility(View.GONE);
                            final ArrayList<HashMap<String, String>> xitemList = new ArrayList<HashMap<String, String>>();
                            for(int i=0; i<mData.size(); i++){
                                JsonObject objData = mData.get(i).getAsJsonObject();
                                String status = "";
                                if(objData.get("status").getAsString().equals("1")){
                                    status = "Menunggu Verifikasi";
                                } else if(objData.get("status").getAsString().equals("2")){
                                    status = "Aktif";
                                } else if(objData.get("status").getAsString().equals("0")){
                                    status = "Tidak Aktif";
                                }
                                JsonArray imgs = objData.get("gambar_iklan").getAsJsonArray();

                                String photo = "";
                                if(!imgs.isJsonNull()){
                                    JsonObject img = imgs.get(0).getAsJsonObject();
                                    photo = new RequestServer().getImg_url()+"iklan/"+objData.get("id").getAsString()+"/"+img.get("img").getAsString();
                                }

                                HashMap<String, String> dataList = new HashMap<String, String>();
                                dataList.put("id",objData.get("id").getAsString());
                                dataList.put("judul",objData.get("judul").getAsString());
                                dataList.put("harga",new Helper().formatNumber(Integer.valueOf(objData.get("harga").getAsString()))+"/"+objData.get("satuan").getAsString());
                                dataList.put("status",status);
                                dataList.put("img",photo);

                                xitemList.add(dataList);
                            }
                            ListAdapter adapter = new SimpleAdapter(
                                    KelolaIklanActivity.this,
                                    xitemList,
                                    R.layout.list_kelola_iklan,
                                    new String[]{"judul","harga","status"},
                                    new int[]{R.id.tvJudul,R.id.tvHarga,R.id.tvStatus}
                            ){
                                @Override
                                public View getView (int position, View convertView, ViewGroup parent) {
                                    View view = super.getView(position, convertView, parent);

                                    HashMap<String, String> data = xitemList.get(position);
                                    ImageView ivIklan = (ImageView) view.findViewById(R.id.ivIklan);

                                    Ion.with(KelolaIklanActivity.this)
                                            .load(data.get("img"))
                                            .withBitmap()
                                            .placeholder(R.drawable.noimage)
                                            .error(R.drawable.noimage)
                                            .intoImageView(ivIklan);

                                    return view;
                                }
                            };
                            lvKelolaIklan.setAdapter(adapter);
                        }else{
                            tvKosong.setVisibility(View.VISIBLE);
                        }

                    }
                });
    }

    @Override
    public void onResume(){
        super.onResume();
        loadData();
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
