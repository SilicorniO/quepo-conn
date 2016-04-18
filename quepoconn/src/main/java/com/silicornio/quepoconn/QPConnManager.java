package com.silicornio.quepoconn;

import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SilicorniO
 */
public class QPConnManager {

    /** List of executors **/
    private List<QPConnExecutor> mExecutors = new ArrayList<>();

    /** Maximum number of executors at same time **/
    private int numMaxExecutors = 4;

    /** Queue of configs to execute **/
    private QPConnQueue mQueue = new QPConnQueue();

    /** Handler to send events from the main thread **/
    private MainHandler mMainHandler = new MainHandler();

    public QPConnManager(){

    }

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
        if(mExecutors.size()>=numMaxExecutors){
            return false;
        }

        //get the next connection config to execute
        QPConnConfig configToExecute = mQueue.popFirst();
        if(configToExecute==null){
            return false;
        }

        //generate the request
        QPRequest request = new QPRequest(configToExecute);
        if(!request.prepare()){
            return false;
        }

        //execute
        QPConnExecutor executor = new QPConnExecutor(request, mExecutorListener);
        mExecutors.add(executor);
        executor.start();

        return true;
    }

    /**
     * Called when a execution finishes
     */
    private QPConnExecutor.QPConnExecutorListener mExecutorListener = new QPConnExecutor.QPConnExecutorListener(){
        @Override
        public void onExecutionEnd(QPConnExecutor executor, QPResponse response) {

            //remove the executor from the list
            if(mExecutors!=null) {
                mExecutors.remove(executor);

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
            QPResponse response = (QPResponse) msg.obj;

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
        for(QPConnExecutor executor : mExecutors){
            executor.destroy();
        }
        mExecutors.clear();
        mExecutors = null;
    }

}
