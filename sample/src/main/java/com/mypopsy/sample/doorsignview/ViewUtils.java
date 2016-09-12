package com.mypopsy.sample.doorsignview;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.View;

/**
 * Created by renaud on 9/12/16.
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
}
