package gr.uoa.di.std08169.mobile.media.share.android.authentication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by labis on 6/23/14.
 */
public class AuthenticatorService extends Service {
    @Override
    public IBinder onBind(final Intent intent) {
        return new Authenticator(this).getIBinder();
    }
}
