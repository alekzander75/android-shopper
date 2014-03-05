package name.alr.android_shopper;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author alopezruiz@gmail.com (Alejandro Lopez Ruiz)
 */
public class ShopperPreferenceActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_screen);
    }

}
