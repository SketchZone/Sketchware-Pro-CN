package dev.aldi.sayuti.editor.injection;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.color.MaterialColors;
import com.google.gson.Gson;
import com.sketchware.remod.R;
import com.sketchware.remod.databinding.AddCustomAttributeBinding;

import java.util.ArrayList;
import java.util.HashMap;

import mod.SketchwareUtil;
import mod.agus.jcoderz.lib.FileUtil;
import mod.hey.studios.util.Helper;
import mod.remaker.view.CustomAttributeView;

public class AddCustomAttributeActivity extends AppCompatActivity {

    private ArrayList<HashMap<String, Object>> activityInjections = new ArrayList<>();
    private String activityInjectionsFilePath = "";
    private String widgetType = "";
    private AddCustomAttributeBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=AddCustomAttributeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.addAttrFab.setOnClickListener(v -> dialog("create", 0));
        Toolbar toolbar = (Toolbar) getLayoutInflater().inflate(R.layout.toolbar_improved, binding.background, false);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(widgetType);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
        binding.background.addView(toolbar, 0);

        if (getIntent().hasExtra("sc_id") && getIntent().hasExtra("file_name") && getIntent().hasExtra("widget_type")) {
            String sc_id = getIntent().getStringExtra("sc_id");
            String activityFilename = getIntent().getStringExtra("file_name");
            widgetType = getIntent().getStringExtra("widget_type");

            activityInjectionsFilePath = FileUtil.getExternalStorageDir() + "/.sketchware/data/" + sc_id + "/injection/appcompat/" + activityFilename;
            if (!FileUtil.isExistFile(activityInjectionsFilePath) || FileUtil.readFile(activityInjectionsFilePath).equals("")) {
                activityInjections = new Gson().fromJson(AppCompatInjection.getDefaultActivityInjections(), Helper.TYPE_MAP_LIST);
            } else {
                activityInjections = new Gson().fromJson(FileUtil.readFile(activityInjectionsFilePath), Helper.TYPE_MAP_LIST);
            }
            binding.addAttrListview.setAdapter(new CustomAdapter(activityInjections));
            ((BaseAdapter) binding.addAttrListview.getAdapter()).notifyDataSetChanged();
        } else {
            finish();
        }
    }

    private void dialog(String type, int position) {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View inflate = getLayoutInflater().inflate(R.layout.custom_dialog_attribute, null);
        dialog.setView(inflate);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView save = inflate.findViewById(R.id.dialog_btn_save);
        TextView cancel = inflate.findViewById(R.id.dialog_btn_cancel);
        EditText namespace = inflate.findViewById(R.id.dialog_input_res);
        EditText name = inflate.findViewById(R.id.dialog_input_attr);
        EditText value = inflate.findViewById(R.id.dialog_input_value);

        if (type.equals("edit")) {
            String injectionValue = activityInjections.get(position).get("value").toString();
            namespace.setText(injectionValue.substring(0, injectionValue.indexOf(":")));
            name.setText(injectionValue.substring(injectionValue.indexOf(":") + 1, injectionValue.indexOf("=")));
            value.setText(injectionValue.substring(injectionValue.indexOf("\"") + 1, injectionValue.length() - 1));
        }

        save.setOnClickListener(v -> {
            String namespaceInput = namespace.getText().toString();
            String nameInput = name.getText().toString();
            String valueInput = value.getText().toString();
            if (!namespaceInput.trim().equals("") && !nameInput.trim().equals("") && !valueInput.trim().equals("")) {
                String newValue = namespaceInput + ":" + nameInput + "=\"" + valueInput + "\"";
                if (type.equals("create")) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("type", widgetType);
                    map.put("value", newValue);
                    activityInjections.add(map);
                    SketchwareUtil.toast(getString(R.string.common_word_added));
                } else if (type.equals("edit")) {
                    activityInjections.get(position).put("value", newValue);
                    SketchwareUtil.toast(getString(R.string.common_word_saved));
                }
                binding.addAttrListview.setAdapter(new CustomAdapter(activityInjections));
                ((BaseAdapter) binding.addAttrListview.getAdapter()).notifyDataSetChanged();
                dialog.dismiss();
                FileUtil.writeFile(activityInjectionsFilePath, new Gson().toJson(activityInjections));
            }
        });
        cancel.setOnClickListener(Helper.getDialogDismissListener(dialog));
        dialog.show();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        namespace.requestFocus();
    }

    private class CustomAdapter extends BaseAdapter {

        private final ArrayList<HashMap<String, Object>> injections;

        public CustomAdapter(ArrayList<HashMap<String, Object>> arrayList) {
            injections = filterInjections(arrayList);
        }

        private ArrayList<HashMap<String, Object>> filterInjections(ArrayList<HashMap<String, Object>> arrayList) {
            ArrayList<HashMap<String, Object>> filteredList = new ArrayList<>();
            for (HashMap<String, Object> injection : arrayList) {
                if (injection.containsKey("type") && injection.get("type").toString().equals(widgetType)) {
                    filteredList.add(injection);
                }
            }
            return filteredList;
        }

        @Override
        public int getCount() {
            return injections.size();
        }

        @Override
        public HashMap<String, Object> getItem(int position) {
            return injections.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CustomAttributeView attributeView = new CustomAttributeView(parent.getContext());

            int violet = getColor(attributeView, R.attr.colorViolet);
            int onSurface = getColor(attributeView, com.google.android.material.R.attr.colorOnSurface);
            int green = getColor(attributeView, R.attr.colorGreen);

            String value = getItem(position).get("value").toString();
            SpannableString spannableString = new SpannableString(value);
            spannableString.setSpan(new ForegroundColorSpan(violet), 0, value.indexOf(":"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(onSurface), value.indexOf(":"), value.indexOf("=") + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(green), value.indexOf("\""), value.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            attributeView.text.setText(spannableString);
            attributeView.icon.setRotation(90);
            attributeView.icon.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(AddCustomAttributeActivity.this, attributeView.icon);
                popupMenu.getMenu().add(Menu.NONE, 0, Menu.NONE, R.string.common_word_edit);
                popupMenu.getMenu().add(Menu.NONE, 1, Menu.NONE, R.string.common_word_delete);
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 0) {
                        dialog("edit", position);
                    } else {
                        injections.remove(position);
                        FileUtil.writeFile(activityInjectionsFilePath, new Gson().toJson(injections));
                        notifyDataSetChanged();
                        SketchwareUtil.toast(getString(R.string.deleted_successfully));
                    }
                    return true;
                });
                popupMenu.show();
            });

            return attributeView;
        }

        // todo: move that method to another class, maybe SkColors
        // SkColors#getColor(View, int) ?????
        private @ColorInt int getColor(View view, @AttrRes int resourceId) {
            return MaterialColors.getColor(view, resourceId);
        }
    }
}