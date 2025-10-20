package com.hasandagasan.anidefteri;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FontAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] fontList;

    public FontAdapter(Context context, String[] fontList) {
        super(context, android.R.layout.simple_list_item_1, fontList);
        this.context = context;
        this.fontList = fontList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        String fontName = fontList[position];
        textView.setText(fontName);

        // Yazı tipini ayarla
        Typeface typeface = getTypefaceFromFontName(fontName);
        textView.setTypeface(typeface);

        return convertView;
    }

    // Yazı tiplerini buradan döndür
    private Typeface getTypefaceFromFontName(String fontName) {
        switch (fontName) {
            case "AguDisplay":
                return Typeface.createFromAsset(context.getAssets(), "fonts/agudisplay.ttf");
            case "Cookie":
                return Typeface.createFromAsset(context.getAssets(), "fonts/cookie.ttf");
            case "DancingScript":
                return Typeface.createFromAsset(context.getAssets(), "fonts/dancing.ttf");
            case "Lobster":
                return Typeface.createFromAsset(context.getAssets(), "fonts/lobster.ttf");
            case "Play":
                return Typeface.createFromAsset(context.getAssets(), "fonts/play.ttf");
            case "RubikVinyl":
                return Typeface.createFromAsset(context.getAssets(), "fonts/rubik.ttf");
            case "VujahdayScript":
                return Typeface.createFromAsset(context.getAssets(), "fonts/vujahday.ttf");
            default:
                return Typeface.SANS_SERIF;
        }
    }
}
