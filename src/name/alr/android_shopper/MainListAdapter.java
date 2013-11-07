package name.alr.android_shopper;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * @author alopezruiz@gmail.com (Alejandro Lopez Ruiz)
 */
public class MainListAdapter extends SimpleCursorAdapter {

    private final MainActivity mainActivity;

    public MainListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, MainActivity mainActivity) {
        super(context, layout, c, from, to);
        this.mainActivity = mainActivity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        ImageButton amountButton = (ImageButton) view.findViewById(R.id.mainListEntryAlterAmountButton);
        amountButton.setImageResource(this.mainActivity.isShowingAll() ? android.R.drawable.ic_input_add
                : android.R.drawable.ic_delete);

        int raiseButtonVisibility;
        int lowerButtonVisibility;

        if (!this.mainActivity.isShowingAll()) {
            raiseButtonVisibility = View.GONE;
            lowerButtonVisibility = View.GONE;
        } else {
            if (position == 0) {
                raiseButtonVisibility = View.GONE;
                lowerButtonVisibility = View.VISIBLE;
            } else {
                raiseButtonVisibility = View.VISIBLE;
                ListView listView = (ListView) parent;
                if (position == (listView.getCount() - 1)) {
                    lowerButtonVisibility = View.GONE;
                } else {
                    lowerButtonVisibility = View.VISIBLE;
                }
            }
        }

        ImageButton raiseButton = (ImageButton) view.findViewById(R.id.mainListEntryRaiseButton);
        raiseButton.setVisibility(raiseButtonVisibility);
        ImageButton lowerButton = (ImageButton) view.findViewById(R.id.mainListEntryLowerButton);
        lowerButton.setVisibility(lowerButtonVisibility);

        return view;
    }

}