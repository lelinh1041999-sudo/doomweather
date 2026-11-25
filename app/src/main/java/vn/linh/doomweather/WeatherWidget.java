package vn.linh.doomweather;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import org.json.JSONObject;

public class WeatherWidget extends AppWidgetProvider {
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    static final String REFRESH = "vn.linh.doomweather.REFRESH";
    static final String TEST_NOTIF = "vn.linh.doomweather.TEST_NOTIF";
    static final String NEXT_THEME = "vn.linh.doomweather.NEXT_THEME";
    static final String EXTRA_THEME = "theme";

    @Override
    public void onReceive(Context c, Intent intent) {
        String action = intent == null ? null : intent.getAction();
        if (REFRESH.equals(action)) {
            refreshAsync(c);
        } else if (TEST_NOTIF.equals(action)) {
            WeatherNotifier.sendSummaryNow(c.getApplicationContext());
        } else if (NEXT_THEME.equals(action)) {
            int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                Context app = c.getApplicationContext();
                AppWidgetManager manager = AppWidgetManager.getInstance(app);
                int[] allIds = manager.getAppWidgetIds(new ComponentName(app, WeatherWidget.class));
                boolean syncGlobal = allIds == null || allIds.length <= 1;
                Store.cycleWidgetTheme(app, widgetId, syncGlobal);
                renderOne(app, manager, widgetId);
            } else {
                render(c.getApplicationContext());
            }
        } else {
            super.onReceive(c, intent);
        }
    }

    @Override
    public void onUpdate(Context c, AppWidgetManager manager, int[] ids) {
        refreshAsync(c);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context c, AppWidgetManager m, int id, Bundle options) {
        renderOne(c.getApplicationContext(), m, id);
    }

    @Override
    public void onDeleted(Context c, int[] appWidgetIds) {
        if (appWidgetIds != null) {
            for (int id : appWidgetIds) {
                Store.clearWidgetTheme(c, id);
            }
        }
        super.onDeleted(c, appWidgetIds);
    }

    private void refreshAsync(Context c) {
        final Context app = c.getApplicationContext();
        render(app);
        final BroadcastReceiver.PendingResult pending = goAsync();
        new Thread(() -> {
            try {
                JSONObject loc = Store.location(app);
                if (loc.optBoolean("gps", false) || "Vị trí của tôi".equals(loc.optString("name"))) {
                    Location fresh = WeatherClient.bestKnownLocation(app);
                    if (fresh != null && WeatherMath.validLocation(fresh.getLatitude(), fresh.getLongitude())) {
                        float[] dist = new float[1];
                        Location.distanceBetween(
                            loc.optDouble("lat"), loc.optDouble("lon"),
                            fresh.getLatitude(), fresh.getLongitude(),
                            dist
                        );
                        if (dist[0] > 250.0f) {
                            String resolved = WeatherClient.reverseGeocode(app, fresh.getLatitude(), fresh.getLongitude());
                            String name = (resolved == null || resolved.isEmpty()) ? loc.optString("name", "Vị trí của tôi") : resolved;
                            Store.setLocation(app, name, fresh.getLatitude(), fresh.getLongitude(), true);
                            loc = Store.location(app);
                        }
                    }
                }
                JSONObject data = WeatherClient.fetch(loc);
                if (Store.saveWeather(app, loc, data)) {
                    WeatherNotifier.maybeNotify(app, loc, data);
                }
            } catch (Exception e) {
                Log.w("DoomWeather", "Widget refresh failed", e);
                Store.prefs(app).edit().putBoolean("offline", true).apply();
            } finally {
                MAIN.post(() -> {
                    try {
                        render(app);
                    } finally {
                        pending.finish();
                    }
                });
            }
        }, "widget-weather").start();
    }

    static void requestUpdate(Context c) {
        c.sendBroadcast(new Intent(c, WeatherWidget.class).setAction(REFRESH));
    }

    static void syncSingleWidgetTheme(Context c, String theme) {
        try {
            AppWidgetManager manager = AppWidgetManager.getInstance(c);
            int[] ids = manager.getAppWidgetIds(new ComponentName(c, WeatherWidget.class));
            if (ids != null && ids.length == 1) {
                Store.setWidgetTheme(c, ids[0], theme);
            }
        } catch (Exception e) {
            Log.w("DoomWeather", "syncSingleWidgetTheme failed", e);
        }
    }

    static void render(Context c) {
        render(c, () -> {});
    }

    static void render(Context c, Runnable done) {
        try {
            AppWidgetManager manager = AppWidgetManager.getInstance(c);
            int[] ids = manager.getAppWidgetIds(new ComponentName(c, WeatherWidget.class));
            for (int id : ids) {
                renderOne(c, manager, id);
            }
        } catch (Exception e) {
            Log.w("DoomWeather", "Widget render failed", e);
        } finally {
            done.run();
        }
    }

    private static void renderOne(Context c, AppWidgetManager manager, int id) {
        try {
            Bundle options = manager.getAppWidgetOptions(id);
            int width = Math.max(220, Math.min(600, options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 340)));
            int height = Math.max(105, Math.min(300, options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 170)));
            String theme = Store.getWidgetTheme(c, id);

            Bitmap bitmap = WidgetPainter.paint(c, width, height, theme);
            RemoteViews views = new RemoteViews(c.getPackageName(), R.layout.weather_widget);
            views.setViewVisibility(R.id.placeholder, View.GONE);
            views.setViewVisibility(R.id.refresh, View.GONE);
            views.setImageViewBitmap(R.id.weather_image, bitmap);
            views.setContentDescription(R.id.weather_image, WidgetPainter.description(c));

            int piFlags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;

            Intent openIntent = new Intent(c, MainActivity.class)
                .putExtra(EXTRA_THEME, theme)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent openPi = PendingIntent.getActivity(c, id, openIntent, piFlags);
            views.setOnClickPendingIntent(R.id.weather_image, openPi);
            views.setOnClickPendingIntent(R.id.tap_open_app, openPi);

            Intent nextThemeIntent = new Intent(c, WeatherWidget.class)
                .setAction(NEXT_THEME)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
            PendingIntent nextThemePi = PendingIntent.getBroadcast(c, 10000 + id, nextThemeIntent, piFlags);
            views.setOnClickPendingIntent(R.id.tap_next_theme, nextThemePi);

            manager.updateAppWidget(id, views);
        } catch (Exception e) {
            Log.w("DoomWeather", "Widget renderOne failed for id=" + id, e);
        }
    }
}
