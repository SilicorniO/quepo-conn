# quepo-conn
Connection library for Android. This library allows to create and launch connections easier. It is integrated with "quepo-translator" library helping to translate data received into Objects of the model.

## Features
 * Execute HTTP connections with HttpURLConnection
 * Configuration of services with a file
 * Integration of quepo-translator

##Installation

You can find the latest version of the library on jCenter repository.

### For Gradle users

In your `build.gradle` you should declare the jCenter repository into `repositories` section:
```gradle
   repositories {
       jcenter()
   }
```
Include the library as dependency:
```gradle
compile 'com.silicornio:quepo-conn:1.2.0'
```

### For Maven users
```maven
<dependency>
  <groupId>com.silicornio</groupId>
  <artifactId>quepo-conn</artifactId>
  <version>1.2.0</version>
  <type>pom</type>
</dependency>
```

##Usage

1. Create a services configuration file:

	You can store it in the assets to get it easier with QPUtils in next steps.

    ```json
		{
			"configuration": {
				"values": [
					{
						"key": "url",
						"value": "http://silicornio.com/quepo-conn"
					}
				],
				"headers": [
					{
						"key": "headerExample",
						"value": "headerValue"
					}
				],
				"responseFormat": "JSON",
				"requestFormat": "JSON",
				"numMaxExecutors" 4
			},
			"services": [
				{
					"name": "serviceExample",
					"url": "{@url}/exampleJSON",
					"method": "POST",
					"priority": 0,
					"requestTranslatorObject": "ObjectRequest",
					"responseTranslatorObject": "ObjectResponse",
					"textData": "data sending"
				}
			]
		}	
    ```
      
  * configuration.values - Array of Key and value objects including values to change in the services fields: url, headers and textData. Values can be included directly in a service too.
  * configuration.headers - Default headers to add to connections. Array of Key and Value objects.
  * configuration.responseFormat - Format to use in quepo-translator in responses by default. Can be changed and included directly in a service too.
  * configuration.requestFormat - Format to use in quepo-translator in requests by default. Can be changed and included directly in a service too.
  * configuration.numMaxExecutors - Maximum number of connections at same time. [not implemented in 1.0.0]
  * services - Array of services. They are equivalent to launcher configurations.
  * service.name - Name of the service, used to get it by code.
  * service.url - URL to call when service is launched. Can contains values to be translated.
  * service.method - Method to set: GET, POST, PUT, DELETE, ...
  * service.priority - Priority of the service when we launch it. If this value is bigger the connection will be launched before others with smaller value. [not implemented in 1.0.0]
  * service.requestTranslatorObject- Object to use in quepo-translator for request data.
  * service.responseTranslatorObject- Object to use in quepo-translator for response data.
  * service.textData - Data to send directly in the service. This data is translated with values.
  
2. Create a QPConnManager instance:

  We use the configuration file when we create an instance of the manager.
  
   ```java
      QPConnManager connManager = new QPConnManager();
   ```
   
3. Get the service to execute

   After the services have been configured in the file we can get them using its QPConnConf instance.
   
   ```java
      QPConnConf connConf = QPUtils.readConfObjectFromAssets(this, "connections.conf", QPConnConf.class);
	  QPConnConfig config = connConf.getService("serviceExample");
   ```
   
4. Execute the service:

  Before executing the service we can modify the config object as we want, including headers, values, ... Anything we need. Then we call to add connection to the manager that will execute the service as soon as possible.
  
  The response is received in two different listeners. The first one is in the thread context used for connection. Don't use this thread for doing long processes because it might stop the execution of other services if the thread limit configuration is too small. The second listener is called from main thread and can be executed interface actions.
    
   ```java
      QPConnConfig config = connConf.getService("getOrders");
        connManager.addConn(config, new QPResponseBgListener() {
            @Override
            public void responseOnBackground(QPConnResponse response, QPConnConfig config) {
                //do something in background, save data into database for example?
            }
        }, new QPResponseListener() {
            @Override
            public void responseOnMainThread(QPConnResponse response, QPConnConfig config) {
                //do something in main thread, call to update data in screen for example?
            }
        });
   ```

## Additional

1. Quepo-translations

   Quepo-conn uses quepo-translator library to convert data to objects and viceversa. It is important to know this library if you want to add this feature to your project.
  
   Quepo-translator can be added getting an instance of a manager and including it into the QPConnManager instance. You can find the typical translator manager generated for connection translations. 
   
   In this example the configuration file of translations is stored in assets. Calendar and Date classes are added to the list of avoid classes to convert because they are typically used, but if your objects don't need them delete them to increase performance.

   ```java
      QPTransManager transManager = QPConnUtils.generateTypicalTranslatorManager(QPUtils.readConfObjectFromAssets(this, "translation.conf", QPTransConf.class));
	  connManager.setTranslatorManager(transManager, new Class[]{Calendar.class, Date.class});
   ```

## Logs

Quepo-conn has a lot of logs, showing all the process. You can enable it but remember to disable it in production releases.

  ```java
  QTL.showLogs = true;
  ```

## License

    Copyright 2016 SilicorniO

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    


