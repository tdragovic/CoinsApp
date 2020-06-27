package com.example.coinsapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.SuccessContinuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonIOException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class CoinInfoActivity extends AppCompatActivity {

    private TextView coinName;
    private TextView coinPrice;
    private TextView coinDescription;
    private TextView coinCountry;
    private TextView coinYear;
    private TextView coinSpecification;
    private ImageView myImage;
    private Button btnAddToLibrary;
    private Button backToCamera;
    private String capturedPicture = "";
    private String errorMessage;
    private Coin coinToShow;

    private String recognitionAPI = "http://192.168.1.5:3000/predict";
    private int responseCode;


    private final int RESPONSE_CODE_RECOGNISED = 200;
    private final int RESPONSE_CODE_UNRECOGNISED = 404;

    private ProgressDialog progressDialog;
    private Coin coin;
    private String currentCoinSide;
    private double currentAccuracy;

    private String clientType;
    private Boolean auth;
    private String clickedCoin = "";
    private byte[] clickedCoinBitmap;
    private boolean guestUserFields;
    private boolean googleUserFields;
    private Uri googleUserProfilePictureUri;
    private Toolbar toolbar;
    private GoogleSignInAccount currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_info);

        toolbar= (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent clientIntent = getIntent();

        capturedPicture = clientIntent.getExtras().getString("capturedPicture");
        clickedCoin = clientIntent.getExtras().getString("clickedCoin");
        clickedCoinBitmap = clientIntent.getExtras().getByteArray("clickedCoinBitmap");
        clientType = clientIntent.getExtras().getString("accountType");
        auth = clientIntent.getExtras().getBoolean("auth");

//        System.out.println("INFOCOIN" + capturedPicture);
//        System.out.println("INFOCOIN" + clickedCoin);
//        System.out.println("INFOCOIN" + clientType);
//        System.out.println("INFOCOIN" + auth);
//        System.out.println("INFOCOIN" + clickedCoinBitmap.length);

        guestUserFields = false;
        googleUserFields = false;

        switch(clientType){
            case "GOOGLE":
                currentUser = GoogleSignIn.getLastSignedInAccount(this);
                if(currentUser == null){

                    auth = false;

                }else{

                    auth = true;
                    googleUserFields=true;
                    googleUserProfilePictureUri = currentUser.getPhotoUrl();
                    btnAddToLibrary = findViewById(R.id.btnAddToLibrary);
                    btnAddToLibrary.setVisibility(View.VISIBLE);
                }
                break;
            case "GUEST":

                guestUserFields = true;
                findViewById(R.id.btnAddToLibrary).setVisibility(View.INVISIBLE);
                auth = true;
                break;
        }

        if(auth == false){
            Intent i = new Intent( CoinInfoActivity.this, ErrorActivity.class);
            i.putExtra("accountType",clientType);
            i.putExtra("auth", auth);
            i.putExtra("message", "Error with auth. Please check your account settings.");
            ((Activity)CoinInfoActivity.this).startActivity(i);

        }

        backToCamera = findViewById(R.id.btnBackToCameraDetails);

        backToCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent( CoinInfoActivity.this, CameraActivity.class);
                i.putExtra("accountType", clientType);
                i.putExtra("auth", auth);
                ((Activity)CoinInfoActivity.this).startActivity(i);

            }
        });

        coin = new Coin();

        if(!clickedCoin.equals("") && clickedCoin!=null) {

            progressDialog = new ProgressDialog(CoinInfoActivity.this);
            progressDialog.show();
            progressDialog.setContentView(R.layout.activity_loading);
            progressDialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );

            try {
                JSONObject coinJson = new JSONObject(clickedCoin);

                coinToShow = new Coin(
                        coinJson.getInt("index"),
                        coinJson.getString("name"),
                        coinJson.getString("country"),
                        coinJson.getString("desc"),
                        coinJson.getString("year"),
                        coinJson.getString("reverse"),
                        coinJson.getString("obverse"),
                        (float) coinJson.getDouble("prices"),
                        coinJson.getString("alignment"),
                        coinJson.getString("thickness"),
                        coinJson.getString("shape"),
                        coinJson.getString("revCodes"),
                        coinJson.getInt("obvCodes"),
                        coinJson.getString("weight"),
                        coinJson.getString("diameter"),
                        coinJson.getString("material")
                );

                try {

                    Bitmap bitmapxx =  bytesToBitmap(clickedCoinBitmap);
                    coinToShow.setImageBitmap(bitmapxx);

                    if(btnAddToLibrary!=null){
                        btnAddToLibrary.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addToLibrary(coinToShow);
                            }
                        });
                    }

                    setCoinInfo(coinToShow);

                } catch (MalformedURLException e) {

                    e.printStackTrace();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }else if(!capturedPicture.equals("") && capturedPicture!=null){

            AsyncTask<String, Void , JSONObject> task = new AsyncTask<String, Void , JSONObject> (){

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    progressDialog = new ProgressDialog(CoinInfoActivity.this);
                    progressDialog.show();
                    progressDialog.setContentView(R.layout.activity_loading);
                    progressDialog.getWindow().setBackgroundDrawableResource(
                            android.R.color.transparent
                    );
                }

                @Override
                protected JSONObject doInBackground(String...args) {
                    System.out.println("RESPONSEREC");
                    JSONObject result = null;
                    StringBuilder sb = new StringBuilder();

                    try {
                        URL myUrl = new URL(args[0]);
                        HttpURLConnection urlConnection = (HttpURLConnection) myUrl.openConnection();
                        urlConnection.setRequestMethod("POST");
                        urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                        urlConnection.setRequestProperty("Accept","application/json");
                        urlConnection.setDoOutput(true);
                        urlConnection.setDoInput(true);
                        urlConnection.connect();

                        JSONObject jsonParam = new JSONObject();

                        jsonParam.put("image", capturedPicture);

                        DataOutputStream outputStreams = new DataOutputStream(urlConnection.getOutputStream());
                        outputStreams.writeBytes(jsonParam.toString());

                        Log.i("JSON OBJ ", String.valueOf(jsonParam));
                        Log.i("STATUS ", String.valueOf(urlConnection.getResponseCode()));
                        Log.i("MSG " , urlConnection.getResponseMessage());

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

                        if((responseCode == RESPONSE_CODE_RECOGNISED) || (responseCode == RESPONSE_CODE_UNRECOGNISED)){
                            result = new JSONObject(sb.toString());
                        }else{
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
                protected void onPostExecute(JSONObject response) {
                    super.onPostExecute(response);

                    try {

                        if(response == null){

                            progressDialog.dismiss();
                            errorMessage = "Sorry, something went wrong, try again!";
                            Intent i = new Intent(CoinInfoActivity.this, ErrorActivity.class);
                            i.putExtra("accountType",clientType);
                            i.putExtra("auth", auth);
                            i.putExtra("message", errorMessage);
                            ((Activity)CoinInfoActivity.this).startActivity(i);

                        } else if(response.getDouble("accuracy") > 20) {

                            coin.setName(response.getJSONObject("coin").getString("name"));
                            coin.setDesc(response.getJSONObject("coin").getString("desc"));
                            coin.setReverse(response.getJSONObject("coin").getString("reverse"));
                            coin.setObverse(response.getJSONObject("coin").getString("obverse"));
                            coin.setYear(response.getJSONObject("coin").getString("year"));
                            coin.setPrices((float)response.getJSONObject("coin").getDouble("prices"));
                            coin.setCountry(response.getJSONObject("coin").getString("country"));
                            coin.setIndex(response.getJSONObject("coin").getInt("index"));
                            coin.setAlignment(response.getJSONObject("coin").getString("alignment"));
                            coin.setThickness(response.getJSONObject("coin").getString("thickness"));
                            coin.setShape(response.getJSONObject("coin").getString("shape"));
                            coin.setRevCodes(response.getJSONObject("coin").getString("revCodes"));
                            coin.setObvCodes(response.getJSONObject("coin").getInt("obvCodes"));
                            coin.setWeight(response.getJSONObject("coin").getString("weight"));
                            coin.setDiameter(response.getJSONObject("coin").getString("diameter"));
                            coin.setMaterial(response.getJSONObject("coin").getString("material"));
                            currentCoinSide = response.getString("side");
                            currentAccuracy = response.getDouble("accuracy");

                            setCoinInfo(coin);

                            //setSimilarCoins(response);

                        }else if((response.getDouble("accuracy") <= 20) && (response.getDouble("accuracy") >= 0)){

                            //setSimilarCoins(response);
                            progressDialog.dismiss();

                            errorMessage = "Sorry, your coin is not recognised.";
                            Intent i = new Intent(CoinInfoActivity.this, ErrorActivity.class);
                            i.putExtra("accountType",clientType);
                            i.putExtra("auth", auth);
                            i.putExtra("message", errorMessage);
                            ((Activity)CoinInfoActivity.this).startActivity(i);

                        }

                    } catch (JSONException | MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            };

            task.execute(recognitionAPI);
        }else{

            errorMessage = "Sorry, something went wrong. Try again.";
            Intent i = new Intent(CoinInfoActivity.this, ErrorActivity.class);
            i.putExtra("accountType",clientType);
            i.putExtra("auth", auth);
            i.putExtra("message", errorMessage);
            ((Activity)CoinInfoActivity.this).startActivity(i);

        }

    }

    private void addToLibrary(final Coin coin){

        FirebaseDatabase  db =  FirebaseDatabase.getInstance();
        final DatabaseReference usersReference = db.getReferenceFromUrl("https://coinsapp-d36d9.firebaseio.com/users");
        final String userID = currentUser.getId();
        final String coinID = String.valueOf(coin.getIndex());


        if(userID != null) {

            final DatabaseReference libRef = usersReference.child(userID).child("collection");

            libRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if(snapshot.hasChild(coinID)) {
                        Toast.makeText(CoinInfoActivity.this, "You have this coin in your collection.", Toast.LENGTH_LONG).show();
                    }else{

                        libRef.child(coinID).setValue(new Coin(coin)).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                task.addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(CoinInfoActivity.this, "COIN ADDED", Toast.LENGTH_LONG).show();
                                        Intent inte = new Intent(CoinInfoActivity.this, MyLibraryActivity.class);
                                        inte.putExtra("accountType", clientType);
                                        inte.putExtra("auth", auth);
                                        ((Activity) CoinInfoActivity.this).startActivity(inte);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(CoinInfoActivity.this, "FAILED TO ADD COIN", Toast.LENGTH_LONG).show();
                                    }
                                });

                            }

                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public void setCoinInfo(final Coin recognisedCoin) throws MalformedURLException {

        coinName = findViewById(R.id.coinName);
        coinName.setText(recognisedCoin.getName());

        coinPrice = findViewById(R.id.coinPrice);
        coinPrice.setText(String.valueOf(recognisedCoin.getPrices()));

        coinDescription = findViewById(R.id.coinDesc);
        coinDescription.setText(recognisedCoin.getDesc());

        coinYear =findViewById(R.id.coinYear);
        coinYear.setText(recognisedCoin.getYear());

        coinCountry = findViewById(R.id.coinCountry);
        coinCountry.setText(recognisedCoin.getCountry());

        coinSpecification = findViewById(R.id.coinSpecification);
        String specString = recognisedCoin.getMaterial();

        coinSpecification.setText(specString);

        String imageUrl1 =  recognisedCoin.getReverse();

        if(!clickedCoin.equals("") && clickedCoin!=null){

            myImage = (ImageView) findViewById(R.id.coinImage);
            myImage.setImageBitmap(recognisedCoin.getImageBitmap());

            progressDialog.dismiss();

        }else if(!capturedPicture.equals("") && capturedPicture!=null) {
            System.out.println("SLUCAJ2");
            String imageUrl2 = recognisedCoin.getObverse();

            AsyncTask<String, Void, Bitmap> task2 = new AsyncTask<String, Void, Bitmap>() {

                Bitmap bitmap = null;
                InputStream stream = null;

                @Override
                protected Bitmap doInBackground(String... args) {

                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    bmOptions.inSampleSize = 1;

                    try {

                        URL myUrl = new URL(args[0]);
                        HttpURLConnection httpConnection = (HttpURLConnection) myUrl.openConnection();
                        httpConnection.setRequestMethod("GET");
                        httpConnection.connect();

                        if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            stream = httpConnection.getInputStream();
                        }

                        bitmap = BitmapFactory.decodeStream(stream, null, bmOptions);
                        stream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    return bitmap;
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);

                    myImage = (ImageView) findViewById(R.id.coinImage);
                    myImage.setImageBitmap(bitmap);

                        System.out.println("SLUCAJ3");

                        recognisedCoin.setImageBitmap(bitmap);

                        if(btnAddToLibrary!=null) {
                            btnAddToLibrary.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    addToLibrary(recognisedCoin);
                                }
                            });
                        }

                    progressDialog.dismiss();

                }
            };

            task2.execute(imageUrl2);
        }
