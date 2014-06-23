package gr.uoa.di.std08169.mobile.media.share.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;

import gr.uoa.di.std08169.mobile.media.share.android.authentication.Authenticator;


public class MainMenu extends ActionBarActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //To activity MainMenu zhtaei ton account manager tou susthmatos.
        final AccountManager accountManager = AccountManager.get(this);
        //phre tous logariasmous pou aforoun auth tin klash
        final Account[] accounts = accountManager.getAccountsByType(Authenticator.TYPE);
        if (accounts.length > 0) { // o xrhsths exei kanei login

            AccountManagerFuture<Bundle> future = accountManager.getAuthToken(accounts[0], Authenticator.TYPE, null, this, null, null);
//            final String password = future.getResult().getString("password");

        } else {
            try {
                AccountManagerFuture<Bundle> future = accountManager.addAccount(Authenticator.TYPE, Authenticator.TYPE,
                        null, null, this, null, null);
                startActivity((Intent) future.getResult().get(AccountManager.KEY_INTENT));
            } catch (final AuthenticatorException e) {
                Log.e(MainMenu.class.getName(), "Error authenticating user", e);
            } catch (final IOException e) {
                Log.e(MainMenu.class.getName(), "Error authenticating user", e);
            } catch (final OperationCanceledException e) {}
        }

        setContentView(R.layout.main_menu);
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
