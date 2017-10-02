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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.android.sewabarang.Utility.RequestServer;
import com.android.sewabarang.Utility.Session;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.File;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class ProfileActivity extends AppCompatActivity {
    private final static int WRITE_EXTERNAL_RESULT = 105;
    private final static int SELECT_PHOTO = 12345;
    Session session;

    private EditText etName,etEmail,etPassword,etPhone,etAddress;
    private ImageView imgView;
    private Button btnSave;
    private ProgressBar mProgressBar;
    private LinearLayout profile_form;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new Session(this);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profile_form = (LinearLayout) findViewById(R.id.profile_form);
        imgView = (ImageView) findViewById(R.id.imageView);
        etName = (EditText) findViewById(R.id.etName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etPhone = (EditText) findViewById(R.id.etPhone);
        etAddress = (EditText) findViewById(R.id.etAddress);
        btnSave = (Button) findViewById(R.id.btnSave);
        mProgressBar = (ProgressBar) findViewById(R.id.register_progress);

        mayRequestPermission();
        loadData();

        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                simpanProfile();
            }
        });
    }

    private void loadData(){
        mProgressBar.setVisibility(View.VISIBLE);
        profile_form.setVisibility(View.GONE);

        String url = new RequestServer().getServer_url()+"edit_account";
        Log.d("Url",">"+url);

        JsonObject jsonReq = new JsonObject();
        jsonReq.addProperty("id", session.getUserId());

        Ion.with(this)
                .load(url)
                .setJsonObjectBody(jsonReq)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        try{
                            JsonObject data = result.getAsJsonObject("data");
                            etName.setText(data.get("name").getAsString());
                            etEmail.setText(data.get("email").getAsString());
                            etPhone.setText(data.get("phone").getAsString());
                            etAddress.setText(data.get("address").getAsString());

                            if (!data.get("img").isJsonNull()) {
                                Ion.with(ProfileActivity.this)
                                        .load(new RequestServer().getImg_url()+"profile/"+data.get("img").getAsString())
                                        .withBitmap()
                                        .placeholder(R.drawable.guest)
                                        .error(R.drawable.guest)
                                        .intoImageView(imgView);
                            }
                            mProgressBar.setVisibility(View.GONE);
                            profile_form.setVisibility(View.VISIBLE);
                        }catch (Exception ex){
                            mProgressBar.setVisibility(View.GONE);
                            profile_form.setVisibility(View.VISIBLE);
                            Snackbar.make(profile_form, "Terjadi kesalahan saat menyambung ke server.", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Tutup", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                        }
                                    }).show();
                        }
                    }
                });
    }

    private void simpanProfile(){
        // Reset errors.
        etName.setError(null);
        etEmail.setError(null);
        etPassword.setError(null);
        etPhone.setError(null);
        etAddress.setError(null);

        // Store values at the time of the login attempt.
        final String name = etName.getText().toString();
        final String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String phone = etPhone.getText().toString();
        String address = etAddress.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name)) {
            etEmail.setError("Nama lengkap tidak boleh kosong");
            focusView = etName;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email tidak boleh kosong");
            focusView = etEmail;
            cancel = true;
        } else if (!email.contains("@")) {
            etEmail.setError("Format email salah");
            focusView = etEmail;
            cancel = true;
        }

        if (!TextUtils.isEmpty(password)) {
            if (password.length() < 6) {
                etPassword.setError("Password tidak boleh kurang dari 6 karakter");
                focusView = etPassword;
                cancel = true;
            }
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("No telephone tidak boleh kosong");
            focusView = etPhone;
            cancel = true;
        }

        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Alamat tidak boleh kosong");
            focusView = etAddress;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            mProgressBar.setVisibility(View.VISIBLE);
            profile_form.setVisibility(View.GONE);

            String url = new RequestServer().getServer_url()+"update_account";
            Log.d("Url",">"+url);

            JsonObject jsonReq = new JsonObject();
            jsonReq.addProperty("name", name);
            jsonReq.addProperty("email", email);
            jsonReq.addProperty("phone", phone);
            jsonReq.addProperty("address", address);
            jsonReq.addProperty("password", password);

            if(TextUtils.isEmpty(imagePath)){
                Ion.with(this)
                        .load(url)
                        .setMultipartParameter("id", session.getUserId())
                        .setMultipartParameter("name", name)
                        .setMultipartParameter("email", email)
                        .setMultipartParameter("phone", phone)
                        .setMultipartParameter("address", address)
                        .setMultipartParameter("password", password)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                try{
                                    session.createLoginSession(session.getUserId(), name, result.get("img").getAsString(), email);
                                    Intent i = getBaseContext().getPackageManager().
                                            getLaunchIntentForPackage(getBaseContext().getPackageName());
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(i);
                                }catch (Exception ex){
                                    mProgressBar.setVisibility(View.GONE);
                                    profile_form.setVisibility(View.VISIBLE);
                                    Snackbar.make(profile_form, "Terjadi kesalahan saat menyambung ke server.", Snackbar.LENGTH_INDEFINITE)
                                            .setAction("Tutup", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {

                                                }
                                            }).show();
                                }

                            }
                        });
            }else{
                Ion.with(this)
                        .load(url)
                        .setMultipartParameter("id", session.getUserId())
                        .setMultipartParameter("name", name)
                        .setMultipartParameter("email", email)
                        .setMultipartParameter("phone", phone)
                        .setMultipartParameter("address", address)
                        .setMultipartParameter("password", password)
                        .setMultipartFile("image", "application/images", new File(imagePath))
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                try {
                                    session.createLoginSession(session.getUserId(), name, result.get("img").getAsString(), email);
                                    Intent i = getBaseContext().getPackageManager().
                                            getLaunchIntentForPackage(getBaseContext().getPackageName());
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(i);
                                }catch (Exception ex){
                                    mProgressBar.setVisibility(View.GONE);
                                    profile_form.setVisibility(View.VISIBLE);
                                    Snackbar.make(profile_form, "Terjadi kesalahan saat menyambung ke server.", Snackbar.LENGTH_INDEFINITE)
                                            .setAction("Tutup", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {

                                                }
                                            }).show();
                                }
                            }
                        });
            }

        }
    }

    private boolean mayRequestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
            Snackbar.make(profile_form, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data != null) {
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
                Snackbar.make(profile_form, "Ukuran gambar terlalu besar. Ukuran file maksimal 3 MB.", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Tutup", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        }).show();
            }else{
                    /*BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);*/
                imgView.setImageBitmap(decodeSampledBitmapFromResource(imagePath, 100, 100));
            }

            cursor.close();
        }
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
