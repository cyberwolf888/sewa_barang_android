package com.android.sewabarang;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.sewabarang.Utility.RequestServer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.HashMap;

public class KategoriActivity extends AppCompatActivity {
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kategori);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = (ListView) findViewById(R.id.tvKategori);

        String url = new RequestServer().getServer_url()+"getCategory";
        JsonObject jsonReq = new JsonObject();
        jsonReq.addProperty("id", true);

        Log.d("Cek Req",">"+jsonReq);
        Log.d("Url",">"+url);
        Ion.with(this)
                .load(url)
                .setJsonObjectBody(jsonReq)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        final JsonArray mData = result.getAsJsonArray("data");
                        ArrayList<HashMap<String, String>> xitemList = new ArrayList<HashMap<String, String>>();
                        for(int i=0; i<mData.size(); i++){
                            JsonObject objData = mData.get(i).getAsJsonObject();
                            HashMap<String, String> dataList = new HashMap<String, String>();
                            dataList.put("id",objData.get("id").getAsString());
                            dataList.put("name",objData.get("name").getAsString());
                            xitemList.add(dataList);
                        }
                        ListAdapter adapter = new SimpleAdapter(
                                KategoriActivity.this,
                                xitemList,
                                R.layout.list_kategori,
                                new String[]{"name"},
                                new int[]{R.id.tvKategori}
                        );
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                JsonObject objSelected = mData.get(i).getAsJsonObject();
                                Intent output = new Intent(KategoriActivity.this,DetailKategoriActivity.class);
                                output.putExtra("id_kategori", objSelected.get("id").getAsString());
                                output.putExtra("label_kategori", objSelected.get("name").getAsString());
                                startActivity(output);

                            }
                        });
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
