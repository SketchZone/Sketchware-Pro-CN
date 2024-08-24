package mod.hilal.saif.activities.android_manifest;

import static mod.SketchwareUtil.getDip;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.besome.sketch.editor.manage.library.LibraryItemView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.sketchware.remod.R;
import com.sketchware.remod.databinding.AndroidManifestInjectionBinding;
import com.sketchware.remod.databinding.DialogAddCustomActivityBinding;
import com.sketchware.remod.databinding.DialogChangeLauncherActivityBinding;
import com.sketchware.remod.databinding.ProgressMsgBoxBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import a.a.a.aB;
import a.a.a.jC;
import a.a.a.wB;
import a.a.a.yq;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.component.Magnifier;
import mod.SketchwareUtil;
import mod.agus.jcoderz.lib.FileUtil;
import mod.hey.studios.code.SrcCodeEditor;
import mod.hey.studios.code.SrcCodeEditorLegacy;
import mod.hey.studios.util.Helper;
import mod.hilal.saif.activities.tools.ConfigActivity;
import mod.hilal.saif.android_manifest.AndroidManifestInjector;
import mod.jbk.code.CodeEditorColorSchemes;
import mod.jbk.code.CodeEditorLanguages;
import mod.remaker.view.CustomAttributeView;

@SuppressLint("SetTextI18n")
public class AndroidManifestInjection extends AppCompatActivity {

