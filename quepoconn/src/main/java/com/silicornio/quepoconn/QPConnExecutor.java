package com.silicornio.quepoconn;

import com.silicornio.quepoconn.general.QPL;
import com.silicornio.quepoconn.general.QPUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by SilicorniO
 */
public class QPConnExecutor extends Thread{

    /** Size of the buffer to read data **/
    private static final int BUFFER_STREAM_READER_SIZE = 1024;

    /** URL connection executing **/
    private HttpURLConnection mUrlConnection = null;

    /** Request to execute **/
    private QPRequest mRequest;

    /** Listener to send events **/
    private QPConnExecutorListener mListener;

    /**
     * New instance
     * @param request QPRequest to execute
     * @param listener QPConnExecutorListener where to send events during execution
     */
    protected QPConnExecutor(QPRequest request, QPConnExecutorListener listener){
        mRequest = request;
        mListener = listener;
    }

    @Override
    public void run() {
        execute();
    }

    /**
     * Execute the connection of the task received
     * @return QPResponse generated with the exeution of the task
     */
    private void execute(){

        //create the response to return
        QPResponse response = new QPResponse(mRequest);

        URL url = null;

        try {

            QPUtils.startCounter("execution");
            QPL.i("Connection start to: '" + mRequest.url + "'");

            //request
            url = new URL(mRequest.url);
            mUrlConnection = (HttpURLConnection) url.openConnection();

            if(mRequest.data!=null || mRequest.dataStream!=null) {
                QPL.i("Sending data");
                mUrlConnection.setDoOutput(true);
                mUrlConnection.setChunkedStreamingMode(0);

                OutputStream os = new BufferedOutputStream(mUrlConnection.getOutputStream());
                writeStream(os, mRequest);
            }

            //read code returned by the server
            response.statusCode = mUrlConnection.getResponseCode();
            QPL.i("Status code: " + response.statusCode);

            //read headers
            for(Map.Entry<String, List<String>> entry : mUrlConnection.getHeaderFields().entrySet()){
                if(entry.getValue().size()>0) {
                    response.headers.put(entry.getKey(), entry.getValue().get(0));
                    QPL.i("Header received '" + entry.getKey() + "' = '" + entry.getValue().get(0) + "'");
                }
            }

            //read data received
            QPL.i("Receiving data");
            InputStream is = new BufferedInputStream(mUrlConnection.getInputStream());
            readStream(is, response);

            QPL.i("Connection end in " + QPUtils.endCounter("execution") + " milliseconds");

        }catch(Exception e){
            QPL.e("Exception executing task " + mRequest.config.toStringId() + ": " + e.toString());
            e.printStackTrace();
        }finally{
            if(mUrlConnection !=null) {
                mUrlConnection.disconnect();
                mUrlConnection = null;
            }
        }

        //return the response with all the data received to the manager, in the listener
        mListener.onExecutionEnd(this, response);
    }

    /**
     * Write the stream to the connection
     * @param os OutputStream where to send data
     * @param request QPRequest to get data
     */
    private void writeStream(OutputStream os, QPRequest request){

        try {
            if (request.data != null) {

            } else {
                os.write(request.data);
            }
        }catch(Exception e){
            QPL.e("Exception sending data " + request.config.toStringId()  + ": " + e.toString());
        }

    }

    /**
     * Write the stream to the connection
     * @param is InputStream to read data
     * @param response QPRequest to get data
     */
    private void readStream(InputStream is, QPResponse response){
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

            //save data into the response
            response.data = buffer.toByteArray();

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
        void onExecutionEnd(QPConnExecutor executor, QPResponse response);
    }
}
