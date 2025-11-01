package com.hasandagasan.anidefteri;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Cihazın açılıp açılmadığını kontrol et (güvenlik önlemi)
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            // Kullanıcıya bir geri bildirim vermek için (test amaçlı, sonra kaldırılabilir)
            Toast.makeText(context, "Anı Defteri alarmları yeniden kuruluyor...", Toast.LENGTH_LONG).show();

            // TODO: Buraya, MainActivity'de veya başka bir yardımcı sınıfta bulunan,
            // JSON dosyasını okuyup tüm aktif hatırlatıcıları yeniden kuran
            // metodu çağıran kod gelecek.
            // Örnek: ReminderScheduler.rescheduleAlarms(context);
        }
    }
}
