package gr.uoa.di.std08169.mobile.media.share.android.authentication;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by labis on 6/23/14.
 * @see: http://udinic.wordpress.com/2013/04/24/write-your-own-android-authenticator/
 */
public class Authenticator extends AbstractAccountAuthenticator {
    public static final String TYPE = "gr.uoa.di.std08169.mobile.media.share";

    private final Context context;

    public Authenticator(final Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public Bundle editProperties(final AccountAuthenticatorResponse response, final String type) {
        return null;
    }

    @Override
    public Bundle addAccount(final AccountAuthenticatorResponse response, final String accountType, final String authTokenType, final String[] features, final Bundle options) throws NetworkErrorException {
        final Intent intent = new Intent(context, AuthenticatorActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        intent.putExtra(AuthenticatorActivity.AUTH_TYPE, authTokenType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(final AccountAuthenticatorResponse response, final Account account, final Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(final AccountAuthenticatorResponse response, final Account account, final String type, final Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public String getAuthTokenLabel(final String type) {
        return null;
    }

    @Override
    public Bundle updateCredentials(final AccountAuthenticatorResponse response, final Account account, final String type, final Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(final AccountAuthenticatorResponse response, final Account account, final String[] features) throws NetworkErrorException {
        return null;
    }
}
