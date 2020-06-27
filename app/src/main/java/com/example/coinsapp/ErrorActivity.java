package com.example.coinsapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;

public class ErrorActivity extends AppCompatActivity {

    private String errorMessage;
    private TextView messageView;
    private Button backToCamera;

    private String clientType;
    private Boolean auth;
    private boolean guestUserFields;
    private boolean googleUserFields;
    private Uri googleUserProfilePictureUri;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        Intent clientIntent = getIntent();

        clientType = clientIntent.getExtras().getString("accountType");
        auth = clientIntent.getExtras().getBoolean("auth");
        errorMessage = (String) clientIntent.getExtras().get("message");

        guestUserFields = false;
        googleUserFields = false;

        switch(clientType){
            case "GOOGLE":
                GoogleSignInAccount currentUser = GoogleSignIn.getLastSignedInAccount(this);
                if(currentUser == null){
                    auth = false;
                    googleUserFields = false;
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
            errorMessage = "Error with auth. Please check your account settings.";
        }

        toolbar= (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(errorMessage != ""){

            messageView = (TextView) findViewById(R.id.errorMessage);
            messageView.setText(errorMessage);
        }

        backToCamera = (Button) findViewById(R.id.btnBackToCameraError);

        backToCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent( ErrorActivity.this, CameraActivity.class);
                i.putExtra("clientType",clientType);
                i.putExtra("auth", auth);
                ((Activity)ErrorActivity.this).startActivity(i);

            }
        });
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent i = new Intent( ErrorActivity.this, MainActivity.class);
        i.putExtra("clientType",clientType);
        i.putExtra("auth", auth);
        ((Activity)ErrorActivity.this).startActivity(i);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        menu.findItem(R.id.action11).setVisible(false);                             //Camera option is hidden cause button for this option already exists
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
                newIntent = new Intent( ErrorActivity.this, AllCoinsActivity.class);
                newIntent.putExtra("accountType", clientType);
                newIntent.putExtra("auth", auth);
                ((Activity)ErrorActivity.this).startActivity(newIntent);
                return true;
            case R.id.action11:
                newIntent = new Intent( ErrorActivity.this, CameraActivity.class);
                newIntent.putExtra("accountType", clientType);
                newIntent.putExtra("auth", auth);
                ((Activity)ErrorActivity.this).startActivity(newIntent);
                return true;
            case R.id.action12:
                newIntent = new Intent( ErrorActivity.this, MyLibraryActivity.class);
                newIntent.putExtra("accountType", clientType);
                newIntent.putExtra("auth", auth);
                ((Activity)ErrorActivity.this).startActivity(newIntent);
                return true;
            case R.id.action2:
                newIntent = new Intent( ErrorActivity.this, MainActivity.class);
                newIntent.putExtra("accountType", "");
                newIntent.putExtra("auth", false);
                ((Activity)ErrorActivity.this).startActivity(newIntent);
                return true;
            case R.id.action21:
                FirebaseAuth.getInstance().signOut();
                newIntent = new Intent( ErrorActivity.this, MainActivity.class);
                newIntent.putExtra("accountType", "");
                newIntent.putExtra("auth", false);
                ((Activity)ErrorActivity.this).startActivity(newIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
