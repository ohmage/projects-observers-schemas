package org.andwellness.xml.datagenerator;

import java.util.HashMap;
import java.util.Map;

public class DataPoint {
    // Possible data types
    public static enum TYPE {
        category, measurement, event, counter 
    }
    
    // Map to hold the data point in a format easily JSONified
    private Map<String, Object> jsonMap = new HashMap<String, Object>();
    
    // Nothing to do here!
    public DataPoint() {}
    
    // Grab the created json map for whatever
    public Map<String, Object> getJson() {
        return jsonMap;
    }
    
    // Large set of putters to create the json map
    public void putLabel(String label) {
        jsonMap.put("label", label);
    }
    
    public void putValue(String value) {
        jsonMap.put("value", value);
    }

    public String getValue() {
        return (String) jsonMap.get("value");
    }
    
    public void putUnit(String unit) {
        jsonMap.put("unit", unit);
    }
    
    public void putDatetime(String datetime) {
        jsonMap.put("datetime", datetime);
    }
    
    public void putTz(String tz) {
        jsonMap.put("tz", tz);
    }
    
    public void putLat(double lat) {
        jsonMap.put("lat", String.valueOf(lat));
    }
    
    public void putLon(double lon) {
        jsonMap.put("lon", String.valueOf(lon));
    }
    
    public void putType(TYPE type) {
        jsonMap.put("type", type.toString());
    }
}
