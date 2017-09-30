package com.android.sewabarang;

import android.annotation.TargetApi;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.android.sewabarang.Utility.RequestServer;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.body.FilePart;
import com.koushikdutta.async.http.body.Part;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class TambahGambarIklanActivity extends AppCompatActivity {
    private final static int WRITE_EXTERNAL_RESULT = 105;
    private final static int SELECT_PHOTO_1 = 111;
    private final static int SELECT_PHOTO_2 = 222;
    private final static int SELECT_PHOTO_3 = 333;

    private LinearLayout form_tambah_gambar;
    private ProgressBar mProgressBar;
    private String iklan_id, img1, img2, img3;
    private ImageView iv1,iv2,iv3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iklan_id = getIntent().getStringExtra("iklan_id");
        Log.d("iklan_id",">"+iklan_id);
        setContentView(R.layout.activity_tambah_gambar_iklan);

        form_tambah_gambar = (LinearLayout) findViewById(R.id.form_tambah_gambar);
        mProgressBar = (ProgressBar) findViewById(R.id.iklan_progress);
        iv1 = (ImageView) findViewById(R.id.iv1);
        iv2 = (ImageView) findViewById(R.id.iv2);
        iv3 = (ImageView) findViewById(R.id.iv3);

        mayRequestPermission();

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

        Button btnSimpan = (Button) findViewById(R.id.btnSimpan);
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImage();
            }
        });
    }

    private void saveImage(){
        form_tambah_gambar.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        boolean cancel = false;
        Log.d("save image img1",">"+img1);
        if (TextUtils.isEmpty(img1)) {
            Snackbar.make(form_tambah_gambar, "Gambar pertama tidak boleh kosong", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Tutup", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
            cancel = true;
        }

        if(!cancel){
            List<Part> files = new ArrayList();
            if (!TextUtils.isEmpty(img1)) {
                files.add(new FilePart("UploadForm[1]", new File(img1)));
            }
            if (!TextUtils.isEmpty(img2)) {
                Log.d("save image img2",">"+img2);
                files.add(new FilePart("UploadForm[2]", new File(img2)));
            }
            if (!TextUtils.isEmpty(img3)) {
                Log.d("save image img3",">"+img3);
                files.add(new FilePart("UploadForm[3]", new File(img3)));
            }

            String url = new RequestServer().getServer_url()+"saveGambarIklan";
            Log.d("Url",">"+url);

            Ion.with(this)
                    .load(url)
                    .setMultipartParameter("iklan_id", iklan_id)
                    .addMultipartParts(files)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            //Log.d("Result",">"+result);
                            finish();
                        }
                    });
        }else{
            form_tambah_gambar.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
            Snackbar.make(form_tambah_gambar, "Ukuran gambar terlalu besar. Ukuran file maksimal 3 MB.", Snackbar.LENGTH_INDEFINITE)
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
        }
        if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
            Snackbar.make(form_tambah_gambar, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
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
                finish();
            }
        }
    }
}
