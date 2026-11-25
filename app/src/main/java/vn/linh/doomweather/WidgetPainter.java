package vn.linh.doomweather;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.Log;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONObject;

final class WidgetPainter {
    static final int BLUE = -10169601;
    private static final int MAX_PIXELS = 260000;
    static final int SNOW = -4528387;
    static final int STORM = -340971;
    static final int WHITE = -1;
    static final int YELLOW = -10742;
    private static final Map<String, Bitmap> themeBitmaps = new HashMap<>();

    private WidgetPainter() {
    }

    static String description(Context c) {
        JSONObject d = Store.cached(c);
        JSONObject cur = d == null ? null : d.optJSONObject("current");
        return Store.location(c).optString("name")
            + ", "
            + (cur == null
                ? "Chưa có dữ liệu"
                : Math.round(cur.optDouble("temperature_2m")) + " độ, " + WeatherMath.condition(cur.optInt("weather_code")))
            + ". Chạm để xem chi tiết.";
    }

    private static Bitmap getThemeBitmap(Context context, String assetName) {
        synchronized (themeBitmaps) {
            Bitmap cached = themeBitmaps.get(assetName);
            if (cached != null && !cached.isRecycled()) {
                return cached;
            }
            try (InputStream is = context.getAssets().open(assetName)) {
                Bitmap decoded = BitmapFactory.decodeStream(is);
                if (decoded != null) {
                    themeBitmaps.put(assetName, decoded);
                }
                return decoded;
            } catch (Exception e) {
                Log.w("DoomWeather", "Failed to load " + assetName, e);
                return null;
            }
        }
    }

    private static void drawMcuWidgetBg(
        Canvas canvas,
        Paint p,
        Context context,
        String assetName,
        float w,
        float h,
        int[] gradColors,
        int leftScrimDark,
        int leftScrimMid
    ) {
        RectF bounds = new RectF(0.0f, 0.0f, w, h);
        p.setShader(new LinearGradient(0.0f, 0.0f, w, h, gradColors, null, Shader.TileMode.CLAMP));
        canvas.drawRect(bounds, p);
        p.setShader(null);
        Bitmap bg = getThemeBitmap(context, assetName);
        if (bg != null) {
            float srcScale = Math.max(w / bg.getWidth(), h / bg.getHeight());
            float dw = bg.getWidth() * srcScale;
            float dh = bg.getHeight() * srcScale;
            float dx = w - dw;
            float dy = (h - dh) * 0.45f;
            p.setFilterBitmap(true);
            canvas.drawBitmap(bg, (Rect) null, new RectF(dx, dy, dx + dw, dy + dh), p);
            p.setFilterBitmap(false);
        }
        p.setShader(new LinearGradient(
            0.0f, 0.0f, w, 0.0f,
            new int[]{leftScrimDark, leftScrimMid, 100663295 & leftScrimMid},
            new float[]{0.0f, 0.48f, 1.0f},
            Shader.TileMode.CLAMP
        ));
        canvas.drawRect(bounds, p);
        p.setShader(new LinearGradient(
            0.0f, h * 0.54f, 0.0f, h,
            new int[]{0, (16777215 & leftScrimDark) | 1207959552},
            null,
            Shader.TileMode.CLAMP
        ));
        canvas.drawRect(bounds, p);
        p.setShader(null);
    }

    static Bitmap paint(Context context, int widthDp, int heightDp) {
        return paint(context, widthDp, heightDp, Store.getTheme(context));
    }

