package org.andwellness.xml.datagenerator;

public class DataPoint {
    // List of data fields in a data point, add more if necessary for extended data points
    private String id;     // The id of the data point
    private String value;  
    
    // Metadata
    private String unit;
    private String datetime;
    private String tz;
    private double lat;
    private double lon;
    private DataPoint.DisplayType displayType; 
    
    
    // Possible display types
    public static enum DisplayType {
        category, measurement, event, counter 
    }
    
    // Nothing to do here!
    public DataPoint() {}
        
    // Large set of putters to create the json map
    public void setId(String _id) {
        id = _id;
    }
    
    public String getId() {
        return id;
    }
    
    public void setValue(String _value) {
        value = _value;
    }

    public String getValue() {
        return value;
    }
    
    public void setUnit(String _unit) {
        unit = _unit;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setDatetime(String _datetime) {
        datetime = _datetime;
    }
    
    public String getDatetime() {
        return datetime;
    }
    
    public void setTz(String _tz) {
        tz = _tz;
    }
    
    public String getTz() {
        return tz;
    }
    
    public void setLat(double _lat) {
        lat = _lat;
    }
    
    public double getLat() {
        return lat;
    }
    
    public void setLon(double _lon) {
        lon = _lon;
    }
    
    public double getLon() {
        return lon;
    }
    
    public void setDisplayType(DisplayType _displayType) {
        displayType = _displayType;
    }
    
    public DisplayType getDisplayType() {
        return displayType;
    }
}
