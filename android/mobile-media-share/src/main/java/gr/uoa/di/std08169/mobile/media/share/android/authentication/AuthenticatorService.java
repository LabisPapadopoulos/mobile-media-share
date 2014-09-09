package gr.uoa.di.std08169.mobile.media.share.android.authentication;

import android.accounts.AccountManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by labis on 6/23/14.
 * Service pou trexei sto background kai dinei ton Authenticator
 */
public class AuthenticatorService extends Service {
    //Molis zhthsei kapoios (opoiadhpote activity) na sundethei tou dinei ton Authenticator
    //IBinder gia na kanei access ston authenticator (asugxrona)
    @Override
    public IBinder onBind(final Intent intent) {
        return new Authenticator(this).getIBinder();
    }
}
