package com.android.imagesearch;

import org.json.JSONObject;

/**
 * Created by nishant pundir on 2/5/2016.
 */
public interface AsyncCommunicator {
    public void AsyncEvent(JSONObject value, String taskid);
}