    private final ArrayList<HashMap<String, Object>> list_map = new ArrayList<>();
    private ListView act_list;
    private String sc_id;
    private String activityName;
    private AndroidManifestInjectionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AndroidManifestInjectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getIntent().hasExtra("sc_id") && getIntent().hasExtra("file_name")) {
            sc_id = getIntent().getStringExtra("sc_id");
            activityName = getIntent().getStringExtra("file_name").replaceAll(".java", "");
        }

        setupCustomToolbar();
        checkAttrs();
        setupViews();
        refreshList();
        checkAttrs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAttrs();
        refreshList();
    }

    private void checkAttrs() {
        String path = FileUtil.getExternalStorageDir().concat("/.sketchware/data/").concat(sc_id).concat("/Injection/androidmanifest/attributes.json");
        if (FileUtil.isExistFile(path)) {
            ArrayList<HashMap<String, Object>> data = new Gson().fromJson(FileUtil.readFile(path),
                    Helper.TYPE_MAP_LIST);
            for (int i = 0; i < data.size(); i++) {
                String str = (String) data.get(i).get("name");
                if (Objects.requireNonNull(str).equals("_application_attrs")) {
                    String str2 = (String) data.get(i).get("value");
                    if (Objects.requireNonNull(str2).contains("android:theme")) {
                        return;

                    }
                }
            }
            HashMap<String, Object> _item = new HashMap<>();
            _item.put("name", "_application_attrs");
            _item.put("value", "android:theme=\"@style/AppTheme\"");
            data.add(_item);
            FileUtil.writeFile(path, new Gson().toJson(data));
        }
    }

    private void setupViews() {
        LibraryItemView application_card = new LibraryItemView(this);
        makeup(application_card, R.drawable.icons8_app_attrs, getString(R.string.application), getString(R.string.default_properties_for_the_app));
        binding.cards.addView(application_card);
        application_card.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), AndroidManifestInjectionDetails.class);
            intent.putExtra("sc_id", sc_id);
            intent.putExtra("file_name", activityName);
            intent.putExtra("type", "application");
            startActivity(intent);
        });

        {
            LibraryItemView permission_card = new LibraryItemView(this);
            makeup(permission_card, R.drawable.event_on_signin_complete_48dp, getString(R.string.permissions), getString(R.string.add_custom_permissions_to_the_app));
            binding.cards.addView(permission_card);
            permission_card.setOnClickListener(_view -> {
                Intent inta = new Intent();
                inta.setClass(getApplicationContext(), AndroidManifestInjectionDetails.class);
                inta.putExtra("sc_id", sc_id);
                inta.putExtra("file_name", activityName);
                inta.putExtra("type", "permission");
                startActivity(inta);
            });
        }

        {
            LibraryItemView permission_card = new LibraryItemView(this);
            makeup(permission_card, R.drawable.recycling_48, getString(R.string.launcher_activity), getString(R.string.change_the_default_launcher_activity));
            binding.cards.addView(permission_card);
            permission_card.setOnClickListener(v -> showLauncherActDialog(AndroidManifestInjector.getLauncherActivity(sc_id)));
        }

        LibraryItemView allAct_card = new LibraryItemView(this);
        makeup(allAct_card, R.drawable.icons8_all_activities_attrs, getString(R.string.all_activities), getString(R.string.add_attributes_for_all_activities));
        binding.cards.addView(allAct_card);
        allAct_card.setOnClickListener(v -> {
            Intent inta = new Intent();
            inta.setClass(getApplicationContext(), AndroidManifestInjectionDetails.class);
            inta.putExtra("sc_id", sc_id);
            inta.putExtra("file_name", activityName);
            inta.putExtra("type", "all");
            startActivity(inta);
        });

        LibraryItemView appCom_card = new LibraryItemView(this);
        makeup(appCom_card, R.drawable.icons8_app_components, getString(R.string.app_components), getString(R.string.add_extra_components));
        binding.cards.addView(appCom_card);
        appCom_card.setOnClickListener(v -> showAppComponentDialog());

        act_list = new ListView(this);
        act_list.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        act_list.setDividerHeight(0);
        binding.content.addView(act_list);
    }

    private void  showAppComponentDialog() {
        Intent intent = new Intent();
        if (ConfigActivity.isLegacyCeEnabled()) {
            intent.setClass(getApplicationContext(), SrcCodeEditorLegacy.class);
        } else {
            intent.setClass(getApplicationContext(), SrcCodeEditor.class);
        }

        String APP_COMPONENTS_PATH = FileUtil.getExternalStorageDir().concat("/.sketchware/data/").concat(sc_id).concat("/Injection/androidmanifest/app_components.txt");
        if (!FileUtil.isExistFile(APP_COMPONENTS_PATH)) FileUtil.writeFile(APP_COMPONENTS_PATH, "");
        intent.putExtra("content", APP_COMPONENTS_PATH);
        intent.putExtra("xml", "");
        intent.putExtra("disableHeader", "");
        String title = getString(R.string.app_components);
        intent.putExtra("title",title );
        startActivity(intent);
    }

    private void showLauncherActDialog(String actnamr) {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
        dialog.setIcon(R.drawable.recycling_48);
        dialog.setTitle(Helper.getResString(R.string.change_launcher_activity_dialog_title));
        DialogChangeLauncherActivityBinding changeLauncherActivityBinding = DialogChangeLauncherActivityBinding.inflate(getLayoutInflater());
        dialog.setView(changeLauncherActivityBinding.getRoot());

        changeLauncherActivityBinding.activityNameInput.setText(actnamr);
        dialog.setPositiveButton(Helper.getResString(R.string.common_word_save),  (dialog1, which) -> {
            if (!changeLauncherActivityBinding.activityNameInput.getText().toString().trim().isEmpty()) {
                AndroidManifestInjector.setLauncherActivity(sc_id, changeLauncherActivityBinding.activityNameInput.getText().toString());
                SketchwareUtil.toast(getString(R.string.common_word_saved));
                dialog1.dismiss();
            } else {
                changeLauncherActivityBinding.activityNameInput.setError(getString(R.string.enter_activity_name));
            }
        });
        dialog.setNegativeButton(R.string.common_word_cancel, (dialog1, which) -> dialog1.dismiss());
        dialog.show();
    }

    // if you change method name, you need also change it in layout
    public void showAddActivityDialog(View view) {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
        dialog.setIcon(R.drawable.add_new_48_orange);
        dialog.setTitle(Helper.getResString(R.string.common_word_add_activtiy));
        DialogAddCustomActivityBinding addCustomActivityBinding = DialogAddCustomActivityBinding.inflate(getLayoutInflater());

        addCustomActivityBinding.activityNameInput.setText(activityName);
        dialog.setView(addCustomActivityBinding.getRoot());
        dialog.setPositiveButton(Helper.getResString(R.string.common_word_save), (dialog1, which) -> {
            if (!addCustomActivityBinding.activityNameInput.getText().toString().trim().isEmpty()) {
                addNewActivity(addCustomActivityBinding.activityNameInput.getText().toString());
                SketchwareUtil.toast(getString(R.string.new_activity_added));
                dialog1.dismiss();
            } else {
                addCustomActivityBinding.activityNameInput.setError(getString(R.string.enter_activity_name));
            }
        });
        dialog.setNegativeButton(R.string.common_word_cancel, (dialog1, which) -> dialog1.dismiss());
        dialog.show();
    }

    private void addNewActivity(String componentName) {
        String path = FileUtil.getExternalStorageDir().concat("/.sketchware/data/").concat(sc_id).concat("/Injection/androidmanifest/attributes.json");
        ArrayList<HashMap<String, Object>> data = new ArrayList<>();
        if (FileUtil.isExistFile(path)) {
            data = new Gson().fromJson(FileUtil.readFile(path), Helper.TYPE_MAP_LIST);
        }
        {
            HashMap<String, Object> _item = new HashMap<>();
            _item.put("name", componentName);
            _item.put("value", "android:configChanges=\"orientation|screenSize|keyboardHidden|smallestScreenSize|screenLayout\"");

            data.add(_item);
        }
        {
            HashMap<String, Object> _item = new HashMap<>();
            _item.put("name", componentName);
            _item.put("value", "android:hardwareAccelerated=\"true\"");

            data.add(_item);
        }
        {
            HashMap<String, Object> _item = new HashMap<>();
            _item.put("name", componentName);
            _item.put("value", "android:supportsPictureInPicture=\"true\"");

            data.add(_item);
        }
        {
            HashMap<String, Object> _item = new HashMap<>();
            _item.put("name", componentName);
            _item.put("value", "android:screenOrientation=\"portrait\"");

            data.add(_item);
        }
        {
            HashMap<String, Object> _item = new HashMap<>();
            _item.put("name", componentName);
            _item.put("value", "android:theme=\"@style/AppTheme\"");

            data.add(_item);
        }

        {
            HashMap<String, Object> _item = new HashMap<>();
            _item.put("name", componentName);
            _item.put("value", "android:windowSoftInputMode=\"stateHidden\"");

            data.add(_item);
        }


        FileUtil.writeFile(path, new Gson().toJson(data));
        refreshList();

    }

    private void refreshList() {
        list_map.clear();
        String path = FileUtil.getExternalStorageDir().concat("/.sketchware/data/").concat(sc_id).concat("/Injection/androidmanifest/attributes.json");
        ArrayList<String> temp = new ArrayList<>();
        ArrayList<HashMap<String, Object>> data;
        if (FileUtil.isExistFile(path)) {
            data = new Gson().fromJson(FileUtil.readFile(path), Helper.TYPE_MAP_LIST);
            for (int i = 0; i < data.size(); i++) {
                if (!temp.contains(Objects.requireNonNull(data.get(i).get("name")).toString())) {
                    if (!Objects.requireNonNull(data.get(i).get("name")).equals("_application_attrs") && !Objects.requireNonNull(data.get(i).get("name")).equals("_apply_for_all_activities") && !Objects.requireNonNull(data.get(i).get("name")).equals("_application_permissions")) {
                        temp.add((String) data.get(i).get("name"));
                    }
                }
            }
            for (int i = 0; i < temp.size(); i++) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("act_name", temp.get(i));
                list_map.add(map);
            }
            act_list.setAdapter(new ListAdapter(list_map));
            ((BaseAdapter) act_list.getAdapter()).notifyDataSetChanged();
        }
    }

    private void deleteActivity(int pos) {
        String activity_name = (String) list_map.get(pos).get("act_name");
        String path = FileUtil.getExternalStorageDir().concat("/.sketchware/data/").concat(sc_id).concat("/Injection/androidmanifest/attributes.json");
        ArrayList<HashMap<String, Object>> data;
        data = new Gson().fromJson(FileUtil.readFile(path), Helper.TYPE_MAP_LIST);
        for (int i = data.size() - 1; i > -1; i--) {
            String temp = (String) data.get(i).get("name");
            if (Objects.requireNonNull(temp).equals(activity_name)) {
                data.remove(i);
            }
        }
        FileUtil.writeFile(path, new Gson().toJson(data));
        refreshList();
        removeComponents(activity_name);
        SketchwareUtil.toast("Activity removed");
    }

    private void removeComponents(String str) {
        String path = FileUtil.getExternalStorageDir().concat("/.sketchware/data/").concat(sc_id).concat("/Injection/androidmanifest/activities_components.json");
        ArrayList<HashMap<String, Object>> data;
        if (FileUtil.isExistFile(path)) {
            data = new Gson().fromJson(FileUtil.readFile(path), Helper.TYPE_MAP_LIST);
            for (int i = data.size() - 1; i > -1; i--) {
                String name = (String) data.get(i).get("name");
                if (Objects.requireNonNull(name).equals(str)) {
                    data.remove(i);
                    break;
                }
            }
            FileUtil.writeFile(path, new Gson().toJson(data));
        }
    }

    private void setupCustomToolbar() {
        binding.toolbar.setTitle(R.string.androidmanifest_manager);
        binding.toolbar.setNavigationOnClickListener(view -> onBackPressed());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "Show Manifest Source").setIcon(getDrawable(R.drawable.ic_code_24)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        String title = menuItem.getTitle().toString();
        if (title.equals("Show Manifest Source")) {
            showQuickManifestSourceDialog();
        } else {
            return false;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void showQuickManifestSourceDialog() {
        ProgressMsgBoxBinding loadingDialogBinding = ProgressMsgBoxBinding.inflate(getLayoutInflater());
        loadingDialogBinding.tvProgress.setText(R.string.generating_source_code);
        var loadingDialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.please_wait)
                .setCancelable(false)
                .setView(loadingDialogBinding.getRoot())
                .create();
        loadingDialog.show();

        new Thread(() -> {
            final String source = new yq(getApplicationContext(), sc_id).getFileSrc("AndroidManifest.xml", jC.b(sc_id), jC.a(sc_id), jC.c(sc_id));

            var dialogBuilder = new MaterialAlertDialogBuilder(this)
                    .setTitle("AndroidManifest.xml")
                    .setPositiveButton(R.string.common_word_dismiss, null);

            runOnUiThread(() -> {
                if (isFinishing()) return;
                loadingDialog.dismiss();

                CodeEditor editor = new CodeEditor(this);
                editor.setTypefaceText(Typeface.MONOSPACE);
                editor.setEditable(false);
                editor.setEditorLanguage(CodeEditorLanguages.loadTextMateLanguage(CodeEditorLanguages.SCOPE_NAME_XML));
                editor.setTextSize(14);
                editor.setText(!source.equals("") ? source : "Failed to generate source.");
                editor.getComponent(Magnifier.class).setWithinEditorForcibly(true);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    Configuration configuration = getResources().getConfiguration();
                    boolean isDarkTheme = isDarkTheme = configuration.isNightModeActive();
                    if (isDarkTheme) {
                        editor.setColorScheme(CodeEditorColorSchemes.loadTextMateColorScheme(CodeEditorColorSchemes.THEME_DRACULA));
                    } else {
                        editor.setColorScheme(CodeEditorColorSchemes.loadTextMateColorScheme(CodeEditorColorSchemes.THEME_GITHUB));
                    }
                } else {
                    editor.setColorScheme(CodeEditorColorSchemes.loadTextMateColorScheme(CodeEditorColorSchemes.THEME_GITHUB));
                }

                AlertDialog dialog = dialogBuilder.create();
                dialog.setView(editor,
                        (int) getDip(24),
                        (int) getDip(20),
                        (int) getDip(24),
                        (int) getDip(0));
                dialog.show();
            });
        }).start();

    }

    private void makeup(LibraryItemView parent, int icon, String title, String description) {
        parent.enabled.setVisibility(View.GONE);
        parent.icon.setImageResource(icon);
        parent.title.setText(title);
        parent.description.setText(description);
    }

    private class ListAdapter extends BaseAdapter {
        private final ArrayList<HashMap<String, Object>> _data;

        public ListAdapter(ArrayList<HashMap<String, Object>> data) {
            _data = data;
        }

        @Override
        public int getCount() {
            return _data.size();
        }

        @Override
        public HashMap<String, Object> getItem(int position) {
            return _data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            CustomAttributeView attributeView = new CustomAttributeView(parent.getContext());

            attributeView.icon.setVisibility(View.GONE);
            attributeView.text.setText((String) list_map.get(position).get("act_name"));
            attributeView.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), AndroidManifestInjectionDetails.class);
                intent.putExtra("sc_id", sc_id);
                intent.putExtra("file_name", (String) _data.get(position).get("act_name"));
                intent.putExtra("type", "activity");
                startActivity(intent);
            });
            attributeView.setOnLongClickListener(v -> {
                {
                    aB dialog = new aB(AndroidManifestInjection.this);
                    dialog.a(R.drawable.icon_delete);
                    dialog.b(Helper.getResString(R.string.delete_custom_activity_dialog_title));
                    dialog.a(Helper.getResString(R.string.delete_custom_activity_dialog_message).replace("%1$s", (String) _data.get(position).get("act_name")));

                    dialog.b(Helper.getResString(R.string.common_word_delete), v1 -> {
                        deleteActivity(position);
                        dialog.dismiss();
                    });
                    dialog.a(Helper.getResString(R.string.common_word_cancel), Helper.getDialogDismissListener(dialog));
                    dialog.show();
                }
                return true;
            });

            return attributeView;
        }
    }
}