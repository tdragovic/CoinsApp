package com.example.coinsapp;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class AdapterAllCoins extends RecyclerView.Adapter<AdapterAllCoins.AllCoinsViewHolder> {

    public class AllCoinsViewHolder extends RecyclerView.ViewHolder{
        public AllCoinsViewHolder(View itemView){
            super(itemView);
        }
    }

    @NonNull
    @Override
    public AllCoinsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull AllCoinsViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
