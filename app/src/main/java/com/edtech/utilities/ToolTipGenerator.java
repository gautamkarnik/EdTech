package com.edtech.utilities;

import android.content.Context;
import android.view.View;

import it.sephiroth.android.library.tooltip.Tooltip;

/**
 * Created by gautamkarnik on 2017-04-14.
 */

public class ToolTipGenerator {

    // wrapper to display pop up window
    public static void showTooltip (Context context, View view, String message, Tooltip.Gravity gravity) {
        Tooltip.make(context,
                new Tooltip.Builder(101) //just an interenal id
                        .anchor(view, gravity)
                        .closePolicy(new Tooltip.ClosePolicy()
                                .insidePolicy(true, false)
                                .outsidePolicy(true, false), 3000)
                        .activateDelay(800)
                        .showDelay(300)
                        .text(message)
                        .maxWidth(500)
                        .withArrow(true)
                        .withOverlay(true)
                        .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                        .build()
        ).show();
    }

}
