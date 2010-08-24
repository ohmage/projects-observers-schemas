package org.andwellness.xml.datagenerator;

import java.util.HashMap;
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
        public List<String> translateTimestamp(String timestamp);
        public List<String> translateNumber(Integer number);
        public List<String> translateHoursBeforeNow(Integer number);
        public List<String> translateText(String text);
        public List<String> translateMultiChoice(List<Integer> choiceList);
        public List<String> translateSingleChoice(Integer choice);
        public List<String> translateSingleChoiceCustom(Integer choice);
        public List<String> translateMultiChoiceCustom(List<Integer> choiceList);
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
    
    public void setLat(String _lat) {
        metadata.put("lat", _lat);
    }
    
    public String getLat() {
        return (String) metadata.get("lat");
    }
    
    public void setLon(String _lon) {
        metadata.put("lon", _lon);
    }
    
    public String getLon() {
        return (String) metadata.get("lon");
    }
    
    public void setDisplayType(DisplayType _displayType) {
        displayType = _displayType;
    }
    
    /**
     * Convenience function to set display type directly from a String
     * 
     * @param _displayType A String representing a type from DisplayType
     */
    public void setDisplayType(String _displayType) {
        if (DisplayType.category.toString().equals(_displayType)) {
            displayType = DisplayType.category;
        }
        else if (DisplayType.event.toString().equals(_displayType)) {
            displayType = DisplayType.event;
        }
        else if (DisplayType.measurement.toString().equals(_displayType)) {
            displayType = DisplayType.measurement;
        }
        else if (DisplayType.counter.toString().equals(_displayType)) {
            displayType = DisplayType.counter;
        }
        else if (DisplayType.metadata.toString().equals(_displayType)) {
            displayType = DisplayType.metadata;
        }
        else {
            throw new IllegalArgumentException("Display type does not exist: " + _displayType);
        }
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
     * Translates the prompt value based on the promptType and the set translator that 
     * implements the JSONTranslator interface
     * 
     * @return A representation of the value that can be easily JSONified (List<Map<String,Object>>)
     */
    @SuppressWarnings("unchecked")
    public List<String> translateValue() {
        if (valueTranslator == null) {
            return null;
        }
        
        switch (promptType) {
        case timestamp:
            return valueTranslator.translateTimestamp((String) value);
            
        case number:
            return valueTranslator.translateNumber((Integer) value);
            
        case hours_before_now:
            return valueTranslator.translateHoursBeforeNow((Integer) value);
            
        case text:
            return valueTranslator.translateText((String) value);
            
        case multi_choice:
            return valueTranslator.translateMultiChoice((List<Integer>) value);
            
        case single_choice:
            return valueTranslator.translateSingleChoice((Integer) value);
            
        case single_choice_custom:
            return valueTranslator.translateSingleChoiceCustom((Integer) value);
            
        case multi_choice_custom:
            return valueTranslator.translateMultiChoiceCustom((List<Integer>) value);
            
            // Not sure what to do with a photo yet
        case photo:
            return null;
            
        default:
            return null;
        }
    }
    
    public String toString() {
        return "type " + promptType.toString() + " id " + id + " value " + value;
    }
}
