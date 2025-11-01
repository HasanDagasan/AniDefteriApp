package com.hasandagasan.anidefteri.classes;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.hasandagasan.anidefteri.FontAdapter;
import com.hasandagasan.anidefteri.MainActivity;
import com.hasandagasan.anidefteri.R;
import com.hasandagasan.anidefteri.editFragment;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;

import yuku.ambilwarna.AmbilWarnaDialog;

public class OptionMenuActions {
    private final MainActivity activity;
    public OptionMenuActions(MainActivity activity) {
        this.activity = activity;
    }
    public void yaziRengiSec(Context context) {
        int defaultColor = Color.BLACK;
        new AmbilWarnaDialog(context, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                activity.mouseClickSound();
                activity.kaydetVeUygula("selectedColor", color, null, null);
                activity.loadPreferences();
                activity.tumFragmentleriYenidenYukle();
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                activity.mouseClickSound();
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
            activity.mouseClickSound();
            String selectedFont = fontList[which];
            GetTypeFace typeFace = new GetTypeFace();
            typeFace.getTypefaceFromFontName(context, selectedFont);
            activity.kaydetVeUygula("selectedFont", null, selectedFont, null);
            activity.loadPreferences();
            activity.tumFragmentleriYenidenYukle();
        });

        builder.show();
    }
    public void sesAyarSec(Boolean sesAyar) {
        activity.kaydetVeUygula("sesAcik", null, null, sesAyar);
    }
    public void showOptionsDialogForNote(String metin, boolean isFavorite, boolean hatirlatici) {
        activity.mouseClickSound();

        boolean hatirlaticiVar = activity.hatirlaticiVarMi(metin);

        final String[] options = {
                "Düzenle",
                "Sil",
                isFavorite ? "Favorilerden Kaldır" : "Favorilere Ekle",
                hatirlaticiVar ? "Hatırlatmayı Düzenle/Kaldır" : "Hatırlatma Ekle"
        };

        new AlertDialog.Builder(activity)
                .setTitle("Ne yapmak istersin?")
                .setItems(options, (dialog, item) -> {
                    if (options[item].equals("Düzenle")) {
                        // --- Düzenleme işlemi ---
                        activity.mouseClickSound();
                        activity.getSupportFragmentManager().popBackStack(null, activity.getSupportFragmentManager().POP_BACK_STACK_INCLUSIVE);
                        editFragment editFragment = com.hasandagasan.anidefteri.editFragment.newInstance(metin, isFavorite, hatirlatici);
                        activity.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, editFragment)
                                .addToBackStack(null)
                                .commit();

                    } else if (options[item].equals("Sil")) {
                        // --- Silme işlemi ---
                        new AlertDialog.Builder(activity)
                                .setTitle("Silme Onayı")
                                .setMessage("Bu öğeyi silmek istediğinize emin misiniz?")
                                .setPositiveButton("Evet", (d, w) -> {
                                    activity.mouseClickSound();
                                    activity.notuSil(metin);
                                    Toast.makeText(activity, "Silindi", Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("Hayır", (d, w) -> activity.mouseClickSound())
                                .show();

                    } else if (options[item].equals("Hatırlatma Ekle") || options[item].equals("Hatırlatmayı Düzenle/Kaldır")) {
                        activity.showReminderDialog(metin);
                    } else {
                        activity.mouseClickSound();
                        activity.toggleFavorite(metin, !isFavorite);
                    }
                })

                .show();
    }
}
