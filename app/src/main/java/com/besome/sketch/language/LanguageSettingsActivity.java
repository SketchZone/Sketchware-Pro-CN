package com.besome.sketch.language;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.besome.sketch.MainActivity;
import com.besome.sketch.SketchApplication;
import com.besome.sketch.language.util.MultiLanguageUtil;
import com.besome.sketch.language.util.SpUtil;
import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.sketchware.remod.R;

import java.util.Locale;
import java.util.Objects;

import mod.hey.studios.util.Helper;

public class LanguageSettingsActivity extends BaseAppCompatActivity {
    private RadioGroup radioGroup_language;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        radioGroup_language = findViewById(R.id.rg_language);
        RadioButton radioButton_zh = findViewById(R.id.rb_chinese);
        RadioButton radioButton_en = findViewById(R.id.rb_english);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.layout_main_logo).setVisibility(View.GONE);
        getSupportActionBar().setTitle(Helper.getResString(R.string.main_drawer_title_language_settings));
        toolbar.setNavigationOnClickListener(Helper.getBackPressedClickListener(this));
        getAppLanguage();
    }

    private void getAppLanguage() {
        String mLocale;
        // 截取java虚拟机返回字符串的前两个字符（中文：zh   英文：en ）。
        // 因为返回的内容为zh_CN，其中zh代表语言（中文），CN代表地区或国家（中国大陆）。
        // 截取前两位只对语言进行判断。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mLocale = String.valueOf(getResources().getConfiguration().getLocales().get(0)).substring(0, 2);
        } else {
            mLocale = String.valueOf(getResources().getConfiguration().locale).substring(0, 2);
        }

//        /**
//         * 也可以使用Locale获取
//         */
//        Locale myLocale = Locale.getDefault();
//        //语言
//        mLocale = myLocale.getLanguage();
//        //地区
//        String mCountry = myLocale.getCountry();

        /**
         * 缓存系统语言
         */
        switch (mLocale) {
            case "zh" ->//中文
                    radioGroup_language.check(R.id.rb_chinese);
            case "en" ->//英文
                    radioGroup_language.check(R.id.rb_english);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.language_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int v = item.getItemId();
        if (v == R.id.menu_save) {
            if (radioGroup_language.getCheckedRadioButtonId() == R.id.rb_chinese) {
                changeLanguage("zh", "CN");
            } else {
                changeLanguage("en", "US");
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * @param language
     * @param area
     */
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
