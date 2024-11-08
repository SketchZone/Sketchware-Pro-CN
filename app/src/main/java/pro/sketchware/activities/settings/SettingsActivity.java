package pro.sketchware.activities.settings;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.fragment.app.Fragment;

import com.besome.sketch.lib.base.BaseAppCompatActivity;

import pro.sketchware.databinding.ActivitySettingsBinding;
import pro.sketchware.fragments.settings.appearance.SettingsAppearanceFragment;
import pro.sketchware.fragments.settings.events.EventsManagerFragment;
import pro.sketchware.fragments.settings.language.SettingsLanguageFragment;
import pro.sketchware.fragments.settings.selector.block.BlockSelectorManagerFragment;

public class SettingsActivity extends BaseAppCompatActivity {

    private ActivitySettingsBinding binding;

    public static final String FRAGMENT_TAG_EXTRA = "fragment_tag";
    public static final String SETTINGS_APPEARANCE_FRAGMENT = "settings_appearance";
    public static final String EVENTS_MANAGER_FRAGMENT = "events_manager";
    public static final String SETTINGS_LANGUAGE_FRAGMENT = "settings_language";
    public static final String BLOCK_SELECTOR_MANAGER_FRAGMENT = "block_selector_manager";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String fragmentTag = getIntent().getStringExtra(FRAGMENT_TAG_EXTRA);
        Fragment fragment;
        switch (fragmentTag) {
            case SETTINGS_APPEARANCE_FRAGMENT:
                fragment = new SettingsAppearanceFragment();
                break;
            case SETTINGS_LANGUAGE_FRAGMENT:
                fragment = new SettingsLanguageFragment();
                break;
            case EVENTS_MANAGER_FRAGMENT:
                fragment = new EventsManagerFragment();
                break;
            case BLOCK_SELECTOR_MANAGER_FRAGMENT:
                fragment = new BlockSelectorManagerFragment();
                break;
            default:
                throw new IllegalArgumentException("Unknown fragment tag: " + fragmentTag);
        }

        openFragment(fragment);
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
            .replace(binding.settingsFragmentContainer.getId(), fragment)
            .commit();
    }
}