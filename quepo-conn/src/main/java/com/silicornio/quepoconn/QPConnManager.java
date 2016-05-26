package com.silicornio.quepoconn;

import android.os.Handler;
import android.os.Message;

import com.silicornio.quepotranslator.QPTransManager;

import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

/**
 * Created by SilicorniO
 */
public class QPConnManager {

    /** List of executors **/
    private Map<QPConnExecutor, Thread> mExecutors = new HashMap<>();

    /** Maximum number of executors at same time **/
    private int mNumMaxExecutors = 4;

    /** Queue of configs to execute **/
    private QPConnQueue mQueue = new QPConnQueue();

    /** Handler to send events from the main thread **/
    private MainHandler mMainHandler = new MainHandler();

    /** Configuration of the connection manager **/
    private QPConnConf mConf;

    /** Translator manager to use if we want to translate objects **/
    private QPTransManager mTransManager;

    /** Array of classes to avoid in translator manager **/
    private Class[] mTransAvoidClasses;

    /** SSL Socket Factory associated to this manager **/
    protected SSLSocketFactory sslSocketFactory;

    /** Flag to know if converting object it is necessary to send null values **/
    private boolean mSerializeNull = false;

    public QPConnManager(){

    }

    public QPConnManager(QPConnConf conf){
        mConf = conf;
    }

    /**
     * Set the translator manager to apply
     * @param transManager QPTransManager
     * @param transAvoidClasses Class[] array of classes to avoid from translations
     */
    public void seTranslatorManager(QPTransManager transManager, Class[] transAvoidClasses) {
        seTranslatorManager(transManager, transAvoidClasses, false);
    }

    /**
     * Set the translator manager to apply
     * @param transManager QPTransManager
     * @param transAvoidClasses Class[] array of classes to avoid from translations
     * @param serializeNull boolean TRUE to show null values, FALSE to hide it
     */
    public void seTranslatorManager(QPTransManager transManager, Class[] transAvoidClasses, boolean serializeNull) {
        mTransManager = transManager;
        mTransAvoidClasses = transAvoidClasses;
        mSerializeNull = serializeNull;
        if(mTransManager!=null){
            mTransManager.setTranslateNullElements(serializeNull);
        }
    }

    /**
     * Set the SSL Socket Factory to use with connections
     * @param sslSocketFactory SSLSocketFactory
     */
    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    /**
     * Set the maximum number of executor at the same time
     * @param numMaxExecutors int maximum number, it must be bigger than 0
     */
    public void setNumMaxExecutors(int numMaxExecutors){
        if(numMaxExecutors>0) {
            mNumMaxExecutors = numMaxExecutors;
        }
    }

    //----- CONNECTIONS -----

    /**
     * Add a connection
     * @param connConfig QPConnConfig
     * @param responseBgListener QPResponseBgListener to call from background
     * @param responseListener QPResponseListener to call from main thread
     */
    public void addConn(QPConnConfig connConfig, QPResponseBgListener responseBgListener, QPResponseListener responseListener){

        //add responses to the config
        connConfig.responseBgListener = responseBgListener;
        connConfig.responseListener = responseListener;

        //add the config to the queue
        mQueue.push(connConfig);

        //try to execute next
        executeNextConn();

    }

    /**
     * Execute the next connection in the queue
     * @return boolean TRUE if another executor was started, FALSE if no config to execute
     *                  or no space for more executors
     */
    private boolean executeNextConn(){

        //check if it is possible to add new executors
        if(mExecutors.size()>= mNumMaxExecutors){
            return false;
        }

        //get the next connection config to execute
        QPConnConfig configToExecute = mQueue.popFirst();
        if(configToExecute==null){
            return false;
        }

        //translate values, prepared for executing
        configToExecute.translateValues();

        //generate the request
        final QPConnRequest request = new QPConnRequest(configToExecute);
        if(!request.prepare(sslSocketFactory)){
            return false;
        }

        //create the executor
        final QPConnExecutor executor = new QPConnExecutor(request, mExecutorListener);

        //create the thread
        Thread threadExecutor = new Thread(){
            @Override
            public void run() {

                //translate objects if necessary
                request.translateValues(mTransManager, mTransAvoidClasses, mSerializeNull);

                //execute
                executor.execute();
            }
        };

        //add the executor with the thread to the map and execute
        mExecutors.put(executor, threadExecutor);
        threadExecutor.start();

        return true;
    }

    /**
     * Called when a execution finishes
     */
    private QPConnExecutor.QPConnExecutorListener mExecutorListener = new QPConnExecutor.QPConnExecutorListener(){
        @Override
        public void onExecutionEnd(QPConnExecutor executor, QPConnResponse response) {

            //remove the executor from the list
            if(mExecutors!=null) {
                mExecutors.remove(executor);

                //translate values if necessary
                response.translateValues(mTransManager);

                //call to the listener in background if this connection has one configured
                if(response.config.responseBgListener!=null){
                    response.config.responseBgListener.responseOnBackground(response, response.config);
                }

                //if config has a main listener configured we call to the listener from the main thread calling to the handler
                if(response.config.responseListener!=null) {
                    Message msg = new Message();
                    msg.obj = response;
                    mMainHandler.sendMessage(msg);
                }
            }

        }
    };

    /**
     * Handler used to send events over the main thread
     */
    private static class MainHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            //get the response
            QPConnResponse response = (QPConnResponse) msg.obj;

            //call to the listener (it was checked before if it was null or not)
            response.config.responseListener.responseOnMainThread(response, response.config);
        }
    }


    /**
     * Destroy everything stopping all executions
     */
    public void destroy(){

        //destroy queue
        mQueue.destroy();
        mQueue = null;

        //destroy executors
        for(QPConnExecutor executor : mExecutors.keySet()){
            executor.destroy();
        }
        mExecutors.clear();
        mExecutors = null;
    }

}
