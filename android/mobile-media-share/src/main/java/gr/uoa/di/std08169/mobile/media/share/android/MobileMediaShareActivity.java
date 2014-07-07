package gr.uoa.di.std08169.mobile.media.share.android;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by labis on 7/3/14.
 */
public abstract class MobileMediaShareActivity extends ActionBarActivity {
    public void error(final int message, final Object... arguments) {
        Log.e(MobileMediaShareActivity.class.getName(), String.format(getResources().getString(message), arguments));
        Toast.makeText(this, String.format(getResources().getString(message), arguments), Toast.LENGTH_LONG).show();
        final Intent activityIntent = new Intent(getApplicationContext(), MainMenu.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(activityIntent);
    }
}
