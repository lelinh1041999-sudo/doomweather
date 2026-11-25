package vn.linh.doomweather;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.function.Consumer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class HtmlWidgetRenderer {
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private HtmlWidgetRenderer() {
    }

    static void render(final Context context, final int width, final int height, final Consumer<Bitmap> completed) {
        final String payload = payload(context).toString();
        MAIN.post(() -> {
            final WebView web = new WebView(context);
            final boolean[] finished = {false};
            final Runnable fallback = () -> {
                if (finished[0]) {
                    return;
                }
                finished[0] = true;
                web.destroy();
                completed.accept(null);
            };
            try {
                web.setBackgroundColor(0);
                web.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                web.getSettings().setJavaScriptEnabled(true);
                web.getSettings().setAllowFileAccess(false);
                web.getSettings().setAllowContentAccess(false);
                web.getSettings().setBlockNetworkLoads(true);
                float density = context.getResources().getDisplayMetrics().density;
                final int baseWidth = Math.round(364.0f * density);
                final int baseHeight = Math.round(176.0f * density);
                web.measure(
                    View.MeasureSpec.makeMeasureSpec(baseWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(baseHeight, View.MeasureSpec.EXACTLY)
                );
                web.layout(0, 0, baseWidth, baseHeight);
                web.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                        return true;
                    }

                    @Override
                    public void onPageFinished(WebView v, String url) {
                        v.evaluateJavascript("renderWidget(" + payload + ")", value -> MAIN.postDelayed(() -> {
                            if (finished[0]) {
                                return;
                            }
                            try {
                                Bitmap bitmap = Bitmap.createBitmap(width * 2, height * 2, Bitmap.Config.ARGB_8888);
                                Canvas canvas = new Canvas(bitmap);
                                float scale = Math.min((width * 2.0f) / baseWidth, (height * 2.0f) / baseHeight);
                                canvas.translate(
                                    ((width * 2) - (baseWidth * scale)) / 2.0f,
                                    ((height * 2) - (baseHeight * scale)) / 2.0f
                                );
                                canvas.scale(scale, scale);
                                web.draw(canvas);
                                finished[0] = true;
                                MAIN.removeCallbacks(fallback);
                                web.destroy();
                                completed.accept(bitmap);
                            } catch (Exception e) {
                                Log.w("DoomWeather", "HTML capture failed", e);
                                fallback.run();
                            }
                        }, 220L));
                    }
                });
                ByteArrayOutputStream body = new ByteArrayOutputStream();
                try (InputStream input = context.getAssets().open("widget.html")) {
                    byte[] buffer = new byte[4096];
                    int n;
                    while ((n = input.read(buffer)) != -1) {
                        body.write(buffer, 0, n);
                    }
                }
                web.loadDataWithBaseURL("https://widget.invalid/", body.toString("UTF-8"), "text/html", "UTF-8", null);
                MAIN.postDelayed(fallback, 2500L);
            } catch (Exception e) {
                Log.w("DoomWeather", "HTML renderer failed", e);
                fallback.run();
            }
        });
    }

    static JSONObject payload(Context context) {
        JSONObject result = new JSONObject();
        try {
            result.put("name", Store.location(context).getString("name"));
            result.put("theme", Store.getTheme(context));
            result.put("offline", Store.prefs(context).getBoolean("offline", false));
            result.put("stale", System.currentTimeMillis() - Store.prefs(context).getLong("updated", 0L) > 3600000L);
            JSONObject data = Store.cached(context);
            if (data == null) {
                return result;
            }
            JSONObject current = data.getJSONObject("current");
            JSONObject daily = data.getJSONObject("daily");
            result.put("temp", Math.round(current.getDouble("temperature_2m")))
                .put("condition", WeatherMath.condition(current.getInt("weather_code")));
            result.put("code", current.getInt("weather_code"))
                .put("day", current.optInt("is_day", 1));
            result.put("high", Math.round(daily.getJSONArray("temperature_2m_max").getDouble(0)))
                .put("low", Math.round(daily.getJSONArray("temperature_2m_min").getDouble(0)));
            result.put("utcOffset", data.optInt("utc_offset_seconds", 0));

            JSONObject hourly = data.optJSONObject("hourly");
            JSONArray hours = new JSONArray();
            if (hourly != null) {
                JSONArray times = hourly.getJSONArray("time");
                long now = System.currentTimeMillis() / 1000L;
                for (int i = 0; i < times.length() && hours.length() < 6; i++) {
                    if (times.getLong(i) >= now) {
                        JSONObject hour = new JSONObject()
                            .put("time", times.getLong(i))
                            .put("temp", Math.round(hourly.getJSONArray("temperature_2m").getDouble(i)))
                            .put("code", hourly.getJSONArray("weather_code").getInt(i))
                            .put("day", hourly.getJSONArray("is_day").getInt(i));
                        hours.put(hour);
                    }
                }
            }
            result.put("hours", hours);

            JSONObject min = data.optJSONObject("minutely_15");
            JSONArray rain = new JSONArray();
            if (min != null) {
                JSONArray t = min.getJSONArray("time");
                JSONArray r = min.getJSONArray("precipitation");
                long[] timesArr = new long[t.length()];
                double[] values = new double[r.length()];
                for (int i = 0; i < timesArr.length; i++) {
                    timesArr[i] = t.getLong(i);
                }
                for (int i = 0; i < values.length; i++) {
                    values[i] = r.isNull(i) ? Double.NaN : r.getDouble(i);
                }
                for (double v : WeatherMath.nextRain(timesArr, values, System.currentTimeMillis() / 1000L)) {
                    rain.put(v);
                }
            }
            result.put("rain", rain);
        } catch (JSONException e) {
            Log.w("DoomWeather", "Widget payload unavailable", e);
        }
        return result;
    }
}
