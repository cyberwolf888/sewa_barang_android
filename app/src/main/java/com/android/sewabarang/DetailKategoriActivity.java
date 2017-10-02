package com.android.sewabarang;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.sewabarang.Adapter.HomeAdapter;
import com.android.sewabarang.Models.Iklan;
import com.android.sewabarang.Utility.RequestServer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;

public class DetailKategoriActivity extends AppCompatActivity {

    private String id_kategori,label_kategori;
    private GridView mGridView;
    private ProgressBar mProgressBar;

    private HomeAdapter mGridAdapter;
    private List<Iklan> mGridData;
    public JsonArray data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id_kategori = getIntent().getStringExtra("id_kategori");
        label_kategori = getIntent().getStringExtra("label_kategori");
        setContentView(R.layout.activity_detail_kategori);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(label_kategori);

        mGridView = (GridView) findViewById(R.id.gridView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        loadData();
    }

    private void loadData(){
        mProgressBar.setVisibility(View.VISIBLE);
        //mGridView.setVisibility(View.GONE);

        mGridData = new ArrayList<>();
        data = new JsonArray();

        String url = new RequestServer().getServer_url() + "getDetailKategori";
        JsonObject jsonReq = new JsonObject();
        jsonReq.addProperty("id_kategori", id_kategori);

        Log.d("url",">"+url);

        Ion.with(this)
                .load(url)
                .setJsonObjectBody(jsonReq)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        Log.d("result",">"+result);
                        try{
                            data = result.getAsJsonArray("data");
                            for (int i=0; i<data.size(); i++){
                                JsonObject objData = data.get(i).getAsJsonObject();
                                JsonArray imgs = objData.get("gambar_iklan").getAsJsonArray();

                                String photo = "";
                                if(!imgs.isJsonNull()){
                                    JsonObject img = imgs.get(0).getAsJsonObject();
                                    photo = new RequestServer().getImg_url()+"iklan/"+objData.get("id").getAsString()+"/"+img.get("img").getAsString();
                                }
                                //Log.d("photo",">"+photo);

                                mGridData.add(new Iklan(
                                        objData.get("id").getAsString(),
                                        photo,
                                        objData.get("judul").getAsString(),
                                        objData.get("harga").getAsString(),
                                        objData.get("satuan").getAsString()
                                ));

                            }
                            mGridAdapter = new HomeAdapter(DetailKategoriActivity.this, R.layout.grid_item_layout, mGridData);
                            mGridView.setAdapter(mGridAdapter);

                            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                                    //Get item at position
                                    Iklan item = (Iklan) parent.getItemAtPosition(position);
                                    //Iklan item = mGridData.get(position);

                                    Intent i = new Intent(DetailKategoriActivity.this,DetailIklanActivity.class);
                                    i.putExtra("id_iklan",item.id_iklan);
                                    startActivity(i);
                                }
                            });

                            mProgressBar.setVisibility(View.GONE);
                            mGridView.setVisibility(View.VISIBLE);
                        }catch (Exception ex){
                            mProgressBar.setVisibility(View.GONE);
                            Snackbar.make(findViewById(R.id.kategori_detail), "Terjadi kesalahan saaat menyambung ke server.", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Tutup", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                        }
                                    }).show();
                        }

                    }
                });
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
