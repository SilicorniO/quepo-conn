package com.silicornio.quepoconn;

import android.net.Uri;

import com.silicornio.quepoconn.general.QPL;
import com.silicornio.quepotranslator.QPTransManager;
import com.silicornio.quepotranslator.QPTransUtils;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by SilicorniO
 */
public class QPConnRequest {

    /** Configuration of request **/
    protected QPConnConfig config;

    /** Complete URL to call **/
    protected String url;

    /** Method of the request **/
    protected String method;

    /** Headers to send with the connection **/
    protected List<QPConnKeyValue> headers;

    /** Data to send **/
    protected byte[] data;

    /** Data but received as stream **/
    protected InputStream dataStream;

    protected QPConnRequest(QPConnConfig config){
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

        //prepare method
        method = config.method;

        //prepare data
        if(config.data!=null) {
            data = config.data;
        }else if(config.textData!=null){
            data = config.textData.getBytes(Charset.defaultCharset());
        }
        dataStream = config.dataOutputStream;

        return true;
    }

    /**
     * This try to translate the objects received in a format to send
     * @param transManager QPTransManager translator manager to use
     * @param avoidClasses Class[] array of classes to avoid of translations
     */
    protected void translateValues(QPTransManager transManager, Class[] avoidClasses){

        //if translator is null or response configuration was not filled we dont translate
        if(transManager==null || config.requestFormat==null || config.requestTranslatorObject==null || config.requestObjects==null){
            return;
        }

        //convert objects to a map
        Map<String, Object> mapObjects = QPTransUtils.convertObjectToMapInversion(config.requestObjects, avoidClasses);
        Map<String, Object> mapInverse = transManager.translateInverse(mapObjects, config.requestTranslatorObject);

        //check map is right
        if(mapInverse==null){
            QPL.e("Error generating inverse map, check errors");
            return;
        }

        //JSON
        if(QPConnConfig.FORMAT_JSON.equalsIgnoreCase(config.responseFormat)){

            //translate the data received as text
            String jsonMapInverse = QPTransUtils.convertMapToJSON(mapInverse);
            if(jsonMapInverse!=null){
                data = jsonMapInverse.getBytes();
            }else{
                QPL.e("JSON not generated, check errors");
            }
        }else{
            QPL.e("Format request'" + config.requestFormat + "' incorrect");
        }

    }

    /**
     * Prepare URL to call
     * @return boolean TRUE if everything is OK, FALSE if something wrong was detected
     */
    private boolean prepareUrl(){

        Uri.Builder builder = new Uri.Builder();

        //prepare params if it has one or more
        for(QPConnKeyValue entry : config.params){
            builder.appendQueryParameter(entry.key, entry.value);
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
