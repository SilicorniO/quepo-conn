package com.silicornio.quepoconn;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by SilicorniO
 */
public class QPConnConfig {

    /** Identifier of connection **/
    protected String connId;

    /** Tag of connection **/
    protected String tag;

    /** Priority of execution, bigger = more priority **/
    protected int priority = 0;

    /** Method to use in connection: GET, POST, PUT, ... **/
    protected String method;

    /** Url to call **/
    protected String url;

    /** Map of headers to send **/
    protected Map<String, String> headers = new HashMap<>();

    /** Map of parameters to add to the url or send as POST **/
    protected Map<String, String> params = new HashMap<>();

    /** Data **/
    protected byte[] data;

    /** Data in a stream to not send all data **/
    protected OutputStream dataOutputStream;

    //listeners used by the manager to send events
    protected QPResponseBgListener responseBgListener;
    protected QPResponseListener responseListener;

    public String getConnId() {
        return connId;
    }

    public void setConnId(String connId) {
        this.connId = connId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public OutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public void setDataOutputStream(OutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }

    @Override
    public String toString() {
        return "QPConnConfig{" +
                "connId='" + connId + '\'' +
                ", tag='" + tag + '\'' +
                ", priority='" + priority + '\'' +
                ", url='" + url + '\'' +
                ", headers=" + headers +
                ", params=" + params +
                ", data=" + Arrays.toString(data) +
                ", dataOutputStream=" + dataOutputStream +
                '}';
    }

    //----- ADDITINAL METHODS -----

    /**
     * Generate a String with the tag and the identifier
     * @return String generated
     */
    public String toStringId(){
        return (tag!=null? tag : "") + " " + (connId!=null? connId : "");
    }
}
