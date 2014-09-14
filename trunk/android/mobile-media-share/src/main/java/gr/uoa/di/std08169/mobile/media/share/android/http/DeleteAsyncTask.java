package gr.uoa.di.std08169.mobile.media.share.android.http;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;

import java.io.IOException;
import java.net.URL;

//Abstract class gia opoios tin kalesei na kanei override tin onPostExecute
//kai na exei topika to apotelesma
//1h parametros: Eisodos tou ena url,
//2h parametros: Den exei tipota ws progress (Void)
//3h parametros: Epistrefomenh apantish ena HttpsResponse
public class DeleteAsyncTask extends AsyncTask<Void, Void, HttpResponse> {
    private final URL url;
    private final HttpClient client;

    public DeleteAsyncTask(final Context context, final URL url) {
        this.url = url;
        client = HttpClient.getClient(context);
    }

    @Override
    protected HttpResponse doInBackground(final Void... _) {
        try {
             //Xtisimo tou Get request gia lhpsh media
            final HttpDelete request = new HttpDelete(url.toString());
            //Ektelei thn eperwthsh ston server
            return client.execute(request);
        } catch (final IOException e) {
            Log.e(DeleteAsyncTask.class.getName(), "Error connecting to " + url, e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(final HttpResponse _) {}
}