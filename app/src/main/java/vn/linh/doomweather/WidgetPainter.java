package vn.linh.doomweather;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.Log;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/* loaded from: classes3.dex */
final class WidgetPainter {
    static final int BLUE = -10169601;
    private static final int MAX_PIXELS = 165000;
    static final int SNOW = -4528387;
    static final int STORM = -340971;
    static final int WHITE = -1;
    static final int YELLOW = -10742;
    private static final Map<String, Bitmap> themeBitmaps = new HashMap();

    WidgetPainter() {
    }

    static String description(Context c) {
        JSONObject d = Store.cached(c);
        JSONObject cur = d == null ? null : d.optJSONObject("current");
        return Store.location(c).optString("name") + ", " + (cur == null ? "Chưa có dữ liệu" : Math.round(cur.optDouble("temperature_2m")) + " độ, " + WeatherMath.condition(cur.optInt("weather_code"))) + ". Chạm để xem chi tiết.";
    }

    private static Bitmap getThemeBitmap(Context context, String assetName) {
        synchronized (themeBitmaps) {
            Bitmap cached = themeBitmaps.get(assetName);
            if (cached != null && !cached.isRecycled()) {
                return cached;
            }
            try {
                InputStream is = context.getAssets().open(assetName);
                try {
                    Bitmap decoded = BitmapFactory.decodeStream(is);
                    if (decoded != null) {
                        themeBitmaps.put(assetName, decoded);
                    }
                    if (is != null) {
                        is.close();
                    }
                    return decoded;
                } catch (Throwable th) {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            } catch (Exception e) {
                Log.w("DoomWeather", "Failed to load " + assetName, e);
                return null;
            }
        }
    }

    private static void drawMcuWidgetBg(Canvas canvas, Paint p, Context context, String assetName, float w, float h, int[] gradColors, int leftScrimDark, int leftScrimMid) {
        RectF bounds = new RectF(0.0f, 0.0f, w, h);
        p.setShader(new LinearGradient(0.0f, 0.0f, w, h, gradColors, (float[]) null, Shader.TileMode.CLAMP));
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
        p.setShader(new LinearGradient(0.0f, 0.0f, w, 0.0f, new int[]{leftScrimDark, leftScrimMid, 100663295 & leftScrimMid}, new float[]{0.0f, 0.48f, 1.0f}, Shader.TileMode.CLAMP));
        canvas.drawRect(bounds, p);
        p.setShader(new LinearGradient(0.0f, h * 0.54f, 0.0f, h, new int[]{0, (16777215 & leftScrimDark) | 1207959552}, (float[]) null, Shader.TileMode.CLAMP));
        canvas.drawRect(bounds, p);
        p.setShader(null);
    }

    /* JADX WARN: Removed duplicated region for block: B:102:0x066e  */
    /* JADX WARN: Removed duplicated region for block: B:104:0x0671  */
    /* JADX WARN: Removed duplicated region for block: B:120:0x018e  */
    /* JADX WARN: Removed duplicated region for block: B:20:0x00f7  */
    /* JADX WARN: Removed duplicated region for block: B:23:0x03ed  */
    /* JADX WARN: Removed duplicated region for block: B:31:0x0432  */
    /* JADX WARN: Unreachable blocks removed: 2, instructions: 3 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    static android.graphics.Bitmap paint(android.content.Context r59, int r60, int r61) {
        /*
            Method dump skipped, instructions count: 1742
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: vn.linh.doomweather.WidgetPainter.paint(android.content.Context, int, int):android.graphics.Bitmap");
    }

    static void text(Canvas canvas, Paint paint, String str, float f, float f2, float f3, int i, boolean z) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(i);
        paint.setTextSize(f3);
        paint.setTypeface(Typeface.create("sans-serif", z ? 1 : 0));
        canvas.drawText(str, f, f2, paint);
    }

    static void textThin(Canvas c, Paint p, String s, float x, float y, float size, int color) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        p.setTextSize(size);
        p.setTypeface(Typeface.create("sans-serif-light", 0));
        c.drawText(s, x, y, p);
    }

    static void centerText(Canvas canvas, Paint paint, String str, float f, float f2, float f3, int i, boolean z) {
        paint.setTypeface(Typeface.create("sans-serif", z ? 1 : 0));
        paint.setTextSize(f3);
        text(canvas, paint, str, f - (paint.measureText(str) / 2.0f), f2, f3, i, z);
    }

    static void right(Canvas c, Paint p, String s, float x, float y, float size, int color) {
        p.setTypeface(Typeface.create("sans-serif", 0));
        p.setTextSize(size);
        text(c, p, s, x - p.measureText(s), y, size, color, false);
    }

    static String ellipsize(String s, Paint p, float size, float max) {
        p.setTypeface(Typeface.create("sans-serif", 1));
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
                    double rad = (a * 3.141592653589793d) / 4.0d;
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
