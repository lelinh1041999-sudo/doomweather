package vn.linh.doomweather;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;

/* loaded from: classes3.dex */
final class Store {
    Store() {
    }

    static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("weather", 0);
    }

    static JSONObject location(Context c) {
        try {
            return new JSONObject(prefs(c).getString("location", "{\"name\":\"Tokyo\",\"lat\":35.6762,\"lon\":139.6503}"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static String key(JSONObject loc) {
        return loc.optDouble("lat") + "," + loc.optDouble("lon");
    }

    static synchronized void setLocation(Context c, String name, double lat, double lon) throws Exception {
        synchronized (Store.class) {
            setLocation(c, name, lat, lon, false);
        }
    }

    static synchronized void setLocation(Context c, String name, double lat, double lon, boolean isGps) throws Exception {
        synchronized (Store.class) {
            if (!WeatherMath.validLocation(lat, lon)) {
                throw new IllegalArgumentException("Vị trí không hợp lệ");
            }
            JSONObject obj = new JSONObject().put("name", name.substring(0, Math.min(80, name.length()))).put("lat", lat).put("lon", lon).put("gps", isGps);
            prefs(c).edit().putString("location", obj.toString()).apply();
        }
    }

    static synchronized boolean saveWeather(Context c, JSONObject loc, JSONObject data) {
        synchronized (Store.class) {
            if (!key(loc).equals(key(location(c)))) {
                return false;
            }
            prefs(c).edit().putString("data", data.toString()).putString("data_key", key(loc)).putLong("updated", System.currentTimeMillis()).putBoolean("offline", false).apply();
            return true;
        }
    }

    static JSONObject cached(Context c) {
        if (!key(location(c)).equals(prefs(c).getString("data_key", ""))) {
            return null;
        }
        try {
            return new JSONObject(prefs(c).getString("data", ""));
        } catch (Exception e) {
            return null;
        }
    }

    static String normalizeTheme(String theme) {
        return ("wallpaper".equals(theme) || "mcu".equals(theme) || "mcu_ironman".equals(theme) || "mcu_spiderman".equals(theme) || "mcu_thor".equals(theme) || "mcu_strange".equals(theme) || "doom".equals(theme) || "ios".equals(theme)) ? theme : "ios";
    }

    static String getTheme(Context c) {
        return normalizeTheme(prefs(c).getString("theme", "ios"));
    }

    static synchronized void setTheme(Context c, String theme) {
        synchronized (Store.class) {
            prefs(c).edit().putString("theme", normalizeTheme(theme)).apply();
        }
    }

    static boolean isNotifEnabled(Context c) {
        return prefs(c).getBoolean("notif_enabled", true);
    }

    static synchronized void setNotifEnabled(Context c, boolean enabled) {
        synchronized (Store.class) {
            prefs(c).edit().putBoolean("notif_enabled", enabled).apply();
        }
    }
}
