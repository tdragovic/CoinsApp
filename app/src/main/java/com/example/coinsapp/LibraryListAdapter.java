package com.example.coinsapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class LibraryListAdapter extends ArrayAdapter<Coin> {

    private ArrayList<Coin> libraryCoins;
    private Context c;

    public LibraryListAdapter(Context context, ArrayList<Coin> libraryCoins) {
        super(context, 0, libraryCoins);

        this.c = context;
        this.libraryCoins = new ArrayList<Coin>(libraryCoins);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {



        View libraryCoinView = convertView;

        if(libraryCoinView==null){
            libraryCoinView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_library_coin, parent, false);
        }

        final Coin currentCoin = getItem(position);

        TextView coinName = (TextView) libraryCoinView.findViewById(R.id.libraryCoinName);
        TextView coinPrice = (TextView) libraryCoinView.findViewById(R.id.libraryCoinPrice);
        TextView coinYear = (TextView) libraryCoinView.findViewById(R.id.libraryCoinYear);
        ImageView coinImage = (ImageView) libraryCoinView.findViewById(R.id.imageViewLibrary);
        ImageButton removeCoin = (ImageButton) libraryCoinView.findViewById(R.id.removeFromLibrary);

        coinName.setText(currentCoin.getName());
        coinPrice.setText(String.valueOf(currentCoin.getPrices()));
        coinYear.setText(currentCoin.getYear());
        coinImage.setImageBitmap(currentCoin.getImageBitmap());

        removeCoin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                removeCoinFromLibrary(currentCoin.getIndex(), c);

            }
        });

        return libraryCoinView;
    }

    public void removeCoinFromLibrary(int id, Context con){


        GoogleSignInAccount currentUser = GoogleSignIn.getLastSignedInAccount(con);
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference usersReference = db.getReference("users");

      //  String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference libraryReference = usersReference.child(currentUser.getId()).child("collection");

        libraryReference.child(String.valueOf(id)).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                task.addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(c,"COIN DELETED", Toast.LENGTH_LONG).show();
                        notifyDataSetChanged();
                        Intent intent = new Intent( c, MyLibraryActivity.class);
                        intent.putExtra("accountType", "GOOGLE");
                        intent.putExtra("auth", true);
                        ((Activity)c).startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(c,"FAILED TO ADD COIN", Toast.LENGTH_LONG).show();
                    }
                });

            }

        });

    }

}