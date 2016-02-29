package br.org.institutotim.parapesquisa.util;

import android.view.View;
import android.view.ViewGroup;

public class ViewUtils {

    public static void enable(View layout) {
        changeEnabled(layout, true);
    }

    public static void disable(View layout) {
        changeEnabled(layout, false);
    }

    public static void changeEnabled(View layout, boolean enabled) {
        layout.setEnabled(enabled);
        if (!(layout instanceof ViewGroup)) {
            return;
        }

        ViewGroup group = (ViewGroup) layout;
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof ViewGroup) {
                changeEnabled(child, enabled);
            } else {
                child.setEnabled(enabled);
            }
        }
    }
}
