package com.silicornio.quepoconn;

import android.net.Uri;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by SilicorniO
 */
public class QPRequest {

    /** Configuration of request **/
    protected QPConnConfig config;

    /** Complete URL to call **/
    protected String url;

    /** Headers to send with the connection **/
    protected Map<String, String> headers;

    /** Data to send **/
    protected byte[] data;

    /** Data but received as stream **/
    protected OutputStream dataStream;

    protected QPRequest(QPConnConfig config){
        this.config = config;
    }

    /**
     * Prepare data before executing the request transforming config to final values
     * @return boolean TRUE if everything is OK, FALSE if something wrong was detected
     */
    protected boolean prepare(){

        //prepare url
        if(!prepareUrl()){
            return false;
        }

        //prepare headers
        headers = config.headers;

        //prepare data
        data = config.data;
        dataStream = config.dataOutputStream;

        return true;
    }

    /**
     * Prepare URL to call
     * @return boolean TRUE if everything is OK, FALSE if something wrong was detected
     */
    private boolean prepareUrl(){

        Uri.Builder builder = new Uri.Builder();

        //prepare params if it has one or more
        for(Map.Entry<String, String> entry : config.params.entrySet()){
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }

        //create the url
        url = config.url + builder.build().toString();

        return true;
    }

    @Override
    public String toString() {
        return "QPRequest{" +
                "config=" + config +
                ", url='" + url + '\'' +
                ", headers=" + headers +
                ", data=" + Arrays.toString(data) +
                ", dataStream=" + dataStream +
                '}';
    }

}
