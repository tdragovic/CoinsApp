package com.example.coinsapp;

import android.graphics.Bitmap;
import android.os.Build;
import androidx.annotation.RequiresApi;

import org.json.JSONObject;

import java.util.Objects;

public class Coin {

    private int index;
    private String name;
    private String country;
    private String desc;
    private String year;
    private String reverse;
    private String obverse;
    private float prices;
    private String alignment;
    private String thickness;
    private String shape;
    private String revCodes;
    private int obvCodes;
    private String weight;
    private String diameter;
    private String material;
    private Bitmap imageBitmap;

    public Coin() {
    }

    public Coin(int index, String name, String country, String desc, String year,
                String reverse, String obverse, float prices, String alignment,
                String thickness, String shape, String revCodes, int obvCodes,
                String weight, String diameter, String material) {

        this.index = index;
        this.name = name;
        this.country = country;
        this.desc = desc;
        this.year = year;
        this.reverse = reverse;
        this.obverse = obverse;
        this.prices = prices;
        this.alignment = alignment;
        this.thickness = thickness;
        this.shape = shape;
        this.revCodes = revCodes;
        this.obvCodes = obvCodes;
        this.weight = weight;
        this.diameter = diameter;
        this.material = material;
    }

    public Coin(Coin coin){

        this.index = coin.getIndex();
        this.name = coin.getName();
        this.country = coin.getCountry();
        this.desc = coin.getDesc();
        this.year = coin.getYear();
        this.reverse = coin.getReverse();
        this.obverse = coin.getObverse();
        this.prices = coin.getPrices();
        this.alignment = coin.getAlignment();
        this.thickness = coin.getThickness();
        this.shape = coin.getShape();
        this.revCodes = coin.getRevCodes();
        this.obvCodes = coin.getObvCodes();
        this.weight = coin.getWeight();
        this.diameter = coin.getDiameter();
        this.material = coin.getMaterial();
    }

    @Override
    public String toString() {
        return "{" +
                "index=" + index +
                ", name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", desc='" + desc + '\'' +
                ", year='" + year + '\'' +
                ", reverse='" + reverse + '\'' +
                ", obverse='" + obverse + '\'' +
                ", prices=" + prices +
                ", alignment='" + alignment + '\'' +
                ", thickness='" + thickness + '\'' +
                ", shape='" + shape + '\'' +
                ", revCodes='" + revCodes + '\'' +
                ", obvCodes=" + obvCodes +
                ", weight='" + weight + '\'' +
                ", diameter='" + diameter + '\'' +
                ", material='" + material + '\'' +
                '}';
    }

    public void setImageBitmap(Bitmap imageBitmap){ this.imageBitmap = imageBitmap;}

    public Bitmap getImageBitmap(){ return this.imageBitmap; }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getReverse() {
        return reverse;
    }

    public void setReverse(String reverse) {
        this.reverse = reverse;
    }

    public String getObverse() {
        return obverse;
    }

    public void setObverse(String obverse) {
        this.obverse = obverse;
    }

    public float getPrices() {
        return prices;
    }

    public void setPrices(float prices) {
        this.prices = prices;
    }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    public String getThickness() {
        return thickness;
    }

    public void setThickness(String thickness) {
        this.thickness = thickness;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public String getRevCodes() {
        return revCodes;
    }

    public void setRevCodes(String revCodes) {
        this.revCodes = revCodes;
    }

    public int getObvCodes() {
        return obvCodes;
    }

    public void setObvCodes(int obvCodes) {
        this.obvCodes = obvCodes;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getDiameter() {
        return diameter;
    }

    public void setDiameter(String diameter) {
        this.diameter = diameter;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coin coin = (Coin) o;
        return index == coin.index &&
                Float.compare(coin.prices, prices) == 0 &&
                obvCodes == coin.obvCodes &&
                name.equals(coin.name) &&
                country.equals(coin.country) &&
                desc.equals(coin.desc) &&
                year.equals(coin.year) &&
                reverse.equals(coin.reverse) &&
                obverse.equals(coin.obverse) &&
                alignment.equals(coin.alignment) &&
                thickness.equals(coin.thickness) &&
                shape.equals(coin.shape) &&
                revCodes.equals(coin.revCodes) &&
                weight.equals(coin.weight) &&
                diameter.equals(coin.diameter) &&
                material.equals(coin.material) &&
                Objects.equals(imageBitmap, coin.imageBitmap);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(index, name, country, desc, year, reverse, obverse, prices, alignment, thickness, shape, revCodes, obvCodes, weight, diameter, material, imageBitmap);
    }
}
