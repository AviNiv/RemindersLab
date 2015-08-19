package il.co.aviniv.reminderslab;

import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Avi on 19/08/2015.
 */
public class Utilities {
    public static void showMessage(Context context, String message) {
        String msg = message;
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showException(Context context, Exception exception) {
        String msg = "Exception: " + exception.getMessage();
        Toast t = Toast.makeText(context, msg,  Toast.LENGTH_LONG);
        TextView tv = (TextView)t.getView().findViewById(android.R.id.message);
        tv.setTextColor(Color.YELLOW);
        t.show();
    }
}
