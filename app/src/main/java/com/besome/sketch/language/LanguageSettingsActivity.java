package com.besome.sketch.language;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.widget.Toolbar;

import com.besome.sketch.MainActivity;
import com.besome.sketch.SketchApplication;
import com.besome.sketch.language.util.MultiLanguageUtil;
import com.besome.sketch.language.util.SpUtil;
import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.besome.sketch.lib.ui.PropertyOneLineItem;
import com.sketchware.remod.R;

import java.util.Locale;
import java.util.Objects;

import a.a.a.mB;
import mod.hey.studios.util.Helper;

public class LanguageSettingsActivity extends BaseAppCompatActivity implements View.OnClickListener {
    private static final int ITEM_LANGUAGE_CHINESE = 1;
    private static final int ITEM_LANGUAGE_ENGLISH = 2;
    private LinearLayout content;

    private void addSingleLineItem(int key, int name) {
        addSingleLineItem(key, getString(name));
    }
    private void addSingleLineItem(int key, String name) {
        PropertyOneLineItem item = new PropertyOneLineItem(this);
        item.setKey(key);
        item.setName(name);
        content.addView(item);
        if (key == ITEM_LANGUAGE_CHINESE || key == ITEM_LANGUAGE_ENGLISH) {
            item.setOnClickListener(this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.layout_main_logo).setVisibility(View.GONE);
        getSupportActionBar().setTitle(Helper.getResString(R.string.main_drawer_title_language_settings));
        toolbar.setNavigationOnClickListener(Helper.getBackPressedClickListener(this));
        content = findViewById(R.id.content);
        addSingleLineItem(ITEM_LANGUAGE_CHINESE,getString(R.string.chinese));
        addSingleLineItem(ITEM_LANGUAGE_ENGLISH,getString(R.string.english));
    }

    @Override
    public void onClick(View v) {
        if (!mB.a()) {
            int key;
            if (v instanceof PropertyOneLineItem) {
                key = ((PropertyOneLineItem) v).getKey();
                switch (key) {
                    case ITEM_LANGUAGE_CHINESE:
                        changeLanguage("zh", "CN");
                        break;
                    case ITEM_LANGUAGE_ENGLISH:
                        changeLanguage("en", "US");
                        break;
                    default:
                        break;
                }
            }
        }
    }

    //修改应用内语言设置
    private void changeLanguage(String language, String area) {
        if (TextUtils.isEmpty(language) && TextUtils.isEmpty(area)) {
            //如果语言和地区都是空，那么跟随系统
            SpUtil.saveString(ConstantGlobal.LOCALE_LANGUAGE, "");
            SpUtil.saveString(ConstantGlobal.LOCALE_COUNTRY, "");
        } else {
            //不为空，那么修改app语言，并true是把语言信息保存到sp中，false是不保存到sp中
            Locale newLocale = new Locale(language, area);
            MultiLanguageUtil.changeAppLanguage(LanguageSettingsActivity.this, newLocale, true);
            MultiLanguageUtil.changeAppLanguage(SketchApplication.getContext(), newLocale, true);
        }
        //重启app,这一步一定要加上，如果不重启app，可能打开新的页面显示的语言会不正确
        Intent intent = new Intent(SketchApplication.getContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        SketchApplication.getContext().startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
