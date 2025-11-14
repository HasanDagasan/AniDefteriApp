package com.hasandagasan.anidefteri;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import java.util.Calendar;

public class ReminderScheduler {

    public static void scheduleReminder(Context context, Calendar calendar, String notMetni, String tekrarTipi, String kayitTarihi) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Android 12 (API 31) ve üzeri için tam zamanlı alarm iznini kontrol et
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(context, "Doğru zamanlı hatırlatıcılar için izin gerekli.", Toast.LENGTH_LONG).show();
                // Kullanıcıyı izin vermesi için ayarlara yönlendir
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                context.startActivity(intent);
                return;
            }
        }
        int alarmId = notMetni.hashCode();

        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
        intent.putExtra("NOT_METNI", notMetni);
        intent.putExtra("NOTIFICATION_ID", alarmId);
        intent.putExtra("TEKRAR_TIPI", tekrarTipi);
        intent.putExtra("KAYIT_TARIHI", kayitTarihi);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        boolean shouldSchedule = true;

        // Alarm zamanı geçmişse
        if ((calendar.getTimeInMillis() + 3500) <= System.currentTimeMillis()) {
            if (!tekrarTipi.equals("Tek Seferlik")) {
                // Tekrarlı alarm ise, bir sonraki periyoda ayarla
                switch (tekrarTipi) {
                    case "Her Gün":
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                        break;
                    case "Her Ay":
                        calendar.add(Calendar.MONTH, 1);
                        break;
                    case "Her Yıl":
                        calendar.add(Calendar.YEAR, 1);
                        break;
                }
                Toast.makeText(context, "Geçmiş bir tarih seçildi, bir sonraki periyoda ayarlandı.", Toast.LENGTH_SHORT).show();
            } else {
                // Tek seferlikse ve tarih geçmişse, alarmı kurma
                Toast.makeText(context, "Geçmiş tarihe tek seferlik hatırlatma kurulamaz.", Toast.LENGTH_LONG).show();
                shouldSchedule = false; // Bayrağı false yap
            }
        }

        if (shouldSchedule) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Log.d("ReminderScheduler", "Alarm kuruldu: " + notMetni + " - Zaman: " + calendar.getTime());

            if (context instanceof MainActivity) {
                ((MainActivity) context).guncelleNotHatirlatici(notMetni, tekrarTipi, calendar);
                Toast.makeText(context, "Hatırlatıcı ayarlandı.", Toast.LENGTH_SHORT).show();
            }

        } else {
            // Alarm kurulmadıysa, (varsa) JSON'daki eski hatırlatıcıyı temizle
            MainActivity.removeReminderFromJson(context, notMetni);
        }
    }

    public static void cancelReminder(Context context, String notMetni) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int alarmId = notMetni.hashCode(); // İptal edilecek alarmın ID'si, kurarken kullanılanla aynı olmalı.

        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);

        // DEĞİŞİKLİK: FLAG_NO_CREATE yerine FLAG_UPDATE_CURRENT kullanmak, mevcut bir PendingIntent'i bulmanın
        // daha güvenilir bir yoludur.
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // PendingIntent her zaman null olmayacaktır bu bayrakla, bu yüzden kontrolü kaldırıp doğrudan iptal ediyoruz.
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel(); // PendingIntent'i de sistemden temizle

        Log.d("ReminderScheduler", "Alarm iptal edildi: " + notMetni);
    }
}
