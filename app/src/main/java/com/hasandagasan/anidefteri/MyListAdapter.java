package com.hasandagasan.anidefteri;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.hasandagasan.anidefteri.classes.HeartButton;
import java.util.ArrayList;
public class MyListAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<String> liste;
    private final int textColor;
    private final Typeface typeface;
    private final boolean multiSelectMode;
    private final SparseBooleanArray selectedItems;
    private OnFavoriteChangedListener favoriteChangedListener;
    public interface OnFavoriteChangedListener {
        void onFavoriteChanged(int position, boolean isFavorite);
    }

    public MyListAdapter(Context context, ArrayList<String> liste, int textColor, Typeface typeface, boolean multiSelectMode) {
        this.context = context;
        this.liste = liste;
        this.textColor = textColor;
        this.typeface = typeface;
        this.multiSelectMode = multiSelectMode;
        this.selectedItems = new SparseBooleanArray();
    }

    public void setOnFavoriteChangedListener(OnFavoriteChangedListener listener) {
        this.favoriteChangedListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_layout, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.textViewItem);
        HeartButton heart = convertView.findViewById(R.id.kalp);

        String text = liste.get(position);

        boolean isFavorite = text.startsWith("★");
        String displayText = isFavorite ? text.substring(1).trim() : text;

        textView.setText(displayText);
        textView.setTextColor(textColor);
        textView.setTypeface(typeface);

        heart.setHeartColor(isFavorite ? textColor : Color.GRAY);

        heart.setOnClickListener(v -> {
            boolean newFavoriteState;
            if (isFavorite) {
                liste.set(position, displayText); // ★ kaldır
                newFavoriteState = false;
            } else {
                liste.set(position, "★ " + displayText); // ★ ekle
                newFavoriteState = true;
            }

            // Callback ile Activity'ye bildir
            if (favoriteChangedListener != null) {
                favoriteChangedListener.onFavoriteChanged(position, newFavoriteState);
            }

            notifyDataSetChanged();
        });

        if (multiSelectMode && selectedItems.get(position, false)) {
            convertView.setBackgroundColor(Color.LTGRAY);
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }

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
    public void toggleFavorite(int position) {
        // Pozisyonun geçerli olup olmadığını kontrol et
        if (position < 0 || position >= liste.size()) {
            return;
        }

        String currentItemText = liste.get(position);
        boolean isCurrentlyFavorite = currentItemText.startsWith("★");

        // Yeni favori durumunu belirle
        boolean newFavoriteState = !isCurrentlyFavorite;

        // Metni güncelle (yıldız ekle veya kaldır)
        if (newFavoriteState) {
            // Favori değilse yıldız ekle
            String displayText = currentItemText.trim();
            liste.set(position, "★ " + displayText);
        } else {
            // Favori ise yıldızı kaldır
            String displayText = currentItemText.substring(1).trim();
            liste.set(position, displayText);
        }

        // Listener'ı (dinleyiciyi) tetikle. Bu, değişikliği JSON dosyasına kaydeder.
        if (favoriteChangedListener != null) {
            favoriteChangedListener.onFavoriteChanged(position, newFavoriteState);
        }

        // Değişikliğin anında görünmesi için adaptörü bilgilendir.
        notifyDataSetChanged();
    }

    @Override
    public int getCount() { return liste.size(); }

    @Override
    public Object getItem(int position) { return liste.get(position); }

    @Override
    public long getItemId(int position) { return position; }
}