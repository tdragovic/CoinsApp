package com.example.coinsapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class MyLibraryActivity extends AppCompatActivity {

    private Button backToCamera;
    private Button btnAllCoins;
    private ListView libraryCoinsListView;
    private LibraryListAdapter libraryCoinAdapter;
    private Coin libraryCoin;
    private ProgressDialog progressDialog;
    private ArrayList<Coin> libraryCoins;
    private String myLibraryAPI;
    private int responseCode;
    private Coin coin;
    private String currentCoinSide;
    private double currentAccuracy;
    private String clientType;
    private Boolean auth;
    private boolean guestUserFields;
    private boolean googleUserFields;
    private Uri googleUserProfilePictureUri;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_library);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent clientIntent = getIntent();
        clientType = clientIntent.getExtras().getString("accountType");
        auth = clientIntent.getExtras().getBoolean("auth");

        guestUserFields = false;
        googleUserFields = false;

        switch(clientType){
            case "GOOGLE":
                GoogleSignInAccount currentUser = GoogleSignIn.getLastSignedInAccount(this);
                myLibraryAPI = "https://coinsapp-d36d9.firebaseio.com/users/"+ currentUser.getId() +"/collection.json";

                if(currentUser == null) {
                    auth = false;
                    googleUserFields = false;
                }else{
                    auth = true;
                    googleUserFields = true;
                    googleUserProfilePictureUri = currentUser.getPhotoUrl();
                }
                break;
            case "GUEST":
                auth = true;
                Intent i = new Intent( MyLibraryActivity.this, ErrorActivity.class);
                i.putExtra("clientType",clientType);
                i.putExtra("auth", auth);
                i.putExtra("message", "Error with auth. Please check your account settings.");
                ((Activity)MyLibraryActivity.this).startActivity(i);
                break;
        }

        if(auth == false){

            Intent i = new Intent( MyLibraryActivity.this, ErrorActivity.class);
            i.putExtra("clientType",clientType);
            i.putExtra("auth", auth);
            i.putExtra("message", "Error with auth. Please check your account settings.");
            ((Activity)MyLibraryActivity.this).startActivity(i);

        }

        libraryCoins = new ArrayList<Coin>();

        backToCamera = findViewById(R.id.btnBackToCameraLib);

        backToCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent openCameraIntent = new Intent( MyLibraryActivity.this, CameraActivity.class);
                openCameraIntent.putExtra("accountType", clientType);
                openCameraIntent.putExtra("auth", auth);
                ((Activity)MyLibraryActivity.this).startActivity(openCameraIntent);

            }
        });

        btnAllCoins = (Button) findViewById(R.id.btnAllCoinsLib);

        btnAllCoins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i3 = new Intent( MyLibraryActivity.this, AllCoinsActivity.class);
                i3.putExtra("accountType", clientType);
                i3.putExtra("auth", auth);
                ((Activity)MyLibraryActivity.this).startActivity(i3);

            }
        });

        AsyncTask<String, Void , JSONArray> setLibraryTask = new AsyncTask<String, Void , JSONArray> (){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = new ProgressDialog(MyLibraryActivity.this);
                progressDialog.show();
                progressDialog.setContentView(R.layout.activity_loading);
                progressDialog.getWindow().setBackgroundDrawableResource(
                        android.R.color.transparent
                );
            }

            @Override
            protected JSONArray doInBackground(String...args) {

                JSONArray result = null;
                StringBuilder sb = new StringBuilder();
                InputStream stream = null;

                try {

                    URL myUrl = new URL(args[0]);
                    HttpURLConnection urlConnection = (HttpURLConnection) myUrl.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    urlConnection.connect();

                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        stream = urlConnection.getInputStream();
                    }

                    Log.i("STATUS ", String.valueOf(urlConnection.getResponseCode()));
                    Log.i("MSG ", urlConnection.getResponseMessage());

                    InputStream in = urlConnection.getInputStream();
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(in);

                    InputStreamReader inputStreamReader = new InputStreamReader(bufferedInputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    String readLine = bufferedReader.readLine();
                    String resp = "";
                    while (readLine != null) {
                        resp =readLine.substring(1,6);
                        sb.append(readLine + "\n");
                        readLine = bufferedReader.readLine();
                    }

                    responseCode = urlConnection.getResponseCode();

                    if (responseCode == 200) {

                        if(resp.equals("EMPTY")){
                            result = new JSONArray();
                        }else {

                            if(sb.toString().charAt(0)=='['){
                                result = new JSONArray(sb.toString());
                            }else {
                                System.out.println("RESPONSELIB3" + sb.toString());
                                result = new JSONArray();

                                JSONObject jsonObject = new JSONObject(sb.toString());
                                Iterator<String> keys = jsonObject.keys();

                                System.out.println("RESPONSELIB4" + jsonObject.toString());
                                while (keys.hasNext()) {
                                    String key = keys.next();
                                    System.out.println("RESPONSELIB5" + key);
                                    System.out.println("RESPONSELIB51" + jsonObject.get(key).toString());
                                    if (jsonObject.get(key) != null) {
                                        System.out.println("RESPONSELIB6" + jsonObject.get(key).toString());
                                        JSONObject j = new JSONObject(jsonObject.get(key).toString());
                                        result.put(j);
                                    }
                                }
                            }
                        }

                    } else {
                        result = null;
                    }

                    urlConnection.disconnect();

                } catch (Exception e) {

                    result = null;
                }

                return result;
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            protected void onPostExecute(JSONArray response) {
                super.onPostExecute(response);

                System.out.println("RESP" + response);

                if(response == null){

                    progressDialog.dismiss();
                    //String errorMessage = "Sorry, something went wrong, try again!";
                    String errorMessage = "Empty collection";
                    Intent i = new Intent(MyLibraryActivity.this, ErrorActivity.class);
                    i.putExtra("accountType",clientType);
                    i.putExtra("auth", auth);
                    i.putExtra("message", errorMessage);
                    ((Activity) MyLibraryActivity.this).startActivity(i);

                }
                else if(response.length()==0){

                    progressDialog.dismiss();
                    System.out.println("RESPONSEEMPTY" + "PRAZAN NIZ");
                    //ISPISATI DA JE LIB PRAZNA

                }
                else{

                       for (int i = 0; i < response.length(); i++){

                        System.out.println("RESPONSELIB" + response);
                        try {
                            System.out.println("Responselib i " + response.getJSONObject(i));

                            if(response.getJSONObject(i)!=null){

                                JSONObject responseCoin = response.getJSONObject(i);
                                System.out.println("RESPONSELIBJSON" + responseCoin.toString());
                                coin = new Coin();

                                coin.setName(responseCoin.getString("name"));
                                coin.setDesc(responseCoin.getString("desc"));
                                coin.setReverse(responseCoin.getString("reverse"));
                                coin.setObverse(responseCoin.getString("obverse"));
                                coin.setYear(responseCoin.getString("year"));
                                coin.setPrices((float)responseCoin.getDouble("prices"));
                                coin.setCountry(responseCoin.getString("country"));
                                coin.setIndex(responseCoin.getInt("index"));
                                coin.setAlignment(responseCoin.getString("alignment"));
                                coin.setThickness(responseCoin.getString("thickness"));
                                coin.setShape(responseCoin.getString("shape"));
                                coin.setRevCodes(responseCoin.getString("revCodes"));
                                coin.setObvCodes(responseCoin.getInt("obvCodes"));
                                coin.setWeight(responseCoin.getString("weight"));
                                coin.setDiameter(responseCoin.getString("diameter"));
                                coin.setMaterial(responseCoin.getString("material"));

                                libraryCoins.add(coin);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                        System.out.println("LIBLIST "+ libraryCoins.toString());

                        setLibraryList(libraryCoins);
                }

            };
        };
        setLibraryTask.execute(myLibraryAPI);

    }

    public void setLibraryList(ArrayList<Coin> libraryCoins){

        final ArrayList<Coin> libraryCoinsNew = libraryCoins;

        AsyncTask<ArrayList<Coin>, Void, ArrayList<Coin>> setBitmap = new AsyncTask<ArrayList<Coin>, Void, ArrayList<Coin>>() {

            Bitmap bitmap = null;
            InputStream stream2 = null;

            @Override
            protected ArrayList<Coin> doInBackground(ArrayList<Coin>...coins) {

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inSampleSize = 1;

                for(Coin c: coins[0]){

                    try {

                        URL myUrl = new URL(c.getObverse());
                        HttpURLConnection httpConnection = (HttpURLConnection) myUrl.openConnection();
                        httpConnection.setRequestMethod("GET");
                        httpConnection.connect();

                        if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            stream2 = httpConnection.getInputStream();
                        }

                        bitmap = BitmapFactory.decodeStream(stream2, null, bmOptions);
                        c.setImageBitmap(bitmap);
                        stream2.close();

                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                }

                return coins[0];
            }

            @Override
            protected void onPostExecute(ArrayList<Coin> newList) {
                super.onPostExecute(newList);


                libraryCoinsListView = (ListView) findViewById(R.id.libraryList);
                libraryCoinAdapter = new LibraryListAdapter(MyLibraryActivity.this, newList);
                libraryCoinsListView.setAdapter(libraryCoinAdapter);

                progressDialog.dismiss();

            }
        };

        setBitmap.execute(libraryCoinsNew);
    }

    @Override
    public void onBackPressed() {
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        Intent ii = new Intent( MyLibraryActivity.this, CameraActivity.class);
        ii.putExtra("accountType", clientType);
        ii.putExtra("auth", auth);
        ((Activity)MyLibraryActivity.this).startActivity(ii);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        menu.findItem(R.id.action11).setVisible(true);                             //Camera option is present for all users
        menu.findItem(R.id.action1).setVisible(true);                               //All coins option is present for all users


        menu.findItem(R.id.profile_picture_google).setVisible(googleUserFields);    // Google profile image
        menu.findItem(R.id.action12).setVisible(false);                             // Library option hidden cause it is current activity
        menu.findItem(R.id.action21).setVisible(googleUserFields);                  //Sign out option

        menu.findItem(R.id.active_profile_picture).setVisible(guestUserFields);     //Guest icon
        menu.findItem(R.id.action2).setVisible(guestUserFields);                    //Sign in option


        //MenuItem menuItem = menu.findItem(R.id.profile_picture_google);
        //System.out.println("V2" + menuItem.getItemId());
        //View view = MenuItemCompat.getActionView(menuItem);
        //System.out.println("V3" + view.getTransitionName());
        //CircleImageView profileImageView = view.findViewById(R.id.profileImageView);
        //profileImageView.setImageURI(googleUserProfilePictureUri);
        //System.out.println("IMAGE URI" + profileImageView.getImageAlpha());
        //Picasso.get().load(googleUserProfilePictureUri).into(profileImageView);

        return true;
    };


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        Intent newIntent;

        switch(item.getItemId()){
            case R.id.action1:
                newIntent = new Intent( MyLibraryActivity.this, AllCoinsActivity.class);
                newIntent.putExtra("accountType", clientType);
                newIntent.putExtra("auth", auth);
                ((Activity)MyLibraryActivity.this).startActivity(newIntent);
                return true;
            case R.id.action11:
                newIntent = new Intent( MyLibraryActivity.this, CameraActivity.class);
                newIntent.putExtra("accountType", clientType);
                newIntent.putExtra("auth", auth);
                ((Activity)MyLibraryActivity.this).startActivity(newIntent);
                return true;
            case R.id.action12:
                newIntent = new Intent( MyLibraryActivity.this, MyLibraryActivity.class);
                newIntent.putExtra("accountType", clientType);
                newIntent.putExtra("auth", auth);
                ((Activity)MyLibraryActivity.this).startActivity(newIntent);
                return true;
            case R.id.action2:
                newIntent = new Intent( MyLibraryActivity.this, MainActivity.class);
                newIntent.putExtra("accountType", "");
                newIntent.putExtra("auth", false);
                ((Activity)MyLibraryActivity.this).startActivity(newIntent);
                return true;
            case R.id.action21:
                FirebaseAuth.getInstance().signOut();
                newIntent = new Intent( MyLibraryActivity.this, MainActivity.class);
                newIntent.putExtra("accountType", "");
                newIntent.putExtra("auth", false);
                ((Activity)MyLibraryActivity.this).startActivity(newIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

}
