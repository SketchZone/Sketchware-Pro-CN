package mod.hilal.saif.activities.tools;

import static mod.SketchwareUtil.dpToPx;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.besome.sketch.editor.manage.library.LibraryItemView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sketchware.remod.R;
import com.sketchware.remod.databinding.BlocksManagerBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import a.a.a.Zx;
import a.a.a.aB;
import mod.SketchwareUtil;
import mod.agus.jcoderz.lib.FileUtil;
import mod.hey.studios.editor.manage.block.v2.BlockLoader;
import mod.hey.studios.util.Helper;
import mod.hilal.saif.lib.PCP;

public class BlocksManager extends AppCompatActivity {

    private ArrayList<HashMap<String, Object>> all_blocks_list = new ArrayList<>();
    private String blocks_dir = "";
    private String pallet_dir = "";
    private ArrayList<HashMap<String, Object>> pallet_listmap = new ArrayList<>();

    private BlocksManagerBinding binding;
    private LibraryItemView recycle_sub;

    @Override
    public void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        binding = BlocksManagerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initialize();
        setupViews();
        initializeLogic();
    }

    private void setupViews() {
        ViewGroup base = (ViewGroup) binding.listPallete.getParent();
        LinearLayout newLayout = newLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0);
        newLayout.setBackgroundColor(Color.parseColor("#00000000"));
        newLayout.setPadding(
                (int) SketchwareUtil.getDip(8),
                (int) SketchwareUtil.getDip(8),
                (int) SketchwareUtil.getDip(8),
                (int) SketchwareUtil.getDip(8)
        );
        newLayout.setFocusable(false);
        newLayout.setGravity(16);
        newLayout.addView(newText(getString(R.string.palettes), 16.0f, false, 0xff888888,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0));
        base.addView(newLayout, 1);
        recycle_sub = new LibraryItemView(this);
        makeup(recycle_sub, R.drawable.icon_delete_active, getString(R.string.common_word_recycle_bin), getString(R.string.blocks) + (long) (_getN(-1)));
        base.addView(recycle_sub, 1);
    }

    private void makeup(LibraryItemView parent, int iconResourceId, String title, String description) {
        parent.enabled.setVisibility(View.GONE);
        parent.icon.setImageResource(iconResourceId);
        parent.title.setText(title);
        parent.description.setText(description);
    }

    private TextView newText(String str, float size, boolean is, int color, int width, int length, float weight) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(width, length, weight));
        textView.setPadding(
                (int) SketchwareUtil.getDip(4),
                (int) SketchwareUtil.getDip(4),
                (int) SketchwareUtil.getDip(4),
                (int) SketchwareUtil.getDip(4)
        );
        textView.setTextColor(color);
        textView.setText(str);
        textView.setTextSize(size);
        if (is) {
            textView.setTypeface(Typeface.DEFAULT_BOLD);
        }
        return textView;
    }

    private LinearLayout newLayout(int width, int height, float weight) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(width, height, weight));
        linearLayout.setPadding(
                (int) SketchwareUtil.getDip(4),
                (int) SketchwareUtil.getDip(4),
                (int) SketchwareUtil.getDip(4),
                (int) SketchwareUtil.getDip(4)
        );
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.WHITE);
        linearLayout.setBackground(new RippleDrawable(new ColorStateList(new int[][]{new int[0]}, new int[]{Color.parseColor("#64B5F6")}), gradientDrawable, null));
        linearLayout.setClickable(true);
        linearLayout.setFocusable(true);
        return linearLayout;
    }

    @Override
    public void onStop() {
        super.onStop();

        BlockLoader.refresh();
    }

    private void initialize() {

        binding.layoutMainLogo.setVisibility(View.GONE);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(R.string.block_manager);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        binding.toolbar.setNavigationOnClickListener(Helper.getBackPressedClickListener(this));
        binding.fab.setOnClickListener(v -> showPaletteDialog(false, null, null, null, null));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, R.string.common_word_settings).setIcon(R.drawable.ic_settings_48).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 0) {
            aB dialog = new aB(this);
            dialog.a(R.drawable.ic_folder_48dp);
            dialog.b(getString(R.string.block_configuration));
            LinearLayout.LayoutParams defaultParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            LinearLayout customView = new LinearLayout(this);
            customView.setLayoutParams(defaultParams);
            customView.setOrientation(LinearLayout.VERTICAL);
            TextInputLayout tilPalettesPath = new TextInputLayout(this);
            tilPalettesPath.setLayoutParams(defaultParams);
            tilPalettesPath.setOrientation(LinearLayout.VERTICAL);
            tilPalettesPath.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            tilPalettesPath.setHint(R.string.json_file_with_palettes);
            customView.addView(tilPalettesPath);
            EditText palettesPath = new EditText(this);
            palettesPath.setLayoutParams(defaultParams);
            palettesPath.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
            palettesPath.setTextSize(14);
            palettesPath.setText(pallet_dir.replace(FileUtil.getExternalStorageDir(), ""));
            tilPalettesPath.addView(palettesPath);
            TextInputLayout tilBlocksPath = new TextInputLayout(this);
            tilBlocksPath.setLayoutParams(defaultParams);
            tilBlocksPath.setOrientation(LinearLayout.VERTICAL);
            tilBlocksPath.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            tilBlocksPath.setHint(R.string.json_file_with_blocks);
            customView.addView(tilBlocksPath);
            EditText blocksPath = new EditText(this);
            blocksPath.setLayoutParams(defaultParams);
            blocksPath.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
            blocksPath.setTextSize(14);
            blocksPath.setText(blocks_dir.replace(FileUtil.getExternalStorageDir(), ""));
            tilBlocksPath.addView(blocksPath);
            dialog.a(customView);
            dialog.b(Helper.getResString(R.string.common_word_save), (d, which) -> {
                ConfigActivity.setSetting(ConfigActivity.SETTING_BLOCKMANAGER_DIRECTORY_PALETTE_FILE_PATH,
                        palettesPath.getText().toString());
                ConfigActivity.setSetting(ConfigActivity.SETTING_BLOCKMANAGER_DIRECTORY_BLOCK_FILE_PATH,
                        blocksPath.getText().toString());

                _readSettings();
                _refresh_list();
                d.dismiss();
            });
            dialog.a(Helper.getResString(R.string.common_word_cancel), (d, which) -> Helper.getDialogDismissListener(d));
            dialog.configureDefaultButton(getString(R.string.common_word_defaults), (d, which) -> {
                ConfigActivity.setSetting(ConfigActivity.SETTING_BLOCKMANAGER_DIRECTORY_PALETTE_FILE_PATH,
                        ConfigActivity.getDefaultValue(ConfigActivity.SETTING_BLOCKMANAGER_DIRECTORY_PALETTE_FILE_PATH));
                ConfigActivity.setSetting(ConfigActivity.SETTING_BLOCKMANAGER_DIRECTORY_BLOCK_FILE_PATH,
                        ConfigActivity.getDefaultValue(ConfigActivity.SETTING_BLOCKMANAGER_DIRECTORY_BLOCK_FILE_PATH));

                _readSettings();
                _refresh_list();
                d.dismiss();
            });
            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeLogic() {
        _readSettings();
        _refresh_list();
        _recycleBin(recycle_sub);
    }

    @Override
    public void onResume() {
        super.onResume();

        _readSettings();
        _refresh_list();
    }

    private void _a(final View _view) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setColor(Color.parseColor("#ffffff"));
        RippleDrawable rippleDrawable = new RippleDrawable(new ColorStateList(new int[][]{new int[0]}, new int[]{Color.parseColor("#20008DCD")}), gradientDrawable, null);
        if (Build.VERSION.SDK_INT >= 21) {
            _view.setBackground(rippleDrawable);
            _view.setClickable(true);
            _view.setFocusable(true);
        }
    }

    private void _readSettings() {
        pallet_dir = FileUtil.getExternalStorageDir() + ConfigActivity.getStringSettingValueOrSetAndGet(ConfigActivity.SETTING_BLOCKMANAGER_DIRECTORY_PALETTE_FILE_PATH,
                (String) ConfigActivity.getDefaultValue(ConfigActivity.SETTING_BLOCKMANAGER_DIRECTORY_PALETTE_FILE_PATH));
        blocks_dir = FileUtil.getExternalStorageDir() + ConfigActivity.getStringSettingValueOrSetAndGet(ConfigActivity.SETTING_BLOCKMANAGER_DIRECTORY_BLOCK_FILE_PATH,
                (String) ConfigActivity.getDefaultValue(ConfigActivity.SETTING_BLOCKMANAGER_DIRECTORY_BLOCK_FILE_PATH));

        if (FileUtil.isExistFile(blocks_dir)) {
            try {
                all_blocks_list = new Gson().fromJson(FileUtil.readFile(blocks_dir), Helper.TYPE_MAP_LIST);

                if (all_blocks_list != null) {
                    return;
                }
                // fall-through to shared handler
            } catch (JsonParseException e) {
                // fall-through to shared handler
            }

            SketchwareUtil.showFailedToParseJsonDialog(this, new File(blocks_dir), "Custom Blocks", v -> _readSettings());
        }
    }

    private void _refresh_list() {
        parsePaletteJson:
        {
            String paletteJsonContent;
            if (FileUtil.isExistFile(pallet_dir) && !(paletteJsonContent = FileUtil.readFile(pallet_dir)).equals("")) {
                try {
                    pallet_listmap = new Gson().fromJson(paletteJsonContent, Helper.TYPE_MAP_LIST);

                    if (pallet_listmap != null) {
                        break parsePaletteJson;
                    }
                    // fall-through to shared handler
                } catch (JsonParseException e) {
                    // fall-through to shared handler
                }

                SketchwareUtil.showFailedToParseJsonDialog(this, new File(pallet_dir), "Custom Block Palettes", v -> _refresh_list());
            }
            pallet_listmap = new ArrayList<>();
        }

        Parcelable savedState = binding.listPallete.onSaveInstanceState();
        binding.listPallete.setAdapter(new PaletteAdapter(pallet_listmap));
        ((BaseAdapter) binding.listPallete.getAdapter()).notifyDataSetChanged();
        binding.listPallete.onRestoreInstanceState(savedState);
    }

    private double _getN(final double _p) {
        int n = 0;
        for (int i = 0; i < all_blocks_list.size(); i++) {
            if (all_blocks_list.get(i).get("palette").toString().equals(String.valueOf((long) (_p)))) {
                n++;
            }
        }
        return (n);
    }

    private void _MoveUp(final double _p) {
        if (_p > 0) {
            Collections.swap(pallet_listmap, (int) (_p), (int) (_p + -1));

            Parcelable savedState = binding.listPallete.onSaveInstanceState();
            FileUtil.writeFile(pallet_dir, new Gson().toJson(pallet_listmap));
            _swapRelatedBlocks(_p + 9, _p + 8);
            _readSettings();
            _refresh_list();
            binding.listPallete.onRestoreInstanceState(savedState);
        }
    }

    private void _recycleBin(final View _v) {
        _a(_v);
        recycle_sub.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), BlocksManagerDetailsActivity.class);
            intent.putExtra("position", "-1");
            intent.putExtra("dirB", blocks_dir);
            intent.putExtra("dirP", pallet_dir);
            startActivity(intent);
        });
        recycle_sub.setOnLongClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.common_word_recycle_bin)
                    .setMessage("Are you sure you want to empty the recycle bin? " +
                            "Blocks inside will be deleted PERMANENTLY, you CANNOT recover them!")
                    .setPositiveButton(R.string.common_word_empty, (dialog, which) -> _emptyRecyclebin())
                    .setNegativeButton(R.string.common_word_cancel, null)
                    .show();
            return true;
        });
    }

    private void _moveDown(final double _p) {
        if (_p < (pallet_listmap.size() - 1)) {
            Collections.swap(pallet_listmap, (int) (_p), (int) (_p + 1));
            {
                Parcelable savedState = binding.listPallete.onSaveInstanceState();
                FileUtil.writeFile(pallet_dir, new Gson().toJson(pallet_listmap));
                _swapRelatedBlocks(_p + 9, _p + 10);
                _readSettings();
                _refresh_list();
                binding.listPallete.onRestoreInstanceState(savedState);
            }
        }
    }

    private void _removeRelatedBlocks(final double _p) {
        List<Map<String, Object>> newBlocks = new LinkedList<>();
        for (int i = 0; i < all_blocks_list.size(); i++) {
            if (!(Double.parseDouble(all_blocks_list.get(i).get("palette").toString()) == _p)) {
                if (Double.parseDouble(all_blocks_list.get(i).get("palette").toString()) > _p) {
                    HashMap<String, Object> m = all_blocks_list.get(i);
                    m.put("palette", String.valueOf((long) (Double.parseDouble(all_blocks_list.get(i).get("palette").toString()) - 1)));
                    newBlocks.add(m);
                } else {
                    newBlocks.add(all_blocks_list.get(i));
                }
            }
        }
        FileUtil.writeFile(blocks_dir, new Gson().toJson(newBlocks));
        _readSettings();
        _refresh_list();
    }

    private void _swapRelatedBlocks(final double _f, final double _s) {
        for (int i = 0; i < all_blocks_list.size(); i++) {
            if (Double.parseDouble(all_blocks_list.get(i).get("palette").toString()) == _f) {
                all_blocks_list.get(i).put("palette", "123456789");
            }
            if (Double.parseDouble(all_blocks_list.get(i).get("palette").toString()) == _s) {
                all_blocks_list.get(i).put("palette", String.valueOf((long) (_f)));
            }
        }

        for (int i = 0; i < all_blocks_list.size(); i++) {
            if (Double.parseDouble(all_blocks_list.get(i).get("palette").toString()) == 123456789) {
                all_blocks_list.get(i).put("palette", String.valueOf((long) (_s)));
            }
        }
        FileUtil.writeFile(blocks_dir, new Gson().toJson(all_blocks_list));
        _readSettings();
        _refresh_list();
    }

    private void _insertBlocksAt(final double _p) {
        for (int i = 0; i < all_blocks_list.size(); i++) {
            if ((Double.parseDouble(all_blocks_list.get(i).get("palette").toString()) > _p) || (Double.parseDouble(all_blocks_list.get(i).get("palette").toString()) == _p)) {
                all_blocks_list.get(i).put("palette", String.valueOf((long) (Double.parseDouble(all_blocks_list.get(i).get("palette").toString()) + 1)));
            }
        }
        FileUtil.writeFile(blocks_dir, new Gson().toJson(all_blocks_list));
        _readSettings();
        _refresh_list();
    }

    private void _moveRelatedBlocksToRecycleBin(final double _p) {
        for (int i = 0; i < all_blocks_list.size(); i++) {
            if (Double.parseDouble(all_blocks_list.get(i).get("palette").toString()) == _p) {
                all_blocks_list.get(i).put("palette", "-1");
            }
        }
        FileUtil.writeFile(blocks_dir, new Gson().toJson(all_blocks_list));
        _readSettings();
        _refresh_list();
    }

    private void _emptyRecyclebin() {
        List<Map<String, Object>> newBlocks = new LinkedList<>();
        for (int i = 0; i < all_blocks_list.size(); i++) {
            if (!(Double.parseDouble(all_blocks_list.get(i).get("palette").toString()) == -1)) {
                newBlocks.add(all_blocks_list.get(i));
            }
        }
        FileUtil.writeFile(blocks_dir, new Gson().toJson(newBlocks));
        _readSettings();
        _refresh_list();
    }

    private View.OnClickListener getSharedPaletteColorPickerShower(MaterialAlertDialogBuilder dialog, EditText storePickedResultIn) {
        return v -> {
            AlertDialog alertDialog = dialog.create();
            LayoutInflater inf = getLayoutInflater();
            final View a = inf.inflate(R.layout.color_picker, null);
            final Zx zx = new Zx(a, this, 0, true, false);
            zx.a(new PCP(this, storePickedResultIn, alertDialog));
            zx.setAnimationStyle(R.anim.abc_fade_in);
            zx.showAtLocation(a, Gravity.CENTER, 0, 0);
            alertDialog.dismiss();
        };
    }

    private void showPaletteDialog(boolean isEditing, Integer oldPosition, String oldName, String oldColor, Integer insertAtPosition) {
        aB dialog = new aB(this);
        dialog.a(R.drawable.positive_96);
        dialog.b(!isEditing ? getString(R.string.create_a_new_palette) : getString(R.string.edit_palette));

        LinearLayout customView = new LinearLayout(this);
        customView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        customView.setOrientation(LinearLayout.VERTICAL);

        TextInputLayout name = new TextInputLayout(this);
        name.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        name.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        name.setOrientation(LinearLayout.VERTICAL);
        name.setHint("Name");
        customView.addView(name);

        EditText nameEditText = new EditText(this);
        nameEditText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        nameEditText.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        nameEditText.setTextColor(0xff000000);
        nameEditText.setHintTextColor(0xff607d8b);
        nameEditText.setTextSize(14);
        if (isEditing) {
            nameEditText.setText(oldName);
        }
        name.addView(nameEditText);

        LinearLayout colorContainer = new LinearLayout(this);
        colorContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        colorContainer.setGravity(Gravity.CENTER | Gravity.LEFT);
        customView.addView(colorContainer);

        TextInputLayout color = new TextInputLayout(this);
        color.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        color.setOrientation(LinearLayout.VERTICAL);
        color.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        color.setHint("Color");
        colorContainer.addView(color);

        EditText colorEditText = new EditText(this);
        colorEditText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        colorEditText.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        colorEditText.setTextColor(0xff000000);
        colorEditText.setHintTextColor(0xff607d8b);
        colorEditText.setTextSize(14);
        if (isEditing) {
            colorEditText.setText(oldColor);
        }
        color.addView(colorEditText);

        ImageView openColorPalette = new ImageView(this);
        openColorPalette.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(50), dpToPx(28)));
        openColorPalette.setFocusable(false);
        openColorPalette.setScaleType(ImageView.ScaleType.FIT_CENTER);
        openColorPalette.setImageResource(R.drawable.color_palette_48);
        colorContainer.addView(openColorPalette);

        dialog.a(customView);
        openColorPalette.setOnClickListener(getSharedPaletteColorPickerShower(dialog, colorEditText));

        dialog.b(Helper.getResString(R.string.common_word_save), (d, which) -> {
            try {
                String nameInput = nameEditText.getText().toString();
                String colorInput = colorEditText.getText().toString();
                Color.parseColor(colorInput);

                if (!isEditing) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("name", nameInput);
                    map.put("color", colorInput);

                    if (insertAtPosition == null) {
                        pallet_listmap.add(map);
                        FileUtil.writeFile(pallet_dir, new Gson().toJson(pallet_listmap));
                        _readSettings();
                        _refresh_list();
                    } else {
                        pallet_listmap.add(insertAtPosition, map);
                        FileUtil.writeFile(pallet_dir, new Gson().toJson(pallet_listmap));
                        _readSettings();
                        _refresh_list();
                        _insertBlocksAt(insertAtPosition + 9);
                    }
                } else {
                    pallet_listmap.get(oldPosition).put("name", nameInput);
                    pallet_listmap.get(oldPosition).put("color", colorInput);
                    FileUtil.writeFile(pallet_dir, new Gson().toJson(pallet_listmap));
                    _readSettings();
                    _refresh_list();
                }
                d.dismiss();
            } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                color.setError("Malformed hexadecimal color");
                color.requestFocus();
            }
        });
        dialog.a(Helper.getResString(R.string.common_word_cancel), (d, which) -> Helper.getDialogDismissListener(d));
        dialog.show();
    }

    public class PaletteAdapter extends BaseAdapter {

        private final ArrayList<HashMap<String, Object>> palettes;

        public PaletteAdapter(ArrayList<HashMap<String, Object>> palettes) {
            this.palettes = palettes;
        }

        @Override
        public int getCount() {
            return palettes.size();
        }

        @Override
        public HashMap<String, Object> getItem(int position) {
            return palettes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater _inflater = getLayoutInflater();
            if (convertView == null) {
                convertView = _inflater.inflate(R.layout.pallet_customview, parent, false);
            }

            final LinearLayout background = convertView.findViewById(R.id.background);
            final LinearLayout color = convertView.findViewById(R.id.color);
            final TextView title = convertView.findViewById(R.id.title);
            final TextView sub = convertView.findViewById(R.id.sub);

            title.setText(pallet_listmap.get(position).get("name").toString());
            sub.setText(getString(R.string.blocks) + (long) (_getN(position + 9)));
            makeup(recycle_sub, 0x7f07043e, getString(R.string.activity_events),
                    getString(R.string.blocks) + (long) (_getN(-1)));

            int backgroundColor;
            String paletteColorValue = (String) palettes.get(position).get("color");
            try {
                backgroundColor = Color.parseColor(paletteColorValue);
            } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                SketchwareUtil.toastError("Invalid background color '" + paletteColorValue + "' in Palette #" + (position + 1));
                backgroundColor = Color.WHITE;
            }
            color.setBackgroundColor(backgroundColor);

            _a(background);
            background.setOnLongClickListener(v -> {

                PopupMenu popup = new PopupMenu(BlocksManager.this, background);
                Menu menu = popup.getMenu();
                if (position != 0) menu.add(Menu.NONE, 1, Menu.NONE, R.string.move_up);
                if (position != getCount() - 1)
                    menu.add(Menu.NONE, 2, Menu.NONE, R.string.move_down);
                menu.add(Menu.NONE, 3, Menu.NONE, R.string.common_word_edit);
                menu.add(Menu.NONE, 4, Menu.NONE, R.string.common_word_delete);
                menu.add(Menu.NONE, 5, Menu.NONE, R.string.insert);
                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case 3:
                            showPaletteDialog(true, position,
                                    pallet_listmap.get(position).get("name").toString(),
                                    pallet_listmap.get(position).get("color").toString(), null);
                            break;

                        case 4:
                            new AlertDialog.Builder(BlocksManager.this)
                                    .setTitle(pallet_listmap.get(position).get("name").toString())
                                    .setMessage(R.string.remove_all_blocks)
                                    .setPositiveButton(R.string.remove_permanently, (dialog, which) -> {
                                        pallet_listmap.remove(position);
                                        FileUtil.writeFile(pallet_dir, new Gson().toJson(pallet_listmap));
                                        _removeRelatedBlocks(position + 9);
                                        _readSettings();
                                        _refresh_list();
                                    })
                                    .setNegativeButton(R.string.common_word_cancel, null)
                                    .setNeutralButton(R.string.move_to_recycle_bin, (dialog, which) -> {
                                        _moveRelatedBlocksToRecycleBin(position + 9);
                                        pallet_listmap.remove(position);
                                        FileUtil.writeFile(pallet_dir, new Gson().toJson(pallet_listmap));
                                        _removeRelatedBlocks(position + 9);
                                        _readSettings();
                                        _refresh_list();
                                    }).show();
                            break;

                        case 1:
                            _MoveUp(position);
                            break;

                        case 2:
                            _moveDown(position);
                            break;

                        case 5:
                            showPaletteDialog(false, null, null, null, position);
                            break;

                        default:
                    }
                    return true;
                });
                popup.show();

                return true;
            });

            background.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), BlocksManagerDetailsActivity.class);
                intent.putExtra("position", String.valueOf((long) (position + 9)));
                intent.putExtra("dirB", blocks_dir);
                intent.putExtra("dirP", pallet_dir);
                startActivity(intent);
            });

            return convertView;
        }
    }
}
