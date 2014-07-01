package gr.uoa.di.std08169.mobile.media.share.android.authentication;

import android.accounts.AccountManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by labis on 6/23/14.
 */
public class AuthenticatorService extends Service {
    @Override
    public IBinder onBind(final Intent intent) {
Log.d(AuthenticatorService.class.getName(), "Intent action: " + intent.getAction());
Log.d(AuthenticatorService.class.getName(), "Binder: " + new Authenticator(this).getIBinder());
        return AccountManager.ACTION_AUTHENTICATOR_INTENT.equals(intent.getAction()) ?
                new Authenticator(this).getIBinder() : null;
    }
}
