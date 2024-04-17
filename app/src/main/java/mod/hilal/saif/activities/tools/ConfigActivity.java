package mod.hilal.saif.activities.tools;

import static mod.SketchwareUtil.dpToPx;
import static mod.SketchwareUtil.getDip;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.android.annotations.NonNull;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sketchware.remod.R;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import mod.SketchwareUtil;
import mod.agus.jcoderz.lib.FileUtil;
import mod.hey.studios.util.Helper;
import mod.jbk.util.LogUtil;

public class ConfigActivity extends AppCompatActivity {

    public static final File SETTINGS_FILE = new File(FileUtil.getExternalStorageDir(), ".sketchware/data/settings.json");
    public static final String SETTING_ALWAYS_SHOW_BLOCKS = "always-show-blocks";
    public static final String SETTING_BACKUP_DIRECTORY = "backup-dir";
    public static final String SETTING_ROOT_AUTO_INSTALL_PROJECTS = "root-auto-install-projects";
    public static final String SETTING_ROOT_AUTO_OPEN_AFTER_INSTALLING = "root-auto-open-after-installing";
    public static final String SETTING_BACKUP_FILENAME = "backup-filename";
    public static final String SETTING_LEGACY_CODE_EDITOR = "legacy-ce";
    public static final String SETTING_SHOW_BUILT_IN_BLOCKS = "built-in-blocks";
    public static final String SETTING_SHOW_EVERY_SINGLE_BLOCK = "show-every-single-block";
    public static final String SETTING_USE_NEW_VERSION_CONTROL = "use-new-version-control";
    public static final String SETTING_USE_ASD_HIGHLIGHTER = "use-asd-highlighter";
    public static final String SETTING_SKIP_MAJOR_CHANGES_REMINDER = "skip-major-changes-reminder";
    public static final String SETTING_BLOCKMANAGER_DIRECTORY_PALETTE_FILE_PATH = "palletteDir";
    public static final String SETTING_BLOCKMANAGER_DIRECTORY_BLOCK_FILE_PATH = "blockDir";
    private LinearLayout root;
    private HashMap<String, Object> setting_map = new HashMap<>();

    public static String getBackupPath() {
        if (FileUtil.isExistFile(SETTINGS_FILE.getAbsolutePath())) {
            HashMap<String, Object> settings = readSettings();
            if (settings.containsKey(SETTING_BACKUP_DIRECTORY)) {
                Object value = settings.get(SETTING_BACKUP_DIRECTORY);
                if (value instanceof String) {
                    return (String) value;
                } else {
                    SketchwareUtil.toastError("Detected invalid preference "
                                    + "for backup directory. Restoring defaults",
                            Toast.LENGTH_LONG);
                    settings.remove(SETTING_BACKUP_DIRECTORY);
                    FileUtil.writeFile(SETTINGS_FILE.getAbsolutePath(), new Gson().toJson(settings));
                }
            }
        }
        return "/.sketchware/backups/";
    }

    public static String getStringSettingValueOrSetAndGet(String settingKey, String toReturnAndSetIfNotFound) {
        HashMap<String, Object> settings = readSettings();

        Object value = settings.get(settingKey);
        if (value instanceof String) {
            return (String) value;
        } else {
            settings.put(settingKey, toReturnAndSetIfNotFound);
            FileUtil.writeFile(SETTINGS_FILE.getAbsolutePath(), new Gson().toJson(settings));

            return toReturnAndSetIfNotFound;
        }
    }


    public static String getBackupFileName() {
        if (FileUtil.isExistFile(SETTINGS_FILE.getAbsolutePath())) {
            HashMap<String, Object> settings = new Gson().fromJson(FileUtil.readFile(SETTINGS_FILE.getAbsolutePath()), Helper.TYPE_MAP);
            if (settings.containsKey(SETTING_BACKUP_FILENAME)) {
                Object value = settings.get(SETTING_BACKUP_FILENAME);
                if (value instanceof String) {
                    return (String) value;
                } else {
                    SketchwareUtil.toastError("Detected invalid preference "
                                    + "for backup filename. Restoring defaults",
                            Toast.LENGTH_LONG);
                    settings.remove(SETTING_BACKUP_FILENAME);
                    FileUtil.writeFile(SETTINGS_FILE.getAbsolutePath(), new Gson().toJson(settings));
                }
            }
        }
        return "$projectName v$versionName ($pkgName, $versionCode) $time(yyyy-MM-dd'T'HHmmss)";
    }