//       ExifInterface e = null;

//        try {
//            e = new ExifInterface();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//
//        int orientation = ExifInterface.ORIENTATION_NORMAL;
//
//        if (e != null)
//            orientation = e.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
//
//        switch (orientation) {
//            case ExifInterface.ORIENTATION_ROTATE_90:
//                myBitmap = rotateBitmap(myBitmap, 90);
//                break;
//            case ExifInterface.ORIENTATION_ROTATE_180:
//                myBitmap = rotateBitmap(myBitmap, 180);
//                break;
//
//            case ExifInterface.ORIENTATION_ROTATE_270:
//                myBitmap = rotateBitmap(myBitmap, 270);
//                break;
//        }



    }

//    public void setSimilarCoins(JSONObject res) throws JSONException {
//
//        ArrayList<Coin> similarCoins = new ArrayList<>();
//        JSONArray similarCoinsJSONArray = new JSONArray();
//        similarCoinsJSONArray = res.getJSONArray("similar");
//
//        for(int i=0;i<similarCoinsJSONArray.length();i++){
//
//            JSONObject jsonData = similarCoinsJSONArray.getJSONObject(i);
//
//            Coin similarCoin = new Coin();
//
//            similarCoin.setName(jsonData.getString("name"));
//            similarCoin.setDescription(jsonData.getString("description"));
//            similarCoin.setImageUrl1(jsonData.getString("imageUrl1"));
//            similarCoin.setImageUrl2(jsonData.getString("imageUrl2"));
//            similarCoin.setYear(jsonData.getString("year"));
//            similarCoin.setPrice(jsonData.getString("price"));
//            similarCoin.setCountry(jsonData.getString("country"));
//            String specifications2[]=new String[]{"Spec1", "Spec2"};
//            similarCoin.setSpecification(specifications2);
//
//            similarCoins.add(similarCoin);
//        }
//
//        ListView similarCoinsListView = (ListView) findViewById(R.id.list_similar_coins);
//        SimilarCoinAdapter similarCoinAdapter;
//        similarCoinAdapter = new SimilarCoinAdapter(CoinInfoActivity.this, similarCoins);
//        similarCoinsListView.setAdapter(similarCoinAdapter);
//    }

