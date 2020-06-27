package com.example.coinsapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class AllCoinsAdapter extends ArrayAdapter<Coin> {

    private ArrayList<Coin> allCoins;
    private Context c;

    public AllCoinsAdapter(Context context, ArrayList<Coin> allCoins) {
        super(context, 0, allCoins);

        this.c = context;
        this.allCoins = new ArrayList<Coin>(allCoins);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View coin = convertView;

        if(coin==null){
            coin = LayoutInflater.from(getContext()).inflate(R.layout.list_item_coin, parent, false);
        }

        final Coin currentCoin = getItem(position);

        TextView coinName = (TextView) coin.findViewById(R.id.allCoinName);
        TextView coinPrice = (TextView) coin.findViewById(R.id.allCoinPrice);
        TextView coinYear = (TextView) coin.findViewById(R.id.allCoinYear);
        ImageView coinImage = (ImageView) coin.findViewById(R.id.imageViewAllCoinsItem);

        coinName.setText(currentCoin.getName());
        coinPrice.setText(String.valueOf(currentCoin.getPrices()));
        coinYear.setText(currentCoin.getYear());
        coinImage.setImageBitmap(currentCoin.getImageBitmap());

//        coin.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//
//                byte[] xx = getByteArray(currentCoin.getImageBitmap());
//                Intent newIntent = new Intent( c, CoinInfoActivity.class);
//                newIntent.putExtra("accountType", "GOOGLE");
//                newIntent.putExtra("auth", true);
//                newIntent.putExtra("capturedPicture","");
//                newIntent.putExtra("clickedCoin", currentCoin.toString());
//                newIntent.putExtra("clickedCoinBitmap", xx);
//                ((Activity)c).startActivity(newIntent);
//
//
//            }
//        });

//        currentCoin.setOnItemClickListener(new AdapterView.OnItemClickListener(){
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                System.out.println("CURRENTITEM" + parent.getSelectedItem());
//                Coin currentCoin = (Coin) parent.getSelectedItem();
//                System.out.println("CURRENTCOIN" + currentCoin);
//                byte[] xx = getByteArray(currentCoin.getImageBitmap());
//                Intent newIntent = new Intent( AllCoinsActivity.this, CoinInfoActivity.class);
//                newIntent.putExtra("accountType", clientType);
//                newIntent.putExtra("auth", auth);
//                newIntent.putExtra("capturedPicture","");
//                newIntent.putExtra("clickedCoin", currentCoin.toString());
//                newIntent.putExtra("clickedCoinBitmap", xx);
//                ((Activity)AllCoinsActivity.this).startActivity(newIntent);
//
//            }
//        });
        return coin;
    }



    public byte[] getByteArray(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        return byteArray;
    }

    @Nullable
    @Override
    public Coin getItem(int position) {
        return super.getItem(position);
    }
}
