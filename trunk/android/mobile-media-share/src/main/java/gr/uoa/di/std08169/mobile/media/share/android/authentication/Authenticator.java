package gr.uoa.di.std08169.mobile.media.share.android.authentication;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

import gr.uoa.di.std08169.mobile.media.share.android.R;
import gr.uoa.di.std08169.mobile.media.share.android.http.HttpClient;
import gr.uoa.di.std08169.mobile.media.share.android.http.PostAsyncTask;

/**
 * Created by labis on 6/23/14.
 * @see: <a href="http://udinic.wordpress.com/2013/04/24/write-your-own-android-authenticator/">Write your own Android Authenticator</a>
 */
//Authenticator: Kaleitai apo ton Account Manager gia na diaxeiristei logariasmous sugkekrimenou tupou
//kathorizontai sto manifest mesw enos service pou epistrefei ton Authenticator.
public class Authenticator extends AbstractAccountAuthenticator {
    private static final String ENTITY = "action=login&email=%s&password=%s&url=http:%%2F%%2Fwww.example.org%%2F&locale=en";
    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String UTF_8 = "UTF-8";

    private final Context context;

    public Authenticator(final Context context) {
        super(context);
        this.context = context;
    }

    //Prosthikh neou logariasmou
    @Override
    public Bundle addAccount(final AccountAuthenticatorResponse response, final String accountType,
                             final String authenticationTokenType, final String[] features, final Bundle options) throws NetworkErrorException {
        // just ask user to enter credentials
        final Intent intent = new Intent(context, Login.class);
        //Fortwnei tin douleia sto activity login kai tou paradidei kai to response gia na boresei
        //ekeino na apanthsei ston xrhsth
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        //Sto bundle borei na bei 'h result (h douleia egine) 'h ena activity
        //kai h douleia ginetai asugxrona
        bundle.putParcelable(AccountManager.KEY_INTENT, intent); //asugxrono apotelesma
        return bundle;
    }

    //Epivevaiwsh logiariasmou
    @Override
    public Bundle confirmCredentials(final AccountAuthenticatorResponse accountAuthenticatorResponse,
                                     final Account account, final Bundle options) throws NetworkErrorException {
        return null;
    }

    //allagh idiothtwn logariasmou (gia My Account)
    @Override
    public Bundle editProperties(final AccountAuthenticatorResponse accountAuthenticatorResponse, final String accountType) {
        throw new UnsupportedOperationException();
    }

    //Gia login
    @Override
    public Bundle getAuthToken(final AccountAuthenticatorResponse response, final Account account,
                               final String type, final Bundle options) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    //Epistrefei label gia to authentication token se diafores glwsses
    @Override
    public String getAuthTokenLabel(final String authenticationTokenType) {
        throw new UnsupportedOperationException();
    }

    //ti xarakthristika exei autos o logariasmos (gia diafores uphresies)
    @Override
    public Bundle hasFeatures(final AccountAuthenticatorResponse accountAuthenticatorResponse, final Account account,
                              final String[] features) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    //gia allagh username kai password
    @Override
    public Bundle updateCredentials(final AccountAuthenticatorResponse accountAuthenticatorResponse,
                                    final Account account, final String authenticationTokenType, final Bundle options) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }
}
