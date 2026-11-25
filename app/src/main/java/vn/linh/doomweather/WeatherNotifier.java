package vn.linh.doomweather;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.Calendar;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes3.dex */
final class WeatherNotifier {
    static final String CHANNEL_ID = "weather_alerts";
    private static final int ID_EVENING = 1003;
    private static final int ID_MORNING = 1002;
    private static final int ID_RAIN = 1001;
    private static final int ID_TEST = 1004;
    private static final long RAIN_COOLDOWN_MS = 10800000;

    WeatherNotifier() {
    }

    static boolean canNotify(Context c) {
        return Build.VERSION.SDK_INT < 33 || c.checkSelfPermission("android.permission.POST_NOTIFICATIONS") == 0;
    }

    /* JADX WARN: Removed duplicated region for block: B:102:0x0426  */
    /* JADX WARN: Removed duplicated region for block: B:115:0x0512  */
    /* JADX WARN: Removed duplicated region for block: B:142:0x02c0  */
    /* JADX WARN: Removed duplicated region for block: B:162:0x0405  */
    /* JADX WARN: Removed duplicated region for block: B:165:0x01f6  */
    /* JADX WARN: Removed duplicated region for block: B:34:0x012f A[Catch: Exception -> 0x05ce, TRY_ENTER, TryCatch #0 {Exception -> 0x05ce, blocks: (B:10:0x001e, B:15:0x002b, B:16:0x0031, B:18:0x009e, B:21:0x00b0, B:23:0x00b6, B:25:0x00bc, B:26:0x00d8, B:28:0x00e1, B:30:0x00e7, B:31:0x0119, B:34:0x012f, B:37:0x013f, B:39:0x016b, B:44:0x019d, B:45:0x01a9, B:46:0x01ae, B:48:0x01b4, B:52:0x01bd, B:57:0x01c6, B:64:0x020c, B:66:0x0217, B:69:0x0228, B:74:0x0235, B:76:0x023b, B:78:0x0246, B:82:0x0261, B:83:0x0251, B:85:0x025b, B:86:0x025f, B:92:0x0268, B:140:0x02b3, B:144:0x02c4, B:145:0x03ed, B:152:0x0316, B:155:0x0356, B:156:0x0339, B:157:0x038c, B:159:0x03b1, B:160:0x03cd), top: B:9:0x001e }] */
    /* JADX WARN: Removed duplicated region for block: B:66:0x0217 A[Catch: Exception -> 0x05ce, TryCatch #0 {Exception -> 0x05ce, blocks: (B:10:0x001e, B:15:0x002b, B:16:0x0031, B:18:0x009e, B:21:0x00b0, B:23:0x00b6, B:25:0x00bc, B:26:0x00d8, B:28:0x00e1, B:30:0x00e7, B:31:0x0119, B:34:0x012f, B:37:0x013f, B:39:0x016b, B:44:0x019d, B:45:0x01a9, B:46:0x01ae, B:48:0x01b4, B:52:0x01bd, B:57:0x01c6, B:64:0x020c, B:66:0x0217, B:69:0x0228, B:74:0x0235, B:76:0x023b, B:78:0x0246, B:82:0x0261, B:83:0x0251, B:85:0x025b, B:86:0x025f, B:92:0x0268, B:140:0x02b3, B:144:0x02c4, B:145:0x03ed, B:152:0x0316, B:155:0x0356, B:156:0x0339, B:157:0x038c, B:159:0x03b1, B:160:0x03cd), top: B:9:0x001e }] */
    /* JADX WARN: Removed duplicated region for block: B:95:0x0287  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    static void maybeNotify(android.content.Context r61, org.json.JSONObject r62, org.json.JSONObject r63) {
        /*
            Method dump skipped, instructions count: 1493
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: vn.linh.doomweather.WeatherNotifier.maybeNotify(android.content.Context, org.json.JSONObject, org.json.JSONObject):void");
    }

    static void sendSummaryNow(Context c) {
        String body;
        String title;
        String place;
        if (canNotify(c)) {
            JSONObject loc = Store.location(c);
            String place2 = loc.optString("name", "Tokyo");
            JSONObject data = Store.cached(c);
            String title2 = place2 + " · Thời tiết hiện tại";
            String body2 = "Chạm để mở dự báo chi tiết và cảnh báo mưa.";
            if (data != null) {
                try {
                    JSONObject cur = data.getJSONObject("current");
                    JSONObject daily = data.optJSONObject("daily");
                    int code = cur.getInt("weather_code");
                    long temp = Math.round(cur.getDouble("temperature_2m"));
                    String cond = WeatherMath.condition(code);
                    title2 = place2 + " · " + temp + "° " + cond;
                    try {
                        StringBuilder sb = new StringBuilder();
                        if (daily == null) {
                            title = title2;
                            place = "Chạm để mở dự báo chi tiết và cảnh báo mưa.";
                        } else {
                            try {
                                JSONArray dMax = daily.optJSONArray("temperature_2m_max");
                                JSONArray dMin = daily.optJSONArray("temperature_2m_min");
                                if (dMax == null || dMin == null || dMax.length() <= 0 || dMin.length() <= 0) {
                                    title = title2;
                                    place = "Chạm để mở dự báo chi tiết và cảnh báo mưa.";
                                } else {
                                    try {
                                        try {
                                            title = title2;
                                            place = "Chạm để mở dự báo chi tiết và cảnh báo mưa.";
                                        } catch (Exception e) {
                                            body = "Chạm để mở dự báo chi tiết và cảnh báo mưa.";
                                        }
                                    } catch (Exception e2) {
                                        body = "Chạm để mở dự báo chi tiết và cảnh báo mưa.";
                                    }
                                    try {
                                        sb.append("C:").append(Math.round(dMax.optDouble(0))).append("° T:").append(Math.round(dMin.optDouble(0))).append("°");
                                    } catch (Exception e3) {
                                        body = place;
                                        title2 = title;
                                        body2 = body;
                                        post(c, ID_TEST, title2, body2);
                                    }
                                }
                            } catch (Exception e4) {
                                body = "Chạm để mở dự báo chi tiết và cảnh báo mưa.";
                            }
                        }
                        int nextProb = 0;
                        try {
                            JSONObject hourly = data.optJSONObject("hourly");
                            if (hourly == null) {
                                body = place;
                            } else {
                                JSONArray hTimes = hourly.optJSONArray("time");
                                JSONArray hProbs = hourly.optJSONArray("precipitation_probability");
                                if (hTimes == null || hProbs == null) {
                                    body = place;
                                } else {
                                    long nowSec = System.currentTimeMillis() / 1000;
                                    int checked = 0;
                                    body = place;
                                    int i = 0;
                                    while (true) {
                                        JSONObject data2 = data;
                                        try {
                                            JSONObject hourly2 = hourly;
                                            if (i >= Math.min(hTimes.length(), hProbs.length()) || checked >= 6) {
                                                break;
                                            }
                                            if (hTimes.optLong(i, 0L) >= nowSec - 1800) {
                                                nextProb = Math.max(nextProb, hProbs.optInt(i, 0));
                                                checked++;
                                            }
                                            i++;
                                            data = data2;
                                            hourly = hourly2;
                                        } catch (Exception e5) {
                                            title2 = title;
                                            body2 = body;
                                            post(c, ID_TEST, title2, body2);
                                        }
                                    }
                                }
                            }
                            if (sb.length() > 0) {
                                sb.append(" · ");
                            }
                            sb.append(nextProb >= 40 ? "Khả năng mưa " + nextProb + "% trong 6h tới" : "Ít khả năng mưa trong 6h tới");
                            body2 = sb.toString();
                            title2 = title;
                        } catch (Exception e6) {
                            body = place;
                            title2 = title;
                        }
                    } catch (Exception e7) {
                        body = "Chạm để mở dự báo chi tiết và cảnh báo mưa.";
                    }
                } catch (Exception e8) {
                    body = "Chạm để mở dự báo chi tiết và cảnh báo mưa.";
                }
            }
            post(c, ID_TEST, title2, body2);
        }
    }

    private static String formatDay(Calendar cal) {
        return String.format(Locale.US, "%04d-%02d-%02d", Integer.valueOf(cal.get(1)), Integer.valueOf(cal.get(2) + 1), Integer.valueOf(cal.get(5)));
    }

    private static void post(Context c, int id, String title, String body) {
        NotificationManager nm = (NotificationManager) c.getSystemService("notification");
        if (nm == null) {
            return;
        }
        NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Thông báo thời tiết", 3);
        ch.setDescription("Cảnh báo mưa và tóm tắt thời tiết sáng/tối");
        nm.createNotificationChannel(ch);
        Intent openIntent = new Intent(c, (Class<?>) MainActivity.class).setFlags(335544320);
        PendingIntent pi = PendingIntent.getActivity(c, 0, openIntent, 201326592);
        Notification n = new Notification.Builder(c, CHANNEL_ID).setSmallIcon(R.drawable.ic_notif_thor).setContentTitle(title).setContentText(body).setStyle(new Notification.BigTextStyle().bigText(body)).setAutoCancel(true).setContentIntent(pi).build();
        nm.notify(id, n);
    }
}
