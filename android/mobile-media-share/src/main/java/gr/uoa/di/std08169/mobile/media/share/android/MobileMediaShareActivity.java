package gr.uoa.di.std08169.mobile.media.share.android;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import gr.uoa.di.std08169.mobile.media.share.android.https.HttpsAsyncTask;
import gr.uoa.di.std08169.mobile.media.share.android.https.HttpsResponse;

/**
 * Created by labis on 7/3/14.
 */
public abstract class MobileMediaShareActivity extends ActionBarActivity {
    protected User user;

    protected void requestUser() {
        //Asungxronh klhsh sto web gia na parei xrhsth
        try {
            new HttpsAsyncTask(getApplicationContext()) {
                @Override
                protected void onPostExecute(HttpsResponse response) {
                    Log.d(MobileMediaShareActivity.class.getName(), "--->Post Execute " + response.getResponse());
                    if (!response.isSuccess()) {
                        error(R.string.authenticationError, response.getResponse());
                        return;
                    }
                    try {
                        //[ ... ] -> json Array
                        final JSONObject user = new JSONObject(response.getResponse());
                        //{"email":"haralambos9094@gmail.com","name":"labis","status":"NORMAL"}
                        final String email = user.getString("email");
                        final UserStatus status = (user.getString("status") == null) ? null : UserStatus.valueOf(user.getString("status"));
                        final String name = user.getString("name");
                        final String photo = user.getString("photo");
                        Log.d(MobileMediaShareActivity.class.getName(), "Retrieved user");
                        MobileMediaShareActivity.this.user = ((email == null) || (status == null)) ? null : new User(email, status, name, photo);
                        Log.d(MobileMediaShareActivity.class.getName(), "Updated list");
                    } catch (final JSONException e) {
                        error(R.string.authenticationError, e.getMessage());
                    }
                }
            }.execute(new URL(getResources().getString(R.string.getCurrentUserUrl))).get();
        } catch (final ExecutionException e) {
            error(R.string.authenticationError, e.getMessage());
        } catch (final InterruptedException e) {
            error(R.string.authenticationError, e.getMessage());
        } catch (final IOException e) {
            error(R.string.authenticationError, e.getMessage());
        }
    }

    protected void error(final int message, final Object... arguments) {
        Log.e(MobileMediaShareActivity.class.getName(), String.format(getResources().getString(message), arguments));
        Toast.makeText(this, String.format(getResources().getString(message), arguments), Toast.LENGTH_LONG).show();
        final Intent activityIntent = new Intent(getApplicationContext(), MainMenu.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(activityIntent);
    }

    protected User getCurrentUser() {
        return user;
    }
}
