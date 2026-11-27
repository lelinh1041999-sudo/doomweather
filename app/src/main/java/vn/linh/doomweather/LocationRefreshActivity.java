package vn.linh.doomweather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONObject;

/**
 * Invisible foreground trampoline Activity launched when tapping the city name
 * on the home-screen widget. Running briefly as a foreground Activity avoids
 * Android 11-17 background location throttling and 10s BroadcastReceiver timeouts.
 */
public class LocationRefreshActivity extends Activity {
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private final AtomicBoolean done = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        final Context app = getApplicationContext();
        boolean hasFine = checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") == 0;
        boolean hasCoarse = checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == 0;
        if (!hasFine && !hasCoarse) {
            Toast.makeText(app, "Hãy cấp quyền vị trí trong Doom Weather", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finishQuietly();
            return;
        }

        Toast.makeText(app, "Đang cập nhật vị trí…", Toast.LENGTH_SHORT).show();

        // Safety timeout so the invisible Activity never lingers
        MAIN.postDelayed(this::finishQuietly, 8000L);

        new Thread(() -> {
            String msg = "Chưa lấy được vị trí GPS";
            try {
                Location fresh = WeatherClient.fetchFreshLocation(app);
                if (fresh != null && WeatherMath.validLocation(fresh.getLatitude(), fresh.getLongitude())) {
                    String resolved = WeatherClient.reverseGeocode(app, fresh.getLatitude(), fresh.getLongitude());
                    String name = (resolved == null || resolved.isEmpty()) ? "Vị trí của tôi" : resolved;
                    Store.setLocation(app, name, fresh.getLatitude(), fresh.getLongitude(), true);
                    JSONObject loc = Store.location(app);
                    try {
                        JSONObject data = WeatherClient.fetch(loc);
                        Store.saveWeather(app, loc, data);
                    } catch (Exception fetchErr) {
                        Log.w("DoomWeather", "Weather fetch after GPS update failed", fetchErr);
                    }
                    msg = "Đã cập nhật: " + name;
                }
            } catch (Exception e) {
                Log.w("DoomWeather", "LocationRefreshActivity failed", e);
                msg = "Không cập nhật được vị trí";
            } finally {
                final String finalMsg = msg;
                MAIN.post(() -> {
                    WeatherWidget.render(app);
                    Toast.makeText(app, finalMsg, Toast.LENGTH_SHORT).show();
                    finishQuietly();
                });
            }
        }, "widget-fg-location").start();
    }

    private void finishQuietly() {
        if (done.compareAndSet(false, true)) {
            if (!isFinishing()) {
                finish();
                overridePendingTransition(0, 0);
            }
        }
    }
}
