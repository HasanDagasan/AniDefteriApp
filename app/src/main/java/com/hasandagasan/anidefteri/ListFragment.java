package com.hasandagasan.anidefteri;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.hasandagasan.anidefteri.classes.GetTypeFace;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;

public class ListFragment extends Fragment {

    public ListFragment() {}
    ListView listView;
    ArrayList<String> liste;
    ArrayList<String> tumListe;
    MyListAdapter adapter;
    TextView textView;
    private androidx.appcompat.view.ActionMode actionMode;
    private AdView adView;
    private Button filterButton;
    private Button deleteButton;
    private int aktifFiltre = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        listView = view.findViewById(R.id.listView);
        textView = view.findViewById(R.id.textViewSozYok);
        filterButton = view.findViewById(R.id.filterButton);
        deleteButton = view.findViewById(R.id.deleteButton);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int selectedColor = sharedPreferences.getInt("selectedColor", Color.BLACK);
        String selectedFont = sharedPreferences.getString("selectedFont", "Sans Serif");
        GetTypeFace typeFace = new GetTypeFace();
        Typeface typeface = typeFace.getTypefaceFromFontName(getContext(), selectedFont);
        textView.setTypeface(typeface);
        textView.setTextColor(selectedColor);

        filterButton.setOnClickListener(v -> {
            ((MainActivity) getActivity()).mouseClickSound();
            showFilterDialog();
        });

        deleteButton.setOnClickListener(v -> {
            ((MainActivity) getActivity()).mouseClickSound();
            new AlertDialog.Builder(requireContext())
                    .setTitle("Silme Onayı")
                    .setMessage("Seçili öğeleri silmek istediğinize emin misiniz?")
                    .setPositiveButton("Evet", (dialog, which) -> {
                        ((MainActivity) getActivity()).mouseClickSound();
                        secilenOgeleriSil();
                        if (actionMode != null) {
                            actionMode.finish();
                        }
                        Toast.makeText(requireContext(), "Silindi", Toast.LENGTH_SHORT).show();
                        if(liste == null || liste.isEmpty()){
                            textView.setVisibility(View.VISIBLE);
                        }
                    })
                    .setNegativeButton("Hayır",(dialog, which) -> {
                        ((MainActivity) getActivity()).mouseClickSound();
                    })
                    .show();
        });

