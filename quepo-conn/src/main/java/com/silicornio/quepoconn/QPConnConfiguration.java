package com.silicornio.quepoconn;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SilicorniO
 */
public class QPConnConfiguration {

    /** List of values to apply to parameters of the connection configuration **/
    protected List<QPConnKeyValue> values = new ArrayList<>();

    /** List of headers to apply to parameters of the connection configuration **/
    protected List<QPConnKeyValue> headers = new ArrayList<>();

    /** Format of response **/
    protected String responseFormat;

    /** Format of the request **/
    protected String requestFormat;
}
