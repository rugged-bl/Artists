package com.example.artists.model;

import java.util.ArrayList;

public class FormatString {
    public static String startWithUpperCase(String str) {
        if (str.isEmpty()) return "";

        String firstLetter = str.substring(0, 1);
        StringBuilder stringBuilder = new StringBuilder(str);
        stringBuilder.replace(0, 1, firstLetter.toUpperCase());

        return stringBuilder.toString();
    }

    public static String formatGenres(ArrayList<String> genres) {
        StringBuilder str = new StringBuilder();

        if (genres.isEmpty()) {
            return str.toString();
        }
        str.append(genres.get(0));
        for (int i = 1; i < genres.size(); i++) {
            str.append(", ").append(genres.get(i));
        }

        return str.toString();
    }

    public static String formatAlbDeclination(int count, String def) {
        String str = count + " " + def;

        int countLastDigit = count % 10;
        if (countLastDigit >= 5 || countLastDigit == 0)
            str += "ов";
        else if (countLastDigit >= 1)
            str += "а";

        return str;
    }
}
