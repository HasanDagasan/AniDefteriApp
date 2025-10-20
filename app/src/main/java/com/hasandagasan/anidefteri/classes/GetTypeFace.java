package com.hasandagasan.anidefteri.classes;

import android.content.Context;
import android.graphics.Typeface;
import androidx.core.content.res.ResourcesCompat;
import com.hasandagasan.anidefteri.R;

public class GetTypeFace {
    public Typeface getTypefaceFromFontName(Context context, String fontName) {
        switch (fontName) {
            case "Sans Serif":
                return Typeface.SANS_SERIF;
            case "AguDisplay":
                return ResourcesCompat.getFont(context, R.font.agudisplay);
            case "Cookie":
                return ResourcesCompat.getFont(context, R.font.cookie);
            case "DancingScript":
                return ResourcesCompat.getFont(context, R.font.dancing);
            case "Lobster":
                return ResourcesCompat.getFont(context, R.font.lobster);
            case "Play":
                return ResourcesCompat.getFont(context, R.font.play);
            case "RubikVinyl":
                return ResourcesCompat.getFont(context, R.font.rubik);
            case "VujahdayScript":
                return ResourcesCompat.getFont(context, R.font.vujahday);
            default:
                return Typeface.DEFAULT;
        }
    }
}
