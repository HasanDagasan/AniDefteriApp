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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.hasandagasan.anidefteri.classes.GetTypeFace;

public class ekleFragment extends Fragment {

    CheckBox checkboxFavori;
    EditText editTextNot;
    Button btnKaydet;
    private AdView adView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private WindowManager getWindowManager() {
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ekle, container, false);

        adView = view.findViewById(R.id.adViewFragment);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        checkboxFavori = view.findViewById(R.id.ekleCheckFavori);
        editTextNot = view.findViewById(R.id.ekleMetin);
        btnKaydet = view.findViewById(R.id.ekleKaydetButton);

        btnKaydet.setOnClickListener(v -> {
            String metin = editTextNot.getText().toString().trim();
            boolean favori = checkboxFavori.isChecked();

            if (metin.isEmpty()) {
                Toast.makeText(getContext(), "Lütfen bir metin girin", Toast.LENGTH_SHORT).show();
                return;
            }
            ekleVeKapat(metin, favori);
        });

        return view;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        yenidenYukle();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setupButonlar();
        }
    }
    private void ekleVeKapat(String metin, boolean favori) {
        try {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).kayitEkle(metin, favori);
                ((MainActivity) getActivity()).mouseClickSound();
            }
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setupButonlar();
            }
            getActivity().getSupportFragmentManager().popBackStack();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
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

        checkboxFavori.setTypeface(typeface);
        checkboxFavori.setTextColor(selectedColor);

        btnKaydet.setTypeface(typeface);
        btnKaydet.setTextColor(selectedColor);
    }
}