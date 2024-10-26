package a.a.a;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.besome.sketch.beans.ProjectResourceBean;
import com.bumptech.glide.Glide;
import com.sketchware.remod.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import mod.hey.studios.util.Helper;

public class tx extends RelativeLayout implements View.OnClickListener {
    public String a;
    public String b;
    public String c;
    public boolean d;
    public TextView e;
    public TextView f;
    public ImageView g;
    public ImageView h;
    public RadioGroup i;
    public LinearLayout j;
    public View k;
    public View l;
    public int m;
    public Kw n;

    public tx(Context context, boolean z, String str, boolean z2) {
        super(context);
        d = false;
        a = str;
        a(context, z, z2);
    }

    public String getKey() {
        return b;
    }

    public String getValue() {
        return c;
    }

    @Override
    public void onClick(View view) {
        if (mB.a()) {
            return;
        }
        a();
    }

    public void setKey(String str) {
        b = str;
        int identifier = getResources().getIdentifier(str, "string", getContext().getPackageName());
        if (identifier > 0) {
            e.setText(xB.b().a(getResources(), identifier));
            if ("property_image".equals(b)) {
                m = R.drawable.ic_picture_48dp;
            } else if ("property_background_resource".equals(b)) {
                m = R.drawable.variation_48;
            }
            if (l.getVisibility() == VISIBLE) {
                ((ImageView) findViewById(R.id.img_icon)).setImageResource(m);
                ((TextView) findViewById(R.id.tv_title)).setText(xB.b().a(getContext(), identifier));
            } else {
                h.setImageResource(m);
            }
        }
    }

    public void setOnPropertyValueChangeListener(Kw kw) {
        n = kw;
    }

    public void setOrientationItem(int i) {
        if (i == 0) {
            k.setVisibility(GONE);
            l.setVisibility(VISIBLE);
            return;
        }
        k.setVisibility(VISIBLE);
        l.setVisibility(GONE);
    }

    public void setValue(String str) {
        Uri fromFile;
        if (str != null && !str.equalsIgnoreCase("NONE")) {
            c = str;
            f.setText(str);
            if (jC.d(a).h(str) == ProjectResourceBean.PROJECT_RES_TYPE_RESOURCE) {
                g.setImageResource(getContext().getResources().getIdentifier(str, "drawable", getContext().getPackageName()));
                return;
            } else if (str.equals("default_image")) {
                g.setImageResource(getContext().getResources().getIdentifier(str, "drawable", getContext().getPackageName()));
                return;
            } else {
                File file = new File(jC.d(a).f(str));
                if (file.exists()) {
                    if (Build.VERSION.SDK_INT >= 24) {
                        Context context = getContext();
                        fromFile = FileProvider.getUriForFile(context, getContext().getPackageName() + ".provider", file);
                    } else {
                        fromFile = Uri.fromFile(file);
                    }
                    Glide.with(getContext()).load(fromFile).signature(kC.n()).error(R.drawable.ic_remove_grey600_24dp).into(g);
                    return;
                }
                g.setImageResource(getContext().getResources().getIdentifier(str, "drawable", getContext().getPackageName()));
                return;
            }
        }
        c = str;
        f.setText("NONE");
        g.setImageDrawable(null);
        g.setBackgroundColor(Color.WHITE);
    }

    public final void a(Context context, boolean z, boolean z2) {
        wB.a(context, this, R.layout.property_resource_item);
        e = findViewById(R.id.tv_name);
        f = findViewById(R.id.tv_value);
        g = findViewById(R.id.view_image);
        h = findViewById(R.id.img_left_icon);
        k = findViewById(R.id.property_item);
        l = findViewById(R.id.property_menu_item);
        d = z2;
        if (z) {
            setOnClickListener(this);
            setSoundEffectsEnabled(true);
        }
    }

