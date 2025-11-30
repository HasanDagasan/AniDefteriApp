package com.hasandagasan.anidefteri;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.hasandagasan.anidefteri.classes.GetTypeFace;
import com.hasandagasan.anidefteri.classes.OptionMenuActions;
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
    private Button filterButton;
    private Button deleteButton;
    private int aktifFiltre = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        listView = view.findViewById(R.id.listView);
        textView = view.findViewById(R.id.textViewSozYok);
        filterButton = view.findViewById(R.id.filterButton);
        deleteButton = view.findViewById(R.id.deleteButton);

        MainActivity mainActivity = (MainActivity) getActivity();
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
            if (mainActivity == null) return;
            mainActivity.mouseClickSound();
            new AlertDialog.Builder(requireContext())
                    .setTitle("Silme Onayı")
                    .setMessage("Seçili öğeleri silmek istediğinize emin misiniz?")
                    .setPositiveButton("Evet", (dialog, which) -> {
                        mainActivity.mouseClickSound();
                        secilenOgeleriSil(); // Bu metot artık MainActivity'yi çağıracak
                        if (actionMode != null) {
                            actionMode.finish();
                        }
                        Toast.makeText(requireContext(), "Silindi", Toast.LENGTH_SHORT).show();
                        if (liste == null || liste.isEmpty()) {
                            textView.setVisibility(View.VISIBLE);
                        }
                    })
                    .setNegativeButton("Hayır", (dialog, which) -> mainActivity.mouseClickSound())
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
                String temizMetin = temizleVeMetniAl(metin);

                // --- DEĞİŞİKLİK: Favori güncelleme işlemi için MainActivity'deki metodu çağır ---
                if (mainActivity != null) {
                    mainActivity.toggleFavorite(temizMetin, isFavorite);
                }

                // Lokal listeyi güncelle (MainActivity zaten tüm fragment'ları güncelleyecek ama anlık tepki için)
                guncelleTumListe(metin, isFavorite);
                if (aktifFiltre != 0) {
                    filtreUygula(aktifFiltre);
                }
            });
            listView.setAdapter(adapter);

            if (liste == null || liste.isEmpty()) {
                textView.setVisibility(View.VISIBLE);
            }

            listView.setOnItemClickListener((parent, view1, position, id) -> {
                if (actionMode == null) {
                    // --- DEĞİŞİKLİK: OptionMenuActions'ı kullan ---
                    showOptionsDialog(position);
                } else {
                    adapter.toggleSelection(position);
                    updateActionModeTitle();
                }
            });

            listView.setOnItemLongClickListener((parent, view1, position, id) -> {
                if (actionMode == null) {
                    actionMode = ((AppCompatActivity) requireActivity()).startSupportActionMode(actionModeCallback);
                    if (mainActivity != null) mainActivity.setActionMode(actionMode);
                    adapter.toggleSelection(position);
                    updateActionModeTitle();
                }
                return true;
            });
        }
        return view;
    }
    private void showFilterDialog() {
        final CharSequence[] filterOptions = {"Filtre Yok", "Favoriler", "Hatırlatıcı Olanlar"};

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
    private void filtreUygula(int filterType) {    liste.clear();
        // MainActivity referansını alalım, çünkü kontrol metodu orada.
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null) return; // Activity mevcut değilse işlemi durdur.

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
                filterButton.setText("Hatırlatıcı Olanlar");
                for (String item : tumListe) {
                    String temizMetin = temizleVeMetniAl(item);
                    if (mainActivity.hatirlaticiVarMi(temizMetin)) {
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
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null) return;

        String secilenMetin = liste.get(position);
        boolean isFavorite = secilenMetin.startsWith("★");
        String temizMetin = temizleVeMetniAl(secilenMetin);

        boolean hatirlaticiVar = mainActivity.hatirlaticiVarMi(temizMetin);
        OptionMenuActions optionMenu = new OptionMenuActions(mainActivity);
        optionMenu.showOptionsDialogForNote(temizMetin, isFavorite, hatirlaticiVar);
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
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null) return;

        ArrayList<Integer> secilenPozisyonlar = adapter.getSelectedItems();
        Collections.sort(secilenPozisyonlar, Collections.reverseOrder());

        for (int pozisyon : secilenPozisyonlar) {
            String silinecekMetin = liste.get(pozisyon);
            String temizMetin = temizleVeMetniAl(silinecekMetin);

            // --- DEĞİŞİKLİK: Silme işlemi için MainActivity'deki metodu çağır ---
            mainActivity.notuSil(temizMetin);

            // Lokal listeleri de anlık olarak güncelle
            liste.remove(pozisyon);
            tumListe.remove(silinecekMetin);
        }
        adapter.notifyDataSetChanged();
    }
    private String temizleVeMetniAl(String tamMetin) {
        String temizMetin = tamMetin.startsWith("★") ? tamMetin.substring(1).trim() : tamMetin;
        return temizMetin.replaceAll("\\s*\\(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}\\)$", "").trim();
    }
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    public void yenidenYukle() {
        if (getContext() == null || getView() == null) return;

        // MainActivity'den en güncel listeyi al
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null) return;

        // Fragment'ın kendi listelerini MainActivity'deki güncel listeyle değiştir
        this.tumListe = new ArrayList<>(mainActivity.gosterilecekListe); // MainActivity'deki listenin adı

        // Filtre durumuna göre gösterilecek listeyi güncelle
        filtreUygula(aktifFiltre);

        // Arayüz ayarlarını (renk, font) yükle
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int selectedColor = sharedPreferences.getInt("selectedColor", Color.BLACK);
        String selectedFont = sharedPreferences.getString("selectedFont", "Sans Serif");
        GetTypeFace typeFace = new GetTypeFace();
        Typeface typeface = typeFace.getTypefaceFromFontName(getContext(), selectedFont);

        // Adaptörü yeniden oluştur veya güncelle

            adapter = new MyListAdapter(getContext(), this.liste, selectedColor, typeface, true);
            listView.setAdapter(adapter);

            // Sadece adaptörün içindeki verinin değiştiğini bildir
            adapter.notifyDataSetChanged();

        // Diğer UI elemanlarını güncelle
        textView.setTypeface(typeface);
        textView.setTextColor(selectedColor);
    }
}