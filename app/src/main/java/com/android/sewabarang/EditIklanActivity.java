package com.android.sewabarang;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.sewabarang.Utility.RequestServer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.body.FilePart;
import com.koushikdutta.async.http.body.Part;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class EditIklanActivity extends AppCompatActivity {
    private final static int WRITE_EXTERNAL_RESULT = 105;
    private final static int REQ_KATEGORI = 111;
    private final static int REQ_SATUAN = 222;
    private final static int SELECT_PHOTO_1 = 1111;
    private final static int SELECT_PHOTO_2 = 2222;
    private final static int SELECT_PHOTO_3 = 3333;
    private String id_kategori;

    private EditText etJudul,etKategori,etHarga,etStock,etSatuan,etDeskripsi;
    private ImageView iv1,iv2,iv3;
    private Button btnSimpan;
    private String iklan_id, img1, img2, img3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iklan_id = getIntent().getStringExtra("iklan_id");
        setContentView(R.layout.activity_edit_iklan);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mayRequestPermission();
        loadData();

        iv1 = (ImageView) findViewById(R.id.iv1);
        iv2 = (ImageView) findViewById(R.id.iv2);
        iv3 = (ImageView) findViewById(R.id.iv3);

        etJudul = (EditText) findViewById(R.id.etJudul);
        etKategori = (EditText) findViewById(R.id.etKategori);
        etHarga = (EditText) findViewById(R.id.etHarga);
        etStock = (EditText) findViewById(R.id.etStock);
        etSatuan = (EditText) findViewById(R.id.etSatuan);
        etDeskripsi = (EditText) findViewById(R.id.etDeskripsi);
        btnSimpan = (Button) findViewById(R.id.btnSimpan);

        etKategori.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EditIklanActivity.this,ListKategoriActivity.class);
                startActivityForResult(i, REQ_KATEGORI);
            }
        });

        etSatuan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EditIklanActivity.this,ListSatuanHargaActivity.class);
                startActivityForResult(i, REQ_SATUAN);
            }
        });

        iv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO_1);
            }
        });

        iv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO_2);
            }
        });

        iv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO_3);
            }
        });

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
    }

    private void loadData(){
        String url = new RequestServer().getServer_url() + "detailIklan";
        JsonObject jsonReq = new JsonObject();
        jsonReq.addProperty("id_iklan", iklan_id);

        Log.d("url",">"+url);

        Ion.with(EditIklanActivity.this)
                .load(url)
                .setJsonObjectBody(jsonReq)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        JsonObject data = result.get("data").getAsJsonObject();
                        final JsonArray imgs = data.get("gambar_iklan").getAsJsonArray();
                        Log.d("data",">"+data);

                        id_kategori = data.get("kategori").getAsJsonObject().get("id").getAsString();
                        etJudul.setText(data.get("judul").getAsString());
                        etKategori.setText(data.get("kategori").getAsJsonObject().get("name").getAsString());
                        etHarga.setText(data.get("harga").getAsString());
                        etStock.setText(data.get("stock").getAsString());
                        etSatuan.setText(data.get("satuan").getAsString());
                        etDeskripsi.setText(data.get("deskripsi").getAsString());

                        if(!imgs.isJsonNull()){
                            if(imgs.size() >= 1){
                                Ion.with(EditIklanActivity.this)
                                        .load(new RequestServer().getImg_url()+"iklan/"+data.get("id").getAsString()+"/"+imgs.get(0).getAsJsonObject().get("img").getAsString())
                                        .withBitmap()
                                        .placeholder(R.drawable.noimage)
                                        .error(R.drawable.noimage)
                                        .intoImageView(iv1);
                                final String id = imgs.get(0).getAsJsonObject().get("id").getAsString();
                                iv1.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        myCustomSnackbar(SELECT_PHOTO_1, id);
                                    }
                                });
                            }
                            if(imgs.size() >= 2){
                                Ion.with(EditIklanActivity.this)
                                        .load(new RequestServer().getImg_url()+"iklan/"+data.get("id").getAsString()+"/"+imgs.get(1).getAsJsonObject().get("img").getAsString())
                                        .withBitmap()
                                        .placeholder(R.drawable.noimage)
                                        .error(R.drawable.noimage)
                                        .intoImageView(iv2);
                                final String id = imgs.get(1).getAsJsonObject().get("id").getAsString();
                                iv2.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        myCustomSnackbar(SELECT_PHOTO_2, id);
                                    }
                                });
                            }
                            if(imgs.size() >= 3){
                                Ion.with(EditIklanActivity.this)
                                        .load(new RequestServer().getImg_url()+"iklan/"+data.get("id").getAsString()+"/"+imgs.get(2).getAsJsonObject().get("img").getAsString())
                                        .withBitmap()
                                        .placeholder(R.drawable.noimage)
                                        .error(R.drawable.noimage)
                                        .intoImageView(iv3);
                                final String id = imgs.get(2).getAsJsonObject().get("id").getAsString();
                                iv3.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        myCustomSnackbar(SELECT_PHOTO_3, id);
                                    }
                                });
                            }
                        }
                    }
                });
    }

    private void saveData(){
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

        List<Part> files = new ArrayList();
        if (!TextUtils.isEmpty(img1)) {
            try{
                files.add(new FilePart("UploadForm[1]", new File(img1)));
            }catch (Exception e){

            }
        }
        if (!TextUtils.isEmpty(img2)) {
            Log.d("save image img2",">"+img2);
            try{
                files.add(new FilePart("UploadForm[2]", new File(img2)));
            }catch (Exception e){

            }
        }
        if (!TextUtils.isEmpty(img3)) {
            Log.d("save image img3",">"+img3);
            try{
                files.add(new FilePart("UploadForm[3]", new File(img3)));
            }catch (Exception e){

            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            String url = new RequestServer().getServer_url()+"updateIklan";
            Log.d("Url",">"+url);

            JsonObject jsonReq = new JsonObject();
            jsonReq.addProperty("id", iklan_id);
            jsonReq.addProperty("judul", judul);
            jsonReq.addProperty("category_id", id_kategori);
            jsonReq.addProperty("harga", harga);
            jsonReq.addProperty("stock", stock);
            jsonReq.addProperty("satuan", satuan);
            jsonReq.addProperty("deskripsi", deskripsi);

            Ion.with(this)
                    .load(url)
                    .setMultipartParameter("id", iklan_id)
                    .setMultipartParameter("judul", judul)
                    .setMultipartParameter("category_id", id_kategori)
                    .setMultipartParameter("harga", harga)
                    .setMultipartParameter("stock", stock)
                    .setMultipartParameter("satuan", satuan)
                    .setMultipartParameter("deskripsi", deskripsi)
                    .addMultipartParts(files)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            Log.d("Result",">"+result);
                            if(!result.equals("")){
                                Snackbar.make(findViewById(R.id.edit_iklan_form), result, Snackbar.LENGTH_INDEFINITE)
                                        .setAction("Tutup", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                                                photoPickerIntent.setType("image/*");
                                                startActivityForResult(photoPickerIntent, SELECT_PHOTO_1);
                                            }
                                        }).show();
                            }else{
                                finish();
                            }
                        }
                    });

        }
    }

    public void myCustomSnackbar(final int request_code, final String img_id)
    {
        // Create the Snackbar
        LinearLayout.LayoutParams objLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final Snackbar snackbar = Snackbar.make(findViewById(R.id.edit_iklan_form), "", Snackbar.LENGTH_INDEFINITE);
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
                String url = new RequestServer().getServer_url() + "deleteImg";
                JsonObject jsonReq = new JsonObject();
                jsonReq.addProperty("id", img_id);
                Ion.with(EditIklanActivity.this)
                        .load(url)
                        .setJsonObjectBody(jsonReq)
                        .asString()
                        .setCallback(new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result) {

                            }
                        });
                switch (request_code){
                    case SELECT_PHOTO_1:
                        iv1.setImageResource(R.drawable.ic_add_img);
                        img1=null;
                        iv1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                                photoPickerIntent.setType("image/*");
                                startActivityForResult(photoPickerIntent, SELECT_PHOTO_1);
                            }
                        });
                        break;
                    case SELECT_PHOTO_2:
                        iv2.setImageResource(R.drawable.ic_add_img);
                        img2=null;
                        iv2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                                photoPickerIntent.setType("image/*");
                                startActivityForResult(photoPickerIntent, SELECT_PHOTO_2);
                            }
                        });
                        break;
                    case SELECT_PHOTO_3:
                        iv3.setImageResource(R.drawable.ic_add_img);
                        img3=null;
                        iv3.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                                photoPickerIntent.setType("image/*");
                                startActivityForResult(photoPickerIntent, SELECT_PHOTO_3);
                            }
                        });
                        break;
                }

            }
        });

        TextView textViewTwo = (TextView) snackView.findViewById(R.id.txtTwo);
        textViewTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Edit
                snackbar.dismiss();
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, request_code);

            }
        });

        // Add the view to the Snackbar's layout
        layout.addView(snackView, objLayoutParams);
        // Show the Snackbar
        snackbar.show();
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
        if (requestCode == SELECT_PHOTO_1 && resultCode == RESULT_OK && data != null) {
            setImage(iv1,data,SELECT_PHOTO_1);
        }

        if (requestCode == SELECT_PHOTO_2 && resultCode == RESULT_OK && data != null) {
            setImage(iv2,data,SELECT_PHOTO_2);
        }

        if (requestCode == SELECT_PHOTO_3 && resultCode == RESULT_OK && data != null) {
            setImage(iv3,data,SELECT_PHOTO_3);
        }

    }

    private void setImage(ImageView img, Intent data, int code){
        String imagePath;
        Uri pickedImage = data.getData();
        String[] filePath = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
        cursor.moveToFirst();
        imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
        //Cek file size
        File file = new File(imagePath);
        int file_size = Integer.parseInt(String.valueOf(file.length()/1024));
        Log.d("File Size",">"+file_size);
        if(file_size>(3*1024)){
            imagePath = "";
            Snackbar.make(findViewById(R.id.edit_iklan_form), "Ukuran gambar terlalu besar. Ukuran file maksimal 3 MB.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Tutup", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
        }else{
                    /*BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);*/
            img.setImageBitmap(decodeSampledBitmapFromResource(imagePath, 300, 300));
        }
        Log.d("code",">"+code);
        switch (code){
            case SELECT_PHOTO_1:
                img1=imagePath;break;
            case SELECT_PHOTO_2:
                img2=imagePath;break;
            case SELECT_PHOTO_3:
                img3=imagePath;break;
        }
        cursor.close();
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(String res, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(res, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(res, options);
    }

    private boolean mayRequestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }else{
            requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, WRITE_EXTERNAL_RESULT);
        }
        if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
            Snackbar.make(findViewById(R.id.edit_iklan_form), R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, WRITE_EXTERNAL_RESULT);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, WRITE_EXTERNAL_RESULT);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_RESULT) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission diterima
            }else{
                //permission ditolak
                mayRequestPermission();
            }
        }
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
