package com.example.coinsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;


public class SimilarCoinAdapter extends ArrayAdapter<Coin> {

    private ArrayList<Coin> similarCoins;
    private Context c;

    public SimilarCoinAdapter(Context context, ArrayList<Coin> similarCoins) {
        super(context, 0, similarCoins);

        this.c = context;
        this.similarCoins = new ArrayList<Coin>(similarCoins);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View similarCoinView = convertView;

        if(similarCoinView==null){
            similarCoinView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_similar_coin, parent, false);
        }

        Coin currentCoin = getItem(position);

        TextView coinName = (TextView) similarCoinView.findViewById(R.id.similarCoinName);
        TextView coinPrice = (TextView) similarCoinView.findViewById(R.id.similarCoinPrice);
        TextView coinYear = (TextView) similarCoinView.findViewById(R.id.similarCoinYear);

        coinName.setText(currentCoin.getName());
        coinPrice.setText(String.valueOf(currentCoin.getPrices()));
        coinYear.setText(currentCoin.getYear());

        return similarCoinView;
    }

}