        androidx.appcompat.view.ActionMode.Callback actionModeCallback = new androidx.appcompat.view.ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(androidx.appcompat.view.ActionMode mode, android.view.Menu menu) {
                setFilterButtonVisible(false);
                setDeleteButtonVisible(true);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(androidx.appcompat.view.ActionMode mode, android.view.Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(androidx.appcompat.view.ActionMode mode, android.view.MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(androidx.appcompat.view.ActionMode mode) {
                adapter.clearSelection();
                actionMode = null;
                setFilterButtonVisible(true);
                setDeleteButtonVisible(false);
            }
        };

        if (getArguments() != null) {
            liste = getArguments().getStringArrayList("liste");
            tumListe = new ArrayList<>(liste);
            adapter = new MyListAdapter(getContext(), liste, selectedColor, typeface, true);

            adapter.setOnFavoriteChangedListener((position, isFavorite) -> {
                String metin = liste.get(position);
                String temizMetin = metin.startsWith("★") ? metin.substring(1).trim() : metin;
                temizMetin = temizMetin.replaceAll("\\(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}\\)", "").trim();

                jsonDosyasindaFavoriGuncelle(temizMetin, isFavorite);
                guncelleTumListe(metin, isFavorite);

                // Filtre aktifse tekrar uygula
                if (aktifFiltre != 0) {
                    filtreUygula(aktifFiltre);
                }
            });
            listView.setAdapter(adapter);

            if(liste == null || liste.isEmpty()){
                textView.setVisibility(View.VISIBLE);
            }

            listView.setOnItemClickListener((parent, view1, position, id) -> {
                if (actionMode == null) {
                    showOptionsDialog(position);
                } else {
                    adapter.toggleSelection(position);
                    updateActionModeTitle();
                }
            });

            listView.setOnItemLongClickListener((parent, view1, position, id) -> {
                if (actionMode == null) {
                    actionMode = ((AppCompatActivity) requireActivity()).startSupportActionMode(actionModeCallback);
                    ((MainActivity) getActivity()).setActionMode(actionMode);
                    adapter.toggleSelection(position);
                    updateActionModeTitle();
                }
                return true;
            });
        }
        return view;
    }

    private void showFilterDialog() {
        final CharSequence[] filterOptions = {"Filtre Yok", "Favoriler", "Favori Olmayanlar"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Filtre Seçin");
        builder.setSingleChoiceItems(filterOptions, aktifFiltre, (dialog, which) -> {
            aktifFiltre = which;
            filtreUygula(which);
            dialog.dismiss();
            ((MainActivity) getActivity()).mouseClickSound();
        });
        builder.show();
    }

    private void filtreUygula(int filterType) {
        liste.clear();

        switch (filterType) {
            case 0:
                liste.addAll(tumListe);
                filterButton.setText("Filtrele");
                break;
            case 1:
                filterButton.setText("Favoriler");
                for (String item : tumListe) {
                    if (item.startsWith("★")) {
                        liste.add(item);
                    }
                }
                break;
            case 2:
                filterButton.setText("Favori Olmayanlar");
                for (String item : tumListe) {
                    if (!item.startsWith("★")) {
                        liste.add(item);

                    }
                }
                break;
        }

        adapter.notifyDataSetChanged();

        if(liste.isEmpty()){
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.INVISIBLE);
        }
    }

    private void setFilterButtonVisible(boolean visible) {
        if (filterButton != null) {
            filterButton.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void setDeleteButtonVisible(boolean visible) {
        if (deleteButton != null) {
            deleteButton.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void guncelleTumListe(String eskiMetin, boolean yeniFavoriDurumu) {
        for (int i = 0; i < tumListe.size(); i++) {
            String item = tumListe.get(i);
            String temizItem = item.startsWith("★") ? item.substring(1).trim() : item;

            // Tarih kısmını da çıkaralım
            String temizItemMetin = temizItem.replaceAll("\\(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}\\)", "").trim();

            String temizEskiMetin = eskiMetin.startsWith("★") ? eskiMetin.substring(1).trim() : eskiMetin;
            String temizEskiMetinSadece = temizEskiMetin.replaceAll("\\(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}\\)", "").trim();

            if (temizItemMetin.equals(temizEskiMetinSadece)) {
                // Tarih kısmını koruyalım
                String tarihKismi = "";
                if (item.contains("(") && item.contains(")")) {
                    int start = item.indexOf("(");
                    int end = item.indexOf(")") + 1;
                    tarihKismi = "\n" + item.substring(start, end);
                }

                if (yeniFavoriDurumu) {
                    tumListe.set(i, "★ " + temizItemMetin + tarihKismi);
                } else {
                    tumListe.set(i, temizItemMetin + tarihKismi);
                }
                break;
            }
        }
    }

    private void showOptionsDialog(int position) {
        if (getActivity() == null) return;

        String secilenMetin = liste.get(position);
        boolean isFavorite = secilenMetin.startsWith("★");

        String favoriSecenegi = isFavorite ? "Favorilerden Kaldır" : "Favorilere Ekle";

        final CharSequence[] options = {"Düzenle", "Sil", favoriSecenegi};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Ne yapmak istersin");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Düzenle")) {
                ((MainActivity) getActivity()).mouseClickSound();

                String temizMetin = isFavorite ? secilenMetin.substring(1).trim() : secilenMetin;
                temizMetin = temizMetin.replaceAll("\\(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}\\)", "").trim();

                getActivity().getSupportFragmentManager().popBackStack();

                editFragment editFragment = com.hasandagasan.anidefteri.editFragment.newInstance(temizMetin, isFavorite);
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, editFragment)
                        .addToBackStack(null)
                        .commit();

            } else if (options[item].equals("Sil")) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Silme Onayı")
                        .setMessage("Bu öğeyi silmek istediğinize emin misiniz?")
                        .setPositiveButton("Evet", (d, w) -> {
                            ((MainActivity) getActivity()).mouseClickSound();

                            String silinecekMetin = liste.get(position);
                            jsonDosyasindanSil(silinecekMetin);
                            liste.remove(position);
                            tumListe.remove(silinecekMetin);
                            adapter.notifyDataSetChanged();

                            Toast.makeText(requireContext(), "Silindi", Toast.LENGTH_SHORT).show();

                            if(liste.isEmpty()){
                                textView.setVisibility(View.VISIBLE);
                            }
                        })
                        .setNegativeButton("Hayır", (d, w) -> ((MainActivity) getActivity()).mouseClickSound())
                        .show();

            } else if (options[item].equals(favoriSecenegi)) {
                ((MainActivity) getActivity()).mouseClickSound();
                adapter.toggleFavorite(position);

                // Favori değişikliğinden sonra filtre aktifse tekrar uygula
                if (aktifFiltre != 0) {
                    // toggleFavorite sonrası adapter otomatik güncellenir
                    // ama liste içeriği değişmez, o yüzden filtreyi tekrar uygulayalım
                    new android.os.Handler().postDelayed(() -> {
                        filtreUygula(aktifFiltre);
                    }, 100);
                }
            }
        });
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setupButonlar();
        }
    }

    private void updateActionModeTitle() {
        if (actionMode != null) {
            int secilenSayisi = adapter.getSelectedItemCount();
            if(secilenSayisi != 0){
                actionMode.setTitle(secilenSayisi + " öğe seçildi");
            }else{
                actionMode.finish();
            }
        }
    }

    private void secilenOgeleriSil() {
        ArrayList<Integer> secilenPozisyonlar = adapter.getSelectedItems();

        Collections.sort(secilenPozisyonlar, Collections.reverseOrder());

        for (int pozisyon : secilenPozisyonlar) {
            String silinecekMetin = liste.get(pozisyon);
            liste.remove(pozisyon);
            tumListe.remove(silinecekMetin);
            Log.d("silinecek metin", "silinecek metin" + ": " + silinecekMetin);
            jsonDosyasindanSil(silinecekMetin);
        }
        adapter.notifyDataSetChanged();
    }

    private void jsonDosyasindanSil(String silinecekMetin) {
        try {
            String FILE_DIR = "Metin";
            String FILE_NAME = "veriler.json";
            File file = new File(requireContext().getExternalFilesDir(FILE_DIR), FILE_NAME);

            if (!file.exists()) {
                Log.d("JSON_SIL", "Dosya bulunamadı: " + file.getAbsolutePath());
                return;
            }

            StringBuilder jsonString = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            reader.close();

            JSONArray sozlerArray = new JSONArray(jsonString.toString());

            String temizMetin = silinecekMetin.startsWith("★") ?
                    silinecekMetin.substring(1).trim() : silinecekMetin;
            temizMetin = temizMetin.replaceAll("\\(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}\\)", "").trim();

            for (int i = sozlerArray.length() - 1; i >= 0; i--) {
                JSONObject sozObj = sozlerArray.getJSONObject(i);
                if (sozObj.getString("metin").trim().equals(temizMetin)) {
                    sozlerArray.remove(i);
                    Log.d("JSON_SIL", "Öğe silindi. Kalan: " + sozlerArray.length());
                    break;
                }
            }

            FileWriter writer = new FileWriter(file);
            writer.write(sozlerArray.toString());
            writer.close();

            Log.d("JSON_SIL", "Dosya güncellendi");

        } catch (Exception e) {
            Log.e("JSON_SIL", "Hata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void jsonDosyasindaFavoriGuncelle(String metin, boolean favoriDurumu) {
        try {
            String FILE_DIR = "Metin";
            String FILE_NAME = "veriler.json";
            File file = new File(requireContext().getExternalFilesDir(FILE_DIR), FILE_NAME);

            if (!file.exists()) {
                Log.d("JSON_FAVORI", "Dosya bulunamadı: " + file.getAbsolutePath());
                return;
            }

            StringBuilder jsonString = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            reader.close();

            JSONArray sozlerArray = new JSONArray(jsonString.toString());

            for (int i = 0; i < sozlerArray.length(); i++) {
                JSONObject sozObj = sozlerArray.getJSONObject(i);
                if (sozObj.getString("metin").trim().equals(metin)) {
                    sozObj.put("favori", favoriDurumu);
                    Log.d("JSON_FAVORI", "Favori güncellendi: " + metin + " -> " + favoriDurumu);
                    break;
                }
            }

            FileWriter writer = new FileWriter(file);
            writer.write(sozlerArray.toString());
            writer.close();

            Log.d("JSON_FAVORI", "Dosya başarıyla güncellendi");

        } catch (Exception e) {
            Log.e("JSON_FAVORI", "Hata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    public void yenidenYukle() {
        if (getContext() == null || getView() == null) return;

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int selectedColor = sharedPreferences.getInt("selectedColor", Color.BLACK);
        String selectedFont = sharedPreferences.getString("selectedFont", "Sans Serif");
        GetTypeFace typeFace = new GetTypeFace();
        Typeface typeface = typeFace.getTypefaceFromFontName(getContext(), selectedFont);

        adapter = new MyListAdapter(getContext(), liste, selectedColor, typeface, false);
        listView.setAdapter(adapter);

        textView.setTypeface(typeface);
        textView.setTextColor(selectedColor);
    }
}