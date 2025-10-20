package com.hasandagasan.anidefteri.classes;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import com.hasandagasan.anidefteri.FontAdapter;
import com.hasandagasan.anidefteri.MainActivity;
import yuku.ambilwarna.AmbilWarnaDialog;

public class OptionMenuActions {
    private MainActivity mainActivity;
    public OptionMenuActions(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void yaziRengiSec(Context context) {
        int defaultColor = Color.BLACK;
        new AmbilWarnaDialog(context, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                mainActivity.mouseClickSound();
                mainActivity.kaydetVeUygula("selectedColor", color, null, null);
                mainActivity.loadPreferences();
                mainActivity.tumFragmentleriYenidenYukle();
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                mainActivity.mouseClickSound();
            }
        }).show();
    }
    public void yaziTipiSec(Context context) {
        String[] fontList = {"Sans Serif", "AguDisplay", "Cookie", "DancingScript",
                "Lobster", "Play", "RubikVinyl", "VujahdayScript"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Yazı Tipi Seçin");

        FontAdapter adapter = new FontAdapter(context, fontList);
        builder.setAdapter(adapter, (dialog, which) -> {
            mainActivity.mouseClickSound();
            String selectedFont = fontList[which];
            GetTypeFace typeFace = new GetTypeFace();
            typeFace.getTypefaceFromFontName(context, selectedFont);
            mainActivity.kaydetVeUygula("selectedFont", null, selectedFont, null);
            mainActivity.loadPreferences();
            mainActivity.tumFragmentleriYenidenYukle();
        });

        builder.show();
    }
    public void sesAyarSec(Boolean sesAyar) {
        mainActivity.kaydetVeUygula("sesAcik", null, null, sesAyar);
    }
}
