/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author ldiog
 */
public final class Bundle {

    private static final Bundle bundle = new Bundle();
    private ResourceBundle resourceBundle;
    private final ArrayList<Locale> locales = new ArrayList<>();
    private Locale currentLocale;

    public enum Locales {
        English,
        Portugues
    };

    public Bundle() {
        locales.add(new Locale("en", "us"));
        locales.add(new Locale("pt", "pt"));
    }

    public static Bundle getBundle() {
        return bundle;
    }

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    public Locale getLocale(Locales localeEnum) {
        switch (localeEnum) {
            case English:
                return locales.get(0);
            case Portugues:
                return locales.get(1);
        }
        return null;
    }

    public void setCurrentLocale(Locales locale) {
        switch (locale) {
            case Portugues:
                currentLocale = new Locale("pt", "pt");
                break;
            case English:
                currentLocale = new Locale("en", "us");
                break;
        }

        resourceBundle = ResourceBundle.getBundle("Text", currentLocale);
    }

    public String getString(String str) {
        return resourceBundle.getString(str);
    }

}
