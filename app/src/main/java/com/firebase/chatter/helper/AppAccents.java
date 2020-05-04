package com.firebase.chatter.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class AppAccents {

    private String accentColor;
    private String textColor;
    private String titleTextColor;
    private SharedPreferences sharedPreferences;

    public AppAccents(Context context) {
        sharedPreferences = context.getSharedPreferences("Chatter_Accents",Context.MODE_PRIVATE);
    }

    public void init(){

        String accent = sharedPreferences.getString("accent", "");
        String tcolor = sharedPreferences.getString("textColor","");
        String ttcolor = sharedPreferences.getString("titleColor","");

        if (accent.equals("") || tcolor.equals("") || ttcolor.equals("")){
            setDefault();
            return;
        }

        setAccentColor(accent);
        setTextColor(tcolor);
        setTitleTextColor(ttcolor);
    }

    public void setDefault(){

        String accent = "#9ab7d3";
        String text = "#FFFFFF";
        String title = "#000000";

        this.accentColor = accent;
        this.textColor = text;
        this.titleTextColor = title;

        sharedPreferences.edit().putString("accent",accent).apply();
        sharedPreferences.edit().putString("textColor",text).apply();
        sharedPreferences.edit().putString("titleColor",title).apply();

    }

    public String getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
        sharedPreferences.edit().putString("accent",accentColor).apply();
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
        sharedPreferences.edit().putString("textColor",textColor).apply();
    }

    public String getTitleTextColor() {
        return titleTextColor;
    }

    public void setTitleTextColor(String titleTextColor) {
        this.titleTextColor = titleTextColor;
        sharedPreferences.edit().putString("titleColor",titleTextColor).apply();
    }
}
