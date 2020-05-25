
package com.test.supportt.activity;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDex;
import com.test.supportt.R;
import com.test.supportt.activity.state.BaseApplicationState;
import com.test.supportt.adapter.item.ThemeInfo;
import com.test.supportt.helper.AH;
import com.test.supportt.helper.BaseResources;
import com.test.supportt.helper.LG;
import com.test.supportt.payment.AdManager;
import com.javax.genericclasses.GenericClasses;
import com.pcvirt.analytics.A;
import com.pcvirt.debug.D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class BaseApplication<S extends BaseApplicationState>
extends Application {
    public static final String PERSIST_DIR_NAME = "persist";
    public static final String PERSIST_STATE_FILENAME = "app_state.obj";
    public static final String PREFS_NEXT_PERSISTENCE_UNIQUE_ID_KEY = "next_persistence_unique_id";
    public static final String SETT_THEME_KEY = "sett_theme";
    final String SETT_PP_CONSENT_KEY = "pp_consent";
    public HashMap<String, LocaleLanguage> availableLocaleLanguages;
    Locale[] availableLocales;
    public Resources resources;
    public boolean settDarkActionBarTheme = true;
    public boolean settEnableAnalytics = true;
    public boolean settHasPPConsent = false;
    public boolean settInAppPurchases = false;
    public String settLanguage = "en";
    public boolean settLightTheme = true;
    public boolean settShowAds = false;
    public String settTheme = "";
    public int settThemeResid;
    public boolean settTranslationTermsAccepted;
    public int settWhatsNewOn;
    private S state = null;
    protected int themeResId = -1;
    protected ArrayList<String> translatablePrefixes;
    public ArrayList<String> userTranslatedLanguageCodes;

    private <T> Class<T> _getBaseActivityGenericType(int n) {
        return GenericClasses.resolveRawArguments(BaseApplication.class, (Object)((Object)this))[n];
    }

    private void debugCurrentLanguage() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Locale.getDefault().getLanguage()=");
        stringBuilder.append(Locale.getDefault().getLanguage());
        D.w((String)stringBuilder.toString());
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("getCurrentLanguage(this)=");
        stringBuilder2.append(BaseApplication.getCurrentLanguage((Context)this));
        D.w((String)stringBuilder2.toString());
        StringBuilder stringBuilder3 = new StringBuilder();
        stringBuilder3.append("getCurrentLanguage(getBaseContext())=");
        stringBuilder3.append(BaseApplication.getCurrentLanguage(this.getBaseContext()));
        D.w((String)stringBuilder3.toString());
        StringBuilder stringBuilder4 = new StringBuilder();
        stringBuilder4.append("getCurrentLanguage(getApplicationContext())=");
        stringBuilder4.append(BaseApplication.getCurrentLanguage(this.getApplicationContext()));
        D.w((String)stringBuilder4.toString());
        StringBuilder stringBuilder5 = new StringBuilder();
        stringBuilder5.append("settLanguage=");
        stringBuilder5.append(this.settLanguage);
        D.w((String)stringBuilder5.toString());
    }

    public static String getCurrentLanguage(Context context) {
        return context.getResources().getConfiguration().locale.getLanguage();
    }

    @Nullable
    public static String getLanguageCode(Locale locale) {
        String string2 = locale.getLanguage();
        if (locale.toString().equals((Object)string2)) {
            return string2;
        }
        String string3 = locale.getVariant();
        if (string3.length() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(string2);
            stringBuilder.append("-r");
            stringBuilder.append(string3);
            return stringBuilder.toString();
        }
        String string4 = locale.getCountry();
        if (string4.length() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(string2);
            stringBuilder.append("-r");
            stringBuilder.append(string4);
            return stringBuilder.toString();
        }
        return null;
    }

    @Nullable
    public static String getLanguageCodeLowercase(Locale locale) {
        String string2 = BaseApplication.getLanguageCode(locale);
        if (string2 == null) {
            return null;
        }
        return string2.toLowerCase();
    }

    public static String getLocaleLanguageTag(Locale locale) {
        if (Build.VERSION.SDK_INT >= 21) {
            return locale.toLanguageTag();
        }
        return null;
    }

    private ArrayList<String> getUserTranslatedLanguageCodes(Context context) {
        Iterator iterator = AH.getSettings((Context)context).getAll().keySet().iterator();
        ArrayList arrayList = null;
        while (iterator.hasNext()) {
            String string2 = (String)iterator.next();
            if (!string2.startsWith("ut_")) continue;
            String string3 = string2.split("_", 3)[1];
            if (arrayList == null) {
                arrayList = new ArrayList();
            }
            if (this.isUserTranslatedLanguageCode(string3)) continue;
            arrayList.add((Object)string3);
        }
        return arrayList;
    }

    private void initializeLocales() {
        if (this.availableLocaleLanguages == null) {
            this.availableLocaleLanguages = new HashMap();
            for (Locale locale : this.availableLocales = Locale.getAvailableLocales()) {
                boolean bl;
                String string2;
                String string3 = locale.getLanguage();
                LocaleLanguage localeLanguage = (LocaleLanguage)this.availableLocaleLanguages.get((Object)string3);
                if (localeLanguage == null) {
                    localeLanguage = new LocaleLanguage();
                    this.availableLocaleLanguages.put((Object)string3, (Object)localeLanguage);
                }
                if (bl = (string2 = BaseApplication.getLanguageCode(locale)) != null && !string2.contains((CharSequence)"-")) {
                    if (localeLanguage.generalLocale == null) {
                        localeLanguage.generalLocale = locale;
                        continue;
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("ERROR: generalLocale already set to ");
                    stringBuilder.append((Object)localeLanguage.generalLocale);
                    D.w((String)stringBuilder.toString());
                    this.debugLocale(localeLanguage.generalLocale);
                    this.debugLocale(locale);
                    continue;
                }
                localeLanguage.countriesLocales.add((Object)locale);
            }
            this.updateTranslatedLanguageCodes();
        }
    }

    public static void setContextLanguage(Context context, String string2) {
        Resources resources = context.getResources();
        resources.getConfiguration().locale = new Locale(string2);
        if (resources instanceof BaseResources) {
            ((BaseResources)resources).mSuperResources.getConfiguration().locale = new Locale(string2);
        }
        resources.updateConfiguration(resources.getConfiguration(), resources.getDisplayMetrics());
    }

    private void setLegacyPPConsent(SharedPreferences sharedPreferences) {
        boolean bl = this.settWhatsNewOn != 0;
        if (bl && !sharedPreferences.contains("pp_consent")) {
            this.updatePPConsent(bl);
        }
        this.settHasPPConsent = bl;
    }

    public boolean appHasAds() {
        return this.getString(R.string.has_ads).equals((Object)"true");
    }

    public boolean appHasRewardedAds() {
        if (!this.appHasAds()) {
            return false;
        }
        return this.getString(R.string.has_rewarded_ads).equals((Object)"true");
    }

    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install((Context)this);
    }

    public void debugLocale(String string2, Locale locale) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("LOCALE: ");
        stringBuilder.append(string2);
        D.w((String)stringBuilder.toString());
        this.debugLocale(locale);
    }

    public void debugLocale(Locale locale) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("------------------------------------------");
        stringBuilder.append((Object)locale);
        D.w((String)stringBuilder.toString());
        if (locale != null) {
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("getDisplayName()=");
            stringBuilder2.append(locale.getDisplayName());
            D.w((String)stringBuilder2.toString());
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append("getLanguage()=");
            stringBuilder3.append(locale.getLanguage());
            D.w((String)stringBuilder3.toString());
            StringBuilder stringBuilder4 = new StringBuilder();
            stringBuilder4.append("getDisplayLanguage()=");
            stringBuilder4.append(locale.getDisplayLanguage());
            D.w((String)stringBuilder4.toString());
            StringBuilder stringBuilder5 = new StringBuilder();
            stringBuilder5.append("getVariant()=");
            stringBuilder5.append(locale.getVariant());
            D.w((String)stringBuilder5.toString());
            StringBuilder stringBuilder6 = new StringBuilder();
            stringBuilder6.append("getDisplayVariant()=");
            stringBuilder6.append(locale.getDisplayVariant());
            D.w((String)stringBuilder6.toString());
            StringBuilder stringBuilder7 = new StringBuilder();
            stringBuilder7.append("getCountry()=");
            stringBuilder7.append(locale.getCountry());
            D.w((String)stringBuilder7.toString());
            StringBuilder stringBuilder8 = new StringBuilder();
            stringBuilder8.append("getDisplayCountry()=");
            stringBuilder8.append(locale.getDisplayCountry());
            D.w((String)stringBuilder8.toString());
            StringBuilder stringBuilder9 = new StringBuilder();
            stringBuilder9.append("getISO3Language()=");
            stringBuilder9.append(locale.getISO3Language());
            D.w((String)stringBuilder9.toString());
            try {
                StringBuilder stringBuilder10 = new StringBuilder();
                stringBuilder10.append("locale.getISO3Country()=");
                stringBuilder10.append(locale.getISO3Country());
                D.w((String)stringBuilder10.toString());
            }
            catch (Throwable throwable) {
                StringBuilder stringBuilder11 = new StringBuilder();
                stringBuilder11.append("locale.getISO3Country()= ERROR:");
                stringBuilder11.append(throwable.getMessage());
                D.w((String)stringBuilder11.toString());
            }
            StringBuilder stringBuilder12 = new StringBuilder();
            stringBuilder12.append("toLanguageTag()=");
            stringBuilder12.append(BaseApplication.getLocaleLanguageTag(locale));
            D.w((String)stringBuilder12.toString());
            StringBuilder stringBuilder13 = new StringBuilder();
            stringBuilder13.append("getIndexOfAvailableLocales=");
            stringBuilder13.append(this.getIndexOfAvailableLocales(locale));
            D.w((String)stringBuilder13.toString());
        }
        D.w((String)"------------------------------------------");
    }

    public void debugLocales() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("availableLocaleLanguages.size()=");
        stringBuilder.append(this.availableLocaleLanguages.size());
        D.i((String)stringBuilder.toString());
        for (String string2 : this.availableLocaleLanguages.keySet()) {
            LocaleLanguage localeLanguage = (LocaleLanguage)this.availableLocaleLanguages.get((Object)string2);
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("code=");
            stringBuilder2.append(string2);
            stringBuilder2.append(", countries: ");
            stringBuilder2.append(localeLanguage.countriesLocales.size());
            stringBuilder2.append(", name(dl)=");
            stringBuilder2.append(localeLanguage.generalLocale.getDisplayName());
            stringBuilder2.append(", name(en)=");
            stringBuilder2.append(localeLanguage.generalLocale.getDisplayName(Locale.US));
            D.i((String)stringBuilder2.toString());
            for (Locale locale : localeLanguage.countriesLocales) {
                StringBuilder stringBuilder3 = new StringBuilder();
                stringBuilder3.append("#code=");
                stringBuilder3.append(BaseApplication.getLanguageCode(locale));
                D.i((String)stringBuilder3.toString());
            }
        }
    }

    protected ArrayList<String> getAnalyticsProvidersList() {
        ArrayList arrayList = new ArrayList();
        arrayList.add((Object)"FIREBASE_ANALYTICS");
        return arrayList;
    }

    public ArrayList<String> getAppLanguages() {
        ArrayList arrayList = new ArrayList();
        arrayList.add((Object)"en");
        return arrayList;
    }

    public Locale getAvailableLocale(String string2) {
        LangCodeParts langCodeParts = new LangCodeParts(string2);
        LocaleLanguage localeLanguage = (LocaleLanguage)this.availableLocaleLanguages.get((Object)langCodeParts.generalLangCode);
        if (localeLanguage == null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("ERROR: NOT FOUND langCode=");
            stringBuilder.append(string2);
            D.w((String)stringBuilder.toString());
            return null;
        }
        if (langCodeParts.countryCode == null) {
            return localeLanguage.generalLocale;
        }
        for (Locale locale : localeLanguage.countriesLocales) {
            String string3 = BaseApplication.getLanguageCodeLowercase(locale);
            if (string3 == null || !string3.equals((Object)string2.toLowerCase())) continue;
            return locale;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ERROR: NOT FOUND langCode=");
        stringBuilder.append(string2);
        D.w((String)stringBuilder.toString());
        return null;
    }

    int getIndexOfAvailableLocales(Locale locale) {
        Locale[] arrlocale;
        String string2 = BaseApplication.getLanguageCode(locale);
        for (int i = 0; i < -1 + (arrlocale = this.availableLocales).length; ++i) {
            String string3 = BaseApplication.getLanguageCode(arrlocale[i]);
            if (string3 == null || !string3.equals((Object)string2)) continue;
            return i;
        }
        return -1;
    }

    public Locale getLocale(String string2) {
        Locale locale = this.getAvailableLocale(string2);
        if (locale == null) {
            locale = this.getNewLocale(string2);
        }
        return locale;
    }

    public Locale getNewLocale(String string2) {
        LangCodeParts langCodeParts = new LangCodeParts(string2);
        String string3 = langCodeParts.countryCode;
        Locale locale = string3 != null ? new Locale(langCodeParts.generalLangCode, string3) : new Locale(langCodeParts.generalLangCode);
        String string4 = BaseApplication.getLanguageCode(locale);
        if (string2.equalsIgnoreCase(string4)) {
            return locale;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ERROR: language.code=");
        stringBuilder.append(string4);
        stringBuilder.append(" must be equal to langCode=");
        stringBuilder.append(string2);
        throw new Error(stringBuilder.toString());
    }

  
    public int getNextPersistUniqueId() {
        BaseApplication baseApplication = this;
        synchronized (baseApplication) {
            SharedPreferences sharedPreferences = AH.getSettings((Context)this);
            int n = sharedPreferences.getInt(PREFS_NEXT_PERSISTENCE_UNIQUE_ID_KEY, 1);
            sharedPreferences.edit().putInt(PREFS_NEXT_PERSISTENCE_UNIQUE_ID_KEY, n + 1).apply();
            return n;
        }
    }

    @NonNull
    public File getPersistDir() {
        return new File(this.getApplicationContext().getFilesDir(), PERSIST_DIR_NAME);
    }

    protected String getSettingsTheme(SharedPreferences sharedPreferences) {
        return AH.getTheme((Context)this, (SharedPreferences)sharedPreferences);
    }

    public S getState() {
        if (this.state == null) {
            File file = this.getStateFile();
            if (file.exists()) {
                try {
                    this.state = (BaseApplicationState)BaseApplicationState.readStateFromFile((File)file);
                }
                catch (Throwable throwable) {
                    throwable.printStackTrace();
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("e=");
                    stringBuilder.append(D.getExceptionInfo((Throwable)throwable));
                    stringBuilder.append(", stateFile=");
                    stringBuilder.append((Object)file);
                    stringBuilder.append(", stateFile.exists=");
                    stringBuilder.append(file.exists());
                    stringBuilder.append(", stateFile.length=");
                    stringBuilder.append(file.length());
                    A.sendDebugEvent((String)"de-persist state error", (String)stringBuilder.toString());
                    Toast.makeText((Context)this.getApplicationContext(), (int)R.string.t_read_persisted_state_error_toast_v2, (int)1).show();
                    file.delete();
                }
            }
            if (this.state == null) {
                this.resetState();
            }
        }
        return this.state;
    }

    public String getStateDebug() {
        S s = this.state;
        if (s != null) {
            return s.toString();
        }
        return null;
    }

    @NonNull
    public File getStateFile() {
        return new File(this.getPersistDir(), PERSIST_STATE_FILENAME);
    }

    public ArrayList<String> getSupportedAppLanguages() {
        ArrayList arrayList = new ArrayList();
        for (String string2 : this.getAppLanguages()) {
            if (this.getAvailableLocale(string2) == null) continue;
            arrayList.add((Object)string2);
        }
        return arrayList;
    }

    public String getTranslationResourceName(String string2) {
        for (int i = 1; i < this.translatablePrefixes.size(); ++i) {
            if (!string2.startsWith((String)this.translatablePrefixes.get(i))) continue;
            return string2;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("t_");
        stringBuilder.append(string2);
        return stringBuilder.toString();
    }

    public String getWhatsNewPrefKey() {
        long l = AH.getInstallId((Context)this);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("whatsnew_");
        stringBuilder.append(l);
        return stringBuilder.toString();
    }

    void initialize() {
        D.activate((Context)this.getApplicationContext());
        this.initializeLocales();
        this.translatablePrefixes = new ArrayList();
        this.translatablePrefixes.add((Object)"t_");
    }

    boolean isSupportedAppLanguage(String string2) {
        if (string2 == null) {
            return false;
        }
        int n = this.getSupportedAppLanguages().indexOf((Object)string2);
        boolean bl = false;
        if (n > -1) {
            bl = true;
        }
        return bl;
    }

    public boolean isTranslatableResourceString(String string2, boolean bl) {
        if (bl) {
            Iterator iterator = this.translatablePrefixes.iterator();
            while (iterator.hasNext()) {
                if (!string2.startsWith((String)iterator.next())) continue;
                return true;
            }
        } else if (string2.startsWith("t_")) {
            return true;
        }
        return false;
    }

    public boolean isUserTranslatedLanguageCode(String string2) {
        ArrayList<String> arrayList = this.userTranslatedLanguageCodes;
        return arrayList != null && arrayList.indexOf((Object)string2) > -1;
    }

    public void loadSettings() {
        SharedPreferences sharedPreferences = AH.getSettings((Context)this);
        this.settTheme = this.getSettingsTheme(sharedPreferences);
        this.settLanguage = sharedPreferences.getString("sett_language", null);
        if (this.settLanguage == null) {
            this.settLanguage = Locale.getDefault().getLanguage();
            if (!this.isSupportedAppLanguage(this.settLanguage)) {
                this.settLanguage = LG.DEFAULT_LANGUAGE;
            }
            this.setLanguageAndUpdateContext(this.settLanguage);
        }
        this.settInAppPurchases = this.getResources().getString(R.string.in_app_purchases).equals((Object)"true");
        this.settShowAds = AdManager.initialiseShowAds((Context)this, (BaseApplication)this);
        this.settWhatsNewOn = sharedPreferences.getInt(this.getWhatsNewPrefKey(), 0);
        this.settHasPPConsent = sharedPreferences.getBoolean("pp_consent", false);
        this.setLegacyPPConsent(sharedPreferences);
        this.settTranslationTermsAccepted = sharedPreferences.getBoolean("translation_terms_accepted", false);
        this.settEnableAnalytics = sharedPreferences.getBoolean("sett_enableanalytics", true);
        if (!sharedPreferences.contains("sett_enableanalytics")) {
            sharedPreferences.edit().putBoolean("sett_enableanalytics", true).apply();
        }
        this.settThemeResid = AH.getThemeResId((Context)this, (String)this.settTheme);
        ThemeInfo themeInfo = AH.getThemeInfo((String)this.settTheme);
        this.settLightTheme = themeInfo.isLightTheme;
        this.settDarkActionBarTheme = themeInfo.isDarkActionBarTheme;
    }

    public void onCreate() {
        super.onCreate();
        this.initialize();
    }

    public void persistState() {
        S s = this.state;
        if (s != null) {
            BaseApplicationState.writeStateToFile(s, (File)this.getStateFile());
        }
    }

    public void resetState() {
        try {
            this.state = (BaseApplicationState)this._getBaseActivityGenericType(0).newInstance();
        }
        catch (IllegalAccessException illegalAccessException) {
            throw new RuntimeException((Throwable)illegalAccessException);
        }
        catch (InstantiationException instantiationException) {
            throw new RuntimeException((Throwable)instantiationException);
        }
        this.getStateFile().delete();
        return;
    }

    public void setContextLanguage() {
        BaseApplication.setContextLanguage((Context)this, this.settLanguage);
        BaseApplication.setContextLanguage(this.getBaseContext(), this.settLanguage);
    }

    public void setLanguage(String string2) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("newValue=");
        stringBuilder.append(string2);
        D.i((String)stringBuilder.toString());
        this.settLanguage = string2;
        AH.setSetting((Context)this.getApplicationContext(), (String)"sett_language", (String)this.settLanguage);
    }

    public void setLanguageAndUpdateContext(String string2) {
        this.setLanguage(string2);
        this.setContextLanguage();
    }

    public void setTheme(int n) {
        if (n != this.themeResId) {
            super.setTheme(n);
            this.themeResId = n;
        }
    }

    public void updatePPConsent(boolean bl) {
        this.settHasPPConsent = bl;
        AH.setSetting((Context)this, (String)"pp_consent", (boolean)bl);
    }

    public void updateTranslatedLanguageCodes() {
        this.userTranslatedLanguageCodes = this.getUserTranslatedLanguageCodes(this.getBaseContext());
    }

    class LangCodeParts {
        String countryCode;
        String generalLangCode;

        public LangCodeParts(String string2) {
            String[] arrstring = string2.split("-r");
            this.generalLangCode = arrstring[0];
            String string3 = arrstring.length > 1 ? arrstring[1] : null;
            this.countryCode = string3;
        }
    }

    public static class LocaleLanguage {
        public ArrayList<Locale> countriesLocales = new ArrayList();
        public Locale generalLocale;
    }

}

