package gr.uoa.di.std08169.mobile.media.share.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

import gr.uoa.di.std08169.mobile.media.share.android.authentication.Authenticator;
import gr.uoa.di.std08169.mobile.media.share.android.http.GetAsyncTask;
import gr.uoa.di.std08169.mobile.media.share.android.http.HttpClient;
import gr.uoa.di.std08169.mobile.media.share.android.http.PostAsyncTask;
import gr.uoa.di.std08169.mobile.media.share.android.media.Media;
import gr.uoa.di.std08169.mobile.media.share.android.media.MediaType;
import gr.uoa.di.std08169.mobile.media.share.android.user.User;
import gr.uoa.di.std08169.mobile.media.share.android.user.UserStatus;

public abstract class MobileMediaShareActivity extends ActionBarActivity {
    protected static final String APPLICATION_FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    protected static final String UTF_8 = "UTF-8";
    protected static final float GOOGLE_MAPS_ZOOM = 8.0f;
    protected static final float MARKER_ANCHOR_X = 0.5f;
    protected static final float MARKER_ANCHOR_Y = 1.0f;
    protected static final double MIN_DISTANCE = 0.000001;
    private static final double DEFAULT_LATITUDE = 37.968546;	//DIT lat
    private static final double DEFAULT_LONGITUDE = 23.766968;	//DIT lng
    private static final int DURATION_BASE = 60;
    private static final BigDecimal DEGREES_BASE = new BigDecimal(60);
    private static final String LOGIN_ENTITY = "action=login&email=%s&password=%s&url=http:%%2F%%2Fwww.example.org%%2F&locale=en&redirect=false";
    private static final String DOWNLOAD_ENTITY = "media=%s"; //postarei sto downloadServlet to id tou media
    protected User currentUser;

    public String formatLatitude(final BigDecimal latitude) {
        // 1 moira = 60 prwta lepta
        // 1 prwto lepto = 60 deutera
        // to divideAndRemainder kanei tin diairesh kai epistefei:
        // temp1[0]: phliko
        // temp1[1]: upoloipo
        final BigDecimal[] temp1 = latitude.multiply(DEGREES_BASE).multiply(DEGREES_BASE).divideAndRemainder(DEGREES_BASE);
        final int seconds = temp1[1].intValue();
        final BigDecimal[] temp2 = temp1[0].divideAndRemainder(DEGREES_BASE);
        final int minutes = temp2[1].intValue();
        final int degrees = temp2[0].intValue();
        //an einai arnhtiko-> einai notia, alliws voreia
        if (latitude.compareTo(BigDecimal.ZERO) < 0) {
            return String.format(getResources().getString(R.string.latitudeFormatSouth), -degrees, -minutes, -seconds);
        } else {
            return String.format(getResources().getString(R.string.latitudeFormatNorth), degrees, minutes, seconds);
        }
    }

    public String formatLongitude(final BigDecimal longitude) {
        final BigDecimal[] temp1 = longitude.multiply(DEGREES_BASE).multiply(DEGREES_BASE).divideAndRemainder(DEGREES_BASE);
        final int seconds = temp1[1].intValue();
        final BigDecimal[] temp2 = temp1[0].divideAndRemainder(DEGREES_BASE);
        final int minutes = temp2[1].intValue();
        final int degrees = temp2[0].intValue();
        //an einai arnhtiko-> einai dutika, alliws anatolika
        if (longitude.compareTo(BigDecimal.ZERO) < 0) {
            return String.format(getResources().getString(R.string.longitudeFormatWest), -degrees, -minutes, -seconds);
        } else {
            return String.format(getResources().getString(R.string.longitudeFormatEast), degrees, minutes, seconds);
        }
    }

    public String formatDuration(final int duration) {
        final int seconds = duration % DURATION_BASE;
        final int minutes = (duration / DURATION_BASE) % DURATION_BASE; //p.x se 63 lepta -> krataei 3 lepta
        final int hours = duration / DURATION_BASE / DURATION_BASE;
        //{0}h {1}m {2}s
        if (hours > 0)
            return String.format(getResources().getString(R.string.durationFormatHours), hours, minutes, seconds);
        else if (minutes > 0)
            return String.format(getResources().getString(R.string.durationFormatMinutes), minutes, seconds);
        else
            return String.format(getResources().getString(R.string.durationFormatSeconds), seconds);
    }

    public String formatEmail(final String email) {
        return String.format(getResources().getString(R.string.emailFormat), email.substring(0, email.indexOf('@')));
    }

