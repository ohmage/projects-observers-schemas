package org.andwellness.xml.datagenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

public class OutJSONGeneratorType extends JSONGeneratorType {
    @Override
    public JSONArray translateSurveyToJsonArray(Survey survey) {
        JSONArray translatedJSON = new JSONArray();
        
        // Loop over the DataPoints, set myself as translator, translate the value, then add to the JSON array
        Iterator<DataPoint> dataPointListIterator = dataPointList.iterator();
        while (dataPointListIterator.hasNext()) {
            DataPoint dataPoint = dataPointListIterator.next();
            
            // The fields we need from the data point
            String label, unit, datetime, tz, displayType, lat, lon;
            
            label = dataPoint.getId();
            unit = dataPoint.getUnit();
            datetime = dataPoint.getDatetime();
            tz = dataPoint.getTz();
            displayType = dataPoint.getDisplayType().toString();
            lat = dataPoint.getLat();
            lon = dataPoint.getLon();
            
            // Grab the translated value
            dataPoint.setTranslator(this);
            List<String> translatedValues = dataPoint.translateValue();
            
            // Make a data point for every translated value in the list
            Iterator<String> translatedValuesIterator = translatedValues.iterator();
            while (translatedValuesIterator.hasNext()) {
                String translatedValue = translatedValuesIterator.next();
                
                // Create a HashMap of the required JSON
                Map<String, Object> jsonHash = new HashMap<String, Object>();
                jsonHash.put("label", label);
                jsonHash.put("unit", unit);
                jsonHash.put("datetime", datetime);
                jsonHash.put("tz", tz);
                jsonHash.put("type", displayType);
                jsonHash.put("lat", new Double(lat).toString());
                jsonHash.put("lon", new Double(lon).toString());
                jsonHash.put("value", translatedValue);
                
                // Put this into a data JSON object
                Map<String, Object> jsonData = new HashMap<String, Object>();
                jsonData.put("data", jsonHash);
                
                // Add to the translatedJSON array
                translatedJSON.put(jsonData);
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
        
        
        translatedValues.add("test");
        
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
