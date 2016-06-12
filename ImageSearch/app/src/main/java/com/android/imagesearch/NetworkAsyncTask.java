package com.android.imagesearch;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by nishant pundir on 2/6/2016.
 */

public class NetworkAsyncTask extends AsyncTask<Void, Void, JSONObject> {

    AsyncCommunicator comm;
    private JSONObject request;
    private String progressMessage;
    private Context context;
    private String link;
    private ProgressDialog progressDialog;


    public NetworkAsyncTask(Context context, JSONObject request, String progressMessage, String link, AsyncCommunicator comm)
    {
        this.comm=comm;
        this.request = request;
        this.progressMessage = progressMessage;
        this.context=context;
        this.link = link;
        progressDialog = new ProgressDialog(context);
    }


    @Override
    protected void onPreExecute() {

    }

    @Override
    protected JSONObject doInBackground(Void... arg0) {
        // TODO Auto-generated method stub

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if(!isConnected)
        {
            JSONObject response = new JSONObject();
            try {
                response.put("success","0");
                response.put("message","No internet Connection , App will not perform normally");
            } catch (JSONException e) {
                e.printStackTrace();
            }



            return response;
        }
        else
        {
            HttpConnectionManager connection = new HttpConnectionManager();
            JSONObject response = null;
            try {
                response = connection.connectAndGetData(link,"");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;

        }

    }
    @Override
    protected void onPostExecute(JSONObject response) {
        progressDialog.dismiss();

        comm.AsyncEvent(response,"RESPONSE");
    }

}

