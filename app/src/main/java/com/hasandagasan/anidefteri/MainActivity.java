package com.hasandagasan.anidefteri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.hasandagasan.anidefteri.classes.ButonControllers;
import com.hasandagasan.anidefteri.classes.ButonControllers.ButonControllersCallBack;
import com.hasandagasan.anidefteri.classes.GetTypeFace;
import com.hasandagasan.anidefteri.classes.HeartButton;
import com.hasandagasan.anidefteri.classes.OptionMenuActions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity implements ButonControllersCallBack {

    private AdView adView;
    private File myExternalFile;
    private JSONArray jsonArray = new JSONArray();
    Button listeleButon, ekleButon, favoriButon, anasayfaButon, ayarlarButon, kayitBosButon;
    Button[] buttons;
    HeartButton[] heartButtons;
    List<JSONObject> son6Not;
    ArrayList<String> gosterilecekListe = new ArrayList<>();
    ArrayList<String> favoriListe = new ArrayList<>();
    Typeface typeface;
    int savedColor;
    private ActionMode mActionMode;
    public MediaPlayer mediaPlayer;
    boolean sesAcik;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        MobileAds.initialize(this, initializationStatus -> {});
        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        mediaPlayer = MediaPlayer.create(this, R.raw.mouseclickmp3);
        initializeButtons();

        ButonControllers butonControllers = new ButonControllers(this, this);
        butonControllers.setButtons(kayitBosButon, anasayfaButon, ekleButon,
                favoriButon, listeleButon, ayarlarButon);
        butonControllers.setupBackStackListener();

        String fragmentToOpen = getIntent().getStringExtra("openFragment");
        if ("ekle".equals(fragmentToOpen)) {
            ekleFragmentAc();
        }

        String FILE_DIR = "Metin";
        String FILE_NAME = "veriler.json";
        myExternalFile = new File(getExternalFilesDir(FILE_DIR), FILE_NAME);

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.e("Depolama", "Harici depolama kullanılamıyor");
            return;
        }

        setupWindowInsets();
    }
    private void setupWindowInsets() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activityMain),
                (v, windowInsets) -> {
                    Insets systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

                    View bottomNav = findViewById(R.id.bottomNavigationBar);
                    if (bottomNav != null) {
                        ViewGroup.LayoutParams layoutParams = bottomNav.getLayoutParams();

                        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) layoutParams;
                            marginParams.bottomMargin = 0;
                            bottomNav.setLayoutParams(marginParams);
                        }

                        bottomNav.setPadding(
                                bottomNav.getPaddingLeft(),
                                bottomNav.getPaddingTop(),
                                bottomNav.getPaddingRight(),
                                systemBarsInsets.bottom
                        );

                        int originalHeight = (int) (50 * getResources().getDisplayMetrics().density);
                        layoutParams.height = originalHeight + systemBarsInsets.bottom;
                        bottomNav.setLayoutParams(layoutParams);
                    }

                    View fragmentContainer = findViewById(R.id.fragment_container);
                    if (fragmentContainer != null) {
                        fragmentContainer.setPadding(
                                0,
                                systemBarsInsets.top,
                                0,
                                0
                        );
                    }

                    //View adView = findViewById(R.id.adView);
                   // if (adView != null) {
                   //     ViewGroup.MarginLayoutParams adParams = (ViewGroup.MarginLayoutParams) adView.getLayoutParams();
                   //     adParams.topMargin = systemBarsInsets.top;
                   //     adView.setLayoutParams(adParams);
                   // }

                    return windowInsets;
                });
    }

    protected void onResume() {
        super.onResume();
        loadPreferences();
    }
    private void initializeButtons(){
        listeleButon  = findViewById(R.id.listele);
        ekleButon     = findViewById(R.id.ekle);
        favoriButon   = findViewById(R.id.favoriler);
        anasayfaButon = findViewById(R.id.home);
        ayarlarButon  = findViewById(R.id.ayarlar);
        kayitBosButon = findViewById(R.id.kayitbos);

        buttons = new Button[] {
                findViewById(R.id.button1),
                findViewById(R.id.button2),
                findViewById(R.id.button3),
                findViewById(R.id.button4),
                findViewById(R.id.button5),
                findViewById(R.id.button6),

        };

        heartButtons = new HeartButton[]{
                findViewById(R.id.kalp1),
                findViewById(R.id.kalp2),
                findViewById(R.id.kalp3),
                findViewById(R.id.kalp4),
                findViewById(R.id.kalp5),
                findViewById(R.id.kalp6)
        };
    }
    public void loadPreferences() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        int savedColor = prefs.getInt("selectedColor", Color.BLACK);
        String savedFont = prefs.getString("selectedFont", "Sans Serif");
        boolean sesAcik = prefs.getBoolean("sesAcik", true);
        GetTypeFace typeFace = new GetTypeFace();
        Typeface typeface = typeFace.getTypefaceFromFontName(this, savedFont);

        this.savedColor = savedColor;
        this.typeface = typeface;
        this.sesAcik = sesAcik;

        setupButonlar();
    }
    public void setupButonlar() {
        updateNotData();

        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);

        updateKayitBosButtonVisibility(currentFragment);
        updateNotButtons();
    }
    private void updateNotData() {
        dosyaOku();
        son6Not = new ArrayList<>();

        int limit = Math.min(6, jsonArray.length());
        for (int i = 0; i < limit; i++) {
            try {
                JSONObject obj = jsonArray.getJSONObject(i);
                son6Not.add(obj);
            } catch (JSONException e) {
                Log.e("MainActivity", "JSON parsing error", e);
            }
        }
    }
    private void updateKayitBosButtonVisibility(Fragment currentFragment) {
        if (son6Not.size() < 1) {
            if (currentFragment == null) {
                kayitBosButon.setVisibility(View.VISIBLE);
            } else {
                kayitBosButon.setVisibility(View.INVISIBLE);
            }
        } else {
            kayitBosButon.setVisibility(View.INVISIBLE);
        }
    }
    private void updateNotButtons() {

        for (int i = 0; i < buttons.length; i++) {
            if (i < son6Not.size()) {
                setupVisibleButton(i);
            } else {
                hideButton(i);
            }
        }
    }
    private void setupVisibleButton(int index) {
        JSONObject not = son6Not.get(index);
        String metin = not.optString("metin", "");
        boolean favori = not.optBoolean("favori", false);

        heartButtons[index].setVisibility(favori ? View.VISIBLE : View.INVISIBLE);
        heartButtons[index].setHeartColor(savedColor);

        buttons[index].setText(metin);
        buttons[index].setVisibility(View.VISIBLE);
        buttons[index].setTextColor(savedColor);
        buttons[index].setTypeface(typeface);

        buttons[index].setOnClickListener(v -> {
            getSupportFragmentManager()
                    .popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            notuDuzenle(index);
            mouseClickSound();
        });
    }
    private void hideButton(int index) {
        buttons[index].setVisibility(View.INVISIBLE);
        heartButtons[index].setVisibility(View.INVISIBLE);
    }
    private void notuDuzenle(int index) {
        try {
            JSONObject secilenNot = son6Not.get(index);
            String metin = secilenNot.getString("metin");
            boolean favori = secilenNot.getBoolean("favori");

            editFragmentAc(metin, favori);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void kayitEkle(String metin, boolean favori) {
        try {
            JSONObject yeniVeri = new JSONObject();
            yeniVeri.put("metin", metin);
            String tarih = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            yeniVeri.put("tarih", tarih);
            yeniVeri.put("favori", favori);

            JSONArray yeniArray = new JSONArray();
            yeniArray.put(0, yeniVeri);

            for (int i = 0; i < jsonArray.length(); i++) {
                yeniArray.put(jsonArray.get(i));
            }

            jsonArray = yeniArray;

            try (FileOutputStream fos = new FileOutputStream(myExternalFile)) {
                fos.write(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
            }
            Toast.makeText(this, "Kayıt eklendi", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void dosyaOku(){
        if (myExternalFile.exists()) {
            try (FileInputStream fis = new FileInputStream(myExternalFile)) {
                byte[] data = new byte[(int) myExternalFile.length()];
                fis.read(data);
                String jsonStr = new String(data, StandardCharsets.UTF_8);
                jsonArray = new JSONArray(jsonStr);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public void listeleVerileri() {
        gosterilecekListe.clear();
        favoriListe.clear();

        dosyaOku();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject obj = jsonArray.getJSONObject(i);
                String metin = obj.optString("metin");
                String tarih = obj.optString("tarih");
                boolean favori = obj.optBoolean("favori");

                if(favori){
                    String favorimetin = "★ " + metin + "\n(" + tarih + ")";
                    favoriListe.add(favorimetin);
                }

                String satir = (favori ? "★ " : "") + metin + "\n(" + tarih + ")";
                gosterilecekListe.add(satir);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public boolean belirliFragmentAcikMi(Class<? extends Fragment> fragmentClass) {
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        return currentFragment != null && fragmentClass.isInstance(currentFragment);
    }
    public void listFragmentAc() {
        listeleVerileri();

        ListFragment fragment = new ListFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("liste", gosterilecekListe);
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, "ListFragmentTag")
                .addToBackStack(null)
                .commit();
    }
    private void editFragmentAc(String metin, Boolean favori) {
        editFragment editFragment = com.hasandagasan.anidefteri.editFragment.newInstance(metin, favori);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, editFragment, "EditFragmentTag")
                .addToBackStack(null)
                .commit();
    }
    public void favoriFragmentAc() {
        listeleVerileri();

        favoriFragment fragment = new favoriFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("liste", favoriListe);
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, "FavoriFragmentTag")
                .addToBackStack(null)
                .commit();
    }
    public void ekleFragmentAc(){
        ekleFragment fragment = new ekleFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, "EkleFragmentTag")
                .addToBackStack(null)
                .commit();
    }
    public void guncelleNot(String eskiMetin, String yeniMetin, boolean yeniFavori) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject obj = jsonArray.getJSONObject(i);
                if (obj.getString("metin").equals(eskiMetin)) {
                    obj.put("metin", yeniMetin);
                    obj.put("favori",yeniFavori);
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        verileriKaydet();
        listeleVerileri();
        setupButonlar();
    }
    private void verileriKaydet() {
        try {
            try (FileOutputStream fos = new FileOutputStream(myExternalFile)) {
                fos.write(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void optionMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_main, popupMenu.getMenu());

        MenuItem sesItem = popupMenu.getMenu().findItem(R.id.sesayar);
        sesItem.setTitle(sesAcik ? "Sesi Kapat" : "Sesi Aç");

        OptionMenuActions optionMenuActions = new OptionMenuActions(this);
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.yazirengi) {
                mouseClickSound();
                optionMenuActions.yaziRengiSec(this);
                return true;
            } else if (itemId == R.id.yazitipi) {
                mouseClickSound();
                optionMenuActions.yaziTipiSec(this);
                return true;
            } else if (itemId == R.id.sesayar) {
                mouseClickSound();
                sesAcik = !sesAcik;
                item.setTitle(sesAcik ? "Sesi Kapat" : "Sesi Aç");
                optionMenuActions.sesAyarSec(sesAcik);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }
    public void kaydetVeUygula(String key, Integer colorValue, String fontValue, Boolean sesAyar) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (colorValue != null) {
            editor.putInt(key, colorValue);
            for (Button button : buttons) {
                button.setTextColor(colorValue);
            }
            for (HeartButton button : heartButtons) {
                button.setHeartColor(colorValue);
            }
        }

        if (fontValue != null) {
            editor.putString(key, fontValue);
            for (Button button : buttons) {
                button.setTypeface(typeface);
            }
        }

        if (sesAyar != null) {
            editor.putBoolean(key, sesAyar);
        }
        editor.apply();
    }
    public void tumFragmentleriYenidenYukle() {
        String[] fragmentTags = {"ListFragmentTag", "FavoriFragmentTag", "EkleFragmentTag", "EditFragmentTag"};
        Class<?>[] fragmentClasses = {ListFragment.class, favoriFragment.class, ekleFragment.class, editFragment.class};

        for (int i = 0; i < fragmentTags.length; i++) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentTags[i]);
            if (fragment != null && fragmentClasses[i].isInstance(fragment)) {
                try {
                    fragment.getClass().getMethod("yenidenYukle").invoke(fragment);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void mouseClickSound(){
        if (sesAcik && mediaPlayer != null) {
            mediaPlayer.start();
        }
    }
    public void setActionMode(ActionMode actionMode) {
        this.mActionMode = actionMode;
    }
    public void closeActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        }
    }
    private boolean isExternalStorageReadOnly() {return Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());}
    private boolean isExternalStorageAvailable() {return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());}

}
