package com.hasandagasan.anidefteri;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter; // DEĞİŞİKLİK: BaseAdapter yerine ArrayAdapter kullanıldı.
import android.widget.ImageView;   // DEĞİŞİKLİK: ImageView import edildi.
import android.widget.TextView;

import androidx.annotation.NonNull; // DEĞİŞİKLİK: Annotation eklendi.
import androidx.annotation.Nullable;

import com.hasandagasan.anidefteri.classes.HeartButton;
import java.util.ArrayList;

// DEĞİŞİKLİK: Sınıf tanımı ArrayAdapter'dan türetildi. Bu, standart ve daha yönetilebilir bir yoldur.
public class MyListAdapter extends ArrayAdapter<String> {

    private final MainActivity mainActivity; // DEĞİŞİKLİK: MainActivity referansı eklendi.
    private final int textColor;
    private final Typeface typeface;
    private final SparseBooleanArray selectedItems;
    private OnFavoriteChangedListener favoriteChangedListener;

    public interface OnFavoriteChangedListener {
        void onFavoriteChanged(int position, boolean isFavorite);
    }

    public MyListAdapter(@NonNull Context context, ArrayList<String> liste, int textColor, Typeface typeface, boolean singleSelectionMode) {
        // DEĞİŞİKLİK: Super constructor'a layout ID ve liste verildi.
        super(context, R.layout.list_item_layout, liste);
        // DEĞİŞİKLİK: MainActivity'yi context üzerinden alıyoruz.
        this.mainActivity = (MainActivity) context;
        this.textColor = textColor;
        this.typeface = typeface;
        this.selectedItems = new SparseBooleanArray();
    }

    public void setOnFavoriteChangedListener(OnFavoriteChangedListener listener) {
        this.favoriteChangedListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // ViewHolder deseni, listelerde performansı artırır.
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_layout, parent, false);
            holder = new ViewHolder();
            holder.textView = convertView.findViewById(R.id.textViewItem);
            holder.heartButton = convertView.findViewById(R.id.kalp);
            holder.hatirlaticiIcon = convertView.findViewById(R.id.hatirlaticiIconList); // İkonu bul
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String tamMetin = getItem(position);
        if (tamMetin == null) return convertView; // Null kontrolü

        boolean isFavorite = tamMetin.startsWith("★");
        String temizMetin = temizleVeMetniAl(tamMetin); // Yardımcı metot
        String gorunenMetin = isFavorite ? tamMetin.substring(1).trim() : tamMetin;

        holder.textView.setText(gorunenMetin);
        holder.textView.setTextColor(textColor);
        holder.textView.setTypeface(typeface);

        // DEĞİŞİKLİK: Kalp butonu rengi ve tıklama olayı
        holder.heartButton.setHeartColor(isFavorite ? textColor : Color.GRAY);
        holder.heartButton.setOnClickListener(v -> {
            // Favori durumunu tersine çevir
            if (favoriteChangedListener != null) {
                favoriteChangedListener.onFavoriteChanged(position, !isFavorite);
            }
        });

        // --- YENİ BÖLÜM: Hatırlatıcı ikonunu yönetme ---
        if (mainActivity.hatirlaticiVarMi(temizMetin)) {
            holder.hatirlaticiIcon.setVisibility(View.VISIBLE);
        } else {
            holder.hatirlaticiIcon.setVisibility(View.GONE);
        }
        // --- YENİ BÖLÜM SONU ---

        // Seçim modu için arkaplan rengi
        if (selectedItems.get(position, false)) {
            convertView.setBackgroundColor(0x33A9A9A9); // Yarı saydam gri
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }

    // DEĞİŞİKLİK: Kod tekrarını önlemek için yardımcı metot
    private String temizleVeMetniAl(String tamMetin) {
        if (tamMetin == null) return "";
        String temizMetin = tamMetin.startsWith("★") ? tamMetin.substring(1).trim() : tamMetin;
        return temizMetin.replaceAll("\\s*\\(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}\\)$", "").trim();
    }

    // ViewHolder Sınıfı
    private static class ViewHolder {
        TextView textView;
        HeartButton heartButton;
        ImageView hatirlaticiIcon;
    }

    // Seçim yönetimi metotları (Değişiklik yok)
    public void toggleSelection(int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public ArrayList<Integer> getSelectedItems() {
        ArrayList<Integer> items = new ArrayList<>();
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }
}
