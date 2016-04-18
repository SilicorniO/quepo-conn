package com.silicornio.quepoconn;

/**
 * Created by SilicorniO
 */
public interface QPResponseBgListener{

    /**
     * Called when response is generated from the same thread than the connection
     * @param response QPResponse with all received data
     * @param config QPConnConfig used to call to the connection
     */
    void responseOnBackground(QPResponse response, QPConnConfig config);

}
