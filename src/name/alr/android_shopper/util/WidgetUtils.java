package name.alr.android_shopper.util;

import android.text.Editable;
import android.widget.EditText;

/**
 * @author alopezruiz@gmail.com (Alejandro Lopez Ruiz)
 */
public class WidgetUtils {

    public static String getTrimmedString(EditText editText) {
        return getTrimmedString(editText.getText());
    }

    public static String getTrimmedString(Editable editable) {
        return editable.toString().trim();
    }

}