    public String formatSize(final long size) {
        //megethos arxeiou (B, KB, MB)
        if (size < 1024l)
            return String.format(getResources().getString(R.string.sizeFormatB), (int) size);
        else if (size < 1024l * 1024l)
            return String.format(getResources().getString(R.string.sizeFormatKB), size / 1024.0f);
        else
            return String.format(getResources().getString(R.string.sizeFormatMB), size / 1024.0f / 1024.0f);
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
                    getResources().getString(R.string.secureBaseUrl)));
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

    protected LatLng getLatLng() {
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //Dhmiourgeia krithriwn gia na parei ton kalutero location manager
        final Criteria criteria = new Criteria();
        criteria.setAltitudeRequired(false); //oxi krithrio upsometrou
        criteria.setBearingRequired(false); //oxi gia fora (pros ta pou koitaei)
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(false); //oxi xrewsh gia ton provider
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH); //auto mas endiaferei gia to latitude kai longitude
        final Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        return new LatLng((location == null) ? DEFAULT_LATITUDE : location.getLatitude(),
                (location == null) ? DEFAULT_LONGITUDE : location.getLongitude());
    }

    protected String formatLocation(final BigDecimal latitude, final BigDecimal longitude) {
        return String.format(getResources().getString(R.string.locationFormat), formatLatitude(latitude), formatLongitude(longitude));
    }

    protected void error(final int message, final Object... arguments) {
        Log.e(MobileMediaShareActivity.class.getName(), String.format(getResources().getString(message), arguments));
        Toast.makeText(this, String.format(getResources().getString(message), arguments), Toast.LENGTH_LONG).show();
        finish();
    }

    protected void downloadMedia(final Media media) {
        try {
            final String requestDownloadUrl = String.format(getString(R.string.requestDownloadUrl), getString(R.string.unsecureBaseUrl));
            final HttpEntity entity = new StringEntity(String.format(DOWNLOAD_ENTITY, media.getId()));
            //* Arxika kanei post gia na parei to dikaiwma gia na katevazei (pairnontas ena token)
                                                            /*H get epistrefei to apotelesma tou AsyncTask*/
            final HttpResponse response = new PostAsyncTask(getApplicationContext(), new URL(requestDownloadUrl),
                    entity, APPLICATION_FORM_URL_ENCODED).execute().get();

            if (response == null) //An null, den exei diktuo
                error(R.string.errorDownloadingMedia, getResources().getString(R.string.connectionError));
            else if ((response.getStatusLine().getStatusCode() == HttpClient.HTTP_UNAUTHORIZED) && login()) { //paei gia login
                downloadMedia(media); //kalei anadromika ton eauto ths gia na kanei to arxiko Download
                return;
            } else if (response.getStatusLine().getStatusCode() != HttpClient.HTTP_OK) //Den einai oute POST oute Unauthorized
                error(R.string.errorDownloadingMedia, response.getStatusLine().getReasonPhrase());
            else {
                //Apantaei m' ena token
                final StringBuilder downloadToken = new StringBuilder();
                final BufferedReader input = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                try {
                    String line;
                    while ((line = input.readLine()) != null)
                        downloadToken.append(line);
                } finally {
                    input.close();
                }
                //** Afou phre to downloadToken zhtaei apo ton downloadManager na kanei to GET
                final String downloadUrl = String.format(getResources().getString(R.string.downloadUrl),
                        getResources().getString(R.string.secureBaseUrl),
                        URLEncoder.encode(downloadToken.toString(), UTF_8));
Log.d(MobileMediaShareActivity.class.getName(), "URL: " + downloadUrl);
                final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
                //apothhkeuetai sta downloads me onoma title
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, media.getId());
                //ti typou arxeio einai
                request.setMimeType(media.getType());
                //to download tha einai orato stin lista twn downloads kai tha eidopoihthei o xrhsths molis katevei
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setTitle(media.getTitle());
                //gia na fainetai sta downloads
                request.setVisibleInDownloadsUi(true);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                request.setAllowedOverMetered(true);
                request.setAllowedOverRoaming(true);
                final MediaType mediaType = MediaType.getMediaType(media.getType());

                if ((mediaType == MediaType.AUDIO) || (mediaType == MediaType.IMAGE) || (mediaType == MediaType.VIDEO))
                    //gia na mathei o media scanner oti uparxei to arxeio kai na to provalei argotera
                    request.allowScanningByMediaScanner();

                final DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                //Xekinaei to download vazontas to sthn oura twn downloads (enqueue)
                final long id = downloadManager.enqueue(request); //Kanei GET
            }
        } catch (final IOException e){
            error(R.string.errorDownloadingMedia, e.getMessage());
        } catch (final InterruptedException e){
            error(R.string.errorDownloadingMedia, e.getMessage());
        } catch (final ExecutionException e){
            error(R.string.errorDownloadingMedia, e.getMessage());
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = retrieveUser();
    }

    @Override
    protected void onPause() {
        try {
            // logout
            final String url = String.format(getResources().getString(R.string.logoutUrl),
                    getResources().getString(R.string.secureBaseUrl));
            new GetAsyncTask(this, new URL(url)).execute().get();
            Log.d(MobileMediaShareActivity.class.getName(), "User " + currentUser + " logged out");
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        currentUser = retrieveUser();
        super.onResume();
    }

    private User retrieveUser() {
        //Asungxronh klhsh sto web gia na parei xrhsth
        try {
            final URL url = new URL(String.format(getResources().getString(R.string.userServletUrl),
                    //Prosthikh get gia na perimenei na teleiwsei h asugxronh ektelesh kai na paroume to apotelesma
                    //san wait() kai get() tautoxrona
                    getResources().getString(R.string.secureBaseUrl)));
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
