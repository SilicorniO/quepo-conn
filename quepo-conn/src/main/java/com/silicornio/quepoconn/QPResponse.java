package com.silicornio.quepoconn;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SilicorniO
 */
public class QPResponse {

    /** Configuration received of the task **/
    protected QPConnConfig config;

    /** Response code returned by the server **/
    protected int statusCode;

    /** Headers received **/
    protected Map<String, String> headers = new HashMap<>();

    /** Data received **/
    protected byte[] data;

    /** Error received **/
    protected String error;

    protected QPResponse(QPRequest request){
        this.config = request.config;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "QPResponse{" +
                "config=" + config +
                ", statusCode=" + statusCode +
                ", headers=" + headers +
                ", data length=" + (data!=null? data.length : 0) +
                ", error=" + error +
                '}';
    }
}
