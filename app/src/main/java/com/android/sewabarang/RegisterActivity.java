package com.android.sewabarang;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.android.sewabarang.Utility.RequestServer;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName,etEmail,etPassword,etPhone,etAddress;
    private Button btnRegister;
    private ProgressBar mProgressBar;
    private LinearLayout register_form;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etName = (EditText) findViewById(R.id.etName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etPhone = (EditText) findViewById(R.id.etPhone);
        etAddress = (EditText) findViewById(R.id.etAddress);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        mProgressBar = (ProgressBar) findViewById(R.id.register_progress);
        register_form = (LinearLayout) findViewById(R.id.register_form);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attempRegister();
            }
        });
    }

    private void attempRegister(){
        // Reset errors.
        etName.setError(null);
        etEmail.setError(null);
        etPassword.setError(null);
        etPhone.setError(null);
        etAddress.setError(null);

        // Store values at the time of the login attempt.
        String name = etName.getText().toString();
        String email = etEmail.getText().toString();
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
        } else if (!isEmailValid(email)) {
            etEmail.setError("Format email salah");
            focusView = etEmail;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password tidak boleh kosong");
            focusView = etPassword;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            etPassword.setError("Password tidak boleh kurang dari 6 karakter");
            focusView = etPassword;
            cancel = true;
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
            register_form.setVisibility(View.GONE);

            String url = new RequestServer().getServer_url()+"register";
            Log.d("Url",">"+url);

            JsonObject jsonReq = new JsonObject();
            jsonReq.addProperty("name", name);
            jsonReq.addProperty("email", email);
            jsonReq.addProperty("phone", phone);
            jsonReq.addProperty("address", address);
            jsonReq.addProperty("password", password);

            Ion.with(RegisterActivity.this)
                    .load(url)
                    .setJsonObjectBody(jsonReq)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            String status = result.get("status").toString();
                            if (status.equals("1")){
                                new AlertDialog.Builder(RegisterActivity.this)
                                        .setIcon(R.drawable.ic_check)
                                        .setTitle("Register Berhasil")
                                        .setMessage("Akun anda telah aktif, silakan login.")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                                                startActivity(i);
                                                finish();
                                            }
                                        })
                                        .show();
                            }else{
                                mProgressBar.setVisibility(View.GONE);
                                register_form.setVisibility(View.VISIBLE);
                                Snackbar.make(findViewById(R.id.register_form), result.get("error").getAsString(), Snackbar.LENGTH_INDEFINITE)
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

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 6;
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
