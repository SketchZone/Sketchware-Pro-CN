package com.besome.sketch.help;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.Toolbar;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.sketchware.remod.R;
import com.sketchware.remod.databinding.ActivityOssLibrariesBinding;

import a.a.a.mB;
import a.a.a.oB;
import mod.hey.studios.util.Helper;

public class LicenseActivity extends BaseAppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        com.sketchware.remod.databinding.ActivityOssLibrariesBinding binding = ActivityOssLibrariesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(Helper.getBackPressedClickListener(this));

        binding.licensesText.setText(new oB().b(getApplicationContext(), "oss.txt"));
        binding.licensesText.setAutoLinkMask(Linkify.WEB_URLS);
        binding.licensesText.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
