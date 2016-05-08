package com.silicornio.quepoconn;

import com.silicornio.quepoconn.general.QPL;
import com.silicornio.quepotranslator.QPTransManager;
import com.silicornio.quepotranslator.QPTransResponse;
import com.silicornio.quepotranslator.QPTransUtils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by SilicorniO
 */
public class QPConnResponse {

    /** Configuration received of the task **/
    protected QPConnConfig config;

    /** Translation response if was applied **/
    protected QPTransResponse transResponse;

    /** Response code returned by the server **/
    protected int statusCode;

    /** Headers received **/
    protected List<QPConnKeyValue> headers = new ArrayList();

    /** Data received **/
    protected byte[] data;

    /** Error received **/
    protected String error;

    protected QPConnResponse(QPConnRequest request){
        this.config = request.config;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public List<QPConnKeyValue> getHeaders() {
        return headers;
    }

    public QPConnConfig getConfig() {
        return config;
    }

    public QPTransResponse getTransResponse() {
        return transResponse;
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

    //----- ADDITIONAL METHODS -----

    /**
     * Called when the data is stored in the response object
     * This try to translate the values received in objects if it wwas configured
     */
    protected void translateValues(QPTransManager transManager){

        //if translator is null or response configuration was not filled we dont translate
        if(transManager==null || config.responseFormat==null || config.responseTranslatorObject==null){
            return;
        }

        //map of data received
        Map<String, Object> mapData = null;

        //JSON
        if(QPConnConfig.FORMAT_JSON.equalsIgnoreCase(config.responseFormat)){

            //translate the data received as text
            if(data!=null) {
                mapData = QPTransUtils.convertJSONToMap(new ByteArrayInputStream(data));
            }else{
                QPL.i("Data no received so we can't translate to objects");
            }

        }else{
            QPL.e("Format response '" + config.responseFormat + "' incorrect");
        }

        //translate
        transResponse = transManager.translate(mapData, config.responseTranslatorObject);

    }


}
