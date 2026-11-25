package vn.linh.doomweather;

/* loaded from: classes3.dex */
public final class WeatherMath {
    private WeatherMath() {
    }

    public static String condition(int code) {
        if (code == 0) {
            return "Trời quang";
        }
        if (code == 1 || code == 2) {
            return "Có mây";
        }
        if (code == 3) {
            return "Nhiều mây";
        }
        if (code == 45 || code == 48) {
            return "Sương mù";
        }
        if (code >= 51 && code <= 57) {
            return "Mưa phùn";
        }
        if (code >= 61 && code <= 67) {
            return code == 65 ? "Mưa lớn" : "Có mưa";
        }
        if ((code >= 71 && code <= 77) || code == 85 || code == 86) {
            return "Có tuyết";
        }
        return (code < 80 || code > 82) ? (code < 95 || code > 99) ? "Thời tiết" : "Mưa dông" : "Mưa rào";
    }

    public static int nextIndex(long[] times, long now) {
        for (int i = 0; i < times.length; i++) {
            if (times[i] >= now) {
                return i;
            }
        }
        int i2 = times.length;
        return i2;
    }

    public static boolean validLocation(double lat, double lon) {
        return Double.isFinite(lat) && Double.isFinite(lon) && Math.abs(lat) <= 90.0d && Math.abs(lon) <= 180.0d;
    }

    public static double[] nextRain(long[] times, double[] rain, long now) {
        int start = nextIndex(times, now);
        if (times.length != rain.length || start + 4 > times.length) {
            return new double[0];
        }
        double[] result = new double[4];
        for (int i = 0; i < 4; i++) {
            if (times[start + i] > 3600 + now || !Double.isFinite(rain[start + i]) || rain[start + i] < 0.0d) {
                return new double[0];
            }
            result[i] = rain[start + i];
        }
        return result;
    }
}
