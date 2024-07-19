package mod.trindadedev.settings.appearance;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.sketchware.remod.databinding.SettingsAppearanceActivityBinding;

public class AppearanceActivity extends AppCompatActivity {
    private SettingsAppearanceActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SettingsAppearanceActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        openFragment(new AppearanceFragment());
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(binding.fragmentContainer.getId(), fragment)
                .commit();
    }
}