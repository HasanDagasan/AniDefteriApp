package com.hasandagasan.anidefteri;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.hasandagasan.anidefteri.classes.GetTypeFace;

public class editFragment extends Fragment {

    // Argument'ler için sabitler (Key'ler)
    private static final String ARG_METIN = "metin";
    private static final String ARG_FAVORI = "favori";
    private static final String ARG_HATIRLATICI = "hatirlatici"; // YENİ

    // Sınıf değişkenleri
    private String orijinalMetin;
    private boolean favoriDurumu;
    private boolean hatirlaticiDurumu; // YENİ

    // View elemanları
    private EditText editTextNot;
    private CheckBox checkboxFavori;
    private CheckBox checkboxHatirlatici; // YENİ
    private Button btnKaydet;
    private AdView adView;

    // Fragment'ı oluşturmak için kullanılan fabrika metodu
    public static editFragment newInstance(String metin, boolean favori, boolean hatirlaticiVar) { // YENİ parametre
        editFragment fragment = new editFragment();
        Bundle args = new Bundle();
        args.putString(ARG_METIN, metin);
        args.putBoolean(ARG_FAVORI, favori);
        args.putBoolean(ARG_HATIRLATICI, hatirlaticiVar); // YENİ
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orijinalMetin = getArguments().getString(ARG_METIN);
            favoriDurumu = getArguments().getBoolean(ARG_FAVORI);
            hatirlaticiDurumu = getArguments().getBoolean(ARG_HATIRLATICI); // YENİ
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit, container, false);

        // View elemanlarını bağla
        adView = view.findViewById(R.id.adViewFragment);
        editTextNot = view.findViewById(R.id.editMetin);
        checkboxFavori = view.findViewById(R.id.checkFavori);
        checkboxHatirlatici = view.findViewById(R.id.checkHatirlatici); // YENİ: ID'yi XML'deki ile eşleştir
        btnKaydet = view.findViewById(R.id.kaydetButton);

        // Reklamı yükle
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        // Gelen verilerle arayüzü doldur
        editTextNot.setText(orijinalMetin);
        checkboxFavori.setChecked(favoriDurumu);
        checkboxHatirlatici.setChecked(hatirlaticiDurumu); // YENİ

        // Kaydet butonunun tıklama olayını ayarla
        btnKaydet.setOnClickListener(view1 -> {
            if (editTextNot.getText().toString().trim().isEmpty()) {
                Toast.makeText(getContext(), "Lütfen bir metin girin", Toast.LENGTH_SHORT).show();
                return;
            }
            kaydetVeKapat();
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Arayüz oluşturulduktan sonra font ve renk ayarlarını uygula
        yenidenYukle();
    }

    private void kaydetVeKapat() {
        try {
            String yeniMetin = editTextNot.getText().toString();
            boolean yeniFavori = checkboxFavori.isChecked();
            boolean yeniHatirlatici = checkboxHatirlatici.isChecked(); // YENİ

            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity == null) return;

            // 1. Önce metin ve favori durumunu her zaman güncelle
            mainActivity.guncelleNot(orijinalMetin, yeniMetin, yeniFavori);

            // 2. Hatırlatıcı durumunu kontrol et
            if (yeniHatirlatici) {
                // Eğer hatırlatıcı seçiliyse, hatırlatıcı diyalogunu göster.
                // Not metni güncellenmiş olabileceğinden 'yeniMetin'i gönderiyoruz.
                mainActivity.showReminderDialog(yeniMetin);
            } else {
                // Eğer hatırlatıcı seçili değilse, mevcut hatırlatıcıyı (varsa) sil.
                MainActivity.removeReminderFromJson(getContext(), yeniMetin);
            }

            mainActivity.mouseClickSound();

            // Fragment'ı kapat
            getActivity().getSupportFragmentManager().popBackStack();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Font ve renk ayarlarını uygulayan metot
    public void yenidenYukle() {
        if (getContext() == null || getView() == null) return;

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int selectedColor = sharedPreferences.getInt("selectedColor", Color.BLACK);
        String selectedFont = sharedPreferences.getString("selectedFont", "Sans Serif");
        GetTypeFace typeFace = new GetTypeFace();
        Typeface typeface = typeFace.getTypefaceFromFontName(getContext(), selectedFont);

        editTextNot.setHintTextColor(selectedColor);
        editTextNot.setTextColor(selectedColor);
        editTextNot.setTypeface(typeface);

        checkboxFavori.setTextColor(selectedColor);
        checkboxFavori.setTypeface(typeface);

        // YENİ: Hatırlatıcı CheckBox'ını da güncelle
        checkboxHatirlatici.setTextColor(selectedColor);
        checkboxHatirlatici.setTypeface(typeface);

        btnKaydet.setTextColor(selectedColor);
        btnKaydet.setTypeface(typeface);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Ana ekrandaki butonların durumunu tekrar ayarla
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setupButonlar();
        }
    }
}
