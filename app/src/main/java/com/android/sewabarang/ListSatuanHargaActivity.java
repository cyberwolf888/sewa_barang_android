package com.android.sewabarang;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class ListSatuanHargaActivity extends AppCompatActivity {
    ListView listView;
    String[] values = new String[] { "Hari", "Jam" };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_satuan_harga);
        listView = (ListView) findViewById(R.id.tvSatuanHarga);

        final ArrayList<HashMap<String, String>> xitemList = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < values.length; ++i) {
            HashMap<String, String> dataList = new HashMap<String, String>();
            dataList.put("satuan",values[i]);
            xitemList.add(dataList);
        }

        ListAdapter adapter = new SimpleAdapter(
                this,
                xitemList,
                R.layout.list_satuan_harga,
                new String[]{"satuan"},
                new int[]{R.id.tvSatuan}
        );

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent output = new Intent();
                output.putExtra("satuan", values[i]);
                setResult(RESULT_OK, output);
                finish();
            }
        });
    }
}
