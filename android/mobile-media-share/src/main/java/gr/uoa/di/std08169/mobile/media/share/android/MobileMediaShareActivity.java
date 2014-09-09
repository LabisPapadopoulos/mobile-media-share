package gr.uoa.di.std08169.mobile.media.share.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

import gr.uoa.di.std08169.mobile.media.share.android.authentication.Authenticator;
import gr.uoa.di.std08169.mobile.media.share.android.http.GetAsyncTask;
import gr.uoa.di.std08169.mobile.media.share.android.http.HttpClient;
import gr.uoa.di.std08169.mobile.media.share.android.http.PostAsyncTask;
import gr.uoa.di.std08169.mobile.media.share.android.user.User;
import gr.uoa.di.std08169.mobile.media.share.android.user.UserStatus;

public abstract class MobileMediaShareActivity extends ActionBarActivity {
    protected static final String APPLICATION_FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    protected static final String UTF_8 = "UTF-8";
    private static final String LOGIN_ENTITY = "action=login&email=%s&password=%s&url=http:%%2F%%2Fwww.example.org%%2F&locale=en&redirect=false";
    protected User currentUser;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = retrieveUser();
    }

    protected boolean login() {
        final AccountManager accountManager = AccountManager.get(this);
        final Account[] accounts = accountManager.getAccountsByType(Authenticator.class.getName());
        if ((accounts.length == 0) || (accountManager.getPassword(accounts[0]) == null)) { // den yparxei logariasmos, xekiname to login activity
            //xekinaei to login activity
            requestCredentials();
            return false;
        }
        try {

            final URL url = new URL(String.format(getResources().getString(R.string.userServletUrl),
                    getResources().getString(R.string.baseUrl)));
            final String email = URLEncoder.encode(accounts[0].name, UTF_8);
            final String password = URLEncoder.encode(accountManager.getPassword(accounts[0]), UTF_8);
            final HttpEntity entity = new StringEntity(String.format(LOGIN_ENTITY, email, password));
                                                            /*H get epistrefei to apotelesma tou AsyncTask*/
            final HttpResponse response = new PostAsyncTask(this, url, entity, APPLICATION_FORM_URL_ENCODED).execute().get();
            if (response == null) { // den yparxei diktyo
                error(R.string.authenticationError, getApplicationContext().getResources().getString(R.string.connectionError));
                return false;
            }
            if (response.getStatusLine().getStatusCode() == HttpClient.HTTP_UNAUTHORIZED) { // den isxyei o logariasmos
                //xekinaei to login activity
                requestCredentials();
                return false;
            }
            if (response.getStatusLine().getStatusCode() != HttpClient.HTTP_OK) { // allo sfalma
                error(R.string.authenticationError, response.getStatusLine().getReasonPhrase());
                return false;
            }
            return true;
        } catch (final ExecutionException e) {
            error(R.string.authenticationError, e.getMessage());
            return false;
        } catch (final InterruptedException e) {
            error(R.string.authenticationError, e.getMessage());
            return false;
        } catch (final IOException e) {
            error(R.string.authenticationError, e.getMessage());
            return false;
        }
    }

    protected void error(final int message, final Object... arguments) {
        Log.e(MobileMediaShareActivity.class.getName(), String.format(getResources().getString(message), arguments));
        Toast.makeText(this, String.format(getResources().getString(message), arguments), Toast.LENGTH_LONG).show();
        finish();
    }

    private User retrieveUser() {
        //Asungxronh klhsh sto web gia na parei xrhsth
        try {
            final URL url = new URL(String.format(getResources().getString(R.string.userServletUrl),
                    //Prosthikh get gia na perimenei na teleiwsei h asugxronh ektelesh kai na paroume to apotelesma
                    //san wait() kai get() tautoxrona
                    getResources().getString(R.string.baseUrl)));
            final HttpResponse response = new GetAsyncTask(this, url).execute().get();
            if (response == null) { //An null, den exei diktuo
                error(R.string.authenticationError, getResources().getString(R.string.connectionError));
                return null;
            }
            if (response.getStatusLine().getStatusCode() == HttpClient.HTTP_UNAUTHORIZED) { //paei gia login
                return login() ? retrieveUser() : null; //kalei anadromika ton eauto ths gia na kanei to arxiko GET
            }
            if (response.getStatusLine().getStatusCode() != HttpClient.HTTP_OK) { //Den einai oute GET oute Unauthorized
                error(R.string.authenticationError, response.getStatusLine().getReasonPhrase());
                return null;
            }
            try {
                //[ ... ] -> json Array
                final StringBuilder json = new StringBuilder();
                final BufferedReader input = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                try {
                    String line;
                    while ((line = input.readLine()) != null)
                        json.append(line);
                } finally {
                    input.close();
                }
                final JSONObject user = new JSONObject(json.toString());
                //{"email":"haralambos9094@gmail.com","name":"labis","status":"NORMAL"}
                final String email = user.getString("email");
                final UserStatus status = UserStatus.valueOf(user.getString("status"));
                final String name = user.has("name") ? user.getString("name") : null;
                final String photo = user.has("photo") ? user.getString("photo") : null;
                return ((email == null) || (status == null)) ? null : new User(email, status, name, photo);
            } catch (final JSONException e) {
                error(R.string.authenticationError, e.getMessage());
                return null;
            }
        } catch (final ExecutionException e) {
            error(R.string.authenticationError, e.getMessage());
            return null;
        } catch (final InterruptedException e) {
            error(R.string.authenticationError, e.getMessage());
            return null;
        } catch (final IOException e) {
            error(R.string.authenticationError, e.getMessage());
            return null;
        }
    }

    private void requestCredentials() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final AccountManager accountManager = AccountManager.get(MobileMediaShareActivity.this);
                    //Apothikeuei to apotelesma pou gurise o account manager
                    final Bundle bundle = accountManager.addAccount(Authenticator.class.getName(), Authenticator.class.getName(),
                            null, null, null, null, null).getResult();
                    //pairnei to intent pou exei mesa to apotelesma (opoio tou pei o Authenticator - dhladh to Login)
                    final Intent login = (Intent) bundle.get(AccountManager.KEY_INTENT);
                    startActivity(login);
                } catch (final OperationCanceledException e) {
                    error(R.string.authenticationError, e.getMessage());
                } catch (final IOException e) {
                    error(R.string.authenticationError, e.getMessage());
                } catch (final AuthenticatorException e) {
                    error(R.string.authenticationError, e.getMessage());
                }
            }
        }).start();
        finish();
    }
}
