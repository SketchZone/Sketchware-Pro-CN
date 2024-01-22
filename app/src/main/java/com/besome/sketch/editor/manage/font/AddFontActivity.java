package com.besome.sketch.editor.manage.font;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.besome.sketch.beans.ProjectResourceBean;
import com.besome.sketch.lib.base.BaseDialogActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sketchware.remod.R;

import java.util.ArrayList;

import a.a.a.Np;
import a.a.a.WB;
import a.a.a.bB;
import a.a.a.mB;
import a.a.a.uq;
import a.a.a.yy;
import mod.SketchwareUtil;
import mod.agus.jcoderz.lib.FileUtil;
import mod.hey.studios.util.Helper;
import mod.jbk.util.LogUtil;

public class AddFontActivity extends BaseDialogActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_FONT_PICKER = 229;

    private TextView fontPreview;
    private CheckBox addOrAddedToCollection;
    private Uri fontUri = null;
    private boolean validFontPicked;
    private String sc_id;
    private TextInputEditText inputFontName;
    private TextInputLayout inputLayoutFontName;
    private WB fontNameValidator;
    private ImageView selectFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        e(Helper.getResString(R.string.design_manager_font_title_add_font));
        d(Helper.getResString(R.string.common_word_save));
        b(Helper.getResString(R.string.common_word_cancel));
        setContentView(R.layout.manage_font_add);

        Intent intent = getIntent();
        sc_id = intent.getStringExtra("sc_id");
        addOrAddedToCollection = findViewById(R.id.add_to_collection_checkbox);
        selectFile = findViewById(R.id.select_file);
        fontPreview = findViewById(R.id.font_preview);
        inputFontName = findViewById(R.id.ed_input);
        inputLayoutFontName = findViewById(R.id.ti_input);
        fontNameValidator = new WB(this, inputLayoutFontName, uq.b, intent.getStringArrayListExtra("font_names"));
        fontPreview.setText(Helper.getResString(R.string.design_manager_font_description_look_like_this));
        selectFile.setOnClickListener(this);
        r.setOnClickListener(this);
        s.setOnClickListener(this);
        if (intent.getIntExtra("request_code", -1) == 272) {
            e(Helper.getResString(R.string.design_manager_font_title_edit_font));
            fontNameValidator = new WB(this, inputLayoutFontName, uq.b, new ArrayList<>());
            inputFontName.setText(((ProjectResourceBean) intent.getParcelableExtra("resource_bean")).resName);
            inputFontName.setEnabled(false);
            addOrAddedToCollection.setEnabled(false);
        }
    }

    private void saveFont() {
        if (isFontValid(fontNameValidator)) {
            String fontName = this.inputFontName.getText().toString();
            String pickedFontFilePath = fontUri.getPath();
            ProjectResourceBean resourceBean = new ProjectResourceBean(ProjectResourceBean.PROJECT_RES_TYPE_FILE, fontName, pickedFontFilePath);
            resourceBean.savedPos = 1;
            resourceBean.isNew = true;

            if (addOrAddedToCollection.isChecked()) {
                try {
                    Np.g().a(sc_id, resourceBean);
                } catch (Exception e) {
                    // Well, (parts of) the bytecode's lying, yy can be thrown.
                    //noinspection ConstantConditions
                    if (e instanceof yy) {
                        switch (e.getMessage()) {
                            case "duplicate_name" ->
                                    bB.b(this, Helper.getResString(R.string.collection_duplicated_name), Toast.LENGTH_LONG).show();
                            case "file_no_exist" ->
                                    bB.b(this, Helper.getResString(R.string.collection_no_exist_file), Toast.LENGTH_LONG).show();
                            case "fail_to_copy" ->
                                    bB.b(this, Helper.getResString(R.string.collection_failed_to_copy), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        throw e;
                    }
                }
            } else {
                Intent intent = new Intent();
                intent.putExtra("resource_bean", resourceBean);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_FONT_PICKER && resultCode == RESULT_OK) {
            Uri intentData = data.getData();

            String filenameExtension = FileUtil.getFileExtension(SketchwareUtil.getSafDocumentDisplayName(intentData).orElse(".ttf"));
            SketchwareUtil.copySafDocumentToTempFile(intentData, this, filenameExtension, tempFontFile -> {
                fontUri = Uri.fromFile(tempFontFile);
                try {
                    validFontPicked = true;
                    Typeface typeface = Typeface.createFromFile(tempFontFile);
                    if (typeface.equals(Typeface.DEFAULT)) {
                        SketchwareUtil.toastError(getString(R.string.warning_font_doesn_t_seem_to_be_valid));
                    }
                    fontPreview.setTypeface(typeface);
                    inputFontName.requestFocus();
                    fontPreview.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    validFontPicked = false;
                    fontPreview.setVisibility(View.GONE);
                    SketchwareUtil.toast(getString(R.string.couldn_t_load_font) + e.getMessage());
                    LogUtil.e("AddFontActivity", getString(R.string.failed_to_load_font), e);
                }
            }, e -> {
                SketchwareUtil.toastError(getString(R.string.error_while_loading_font) + e.getMessage());
                LogUtil.e("AddFontActivity", getString(R.string.failed_to_load_font), e);
            });
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.common_dialog_cancel_button) {
            finish();
        } else if (id == R.id.common_dialog_ok_button) {
            saveFont();
        } else if (id == R.id.select_file) {
            if (!mB.a()) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(Intent.createChooser(intent, Helper.getResString(R.string.common_word_choose)), REQUEST_CODE_FONT_PICKER);
            }
        }
    }

    private boolean isFontValid(WB wb) {
        if (!wb.b()) {
            return false;
        }
        if (validFontPicked && fontUri != null) {
            return true;
        }
        selectFile.startAnimation(AnimationUtils.loadAnimation(this, R.anim.ani_1));
        return false;
    }
}