    public static boolean isLegacyCeEnabled() {
        /* The legacy Code Editor is specifically opt-in */
        if (!FileUtil.isExistFile(SETTINGS_FILE.getAbsolutePath())) {
            return false;
        }

        HashMap<String, Object> settings = readSettings();
        if (settings.containsKey(SETTING_LEGACY_CODE_EDITOR)) {
            Object value = settings.get(SETTING_LEGACY_CODE_EDITOR);
            if (value instanceof Boolean) {
                return (Boolean) value;
            } else {
                SketchwareUtil.toastError("Detected invalid preference for legacy "
                                + " Code Editor. Restoring defaults",
                        Toast.LENGTH_LONG);
                settings.remove(SETTING_LEGACY_CODE_EDITOR);
                FileUtil.writeFile(SETTINGS_FILE.getAbsolutePath(), new Gson().toJson(settings));
            }
        }
        return false;
    }

    public static boolean isSettingEnabled(String keyName) {
        if (!FileUtil.isExistFile(SETTINGS_FILE.getAbsolutePath())) {
            return false;
        }

        HashMap<String, Object> settings = readSettings();
        if (settings.containsKey(keyName)) {
            Object value = settings.get(keyName);
            if (value instanceof Boolean) {
                return (Boolean) value;
            } else {
                SketchwareUtil.toastError("Detected invalid preference. Restoring defaults",
                        Toast.LENGTH_LONG);
                settings.remove(keyName);
                FileUtil.writeFile(SETTINGS_FILE.getAbsolutePath(), new Gson().toJson(settings));
            }
        }
        return false;
    }

    public static void setSetting(String key, Object value) {
        HashMap<String, Object> settings = readSettings();
        settings.put(key, value);
        FileUtil.writeFile(SETTINGS_FILE.getAbsolutePath(), new Gson().toJson(settings));
    }

    @NonNull
    private static HashMap<String, Object> readSettings() {
        HashMap<String, Object> settings;

        if (SETTINGS_FILE.exists()) {
            Exception toLog;

            try {
                settings = new Gson().fromJson(FileUtil.readFile(SETTINGS_FILE.getAbsolutePath()), Helper.TYPE_MAP);

                if (settings != null) {
                    return settings;
                }

                toLog = new NullPointerException("settings == null");
                // fall-through to shared error handler
            } catch (JsonParseException e) {
                toLog = e;
                // fall-through to shared error handler
            }

            SketchwareUtil.toastError("Couldn't parse Mod Settings! Restoring defaults.");
            LogUtil.e("ConfigActivity", "Failed to parse Mod Settings.", toLog);
        }
        settings = new HashMap<>();
        restoreDefaultSettings(settings);

        return settings;
    }

    private static void restoreDefaultSettings(HashMap<String, Object> settings) {
        settings.clear();

        List<String> keys = Arrays.asList(SETTING_ALWAYS_SHOW_BLOCKS,
                SETTING_BACKUP_DIRECTORY,
                SETTING_LEGACY_CODE_EDITOR,
                SETTING_ROOT_AUTO_INSTALL_PROJECTS,
                SETTING_ROOT_AUTO_OPEN_AFTER_INSTALLING,
                SETTING_SHOW_BUILT_IN_BLOCKS,
                SETTING_SHOW_EVERY_SINGLE_BLOCK,
                SETTING_USE_NEW_VERSION_CONTROL,
                SETTING_USE_ASD_HIGHLIGHTER,
                SETTING_BLOCKMANAGER_DIRECTORY_PALETTE_FILE_PATH,
                SETTING_BLOCKMANAGER_DIRECTORY_BLOCK_FILE_PATH);

        for (String key : keys) {
            settings.put(key, getDefaultValue(key));
        }
        FileUtil.writeFile(SETTINGS_FILE.getAbsolutePath(), new Gson().toJson(settings));
    }

