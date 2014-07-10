package gr.uoa.di.std08169.mobile.media.share.android.https;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

//Abstract class gia opoios tin kalesei na kanei override tin onPostExecute
//kai na exei topika to apotelesma
//1h parametros: Eisodos tou ena url,
//2h parametros: Den exei tipota ws progress (Void)
//3h parametros: Epistrefomenh apantish ena HttpsResponse
public abstract class HttpsAsyncTask extends AsyncTask<URL, Void, HttpsResponse> {
    private static final int HTTP_OK = 200;
    private final Context context;
    private final HttpClient client;

    public HttpsAsyncTask(final Context context) {
        this.context = context;
        client = new HttpClient(context);
    }

    @Override
    protected HttpsResponse doInBackground(final URL... urls) {
        if (urls.length == 0)
            return null;
        try {
            Log.i(HttpsAsyncTask.class.getName(), "Requesting " + urls[0]);
            login(); //TODO

            //Xtisimo tou Get request gia lhpsh media
            final HttpGet request = new HttpGet(urls[0].toString());
//            request.addHeader();

//            request.addHeader(BasicScheme.authenticate(
//                    new UsernamePasswordCredentials("haralambos9094@gmail.com", "202cb962ac59075b964b07152d234b70"),
//                    "UTF-8", false));




            //Ektelei thn eperwthsh ston server
            final HttpResponse response = client.execute(request);
            //to http status code pou gurnaei o server
            if (response.getStatusLine().getStatusCode() != HTTP_OK) {
                Log.e(HttpsAsyncTask.class.getName(), "Error connecting to " + urls[0] + ": " +
                        //mhnuma sfalmatos tou server
                        response.getStatusLine().getReasonPhrase());
                return new HttpsResponse(false, response.getStatusLine().getReasonPhrase());
            }
                                                                        //diavasma swmatos apanthshs
            final BufferedReader input = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            try {
                final StringBuilder json = new StringBuilder();
                String line = null;
                while ((line = input.readLine()) != null)
                    json.append(line).append('\n');
                Log.i(HttpsAsyncTask.class.getName(), "Retrieved " + response.getEntity().getContentLength() + " bytes from " + urls[0]);
                return new HttpsResponse(true, json.toString());
            } finally {
                input.close();
            }
        } catch (final IOException e) {
            Log.e(HttpsAsyncTask.class.getName(), "Error connecting to " + urls[0], e);
            return new HttpsResponse(false, e.getMessage());
        }
    }

    @Override
    protected abstract void onPostExecute(final HttpsResponse response);

    private void login() throws IOException { // TODO
        final HttpPost request = new HttpPost("https://snf-216465.vm.okeanos.grnet.gr/mobile-media-share/userServlet");
        //ta stelnei san na htan forma
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        request.setEntity(new StringEntity("action=login&email=haralambos9094%40gmail.com&password=123&url=http:%2F%2Fwww.example.org%2F&locale=en"));


//        final HttpParams httpParams = new BasicHttpParams();
//        httpParams.setParameter("action", "login");
//        httpParams.setParameter("email", "haralambos9094@gmail.com");
//        httpParams.setParameter("password", "123");
//        httpParams.setParameter("locale", "en");
//        httpParams.setParameter("url", "http://www.example.org/");
//        request.setParams(httpParams);
        final HttpResponse response = client.execute(request);
        if (response.getStatusLine().getStatusCode() != 200)
            throw new IOException("Login failed (" + response.getStatusLine().getStatusCode() + ")");
    }
}