//    public Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
//        Matrix matrix = new Matrix();
//        matrix.postRotate(degrees);
//        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//    }

    @Override
    public void onBackPressed(){
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        Intent i = new Intent( CoinInfoActivity.this, AllCoinsActivity.class);
        i.putExtra("accountType", clientType);
        i.putExtra("auth", auth);
        ((Activity)CoinInfoActivity.this).startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        menu.findItem(R.id.action11).setVisible(true);                             //Camera option is present for all users
        menu.findItem(R.id.action1).setVisible(true);                               //All coins option is present for all users


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
                newIntent = new Intent( CoinInfoActivity.this, AllCoinsActivity.class);
                newIntent.putExtra("accountType", clientType);
                newIntent.putExtra("auth", auth);
                ((Activity)CoinInfoActivity.this).startActivity(newIntent);
                return true;
            case R.id.action11:
                newIntent = new Intent( CoinInfoActivity.this, CameraActivity.class);
                newIntent.putExtra("accountType", clientType);
                newIntent.putExtra("auth", auth);
                ((Activity)CoinInfoActivity.this).startActivity(newIntent);
                return true;
            case R.id.action12:
                newIntent = new Intent( CoinInfoActivity.this, MyLibraryActivity.class);
                newIntent.putExtra("accountType", clientType);
                newIntent.putExtra("auth", auth);
                ((Activity)CoinInfoActivity.this).startActivity(newIntent);
                return true;
            case R.id.action2:
                newIntent = new Intent( CoinInfoActivity.this, MainActivity.class);
                newIntent.putExtra("accountType", "");
                newIntent.putExtra("auth", false);
                ((Activity)CoinInfoActivity.this).startActivity(newIntent);
                return true;
            case R.id.action21:
                FirebaseAuth.getInstance().signOut();
                newIntent = new Intent( CoinInfoActivity.this, MainActivity.class);
                newIntent.putExtra("accountType", "");
                newIntent.putExtra("auth", false);
                ((Activity)CoinInfoActivity.this).startActivity(newIntent);
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
}
