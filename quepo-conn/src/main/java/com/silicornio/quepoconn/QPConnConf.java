package com.silicornio.quepoconn;

import com.silicornio.quepoconn.general.QPL;

import java.util.List;

/**
 * Created by SilicorniO
 */
public class QPConnConf {

    /** Configuration **/
    protected QPConnConfiguration configuration;

    /** Services **/
    protected List<QPConnConfig> services;

    //----- ADDITIONAL METHODS -----

    /**
     *
     * @param name String name of the service
     * @return QPConnConfig Configuration cloned instance
     */
    public QPConnConfig getService(String name){

        //check name not null
        if(name==null){
            throw new IllegalArgumentException("Name cannot be null");
        }

        //check there are services
        if(services==null || services.size()==0){
            QPL.e("Configuration has not services loaded");
            return null;
        }

        //search the service in the list
        for(QPConnConfig service : services){
            if(name.equals(service.getName())){

                //clone the service
                QPConnConfig serviceClone = service.clone();

                //apply configuration to config
                serviceClone.applyConfiguration(configuration);

                return serviceClone;
            }
        }

        //not found
        return null;
    }

}
