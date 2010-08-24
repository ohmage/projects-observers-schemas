package org.andwellness.xml.datagenerator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DataPoint {
    // List of data fields in a data point, add more if necessary for extended data points
    private String id;
    private Object value;  
    private DataPoint.DisplayType displayType;
    private DataPoint.PromptType promptType;
    
    // Map to store all possible metadata
    Map<String, Object> metadata = new HashMap<String, Object>();
    
    // Translator to translate the value to turn into JSON
    ValueTranslator valueTranslator = null;
    
    // Possible display types
    public static enum DisplayType {
        category, measurement, event, counter, metadata
    }
    
    // Possible prompt types
    public static enum PromptType {
        timestamp, number, hours_before_now, text, multi_choice, single_choice, single_choice_custom, multi_choice_custom, photo
    }
    
    // Public interface that all JSON translators must implement!
    public interface ValueTranslator {
        public List<Map<String, Object>> translateTimestamp(String timestampValue);
        public List<Map<String, Object>> translateNumber(String numberValue);
        public List<Map<String, Object>> translateHoursBeforeNow(String hoursBeforeNowValue);
        public List<Map<String, Object>> translateText(String textValue);
        public List<Map<String, Object>> translateMultiChoice(List<Map<String,String>> multiChoiceValue);
        public List<Map<String, Object>> translateSingleChoice(Map<String,String> singleChoiceValue);
        public List<Map<String, Object>> translateSingleChoiceCustom(Map<String,String> singleChoiceValue);
        public List<Map<String, Object>> translateMultiChoiceCustom(List<Map<String,String>> multiChoiceValue);
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
    
    public void setValue(Object _value) {
        value = _value;
    }

    public Object getValue() {
        return value;
    }
    
    public void setUnit(String _unit) {
        metadata.put("unit", _unit);
    }
    
    public String getUnit() {
       return (String) metadata.get("unit");
    }
    
    public void setDatetime(String _datetime) {
        metadata.put("datetime", _datetime);
    }
    
    public String getDatetime() {
        return (String) metadata.get("datetime");
    }
    
    public void setTz(String _tz) {
        metadata.put("tz", _tz);
    }
    
    public String getTz() {
        return (String) metadata.get("tz");
    }
    
    public void setLat(double _lat) {
        metadata.put("lat", new Double(_lat));
    }
    
    public double getLat() {
        Double lat = (Double) metadata.get("lat");
        
        if (lat == null) {
            return Double.NaN;
        }
        else {
            return lat.doubleValue();
        }
    }
    
    public void setLon(double _lon) {
        metadata.put("lon", new Double(_lon));
    }
    
    public double getLon() {
        Double lon = (Double) metadata.get("lon");
        
        if (lon == null) {
            return Double.NaN;
        }
        else {
            return lon.doubleValue();
        }
    }
    
    public void setDisplayType(DisplayType _displayType) {
        displayType = _displayType;
    }
    
    public DisplayType getDisplayType() {
        return displayType;
    }
    
    public void setPromptType(PromptType _promptType) {
        promptType = _promptType;
    }
    
    public PromptType getPromptType() {
        return promptType;
    }
    
    public void setTranslator(ValueTranslator _valueTranslator) {
        valueTranslator = _valueTranslator;
    }
    
    public boolean isMetadata() {
        if (DisplayType.metadata.equals(displayType)) {
            return true;
        }
            
        return false;
    }
    
    /**
     *  Grab a new metadata Map and copy all key/values into our current metadata.
     * 
     * @param _metadata Must implement the Metadata interface
     */
    public void updateMetadata(Map<String, Object> metadata) {
        Iterator<String> newMetadataKeyIterator = metadata.keySet().iterator();
        
        // Loop over every key, add to our current metadata
        // Overwrite any keys we already have
        while (newMetadataKeyIterator.hasNext()) {
            String currentKey = newMetadataKeyIterator.next();
            
            metadata.put(currentKey, metadata.get(currentKey));
        }
    }

    /**
     * Translates the prompt value based on the promptType and the set translator that 
     * implements the JSONTranslator interface
     * 
     * @return A representation of the value that can be easily JSONified (List<Map<String,Object>>)
     */
    @SuppressWarnings("unchecked")
    public List<Map<String,Object>> translateValue() {
        if (valueTranslator == null) {
            return null;
        }
        
        switch (promptType) {
        case timestamp:
            return valueTranslator.translateTimestamp((String) value);
            
        case number:
            return valueTranslator.translateNumber((String) value);
            
        case hours_before_now:
            return valueTranslator.translateHoursBeforeNow((String) value);
            
        case text:
            return valueTranslator.translateText((String) value);
            
        case multi_choice:
            return valueTranslator.translateMultiChoice((List<Map<String,String>>) value);
            
        case single_choice:
            return valueTranslator.translateSingleChoice((Map<String,String>) value);
            
        case single_choice_custom:
            return valueTranslator.translateSingleChoiceCustom((Map<String,String>) value);
            
        case multi_choice_custom:
            return valueTranslator.translateMultiChoiceCustom((List<Map<String,String>>) value);
            
            // Not sure what to do with a photo yet
        case photo:
            return null;
            
        default:
            return null;
        }
    }
}
