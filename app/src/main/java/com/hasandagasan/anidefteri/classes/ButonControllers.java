package com.hasandagasan.anidefteri.classes;

import android.view.View;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import com.hasandagasan.anidefteri.ListFragment;
import com.hasandagasan.anidefteri.R;
import com.hasandagasan.anidefteri.ekleFragment;
import com.hasandagasan.anidefteri.favoriFragment;

public class ButonControllers {
    private final FragmentManager fragmentManager;
    private Button kayitBosButon;
    private Button anasayfaButon;
    private Button ekleButon;
    private Button favoriButon;
    private Button listeleButon;
    private Button ayarlarButon;

    public interface ButonControllersCallBack {
        void ekleFragmentAc();
        void listFragmentAc();
        void favoriFragmentAc();
        void listeleVerileri();
        void setupButonlar();
        void closeActionMode();
        void mouseClickSound();
        void optionMenu(View view);
        boolean belirliFragmentAcikMi(Class<? extends Fragment> fragmentClass);
    }
    private final ButonControllersCallBack callback;
    public ButonControllers(FragmentActivity activity, ButonControllersCallBack callback) {
        this.fragmentManager = activity.getSupportFragmentManager();
        this.callback = callback;
    }
    public void setButtons(Button kayitBosButon, Button anasayfaButon, Button ekleButon, Button favoriButon, Button listeleButon, Button ayarlarButon) {
        this.kayitBosButon = kayitBosButon;
        this.anasayfaButon = anasayfaButon;
        this.ekleButon     = ekleButon;
        this.favoriButon   = favoriButon;
        this.listeleButon  = listeleButon;
        this.ayarlarButon  = ayarlarButon;

        setupListeners();
    }
    public void setupBackStackListener() {
        fragmentManager.addOnBackStackChangedListener(() -> {
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);

            if (currentFragment != null) {
                kayitBosButon.setVisibility(View.GONE);
                anasayfaButon.setBackgroundResource(R.drawable.anasayfa_bos);
                updateButtonStates(currentFragment);

            } else {
                setDefaultButtonStates();
            }
        });
    }
    public void updateButtonStates(Fragment currentFragment) {

        if (currentFragment instanceof ekleFragment) {
            ekleButon.setBackgroundResource(R.drawable.ekleaktif);
        } else {
            ekleButon.setBackgroundResource(R.drawable.eklepasif);
        }

        if (currentFragment instanceof favoriFragment) {
            favoriButon.setBackgroundResource(R.drawable.heart_aktif);
        } else {
            favoriButon.setBackgroundResource(R.drawable.heart_bos);
        }

        if (currentFragment instanceof ListFragment) {
            listeleButon.setBackgroundResource(R.drawable.listeaktif);
        } else {
            listeleButon.setBackgroundResource(R.drawable.listepasif);
        }
    }
    public void setDefaultButtonStates() {
        anasayfaButon.setBackgroundResource(R.drawable.anasayfa_dolu);
        ekleButon.setBackgroundResource(R.drawable.eklepasif);
        favoriButon.setBackgroundResource(R.drawable.heart_bos);
        listeleButon.setBackgroundResource(R.drawable.listepasif);
    }
    private void setupListeners() {
        kayitBosButon.setOnClickListener(view -> {
            callback.closeActionMode();
            if (!callback.belirliFragmentAcikMi(ekleFragment.class)) {
                fragmentManager.popBackStack();
                callback.ekleFragmentAc();
                kayitBosButon.setVisibility(View.INVISIBLE);
                callback.mouseClickSound();
            }
        });

        anasayfaButon.setOnClickListener(view -> {
            callback.closeActionMode();
            fragmentManager.popBackStack();
            callback.listeleVerileri();
            callback.setupButonlar();
            callback.mouseClickSound();
        });

        listeleButon.setOnClickListener(view -> {
            callback.closeActionMode();
            if (!callback.belirliFragmentAcikMi(ListFragment.class)) {
                fragmentManager.popBackStack();
                callback.listFragmentAc();
                callback.mouseClickSound();
            }
        });

        ekleButon.setOnClickListener(view -> {
            callback.closeActionMode();
            if (!callback.belirliFragmentAcikMi(ekleFragment.class)) {
                fragmentManager.popBackStack();
                callback.ekleFragmentAc();
                kayitBosButon.setVisibility(View.INVISIBLE);
                callback.mouseClickSound();
            }
        });

        favoriButon.setOnClickListener(view -> {
            callback.closeActionMode();
            if (!callback.belirliFragmentAcikMi(favoriFragment.class)) {
                fragmentManager.popBackStack();
                callback.favoriFragmentAc();
                callback.mouseClickSound();
            }
        });

        ayarlarButon.setOnClickListener(view -> {
            callback.mouseClickSound();
            callback.optionMenu(view);
        });
    }
}
