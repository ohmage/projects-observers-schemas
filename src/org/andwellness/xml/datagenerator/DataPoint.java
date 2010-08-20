package org.andwellness.xml.datagenerator;

import java.util.List;
import java.util.Map;

public class DataPoint {
    // List of data fields in a data point, add more if necessary for extended data points
    private String id;
    private Object value;  
    private DataPoint.DisplayType displayType;
    private DataPoint.PromptType promptType;
    
    // Metadata
    private String unit;
    private String datetime;
    private String tz;
    private double lat;
    private double lon;
    
    // Translator to translate the value to turn into JSON
    ValueTranslator valueTranslator = null;
    
    // Possible display types
    public static enum DisplayType {
        category, measurement, event, counter 
    }
    
    // Possible prompt types
    public static enum PromptType {
        timestamp, number, hours_before_now, text, multi_choice, single_choice, single_choice_custom, multi_choice_custom, photo
    }
    
    // Public interface that all JSON translators must implement!
    public interface ValueTranslator {
        public List<Map<String,Object>> translateTimestamp(String timestampValue);
        public List<Map<String,Object>> translateNumber(String numberValue);
        public List<Map<String,Object>> translateHoursBeforeNow(String hoursBeforeNowValue);
        public List<Map<String,Object>> translateText(String textValue);
        public List<Map<String,Object>> translateMultiChoice(List<Map<String,String>> multiChoiceValue);
        public List<Map<String,Object>> translateSingleChoice(Map<String,String> singleChoiceValue);
        public List<Map<String,Object>> translateSingleChoiceCustom(Map<String,String> singleChoiceValue);
        public List<Map<String,Object>> translateMultiChoiceCustom(List<Map<String,String>> multiChoiceValue);
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
    
    public void setPromptType(PromptType _promptType) {
        promptType = _promptType;
    }
    
    public PromptType getPromptType() {
        return promptType;
    }
    
    public void setTranslator(ValueTranslator _valueTranslator) {
        valueTranslator = _valueTranslator;
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
