package vn.linh.doomweather;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;

/* loaded from: classes3.dex */
public class WeatherWidget extends AppWidgetProvider {
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    static final String REFRESH = "vn.linh.doomweather.REFRESH";
    static final String TEST_NOTIF = "vn.linh.doomweather.TEST_NOTIF";

    @Override // android.appwidget.AppWidgetProvider, android.content.BroadcastReceiver
    public void onReceive(Context c, Intent intent) {
        if (REFRESH.equals(intent.getAction())) {
            refreshAsync(c);
        } else if (TEST_NOTIF.equals(intent.getAction())) {
            WeatherNotifier.sendSummaryNow(c.getApplicationContext());
        } else {
            super.onReceive(c, intent);
        }
    }

    @Override // android.appwidget.AppWidgetProvider
    public void onUpdate(Context c, AppWidgetManager manager, int[] ids) {
        refreshAsync(c);
    }

    @Override // android.appwidget.AppWidgetProvider
    public void onAppWidgetOptionsChanged(Context c, AppWidgetManager m, int id, Bundle options) {
        render(c);
    }

    private void refreshAsync(Context c) {
        final Context app = c.getApplicationContext();
        render(app);
        final BroadcastReceiver.PendingResult pending = goAsync();
        new Thread(new Runnable() { // from class: vn.linh.doomweather.WeatherWidget$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                WeatherWidget.lambda$refreshAsync$1(app, pending);
            }
        }, "widget-weather").start();
    }

    /* JADX WARN: Removed duplicated region for block: B:11:0x0025 A[Catch: all -> 0x00ea, Exception -> 0x00ec, TryCatch #0 {Exception -> 0x00ec, blocks: (B:3:0x0008, B:6:0x0015, B:11:0x0025, B:13:0x003e, B:15:0x0051, B:17:0x006a, B:19:0x0081, B:23:0x00d5, B:29:0x008f, B:31:0x0096, B:34:0x00a7, B:36:0x00bc, B:40:0x00a3), top: B:2:0x0008, outer: #1 }] */
    /* JADX WARN: Removed duplicated region for block: B:36:0x00bc A[Catch: all -> 0x00ea, Exception -> 0x00ec, TryCatch #0 {Exception -> 0x00ec, blocks: (B:3:0x0008, B:6:0x0015, B:11:0x0025, B:13:0x003e, B:15:0x0051, B:17:0x006a, B:19:0x0081, B:23:0x00d5, B:29:0x008f, B:31:0x0096, B:34:0x00a7, B:36:0x00bc, B:40:0x00a3), top: B:2:0x0008, outer: #1 }] */
    /* JADX WARN: Removed duplicated region for block: B:37:0x00cc  */
    /* JADX WARN: Removed duplicated region for block: B:39:0x00a0  */
    /* JADX WARN: Removed duplicated region for block: B:40:0x00a3 A[Catch: all -> 0x00ea, Exception -> 0x00ec, TryCatch #0 {Exception -> 0x00ec, blocks: (B:3:0x0008, B:6:0x0015, B:11:0x0025, B:13:0x003e, B:15:0x0051, B:17:0x006a, B:19:0x0081, B:23:0x00d5, B:29:0x008f, B:31:0x0096, B:34:0x00a7, B:36:0x00bc, B:40:0x00a3), top: B:2:0x0008, outer: #1 }] */
    /* JADX WARN: Removed duplicated region for block: B:44:0x00d3  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    static /* synthetic */ void lambda$refreshAsync$1(final android.content.Context r27, final android.content.BroadcastReceiver.PendingResult r28) {
        /*
            Method dump skipped, instructions count: 300
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: vn.linh.doomweather.WeatherWidget.lambda$refreshAsync$1(android.content.Context, android.content.BroadcastReceiver$PendingResult):void");
    }

    static /* synthetic */ void lambda$refreshAsync$0(Context app, BroadcastReceiver.PendingResult pending) {
        try {
            render(app);
        } finally {
            pending.finish();
        }
    }

    static void requestUpdate(Context c) {
        c.sendBroadcast(new Intent(c, (Class<?>) WeatherWidget.class).setAction(REFRESH));
    }

    static /* synthetic */ void lambda$render$2() {
    }

    static void render(Context c) {
        render(c, new Runnable() { // from class: vn.linh.doomweather.WeatherWidget$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                WeatherWidget.lambda$render$2();
            }
        });
    }

    static void render(Context c, Runnable done) {
        try {
            try {
                AppWidgetManager manager = AppWidgetManager.getInstance(c);
                int[] ids = manager.getAppWidgetIds(new ComponentName(c, (Class<?>) WeatherWidget.class));
                int length = ids.length;
                int i = 0;
                int i2 = 0;
                while (i2 < length) {
                    int id = ids[i2];
                    Bundle options = manager.getAppWidgetOptions(id);
                    int width = Math.max(220, Math.min(600, options.getInt("appWidgetMinWidth", 340)));
                    int height = Math.max(105, Math.min(300, options.getInt("appWidgetMinHeight", 170)));
                    Bitmap bitmap = WidgetPainter.paint(c, width, height);
                    RemoteViews views = new RemoteViews(c.getPackageName(), R.layout.weather_widget);
                    views.setViewVisibility(R.id.placeholder, 8);
                    views.setViewVisibility(R.id.refresh, 8);
                    views.setImageViewBitmap(R.id.weather_image, bitmap);
                    views.setContentDescription(R.id.weather_image, WidgetPainter.description(c));
                    views.setOnClickPendingIntent(R.id.weather_image, PendingIntent.getActivity(c, i, new Intent(c, (Class<?>) MainActivity.class), 201326592));
                    views.setOnClickPendingIntent(R.id.refresh, PendingIntent.getBroadcast(c, 1, new Intent(c, (Class<?>) WeatherWidget.class).setAction(REFRESH), 201326592));
                    manager.updateAppWidget(id, views);
                    i2++;
                    i = 0;
                }
            } catch (Exception e) {
                Log.w("DoomWeather", "Widget render failed", e);
            }
        } finally {
            done.run();
        }
    }
}
