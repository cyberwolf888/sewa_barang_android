package com.android.sewabarang;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.sewabarang.Adapter.HomeAdapter;
import com.android.sewabarang.Models.Iklan;
import com.android.sewabarang.Utility.RequestServer;
import com.android.sewabarang.Utility.Session;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MenuItem mSearchAction;
    private boolean isSearchOpened = false;
    private EditText edtSeach;

    private GridView mGridView;
    private ProgressBar mProgressBar;

    private HomeAdapter mGridAdapter;
    private List<Iklan> mGridData;
    public JsonArray data;

    Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new Session(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().clear();
        if(session.isLoggedIn()){
            navigationView.inflateMenu(R.menu.activity_main_drawer);
        }else{
            navigationView.inflateMenu(R.menu.activity_main_drawer_guest);
        }
        navigationView.setNavigationItemSelectedListener(this);

        View header=navigationView.getHeaderView(0);
        TextView name = (TextView)header.findViewById(R.id.username);
        TextView email = (TextView)header.findViewById(R.id.email);
        ImageView avatar = (ImageView)header.findViewById(R.id.imageView);
        if(session.isLoggedIn()){
            name.setText(session.getFullname());
            email.setText(session.getEmail());
            if(!session.getPhoto().equals("")){
                Ion.with(this)
                        .load(new RequestServer().getImg_url()+"profile/"+session.getPhoto())
                        .withBitmap()
                        .placeholder(R.drawable.guest)
                        .error(R.drawable.guest)
                        .intoImageView(avatar);
            }
            Log.d("img",">"+new RequestServer().getImg_url()+"profile/"+session.getPhoto());
        }else{
            name.setText("Guest");
            email.setVisibility(View.GONE);
        }
        mGridView = (GridView) findViewById(R.id.gridView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    private void getData(){
        mProgressBar.setVisibility(View.VISIBLE);
        mGridView.setVisibility(View.GONE);

        mGridData = new ArrayList<>();
        data = new JsonArray();

        String url = new RequestServer().getServer_url() + "getHomePage";
        JsonObject jsonReq = new JsonObject();
        jsonReq.addProperty("getHomePage", true);

        Log.d("url",">"+url);

        Ion.with(MainActivity.this)
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

                            mGridAdapter = new HomeAdapter(MainActivity.this, R.layout.grid_item_layout, mGridData);
                            mGridView.setAdapter(mGridAdapter);

                            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                                    //Get item at position
                                    Iklan item = (Iklan) parent.getItemAtPosition(position);
                                    //Iklan item = mGridData.get(position);

                                    Intent i = new Intent(MainActivity.this,DetailIklanActivity.class);
                                    i.putExtra("id_iklan",item.id_iklan);
                                    startActivity(i);
                                }
                            });

                            mProgressBar.setVisibility(View.GONE);
                            mGridView.setVisibility(View.VISIBLE);
                        }catch (Exception ex){
                            mProgressBar.setVisibility(View.GONE);
                            Snackbar.make(findViewById(R.id.drawer_layout), "Terjadi kesalahan saaat menyambung ke server.", Snackbar.LENGTH_INDEFINITE)
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
    public void onResume(){
        super.onResume();
        getData();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(isSearchOpened) {
                handleMenuSearch();
                return;
            }
            super.onBackPressed();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mSearchAction = menu.findItem(R.id.action_search);
        return super.onPrepareOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                handleMenuSearch();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(session.isLoggedIn()){
            if (id == R.id.nav_kategori) {
                Intent i = new Intent(MainActivity.this, KategoriActivity.class);
                startActivity(i);
            } else if (id == R.id.nav_iklan) {
                Intent i = new Intent(MainActivity.this, KelolaIklanActivity.class);
                startActivity(i);
            } else if (id == R.id.nav_profile) {
                Intent i = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(i);
            } else if (id == R.id.nav_logout) {
                new AlertDialog.Builder(MainActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Logout")
                        .setMessage("Apakah anda yakin untuk logout dari aplikasi?")
                        .setPositiveButton("Iya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                session.logoutUser();
                            }
                        })
                        .setNegativeButton("Tidak",null)
                        .show();

            }
        }else{
            if (id == R.id.nav_kategori) {
                Intent i = new Intent(MainActivity.this, KategoriActivity.class);
                startActivity(i);
            } else if (id == R.id.nav_login) {
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
            } else if (id == R.id.nav_register) {
                Intent i = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    protected void handleMenuSearch(){
        ActionBar action = getSupportActionBar(); //get the actionbar

        if(isSearchOpened){ //test if the search is open

            action.setDisplayShowCustomEnabled(false); //disable a custom view inside the actionbar
            action.setDisplayShowTitleEnabled(true); //show the title in the action bar

            //hides the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edtSeach.getWindowToken(), 0);

            //add the search icon in the action bar
            mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_search));

            isSearchOpened = false;
        } else { //open the search entry

            action.setDisplayShowCustomEnabled(true); //enable it to display a
            // custom view in the action bar.
            action.setCustomView(R.layout.search_bar);//add the custom view
            action.setDisplayShowTitleEnabled(false); //hide the title

            edtSeach = (EditText)action.getCustomView().findViewById(R.id.edtSearch); //the text editor

            //this is a listener to do a search when the user clicks on search button
            edtSeach.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        doSearch();
                        return true;
                    }
                    return false;
                }
            });


            edtSeach.requestFocus();

            //open the keyboard focused in the edtSearch
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edtSeach, InputMethodManager.SHOW_IMPLICIT);


            //add the close icon
            mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_close));

            isSearchOpened = true;
        }
    }

    private void doSearch() {
        EditText edtSearch = (EditText) findViewById(R.id.edtSearch);
        Intent i = new Intent(MainActivity.this,SearchActivity.class);
        i.putExtra("keyword",edtSearch.getText().toString());
        startActivity(i);
    }
}
