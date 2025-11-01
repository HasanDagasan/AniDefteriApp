package com.hasandagasan.anidefteri;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;
import java.util.Calendar;
public class ReminderScheduler {

    public static void scheduleReminder(Context context, Calendar calendar, String notMetni, String tekrarTipi, String kayitTarihi) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Android 12 ve üzeri için tam zamanlı alarm iznini kontrol et
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // İzin yoksa kullanıcıyı ayarlara yönlendir (isteğe bağlı ama önerilir)
                Toast.makeText(context, "Tam zamanlı alarm izni gerekli.", Toast.LENGTH_LONG).show();
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

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            if (!tekrarTipi.equals("Tek Seferlik")) {
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
            }
        }
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    public static void cancelReminder(Context context, String notMetni) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int alarmId = notMetni.hashCode();

        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        // Eğer PendingIntent varsa (yani alarm kuruluysa), iptal et.
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }
}
