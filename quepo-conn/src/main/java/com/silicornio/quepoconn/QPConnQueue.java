package com.silicornio.quepoconn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by SilicorniO.
 */
public class QPConnQueue {

    /** List of elements **/
    private List<QPConnConfig> mQueue = new ArrayList<>();

    /**
     * Add an element to the list
     * @param config QPConnConfig to add
     */
    public void push(QPConnConfig config){

        synchronized (mQueue) {
            mQueue.add(config);
            orderList();
        }

    }

    /**
     * Remove the element from the queue
     * @param config QPConnConfig
     */
    public void remove(QPConnConfig config){
        synchronized (mQueue) {
            mQueue.remove(config);
        }
    }

    /**
     * Get the first element from the list and remove it
     * @return QPConnConfig in first position or null if queue is empty
     */
    public QPConnConfig popFirst(){
        if(mQueue.isEmpty()){
            return null;
        }else{
            QPConnConfig config;
            synchronized (mQueue) {
                config = mQueue.get(0);
                mQueue.remove(0);
            }
            return config;
        }
    }

    /**
     * Order the elements of the list
     */
    private void orderList(){
        Collections.sort(mQueue, new Comparator<QPConnConfig>() {
            @Override
            public int compare(QPConnConfig config1, QPConnConfig config2) {
                return config2.priority - config1.priority;
            }
        });
    }

    /**
     * Search all config with the tag received or the connId received or both if both values are filled
     * @param tag String tag used for the connection, can be null for not use this value
     * @param connId String identifier of connection, can be null for not use this value
     * @return List of config found
     */
    public List<QPConnConfig> search(String tag, String connId){

        //generate the list
        List<QPConnConfig> configs = new ArrayList<>();

        //search
        if(tag!=null && connId==null){
            for(QPConnConfig config : mQueue){
                if(tag.equals(config.tag)){
                    configs.add(config);
                }
            }

        }else if(tag==null && connId!=null){
            for(QPConnConfig config : mQueue){
                if(connId.equals(config.connId)){
                    configs.add(config);
                }
            }

        }else if(tag!=null && connId!=null){
            for(QPConnConfig config : mQueue){
                if(tag.equals(config.tag) && connId.equals(config.connId)){
                    configs.add(config);
                }
            }
        }

        //return the list
        return configs;
    }

    /**
     * Destroy the queue removing all data and removing values
     */
    public void destroy(){
        mQueue.clear();
        mQueue = null;
    }

}
