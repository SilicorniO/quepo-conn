package com.silicornio.quepoconn;

import com.silicornio.quepoconn.general.QPL;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocketFactory;

/**
 * Created by SilicorniO
 */
public class QPConnConfig {

    //available formats of the response
    public static final String FORMAT_JSON = "JSON";

    /** Name of the service **/
    protected String name;

    /** Identifier of connection **/
    protected String connId;

    /** Tag of connection **/
    protected Object tag;

    /** Priority of execution, bigger = more priority **/
    protected int priority = 0;

    /** Method to use in connection: GET, POST, PUT, ... **/
    protected String method;

    /** Url to call **/
    protected String url;

    /** Map of headers to send **/
    protected List<QPConnKeyValue> headers = new ArrayList<>();

    /** Map of parameters to add to the url or send as POST **/
    protected List<QPConnKeyValue> params = new ArrayList<>();

    /** List of values to apply to parameters of the connection configuration **/
    protected List<QPConnKeyValue> values = new ArrayList<>();

    /** Data in text format **/
    protected String textData;

    /** Data **/
    protected byte[] data;

    /** Data in a stream to not send all data **/
    protected InputStream dataOutputStream;

    /** Format of response **/
    protected String responseFormat;

    /** Name of the object of translator for the response **/
    protected String responseTranslatorObject;

    /** Format of the request **/
    protected String requestFormat;

    /** Name of the object of translator for the request **/
    protected String requestTranslatorObject;

    /** Objects to send in the format of the request **/
    protected Object[] requestObjects;

    /** SSL Socket Factory associated to this request **/
    protected SSLSocketFactory sslSocketFactory;

    //listeners used by the manager to send events
    protected QPResponseBgListener responseBgListener;
    protected QPResponseListener responseListener;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConnId() {
        return connId;
    }

    public void setConnId(String connId) {
        this.connId = connId;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
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

    public List<QPConnKeyValue> getHeaders() {
        return headers;
    }

    public List<QPConnKeyValue> getParams() {
        return params;
    }

    public List<QPConnKeyValue> getValues() {
        return values;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getTextData() {
        return textData;
    }

    public void setTextData(String textData) {
        this.textData = textData;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public InputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public void setDataOutputStream(InputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }

    public String getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(String responseFormat) {
        this.responseFormat = responseFormat;
    }

    public String getResponseTranslatorObject() {
        return responseTranslatorObject;
    }

    public void setResponseTranslatorObject(String responseTranslatorObject) {
        this.responseTranslatorObject = responseTranslatorObject;
    }

    public String getRequestFormat() {
        return requestFormat;
    }

    public void setRequestFormat(String requestFormat) {
        this.requestFormat = requestFormat;
    }

    public String getRequestTranslatorObject() {
        return requestTranslatorObject;
    }

    public void setRequestTranslatorObject(String requestTranslatorObject) {
        this.requestTranslatorObject = requestTranslatorObject;
    }

    public Object[] getRequestObjects() {
        return requestObjects;
    }

    public void setRequestObjects(Object[] requestObjects) {
        this.requestObjects = requestObjects;
    }

    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    public QPConnConfig clone(){
        QPConnConfig config = new QPConnConfig();
        config.name = name;
        config.connId = connId;
        config.tag = tag;
        config.priority = priority;
        config.method = method;
        config.url = url;
        config.textData = textData;
        config.headers = cloneKeyValues(headers);
        config.params = cloneKeyValues(params);
        config.values = cloneKeyValues(values);
        config.responseFormat = responseFormat;
        config.responseTranslatorObject = responseTranslatorObject;
        config.requestFormat = requestFormat;
        config.requestTranslatorObject = requestTranslatorObject;

        return config;
    }

    /**
     * Clone the list of keyvalues received
     * @param listItems List<QPConnKeyValue> to clone
     * @return List<QPConnKeyValue> with all values cloned
     */
    private static List<QPConnKeyValue> cloneKeyValues(List<QPConnKeyValue> listItems){
        List<QPConnKeyValue> listClone = new ArrayList<>();
        for(QPConnKeyValue item : listItems){
            listClone.add(item.clone());
        }
        return listClone;
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

    /**
     * Apply the configuration values
     * @param configuration QPConnfiguration to apply
     */
    protected void applyConfiguration(QPConnConfiguration configuration){

        //check configuration is not null
        if(configuration==null){
            return;
        }

        //apply configuration
        mergeListKeyValues(values, configuration.values);
        mergeListKeyValues(headers, configuration.headers);

        if(requestFormat==null){
            requestFormat = configuration.requestFormat;
        }
        if(responseFormat==null){
            responseFormat = configuration.responseFormat;
        }
    }

    /**
     * Merge the lists received
     * @param list1 List<QPConnKeyValue> where to add new values
     * @param list2 List<QPConnKeyValue> where to get the values to add
     */
    private static void mergeListKeyValues(List<QPConnKeyValue> list1, List<QPConnKeyValue> list2){
        if(list1 == null){
            list1 = new ArrayList<>();
        }
        if(list2!=null) {
            for (QPConnKeyValue keyValue : list2) {
                if (!list1.contains(keyValue.value)) {
                    list1.add(keyValue);
                }
            }
        }
    }

    /**
     * Translate the values into all possible fields
     */
    protected void translateValues(){


        //translate url
        url = translateText(url, values);

        //translate headers
        if(headers!=null) {
            for (QPConnKeyValue keyValue : headers) {
                keyValue.key = translateText(keyValue.key, values);
                keyValue.value = translateText(keyValue.value, values);
            }
        }

        //translate params
        if(params!=null) {
            for (QPConnKeyValue keyValue : params) {
                keyValue.key = translateText(keyValue.key, values);
                keyValue.value = translateText(keyValue.value, values);
            }
        }

        //translate text data
        if(textData!=null){
            textData = translateText(textData, values);
        }

    }

    /**
     * Translate the text using the list of keys received
     * @param text String text to translate
     * @param listValues List<QPConnKeyValue> with values to translate
     * @return String translated or the same received
     */
    private static String translateText(String text, List<QPConnKeyValue> listValues){

        if(text!=null){
            Pattern pattern = Pattern.compile("\\{@.[^}]*\\}");
            Matcher matcher = pattern.matcher(text);
            while(matcher.find()){
                String key = matcher.group(0);
                int indexKey = listValues.indexOf(new QPConnKeyValue(key.substring(2, key.length()-1), null));
                if(indexKey!=-1){
                    if(listValues.get(indexKey).value!=null) {
                        text = text.replace(key, listValues.get(indexKey).value);
                    }else{
                        QPL.e("Value of '" + key.substring(2, key.length()-1) + "' is null");
                    }
                }else{
                    QPL.i("The attribute '" + key.substring(2, key.length()-1) + "' was not translated because there isn't a value for it");
                }
            }
        }

        //return same text
        return text;
    }
}
