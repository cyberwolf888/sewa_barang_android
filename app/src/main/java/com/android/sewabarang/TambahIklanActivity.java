package com.android.sewabarang;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.sewabarang.Utility.RequestServer;
import com.android.sewabarang.Utility.Session;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class TambahIklanActivity extends AppCompatActivity {
    private final static int REQ_KATEGORI = 111;
    private final static int REQ_SATUAN = 222;
    private String id_kategori;

    private EditText etJudul,etKategori,etHarga,etStock,etSatuan,etDeskripsi;
    private Button btnNext;

    Session session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new Session(this);
        setContentView(R.layout.activity_tambah_iklan);

        etJudul = (EditText) findViewById(R.id.etJudul);
        etKategori = (EditText) findViewById(R.id.etKategori);
        etHarga = (EditText) findViewById(R.id.etHarga);
        etStock = (EditText) findViewById(R.id.etStock);
        etSatuan = (EditText) findViewById(R.id.etSatuan);
        etDeskripsi = (EditText) findViewById(R.id.etDeskripsi);
        btnNext = (Button) findViewById(R.id.btnNext);

        etKategori.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(TambahIklanActivity.this,ListKategoriActivity.class);
                startActivityForResult(i, REQ_KATEGORI);
            }
        });

        etSatuan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(TambahIklanActivity.this,ListSatuanHargaActivity.class);
                startActivityForResult(i, REQ_SATUAN);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next();
            }
        });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Batal")
                .setMessage("Data iklan anda akan hilang jika keluar saat ini")
                .setPositiveButton("Iya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("Tidak",null)
                .show();
    }

    private void next(){
        // Reset errors.
        etJudul.setError(null);
        etKategori.setError(null);
        etHarga.setError(null);
        etStock.setError(null);
        etSatuan.setError(null);
        etDeskripsi.setError(null);

        // Store values at the time of the login attempt.
        String judul = etJudul.getText().toString();
        String kategori = etKategori.getText().toString();
        String harga = etHarga.getText().toString();
        String stock = etStock.getText().toString();
        String satuan = etSatuan.getText().toString();
        String deskripsi = etDeskripsi.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(judul)) {
            etJudul.setError("Judul tidak boleh kosong");
            focusView = etJudul;
            cancel = true;
        }
        if (TextUtils.isEmpty(kategori)) {
            etKategori.setError("Kategori tidak boleh kosong");
            focusView = etKategori;
            cancel = true;
        }
        if (TextUtils.isEmpty(harga)) {
            etHarga.setError("Harga tidak boleh kosong");
            focusView = etHarga;
            cancel = true;
        }
        if (TextUtils.isEmpty(stock)) {
            etStock.setError("Stock tidak boleh kosong");
            focusView = etStock;
            cancel = true;
        }
        if (TextUtils.isEmpty(satuan)) {
            etSatuan.setError("Satuan Harga tidak boleh kosong");
            focusView = etSatuan;
            cancel = true;
        }
        if (TextUtils.isEmpty(deskripsi)) {
            etDeskripsi.setError("Deskripsi tidak boleh kosong");
            focusView = etDeskripsi;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            String url = new RequestServer().getServer_url()+"saveIklan";
            Log.d("Url",">"+url);

            JsonObject jsonReq = new JsonObject();
            jsonReq.addProperty("user_id", session.getUserId());
            jsonReq.addProperty("judul", judul);
            jsonReq.addProperty("category_id", id_kategori);
            jsonReq.addProperty("harga", harga);
            jsonReq.addProperty("stock", stock);
            jsonReq.addProperty("satuan", satuan);
            jsonReq.addProperty("deskripsi", deskripsi);

            Ion.with(this)
                    .load(url)
                    .setJsonObjectBody(jsonReq)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            Intent i = new Intent(TambahIklanActivity.this, TambahGambarIklanActivity.class);
                            i.putExtra("iklan_id",result.get("id").getAsString());
                            startActivity(i);
                            finish();
                        }
                    });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_KATEGORI && resultCode == RESULT_OK && data != null) {
            id_kategori = data.getStringExtra("id_kategori");
            etKategori.setText(data.getStringExtra("label_kategori"));
        }
        if (requestCode == REQ_SATUAN && resultCode == RESULT_OK && data != null) {
            String satuan = data.getStringExtra("satuan");
            etSatuan.setText(satuan);
        }
    }
}
