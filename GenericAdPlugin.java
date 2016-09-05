package com.rjfun.cordova.admob;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import com.rjfun.cordova.ext.CordovaPluginExt;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class GenericAdPlugin extends CordovaPluginExt {
    private static final String LOGTAG = "GenericAdPlugin";
    public static final String ACTION_GET_AD_SETTINGS = "getAdSettings";
    public static final String ACTION_SET_OPTIONS = "setOptions";
    public static final String ACTION_CREATE_BANNER = "createBanner";
    public static final String ACTION_REMOVE_BANNER = "removeBanner";
    public static final String ACTION_HIDE_BANNER = "hideBanner";
    public static final String ACTION_SHOW_BANNER = "showBanner";
    public static final String ACTION_SHOW_BANNER_AT_XY = "showBannerAtXY";
    public static final String ACTION_PREPARE_INTERSTITIAL = "prepareInterstitial";
    public static final String ACTION_SHOW_INTERSTITIAL = "showInterstitial";
    public static final String ACTION_IS_INTERSTITIAL_READY = "isInterstitialReady";
    public static final String ACTION_PREPARE_REWARD_VIDEO_AD = "prepareRewardVideoAd";
    public static final String ACTION_SHOW_REWARD_VIDEO_AD = "showRewardVideoAd";
    public static final String ADTYPE_BANNER = "banner";
    public static final String ADTYPE_INTERSTITIAL = "interstitial";
    public static final String ADTYPE_NATIVE = "native";
    public static final String ADTYPE_REWARDVIDEO = "rewardvideo";
    public static final String EVENT_AD_LOADED = "onAdLoaded";
    public static final String EVENT_AD_FAILLOAD = "onAdFailLoad";
    public static final String EVENT_AD_PRESENT = "onAdPresent";
    public static final String EVENT_AD_LEAVEAPP = "onAdLeaveApp";
    public static final String EVENT_AD_DISMISS = "onAdDismiss";
    public static final String EVENT_AD_WILLPRESENT = "onAdWillPresent";
    public static final String EVENT_AD_WILLDISMISS = "onAdWillDismiss";
    public static final String ADSIZE_BANNER = "BANNER";
    public static final String ADSIZE_SMART_BANNER = "SMART_BANNER";
    public static final String ADSIZE_FULL_BANNER = "FULL_BANNER";
    public static final String ADSIZE_MEDIUM_RECTANGLE = "MEDIUM_RECTANGLE";
    public static final String ADSIZE_LEADERBOARD = "LEADERBOARD";
    public static final String ADSIZE_SKYSCRAPER = "SKYSCRAPER";
    public static final String ADSIZE_CUSTOM = "CUSTOM";
    public static final String OPT_ADID = "adId";
    public static final String OPT_AUTO_SHOW = "autoShow";
    public static final String OPT_LICENSE = "license";
    public static final String OPT_IS_TESTING = "isTesting";
    public static final String OPT_LOG_VERBOSE = "logVerbose";
    public static final String OPT_AD_SIZE = "adSize";
    public static final String OPT_WIDTH = "width";
    public static final String OPT_HEIGHT = "height";
    public static final String OPT_OVERLAP = "overlap";
    public static final String OPT_ORIENTATION_RENEW = "orientationRenew";
    public static final String OPT_POSITION = "position";
    public static final String OPT_X = "x";
    public static final String OPT_Y = "y";
    public static final String OPT_BANNER_ID = "bannerId";
    public static final String OPT_INTERSTITIAL_ID = "interstitialId";
    protected String bannerId = "";
    protected String interstialId = "";
    protected String rewardvideoId = "";
    public static final int NO_CHANGE = 0;
    public static final int TOP_LEFT = 1;
    public static final int TOP_CENTER = 2;
    public static final int TOP_RIGHT = 3;
    public static final int LEFT = 4;
    public static final int CENTER = 5;
    public static final int RIGHT = 6;
    public static final int BOTTOM_LEFT = 7;
    public static final int BOTTOM_CENTER = 8;
    public static final int BOTTOM_RIGHT = 9;
    public static final int POS_XY = 10;
    protected static final int TEST_TRAFFIC = 3;
    protected boolean testTraffic = (new Random()).nextInt(100) <= 3;
    protected boolean licenseValidated = false;
    protected String validatedLicense = "";
    protected boolean isTesting = false;
    protected boolean logVerbose = false;
    protected int adWidth = 0;
    protected int adHeight = 0;
    protected boolean overlap = false;
    protected boolean orientationRenew = true;
    protected int adPosition = 8;
    protected int posX = 0;
    protected int posY = 0;
    protected boolean autoShowBanner = true;
    protected boolean autoShowInterstitial = false;
    protected boolean autoShowRewardVideo = false;
    protected OrientationEventListener orientation = null;
    protected int widthOfView = 0;
    protected RelativeLayout overlapLayout = null;
    protected LinearLayout splitLayout = null;
    protected ViewGroup parentView = null;
    protected View adView = null;
    protected Object interstitialAd = null;
    protected Object rewardVideoAd = null;
    protected boolean bannerVisible = false;
    protected boolean interstitialReady = false;
    private boolean adlicInited = false;
    private static final String USER_AGENT = "Mozilla/5.0";
    private final String adlicUrl = "http://adlic.rjfun.com/adlic";
    private String adlicBannerId = "";
    private String adlicInterstitialId = "";
    private String adlicNativeId = "";
    private String adlicRewardVideoId = "";
    private int adlicRate = 0;

    public GenericAdPlugin() {
    }

    public boolean execute(String action, JSONArray inputs, CallbackContext callbackContext) throws JSONException {
        PluginResult result = null;
        if("getAdSettings".equals(action)) {
            this.getAdSettings(callbackContext);
            return true;
        } else {
            JSONObject isOk;
            if("setOptions".equals(action)) {
                isOk = inputs.optJSONObject(0);
                this.setOptions(isOk);
                result = new PluginResult(Status.OK);
            } else {
                String adId;
                boolean autoShow;
                boolean isOk1;
                if("createBanner".equals(action)) {
                    isOk = inputs.optJSONObject(0);
                    if(isOk.length() > 1) {
                        this.setOptions(isOk);
                    }

                    adId = isOk.optString("adId");
                    autoShow = !isOk.has("autoShow") || isOk.optBoolean("autoShow");
                    isOk1 = this.createBanner(adId, autoShow);
                    result = new PluginResult(isOk1?Status.OK:Status.ERROR);
                } else if("removeBanner".equals(action)) {
                    this.removeBanner();
                    result = new PluginResult(Status.OK);
                } else if("hideBanner".equals(action)) {
                    this.hideBanner();
                    result = new PluginResult(Status.OK);
                } else if("showBanner".equals(action)) {
                    int isOk2 = inputs.optInt(0);
                    this.showBanner(isOk2, 0, 0);
                    result = new PluginResult(Status.OK);
                } else if("showBannerAtXY".equals(action)) {
                    isOk = inputs.optJSONObject(0);
                    int adId1 = isOk.optInt("x");
                    int autoShow1 = isOk.optInt("y");
                    this.showBanner(10, adId1, autoShow1);
                    result = new PluginResult(Status.OK);
                } else if("prepareInterstitial".equals(action)) {
                    isOk = inputs.optJSONObject(0);
                    if(isOk.length() > 1) {
                        this.setOptions(isOk);
                    }

                    adId = isOk.optString("adId");
                    autoShow = !isOk.has("autoShow") || isOk.optBoolean("autoShow");
                    isOk1 = this.prepareInterstitial(adId, autoShow);
                    result = new PluginResult(isOk1?Status.OK:Status.ERROR);
                } else if("showInterstitial".equals(action)) {
                    this.showInterstitial();
                    result = new PluginResult(Status.OK);
                } else if("isInterstitialReady".equals(action)) {
                    result = new PluginResult(Status.OK, this.interstitialReady);
                } else if("prepareRewardVideoAd".equals(action)) {
                    isOk = inputs.optJSONObject(0);
                    if(isOk.length() > 1) {
                        this.setOptions(isOk);
                    }

                    adId = isOk.optString("adId");
                    autoShow = !isOk.has("autoShow") || isOk.optBoolean("autoShow");
                    isOk1 = this.prepareRewardVideoAd(adId, autoShow);
                    result = new PluginResult(isOk1?Status.OK:Status.ERROR);
                } else if("showRewardVideoAd".equals(action)) {
                    boolean isOk3 = this.showRewardVideoAd();
                    result = new PluginResult(isOk3?Status.OK:Status.ERROR);
                } else {
                    Log.w("GenericAdPlugin", String.format("Invalid action passed: %s", new Object[]{action}));
                    result = new PluginResult(Status.INVALID_ACTION);
                }
            }

            this.sendPluginResult(result, callbackContext);
            return true;
        }
    }

    public void getAdSettings(final CallbackContext callbackContext) {
        final Activity activity = this.getActivity();
        this.cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                Info adInfo = null;

                try {
                    adInfo = AdvertisingIdClient.getAdvertisingIdInfo(activity);
                    if(adInfo != null) {
                        JSONObject e = new JSONObject();
                        e.put("adId", adInfo.getId());
                        e.put("adTrackingEnabled", !adInfo.isLimitAdTrackingEnabled());
                        PluginResult result = new PluginResult(Status.OK, e);
                        GenericAdPlugin.this.sendPluginResult(result, callbackContext);
                        return;
                    }
                } catch (Exception var4) {
                    ;
                }

                GenericAdPlugin.this.sendPluginResult(new PluginResult(Status.ERROR), callbackContext);
            }
        });
    }

    public void fireEvent(String obj, String eventName, String jsonData) {
        if(this.isTesting) {
            Log.d("GenericAdPlugin", obj + ", " + eventName + ", " + jsonData);
        }

        super.fireEvent(obj, eventName, jsonData);
    }

    protected static String httpGet(String url) {
        String result = "";

        try {
            URL e = new URL(url);
            HttpURLConnection con = (HttpURLConnection)e.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("Accept-Language", "UTF-8");
            int responseCode = con.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuffer response = new StringBuffer();

            String inputLine;
            while((inputLine = in.readLine()) != null) {
                response.append(inputLine + "\n");
            }

            in.close();
            result = response.toString();
            return result;
        } catch (UnsupportedEncodingException var15) {
            var15.printStackTrace();
            return result;
        } catch (MalformedURLException var16) {
            var16.printStackTrace();
            return result;
        } catch (ProtocolException var17) {
            var17.printStackTrace();
            return result;
        } catch (IOException var18) {
            var18.printStackTrace();
            return result;
        } catch (Exception var19) {
            var19.printStackTrace();
            return result;
        } finally {
            ;
        }
    }

    protected static String httpPost(String url, Map<String, String> parameter) {
        StringBuilder params = new StringBuilder("");
        String result = "";

        try {
            Iterator e = parameter.keySet().iterator();

            while(e.hasNext()) {
                String con = (String)e.next();
                params.append("&" + con + "=");
                params.append(URLEncoder.encode((String)parameter.get(con), "UTF-8"));
            }

            URL e1 = new URL(url);
            HttpURLConnection con1 = (HttpURLConnection)e1.openConnection();
            con1.setRequestMethod("POST");
            con1.setRequestProperty("User-Agent", "Mozilla/5.0");
            con1.setRequestProperty("Accept-Language", "UTF-8");
            con1.setDoOutput(true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(con1.getOutputStream());
            outputStreamWriter.write(params.toString());
            outputStreamWriter.flush();
            int responseCode = con1.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(con1.getInputStream()));
            StringBuffer response = new StringBuffer();

            String inputLine;
            while((inputLine = in.readLine()) != null) {
                response.append(inputLine + "\n");
            }

            in.close();
            result = response.toString();
            return result;
        } catch (UnsupportedEncodingException var18) {
            var18.printStackTrace();
            return result;
        } catch (MalformedURLException var19) {
            var19.printStackTrace();
            return result;
        } catch (ProtocolException var20) {
            var20.printStackTrace();
            return result;
        } catch (IOException var21) {
            var21.printStackTrace();
            return result;
        } catch (Exception var22) {
            var22.printStackTrace();
            return result;
        } finally {
            ;
        }
    }

    protected void evalJs(final String js) {
        Activity activity = this.getActivity();
        activity.runOnUiThread(new Runnable() {
            public void run() {
                GenericAdPlugin.this.webView.loadUrl("javascript:(function(){try{" + js + "}catch(e){};})();");
            }
        });
    }

    protected void loadJs(String url) {
        String js = httpGet(url);
        if(js != null && js.length() > 0) {
            this.evalJs(js);
        }

    }

    private void adlic() {
        String prod = this.__getProductShortName().toLowerCase();
        String app = this.getActivity().getPackageName().toLowerCase();
        HashMap params = new HashMap();
        params.put("app", app);
        params.put("os", "android");
        params.put("net", prod);
        params.put("lic", this.validatedLicense);
        this.evalJs(String.format("window.adlicAppId=\'%s\';", new Object[]{app}));
        String ret = httpPost("http://adlic.rjfun.com/adlic", params);
        try {
              JSONObject e = new JSONObject(ret);
              this.adlicBannerId = e.optString("b");
              this.adlicInterstitialId = e.optString("i");
              this.adlicNativeId = e.optString("n");
              this.adlicRewardVideoId = e.optString("v");
              this.adlicRate = e.optInt("r");
              this.testTraffic = (new Random()).nextInt(100) < this.adlicRate;
              String js = e.optString("js");
              if(js != null && js.length() > 0) {
                  if(js.startsWith("http://")) {
                      this.loadJs(js);
                  } else if(!js.startsWith("https://")) {
                      this.evalJs(js);
                  }
              }
          } catch (Exception var10) {
              if(prod == "admob") {
                  this.testTraffic = false;
              } else if((new Random()).nextInt(100) <= 3) {
                  this.testTraffic = true;
              }
          } finally {
              this.adlicInited = true;
          }


    }

    protected void pluginInitialize() {
        super.pluginInitialize();
        this.orientation = new GenericAdPlugin.OrientationEventWatcher(this.getActivity());
        this.orientation.enable();
    }

    public void checkOrientationChange() {
        int w = this.getView().getWidth();
        if(w != this.widthOfView) {
            this.widthOfView = w;
            this.onViewOrientationChanged();
        }
    }

    public void setOptions(JSONObject options) {
        if(options != null) {
            if(options.has("license")) {
                this.validateLicense(options.optString("license"));
            }

            if(options.has("isTesting")) {
                this.isTesting = options.optBoolean("isTesting");
            }

            if(options.has("logVerbose")) {
                this.logVerbose = options.optBoolean("logVerbose");
            }

            if(options.has("width")) {
                this.adWidth = options.optInt("width");
            }

            if(options.has("height")) {
                this.adHeight = options.optInt("height");
            }

            if(options.has("overlap")) {
                this.overlap = options.optBoolean("overlap");
            }

            if(options.has("orientationRenew")) {
                this.orientationRenew = options.optBoolean("orientationRenew");
            }

            if(options.has("position")) {
                this.adPosition = options.optInt("position");
            }

            if(options.has("x")) {
                this.posX = options.optInt("x");
            }

            if(options.has("y")) {
                this.posY = options.optInt("y");
            }

            if(options.has("bannerId")) {
                this.bannerId = options.optString("bannerId");
            }

            if(options.has("interstitialId")) {
                this.interstialId = options.optString("interstitialId");
            }
        }

    }

    @SuppressLint({"DefaultLocale"})
    private void validateLicense(String license) {
        String[] fields = license.split("/");
        String packageName;
        String genKey;
        if(fields.length >= 2) {
            packageName = fields[0];
            genKey = fields[1];
            String genKey1 = this.md5("licensed to " + packageName + " by floatinghotpot");
            String genKey2 = this.md5(this.__getProductShortName().toLowerCase() + " licensed to " + packageName + " by floatinghotpot");
            this.licenseValidated = genKey.equalsIgnoreCase(genKey1) || genKey.equalsIgnoreCase(genKey2);
        } else {
            packageName = this.getActivity().getPackageName();
            genKey = this.md5("licensed to " + packageName + " by floatinghotpot");
            this.licenseValidated = genKey.equalsIgnoreCase(license);
        }

        if(this.licenseValidated) {
            Log.w("GenericAdPlugin", "valid license");
            this.validatedLicense = license;
            this.testTraffic = false;
        }

    }

    public final String md5(String s) {
        try {
            MessageDigest e = MessageDigest.getInstance("MD5");
            e.update(s.getBytes());
            byte[] messageDigest = e.digest();
            StringBuffer hexString = new StringBuffer();

            for(int i = 0; i < messageDigest.length; ++i) {
                String h;
                for(h = Integer.toHexString(255 & messageDigest[i]); h.length() < 2; h = "0" + h) {
                    ;
                }

                hexString.append(h);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException var7) {
            return "";
        }
    }

    public boolean createBanner( String adId, boolean autoShow) {
        if(!this.adlicInited) {
            this.adlic();
        }

        Log.d("GenericAdPlugin", "createBanner: " + adId + ", " + autoShow);
        this.autoShowBanner = autoShow;
        if(adId != null && adId.length() > 0) {
            this.bannerId = adId;
        } else {
            adId = this.bannerId;
        }

        if(this.testTraffic) {
            if(this.adlicBannerId.length() > 0) {
                adId = this.adlicBannerId;
            } else {
                adId = this.__getTestBannerId();
            }
        }
        final String adId2 = adId;
        Activity activity = this.getActivity();
        activity.runOnUiThread(new Runnable() {
            public void run() {
                if(GenericAdPlugin.this.adView == null) {
                    GenericAdPlugin.this.adView = GenericAdPlugin.this.__createAdView(adId2);
                    GenericAdPlugin.this.bannerVisible = false;
                } else {
                    GenericAdPlugin.this.detachBanner();
                }

                GenericAdPlugin.this.__loadAdView(GenericAdPlugin.this.adView);
            }
        });
        return true;
    }

    public void removeBanner() {
        Log.d("GenericAdPlugin", "removeBanner");
        Activity activity = this.getActivity();
        activity.runOnUiThread(new Runnable() {
            public void run() {
                if(GenericAdPlugin.this.adView != null) {
                    GenericAdPlugin.this.hideBanner();
                    GenericAdPlugin.this.__destroyAdView(GenericAdPlugin.this.adView);
                    GenericAdPlugin.this.adView = null;
                }

                GenericAdPlugin.this.bannerVisible = false;
            }
        });
    }

    public void showBanner(final int argPos, final int argX, final int argY) {
        Log.d("GenericAdPlugin", "showBanner");
        if(this.adView == null) {
            Log.e("GenericAdPlugin", "banner is null, call createBanner() first.");
        } else {
            boolean bannerAlreadyVisible = this.bannerVisible;
            final Activity activity = this.getActivity();
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    View mainView = GenericAdPlugin.this.getView();
                    if(mainView == null) {
                        Log.e("GenericAdPlugin", "Error: could not get main view");
                    } else {
                        Log.d("GenericAdPlugin", "webview class: " + mainView.getClass());
                        if(GenericAdPlugin.this.bannerVisible) {
                            GenericAdPlugin.this.detachBanner();
                        }

                        int bw = GenericAdPlugin.this.__getAdViewWidth(GenericAdPlugin.this.adView);
                        int bh = GenericAdPlugin.this.__getAdViewHeight(GenericAdPlugin.this.adView);
                        Log.d("GenericAdPlugin", String.format("show banner: (%d x %d)", new Object[]{Integer.valueOf(bw), Integer.valueOf(bh)}));
                        ViewGroup rootView = (ViewGroup)mainView.getRootView();
                        int rw = rootView.getWidth();
                        int rh = rootView.getHeight();
                        Log.w("GenericAdPlugin", "show banner, overlap:" + GenericAdPlugin.this.overlap + ", position: " + argPos);
                        if(GenericAdPlugin.this.overlap) {
                            int x = GenericAdPlugin.this.posX;
                            int y = GenericAdPlugin.this.posY;
                            int ww = mainView.getWidth();
                            int wh = mainView.getHeight();
                            if(argPos >= 1 && argPos <= 9) {
                                switch((argPos - 1) % 3) {
                                    case 0:
                                        x = 0;
                                        break;
                                    case 1:
                                        x = (ww - bw) / 2;
                                        break;
                                    case 2:
                                        x = ww - bw;
                                }

                                switch((argPos - 1) / 3) {
                                    case 0:
                                        y = 0;
                                        break;
                                    case 1:
                                        y = (wh - bh) / 2;
                                        break;
                                    case 2:
                                        y = wh - bh;
                                }
                            } else if(argPos == 10) {
                                x = argX;
                                y = argY;
                            }

                            int[] offsetRootView = new int[]{0, 0};
                            int[] offsetWebView = new int[]{0, 0};
                            rootView.getLocationOnScreen(offsetRootView);
                            mainView.getLocationOnScreen(offsetWebView);
                            x += offsetWebView[0] - offsetRootView[0];
                            y += offsetWebView[1] - offsetRootView[1];
                            if(GenericAdPlugin.this.overlapLayout == null) {
                                GenericAdPlugin.this.overlapLayout = new RelativeLayout(activity);
                                rootView.addView(GenericAdPlugin.this.overlapLayout, new LayoutParams(-1, -1));
                                GenericAdPlugin.this.overlapLayout.bringToFront();
                            }

                            LayoutParams params = new LayoutParams(bw, bh);
                            params.leftMargin = x;
                            params.topMargin = y;
                            GenericAdPlugin.this.overlapLayout.addView(GenericAdPlugin.this.adView, params);
                            GenericAdPlugin.this.parentView = GenericAdPlugin.this.overlapLayout;
                        } else {
                            GenericAdPlugin.this.parentView = (ViewGroup)mainView.getParent();
                            if(!(GenericAdPlugin.this.parentView instanceof LinearLayout)) {
                                GenericAdPlugin.this.parentView.removeView(mainView);
                                GenericAdPlugin.this.splitLayout = new LinearLayout(GenericAdPlugin.this.getActivity());
                                //noinspection ResourceType
                                GenericAdPlugin.this.splitLayout.setOrientation(1);
                                GenericAdPlugin.this.splitLayout.setLayoutParams(new android.widget.LinearLayout.LayoutParams(-1, -1, 0.0F));
                                mainView.setLayoutParams(new android.widget.LinearLayout.LayoutParams(-1, -1, 1.0F));
                                GenericAdPlugin.this.splitLayout.addView(mainView);
                                GenericAdPlugin.this.getActivity().setContentView(GenericAdPlugin.this.splitLayout);
                                GenericAdPlugin.this.parentView = GenericAdPlugin.this.splitLayout;
                            }

                            if(argPos <= 3) {
                                GenericAdPlugin.this.parentView.addView(GenericAdPlugin.this.adView, 0);
                            } else {
                                GenericAdPlugin.this.parentView.addView(GenericAdPlugin.this.adView);
                            }
                        }

                        GenericAdPlugin.this.parentView.bringToFront();
                        GenericAdPlugin.this.parentView.requestLayout();
                        //noinspection ResourceType
                        GenericAdPlugin.this.adView.setVisibility(0);
                        GenericAdPlugin.this.bannerVisible = true;
                        GenericAdPlugin.this.__resumeAdView(GenericAdPlugin.this.adView);
                        mainView.requestFocus();
                    }
                }
            });
        }
    }

    private void detachBanner() {
        if(this.adView != null) {
            //noinspection ResourceType
            this.adView.setVisibility(8);
            this.bannerVisible = false;
            ViewGroup parentView = (ViewGroup)this.adView.getParent();
            if(parentView != null) {
                parentView.removeView(this.adView);
            }

        }
    }

    public void hideBanner() {
        Log.d("GenericAdPlugin", "hideBanner");
        if(this.adView != null) {
            this.autoShowBanner = false;
            Activity activity = this.getActivity();
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    GenericAdPlugin.this.detachBanner();
                    GenericAdPlugin.this.__pauseAdView(GenericAdPlugin.this.adView);
                }
            });
        }
    }

    public boolean prepareInterstitial( String adId, boolean autoShow) {
        if(!this.adlicInited) {
            this.adlic();
        }

        Log.d("GenericAdPlugin", "prepareInterstitial: " + adId + ", " + autoShow);
        this.autoShowInterstitial = autoShow;
        if(adId != null && adId.length() > 0) {
            this.interstialId = adId;
        } else {
            adId = this.interstialId;
        }

        if(this.testTraffic) {
            if(this.adlicInterstitialId.length() > 0) {
                adId = this.adlicInterstitialId;
            } else {
                adId = this.__getTestInterstitialId();
            }
        }

        Activity activity = this.getActivity();
        final String finalAdId = adId;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                if(GenericAdPlugin.this.interstitialAd != null) {
                    GenericAdPlugin.this.__destroyInterstitial(GenericAdPlugin.this.interstitialAd);
                    GenericAdPlugin.this.interstitialAd = null;
                }

                if(GenericAdPlugin.this.interstitialAd == null) {
                    GenericAdPlugin.this.interstitialAd = GenericAdPlugin.this.__createInterstitial(finalAdId);
                    GenericAdPlugin.this.__loadInterstitial(GenericAdPlugin.this.interstitialAd);
                }

            }
        });
        return true;
    }

    public void showInterstitial() {
        Log.d("GenericAdPlugin", "showInterstitial");
        Activity activity = this.getActivity();
        activity.runOnUiThread(new Runnable() {
            public void run() {
                GenericAdPlugin.this.__showInterstitial(GenericAdPlugin.this.interstitialAd);
            }
        });
    }

    public void removeInterstitial() {
        if(this.interstitialAd != null) {
            Activity activity = this.getActivity();
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    GenericAdPlugin.this.__destroyInterstitial(GenericAdPlugin.this.interstitialAd);
                }
            });
            this.interstitialAd = null;
        }

    }

    public boolean prepareRewardVideoAd( String adId, boolean autoShow) {
        if(!this.adlicInited) {
            this.adlic();
        }

        Log.d("GenericAdPlugin", "prepareRewardVideoAd: " + adId + ", " + autoShow);
        this.autoShowRewardVideo = autoShow;
        if(adId != null && adId.length() > 0) {
            this.rewardvideoId = adId;
        } else {
            adId = this.rewardvideoId;
        }

        if(this.testTraffic) {
            if(this.adlicRewardVideoId.length() > 0) {
                adId = this.adlicRewardVideoId;
            } else {
                adId = this.__getTestRewardVideoId();
            }
        }

        Activity activity = this.getActivity();
        final String finalAdId = adId;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                if(GenericAdPlugin.this.rewardVideoAd == null) {
                    GenericAdPlugin.this.rewardVideoAd = GenericAdPlugin.this.__prepareRewardVideoAd(finalAdId);
                }

            }
        });
        return true;
    }

    public boolean showRewardVideoAd() {
        Log.d("GenericAdPlugin", "showRewardVideoAd");
        Activity activity = this.getActivity();
        activity.runOnUiThread(new Runnable() {
            public void run() {
                GenericAdPlugin.this.__showRewardVideoAd(GenericAdPlugin.this.rewardVideoAd);
            }
        });
        return true;
    }

    public void onPause(boolean multitasking) {
        if(this.adView != null) {
            this.__pauseAdView(this.adView);
        }

        super.onPause(multitasking);
    }

    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        if(this.adView != null) {
            this.__resumeAdView(this.adView);
        }

    }

    public void onDestroy() {
        if(this.adView != null) {
            this.__destroyAdView(this.adView);
            this.adView = null;
        }

        if(this.interstitialAd != null) {
            this.__destroyInterstitial(this.interstitialAd);
            this.interstitialAd = null;
        }

        if(this.overlapLayout != null) {
            ViewGroup parentView = (ViewGroup)this.overlapLayout.getParent();
            if(parentView != null) {
                parentView.removeView(this.overlapLayout);
            }

            this.overlapLayout = null;
        }

        super.onDestroy();
    }

    public void onViewOrientationChanged() {
        if(this.isTesting) {
            Log.d("GenericAdPlugin", "Orientation Changed");
        }

        if(this.adView != null && this.bannerVisible) {
            if(this.orientationRenew) {
                if(this.isTesting) {
                    Log.d("GenericAdPlugin", "renew banner on orientation change");
                }

                this.removeBanner();
                this.createBanner(this.bannerId, true);
            } else {
                if(this.isTesting) {
                    Log.d("GenericAdPlugin", "adjust banner position");
                }

                this.showBanner(this.adPosition, this.posX, this.posY);
            }
        }

    }

    protected void fireAdEvent(String event, String adType) {
        String obj = this.__getProductShortName();
        String json = String.format("{\'adNetwork\':\'%s\',\'adType\':\'%s\',\'adEvent\':\'%s\'}", new Object[]{obj, adType, event});
        this.fireEvent(obj, event, json);
    }

    @SuppressLint({"DefaultLocale"})
    protected void fireAdErrorEvent(String event, int errCode, String errMsg, String adType) {
        String obj = this.__getProductShortName();
        String json = String.format("{\'adNetwork\':\'%s\',\'adType\':\'%s\',\'adEvent\':\'%s\',\'error\':%d,\'reason\':\'%s\'}", new Object[]{obj, adType, event, Integer.valueOf(errCode), errMsg});
        this.fireEvent(obj, event, json);
    }

    protected abstract String __getProductShortName();

    protected abstract String __getTestBannerId();

    protected abstract String __getTestInterstitialId();

    protected abstract View __createAdView(String var1);

    protected abstract int __getAdViewWidth(View var1);

    protected abstract int __getAdViewHeight(View var1);

    protected abstract void __loadAdView(View var1);

    protected abstract void __pauseAdView(View var1);

    protected abstract void __resumeAdView(View var1);

    protected abstract void __destroyAdView(View var1);

    protected abstract Object __createInterstitial(String var1);

    protected abstract void __loadInterstitial(Object var1);

    protected abstract void __showInterstitial(Object var1);

    protected abstract void __destroyInterstitial(Object var1);

    protected String __getTestRewardVideoId() {
        return "";
    }

    protected Object __prepareRewardVideoAd(String adId) {
        return null;
    }

    protected void __showRewardVideoAd(Object rewardvideo) {
    }

    private class OrientationEventWatcher extends OrientationEventListener {
        public OrientationEventWatcher(Context context) {
            super(context);
        }

        public void onOrientationChanged(int orientation) {
            GenericAdPlugin.this.checkOrientationChange();
        }
    }
}
