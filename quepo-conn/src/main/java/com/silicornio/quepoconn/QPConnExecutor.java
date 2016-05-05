package com.silicornio.quepoconn;

import com.silicornio.quepoconn.general.QPL;
import com.silicornio.quepoconn.general.QPUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by SilicorniO
 */
public class QPConnExecutor{

    /** Size of the buffer to read data **/
    private static final int BUFFER_STREAM_READER_SIZE = 1024;

    /** URL connection executing **/
    private HttpURLConnection mUrlConnection = null;

    /** Request to execute **/
    private QPConnRequest mRequest;

    /** Listener to send events **/
    private QPConnExecutorListener mListener;

    /**
     * New instance
     * @param request QPRequest to execute
     * @param listener QPConnExecutorListener where to send events during execution
     */
    protected QPConnExecutor(QPConnRequest request, QPConnExecutorListener listener){
        mRequest = request;
        mListener = listener;
    }

    /**
     * Execute the connection of the task received
     * @return QPResponse generated with the exeution of the task
     */
    protected void execute(){

        //create the response to return
        QPConnResponse response = new QPConnResponse(mRequest);

        URL url = null;

        try {

            QPUtils.startCounter("execution");
            QPL.i("Connection start to: '" + mRequest.url + "'");

            //request
            url = new URL(mRequest.url);
            if(mRequest.url.startsWith("https")) {
                mUrlConnection = (HttpsURLConnection) url.openConnection();
                if(mRequest.sslSocketFactory!=null) {
                    ((HttpsURLConnection) mUrlConnection).setSSLSocketFactory(mRequest.sslSocketFactory);
                }
            }else{
                mUrlConnection = (HttpURLConnection) url.openConnection();
            }

            //set method
            writeMethod(mUrlConnection, mRequest.method);

            //set headers
            writeHeaders(mUrlConnection, mRequest.headers);

            //write data
            writeData(mUrlConnection, mRequest);

            //read code returned by the server
            readStatusCode(mUrlConnection, response);

            //read headers
            readHeaders(mUrlConnection, response);

            //read data received
            readData(mUrlConnection, response);

            QPL.i("Connection end in " + QPUtils.endCounter("execution") + " milliseconds");

        }catch(Exception e){
            QPL.e("Exception executing task " + mRequest.config.toStringId() + ": " + e.toString());
            response.error = "Error executing task: " + e.toString();
        }finally{
            if(mUrlConnection !=null) {
                mUrlConnection.disconnect();
                mUrlConnection = null;
            }
        }

        //return the response with all the data received to the manager, in the listener
        mListener.onExecutionEnd(this, response);
    }

    //----- CONFIGURATION -----

    //----- REQUEST -----

    /**
     * Write the method to the connection
     * @param urlConnection HttpURLConnection
     * @param method String method
     */
    private void writeMethod(HttpURLConnection urlConnection, String method){
        try {
            urlConnection.setRequestMethod(method);
        }catch(ProtocolException pe){
            QPL.e("Method '" + method + "' not allowed: " + pe.toString());
        }
    }

    /**
     * Write the list of headers to the connection
     * @param urlConnection HttpURLConnection
     * @param headers List<QPConnKeyValue> list of headers
     */
    private static void writeHeaders(HttpURLConnection urlConnection, List<QPConnKeyValue> headers){
        for(QPConnKeyValue entry : headers){
            urlConnection.setRequestProperty(entry.key, entry.value);
            QPL.i("Header sent '" + entry.key + "' = '" + entry.value + "'");
        }
    }

    /**
     * Write data from request into the connection if it is necessary
     * @param urlConnection HttpURLConnection
     * @param request QPConnRequest with data
     */
    private static void writeData(HttpURLConnection urlConnection, QPConnRequest request){


        try {

            if (request.dataStream != null) {
                QPL.i("Sending data from inputstream");

                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);

                //read from inputstream and set to outputstream directly
                OutputStream os = new BufferedOutputStream(urlConnection.getOutputStream());
                byte[] data = new byte[BUFFER_STREAM_READER_SIZE];
                while (request.dataStream.read(data, 0, BUFFER_STREAM_READER_SIZE) != -1) {
                    os.write(data);
                }
                os.flush();
                os.close();

            } else if (request.data != null){

                QPL.i("Sending data: " + String.valueOf(request.data));

                urlConnection.setDoOutput(true);

                OutputStream os = new BufferedOutputStream(urlConnection.getOutputStream());
                os.write(request.data);
                os.flush();
                os.close();
            }


        }catch (IOException ioe){
            QPL.e("Exception sending data: " + ioe.toString());
        }

    }

    //----- RESPONSE -----

    /**
     * Read the status code from connectiong and save it in response
     * @param urlConnection HttpURLConnection
     * @param response QPConnResponse where to set the status code
     */
    private static void readStatusCode(HttpURLConnection urlConnection, QPConnResponse response){
        try {
            response.statusCode = urlConnection.getResponseCode();
            QPL.i("Status code: " + response.statusCode);
        }catch(IOException ioe){
            QPL.e("Exception getting status code: " + ioe.toString());
        }
    }

    /**
     * Read the headers from the connection
     * @param urlConnection HttpURLConnection
     * @param response QPConnResponse where to save the headers
     */
    private static void readHeaders(HttpURLConnection urlConnection, QPConnResponse response){
        for(Map.Entry<String, List<String>> entry : urlConnection.getHeaderFields().entrySet()){
            if(entry.getValue().size()>0) {
                response.headers.add(new QPConnKeyValue(entry.getKey(), entry.getValue().get(0)));
                QPL.i("Header received '" + entry.getKey() + "' = '" + entry.getValue().get(0) + "'");
            }
        }
    }

    /**
     * Read data from connection to the response
     * @param urlConnection HttpURLConnection
     * @param response QPConnResponse where to save the data
     */
    private static void readData(HttpURLConnection urlConnection, QPConnResponse response){
        QPL.i("Receiving data");
        try {
            InputStream is = new BufferedInputStream(urlConnection.getInputStream());
            readStream(is, response);
        }catch(IOException ioe){
            QPL.e("Exception receiving data: " + ioe.toString());
        }
    }

    /**
     * Write the stream to the connection
     * @param is InputStream to read data
     * @param response QPRequest to get data
     */
    private static void readStream(InputStream is, QPConnResponse response){
        try {

            //prepare variables to read data
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int length;
            byte[] data = new byte[BUFFER_STREAM_READER_SIZE];

            //read data
            while ((length = is.read(data, 0, BUFFER_STREAM_READER_SIZE)) != -1) {
                buffer.write(data, 0, length);
            }
            buffer.flush();
            is.close();

            //save data into the response
            response.data = buffer.toByteArray();

            QPL.i("Data length: " + response.data.length + " bytes");

        }catch(Exception e){
            QPL.e("Exception receiving data " + response.config.toStringId()  + ": " + e.toString());
        }
    }

    //----- ADDITIONAL ACTIONS -----

    /**
     * Stop the actual execution
     */
    public void stopExecution(){
        if(mUrlConnection!=null){
            mUrlConnection.disconnect();
        }
    }

    /**
     * Destroy all instances and stop connection
     */
    public void destroy(){
        stopExecution();
    }

    //----- LISTENER -----

    protected interface QPConnExecutorListener{

        /**
         * Called when the execution is finished
         * @param executor QPConnExecutor this
         * @param response response with all data
         */
        void onExecutionEnd(QPConnExecutor executor, QPConnResponse response);
    }
}