    public final void a() {
        LinearLayout a2;
        aB aBVar = new aB((Activity) getContext());
        aBVar.b(e.getText().toString());
        aBVar.a(m);
        View a3 = wB.a(getContext(), R.layout.property_popup_selector_color);
        ScrollView scrollView = a3.findViewById(R.id.scroll_view);
        i = a3.findViewById(R.id.rg);
        j = a3.findViewById(R.id.content);
        ArrayList<String> m = jC.d(a).m();
        if (xq.a(a) || xq.b(a)) {
            if (d) {
                m.add(0, "default_image");
            } else {
                m.add(0, "NONE");
            }
        }
        Iterator<String> it = m.iterator();
        RadioButton radioButton = null;
        while (it.hasNext()) {
            String next = it.next();
            RadioButton a4 = a(next);
            i.addView(a4);
            if (next.equals(c)) {
                a4.setChecked(true);
                radioButton = a4;
            }
            if (xq.a(a)) {
                if (next.equals("default_image")) {
                    a2 = a(next, true);
                } else {
                    a2 = a(next, false);
                }
            } else if (xq.b(a)) {
                if (next.equals("default_image")) {
                    a2 = a(next, true);
                } else {
                    a2 = a(next, false);
                }
            } else {
                a2 = a(next, true);
            }
            a2.setOnClickListener(v -> ((RadioButton) i.getChildAt(j.indexOfChild(v))).setChecked(true));
            j.addView(a2);
        }
        if (radioButton == null) {
            radioButton = (RadioButton) i.getChildAt(0);
            radioButton.setChecked(true);
        }
        aBVar.a(a3);
        aBVar.b(xB.b().a(getContext(), R.string.common_word_select), v -> {
            for (int i = 0; i < this.i.getChildCount(); i++) {
                RadioButton child = (RadioButton) this.i.getChildAt(i);
                if (child.isChecked()) {
                    setValue(child.getTag().toString());
                    if (n != null) {
                        n.a(b, c);
                    }
                    break;
                }
            }
            aBVar.dismiss();
        });
        aBVar.a(xB.b().a(getContext(), R.string.common_word_cancel), Helper.getDialogDismissListener(aBVar));
        RadioButton finalRadioButton = radioButton;
        aBVar.setOnShowListener(dialog -> scrollView.smoothScrollTo(0, (int) finalRadioButton.getY()));
        aBVar.show();
    }

    public final RadioButton a(String str) {
        RadioButton radioButton = new RadioButton(getContext());
        radioButton.setText("");
        radioButton.setTag(str);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                (int) (wB.a(getContext(), 1.0f) * 60.0f));
        radioButton.setGravity(Gravity.CENTER | Gravity.LEFT);
        radioButton.setLayoutParams(layoutParams);
        return radioButton;
    }

    public final LinearLayout a(String str, boolean z) {
        Uri fromFile;
        float a2 = wB.a(getContext(), 1.0f);
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (60.0f * a2)));
        linearLayout.setGravity(Gravity.CENTER | Gravity.LEFT);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        TextView textView = new TextView(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1.0f;
        layoutParams.rightMargin = (int) (8.0f * a2);
        textView.setLayoutParams(layoutParams);
        textView.setText(str);
        linearLayout.addView(textView);
        ImageView imageView = new ImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int i = (int) (a2 * 48.0f);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(i, i));
        if (!str.equalsIgnoreCase("NONE")) {
            if (z) {
                imageView.setImageResource(getContext().getResources().getIdentifier(str, "drawable", getContext().getPackageName()));
            } else {
                File file = new File(jC.d(a).f(str));
                if (file.exists()) {
                    if (Build.VERSION.SDK_INT >= 24) {
                        Context context = getContext();
                        fromFile = FileProvider.getUriForFile(context, getContext().getPackageName() + ".provider", file);
                    } else {
                        fromFile = Uri.fromFile(file);
                    }
                    Glide.with(getContext()).load(fromFile).signature(kC.n()).error(R.drawable.ic_remove_grey600_24dp).into(imageView);
                } else {
                    imageView.setImageResource(getContext().getResources().getIdentifier(str, "drawable", getContext().getPackageName()));
                }
            }
            imageView.setBackgroundResource(R.drawable.bg_outline);
        } else {
            imageView.setBackgroundResource(R.drawable.bg_outline);
        }
        linearLayout.addView(imageView);
        return linearLayout;
    }
}
