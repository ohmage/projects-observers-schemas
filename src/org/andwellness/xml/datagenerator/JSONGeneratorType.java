package org.andwellness.xml.datagenerator;


import java.util.List;

import org.json.JSONArray;


public interface JSONGeneratorType {
    public JSONArray translateDataPointsToJsonArray(List<DataPoint> dataPointList);
}
