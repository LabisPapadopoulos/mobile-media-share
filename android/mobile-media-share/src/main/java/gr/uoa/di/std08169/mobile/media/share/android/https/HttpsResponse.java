package gr.uoa.di.std08169.mobile.media.share.android.https;

/**
 * Created by labis on 7/3/14.
 */
public class HttpsResponse {
    private final boolean success;
    private final String response;

    public HttpsResponse(final boolean success, final String response) {
        this.success = success;
        this.response = response;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getResponse() {
        return response;
    }
}
