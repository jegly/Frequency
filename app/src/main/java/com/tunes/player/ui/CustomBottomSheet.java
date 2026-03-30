package com.tunes.player.ui;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.tunes.player.R;

public class CustomBottomSheet extends BottomSheetDialog {

    public CustomBottomSheet(@NonNull Context context) {
        super(context, R.style.BottomSheetDialog);
    }
}
