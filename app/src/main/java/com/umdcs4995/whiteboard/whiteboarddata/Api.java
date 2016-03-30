package com.umdcs4995.whiteboard.whiteboarddata;

/**
 * Created by LauraKrebs on 3/29/16.
 */

import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.squareup.otto.DeadEvent;
import com.squareup.otto.Subscribe;
import com.umdcs4995.whiteboard.BuildConfig;
import com.umdcs4995.whiteboard.MainActivity;
import com.umdcs4995.whiteboard.R;

import java.util.LinkedList;
import java.util.List;

/**
 * Class that performs asynchronous API calls and posts the results to the
 * event bus.
 */
public class Api {
    private interface Responder {
        public void setResult(ApiEvent<?> result);
    }

    private static final int INVALID_LOGIN_CODE = 401;
    private static final String TAG = "Api";

    /**
     * Pending API call. This is set when an authenticated request is performed
     * but the user is not logged in. This is then performed once the user has
     * authenticated.
     */
    private static ApiCall myPendingApiCall;

    /**
     * List of events that have been sent but not received by any subscribers.
     */
    private static List<ApiEvent<?>> myDeadApiEvents;

    /**
     * Returns true if the the user needs to be authenticated for the given site and endpoint.
     */
    private static boolean requireAuthentication(ApiEndpoint endpoint) {
        return (endpoint.mAuthenticated) &&
                !endpoint.mForcePublic;
    }

    /**
     * Performs the API call defined by the given Intent. This takes care of opening a
     * login dialog and saving the Intent if the user isn't authenticated but should be.
     */
    public static void call(Activity activity, final ApiCall apiCall) {
        ApiEndpoint endpoint = apiCall.mEndpoint;
        if (activity != null) {
            apiCall.mActivityid = ((MainActivity)activity).getActivityid();
        } else if (apiCall.mActivityid == -1) {
            Log.w(TAG, "Missing activityid!", new Exception());
        }

        User user = App.get().getUser();
        apiCall.mUser = user;

        if (apiCall.mAuthToken == null && user != null) {
            // Set the auth token to the current user's auth token if one isn't
            // explicitly set. Originally added for logout and user info because a
            // user isn't associated with the auth token when the API call is performed.
            apiCall.mAuthToken = user.getAuthToken();
        }

        // User needs to be logged in for an authenticated endpoint with the exception of login.
        if (requireAuthentication(endpoint) && !App.get().isUserLoggedIn()) {
            App.getBus().post(getUnauthorizedEvent(apiCall));
        } else {
            performRequest(apiCall, new Responder() {
                public void setResult(ApiEvent<?> result) {
                    if (apiCall.mEndpoint.mPostResults) {
                        /**
                         * Always post the result despite any errors. This actually sends it off
                         * to BaseActivity which posts the underlying ApiEvent<?> if the ApiCall
                         * was initiated by that Activity instance.
                         */
                        App.getBus().post(new ApiEvent.ActivityProxy(result));
                    }
                }
            });
        }
    }

    /**
     * Returns an ApiEvent that triggers a login dialog and sets up the ApiCall to be performed
     * once the user successfully logs in.
     */
    private static ApiEvent<?> getUnauthorizedEvent(ApiCall apiCall) {
        myPendingApiCall = apiCall;

        // We aren't logged in anymore so lets make sure we don't think we are.
        // Note: This does _not_ remove the account from the AccountManager. The
        // user still has a chance to reauthenticate and salvage the account.
        App.get().shallowLogout(false);

        // The ApiError doesn't matter as long as one exists.
        return new ApiEvent.Unauthorized().
                setCode(INVALID_LOGIN_CODE).
                setError(new ApiError("", "", ApiError.Type.UNAUTHORIZED)).
                setApiCall(apiCall);
    }

    /**
     * Returns the pending API call and sets it to null. Returns null if no pending API call.
     */
    public static ApiCall getAndRemovePendingApiCall(Context context) {
        ApiCall pendingApiCall = myPendingApiCall;
        myPendingApiCall = null;

        if (pendingApiCall != null) {
            return pendingApiCall;
        } else {
            return null;
        }
    }

