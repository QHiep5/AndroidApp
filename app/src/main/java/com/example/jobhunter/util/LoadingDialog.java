package com.example.jobhunter.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.TextView;

import com.example.jobhunter.R;

public class LoadingDialog {
    private Dialog dialog;
    private TextView messageTextView;

    public LoadingDialog(Context context) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        
        messageTextView = dialog.findViewById(R.id.messageTextView);
    }

    public void show(String message) {
        if (message != null && !message.isEmpty()) {
            messageTextView.setText(message);
        }
        dialog.show();
    }

    public void show() {
        show("Đang tải...");
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
} 