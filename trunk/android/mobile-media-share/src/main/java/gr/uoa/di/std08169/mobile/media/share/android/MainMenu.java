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
import android.view.View;
import android.widget.LinearLayout;

import java.io.IOException;

import gr.uoa.di.std08169.mobile.media.share.android.authentication.Authenticator;


public class MainMenu extends ActionBarActivity implements View.OnClickListener {
    private LinearLayout map;
    private LinearLayout list;
    private LinearLayout newPhoto;
    private LinearLayout newVideo;
    private LinearLayout upload;
    private LinearLayout myAccount;
    //logout

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        //To activity MainMenu zhtaei ton account manager tou susthmatos.
//Log.d(MainMenu.class.getName(), "Before authentication");
//        final AccountManager accountManager = AccountManager.get(this);
//Log.d(MainMenu.class.getName(), "Account manager: " + accountManager);
//        //phre tous logariasmous pou aforoun auth tin klash
//        final Account[] accounts = accountManager.getAccountsByType(Authenticator.class.getName());
//Log.d(MainMenu.class.getName(), "Found " + accounts.length + " accounts");
//        if (accounts.length > 0) { // o xrhsths exei kanei login
//            AccountManagerFuture<Bundle> future = accountManager.getAuthToken(accounts[0], Authenticator.class.getName(), null, this, null, null);
////            final String password = future.getResult().getString("password");
//
//        } else {
//            try {
//                AccountManagerFuture<Bundle> future = accountManager.addAccount(Authenticator.class.getName(), Authenticator.class.getName(),
//                        null, null, this, null, null);
//                startActivity((Intent) future.getResult().get(AccountManager.KEY_INTENT));
//            } catch (final AuthenticatorException e) {
//                Log.e(MainMenu.class.getName(), "Error authenticating user", e);
//            } catch (final IOException e) {
//                Log.e(MainMenu.class.getName(), "Error authenticating user", e);
//            } catch (final OperationCanceledException e) {}
//        }
        setContentView(R.layout.main_menu);

        map = (LinearLayout) findViewById(R.id.map);
        map.setOnClickListener(this);
        list = (LinearLayout) findViewById(R.id.list);
        list.setOnClickListener(this);
        newPhoto = (LinearLayout) findViewById(R.id.newPhoto);
        newPhoto.setOnClickListener(this);
        newVideo = (LinearLayout) findViewById(R.id.newVideo);
        newVideo.setOnClickListener(this);
        upload = (LinearLayout) findViewById(R.id.upload);
        upload.setOnClickListener(this);
        myAccount = (LinearLayout) findViewById(R.id.myAccount);
        myAccount.setOnClickListener(this);
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

    @Override
    public void onClick(final View view) {
        Class<?> clazz = null;
        if (map.equals(view))
            clazz = Map.class;
        else if (list.equals(view))
            clazz = List.class;
        else if (newPhoto.equals(view))
            clazz = NewPhoto.class;
        else if (newVideo.equals(view))
            clazz = NewVideo.class;
        else if (upload.equals(view))
            clazz = Upload.class;
        else if (myAccount.equals(view))
            clazz = MyAccount.class;

        if (clazz != null) {
            final Intent activityIntent = new Intent(getApplicationContext(), clazz);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(activityIntent);
        }
    }
}