    /**
     * Parse the response in the given result with the given requestTarget.
     */
    private static ApiEvent<?> parseResult(ApiEvent<?> result, ApiEndpoint endpoint) {
        ApiEvent<?> event;

        int code = result.mCode;
        String response = result.getResponse();

        ApiError error = null;

        if (!isSuccess(code)) {
            error = JSONHelper.parseError(response, code);
        }

        if (error != null) {
            event = result.setError(error);
        } else {
            try {
                // We don't know the type of ApiEvent it is so we must let the endpoint's
                // parseResult return the correct one...
                event = endpoint.parseResult(response);

                // ... and then we can copy over the other values we need.
                event.mCode = code;
                event.mApiCall = result.mApiCall;
                event.mResponse = result.mResponse;
                event.mStoredResponse = result.mStoredResponse;
            } catch (Exception e) {
                // This is meant to catch JSON and GSON parse exceptions but enumerating
                // all different types of Exceptions and putting error handling code
                // in one place is tedious.
                Log.e(TAG, "API parse error", e);
                result.setError(new ApiError(ApiError.Type.PARSE));

                event = result;
            }

            if (!isSuccess(code)) {
                event.setError(ApiError.getByStatusCode(code));
            }
        }

        return event;
    }

    private static boolean isSuccess(int code) {
        return code >= 200 && code < 300;
    }