    static Bitmap paint(Context context, int widthDp, int heightDp, String rawTheme) {
        String theme = Store.normalizeTheme(rawTheme);
        float w = 364.0f;
        float h = 176.0f;
        float aspect = (widthDp > 0 && heightDp > 0) ? ((float) widthDp / (float) heightDp) : (w / h);
        aspect = Math.max(1.5f, Math.min(2.6f, aspect));
        int bmpW = Math.round((float) Math.sqrt(MAX_PIXELS * aspect));
        int bmpH = Math.max(120, Math.round(bmpW / aspect));

        Bitmap bitmap = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        float sx = bmpW / w;
        float sy = bmpH / h;
        canvas.scale(sx, sy);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        RectF card = new RectF(0.0f, 0.0f, w, h);
        Path clip = new Path();
        clip.addRoundRect(card, 24.0f, 24.0f, Path.Direction.CW);
        canvas.save();
        canvas.clipPath(clip);

        JSONObject loc = Store.location(context);
        JSONObject data = Store.cached(context);
        JSONObject cur = data == null ? null : data.optJSONObject("current");
        JSONObject daily = data == null ? null : data.optJSONObject("daily");
        int code = cur == null ? 0 : cur.optInt("weather_code", 0);
        boolean isDay = cur == null || cur.optInt("is_day", 1) != 0;

        // 1. Background by theme
        int borderColor;
        switch (theme) {
            case "doomsday":
                drawMcuWidgetBg(
                    canvas, p, context, "doomsday_widget_bg.webp", w, h,
                    new int[]{0xFF071B1E, 0xFF0C2A32, 0xFF10203A},
                    0x7A051418, 0x260A2028
                );
                borderColor = 0x7A6EE7B7;
                break;
            case "doom":
                drawMcuWidgetBg(
                    canvas, p, context, "doom_widget_bg.webp", w, h,
                    new int[]{0xFF0B2E20, 0xFF0F3B28, 0xFF185C3E},
                    0x70072116, 0x220D3826
                );
                borderColor = 0x7A6EE7B7;
                break;
            case "mcu":
                drawMcuWidgetBg(
                    canvas, p, context, "mcu_widget_bg.webp", w, h,
                    new int[]{0xFF0A1430, 0xFF102046, 0xFF1B336B},
                    0x75080E24, 0x240C1634
                );
                borderColor = 0x6667E8F9;
                break;
            case "mcu_ironman":
                drawMcuWidgetBg(
                    canvas, p, context, "mcu_ironman_widget_bg.webp", w, h,
                    new int[]{0xFF3A0C12, 0xFF5A1622, 0xFF8C281E},
                    0x6838090F, 0x2068141B
                );
                borderColor = 0x73FCD34D;
                break;
            case "mcu_spiderman":
                drawMcuWidgetBg(
                    canvas, p, context, "mcu_spiderman_widget_bg.webp", w, h,
                    new int[]{0xFF0E3F8A, 0xFF1558B0, 0xFF2B7CE0},
                    0x5C0C2E6B, 0x1E154794
                );
                borderColor = 0x73FB7185;
                break;
            case "mcu_thor":
                drawMcuWidgetBg(
                    canvas, p, context, "mcu_thor_widget_bg.webp", w, h,
                    new int[]{0xFF0A1F44, 0xFF0F2D5E, 0xFF1A4E8F},
                    0x64091B3D, 0x22103066
                );
                borderColor = 0x7A7DD3FC;
                break;
            case "mcu_strange":
                drawMcuWidgetBg(
                    canvas, p, context, "mcu_strange_widget_bg.webp", w, h,
                    new int[]{0xFF1E0B45, 0xFF2E1065, 0xFF55218A},
                    0x6A1B0A3E, 0x2233136E
                );
                borderColor = 0x7AFDBA74;
                break;
            case "wallpaper":
                p.setShader(new LinearGradient(
                    0.0f, 0.0f, w, h,
                    new int[]{0xFF543DBA, 0xFF7C62DB, 0xFFA884E4, 0xFFCB9EDE},
                    new float[]{0.0f, 0.52f, 0.85f, 1.0f},
                    Shader.TileMode.CLAMP
                ));
                canvas.drawRect(card, p);
                p.setShader(null);
                borderColor = 0x60FFFFFF;
                break;
            default: {
                int[] skyColors;
                if (code >= 51 || code == 3 || code == 45 || code == 48) {
                    skyColors = new int[]{0xFF3B4F68, 0xFF506682, 0xFF677E9B};
                } else if (!isDay) {
                    skyColors = new int[]{0xFF0D1B3E, 0xFF172E60, 0xFF24417A};
                } else {
                    skyColors = new int[]{0xFF1A60B6, 0xFF3586DC, 0xFF5CA3EB};
                }
                p.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, h, skyColors, null, Shader.TileMode.CLAMP));
                canvas.drawRect(card, p);
                p.setShader(null);
                borderColor = 0x3DFFFFFF;
                break;
            }
        }

        // Subtle text shadow for crisp legibility on artwork backgrounds
        boolean artTheme = !"ios".equals(theme) && !"wallpaper".equals(theme);
        if (artTheme) {
            p.setShadowLayer(4.0f, 0.0f, 1.2f, 0xB8000000);
        } else {
            p.setShadowLayer(2.0f, 0.0f, 1.0f, 0x33000000);
        }

        // 2. Top-left: City & Current Temp
        String city = ellipsize(loc.optString("name", "Tokyo"), p, 18.5f, 215.0f);
        text(canvas, p, city, 18.0f, 34.0f, 18.5f, WHITE, true);

        if (cur == null) {
            textThin(canvas, p, "—°", 17.0f, 82.0f, 46.0f, WHITE);
            boolean offline = Store.prefs(context).getBoolean("offline", false);
            text(canvas, p, offline ? "Không tải được thời tiết. Chạm để thử lại." : "Đang tải thời tiết…",
                18.0f, 150.0f, 12.5f, 0xE0FFFFFF, false);
        } else {
            long temp = Math.round(cur.optDouble("temperature_2m"));
            textThin(canvas, p, temp + "°", 17.0f, 82.0f, 48.0f, WHITE);

            // Top-right: Icon, Condition, High/Low
            p.clearShadowLayer();
            icon(canvas, p, code, isDay, w - 27.0f, 27.0f);
            if (artTheme) {
                p.setShadowLayer(4.0f, 0.0f, 1.2f, 0xB8000000);
            }
            right(canvas, p, WeatherMath.condition(code), w - 18.0f, 58.0f, 13.5f, WHITE);
            if (daily != null) {
                JSONArray dMax = daily.optJSONArray("temperature_2m_max");
                JSONArray dMin = daily.optJSONArray("temperature_2m_min");
                if (dMax != null && dMin != null && dMax.length() > 0 && dMin.length() > 0) {
                    String hiLo = "C:" + Math.round(dMax.optDouble(0)) + "°  T:" + Math.round(dMin.optDouble(0)) + "°";
                    right(canvas, p, hiLo, w - 18.0f, 76.0f, 13.5f, 0xF2FFFFFF);
                }
            }

            // Bottom row: 6-hour forecast
            JSONObject hourly = data.optJSONObject("hourly");
            int utcOffset = data.optInt("utc_offset_seconds", 0);
            int drawn = 0;
            if (hourly != null) {
                JSONArray times = hourly.optJSONArray("time");
                JSONArray temps = hourly.optJSONArray("temperature_2m");
                JSONArray codes = hourly.optJSONArray("weather_code");
                JSONArray days = hourly.optJSONArray("is_day");
                if (times != null && temps != null && codes != null && days != null) {
                    long nowSec = System.currentTimeMillis() / 1000L;
                    int len = Math.min(Math.min(times.length(), temps.length()), Math.min(codes.length(), days.length()));
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    float startX = 34.0f;
                    float stepX = (w - 68.0f) / 5.0f;
                    for (int i = 0; i < len && drawn < 6; i++) {
                        long t = times.optLong(i, 0L);
                        if (t < nowSec) {
                            continue;
                        }
                        float cx = startX + drawn * stepX;
                        cal.setTimeInMillis((t + utcOffset) * 1000L);
                        String hr = cal.get(Calendar.HOUR_OF_DAY) + "h";
                        centerText(canvas, p, hr, cx, 114.0f, 11.5f, 0xD2FFFFFF, true);
                        p.clearShadowLayer();
                        icon(canvas, p, codes.optInt(i, 0), days.optInt(i, 1) != 0, cx, 133.0f);
                        if (artTheme) {
                            p.setShadowLayer(4.0f, 0.0f, 1.2f, 0xB8000000);
                        }
                        centerText(canvas, p, Math.round(temps.optDouble(i)) + "°", cx, 160.0f, 14.0f, WHITE, true);
                        drawn++;
                    }
                }
            }
            if (drawn == 0) {
                text(canvas, p, "Chưa có dự báo theo giờ", 18.0f, 150.0f, 12.5f, 0xE0FFFFFF, false);
            }
        }

        // Subtle inner card border
        p.clearShadowLayer();
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.2f);
        p.setColor(borderColor);
        canvas.drawRoundRect(new RectF(0.6f, 0.6f, w - 0.6f, h - 0.6f), 23.4f, 23.4f, p);
        canvas.restore();
        return bitmap;
    }

    static void text(Canvas canvas, Paint paint, String str, float f, float f2, float f3, int i, boolean z) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(i);
        paint.setTextSize(f3);
        paint.setLetterSpacing(z ? -0.015f : -0.005f);
        paint.setTypeface(Typeface.create(z ? "sans-serif-medium" : "sans-serif", Typeface.NORMAL));
        canvas.drawText(str, f, f2, paint);
    }

    static void textThin(Canvas c, Paint p, String s, float x, float y, float size, int color) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        p.setTextSize(size);
        p.setLetterSpacing(-0.05f);
        p.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        c.drawText(s, x, y, p);
        p.setLetterSpacing(0.0f);
    }

    static void centerText(Canvas canvas, Paint paint, String str, float f, float f2, float f3, int i, boolean z) {
        paint.setTypeface(Typeface.create(z ? "sans-serif-medium" : "sans-serif", Typeface.NORMAL));
        paint.setLetterSpacing(z ? -0.01f : 0.0f);
        paint.setTextSize(f3);
        text(canvas, paint, str, f - (paint.measureText(str) / 2.0f), f2, f3, i, z);
    }

    static void right(Canvas c, Paint p, String s, float x, float y, float size, int color) {
        p.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        p.setLetterSpacing(-0.01f);
        p.setTextSize(size);
        text(c, p, s, x - p.measureText(s), y, size, color, true);
    }

    static String ellipsize(String s, Paint p, float size, float max) {
        p.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        p.setLetterSpacing(-0.015f);
        p.setTextSize(size);
        if (p.measureText(s) <= max) {
            return s;
        }
        while (!s.isEmpty() && p.measureText(s + "…") > max) {
            s = s.substring(0, s.length() - 1);
        }
        return s + "…";
    }

    static void icon(Canvas c, Paint p, int code, boolean day, float x, float y) {
        p.setStyle(Paint.Style.FILL);
        if (code == 0) {
            if (day) {
                p.setColor(YELLOW);
                c.drawCircle(x, y, 5.2f, p);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(1.6f);
                p.setStrokeCap(Paint.Cap.ROUND);
                for (int a = 0; a < 8; a++) {
                    double rad = (a * Math.PI) / 4.0d;
                    float cs = (float) Math.cos(rad);
                    float sn = (float) Math.sin(rad);
                    c.drawLine((cs * 7.2f) + x, (sn * 7.2f) + y, (cs * 9.6f) + x, (9.6f * sn) + y, p);
                }
                p.setStyle(Paint.Style.FILL);
                return;
            }
            p.setColor(-1708289);
            Path moon = new Path();
            moon.addCircle(x, y, 8.2f, Path.Direction.CW);
            Path cut = new Path();
            cut.addCircle(x + 4.4f, y - 3.6f, 7.0f, Path.Direction.CW);
            moon.op(cut, Path.Op.DIFFERENCE);
            c.drawPath(moon, p);
            return;
        }
        if (code <= 2) {
            if (day) {
                p.setColor(YELLOW);
                c.drawCircle(x - 3.5f, y - 3.2f, 4.2f, p);
            } else {
                p.setColor(-2299649);
                Path moon2 = new Path();
                moon2.addCircle(x - 3.0f, y - 3.2f, 4.6f, Path.Direction.CW);
                Path cut2 = new Path();
                cut2.addCircle(x - 0.5f, y - 5.2f, 4.0f, Path.Direction.CW);
                moon2.op(cut2, Path.Op.DIFFERENCE);
                c.drawPath(moon2, p);
            }
            drawCloud(c, p, x + 0.5f, y + 1.0f, 0.9f);
            return;
        }
        if (code == 3 || code == 45 || code == 48) {
            drawCloud(c, p, x, y + 0.5f, 1.0f);
            return;
        }
        drawCloud(c, p, x, y - 1.5f, 0.95f);
        if ((code >= 71 && code <= 77) || code == 85 || code == 86) {
            p.setColor(SNOW);
            c.drawCircle(x - 4.5f, y + 7.5f, 1.3f, p);
            c.drawCircle(x, 9.2f + y, 1.3f, p);
            c.drawCircle(4.5f + x, y + 7.5f, 1.3f, p);
            return;
        }
        if (code >= 95) {
            p.setColor(STORM);
            Path bolt = new Path();
            bolt.moveTo(x + 1.2f, 3.5f + y);
            bolt.lineTo(x - 3.0f, y + 8.8f);
            bolt.lineTo(0.4f + x, 8.8f + y);
            bolt.lineTo(x - 1.2f, 13.5f + y);
            bolt.lineTo(4.8f + x, y + 7.2f);
            bolt.lineTo(1.2f + x, y + 7.2f);
            bolt.close();
            c.drawPath(bolt, p);
            return;
        }
        p.setColor(BLUE);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.8f);
        p.setStrokeCap(Paint.Cap.ROUND);
        c.drawLine(x - 4.0f, y + 6.5f, x - 5.2f, y + 10.2f, p);
        c.drawLine(x + 0.2f, y + 6.5f, x - 1.0f, y + 10.2f, p);
        c.drawLine(x + 4.4f, y + 6.5f, x + 3.2f, y + 10.2f, p);
        p.setStyle(Paint.Style.FILL);
    }

    private static void drawCloud(Canvas c, Paint p, float x, float y, float s) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(-789258);
        c.drawCircle(x - (4.5f * s), (0.5f * s) + y, 4.2f * s, p);
        c.drawCircle((0.8f * s) + x, y - (2.4f * s), 5.4f * s, p);
        c.drawCircle((5.8f * s) + x, (1.0f * s) + y, 3.6f * s, p);
        c.drawRoundRect(new RectF(x - (8.5f * s), y, (9.4f * s) + x, (4.6f * s) + y), s * 2.3f, 2.3f * s, p);
    }
}
