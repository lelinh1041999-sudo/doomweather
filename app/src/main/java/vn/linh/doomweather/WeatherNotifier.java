package vn.linh.doomweather;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import java.util.Calendar;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

final class WeatherNotifier {
    static final String CHANNEL_ID = "weather_alerts";
    private static final int ID_RAIN = 1001;
    private static final int ID_MORNING = 1002;
    private static final int ID_EVENING = 1003;
    private static final int ID_TEST = 1004;
    private static final long RAIN_COOLDOWN_MS = 10_800_000L;

    private WeatherNotifier() {
    }

    static boolean canNotify(Context c) {
        return Build.VERSION.SDK_INT < 33 || c.checkSelfPermission("android.permission.POST_NOTIFICATIONS") == 0;
    }

    static void maybeNotify(Context c, JSONObject loc, JSONObject data) {
        if (c == null || loc == null || data == null || !Store.isNotifEnabled(c) || !canNotify(c)) {
            return;
        }
        try {
            SharedPreferences prefs = Store.prefs(c);
            long nowMs = System.currentTimeMillis();
            long nowSec = nowMs / 1000L;
            String place = loc.optString("name", "Tokyo");
            JSONObject cur = data.optJSONObject("current");
            JSONObject daily = data.optJSONObject("daily");
            JSONObject hourly = data.optJSONObject("hourly");
            if (cur == null) {
                return;
            }

            // 1. Rain alert (next 2 hours)
            long lastRain = prefs.getLong("notif_rain_ts", 0L);
            if (nowMs - lastRain >= RAIN_COOLDOWN_MS && hourly != null) {
                JSONArray hTimes = hourly.optJSONArray("time");
                JSONArray hProbs = hourly.optJSONArray("precipitation_probability");
                int maxProb = 0;
                if (hTimes != null && hProbs != null) {
                    int checked = 0;
                    for (int i = 0; i < Math.min(hTimes.length(), hProbs.length()) && checked < 2; i++) {
                        if (hTimes.optLong(i, 0L) >= nowSec - 900) {
                            maxProb = Math.max(maxProb, hProbs.optInt(i, 0));
                            checked++;
                        }
                    }
                }
                if (maxProb >= 60) {
                    prefs.edit().putLong("notif_rain_ts", nowMs).apply();
                    post(c, ID_RAIN, place + " · Sắp có mưa (" + maxProb + "%)", "Nhớ mang theo ô hoặc áo mưa nhé.");
                }
            }

            // 2. Morning (7-9h) / Evening (19-21h) daily summary
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            String today = formatDay(cal);
            long temp = Math.round(cur.optDouble("temperature_2m"));
            String cond = WeatherMath.condition(cur.optInt("weather_code", 0));

            String hiLo = "";
            if (daily != null) {
                JSONArray dMax = daily.optJSONArray("temperature_2m_max");
                JSONArray dMin = daily.optJSONArray("temperature_2m_min");
                if (dMax != null && dMin != null && dMax.length() > 0 && dMin.length() > 0) {
                    hiLo = "C:" + Math.round(dMax.optDouble(0)) + "° T:" + Math.round(dMin.optDouble(0)) + "°";
                }
            }

            if (hour >= 7 && hour <= 9 && !today.equals(prefs.getString("notif_morning_day", ""))) {
                prefs.edit().putString("notif_morning_day", today).apply();
                post(c, ID_MORNING, place + " · Sáng nay " + temp + "° " + cond, hiLo.isEmpty() ? "Chạm để xem dự báo hôm nay." : hiLo + " · Chạm để xem chi tiết.");
            } else if (hour >= 19 && hour <= 21 && !today.equals(prefs.getString("notif_evening_day", ""))) {
                prefs.edit().putString("notif_evening_day", today).apply();
                post(c, ID_EVENING, place + " · Tối nay " + temp + "° " + cond, hiLo.isEmpty() ? "Chạm để xem dự báo ngày mai." : hiLo + " · Chạm để xem dự báo ngày mai.");
            }
        } catch (Exception e) {
            Log.w("DoomWeather", "maybeNotify failed", e);
        }
    }

    static void sendSummaryNow(Context c) {
        if (!canNotify(c)) {
            return;
        }
        JSONObject loc = Store.location(c);
        String place = loc.optString("name", "Tokyo");
        JSONObject data = Store.cached(c);
        String title = place + " · Thời tiết hiện tại";
        String body = "Chạm để mở dự báo chi tiết và cảnh báo mưa.";
        if (data != null) {
            try {
                JSONObject cur = data.getJSONObject("current");
                JSONObject daily = data.optJSONObject("daily");
                int code = cur.getInt("weather_code");
                long temp = Math.round(cur.getDouble("temperature_2m"));
                String cond = WeatherMath.condition(code);
                title = place + " · " + temp + "° " + cond;

                StringBuilder sb = new StringBuilder();
                if (daily != null) {
                    JSONArray dMax = daily.optJSONArray("temperature_2m_max");
                    JSONArray dMin = daily.optJSONArray("temperature_2m_min");
                    if (dMax != null && dMin != null && dMax.length() > 0 && dMin.length() > 0) {
                        sb.append("C:").append(Math.round(dMax.optDouble(0)))
                            .append("° T:").append(Math.round(dMin.optDouble(0))).append("°");
                    }
                }
                int nextProb = 0;
                JSONObject hourly = data.optJSONObject("hourly");
                if (hourly != null) {
                    JSONArray hTimes = hourly.optJSONArray("time");
                    JSONArray hProbs = hourly.optJSONArray("precipitation_probability");
                    if (hTimes != null && hProbs != null) {
                        long nowSec = System.currentTimeMillis() / 1000L;
                        int checked = 0;
                        for (int i = 0; i < Math.min(hTimes.length(), hProbs.length()) && checked < 6; i++) {
                            if (hTimes.optLong(i, 0L) >= nowSec - 1800) {
                                nextProb = Math.max(nextProb, hProbs.optInt(i, 0));
                                checked++;
                            }
                        }
                    }
                }
                if (sb.length() > 0) {
                    sb.append(" · ");
                }
                sb.append(nextProb >= 40
                    ? "Khả năng mưa " + nextProb + "% trong 6h tới"
                    : "Ít khả năng mưa trong 6h tới");
                body = sb.toString();
            } catch (Exception ignored) {
            }
        }
        post(c, ID_TEST, title, body);
    }

    private static String formatDay(Calendar cal) {
        return String.format(Locale.US, "%04d-%02d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH));
    }

    private static void post(Context c, int id, String title, String body) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) {
            return;
        }
        NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Thông báo thời tiết", NotificationManager.IMPORTANCE_DEFAULT);
        ch.setDescription("Cảnh báo mưa và tóm tắt thời tiết sáng/tối");
        nm.createNotificationChannel(ch);
        Intent openIntent = new Intent(c, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(c, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification n = new Notification.Builder(c, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notif_thor)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(new Notification.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build();
        nm.notify(id, n);
    }
}