    public static Object getDefaultValue(String key) {
        switch (key) {
            case SETTING_ALWAYS_SHOW_BLOCKS:
            case SETTING_LEGACY_CODE_EDITOR:
            case SETTING_ROOT_AUTO_INSTALL_PROJECTS:
            case SETTING_SHOW_BUILT_IN_BLOCKS:
            case SETTING_SHOW_EVERY_SINGLE_BLOCK:
            case SETTING_USE_NEW_VERSION_CONTROL:
            case SETTING_USE_ASD_HIGHLIGHTER:
                return false;

            case SETTING_BACKUP_DIRECTORY:
                return "/.sketchware/backups/";

            case SETTING_ROOT_AUTO_OPEN_AFTER_INSTALLING:
                return true;

            case SETTING_BLOCKMANAGER_DIRECTORY_PALETTE_FILE_PATH:
                return "/.sketchware/resources/block/My Block/palette.json";

            case SETTING_BLOCKMANAGER_DIRECTORY_BLOCK_FILE_PATH:
                return "/.sketchware/resources/block/My Block/block.json";

            default:
                throw new IllegalArgumentException("Unknown key '" + key + "'!");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FileUtil.isExistFile(SETTINGS_FILE.getAbsolutePath())) {
            setting_map = readSettings();
            if (!setting_map.containsKey(SETTING_SHOW_BUILT_IN_BLOCKS) || !setting_map.containsKey(SETTING_ALWAYS_SHOW_BLOCKS)) {
                restoreDefaultSettings();
            }
        } else {
            restoreDefaultSettings();
        }
        initialize();
    }