    public static AlertDialog getErrorDialog(final Activity activity,
                                             final ApiEvent<?> event) {
        ApiError error = event.getError();

        int positiveButton = error.mType.mTryAgain ?
                R.string.try_again : R.string.error_confirm;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(error.mTitle)
                .setMessage(error.mMessage)
                .setPositiveButton(positiveButton,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Try performing the request again.
                                if (event.mError.mType.mTryAgain) {
                                    call(activity, event.mApiCall);
                                }

                                dialog.dismiss();

                                if (event.mError.mType.mFinishActivity) {
                                    activity.finish();
                                }
                            }
                        });

        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                activity.finish();
            }
        });

        return dialog;
    }

    public static void init() {
        myDeadApiEvents = new LinkedList<ApiEvent<?>>();

        App.getBus().register(new Object() {
            @Subscribe
            public void onDeadEvent(DeadEvent deadEvent) {
                Object event = deadEvent.event;

                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "onDeadEvent: " + event.getClass().getName());
                }

                if (event instanceof ApiEvent<?>) {
                    addDeadApiEvent((ApiEvent<?>)event);
                } else if (event instanceof ApiEvent.ActivityProxy) {
                    addDeadApiEvent(((ApiEvent.ActivityProxy)event).getApiEvent());
                }
            }

        });
    }

    private static void addDeadApiEvent(ApiEvent<?> apiEvent) {
        synchronized (myDeadApiEvents) {
            myDeadApiEvents.add(apiEvent);
        }
    }

    public static void retryDeadEvents(MainActivity activity) {
        synchronized (myDeadApiEvents) {
            if (myDeadApiEvents.isEmpty()) {
                return;
            }

            List<ApiEvent<?>> deadApiEvents = myDeadApiEvents;
            myDeadApiEvents = new LinkedList<ApiEvent<?>>();
            int activityid = activity.getActivityid();

            // Iterate over all the dead events, firing off each one.  If it fails,
            // it is recaught by the @Subscribe onDeadEvent, and added back to the list.
            for (ApiEvent<?> apiEvent : deadApiEvents) {
                // Fire the event If the activityids match, otherwise add it back
                // to the list of dead events so we can try it again later.
                if (activityid == apiEvent.mApiCall.mActivityid) {
                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "Retrying dead event: " +
                                apiEvent.getClass().getName());
                    }

                    App.getBus().post(apiEvent);
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "Adding dead event: " + apiEvent.getClass().toString());
                    }

                    myDeadApiEvents.add(apiEvent);
                }
            }

            if (BuildConfig.DEBUG && myDeadApiEvents.size() > 0) {
                Log.i(TAG, "Skipped " + myDeadApiEvents.size() + " dead events");
            }
        }
    }

    private static void performRequest(final ApiCall apiCall, final Responder responder) {
        AsyncTask<String, Void, ApiEvent<?>> as = new AsyncTask<String, Void, ApiEvent<?>>() {
            @Override
            protected ApiEvent<?> doInBackground(String... dummy) {
                return performAndParseApiCall(apiCall);
            }

            @Override
            protected void onPostExecute(ApiEvent<?> result) {
                responder.setResult(result);
            }
        };

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) {
            as.execute();
        } else {
            as.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    protected static ApiEvent<?> performAndParseApiCall(ApiCall apiCall) {
        ApiEndpoint endpoint = apiCall.mEndpoint;
        final String url = endpoint.getUrl(apiCall.mSite, apiCall.mQuery);
        ApiEvent<?> event = endpoint.getEvent();
        event.setApiCall(apiCall);

        if (App.inDebug()) {
            Log.i(TAG, "Performing API call: " + endpoint.mMethod + " " + url);
            Log.i(TAG, "Request body: " + apiCall.mRequestBody);
        }

        try {
            ApiEvent<?> response = getResponse(url, event, apiCall);

            if (!response.hasError()) {
                response = parseResult(response, endpoint);
            }

            if (!response.hasError() && endpoint.mMethod.equals("GET") &&
                    !response.mStoredResponse) {
                storeResponse(url, apiCall, response.getResponse());
            }

            return response;
        } catch (HttpRequestException e) {
            Log.e(TAG, "API error", e);

            return event.setError(new ApiError(ApiError.Type.PARSE));
        }
    }

    private static ApiEvent<?> getResponse(String url, ApiEvent<?> event, ApiCall apiCall) {
        long startTime = System.currentTimeMillis();

        if (!App.get().isConnected()) {
            if (apiCall.mEndpoint.mMethod.equals("GET")) {
                String response = getStoredResponse(url, apiCall);
                if (response != null) {
                    if (App.inDebug()) {
                        Log.i(TAG, "Using stored API response");
                    }
                    // All GETs will be 200's if they're valid.
                    return event.setCode(200).setResponse(response).setStoredResponse(true);
                }
            }

            return event.setError(new ApiError(ApiError.Type.CONNECTION));
        }
        return event;
    }

        /**
         * Unfortunately we must split the creation of the HttpRequest
         * object and the appropriate actions to take for a GET vs. a POST
         * request. The request headers and trustAllCerts calls must be
         * made before any data is sent. However, we must have an HttpRequest
         * object already.
         */
//        HttpRequest request;

//        if (apiCall.mEndpoint.mMethod.equals("GET")) request = new com.umdcs4995.whiteboard.whiteboarddata.HttpRequest(url, "GET");
//        else {
//            /**
//             * For all methods other than get we actually perform a POST but send
//             * a header indicating the actual request we are performing. This is
//             * because http-request's underlying HTTPRequest library doesn't
//             * support PATCH requests.
//             */
//            request = HttpRequest.post(url);
//            request.header("X-HTTP-Method-Override", apiCall.mEndpoint.mMethod);
//        }

        /**
         * Send along the auth token if we have one.
         */
//        if (apiCall.mAuthToken != null) {
//            request.header("Authorization", "api " + apiCall.mAuthToken);
//        }
//
//        request.userAgent(App.get().getUserAgent());
//        request.header("X-App-Id", BuildConfig.APP_ID);
//        request.followRedirects(false);
//
//        /**
//         * Continue with constructing the request body.
//         */
//        if (apiCall.mFilePath != null) {
//            // POST the file if present.
//            request.send(new File(apiCall.mFilePath));
//        } else if (apiCall.mRequestBody != null) {
//            request.send(apiCall.mRequestBody);
//        }
//
//        /**
//         * The order is important here. If the code() is called first an IOException
//         * is thrown in some cases (invalid login for one, maybe more).
//         */
//        String responseBody = request.body();
//        int code = request.code();

//        if (App.inDebug()) {
//            long endTime = System.currentTimeMillis();
//
//            Log.d(TAG, "Response code: " + code);
//            Log.d(TAG, "Response body: " + responseBody);
//            Log.d(TAG, "Request time: " + (endTime - startTime) + "ms");
//        }

        /**
         * If the server responds with a 401, the user is logged out even though we
         * think that they are logged in. Return an Unauthorized event to prompt the
         * user to log in. Don't do this if we are logging in because the login dialog
         * will automatically handle these errors.
         */
//        if (code == INVALID_LOGIN_CODE && !App.get().isLoggingIn()) {
//            String newAuthToken = null;
//
//            // If mAuthToken is null that means that this is resulting from reauthenticating
//            // in which case the user's password has expired. Fall through to presenting
//            // a login dialog so the user can reenter credentials. Upon success, the account
//            // will be updated. If the user doesn't sign in then it will eventually be
//            // removed.
//            if (apiCall.mAuthToken != null) {
//                newAuthToken = attemptReauthentication(apiCall);
//            }
//
//            if (newAuthToken != null) {
//                // Try again with the new auth token.
//                apiCall.mAuthToken = newAuthToken;
//                return getResponse(url, event, apiCall);
//            } else {
//                return getUnauthorizedEvent(apiCall);
//            }
//        } else {
//            return event.setCode(code).setResponse(responseBody);
//        }
//    }

    /**
     * Attempts to reauthenticate the user with the stored credentials. Returns
     * a fresh authToken if successful, null otherwise.
     */
    private static String attemptReauthentication(ApiCall attemptedApiCall) {
        if (!attemptedApiCall.mSite.reauthenticateOnLogout()) {
            return null;
        }

        Authenticator authenticator = new Authenticator(App.get());
        authenticator.invalidateAuthToken(attemptedApiCall.mAuthToken);
        Account account = authenticator.getAccountForSite(attemptedApiCall.mSite);

        // We can't reauthenticate if the account doesn't exist.
        if (account == null) {
            return null;
        }

        String email = attemptedApiCall.mUser.mEmail;
        String password = authenticator.getPassword(account);

        ApiCall loginApiCall = ApiCall.login(email, password);
        loginApiCall.mSite = attemptedApiCall.mSite;
        ApiEvent<?> result = performAndParseApiCall(loginApiCall);

        if (result.hasError()) {
            Log.w(TAG, "Reauthentication failed");
            return null;
        }

        Object resultObject = result.getResult();
        if (resultObject instanceof User) {
            User user = (User)resultObject;

            // Don't notify because this is on a different thread and Otto fails.
            App.get().login(user, email, password, false);

            return user.getAuthToken();
        } else {
            Log.w(TAG, "Reauthentication result isn't a User");
            return null;
        }
    }

    private static String getStoredResponse(String url, ApiCall apiCall) {
        long startTime = System.currentTimeMillis();

        String response = FileCache.get(getCacheKey(url, apiCall.mUser));

        if (App.inDebug()) {
            long endTime = System.currentTimeMillis();
            Log.i(TAG, "Retrieved response in " + (endTime - startTime) + "ms");
        }

        return response;
    }

    private static void storeResponse(String url, ApiCall apiCall, String response) {
        long startTime = System.currentTimeMillis();

        FileCache.set(getCacheKey(url, apiCall.mUser), response);

        if (App.inDebug()) {
            long endTime = System.currentTimeMillis();
            Log.i(TAG, "Stored response in " + (endTime - startTime) + "ms");
        }
    }

    private static String getCacheKey(String url, User user) {
        String key = "api_responses_" + url;

        if (user != null) {
            key += "_" + user.getUserid();
        }

        return key;
    }
}
