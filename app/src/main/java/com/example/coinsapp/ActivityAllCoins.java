package com.example.coinsapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;

public class ActivityAllCoins extends AppCompatActivity {

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
            Intent i = new Intent( ActivityAllCoins.this, ErrorActivity.class);
            i.putExtra("message", "Error with auth. Please check your account settings.");
            i.putExtra("clientType",clientType);
            i.putExtra("auth", auth);
            ((Activity)ActivityAllCoins.this).startActivity(i);

        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_all_coins);

        toolbar= (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);









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
                newIntent = new Intent( ActivityAllCoins.this, AllCoinsActivity.class);
                newIntent.putExtra("accountType", clientType);
                newIntent.putExtra("auth", auth);
                ((Activity)ActivityAllCoins.this).startActivity(newIntent);
                return true;
            case R.id.action11:
                newIntent = new Intent( ActivityAllCoins.this, CameraActivity.class);
                newIntent.putExtra("accountType", clientType);
                newIntent.putExtra("auth", auth);
                ((Activity)ActivityAllCoins.this).startActivity(newIntent);
                return true;
            case R.id.action12:
                newIntent = new Intent( ActivityAllCoins.this, MyLibraryActivity.class);
                newIntent.putExtra("accountType", clientType);
                newIntent.putExtra("auth", auth);
                ((Activity)ActivityAllCoins.this).startActivity(newIntent);
                return true;
            case R.id.action2:
                newIntent = new Intent( ActivityAllCoins.this, MainActivity.class);
                newIntent.putExtra("accountType", "");
                newIntent.putExtra("auth", false);
                ((Activity)ActivityAllCoins.this).startActivity(newIntent);
                return true;
            case R.id.action21:
                FirebaseAuth.getInstance().signOut();
                newIntent = new Intent( ActivityAllCoins.this, MainActivity.class);
                newIntent.putExtra("accountType", "");
                newIntent.putExtra("auth", false);
                ((Activity)ActivityAllCoins.this).startActivity(newIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
