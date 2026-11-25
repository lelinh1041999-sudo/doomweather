package vn.linh.doomweather;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;

final class Store {
    static final String[] THEMES = {
        "doomsday",
        "doom",
        "mcu",
        "mcu_ironman",
        "mcu_spiderman",
        "mcu_thor",
        "mcu_strange",
        "ios",
        "wallpaper"
    };

    private Store() {
    }

    static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("weather", Context.MODE_PRIVATE);
    }

    static JSONObject location(Context c) {
        try {
            return new JSONObject(prefs(c).getString(
                "location",
                "{\"name\":\"Tokyo\",\"lat\":35.6762,\"lon\":139.6503}"
            ));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static String key(JSONObject loc) {
        return loc.optDouble("lat") + "," + loc.optDouble("lon");
    }

    static synchronized void setLocation(Context c, String name, double lat, double lon) throws Exception {
        setLocation(c, name, lat, lon, false);
    }

    static synchronized void setLocation(Context c, String name, double lat, double lon, boolean isGps) throws Exception {
        if (!WeatherMath.validLocation(lat, lon)) {
            throw new IllegalArgumentException("Vị trí không hợp lệ");
        }
        JSONObject obj = new JSONObject()
            .put("name", name.substring(0, Math.min(80, name.length())))
            .put("lat", lat)
            .put("lon", lon)
            .put("gps", isGps);
        prefs(c).edit().putString("location", obj.toString()).apply();
    }

    static synchronized boolean saveWeather(Context c, JSONObject loc, JSONObject data) {
        if (!key(loc).equals(key(location(c)))) {
            return false;
        }
        prefs(c).edit()
            .putString("data", data.toString())
            .putString("data_key", key(loc))
            .putLong("updated", System.currentTimeMillis())
            .putBoolean("offline", false)
            .apply();
        return true;
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
        if (theme != null) {
            for (String valid : THEMES) {
                if (valid.equals(theme)) {
                    return valid;
                }
            }
        }
        return "ios";
    }

    static String getTheme(Context c) {
        return normalizeTheme(prefs(c).getString("theme", "ios"));
    }

    static synchronized void setTheme(Context c, String theme) {
        prefs(c).edit().putString("theme", normalizeTheme(theme)).apply();
    }

    static String getWidgetTheme(Context c, int appWidgetId) {
        String custom = prefs(c).getString("widget_theme_" + appWidgetId, null);
        return custom == null ? getTheme(c) : normalizeTheme(custom);
    }

    static synchronized void setWidgetTheme(Context c, int appWidgetId, String theme) {
        prefs(c).edit().putString("widget_theme_" + appWidgetId, normalizeTheme(theme)).apply();
    }

    static synchronized void clearWidgetTheme(Context c, int appWidgetId) {
        prefs(c).edit().remove("widget_theme_" + appWidgetId).apply();
    }

    static String nextTheme(String current) {
        String normalized = normalizeTheme(current);
        for (int i = 0; i < THEMES.length; i++) {
            if (THEMES[i].equals(normalized)) {
                return THEMES[(i + 1) % THEMES.length];
            }
        }
        return THEMES[0];
    }

    static synchronized String cycleWidgetTheme(Context c, int appWidgetId, boolean syncGlobal) {
        String next = nextTheme(getWidgetTheme(c, appWidgetId));
        setWidgetTheme(c, appWidgetId, next);
        if (syncGlobal) {
            setTheme(c, next);
        }
        return next;
    }

    static boolean isNotifEnabled(Context c) {
        return prefs(c).getBoolean("notif_enabled", true);
    }

    static synchronized void setNotifEnabled(Context c, boolean enabled) {
        prefs(c).edit().putBoolean("notif_enabled", enabled).apply();
    }
}
