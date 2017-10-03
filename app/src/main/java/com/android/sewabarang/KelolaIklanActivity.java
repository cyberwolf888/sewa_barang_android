package com.android.sewabarang;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
        progressBar(true);
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
                                }else{
                                    //photo = new RequestServer().getImg_url()+"iklan/"+objData.get("id").getAsString()+"/"+img.get("img").getAsString();
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
                            lvKelolaIklan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    JsonObject objSelected = mData.get(i).getAsJsonObject();
                                    myCustomSnackbar(objSelected);
                                }
                            });
                            progressBar(false);
                        }else{
                            tvKosong.setVisibility(View.VISIBLE);
                            progressBar(false);
                        }

                    }
                });
    }

    public void progressBar(final Boolean show){
        final RelativeLayout layout = (RelativeLayout) findViewById(R.id.progressBar);
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        layout.setVisibility(show ? View.GONE : View.VISIBLE);
        layout.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                layout.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        layout.setVisibility(show ? View.VISIBLE : View.GONE);
        layout.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                layout.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    public void myCustomSnackbar(final JsonObject objSelected)
    {
        // Create the Snackbar
        LinearLayout.LayoutParams objLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final Snackbar snackbar = Snackbar.make(content_kelola_iklan, "", Snackbar.LENGTH_INDEFINITE);
        // Get the Snackbar's layout view
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
        layout.setPadding(0,0,0,0);
        // Hide the text
        TextView textView = (TextView) layout.findViewById(android.support.design.R.id.snackbar_text);
        textView.setVisibility(View.INVISIBLE);

        LayoutInflater mInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        // Inflate our custom view
        View snackView = getLayoutInflater().inflate(R.layout.my_snackbar, null);
        // Configure the view
        TextView textViewOne = (TextView) snackView.findViewById(R.id.txtOne);

        textViewOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Hapus
                snackbar.dismiss();
                progressBar(true);
                String url = new RequestServer().getServer_url() + "deleteIklan";
                JsonObject jsonReq = new JsonObject();
                jsonReq.addProperty("iklan_id", objSelected.get("id").getAsString());
                Ion.with(KelolaIklanActivity.this)
                        .load(url)
                        .setJsonObjectBody(jsonReq)
                        .asString()
                        .setCallback(new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result) {
                                progressBar(false);
                                Log.d("Result",">"+result);
                                loadData();
                            }
                        });
            }
        });

        TextView textViewTwo = (TextView) snackView.findViewById(R.id.txtTwo);
        textViewTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Edit
                snackbar.dismiss();
                Intent a = new Intent(KelolaIklanActivity.this,EditIklanActivity.class);
                a.putExtra("iklan_id",objSelected.get("id").getAsString());
                startActivity(a);
            }
        });

        // Add the view to the Snackbar's layout
        layout.addView(snackView, objLayoutParams);
        // Show the Snackbar
        snackbar.show();
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
