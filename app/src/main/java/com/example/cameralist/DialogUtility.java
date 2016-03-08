package com.example.cameralist;

import android.content.Context;
import android.support.v7.app.AlertDialog;

/**
 * Created by JesusManuel on 17/01/16.
 */
public class DialogUtility {

    public static AlertDialog createSimpleDialog(Context context, String title, String message) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(context)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, null);
        return builder.create();
    }
}
