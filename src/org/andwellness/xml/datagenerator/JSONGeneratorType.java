package org.andwellness.xml.datagenerator;


import java.util.List;

import org.json.JSONArray;

/**
 * Use JSONGeneratorType to translate DataPoints to JSON one by one.  A class that
 * extends JSONGeneratorType must implement a function to translate each data point prompt
 * type as specified in the DataPoint.ValueTranslator interface.
 * 
 * @author jhicks
 *
 */
public abstract class JSONGeneratorType implements DataPoint.ValueTranslator {
    public JSONGeneratorType() {};
    
    public abstract JSONArray translateDataPointsToJsonArray(List<DataPoint> dataPointList);
}
