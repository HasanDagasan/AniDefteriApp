package com.hasandagasan.anidefteri;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
    MyListAdapter adapter;
    TextView textView;
    private androidx.appcompat.view.ActionMode actionMode;
    private AdView adView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        adView = view.findViewById(R.id.adViewFragment);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        listView = view.findViewById(R.id.listView);
        textView = view.findViewById(R.id.textViewSozYok);
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int selectedColor = sharedPreferences.getInt("selectedColor", Color.BLACK);
        String selectedFont = sharedPreferences.getString("selectedFont", "Sans Serif");
        GetTypeFace typeFace = new GetTypeFace();
        Typeface typeface = typeFace.getTypefaceFromFontName(getContext(), selectedFont);

        textView.setTypeface(typeface);
        textView.setTextColor(selectedColor);

        androidx.appcompat.view.ActionMode.Callback actionModeCallback = new androidx.appcompat.view.ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.multiple_selection, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(androidx.appcompat.view.ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.action_delete) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Silme Onayı")
                            .setMessage("Bu öğeyi silmek istediğinize emin misiniz?")
                            .setPositiveButton("Evet", (dialog, which) -> {
                                ((MainActivity) getActivity()).mouseClickSound();
                                secilenOgeleriSil();
                                mode.finish();
                                Toast.makeText(requireContext(), "Silindi", Toast.LENGTH_SHORT).show();
                                if(liste == null || liste.isEmpty()){
                                    textView.setVisibility(View.VISIBLE);
                                }
                            })
                            .setNegativeButton("Hayır",(dialog, which) -> {
                                ((MainActivity) getActivity()).mouseClickSound();
                            })
                            .show();
                    ((MainActivity) getActivity()).mouseClickSound();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(androidx.appcompat.view.ActionMode mode) {
                adapter.clearSelection();
                actionMode = null;
            }
        };
        if (getArguments() != null) {
            liste = getArguments().getStringArrayList("liste");
            adapter = new MyListAdapter(getContext(), liste, selectedColor, typeface, true);

            // Favori değişikliği callback'ini ayarla
            adapter.setOnFavoriteChangedListener((position, isFavorite) -> {
                String metin = liste.get(position);
                String temizMetin = metin.startsWith("★") ? metin.substring(1).trim() : metin;
                temizMetin = temizMetin.replaceAll("\\(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}\\)", "").trim();

                jsonDosyasindaFavoriGuncelle(temizMetin, isFavorite);
            });
            listView.setAdapter(adapter);

            if(liste == null || liste.isEmpty()){
                textView.setVisibility(View.VISIBLE);
            }

            listView.setOnItemClickListener((parent, view1, position, id) -> {
                /*
                if (actionMode == null) {
                    String secilenMetin = liste.get(position);
                    boolean favoriDurumu = secilenMetin.startsWith("★");

                    String temizMetin = favoriDurumu ? secilenMetin.substring(1).trim() : secilenMetin;
                    temizMetin = temizMetin.replaceAll("\\(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}\\)", "").trim();

                    getActivity().getSupportFragmentManager().popBackStack();

                    ((MainActivity) getActivity()).mouseClickSound();
                    editFragment editFragment = com.hasandagasan.anidefteri.editFragment.newInstance(temizMetin, favoriDurumu);
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, editFragment)
                                .addToBackStack(null)
                                .commit();

                    }
                } else {
                        adapter.toggleSelection(position);
                        updateActionModeTitle();
                }*/

                if (actionMode == null) {
                    showOptionsDialog(position);
                } else {
                    // ActionMode aktif ise çoklu seçime devam et.
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
    private void showOptionsDialog(int position) {
        if (getActivity() == null) return; // Güvenlik kontrolü

        // Tıklanan öğenin metnini ve favori durumunu alalım.
        String secilenMetin = liste.get(position);
        boolean isFavorite = secilenMetin.startsWith("★");

        // "Favorilere Ekle" veya "Favorilerden Kaldır" metnini belirle
        String favoriSecenegi = isFavorite ? "Favorilerden Kaldır" : "Favorilere Ekle";

        // Seçenekler dizisini oluştur
        final CharSequence[] options = {"Düzenle", "Sil", favoriSecenegi};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Seçenekler");
        builder.setItems(options, (dialog, item) -> {
            // Hangi seçeneğe tıklandığını kontrol et
            if (options[item].equals("Düzenle")) {
                // --- DÜZENLEME İŞLEMİ ---
                // Bu kod bloğu zaten onItemClickListener içinde vardı, buraya taşıdık.
                ((MainActivity) getActivity()).mouseClickSound();

                // Metni ve favori durumunu editFragment'e gönder
                String temizMetin = isFavorite ? secilenMetin.substring(1).trim() : secilenMetin;
                temizMetin = temizMetin.replaceAll("\\(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}\\)", "").trim();

                getActivity().getSupportFragmentManager().popBackStack(); // ListFragment'ı kapat

                editFragment editFragment = com.hasandagasan.anidefteri.editFragment.newInstance(temizMetin, isFavorite);
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, editFragment)
                        .addToBackStack(null)
                        .commit();

            } else if (options[item].equals("Sil")) {
                // --- SİLME İŞLEMİ ---
                // Çoklu silme yerine tekli silme için onay dialog'u gösterelim
                new AlertDialog.Builder(requireContext())
                        .setTitle("Silme Onayı")
                        .setMessage("Bu öğeyi silmek istediğinize emin misiniz?")
                        .setPositiveButton("Evet", (d, w) -> {
                            ((MainActivity) getActivity()).mouseClickSound();

                            // JSON dosyasından ve listeden silme işlemi
                            String silinecekMetin = liste.get(position);
                            jsonDosyasindanSil(silinecekMetin); // JSON'dan sil
                            liste.remove(position); // ArrayList'ten sil
                            adapter.notifyDataSetChanged(); // Adaptörü güncelle

                            Toast.makeText(requireContext(), "Silindi", Toast.LENGTH_SHORT).show();

                            // Eğer liste boşaldıysa "Söz Yok" metnini göster
                            if(liste.isEmpty()){
                                textView.setVisibility(View.VISIBLE);
                            }
                        })
                        .setNegativeButton("Hayır", (d, w) -> ((MainActivity) getActivity()).mouseClickSound())
                        .show();

            } else if (options[item].equals(favoriSecenegi)) {
                // --- FAVORİ EKLEME/KALDIRMA İŞLEMİ ---
                ((MainActivity) getActivity()).mouseClickSound();

                // Adapter'daki favori değiştirme metodunu tetikle
                adapter.toggleFavorite(position);
            }
        });
        builder.show(); // Dialog penceresini göster
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
            Log.d("silinecek metin", "silinecek metin" + ": " + silinecekMetin);
            // JSON dosyasından da sil
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

            // JSON dosyasını oku
            StringBuilder jsonString = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            reader.close();

            JSONArray sozlerArray = new JSONArray(jsonString.toString());

            // İlgili metni bul ve favori durumunu güncelle
            for (int i = 0; i < sozlerArray.length(); i++) {
                JSONObject sozObj = sozlerArray.getJSONObject(i);
                if (sozObj.getString("metin").trim().equals(metin)) {
                    sozObj.put("favori", favoriDurumu);
                    Log.d("JSON_FAVORI", "Favori güncellendi: " + metin + " -> " + favoriDurumu);
                    break;
                }
            }

            // Güncellenmiş JSON'u dosyaya yaz
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