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
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class AllCoinsActivity extends AppCompatActivity {


    private Button backToCamera;
    private ArrayList<Coin> allCoinsList;
    private Coin coin;
    private ProgressDialog progressDialog;
    private String allCoinsAPI = "https://euro-coin-418c3.firebaseio.com/coins.json";
    private int responseCode;
    private ListView allCoinsListView;
    private AllCoinsAdapter allCoinsAdapter;
    private String currentCoinSide;
    private double currentAccuracy;

    private String clientType;
    private Boolean auth;
    private boolean guestUserFields;
    private boolean googleUserFields;
    private Uri googleUserProfilePictureUri;
    private Toolbar toolbar;

    @Override
    protected void onStart() {
        super.onStart();

        Intent clientIntent = getIntent();
        clientType = clientIntent.getExtras().getString("accountType");
        auth = clientIntent.getExtras().getBoolean("auth");

        guestUserFields = false;
        googleUserFields = false;

        switch(clientType){
            case "GOOGLE":
                GoogleSignInAccount currentUser = GoogleSignIn.getLastSignedInAccount(this);
                if(currentUser == null){
                    auth = false;
                }else{
                    auth = true;
                    googleUserFields = true;
                }
                break;
            case "GUEST":
                auth = true;
                guestUserFields=true;
                break;
        }

        if(auth == false){
            Intent i = new Intent( AllCoinsActivity.this, ErrorActivity.class);
            i.putExtra("message", "Error with auth. Please check your account settings.");
            i.putExtra("clientType",clientType);
            i.putExtra("auth", auth);
            ((Activity)AllCoinsActivity.this).startActivity(i);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_coins);

            toolbar= (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            allCoinsList = new ArrayList<>();

            AsyncTask<String, Void , JSONArray> task = new AsyncTask<String, Void , JSONArray> (){
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    progressDialog = new ProgressDialog(AllCoinsActivity.this);
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
                        while (readLine != null) {
                            sb.append(readLine + "\n");
                            readLine = bufferedReader.readLine();
                        }

                        responseCode = urlConnection.getResponseCode();

                        if (responseCode == 200) {

                            result = new JSONArray(sb.toString());

                        } else {
                            result = null;
                        }
                        Log.i("RESPT ", String.valueOf(result));
                        urlConnection.disconnect();

                    } catch (Exception e) {
                        Log.i("ERRT ", sb.toString());
                        result = null;
                    }

                    return result;
                }

                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                protected void onPostExecute(JSONArray response) {
                    super.onPostExecute(response);

                        if (response == null) {

                            progressDialog.dismiss();
                            String errorMessage = "Sorry, something went wrong, try again!";
                            Intent i = new Intent(AllCoinsActivity.this, ErrorActivity.class);
                            i.putExtra("clientType",clientType);
                            i.putExtra("auth", auth);
                            i.putExtra("message", errorMessage);
                            ((Activity) AllCoinsActivity.this).startActivity(i);

                        } else {
                            //for (int i = 0; i < (response.length() - 1); i++){
                             for (int i = 0; i < 5; i++){

                                   try {

                                       JSONObject responseCoin = response.getJSONObject(i);

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
                                       //currentCoinSide = responseCoin.getString("side");
                                      // currentAccuracy = responseCoin.getDouble("accuracy");

                                    } catch (JSONException e) {
                                       e.printStackTrace();
                                   }

                                    allCoinsList.add(coin);


                                }

                            try {

                                setAllCoinsList(allCoinsList);

                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                     }

                };
            };

            task.execute(allCoinsAPI);

    }

    public void setAllCoinsList(ArrayList<Coin> allCoinsList) throws MalformedURLException {

        final ArrayList<Coin> newList = allCoinsList;

            AsyncTask<ArrayList<Coin>, Void, ArrayList<Coin>> task2 = new AsyncTask<ArrayList<Coin>, Void, ArrayList<Coin>>() {

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

                    allCoinsListView = findViewById(R.id.allCoins);
                    allCoinsAdapter = new AllCoinsAdapter(AllCoinsActivity.this, newList);
                    allCoinsListView.setAdapter(allCoinsAdapter);


                    allCoinsListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                           Coin currentCoin = (Coin) parent.getItemAtPosition(position);
                           System.out.println("CURRENTCOIN" + currentCoin.toString());
                            byte[] xx = getByteArray(currentCoin.getImageBitmap());

                            System.out.println("CURRENTCOIN" + clientType);
                            System.out.println("CURRENTCOIN" + auth);
                            System.out.println("CURRENTCOIN" + xx.toString());
                            Intent newIntent = new Intent( AllCoinsActivity.this, CoinInfoActivity.class);
                            newIntent.putExtra("accountType", clientType);
                            newIntent.putExtra("auth", auth);
                            newIntent.putExtra("capturedPicture","");
                            newIntent.putExtra("clickedCoin", currentCoin.toString());
                            newIntent.putExtra("clickedCoinBitmap", xx);
                            ((Activity)AllCoinsActivity.this).startActivity(newIntent);

                        }
                    });

                    backToCamera = findViewById(R.id.btnBackToCameraAllCoins);

                    backToCamera.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick (View v){

                            Intent i = new Intent(AllCoinsActivity.this, CameraActivity.class);
                            i.putExtra("accountType", clientType);
                            i.putExtra("auth", auth);
                            ((Activity) AllCoinsActivity.this).startActivity(i);

                        }
                    });

                    progressDialog.dismiss();

                }
            };

        task2.execute(newList);

    }

    @Override
    public void onBackPressed(){

        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        Intent i = new Intent( AllCoinsActivity.this, CameraActivity.class);
        i.putExtra("accountType", clientType);
        i.putExtra("auth", auth);
        ((Activity)AllCoinsActivity.this).startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        menu.findItem(R.id.action11).setVisible(true);                             //Camera option is present for all users
        menu.findItem(R.id.action1).setVisible(false);                               //All coins option hidden like current activity


        menu.findItem(R.id.profile_picture_google).setVisible(googleUserFields);    // Google profile image
        menu.findItem(R.id.action12).setVisible(googleUserFields);                  // Library option
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
                newIntent = new Intent( AllCoinsActivity.this, AllCoinsActivity.class);
                newIntent.putExtra("accountType", clientType);
                newIntent.putExtra("auth", auth);
                ((Activity)AllCoinsActivity.this).startActivity(newIntent);
                return true;
            case R.id.action11:
                newIntent = new Intent( AllCoinsActivity.this, CameraActivity.class);
                newIntent.putExtra("accountType", clientType);
                newIntent.putExtra("auth", auth);
                ((Activity)AllCoinsActivity.this).startActivity(newIntent);
                return true;
            case R.id.action12:
                newIntent = new Intent( AllCoinsActivity.this, MyLibraryActivity.class);
                newIntent.putExtra("accountType", clientType);
                newIntent.putExtra("auth", auth);
                ((Activity)AllCoinsActivity.this).startActivity(newIntent);
                return true;
            case R.id.action2:
                newIntent = new Intent( AllCoinsActivity.this, MainActivity.class);
                newIntent.putExtra("accountType", "");
                newIntent.putExtra("auth", false);
                ((Activity)AllCoinsActivity.this).startActivity(newIntent);
                return true;
            case R.id.action21:
                FirebaseAuth.getInstance().signOut();
                newIntent = new Intent( AllCoinsActivity.this, MainActivity.class);
                newIntent.putExtra("accountType", "");
                newIntent.putExtra("auth", false);
                ((Activity)AllCoinsActivity.this).startActivity(newIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public Bitmap bytesToBitmap(byte[] bytes){
        try{

            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            return bmp;
        }
        catch(Exception e){
            e.getMessage();
            return null;
        }
    }

    public byte[] getByteArray(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        return byteArray;
    }
}
