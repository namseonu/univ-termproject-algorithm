package com.example.midpoint_finder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.midpoint_finder.databinding.ActivitySplashBinding;

import kotlin.jvm.internal.Intrinsics;

public class SplashActivity extends AppCompatActivity {
    public ActivitySplashBinding binding;

    public final ActivitySplashBinding getBinding() {
        ActivitySplashBinding var = this.binding;
        if(var == null) Intrinsics.throwUninitializedPropertyAccessException("binding");
        return var;
    }

    public final void setBinding(ActivitySplashBinding var) {
        Intrinsics.checkNotNullParameter(var, "<set-?>");
        this.binding = var;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplashBinding var = ActivitySplashBinding.inflate(this.getLayoutInflater());
        Intrinsics.checkNotNullExpressionValue(var, "ActivitySplashBinding.inflate(layoutInflater)");
        this.binding = var;
        var = this.binding;

        if(var == null) Intrinsics.throwUninitializedPropertyAccessException("binding");

        this.setContentView((View)var.getRoot());
        (new Handler(Looper.getMainLooper())).postDelayed((Runnable)(new Runnable() {
            public final void run() {
                Intent intent = new Intent((Context)SplashActivity.this, MainActivity.class);
                SplashActivity.this.startActivity(intent);
            }
        }), 2000L);
    }
}