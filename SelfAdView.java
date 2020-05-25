
package com.test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.appcompat.widget.AppCompatButton;
import com.test.helpers.AMH;
import com.test.helpers.Helper;
import com.test.selfadview.R;
import com.pcvirt.analytics.A;
import com.pcvirt.debug.D;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class SelfAdView
extends WebView {
    public static final String SELFAD_NS = "http://selfad.test.com";
    boolean isExtraContentVisible;
    boolean loaded = false;
    ProgressBar loader;
    boolean loadingError = false;
    Runnable onPageLoaded;
    boolean showExtraContentIfError;
    String urlPostfix = null;

    public SelfAdView(Context context) {
        super(context);
        this._init(context, null, null);
    }

    public SelfAdView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this._init(context, attributeSet, null);
    }

    public SelfAdView(Context context, AttributeSet attributeSet, int n) {
        super(context, attributeSet, n);
        this._init(context, attributeSet, null);
    }

    public SelfAdView(Context context, Object object) {
        super(context);
        this._init(context, null, object);
    }

    public static void addAllAppsLinkButton(ViewGroup viewGroup) {
        final Context context = viewGroup.getContext();
        AppCompatButton appCompatButton = new AppCompatButton(context, null, R.attr.widgetAppCompatButtonBorderlessColoredStyle);
        appCompatButton.setText((CharSequence)"Check out all our apps");
        appCompatButton.setTextColor(-1);
        appCompatButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view) {
                try {
                    SelfAdView.sendEvent("launchDeveloperMarketUrl");
                    AMH.launchDeveloperMarketUrl((Context)context, (String)context.getResources().getString(R.string.market_developers_name_escaped));
                    return;
                }
                catch (Throwable throwable) {
                    Toast.makeText((Context)context.getApplicationContext(), (CharSequence)throwable.getMessage(), (int)1).show();
                    return;
                }
            }
        });
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.addView((View)appCompatButton);
        linearLayout.setGravity(17);
        viewGroup.addView((View)linearLayout);
        SelfAdView.setWHinPx((View)linearLayout, -1, -2);
    }

    private String getAttributeStringValue(AttributeSet attributeSet, String string2, String string3, String string4) {
        String string5 = attributeSet.getAttributeValue(string2, string3);
        if (string5 == null) {
            string5 = string4;
        }
        return string5;
    }

    public static boolean isAppInstalled(Context context, String string2) {
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(string2, 1);
            return true;
        }
        catch (PackageManager.NameNotFoundException nameNotFoundException) {
            return false;
        }
    }

    public static void sendEvent(String string2) {
        A.sendAdEvent((String)"self-ad view", (String)string2, (boolean)false, (float)1.0f);
    }

    public static void setWHinPx(View view, int n, int n2) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(n, n2);
        } else {
            layoutParams.width = n;
            layoutParams.height = n2;
        }
        view.setLayoutParams(layoutParams);
    }

    protected void _forceNoZoom() {
        this.setInitialScale(this.getDensityScaledPx(100));
        this.getSettings().setLoadWithOverviewMode(false);
        this.getSettings().setUseWideViewPort(false);
    }

    protected ViewGroup.LayoutParams _getLayoutParams(View view, int n, int n2, View view2) {
        if (view2 == null) {
            view2 = (View)view.getParent();
        }
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (view2 instanceof LinearLayout && !(layoutParams instanceof LinearLayout.LayoutParams)) {
            layoutParams = new LinearLayout.LayoutParams(n, n2);
        }
        if (view2 instanceof RelativeLayout && !(layoutParams instanceof RelativeLayout.LayoutParams)) {
            return new RelativeLayout.LayoutParams(n, n2);
        }
        if (view2 instanceof FrameLayout && !(layoutParams instanceof FrameLayout.LayoutParams)) {
            return new FrameLayout.LayoutParams(n, n2);
        }
        if (layoutParams != null) {
            layoutParams.width = n;
            layoutParams.height = n2;
            return layoutParams;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unexpected layout type: ");
        stringBuilder.append(view2.getClass().getSimpleName());
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    protected ViewGroup.LayoutParams _getLayoutParams(View view, int n, int n2, ViewParent viewParent) {
        return this._getLayoutParams(view, n, n2, (View)viewParent);
    }

    @SuppressLint(value={"SetJavaScriptEnabled"})
    protected void _init(Context context, AttributeSet attributeSet, Object object) {
        boolean bl;
        this.setWebChromeClient(new WebChromeClient(){

            public void onReceivedTitle(WebView webView, String string2) {
                super.onReceivedTitle(webView, string2);
                if (string2 == null || !string2.equals((Object)"ad_success")) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("error title: ");
                    stringBuilder.append(string2);
                    SelfAdView.sendEvent(stringBuilder.toString());
                    SelfAdView.this._loadFallbackContent();
                    SelfAdView selfAdView = SelfAdView.this;
                    selfAdView.loadingError = true;
                    if (selfAdView.showExtraContentIfError) {
                        selfAdView.generateExtraContent();
                    }
                }
            }
        });
        this.setWebViewClient(new WebViewClient(){

            public void onPageFinished(WebView webView, String string2) {
                ViewParent viewParent;
                Runnable runnable;
                super.onPageFinished(webView, string2);
                SelfAdView selfAdView = SelfAdView.this;
                selfAdView.loaded = true;
                ProgressBar progressBar = selfAdView.loader;
                if (progressBar != null && (viewParent = progressBar.getParent()) != null && viewParent instanceof ViewGroup) {
                    ((ViewGroup)viewParent).removeView((View)SelfAdView.this.loader);
                }
                if ((runnable = SelfAdView.this.onPageLoaded) != null) {
                    runnable.run();
                }
            }
        });
        this._forceNoZoom();
        this._setTransparentBackground();
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setCacheMode(2);
        if (attributeSet != null) {
            String string2;
            String string3 = this.getAttributeStringValue(attributeSet, SELFAD_NS, "campaign_medium", null);
            if (string3 != null) {
                object = new JsInterface(context, new AMH.PlayCampaignParameters(context, string3));
            }
            if ((string2 = this.getAttributeStringValue(attributeSet, SELFAD_NS, "type", null)) != null) {
                this.appendType(string2);
            }
            bl = "true".equals((Object)this.getAttributeStringValue(attributeSet, SELFAD_NS, "autoload", null));
        } else {
            bl = false;
        }
        if (object != null) {
            this.addJavascriptInterface(object, "App");
        }
        SelfAdView.sendEvent("open");
        if (bl) {
            this.load();
        }
    }

    protected void _loadFallbackContent() {
        this.loadData("", "text/html", null);
        this.setVisibility(8);
    }

 

    public void addJavascriptInterface(Object object) {
        this.addJavascriptInterface(object, "App");
    }

    public void appendPurchasedAdRemoval(boolean bl) {
        if (bl) {
            this.appendUrlPostfix("par", "1");
        }
    }

    public void appendThemeFontColor(int n) {
        this.appendUrlPostfix("tfc", this.formatCssColor(n));
    }

    public void appendTo(ViewGroup viewGroup) {
        this.appendTo(viewGroup, false);
    }

    public void appendTo(ViewGroup viewGroup, boolean bl) {
        this.appendTo(viewGroup, bl, null);
    }

    public void appendTo(ViewGroup viewGroup, boolean bl, Integer n) {
        ViewGroup.LayoutParams layoutParams = this._getLayoutParams((View)this, -1, -2, (View)viewGroup);
        if (n != null && layoutParams instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams)layoutParams).setMargins(n.intValue(), 0, n.intValue(), 0);
        }
        viewGroup.addView((View)this, layoutParams);
        if (bl) {
            this.generateExtraContent();
        }
        this.load();
    }

    public void appendType(String string2) {
        try {
            string2 = URLEncoder.encode((String)string2, (String)"UTF-8");
        }
        catch (UnsupportedEncodingException unsupportedEncodingException) {}
        this.appendUrlPostfix("t", string2);
    }

    public void appendUrlPostfix(String string2) {
        StringBuilder stringBuilder = new StringBuilder();
        String string3 = this.urlPostfix;
        if (string3 == null) {
            string3 = "";
        }
        stringBuilder.append(string3);
        stringBuilder.append(string2);
        this.urlPostfix = stringBuilder.toString();
    }

    public void appendUrlPostfix(String string2, String string3) {
        try {
            string2 = URLEncoder.encode((String)string2, (String)"UTF-8");
            string3 = URLEncoder.encode((String)string3, (String)"UTF-8");
        }
        catch (UnsupportedEncodingException unsupportedEncodingException) {}
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("&");
        stringBuilder.append(string2);
        stringBuilder.append("=");
        stringBuilder.append(string3);
        this.appendUrlPostfix(stringBuilder.toString());
    }

    public String formatCssColor(int n) {
        Object[] arrobject = new Object[]{n & 16777215};
        return String.format((String)"%06X", (Object[])arrobject);
    }

    public void generateExtraContent() {
        if (this.isExtraContentVisible) {
            return;
        }
        this.isExtraContentVisible = true;
        ViewGroup viewGroup = (ViewGroup)this.getParent();
        this.loader = new ProgressBar(this.getContext());
        viewGroup.addView((View)this.loader);
        ViewGroup.LayoutParams layoutParams = this._getLayoutParams((View)this.loader, -2, -2, (View)viewGroup);
        if (layoutParams instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams)layoutParams).gravity = 17;
        }
        this.loader.setLayoutParams(layoutParams);
        SelfAdView.addAllAppsLinkButton(viewGroup);
    }

    protected int getDensityScaledPx(int n) {
        return Math.round((float)(this.getContext().getResources().getDisplayMetrics().density * (float)n));
    }

    public String getStatusString() {
        if (this.loadingError) {
            return "ad error";
        }
        if (this.loaded) {
            return "ad loaded";
        }
        return "ad not loaded";
    }

    public boolean hasLoadingError() {
        return this.loadingError;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public void load() {
        this.loadInto(this);
    }


    public void loadInto(WebView webView) {
        StringBuilder stringBuilder;
        String string2;
        String string3;
        try {
            Context context = webView.getContext();
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            stringBuilder = new StringBuilder();
            stringBuilder.append("http://test.com/android/ads/?v=5&ap=");
            stringBuilder.append(context.getApplicationContext().getPackageName());
            stringBuilder.append("&am=");
            stringBuilder.append(context.getResources().getString(R.string.app_market));
            stringBuilder.append("&ar=");
            stringBuilder.append(context.getResources().getString(R.string.app_release));
            stringBuilder.append("&avn=");
            stringBuilder.append(packageInfo.versionName);
            stringBuilder.append("&avc=");
            stringBuilder.append(packageInfo.versionCode);
            stringBuilder.append("&ov=");
            stringBuilder.append(Build.VERSION.SDK_INT);
            stringBuilder.append("&dm=");
            stringBuilder.append(Build.MANUFACTURER);
            stringBuilder.append("&dd=");
            stringBuilder.append(Build.MODEL);
            stringBuilder.append("&ddd=");
            stringBuilder.append(context.getResources().getDisplayMetrics().density);
            boolean bl = SelfAdView.isAppInstalled(context, "com.google.android.wearable.app");
            string2 = "";
            string3 = bl ? "&fw=1" : string2;
        }
        catch (PackageManager.NameNotFoundException nameNotFoundException) {
            nameNotFoundException.printStackTrace();
            return;
        }
        stringBuilder.append(string3);
        if (this.urlPostfix != null) {
            string2 = this.urlPostfix;
        }
        stringBuilder.append(string2);
        String string4 = stringBuilder.toString();
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("url=");
        stringBuilder2.append(string4);
        D.i((String)stringBuilder2.toString());
        webView.clearCache(true);
        webView.loadUrl(string4);
        A.sendAdEvent((String)"self-ad", (String)"load", (boolean)false, (float)1.0f);
    }

    public void setOnPageLoaded(Runnable runnable) {
        this.onPageLoaded = runnable;
    }

    public void setUrlPostfix(String string2) {
        this.urlPostfix = string2;
    }

    public void showExtraContentIfError(boolean bl) {
        this.showExtraContentIfError = bl;
    }

    public class JsInterface {
        protected Activity activity;
        public AMH.PlayCampaignParameters campaignParams;
        public ViewGroup container;
        protected Context context;

        public JsInterface(Activity activity, ViewGroup viewGroup, AMH.PlayCampaignParameters playCampaignParameters) {
            this.activity = activity;
            this.context = activity;
            this.container = viewGroup;
            this.campaignParams = playCampaignParameters;
        }

        public JsInterface(Context context, AMH.PlayCampaignParameters playCampaignParameters) {
            this.context = context;
            this.campaignParams = playCampaignParameters;
        }

        @JavascriptInterface
        public void documentSizeKnown(int n, final int n2) {
            new Handler(this.context.getMainLooper()).post(new Runnable(){

                public void run() {
                    try {
                        SelfAdView.this.setLayoutParams(SelfAdView.this._getLayoutParams((View)SelfAdView.this, -1, SelfAdView.this.getDensityScaledPx(n2), SelfAdView.this.getParent()));
                        return;
                    }
                    catch (Throwable throwable) {
                        throwable.printStackTrace();
                        return;
                    }
                }
            });
        }

        public boolean isAppInstalled(Context context, String string2) {
            PackageManager packageManager = context.getPackageManager();
            try {
                packageManager.getPackageInfo(string2, 1);
                return true;
            }
            catch (PackageManager.NameNotFoundException nameNotFoundException) {
                return false;
            }
        }

        @JavascriptInterface
        public void launchDeveloperMarketUrl() {
            try {
                SelfAdView.sendEvent("launchDeveloperMarketUrl");
                AMH.launchDeveloperMarketUrl((Context)this.context, (String)SelfAdView.this.getResources().getString(R.string.market_developers_name_escaped));
                return;
            }
            catch (Throwable throwable) {
                Toast.makeText((Context)this.context, (CharSequence)throwable.getMessage(), (int)1).show();
                return;
            }
        }

        @JavascriptInterface
        public void launchMarketUrl(String string2) {
            try {
                if (this.isAppInstalled(this.context, string2)) {
                    Helper.openApp((Context)this.context, (String)string2);
                    return;
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("launchMarketUrl(");
                stringBuilder.append(string2);
                stringBuilder.append(")");
                SelfAdView.sendEvent(stringBuilder.toString());
                AMH.launchMarketUrl((Context)this.context, (String)string2, (AMH.PlayCampaignParameters)this.campaignParams);
                return;
            }
            catch (Throwable throwable) {
                Toast.makeText((Context)this.context, (CharSequence)throwable.getMessage(), (int)1).show();
                return;
            }
        }

        @JavascriptInterface
        public void launchMarketUrl(String string2, String string3) {
            try {
                if (this.isAppInstalled(this.context, string2)) {
                    Helper.openApp((Context)this.context, (String)string2);
                    return;
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("launchMarketUrl(");
                stringBuilder.append(string2);
                stringBuilder.append(")");
                SelfAdView.sendEvent(stringBuilder.toString());
                AMH.PlayCampaignParameters playCampaignParameters = new AMH.PlayCampaignParameters(this.campaignParams);
                playCampaignParameters.content = string3;
                AMH.launchMarketUrl((Context)this.context, (String)string2, (AMH.PlayCampaignParameters)playCampaignParameters);
                return;
            }
            catch (Throwable throwable) {
                Toast.makeText((Context)this.context, (CharSequence)throwable.getMessage(), (int)1).show();
                return;
            }
        }

        @JavascriptInterface
        public void launchWebUrl(String string2) {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("launchWebUrl(");
                stringBuilder.append(string2);
                stringBuilder.append(")");
                SelfAdView.sendEvent(stringBuilder.toString());
                this.activity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse((String)string2)));
                return;
            }
            catch (Throwable throwable) {
                throwable.printStackTrace();
                Toast.makeText((Context)this.context, (CharSequence)throwable.getMessage(), (int)1).show();
                return;
            }
        }

        @JavascriptInterface
        public void showAdAbove() {
            Activity activity = this.activity;
            if (activity == null) {
                return;
            }
            activity.runOnUiThread(new Runnable(){

                public void run() {
                    JsInterface jsInterface = JsInterface.this;
                    ViewGroup viewGroup = jsInterface.container;
                    if (viewGroup != null) {
                        try {
                            viewGroup.removeView((View)jsInterface.SelfAdView.this);
                            JsInterface.this.container.addView((View)SelfAdView.this, 0);
                            return;
                        }
                        catch (Throwable throwable) {
                            throwable.printStackTrace();
                            return;
                        }
                    }
                    D.w((String)"self-ad can only show above if container is specified");
                }
            });
        }

    }

}

