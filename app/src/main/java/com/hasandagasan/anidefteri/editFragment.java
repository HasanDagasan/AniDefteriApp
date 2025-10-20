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

    private static final String ARG_METIN = "metin";
    private static final String ARG_FAVORI = "favori";
    private String metin;
    private boolean favori;
    private EditText editTextNot;
    private CheckBox checkboxFavori;
    Button btnKaydet;
    private AdView adView;
    public static editFragment newInstance(String metin, boolean favori) {
        editFragment fragment = new editFragment();
        Bundle args = new Bundle();
        args.putString(ARG_METIN, metin);
        args.putBoolean(ARG_FAVORI, favori);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            metin = getArguments().getString(ARG_METIN);
            favori = getArguments().getBoolean(ARG_FAVORI);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit, container, false);

        adView = view.findViewById(R.id.adViewFragment);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        checkboxFavori = view.findViewById(R.id.checkFavori);
        checkboxFavori.setChecked(favori);
        editTextNot = view.findViewById(R.id.editMetin);
        btnKaydet = view.findViewById(R.id.kaydetButton);

        editTextNot.setText(metin);

        btnKaydet.setOnClickListener(view1 -> {
            if(editTextNot.getText().toString().trim().isEmpty()){
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
        yenidenYukle();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setupButonlar();
        }
    }
    private void kaydetVeKapat() {
        try {
            String yeniMetin = editTextNot.getText().toString();
            boolean yeniFavori = checkboxFavori.isChecked();

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).guncelleNot(metin, yeniMetin, yeniFavori);
                ((MainActivity) getActivity()).mouseClickSound();

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

        checkboxFavori.setTextColor(selectedColor);
        checkboxFavori.setTypeface(typeface);

        btnKaydet.setTextColor(selectedColor);
        btnKaydet.setTypeface(typeface);

    }

}
