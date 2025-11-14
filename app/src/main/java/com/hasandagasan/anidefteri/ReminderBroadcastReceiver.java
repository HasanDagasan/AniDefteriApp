package com.hasandagasan.anidefteri;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ReminderBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String notMetni = intent.getStringExtra("NOT_METNI");
        int notificationId = intent.getIntExtra("NOTIFICATION_ID", 0);
        String tekrarTipi = intent.getStringExtra("TEKRAR_TIPI");
        String kayitTarihiStr = intent.getStringExtra("KAYIT_TARIHI");
        String zamanFarkiBasligi = getZamanFarkiMetni(kayitTarihiStr);

        Intent activityIntent = new Intent(context, MainActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // MainActivity'ye hangi fragment'ı açacağını ve hangi veriyi taşıyacağını söyle
        activityIntent.putExtra("OPEN_FRAGMENT", "HatirlatmaDetayFragment");
        activityIntent.putExtra("NOT_METNI", notMetni);
        activityIntent.putExtra("KAYIT_TARIHI", kayitTarihiStr);

        // Her bildirim için benzersiz bir requestCode kullanarak PendingIntent oluştur
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId, // Benzersiz ID
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "reminder_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Anı Defteri Hatırlatıcıları", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(largeIcon)
                .setContentTitle(zamanFarkiBasligi)
                .setContentText(notMetni)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notMetni))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(notificationId, builder.build());

        // TEKRARLI ALARMLARI YENİDEN KURMA
        if (tekrarTipi != null) {
            if (tekrarTipi.equals("Tek Seferlik")) {
                // Eğer hatırlatıcı tek seferlik ise, MainActivity'deki static metodu doğrudan çağır.
                // Bu, uygulama kapalıyken bile %100 çalışır.
                Log.d("Receiver", "Tek seferlik hatırlatıcı için static silme metodu çağrılıyor.");
                MainActivity.removeReminderFromJson(context, notMetni);

                // 2. YENİ: MainActivity'e arayüzünü güncellemesi için anons gönder
                Log.d("Receiver", "Tek seferlik hatırlatıcı silindi, MainActivity'e anons gönderiliyor.");
                Intent updateUIIntent = new Intent("REMINDER_DELETED_ACTION");
                LocalBroadcastManager.getInstance(context).sendBroadcast(updateUIIntent);

            } else {
                // Eğer tekrarlı ise, bir sonraki alarmı kur.
                Calendar calendar = Calendar.getInstance();
                // ... (switch-case kodunuz aynı)
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
                ReminderScheduler.scheduleReminder(context, calendar, notMetni, tekrarTipi, kayitTarihiStr);
            }
        }
    }
    private String getZamanFarkiMetni(String kayitTarihiStr) {
        if (kayitTarihiStr == null) {
            return "Anı Defteri Hatırlatıcısı";
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date kayitTarihi = format.parse(kayitTarihiStr);
            Date simdikiZaman = new Date();

            long farkMilisaniye = simdikiZaman.getTime() - kayitTarihi.getTime();
            long farkSaniye = TimeUnit.MILLISECONDS.toSeconds(farkMilisaniye);
            long farkDakika = TimeUnit.MILLISECONDS.toMinutes(farkMilisaniye);
            long farkSaat = TimeUnit.MILLISECONDS.toHours(farkMilisaniye);
            long farkGun = TimeUnit.MILLISECONDS.toDays(farkMilisaniye);

            if(farkSaniye < 60){
                return farkSaniye + " saniye önce bu sözü kaydettin, hatırlamak ister misin?";
            }else if (farkDakika < 60) {
                return farkDakika + " dakika önce bu sözü kaydettin, hatırlamak ister misin?";
            } else if (farkSaat < 24) {
                return farkSaat + " saat önce bu sözü kaydettin, hatırlamak ister misin?";
            } else if (farkGun < 7) {
                return farkGun + " gün önce bu sözü kaydettin, hatırlamak ister misin?";
            } else if (farkGun < 30) {
                return (farkGun / 7) + " hafta önce bu sözü kaydettin, hatırlamak ister misin?";
            } else if (farkGun < 365) {
                return (farkGun / 30) + " ay önce bu sözü kaydettin, hatırlamak ister misin?";
            } else {
                return (farkGun / 365) + " yıl önce bu sözü kaydettin, hatırlamak ister misin?";
            }

        } catch (ParseException e) {
            e.printStackTrace();
            return "Anı Defteri Hatırlatıcısı";
        }
    }
}
