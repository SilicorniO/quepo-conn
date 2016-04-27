package com.silicornio.quepoconn;

/**
 * Created by Silicornio
 */
public class QPConnKeyValue {

    /** Key **/
    protected String key;

    /** Value **/
    protected String value;

    public QPConnKeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QPConnKeyValue that = (QPConnKeyValue) o;

        return key != null ? key.equals(that.key) : that.key == null;

    }

    protected QPConnKeyValue clone(){
        return new QPConnKeyValue(key, value);
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

}
