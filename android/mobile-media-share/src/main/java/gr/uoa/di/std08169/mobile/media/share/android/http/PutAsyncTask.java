package gr.uoa.di.std08169.mobile.media.share.android.http;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;

import java.io.IOException;
import java.net.URL;

/**
 * @see <href="http://stackoverflow.com/questions/18964288/upload-a-file-through-an-http-form-via-multipartentitybuilder-with-a-progress">upload a file through an http form via multipart-entitybuilder with a progres</href="http://stackoverflow.com/questions/18964288/upload-a-file-through-an-http-form-via-multipartentitybuilder-with-a-progress">
 * Gia edit
 */
public class PutAsyncTask extends AsyncTask<Void, Void, HttpResponse> {
    private final Context context;
    private final URL url;

    public PutAsyncTask(final Context context, final URL url) {
        this.context = context;
        this.url = url;
    }

    @Override
    protected HttpResponse doInBackground(final Void... _) {
        try {
            final HttpPut request = new HttpPut(url.toString());
            return HttpClient.getClient(context).execute(request);
        } catch (final IOException e) {
            Log.e(PutAsyncTask.class.getName(), "Error connecting to " + url, e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(final HttpResponse _) {}
}
