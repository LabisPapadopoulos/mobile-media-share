package gr.uoa.di.std08169.mobile.media.share.android.http;

import android.content.Context;
import android.util.Log;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import gr.uoa.di.std08169.mobile.media.share.android.R;

/**
 * Created by labis on 7/8/14.
 * @see <a href="http://blog.antoine.li/2010/10/22/android-trusting-ssl-certificates/">Android: Trusting SSL certificates</a>
 */
//HttpClient me peiragmeno to Https prwtokollo
public final class HttpClient extends DefaultHttpClient {
    public static final int HTTP_OK = 200;
    public static final int HTTP_UNAUTHORIZED = 401;
    private static final String HTTP_SCHEME = "http";
    private static final int HTTP_PORT = 80;
    private static final String HTTPS_SCHEME = "https";
    private static final int HTTPS_PORT = 443;
    private static final String KEY_STORE_TYPE = "BKS";
    private static final String KEY_STORE_PASSWORD = "mysecret";

    private static HttpClient INSTANCE;

    private final Context context;

    public static synchronized HttpClient getClient(final Context context) {
        if (INSTANCE == null)
            INSTANCE = new HttpClient(context);
        return INSTANCE;
    }

    private HttpClient(final Context context) {
        this.context = context;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException(HttpClient.class.getName() + " can not be cloned");
    }

    //Orizei enan ClientConnectionManager pou tha xrhsimopoiei anti gia ton Default
    @Override
    protected ClientConnectionManager createClientConnectionManager() {
        try {
            //Ena map apo prwtokollo, socketFactory (pws anoigei h porta) kai port
            final SchemeRegistry registry = new SchemeRegistry();
            //Prosthikh kataxwrishs gia http me default porta 80 kai to default socket factory (gia to pws tha anoigei ta sockets)
            registry.register(new Scheme(HTTP_SCHEME, PlainSocketFactory.getSocketFactory(), HTTP_PORT));
            //Dhmiourgia key store typou BSK (sullogh apo pistopoihtika, pou exei mono to diko mou mazi me to password)

            final KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
            //Anoigma tou arxeiou s' ena InputStream gia diavasma
            final InputStream input = context.getResources().openRawResource(R.raw.keystore);
            try {
                //gemisma tou keystore apo to arxeio, me xrhsh tou password gia apokruptografhsh
                keyStore.load(input, KEY_STORE_PASSWORD.toCharArray());
            } finally {
                input.close();
            }
            //Dhmiourgia socket factory pou uposthrizei SSL kai empisteuetai osa certificates vrei sto keystore
            final SSLSocketFactory socketFactory = new SSLSocketFactory(keyStore);
            //Na sumfwnei to onoma tou server me to onoma tou pistopoihtikou
            socketFactory.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
            //Prosthikh kataxwrishs gia https me defult porta 443 kai to socket factory pou ftiaxthke prin
            registry.register(new Scheme(HTTPS_SCHEME, socketFactory, HTTPS_PORT));
            //Gia mia sundesh tin fora, me default parametrous (getParams()), alla me to
            //SchemeRegistry pou einai orismeno
            return new SingleClientConnManager(getParams(), registry);
        } catch (final CertificateException e) {
            Log.e(HttpClient.class.getName(), "Error loading server certificate", e);
            return null;
        } catch (final IOException e) {
            Log.e(HttpClient.class.getName(), "Error loading server certificate", e);
            return null;
        } catch (final KeyManagementException e) {
            Log.e(HttpClient.class.getName(), "Error loading server certificate", e);
            return null;
        } catch (final KeyStoreException e) {
            Log.e(HttpClient.class.getName(), "Error loading server certificate", e);
            return null;
        } catch (final NoSuchAlgorithmException e) {
            Log.e(HttpClient.class.getName(), "Error loading server certificate", e);
            return null;
        } catch (final UnrecoverableKeyException e) {
            Log.e(HttpClient.class.getName(), "Error loading server certificate", e);
            return null;
        }
    }
}
