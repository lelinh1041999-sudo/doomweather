package vn.linh.doomweather;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Insets;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowInsets;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import okhttp3.internal.http.HttpStatusCodesKt;
import org.json.JSONObject;
import vn.linh.doomweather.MainActivity;

/* loaded from: classes3.dex */
public class MainActivity extends Activity {
    private LocationListener listener;
    private LocationManager locationManager;
    private LinearLayout root;
    private WebView web;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable locationTimeout = new Runnable() { // from class: vn.linh.doomweather.MainActivity$$ExternalSyntheticLambda0
        @Override // java.lang.Runnable
        public final void run() {
            MainActivity.this.lambda$new$0();
        }
    };
    private boolean askedFine = false;

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0() {
        stopLocation();
        toast("Chưa lấy được vị trí. M có thể chọn thành phố trong ứng dụng.");
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    public static int themeBgColor(String theme) {
        char c;
        String normalizeTheme = Store.normalizeTheme(theme);
        switch (normalizeTheme.hashCode()) {
            case -1461462798:
                if (normalizeTheme.equals("mcu_ironman")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1419997771:
                if (normalizeTheme.equals("mcu_spiderman")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -1116717860:
                if (normalizeTheme.equals("mcu_strange")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 107935:
                if (normalizeTheme.equals("mcu")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 3089321:
                if (normalizeTheme.equals("doom")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 1474694658:
                if (normalizeTheme.equals("wallpaper")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 2097221111:
                if (normalizeTheme.equals("mcu_thor")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return Color.rgb(91, 71, 194);
            case 1:
                return Color.rgb(16, 32, 70);
            case 2:
                return Color.rgb(92, 22, 24);
            case 3:
                return Color.rgb(28, 46, HttpStatusCodesKt.HTTP_PROCESSING);
            case 4:
                return Color.rgb(18, 52, 92);
            case 5:
                return Color.rgb(52, 26, 78);
            case 6:
                return Color.rgb(15, 59, 40);
            default:
                return Color.rgb(27, 100, 186);
        }
    }

    @Override // android.app.Activity
    public void onCreate(Bundle state) {
        super.onCreate(state);
        int bg = themeBgColor(Store.getTheme(this));
        this.root = new LinearLayout(this);
        this.root.setOrientation(1);
        this.root.setBackgroundColor(bg);
        this.root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() { // from class: vn.linh.doomweather.MainActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnApplyWindowInsetsListener
            public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                return MainActivity.lambda$onCreate$1(view, windowInsets);
            }
        });
        LinearLayout tools = new LinearLayout(this);
        tools.setPadding(8, 4, 8, 4);
        button(tools, "＋ Widget", new Runnable() { // from class: vn.linh.doomweather.MainActivity$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.pinWidget();
            }
        });
        button(tools, "Thành phố", new Runnable() { // from class: vn.linh.doomweather.MainActivity$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$onCreate$2();
            }
        });
        button(tools, "Vị trí", new Runnable() { // from class: vn.linh.doomweather.MainActivity$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.requestLocation();
            }
        });
        button(tools, "ⓘ", new Runnable() { // from class: vn.linh.doomweather.MainActivity$$ExternalSyntheticLambda5
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.help();
            }
        });
        this.root.addView(tools, new LinearLayout.LayoutParams(-1, -2));
        tools.setVisibility(8);
        this.web = new WebView(this);
        this.root.addView(this.web, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        setContentView(this.root);
        this.web.setBackgroundColor(bg);
        this.web.getSettings().setJavaScriptEnabled(true);
        this.web.getSettings().setDomStorageEnabled(true);
        this.web.getSettings().setAllowFileAccess(false);
        this.web.getSettings().setAllowContentAccess(false);
        this.web.getSettings().setMixedContentMode(1);
        this.web.addJavascriptInterface(new Bridge(), "Native");
        this.web.setWebViewClient(new WebViewClient() { // from class: vn.linh.doomweather.MainActivity.1
            @Override // android.webkit.WebViewClient
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return true;
            }

            @Override // android.webkit.WebViewClient
            public void onPageFinished(WebView view, String url) {
                view.evaluateJavascript("openWeatherApp()", null);
            }
        });
        try {
            InputStream input = getAssets().open("weather.html");
            try {
                ByteArrayOutputStream body = new ByteArrayOutputStream();
                byte[] b = new byte[8192];
                while (true) {
                    int n = input.read(b);
                    if (n == -1) {
                        break;
                    } else {
                        body.write(b, 0, n);
                    }
                }
                this.web.loadDataWithBaseURL("https://appassets.androidplatform.net/", body.toString("UTF-8"), "text/html", "UTF-8", null);
                if (input != null) {
                    input.close();
                }
            } finally {
            }
        } catch (Exception e) {
            toast("Không mở được giao diện thời tiết");
        }
        WeatherWidget.requestUpdate(this);
        if (Build.VERSION.SDK_INT >= 33 && Store.isNotifEnabled(this) && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != 0) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 11);
        }
        JSONObject savedLoc = Store.location(this);
        if ("Vị trí của tôi".equals(savedLoc.optString("name"))) {
            final double lat = savedLoc.optDouble("lat");
            final double lon = savedLoc.optDouble("lon");
            new Thread(new Runnable() { // from class: vn.linh.doomweather.MainActivity$$ExternalSyntheticLambda6
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.lambda$onCreate$4(lat, lon);
                }
            }, "init-geocode").start();
        }
        if (!Store.prefs(this).getBoolean("intro", false)) {
            Store.prefs(this).edit().putBoolean("intro", true).apply();
            help();
        }
    }

    static /* synthetic */ WindowInsets lambda$onCreate$1(View v, WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= 30) {
            Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
        } else {
            v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
        }
        return insets;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$2() {
        this.web.evaluateJavascript("toggleCityDrawer(true)", null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$4(final double lat, final double lon) {
        final String resolved = WeatherClient.reverseGeocode(getApplicationContext(), lat, lon);
        if (resolved != null && !resolved.isEmpty()) {
            this.handler.post(new Runnable() { // from class: vn.linh.doomweather.MainActivity$$ExternalSyntheticLambda10
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.lambda$onCreate$3(lat, lon, resolved);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$3(double lat, double lon, String resolved) {
        JSONObject cur = Store.location(this);
        if ("Vị trí của tôi".equals(cur.optString("name")) && Math.abs(cur.optDouble("lat") - lat) < 1.0E-4d && Math.abs(cur.optDouble("lon") - lon) < 1.0E-4d) {
            applyLocation(resolved, lat, lon, true);
        }
    }

    private void button(LinearLayout row, String label, final Runnable action) {
        int dp = (int) getResources().getDisplayMetrics().density;
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(12.0f);
        b.setTextColor(-6425160);
        b.setPadding(dp * 4, 0, dp * 4, 0);
        b.setMinWidth(0);
        b.setSingleLine(true);
        b.setBackgroundTintList(ColorStateList.valueOf(-15719397));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp * 48, label.equals("ⓘ") ? 0.45f : 1.0f);
        lp.setMargins(dp * 2, 0, dp * 2, 0);
        row.addView(b, lp);
        b.setOnClickListener(new View.OnClickListener() { // from class: vn.linh.doomweather.MainActivity$$ExternalSyntheticLambda9
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                action.run();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void help() {
        new AlertDialog.Builder(this).setTitle("Doom Weather").setMessage("1. Bấm Thành phố để tìm nơi m muốn, hoặc bấm Vị trí.\n2. Bấm ＋ Widget để thêm ra màn hình chính.\n3. Xóa ô thời tiết Samsung khỏi màn hình chính nếu muốn thay bằng ô này.\n\nWidget cập nhật khoảng 30 phút/lần, có thể chậm khi máy tiết kiệm pin. Bấm ↻ để tải ngay. Mọi widget dùng chung thành phố đã chọn; vị trí không tự theo dõi khi app đóng.\n\nBiểu đồ dùng dữ liệu dự báo mỗi 15 phút, không phải radar mưa từng phút. Hiệu ứng động chỉ có trong ứng dụng.\n\nDữ liệu: Open-Meteo, CC BY 4.0. Ứng dụng cá nhân, không thuộc Samsung.").setPositiveButton("Đã hiểu", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void pinWidget() {
        AppWidgetManager m = AppWidgetManager.getInstance(this);
        if (m.isRequestPinAppWidgetSupported()) {
            m.requestPinAppWidget(new ComponentName(this, (Class<?>) WeatherWidget.class), null, null);
        } else {
            toast("Nhấn giữ màn hình chính → Widget → Doom Weather.");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void resolveAndApplyLocation(final double lat, final double lon) {
        new Thread(new Runnable() { // from class: vn.linh.doomweather.MainActivity$$ExternalSyntheticLambda8
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$resolveAndApplyLocation$7(lat, lon);
            }
        }, "gps-geocode").start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$resolveAndApplyLocation$7(final double lat, final double lon) {
        String resolved = WeatherClient.reverseGeocode(getApplicationContext(), lat, lon);
        final String name = (resolved == null || resolved.isEmpty()) ? "Vị trí của tôi" : resolved;
        this.handler.post(new Runnable() { // from class: vn.linh.doomweather.MainActivity$$ExternalSyntheticLambda13
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$resolveAndApplyLocation$6(name, lat, lon);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$resolveAndApplyLocation$6(String name, double lat, double lon) {
        applyLocation(name, lat, lon, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void requestLocation() {
        Location last;
        boolean hasFine = checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") == 0;
        boolean hasCoarse = checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == 0;
        if (!hasFine && !hasCoarse) {
            requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"}, 10);
            return;
        }
        if (!hasFine && !this.askedFine) {
            this.askedFine = true;
            requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"}, 10);
            return;
        }
        stopLocation();
        this.locationManager = (LocationManager) getSystemService("location");
        List<String> providers = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 31) {
            providers.add("fused");
        }
        providers.add("gps");
        providers.add("network");
        try {
            long now = System.currentTimeMillis();
            for (String p : providers) {
                if (this.locationManager.isProviderEnabled(p) && (last = this.locationManager.getLastKnownLocation(p)) != null && now - last.getTime() <= 120000 && (!last.hasAccuracy() || last.getAccuracy() <= 150.0f)) {
                    toast("Đang cập nhật vị trí…");
                    resolveAndApplyLocation(last.getLatitude(), last.getLongitude());
                    return;
                }
            }
        } catch (SecurityException e) {
        }
        this.listener = new LocationListener() { // from class: vn.linh.doomweather.MainActivity.2
            @Override // android.location.LocationListener
            public void onLocationChanged(Location loc) {
                MainActivity.this.stopLocation();
                MainActivity.this.resolveAndApplyLocation(loc.getLatitude(), loc.getLongitude());
            }

            @Override // android.location.LocationListener
            public void onProviderDisabled(String p2) {
            }

            @Override // android.location.LocationListener
            public void onProviderEnabled(String p2) {
            }

            @Override // android.location.LocationListener
            public void onStatusChanged(String p2, int s, Bundle b) {
            }
        };
        boolean requested = false;
        try {
            for (String provider : providers) {
                if (this.locationManager.isProviderEnabled(provider)) {
                    this.locationManager.requestLocationUpdates(provider, 0L, 0.0f, this.listener, Looper.getMainLooper());
                    requested = true;
                }
            }
            if (requested) {
                toast("Đang lấy vị trí chính xác…");
                this.handler.postDelayed(this.locationTimeout, 15000L);
            } else {
                stopLocation();
                toast("Bật Vị trí trên điện thoại hoặc chọn thành phố thủ công.");
            }
        } catch (SecurityException e2) {
            stopLocation();
            toast("Chưa được cấp quyền vị trí.");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopLocation() {
        this.handler.removeCallbacks(this.locationTimeout);
        if (this.locationManager != null && this.listener != null) {
            this.locationManager.removeUpdates(this.listener);
        }
        this.listener = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void applyLocation(String name, double lat, double lon) {
        applyLocation(name, lat, lon, false);
    }

    private void applyLocation(String name, double lat, double lon, boolean isGps) {
        try {
            Store.setLocation(this, name, lat, lon, isGps);
            WeatherWidget.render(this);
            WeatherWidget.requestUpdate(this);
            JSONObject loc = Store.location(this);
            this.web.evaluateJavascript("currentLoc=" + loc + ";localStorage.setItem('doomsday_weather_loc',JSON.stringify(currentLoc));fetchLiveWeather();", null);
        } catch (Exception e) {
            toast("Vị trí không hợp lệ");
        }
    }

    @Override // android.app.Activity
    public void onRequestPermissionsResult(int request, String[] permissions, int[] grants) {
        String str;
        super.onRequestPermissionsResult(request, permissions, grants);
        boolean granted = false;
        if (grants != null) {
            for (int g : grants) {
                if (g == 0) {
                    granted = true;
                }
            }
        }
        if (request == 10) {
            if (granted) {
                requestLocation();
                return;
            }
            str = "M vẫn có thể tìm thành phố mà không cần cấp quyền.";
        } else {
            if (request != 11) {
                return;
            }
            if (this.web != null) {
                this.web.evaluateJavascript("syncNotifBtn()", null);
            }
            if (granted) {
                return;
            } else {
                str = "M có thể bật quyền thông báo trong Cài đặt bất cứ lúc nào.";
            }
        }
        toast(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void toast(String s) {
        Toast.makeText(this, s, 1).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void menu() {
        new AlertDialog.Builder(this).setTitle("Thời tiết").setItems(new String[]{"Đổi thành phố", "Vị trí của tôi", "Cập nhật", "Thêm widget", "Thông tin"}, new DialogInterface.OnClickListener() { // from class: vn.linh.doomweather.MainActivity$$ExternalSyntheticLambda7
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.lambda$menu$8(dialogInterface, i);
            }
        }).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$menu$8(DialogInterface d, int which) {
        if (which == 0) {
            this.web.evaluateJavascript("toggleCityDrawer(true)", null);
            return;
        }
        if (which != 1) {
            if (which == 2) {
                WeatherWidget.requestUpdate(this);
                this.web.evaluateJavascript("fetchLiveWeather()", null);
                return;
            } else if (which == 3) {
                pinWidget();
                return;
            } else {
                help();
                return;
            }
        }
        requestLocation();
    }

    @Override // android.app.Activity
    protected void onPause() {
        stopLocation();
        if (this.web != null) {
            this.web.onPause();
        }
        super.onPause();
    }

    @Override // android.app.Activity
    protected void onResume() {
        double useLat;
        double useLon;
        super.onResume();
        if (this.web != null) {
            this.web.onResume();
        }
        JSONObject saved = Store.location(this);
        if (saved.optBoolean("gps", false) || "Vị trí của tôi".equals(saved.optString("name"))) {
            double curLat = saved.optDouble("lat");
            double curLon = saved.optDouble("lon");
            String curName = saved.optString("name");
            Location fresh = WeatherClient.bestKnownLocation(this);
            boolean moved = false;
            if (fresh != null && WeatherMath.validLocation(fresh.getLatitude(), fresh.getLongitude())) {
                float[] dist = new float[1];
                Location.distanceBetween(curLat, curLon, fresh.getLatitude(), fresh.getLongitude(), dist);
                if (dist[0] > 250.0f) {
                    double useLat2 = fresh.getLatitude();
                    double useLon2 = fresh.getLongitude();
                    moved = true;
                    useLat = useLat2;
                    useLon = useLon2;
                    if (!moved || WeatherClient.needsJapaneseUpgrade(curName, useLat, useLon)) {
                        final double fLat = useLat;
                        final double fLon = useLon;
                        final boolean fMoved = moved;
                        new Thread(new Runnable() { // from class: vn.linh.doomweather.MainActivity$$ExternalSyntheticLambda11
                            @Override // java.lang.Runnable
                            public final void run() {
                                MainActivity.this.lambda$onResume$10(fLat, fLon, fMoved);
                            }
                        }, "resume-gps").start();
                    }
                    return;
                }
            }
            useLat = curLat;
            useLon = curLon;
            if (!moved) {
            }
            final double fLat2 = useLat;
            final double fLon2 = useLon;
            final boolean fMoved2 = moved;
            new Thread(new Runnable() { // from class: vn.linh.doomweather.MainActivity$$ExternalSyntheticLambda11
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.lambda$onResume$10(fLat2, fLon2, fMoved2);
                }
            }, "resume-gps").start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onResume$10(final double fLat, final double fLon, final boolean fMoved) {
        final String resolved = WeatherClient.reverseGeocode(getApplicationContext(), fLat, fLon);
        if (resolved != null && !resolved.isEmpty()) {
            this.handler.post(new Runnable() { // from class: vn.linh.doomweather.MainActivity$$ExternalSyntheticLambda12
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.lambda$onResume$9(fMoved, fLat, fLon, resolved);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onResume$9(boolean fMoved, double fLat, double fLon, String resolved) {
        JSONObject latest = Store.location(this);
        if (latest.optBoolean("gps", false) || "Vị trí của tôi".equals(latest.optString("name"))) {
            if (fMoved || WeatherClient.needsJapaneseUpgrade(latest.optString("name"), fLat, fLon)) {
                applyLocation(resolved, fLat, fLon, true);
            }
        }
    }

    final class Bridge {
        Bridge() {
        }

        @JavascriptInterface
        public String getLocationJson() {
            return Store.location(MainActivity.this).toString();
        }

        @JavascriptInterface
        public String getTheme() {
            return Store.getTheme(MainActivity.this);
        }

        @JavascriptInterface
        public void setTheme(final String theme) {
            MainActivity.this.runOnUiThread(new Runnable() { // from class: vn.linh.doomweather.MainActivity$Bridge$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.Bridge.this.lambda$setTheme$0(theme);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$setTheme$0(String theme) {
            Store.setTheme(MainActivity.this, theme);
            int bg = MainActivity.themeBgColor(Store.getTheme(MainActivity.this));
            MainActivity.this.root.setBackgroundColor(bg);
            MainActivity.this.web.setBackgroundColor(bg);
            WeatherWidget.render(MainActivity.this);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$pinWidget$1() {
            MainActivity.this.pinWidget();
        }

        @JavascriptInterface
        public void pinWidget() {
            MainActivity.this.runOnUiThread(new Runnable() { // from class: vn.linh.doomweather.MainActivity$Bridge$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.Bridge.this.lambda$pinWidget$1();
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$requestLocation$2() {
            MainActivity.this.requestLocation();
        }

        @JavascriptInterface
        public void requestLocation() {
            MainActivity.this.runOnUiThread(new Runnable() { // from class: vn.linh.doomweather.MainActivity$Bridge$$ExternalSyntheticLambda3
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.Bridge.this.lambda$requestLocation$2();
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$showMenu$3() {
            MainActivity.this.menu();
        }

        @JavascriptInterface
        public void showMenu() {
            MainActivity.this.runOnUiThread(new Runnable() { // from class: vn.linh.doomweather.MainActivity$Bridge$$ExternalSyntheticLambda2
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.Bridge.this.lambda$showMenu$3();
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$selectLocation$4(String name, double lat, double lon) {
            MainActivity.this.applyLocation(name, lat, lon);
        }

        @JavascriptInterface
        public void selectLocation(final String name, final double lat, final double lon) {
            MainActivity.this.runOnUiThread(new Runnable() { // from class: vn.linh.doomweather.MainActivity$Bridge$$ExternalSyntheticLambda4
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.Bridge.this.lambda$selectLocation$4(name, lat, lon);
                }
            });
        }

        @JavascriptInterface
        public boolean isNotifEnabled() {
            return Store.isNotifEnabled(MainActivity.this);
        }

        @JavascriptInterface
        public void setNotifEnabled(final boolean enabled) {
            MainActivity.this.runOnUiThread(new Runnable() { // from class: vn.linh.doomweather.MainActivity$Bridge$$ExternalSyntheticLambda6
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.Bridge.this.lambda$setNotifEnabled$5(enabled);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$setNotifEnabled$5(boolean enabled) {
            Store.setNotifEnabled(MainActivity.this, enabled);
            if (enabled && Build.VERSION.SDK_INT >= 33 && MainActivity.this.checkSelfPermission("android.permission.POST_NOTIFICATIONS") != 0) {
                MainActivity.this.requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 11);
            }
            MainActivity.this.web.evaluateJavascript("syncNotifBtn()", null);
        }

        @JavascriptInterface
        public void testNotification() {
            MainActivity.this.runOnUiThread(new Runnable() { // from class: vn.linh.doomweather.MainActivity$Bridge$$ExternalSyntheticLambda5
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.Bridge.this.lambda$testNotification$6();
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$testNotification$6() {
            if (Build.VERSION.SDK_INT >= 33 && MainActivity.this.checkSelfPermission("android.permission.POST_NOTIFICATIONS") != 0) {
                MainActivity.this.requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 11);
            } else {
                WeatherNotifier.sendSummaryNow(MainActivity.this);
                MainActivity.this.toast("Đã gửi thông báo thời tiết thử");
            }
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        if (this.web != null) {
            this.web.removeJavascriptInterface("Native");
            this.web.destroy();
        }
        super.onDestroy();
    }
}
