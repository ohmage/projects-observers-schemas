package org.andwellness.xml.datagenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

public class OutJSONGeneratorType extends JSONGeneratorType {
    // Hold the survey we are translating for use by the translate* functions
    Survey _survey;
    
    @Override
    public JSONArray translateSurveyToJsonArray(Survey survey) {
        _survey = survey;
        
        JSONArray translatedJSON = new JSONArray();
        // Data point metadata, various rules control these from the wiki
        Map<String, Object> metadata = new HashMap<String, Object>();
        
        // Loop over the Responses, set myself as translator, translate the value, then add to the JSON array
        List<Response> responseList = survey.getResponseList();
        Iterator<Response> responseListIterator = responseList.iterator();
        while (responseListIterator.hasNext()) {
            Response response = responseListIterator.next();
            
            // Check if this is a DataPoint or a RepeatableSet
            if (response instanceof org.andwellness.xml.datagenerator.DataPoint) {
                DataPoint dataPoint = (DataPoint) response;
                
                // Fields we need from the data point
                String label, value, displayType;
            
                label = dataPoint.getId();
                displayType = dataPoint.getDisplayType().toString();
                
                // Grab the set of translated values, create a new data point for each
                dataPoint.setTranslator(this);
                List<String> valueList = dataPoint.translateValue();
                Iterator<String> valueListIterator = valueList.iterator();
                while (valueListIterator.hasNext()) {
                    value = valueListIterator.next();
                    
                    // Create the data point
                    Map<String, Object> jsonHash = new HashMap<String, Object>();
                    
                    jsonHash.put("label", label);
                    jsonHash.put("datetime", ValueCreator.date(survey.getCreationTime()));
                    jsonHash.put("value", value);
                    jsonHash.put("tz", survey.getTz());
                    jsonHash.put("lat", new Double(survey.getLat()).toString());
                    jsonHash.put("long", new Double(survey.getLon()).toString());
                    jsonHash.put("type", displayType);
                    
                    // Put this into a data JSON object
                    Map<String, Object> jsonData = new HashMap<String, Object>();
                    jsonData.put("data", jsonHash);
                    
                    // Add to the translatedJSON array
                    translatedJSON.put(jsonData);
                }
                
                
            
            }
            else if (response instanceof org.andwellness.xml.datagenerator.RepeatableSet) {
                RepeatableSet repeartableSet = (RepeatableSet) response;
                
                // Create a number of data points from the repeatable set
            }
        }

        return translatedJSON;
    }
    
    @Override
    public List<String> translateTimestamp(String timestamp) {
        List<String> translatedValues = new ArrayList<String>();
        
        translatedValues.add(timestamp);
        
        return translatedValues;
    }

    @Override
    public List<String> translateNumber(Integer number) {
        List<String> translatedValues = new ArrayList<String>();
        
        translatedValues.add(number.toString());
        
        return translatedValues;
    }

    @Override
    public List<String> translateHoursBeforeNow(Integer number) {
        List<String> translatedValues = new ArrayList<String>();
        
        // hoursBeforeNow data points keep the hours before the creation date
        String timestamp = ValueCreator.hours_before_date(_survey.getCreationTime(), number.intValue());
        translatedValues.add(timestamp);
        
        return translatedValues;
    }

    @Override
    public List<String> translateText(String text) {
        List<String> translatedValues = new ArrayList<String>();
        
        return translatedValues;
    }

    @Override
    public List<String> translateMultiChoice(List<Integer> choiceList) {
        List<String> translatedValues = new ArrayList<String>();
        
        return translatedValues;
    }

    @Override
    public List<String> translateSingleChoice(Integer choice) {
        List<String> translatedValues = new ArrayList<String>();
        
        translatedValues.add(choice.toString());
        
        return translatedValues;
    }

    @Override
    public List<String> translateSingleChoiceCustom(Integer choice) {
        List<String> translatedValues = new ArrayList<String>();
        
        return translatedValues;
    }

    @Override
    public List<String> translateMultiChoiceCustom(List<Integer> choiceList) {
        List<String> translatedValues = new ArrayList<String>();
        
        return translatedValues;
    }}
