package vn.linh.doomweather;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.function.Consumer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import vn.linh.doomweather.HtmlWidgetRenderer;

/* loaded from: classes3.dex */
final class HtmlWidgetRenderer {
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    HtmlWidgetRenderer() {
    }

    static void render(final Context context, final int width, final int height, final Consumer<Bitmap> completed) {
        final String payload = payload(context).toString();
        MAIN.post(new Runnable() { // from class: vn.linh.doomweather.HtmlWidgetRenderer$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                HtmlWidgetRenderer.lambda$render$1(context, completed, payload, width, height);
            }
        });
    }

    static /* synthetic */ void lambda$render$1(Context context, final Consumer completed, String payload, int width, int height) {
        final WebView web = new WebView(context);
        final boolean[] finished = {false};
        Runnable fallback = new Runnable() { // from class: vn.linh.doomweather.HtmlWidgetRenderer$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                HtmlWidgetRenderer.lambda$render$0(finished, web, completed);
            }
        };
        try {
            web.setBackgroundColor(0);
            web.setLayerType(1, null);
            web.getSettings().setJavaScriptEnabled(true);
            web.getSettings().setAllowFileAccess(false);
            web.getSettings().setAllowContentAccess(false);
            web.getSettings().setBlockNetworkLoads(true);
            float density = context.getResources().getDisplayMetrics().density;
            int baseWidth = Math.round(364.0f * density);
            int baseHeight = Math.round(176.0f * density);
            web.measure(View.MeasureSpec.makeMeasureSpec(baseWidth, 1073741824), View.MeasureSpec.makeMeasureSpec(baseHeight, 1073741824));
            web.layout(0, 0, baseWidth, baseHeight);
            try {
                try {
                    web.setWebViewClient(new AnonymousClass1(payload, finished, width, height, baseWidth, baseHeight, web, fallback, completed));
                    ByteArrayOutputStream body = new ByteArrayOutputStream();
                    InputStream input = context.getAssets().open("widget.html");
                    try {
                        byte[] buffer = new byte[4096];
                        while (true) {
                            int n = input.read(buffer);
                            if (n == -1) {
                                break;
                            } else {
                                body.write(buffer, 0, n);
                            }
                        }
                        if (input != null) {
                            input.close();
                        }
                        try {
                            web.loadDataWithBaseURL("https://widget.invalid/", body.toString("UTF-8"), "text/html", "UTF-8", null);
                            MAIN.postDelayed(fallback, 2500L);
                        } catch (Exception e) {
                            e = e;
                            Log.w("DoomWeather", "HTML renderer failed", e);
                            fallback.run();
                        }
                    } finally {
                    }
                } catch (Exception e2) {
                    e = e2;
                }
            } catch (Exception e3) {
                e = e3;
            }
        } catch (Exception e4) {
            e = e4;
        }
    }

    static /* synthetic */ void lambda$render$0(boolean[] finished, WebView web, Consumer completed) {
        if (finished[0]) {
            return;
        }
        finished[0] = true;
        web.destroy();
        completed.accept(null);
    }

    /* renamed from: vn.linh.doomweather.HtmlWidgetRenderer$1, reason: invalid class name */
    class AnonymousClass1 extends WebViewClient {
        final /* synthetic */ int val$baseHeight;
        final /* synthetic */ int val$baseWidth;
        final /* synthetic */ Consumer val$completed;
        final /* synthetic */ Runnable val$fallback;
        final /* synthetic */ boolean[] val$finished;
        final /* synthetic */ int val$height;
        final /* synthetic */ String val$payload;
        final /* synthetic */ WebView val$web;
        final /* synthetic */ int val$width;

        AnonymousClass1(String str, boolean[] zArr, int i, int i2, int i3, int i4, WebView webView, Runnable runnable, Consumer consumer) {
            this.val$payload = str;
            this.val$finished = zArr;
            this.val$width = i;
            this.val$height = i2;
            this.val$baseWidth = i3;
            this.val$baseHeight = i4;
            this.val$web = webView;
            this.val$fallback = runnable;
            this.val$completed = consumer;
        }

        @Override // android.webkit.WebViewClient
        public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
            return true;
        }

        @Override // android.webkit.WebViewClient
        public void onPageFinished(WebView v, String url) {
            String str = "renderWidget(" + this.val$payload + ")";
            final boolean[] zArr = this.val$finished;
            final int i = this.val$width;
            final int i2 = this.val$height;
            final int i3 = this.val$baseWidth;
            final int i4 = this.val$baseHeight;
            final WebView webView = this.val$web;
            final Runnable runnable = this.val$fallback;
            final Consumer consumer = this.val$completed;
            v.evaluateJavascript(str, new ValueCallback() { // from class: vn.linh.doomweather.HtmlWidgetRenderer$1$$ExternalSyntheticLambda0
                @Override // android.webkit.ValueCallback
                public final void onReceiveValue(Object obj) {
                    HtmlWidgetRenderer.MAIN.postDelayed(new Runnable() { // from class: vn.linh.doomweather.HtmlWidgetRenderer$1$$ExternalSyntheticLambda1
                        @Override // java.lang.Runnable
                        public final void run() {
                            HtmlWidgetRenderer.AnonymousClass1.lambda$onPageFinished$0(r1, r2, r3, r4, r5, r6, r7, r8);
                        }
                    }, 220L);
                }
            });
        }

        static /* synthetic */ void lambda$onPageFinished$0(boolean[] finished, int width, int height, int baseWidth, int baseHeight, WebView web, Runnable fallback, Consumer completed) {
            if (finished[0]) {
                return;
            }
            try {
                Bitmap bitmap = Bitmap.createBitmap(width * 2, height * 2, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                float scale = Math.min((width * 2.0f) / baseWidth, (height * 2.0f) / baseHeight);
                canvas.translate(((width * 2) - (baseWidth * scale)) / 2.0f, ((height * 2) - (baseHeight * scale)) / 2.0f);
                canvas.scale(scale, scale);
                web.draw(canvas);
                finished[0] = true;
                HtmlWidgetRenderer.MAIN.removeCallbacks(fallback);
                web.destroy();
                completed.accept(bitmap);
            } catch (Exception e) {
                Log.w("DoomWeather", "HTML capture failed", e);
                fallback.run();
            }
        }
    }

    static JSONObject payload(Context context) {
        JSONObject data;
        JSONObject data2;
        long j;
        JSONObject data3;
        JSONObject current;
        JSONArray times;
        JSONObject result = new JSONObject();
        try {
            result.put("name", Store.location(context).getString("name"));
            result.put("theme", Store.getTheme(context));
            result.put("offline", Store.prefs(context).getBoolean("offline", false));
            result.put("stale", System.currentTimeMillis() - Store.prefs(context).getLong("updated", 0L) > 3600000);
            data = Store.cached(context);
        } catch (JSONException e) {
            Log.w("DoomWeather", "Widget payload unavailable", e);
        }
        if (data == null) {
            return result;
        }
        JSONObject current2 = data.getJSONObject("current");
        JSONObject daily = data.getJSONObject("daily");
        result.put("temp", Math.round(current2.getDouble("temperature_2m"))).put("condition", WeatherMath.condition(current2.getInt("weather_code")));
        result.put("code", current2.getInt("weather_code")).put("day", current2.optInt("is_day", 1));
        result.put("high", Math.round(daily.getJSONArray("temperature_2m_max").getDouble(0))).put("low", Math.round(daily.getJSONArray("temperature_2m_min").getDouble(0)));
        result.put("utcOffset", data.optInt("utc_offset_seconds", 0));
        JSONObject hourly = data.optJSONObject("hourly");
        JSONArray hours = new JSONArray();
        long j2 = 1000;
        if (hourly == null) {
            data2 = data;
            j = 1000;
        } else {
            JSONArray times2 = hourly.getJSONArray("time");
            long now = System.currentTimeMillis() / 1000;
            int i = 0;
            while (true) {
                j = j2;
                if (i >= times2.length() || hours.length() >= 6) {
                    break;
                }
                JSONArray times3 = times2;
                if (times3.getLong(i) >= now) {
                    data3 = data;
                    current = current2;
                    times = times3;
                    JSONObject hour = new JSONObject().put("time", times3.getLong(i)).put("temp", Math.round(hourly.getJSONArray("temperature_2m").getDouble(i))).put("code", hourly.getJSONArray("weather_code").getInt(i)).put("day", hourly.getJSONArray("is_day").getInt(i));
                    hours.put(hour);
                } else {
                    data3 = data;
                    current = current2;
                    times = times3;
                }
                i++;
                JSONObject jSONObject = current;
                times2 = times;
                current2 = jSONObject;
                data = data3;
                j2 = j;
            }
            data2 = data;
        }
        result.put("hours", hours);
        JSONObject min = data2.optJSONObject("minutely_15");
        JSONArray rain = new JSONArray();
        if (min != null) {
            JSONArray t = min.getJSONArray("time");
            JSONArray r = min.getJSONArray("precipitation");
            long[] times4 = new long[t.length()];
            double[] values = new double[r.length()];
            for (int i2 = 0; i2 < times4.length; i2++) {
                times4[i2] = t.getLong(i2);
            }
            for (int i3 = 0; i3 < values.length; i3++) {
                values[i3] = r.isNull(i3) ? Double.NaN : r.getDouble(i3);
            }
            for (double v : WeatherMath.nextRain(times4, values, System.currentTimeMillis() / j)) {
                rain.put(v);
            }
        }
        result.put("rain", rain);
        return result;
    }
}
