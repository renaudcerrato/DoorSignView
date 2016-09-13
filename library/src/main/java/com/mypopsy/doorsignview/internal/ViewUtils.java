package com.mypopsy.doorsignview.internal;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;

/**
 * Created by Cerrato Renaud <renaud.cerrato@gmail.com>
 * https://github.com/renaudcerrato
 * 9/12/16
 */
public class ViewUtils {

    public static Activity getActivity(View view) {
        for(Context context = view.getContext(); context instanceof ContextWrapper; context = ((ContextWrapper)context).getBaseContext()) {
            if(context instanceof Activity) {
                return (Activity)context;
            }
        }
        throw new IllegalStateException("can't find activity");
    }

    public static float dpTopx(View view, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, view.getResources().getDisplayMetrics());
    }

    public static Display getDisplay(View view) {
        if(Build.VERSION.SDK_INT < 17)
            return getActivity(view).getWindowManager().getDefaultDisplay();
        else
            return view.getDisplay();
    }
}
