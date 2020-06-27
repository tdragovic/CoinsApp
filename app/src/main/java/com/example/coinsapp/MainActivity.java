package com.example.coinsapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    private Button btnGuest;
    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123;
    private boolean auth;
    private FirebaseAuth mAuth;
    private String accountType;
    private FirebaseUser googleUser;

    @Override
    public void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();
        googleUser = mAuth.getCurrentUser();

        if(googleUser != null){

            auth = true;
            accountType = "GOOGLE";
            updateUI(accountType, auth, googleUser);


        }else if(accountType == "GUEST" && googleUser != null){

            auth = true;
            updateUI(accountType, auth, googleUser);

        }else{

            auth = false;
            googleUser = null;
            accountType = null;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar= (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnGuest = (Button) findViewById(R.id.signInGuest);

        findViewById(R.id.google_sign_in).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                createRequest();
                signInWithGoogle();
            }
        });

        btnGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInLikeAGuest();
            }
        });

    }

    public void createRequest(){

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);



    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {

                GoogleSignInAccount account = task.getResult(ApiException.class);


                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {

                Log.w("FIREBASE ACC failed: ", "Google sign in failed", e);

            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        createUsersCoinCollection(account.getId());

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @SuppressLint("ResourceType")
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("FB USER SUCCESS", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            auth = true;
                            accountType = "GOOGLE";
                            updateUI(accountType, auth, user);
                        } else {
                            Log.w("FB USER FAILED", "signInWithCredential:failure", task.getException());
                            auth = false;
                            accountType = "GOOGLE";
                            updateUI(accountType, auth, null);
                        }

                    }
                });
    }

    private void createUsersCoinCollection(final String id) {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference usersReference = db.getReference("users");

        usersReference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("collection")){
                    //Toast.makeText(MainActivity.this,"Collection exists", Toast.LENGTH_LONG).show();
                }else{
                    usersReference.child(id).child("collection").setValue("EMPTY").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //Toast.makeText(MainActivity.this,"Collection made", Toast.LENGTH_LONG).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //Toast.makeText(MainActivity.this,"FAILED TO MAKE COIN COLLECTION", Toast.LENGTH_LONG).show();
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

    private void signInLikeAGuest() {

        auth = true;
        accountType = "GUEST";
        updateUI(accountType, auth, null);
    }

    public void updateUI(String accountType, boolean auth,  FirebaseUser user){

       if(auth){
           Log.i("CLIENT2", accountType);
          Intent i = new Intent( MainActivity.this, CameraActivity.class);
          i.putExtra("accountType", accountType);
          i.putExtra("auth", auth);
          ((Activity)MainActivity.this).startActivity(i);

       }else{

           Intent i = new Intent( MainActivity.this, ErrorActivity.class);
           i.putExtra("clientType",accountType);
           i.putExtra("auth", auth);
           i.putExtra("message", "Sign in failed. Try again in few minutes.");
           ((Activity)MainActivity.this).startActivity(i);
       }


    }

    @Override
    protected void onStop() {
        super.onStop();


    }

    @Override
    protected void onPause() {
        super.onPause();

        //keep auth data alive for next start

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //close app
    }

}
