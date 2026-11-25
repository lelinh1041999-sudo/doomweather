package vn.linh.doomweather;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.ws.RealWebSocket;
import org.json.JSONObject;

/* loaded from: classes3.dex */
final class WeatherClient {
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder().connectTimeout(3, TimeUnit.SECONDS).readTimeout(6, TimeUnit.SECONDS).callTimeout(8, TimeUnit.SECONDS).fastFallback(true).build();

    WeatherClient() {
    }

    static JSONObject fetch(JSONObject loc) throws Exception {
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + loc.getDouble("lat") + "&longitude=" + loc.getDouble("lon") + "&current=temperature_2m,weather_code,is_day&daily=temperature_2m_max,temperature_2m_min,sunrise,sunset&hourly=temperature_2m,weather_code,is_day,precipitation_probability&minutely_15=precipitation&timezone=auto&timeformat=unixtime&forecast_days=2";
        Response response = CLIENT.newCall(new Request.Builder().url(url).build()).execute();
        try {
            if (!response.getIsSuccessful() || response.body() == null) {
                throw new IOException("HTTP " + response.code());
            }
            JSONObject data = new JSONObject(response.body().string());
            JSONObject current = data.getJSONObject("current");
            if (!Double.isFinite(current.getDouble("temperature_2m"))) {
                throw new IOException("Thiếu nhiệt độ");
            }
            data.getJSONObject("daily").getJSONArray("temperature_2m_max").getDouble(0);
            data.getJSONObject("daily").getJSONArray("temperature_2m_min").getDouble(0);
            if (response != null) {
                response.close();
            }
            return data;
        } catch (Throwable th) {
            if (response != null) {
                try {
                    response.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            throw th;
        }
    }

    static Location bestKnownLocation(Context context) {
        LocationManager lm;
        Location cand;
        if (context == null) {
            return null;
        }
        boolean hasFine = context.checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") == 0;
        boolean hasCoarse = context.checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == 0;
        if ((!hasFine && !hasCoarse) || (lm = (LocationManager) context.getSystemService("location")) == null) {
            return null;
        }
        List<String> providers = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 31) {
            providers.add("fused");
        }
        providers.add("gps");
        providers.add("network");
        Location best = null;
        for (String p : providers) {
            try {
                if (lm.isProviderEnabled(p) && (cand = lm.getLastKnownLocation(p)) != null && (best == null || cand.getTime() > best.getTime() + RealWebSocket.CANCEL_AFTER_CLOSE_MILLIS || (Math.abs(cand.getTime() - best.getTime()) <= RealWebSocket.CANCEL_AFTER_CLOSE_MILLIS && cand.hasAccuracy() && (!best.hasAccuracy() || cand.getAccuracy() < best.getAccuracy())))) {
                    best = cand;
                }
            } catch (SecurityException e) {
            }
        }
        return best;
    }

    static boolean needsJapaneseUpgrade(String name, double lat, double lon) {
        if ("Vị trí của tôi".equals(name)) {
            return true;
        }
        boolean inJapan = lat >= 24.0d && lat <= 46.0d && lon >= 122.0d && lon <= 146.0d;
        return inJapan && name != null && name.matches("^[\\x00-\\x7F]+$");
    }

    static String reverseGeocode(Context context, double lat, double lon) {
        if (!WeatherMath.validLocation(lat, lon)) {
            return null;
        }
        try {
        } catch (Exception e) {
            e = e;
        }
        try {
        } catch (Exception e2) {
            e = e2;
            Log.w("DoomWeather", "Nominatim reverseGeocode failed", e);
            return reverseGeocodePlatform(context, lat, lon);
        }
        try {
            String url = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=" + lat + "&lon=" + lon + "&zoom=16&accept-language=ja,vi,en";
            Request req = new Request.Builder().url(url).header("User-Agent", "DoomWeather/1.0").build();
            Response response = CLIENT.newCall(req).execute();
            try {
                if (response.getIsSuccessful() && response.body() != null) {
                    JSONObject obj = new JSONObject(response.body().string());
                    JSONObject addr = obj.optJSONObject("address");
                    if (addr != null) {
                        String[] keys = {"quarter", "neighbourhood", "suburb", "city_district", "town", "village", "city", "municipality", "county", "state"};
                        for (String k : keys) {
                            String v = cleanAreaName(addr.optString(k, ""));
                            if (!v.isEmpty()) {
                                if (response != null) {
                                    response.close();
                                }
                                return v;
                            }
                        }
                    }
                    String top = cleanAreaName(obj.optString("name", ""));
                    if (!top.isEmpty()) {
                        if (response != null) {
                            response.close();
                        }
                        return top;
                    }
                }
                if (response != null) {
                    response.close();
                }
            } finally {
            }
        } catch (Exception e3) {
            e = e3;
            Log.w("DoomWeather", "Nominatim reverseGeocode failed", e);
            return reverseGeocodePlatform(context, lat, lon);
        }
        return reverseGeocodePlatform(context, lat, lon);
    }

    private static String cleanAreaName(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return "";
        }
        String noJpChome = s.replaceFirst("[0-9０-９一二三四五六七八九十]+丁目$", "").trim();
        if (noJpChome.isEmpty() || noJpChome.equals(s)) {
            String noChome = s.replaceFirst("(?iu)\\s+\\d+(?:-?(?:ch[oō]me|j[oō]))?$", "").trim();
            if (!noChome.isEmpty() && !noChome.matches("(?iu).*(?:District|Ward|Section|Line|Zone|Quận|Phường|Xã)$")) {
                return noChome;
            }
            return s;
        }
        return noJpChome;
    }

    private static String reverseGeocodePlatform(Context context, double lat, double lon) {
        Exception e;
        if (context != null) {
            try {
            } catch (Exception e2) {
                e = e2;
            }
            if (Geocoder.isPresent()) {
                try {
                    List<Address> list = new Geocoder(context, Locale.JAPAN).getFromLocation(lat, lon, 1);
                    if (list != null && !list.isEmpty()) {
                        Address a = list.get(0);
                        String[] parts = {a.getSubLocality(), a.getLocality(), a.getSubAdminArea(), a.getAdminArea()};
                        for (String s : parts) {
                            String c = cleanAreaName(s);
                            if (!c.isEmpty()) {
                                return c;
                            }
                        }
                    }
                } catch (Exception e3) {
                    e = e3;
                    Log.w("DoomWeather", "Platform Geocoder failed", e);
                    return null;
                }
                return null;
            }
        }
        return null;
    }
}
