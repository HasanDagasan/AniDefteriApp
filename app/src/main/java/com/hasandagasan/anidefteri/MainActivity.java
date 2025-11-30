package com.hasandagasan.anidefteri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.app.NotificationManager;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
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
import java.util.Calendar;
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
    ImageView[] hatirticiIcons;
    List<JSONObject> son6Not;
    ArrayList<String> gosterilecekListe = new ArrayList<>();
    ArrayList<String> favoriListe = new ArrayList<>();
    Typeface typeface;
    int savedColor;
    private ActionMode mActionMode;
    public MediaPlayer mediaPlayer;
    boolean sesAcik;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;

    private final BroadcastReceiver reminderDeletedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // "REMINDER_DELETED_ACTION" anonsu geldiğinde
            Log.d("MainActivity", "Hatırlatıcı silindi anonsu alındı, arayüz güncelleniyor.");
            setupButonlar(); // Ana ekran butonlarını ve ikonlarını yenile
            tumFragmentleriYenidenYukle(); // ListFragment'ı ve ikonlarını yenile
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAndRequestNotificationPermission();
        IntentFilter filter = new IntentFilter("REMINDER_DELETED_ACTION");
        LocalBroadcastManager.getInstance(this).registerReceiver(reminderDeletedReceiver, filter);

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
        handleIntent(getIntent());
        setupWindowInsets();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Uygulama zaten açıksa ve bildirimden yeni bir intent geldiyse, onu işle
        handleIntent(intent);
    }
    private void handleIntent(Intent intent) {
        if (intent != null && "HatirlatmaDetayFragment".equals(intent.getStringExtra("OPEN_FRAGMENT"))) {
            String notMetni = intent.getStringExtra("NOT_METNI");
            String kayitTarihi = intent.getStringExtra("KAYIT_TARIHI");

            if (notMetni != null) {
                // Fragment'ı oluştur ve verileri gönder
                HatirlatmaDetayFragment fragment = HatirlatmaDetayFragment.newInstance(notMetni, kayitTarihi);

                // Fragment'ı ekranda göster
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment) // `fragment_container` sizin ana fragment alanınızın ID'si olmalı
                        .addToBackStack(null) // Geri tuşuna basınca ana ekrana dönmesi için
                        .commit();

                // Intent'i işledikten sonra içini temizle ki tekrar tetiklenmesin
                intent.removeExtra("OPEN_FRAGMENT");
            }
        }
    }
    private void checkAndRequestNotificationPermission() {
        // Android 13 (API 33) ve üzeri için POST_NOTIFICATIONS izni gereklidir.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // İzin verilmiş mi diye kontrol et
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // İzin zaten var, bir şey yapmaya gerek yok.
                Log.d("Permission", "Bildirim izni zaten verilmiş.");
            } else {
                // Kullanıcıya daha önce "bir daha sorma" deyip demediğini kontrol et
                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                boolean permissionDeclined = prefs.getBoolean("notification_permission_declined", false);

                if (!permissionDeclined) {
                    // İzin daha önce reddedilmediyse, açıklama diyalogunu göster
                    showPermissionExplanationDialog();
                } else {
                    // Kullanıcı kalıcı olarak reddetmiş, artık rahatsız etmiyoruz.
                    Log.d("Permission", "Kullanıcı bildirim iznini kalıcı olarak reddetmiş.");
                }
            }
        }
    }
    private void showPermissionExplanationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Bildirim İzni Gerekiyor")
                .setMessage("Anılarınıza hatırlatıcı ekleyebilmeniz için uygulamanın bildirim gönderme iznine ihtiyacı var. İzin verirseniz hatırlatıcılar çalışır, vermezseniz bu özellik pasif kalır.")
                .setPositiveButton("İzin Ver", (dialog, which) -> {
                    requestNotificationPermission();
                })
                .setNegativeButton("İzin Verme", (dialog, which) -> {
                    SharedPreferences.Editor editor = getSharedPreferences("AppPrefs", MODE_PRIVATE).edit();
                    editor.putBoolean("notification_permission_declined", true);
                    editor.apply();
                    Toast.makeText(this, "Hatırlatıcı özelliği devre dışı bırakıldı.", Toast.LENGTH_LONG).show();
                })
                .setCancelable(false)
                .show();
    }
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST_CODE
            );
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Bildirim izni verildi. Hatırlatıcılar artık aktif!", Toast.LENGTH_SHORT).show();
            } else {
                SharedPreferences.Editor editor = getSharedPreferences("AppPrefs", MODE_PRIVATE).edit();
                editor.putBoolean("notification_permission_declined", true);
                editor.apply();
                Toast.makeText(this, "İzin verilmedi. Hatırlatıcı özelliği kullanılamayacak.", Toast.LENGTH_LONG).show();
            }
        }
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
                    return windowInsets;
                });
    }

    protected void onResume() {
        super.onResume();
        loadPreferences();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
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

        hatirticiIcons = new ImageView[]{
                findViewById(R.id.hatirlaticiIconMain1),
                findViewById(R.id.hatirlaticiIconMain2),
                findViewById(R.id.hatirlaticiIconMain3),
                findViewById(R.id.hatirlaticiIconMain4),
                findViewById(R.id.hatirlaticiIconMain5),
                findViewById(R.id.hatirlaticiIconMain6)
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
        boolean hatirlaticiVar = not.has("hatirlatici") && !not.isNull("hatirlatici");

        hatirticiIcons[index].setVisibility(hatirlaticiVar ? View.VISIBLE : View.INVISIBLE);
        heartButtons[index].setVisibility(favori ? View.VISIBLE : View.INVISIBLE);
        heartButtons[index].setHeartColor(savedColor);

        buttons[index].setText(metin);
        buttons[index].setVisibility(View.VISIBLE);
        buttons[index].setTextColor(savedColor);
        buttons[index].setTypeface(typeface);

        buttons[index].setOnClickListener(v -> {
            mouseClickSound();

            OptionMenuActions optionMenu = new OptionMenuActions(this);
            optionMenu.showOptionsDialogForNote(metin, favori, hatirlaticiVar);
        });
    }
    private void hideButton(int index) {
        buttons[index].setVisibility(View.INVISIBLE);
        heartButtons[index].setVisibility(View.INVISIBLE);
        hatirticiIcons[index].setVisibility(View.INVISIBLE);
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
        // 1. Önce veri kaynağını (JSON'dan okuyarak) güncelle
        listeleVerileri();

        // 2. Güncellenecek fragment'ları etiketleriyle bul ve yenile
        String[] fragmentTags = {"ListFragmentTag", "FavoriFragmentTag", "EkleFragmentTag", "EditFragmentTag"};

        for (String tag : fragmentTags) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);

            // Fragment bulunduysa ve "yenidenYukle" metodu varsa çağır
            if (fragment != null) {
                try {
                    // Fragment'ın kendi içindeki `yenidenYukle` metodunu çağırıyoruz.
                    // Artık o metot, güncel veriyi MainActivity'den kendisi çekecek.
                    fragment.getClass().getMethod("yenidenYukle").invoke(fragment);
                } catch (NoSuchMethodException e) {
                    // Bu fragment'ta yenidenYukle metodu yoksa sorun değil, devam et.
                    // Log.d("Yenileme", tag + " fragment'ında yenidenYukle metodu bulunamadı.");
                } catch (Exception e) {
                    // Diğer olası hatalar
                    Log.e("Yenileme", tag + " güncellenirken hata oluştu.", e);
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
    public void showReminderDialog(String metin) {
        // 1. XML dosyasını view olarak yükle
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_reminder, null);

        // 2. View içindeki bileşenleri bul
        Spinner spinnerReminderType = dialogView.findViewById(R.id.spinner_reminder_type);
        Button btnSelectDate = dialogView.findViewById(R.id.btn_select_date);
        Button btnSelectTime = dialogView.findViewById(R.id.btn_select_time);

        // 3. Spinner'ı ayarla
        final String[] tekrarTipleri = {"Tek Seferlik", "Her Gün", "Her Ay", "Her Yıl"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tekrarTipleri);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReminderType.setAdapter(adapter);

        // 4. Tarih ve saat seçimi için bir Calendar nesnesi ve formatlayıcılar oluştur
        final Calendar selectedCalendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat jsonFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        // --- YENİ BÖLÜM 1: Mevcut Hatırlatıcı Bilgilerini Yükleme ---
        JSONObject mevcutHatirlatici = getHatirlaticiObjesi(metin);
        if (mevcutHatirlatici != null) {
            try {
                // Kayıtlı zamanı parse et ve Calendar nesnesini ayarla
                String zamanStr = mevcutHatirlatici.getString("zaman");
                Date hatirlaticiZamani = jsonFormat.parse(zamanStr);
                if (hatirlaticiZamani != null) {
                    selectedCalendar.setTime(hatirlaticiZamani);
                }

                // Kayıtlı tekrar tipini Spinner'da seçili hale getir
                String tekrarTipi = mevcutHatirlatici.getString("tekrar");
                for (int i = 0; i < tekrarTipleri.length; i++) {
                    if (tekrarTipleri[i].equals(tekrarTipi)) {
                        spinnerReminderType.setSelection(i);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("showReminderDialog", "Mevcut hatırlatıcı parse edilirken hata.", e);
            }
        }

        // Başlangıçta buton metinlerini ayarla (mevcut veya o anki zaman)
        btnSelectDate.setText(dateFormat.format(selectedCalendar.getTime()));
        btnSelectTime.setText(timeFormat.format(selectedCalendar.getTime()));

        // --- YENİ BÖLÜM 2: Dinamik Alan Kontrolü ---
        final Runnable updateUIState = () -> {
            String secilenTip = spinnerReminderType.getSelectedItem().toString();
            if (secilenTip.equals("Her Gün")) {
                btnSelectDate.setEnabled(false); // Tarih butonunu pasif yap
                btnSelectDate.setAlpha(0.5f);     // Görünümünü soluklaştır
            } else {
                btnSelectDate.setEnabled(true);  // Diğer durumlarda aktif yap
                btnSelectDate.setAlpha(1.0f);
            }
        };

        // Spinner seçimi değiştiğinde UI durumunu güncelle
        spinnerReminderType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateUIState.run();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Dialog ilk açıldığında da kontrolü çalıştır
        updateUIState.run();


        // 5. Butonların tıklama olaylarını ayarla
        btnSelectDate.setOnClickListener(v -> {
            mouseClickSound();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedCalendar.set(Calendar.YEAR, year);
                selectedCalendar.set(Calendar.MONTH, month);
                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                btnSelectDate.setText(dateFormat.format(selectedCalendar.getTime()));
            }, selectedCalendar.get(Calendar.YEAR), selectedCalendar.get(Calendar.MONTH), selectedCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSelectTime.setOnClickListener(v -> {
            mouseClickSound();
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedCalendar.set(Calendar.MINUTE, minute);
                btnSelectTime.setText(timeFormat.format(selectedCalendar.getTime()));
            }, selectedCalendar.get(Calendar.HOUR_OF_DAY), selectedCalendar.get(Calendar.MINUTE), true).show();
        });

        // 6. AlertDialog'u oluştur
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Hatırlatıcı Ayarla")
                .setPositiveButton("Kaydet", (dialog, which) -> {
                    mouseClickSound();
                    String secilenTip = spinnerReminderType.getSelectedItem().toString();
                    String kayitTarihi = getNotTarihi(metin);
                    ReminderScheduler.scheduleReminder(this, selectedCalendar, metin, secilenTip, kayitTarihi);

                    // Arayüzü güncellemek için bu iki satırı geri ekliyoruz, çünkü JSON güncellemesi
                    // artık ReminderScheduler içinde yapılıyor ve MainActivity bu değişiklikten haberdar olmalı.
                    setupButonlar();
                    tumFragmentleriYenidenYukle();
                })
                .setNegativeButton("İptal", (dialog, which) -> mouseClickSound())
                .setNeutralButton("Kaldır", (dialog, which) -> {
                    mouseClickSound();
                    ReminderScheduler.cancelReminder(this, metin);
                    removeReminderFromJson(this, metin);
                    Toast.makeText(this, "Hatırlatıcı kaldırıldı.", Toast.LENGTH_SHORT).show();
                    setupButonlar();
                    tumFragmentleriYenidenYukle();
                });
        builder.create().show();
    }
    private JSONObject getHatirlaticiObjesi(String metin) {
        dosyaOku();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject obj = jsonArray.getJSONObject(i);
                if (obj.getString("metin").equals(metin)) {
                    if (obj.has("hatirlatici") && !obj.isNull("hatirlatici")) {
                        return obj.getJSONObject("hatirlatici");
                    }
                    break; // Not bulundu ama hatırlatıcı yok, aramayı bitir.
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null; // Not bulunamadı veya hatırlatıcı yok.
    }
    private String getNotTarihi(String metin) {
        dosyaOku();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject obj = jsonArray.getJSONObject(i);
                if (obj.getString("metin").equals(metin)) {
                    return obj.getString("tarih"); // "yyyy-MM-dd HH:mm" formatındaki tarihi döndür
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // Tarih bulunamazsa, o anki tarihi döndür (bir yedekleme olarak)
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
    }
    public void guncelleNotHatirlatici(String metin, String tekrarTipi, Calendar calendar) {
        dosyaOku();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String hatirlatmaZamani = format.format(calendar.getTime());

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject obj = jsonArray.getJSONObject(i);
                if (obj.getString("metin").equals(metin)) {
                    JSONObject hatirlaticiObj = new JSONObject();
                    hatirlaticiObj.put("zaman", hatirlatmaZamani);
                    hatirlaticiObj.put("tekrar", tekrarTipi);
                    obj.put("hatirlatici", hatirlaticiObj);
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        verileriKaydet();
    }
    public static void removeReminderFromJson(Context context, String metin) {
        if (metin == null || context == null) {
            return; // Hatalı girişi engelle
        }

        // Bu metot kendi dosya yolunu oluşturmalı, çünkü MainActivity'nin "myExternalFile" değişkenine erişemez.
        File myExternalFile = new File(context.getExternalFilesDir("Metin"), "veriler.json");
        JSONArray jsonArray = new JSONArray();

        // 1. Dosyayı oku
        if (myExternalFile.exists()) {
            try (FileInputStream fis = new FileInputStream(myExternalFile)) {
                byte[] data = new byte[(int) myExternalFile.length()];
                fis.read(data);
                String jsonStr = new String(data, StandardCharsets.UTF_8);
                if (!jsonStr.isEmpty()) {
                    jsonArray = new JSONArray(jsonStr);
                }
            } catch (Exception e) {
                Log.e("StaticRemove", "Dosya okunurken hata.", e);
                return;
            }
        } else {
            return; // Dosya yoksa yapılacak bir şey yok
        }

        // 2. İlgili notu bul ve "hatirlatici" alanını kaldır
        boolean degisiklikYapildi = false;
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject obj = jsonArray.getJSONObject(i);
                if (obj.has("metin") && obj.getString("metin").equals(metin)) {
                    if (obj.has("hatirlatici")) {
                        obj.remove("hatirlatici");
                        degisiklikYapildi = true;
                        Log.d("StaticRemove", "'" + metin + "' notunun hatırlatıcısı statik metot ile silindi.");
                        break; // Notu bulduk, döngüden çıkabiliriz.
                    }
                }
            } catch (JSONException e) {
                Log.e("StaticRemove", "JSON işlenirken hata.", e);
            }
        }

        // 3. Değişiklik yapıldıysa, güncellenmiş veriyi dosyaya geri yaz
        if (degisiklikYapildi) {
            try (FileOutputStream fos = new FileOutputStream(myExternalFile, false)) { // 'false' ile üzerine yaz
                fos.write(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                Log.e("StaticRemove", "Dosya yazılırken hata.", e);
            }
        }
    }
    public boolean hatirlaticiVarMi(String metin) {
        dosyaOku(); // JSON verisinin en güncel halini okuduğumuzdan emin olalım.

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject obj = jsonArray.getJSONObject(i);
                // Notu metniyle eşleştir
                if (obj.getString("metin").equals(metin)) {
                    // "hatirlatici" anahtarı var mı ve bu anahtarın değeri boş (null) değil mi diye kontrol et.
                    return obj.has("hatirlatici") && !obj.isNull("hatirlatici");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // Döngü bittiğinde not bulunamadıysa veya bulunup hatırlatıcısı yoksa false döndür.
        return false;
    }
    public void notuSil(String metin) {
        ReminderScheduler.cancelReminder(this, metin);
        dosyaOku();
        JSONArray guncelJsonArray = new JSONArray();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject obj = jsonArray.getJSONObject(i);
                // Silinecek not dışındaki tüm notları yeni array'e ekle
                if (!obj.getString("metin").equals(metin)) {
                    guncelJsonArray.put(obj);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        jsonArray = guncelJsonArray; // Ana array'i filtrelenmiş olanla değiştir
        verileriKaydet(); // Değişikliği dosyaya yaz
        setupButonlar(); // Ana ekrandaki butonları güncelle
        tumFragmentleriYenidenYukle(); // Listeleme fragment'ını yenile
    }
    public void toggleFavorite(String metin, boolean yeniFavoriDurumu) {
        dosyaOku(); // En güncel veriyi oku
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject obj = jsonArray.getJSONObject(i);
                if (obj.getString("metin").equals(metin)) {
                    obj.put("favori", yeniFavoriDurumu);
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        verileriKaydet();
        setupButonlar();
        tumFragmentleriYenidenYukle();

        // Kullanıcıya bilgi ver
        String mesaj = yeniFavoriDurumu ? "Favorilere eklendi" : "Favorilerden kaldırıldı";
        Toast.makeText(this, mesaj, Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(reminderDeletedReceiver);
    }
    private boolean isExternalStorageReadOnly() {return Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());}
    private boolean isExternalStorageAvailable() {return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());}

}
