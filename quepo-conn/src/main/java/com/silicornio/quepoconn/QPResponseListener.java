package com.silicornio.quepoconn;

/**
 * Created by SilicorniO
 */
public interface QPResponseListener {

    /**
     * Called when response is generated from the main thread
     * @param response QPResponse with all received data
     * @param config QPConnConfig used to call to the connection
     */
    void responseOnMainThread(QPResponse response, QPConnConfig config);

}