    @SuppressLint("SetTextI18n")
    private void initialize() {
        root = new LinearLayout(this);
        root.setBackgroundColor(ContextCompat.getColor(this,R.color.backgroundColor));
        root.setOrientation(LinearLayout.VERTICAL);

        ScrollView _scroll = new ScrollView(this);
        LinearLayout.LayoutParams _lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        _scroll.setLayoutParams(_lp);

        LinearLayout _base = new LinearLayout(this);
        _base.setBackgroundColor(ContextCompat.getColor(this,R.color.backgroundColor));
        _base.setOrientation(LinearLayout.VERTICAL);
        _base.setLayoutParams(_lp);

        View toolbar = getLayoutInflater().inflate(R.layout.toolbar, root, false);
        _base.addView(toolbar);
        _base.addView(_scroll);
        _scroll.addView(root);
        setContentView(_base);
        toolbar.findViewById(R.id.layout_main_logo).setVisibility(View.GONE);
        setSupportActionBar((Toolbar) toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.mod_settings);
        ((Toolbar) toolbar).setNavigationOnClickListener(Helper.getBackPressedClickListener(this));


        addSwitchPreference(getString(R.string.built_in_blocks),
                getString(R.string.may_slow_down_loading_blocks_in_logic_editor),
                SETTING_SHOW_BUILT_IN_BLOCKS,
                false);
        addSwitchPreference(getString(R.string.show_all_variable_blocks),
                getString(R.string.all_variable_blocks_will_be_visible),
                SETTING_ALWAYS_SHOW_BLOCKS,
                false);
        addSwitchPreference(getString(R.string.show_all_blocks_of_palettes),
                getString(R.string.every_single_available_block_will_be_shown),
                SETTING_SHOW_EVERY_SINGLE_BLOCK,
                false);
        addTextInputPreference(getString(R.string.backup_directory),
                getString(R.string.the_default_directory), v -> {
                    final LinearLayout container = new LinearLayout(this);
                    container.setPadding(
                            (int) getDip(20),
                            (int) getDip(8),
                            (int) getDip(20),
                            0);

                    final TextInputLayout tilBackupDirectory = new TextInputLayout(this);
                    tilBackupDirectory.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    tilBackupDirectory.setHint(R.string.backup_directory);
                    tilBackupDirectory.setHelperText(getString(R.string.directory_inside_internal_storage));
                    container.addView(tilBackupDirectory);

                    final EditText backupDirectory = new EditText(this);
                    backupDirectory.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT));
                    backupDirectory.setTextSize(14.0f);
                    backupDirectory.setText(getBackupPath());
                    tilBackupDirectory.addView(backupDirectory);

                    new AlertDialog.Builder(this)
                            .setTitle(R.string.backup_directory)
                            .setView(container)
                            .setPositiveButton(R.string.common_word_save, (dialogInterface, which) -> {
                                ConfigActivity.setSetting(SETTING_BACKUP_DIRECTORY, backupDirectory.getText().toString());
                                SketchwareUtil.toast("Saved");
                            })
                            .setNegativeButton(R.string.common_word_cancel, (dialogInterface, which) -> dialogInterface.dismiss())
                            .show();
                });
        addSwitchPreference(getString(R.string.use_legacy_code_editor),
                getString(R.string.enables_old_code_editor),
                SETTING_LEGACY_CODE_EDITOR,
                false);
        addSwitchPreference(getString(R.string.install_projects_with_root_access), getString(R.string.automatically_installs_project_apks),
                SETTING_ROOT_AUTO_INSTALL_PROJECTS, false, (buttonView, isChecked) -> {
            if (isChecked) {
                Shell.getShell(shell -> {
                    if (!shell.isRoot()) {
                        SketchwareUtil.toastError("Couldn't acquire root access");
                        buttonView.setChecked(false);
                    }
                });
            }
        });
        addSwitchPreference(getString(R.string.launch_projects_after_installing),
                getString(R.string.opens_projects_automatically),
                SETTING_ROOT_AUTO_OPEN_AFTER_INSTALLING,
                true);
        addSwitchPreference(getString(R.string.use_new_version_control),
                getString(R.string.enables_custom_version_code_and_name_for_projects),
                SETTING_USE_NEW_VERSION_CONTROL,
                false);
        addSwitchPreference(getString(R.string.enable_block_text_input_highlighting),
                getString(R.string.enables_syntax_highlighting_while_editing_blocks_text_parameters),
                SETTING_USE_ASD_HIGHLIGHTER,
                false);
        addTextInputPreference(getString(R.string.backup_filename_format),
                getString(R.string.default_is_projectname_v_versionname_pkgname_versioncode_time), v -> {
                    final LinearLayout container = new LinearLayout(this);
                    container.setPadding(
                            (int) getDip(20),
                            (int) getDip(8),
                            (int) getDip(20),
                            0);

                    final TextInputLayout tilBackupFormat = new TextInputLayout(this);
                    tilBackupFormat.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    tilBackupFormat.setHint(R.string.format);
                    tilBackupFormat.setHelperText("This defines how SWB backup files get named.\n" +
                            "Available variables:\n" +
                            " - $projectName - Project name\n" +
                            " - $versionCode - App version code\n" +
                            " - $versionName - App version name\n" +
                            " - $pkgName - App package name\n" +
                            " - $timeInMs - Time during backup in milliseconds\n" +
                            "\n" +
                            "Additionally, you can format your own time like this using Java's date formatter syntax:\n" +
                            "$time(yyyy-MM-dd'T'HHmmss)\n");
                    container.addView(tilBackupFormat);

                    final EditText backupFilename = new EditText(this);
                    backupFilename.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT));
                    backupFilename.setTextSize(14.0f);
                    backupFilename.setText(getBackupFileName());
                    tilBackupFormat.addView(backupFilename);

                    new AlertDialog.Builder(this)
                            .setTitle(R.string.backup_filename_format)
                            .setView(container)
                            .setNegativeButton(R.string.common_word_cancel, (dialogInterface, which) -> dialogInterface.dismiss())
                            .setPositiveButton(R.string.common_word_save, (dialogInterface, which) -> {
                                setting_map.put(SETTING_BACKUP_FILENAME, backupFilename.getText().toString());
                                FileUtil.writeFile(SETTINGS_FILE.getAbsolutePath(), new Gson().toJson(setting_map));
                                SketchwareUtil.toast("Saved");
                            })
                            .setNeutralButton(R.string.common_word_reset, (dialogInterface, which) -> {
                                setting_map.remove(SETTING_BACKUP_FILENAME);
                                FileUtil.writeFile(SETTINGS_FILE.getAbsolutePath(), new Gson().toJson(setting_map));
                                SketchwareUtil.toast("Reset to default complete.");
                            })
                            .show();
                });
    }

    private void applyDesign(View view) {
        Helper.applyRippleEffect(view, Color.parseColor("#dbedf5"), ContextCompat.getColor(this,R.color.backgroundColor));
        view.setClickable(true);
        view.setFocusable(true);
    }

    private void addSwitchPreference(String title, String subtitle, String keyName, boolean defaultValue) {
        addSwitchPreference(title, subtitle, keyName, defaultValue, null);
    }

    private void addSwitchPreference(String title, String subtitle, String keyName, boolean defaultValue, CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        LinearLayout preferenceRoot = new LinearLayout(this);
        preferenceRoot.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.0f
        ));
        preferenceRoot.setBackgroundColor(ContextCompat.getColor(this,R.color.backgroundColor));
        preferenceRoot.setOrientation(LinearLayout.HORIZONTAL);
        preferenceRoot.setPadding(
                dpToPx(4),
                dpToPx(4),
                dpToPx(4),
                dpToPx(4)
        );
        /* Android Studio complained about that inside the original XML */
        preferenceRoot.setBaselineAligned(false);
        root.addView(preferenceRoot);

        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(
                dpToPx(0),
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.0f
        ));
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setPadding(
                dpToPx(8),
                dpToPx(8),
                dpToPx(8),
                dpToPx(8)
        );
        preferenceRoot.addView(textContainer);

        TextView titleView = new TextView(this);
        titleView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        titleView.setPadding(
                dpToPx(0),
                dpToPx(0),
                dpToPx(0),
                dpToPx(4)
        );
        titleView.setText(title);
        titleView.setTextSize(16);
        textContainer.addView(titleView);

        TextView subtitleView = new TextView(this);
        subtitleView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        subtitleView.setText(subtitle);
        subtitleView.setTextSize(12);
        textContainer.addView(subtitleView);

        LinearLayout switchContainer = new LinearLayout(this);
        switchContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                0.0f
        ));
        switchContainer.setGravity(Gravity.CENTER);
        switchContainer.setOrientation(LinearLayout.VERTICAL);
        switchContainer.setPadding(
                dpToPx(8),
                dpToPx(8),
                dpToPx(8),
                dpToPx(8)
        );
        preferenceRoot.addView(switchContainer);

        MaterialSwitch switchView = new MaterialSwitch(this);
        switchView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        switchView.setPadding(
                dpToPx(8),
                dpToPx(8),
                dpToPx(8),
                dpToPx(8)
        );
        switchView.setTextSize(12);
        switchContainer.addView(switchView);

        preferenceRoot.setOnClickListener(v -> switchView.setChecked(!switchView.isChecked()));

        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ConfigActivity.setSetting(keyName, isChecked);
            if (onCheckedChangeListener != null) {
                onCheckedChangeListener.onCheckedChanged(buttonView, isChecked);
            }
        });

        if (setting_map.containsKey(keyName)) {
            Object value = setting_map.get(keyName);
            if (value == null) {
                /* Nulls aren't great */
                setting_map.remove(keyName);
            } else {
                if (value instanceof Boolean) {
                    switchView.setChecked((boolean) value);
                } else {
                    SketchwareUtil.toastError("Detected invalid value for preference \""
                            + title + "\". Restoring defaults");
                    setting_map.remove(keyName);
                    FileUtil.writeFile(SETTINGS_FILE.getAbsolutePath(), new Gson().toJson(setting_map));
                }
            }
        } else {
            setting_map.put(keyName, defaultValue);
            switchView.setChecked(defaultValue);
            FileUtil.writeFile(SETTINGS_FILE.getAbsolutePath(), new Gson().toJson(setting_map));
        }
        applyDesign(preferenceRoot);
    }

    private void addTextInputPreference(String title, String subtitle, View.OnClickListener listener) {
        LinearLayout preferenceRoot = new LinearLayout(this);
        LinearLayout.LayoutParams preferenceRootParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.0f
        );
        preferenceRootParams.bottomMargin = dpToPx(4);
        preferenceRoot.setLayoutParams(preferenceRootParams);
        preferenceRoot.setBackgroundColor(ContextCompat.getColor(this,R.color.backgroundColor));
        preferenceRoot.setOrientation(LinearLayout.HORIZONTAL);
        preferenceRoot.setPadding(
                dpToPx(4),
                dpToPx(4),
                dpToPx(4),
                dpToPx(4)
        );
        /* Android Studio complained about this in the original XML files */
        preferenceRoot.setBaselineAligned(false);
        root.addView(preferenceRoot);

        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(
                dpToPx(0),
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.0f
        ));
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setPadding(
                dpToPx(8),
                dpToPx(8),
                dpToPx(8),
                dpToPx(8)
        );
        preferenceRoot.addView(textContainer);

        TextView titleView = new TextView(this);
        titleView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        titleView.setText(title);
        titleView.setTextSize(16);
        textContainer.addView(titleView);

        TextView subtitleView = new TextView(this);
        subtitleView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        subtitleView.setText(subtitle);
        subtitleView.setTextSize(12);
        textContainer.addView(subtitleView);

        preferenceRoot.setOnClickListener(listener);
        applyDesign(preferenceRoot);
    }

    private void restoreDefaultSettings() {
        restoreDefaultSettings(setting_map);
    }
}
