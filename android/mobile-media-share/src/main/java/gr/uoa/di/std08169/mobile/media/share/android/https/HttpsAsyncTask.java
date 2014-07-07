package gr.uoa.di.std08169.mobile.media.share.android.https;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

//Abstract class gia opoios tin kalesei na kanei override tin onPostExecute
//kai na exei topika to apotelesma
//1h parametros: Eisodos tou ena url,
//2h parametros: Den exei tipota ws progress (Void)
//3h parametros: Epistrefomenh apantish ena HttpsResponse
public abstract class HttpsAsyncTask extends AsyncTask<URL, Void, HttpsResponse> {
    @Override
    protected HttpsResponse doInBackground(URL... urls) {
        if (urls.length == 0)
            return null;
        Log.i(HttpsAsyncTask.class.getName(), "Requesting " + urls[0]);
        try {
            //Dhmiourgeia enos Https connection gia epikoinwnia me to servlet
            final HttpURLConnection connection = (HttpURLConnection) urls[0].openConnection();
            connection.setRequestMethod("GET");
            //            connection.setRequestProperty(); // TODO authentication
            //true giati tha diavasei dedomena (tha tou stalei apo to servlet dedomena)
            connection.setDoInput(true);
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.w(HttpsAsyncTask.class.getName(), "Connection error: " + connection.getResponseMessage() +
                        " (" + connection.getResponseCode() + ")");
                return new HttpsResponse(false, connection.getResponseMessage());
            }
            //diavazei tin apantish tou servlet
            final BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            try {
                final StringBuilder response = new StringBuilder();
                String line = null;
                while ((line = input.readLine()) != null)
                    response.append(line).append('\n');
                Log.w(HttpsAsyncTask.class.getName(), "Connection success: " + response.toString());
                return new HttpsResponse(true, response.toString());
            } finally {
                input.close();
            }
        } catch (final IOException e) {
            Log.w(HttpsAsyncTask.class.getName(), "Connection error", e);
            return new HttpsResponse(false, e.getMessage());
        }
    }

    @Override
    protected abstract void onPostExecute(final HttpsResponse response);
}