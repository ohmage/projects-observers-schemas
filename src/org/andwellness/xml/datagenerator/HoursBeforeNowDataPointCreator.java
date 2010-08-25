package org.andwellness.xml.datagenerator;

import java.util.Map;

import org.apache.log4j.Logger;

import nu.xom.Node;

public class HoursBeforeNowDataPointCreator extends DataPointCreator {
    private Logger _logger = Logger.getLogger(HoursBeforeNowDataPointCreator.class);
    
    @Override
    public DataPoint create(Node currentNode) {
        // Grab necessary values from the currentNode
        String displayType = currentNode.query("displayType").get(0).getValue();
        String nodeId = currentNode.query("id").get(0).getValue();
        Map<String, String> nodeProperties;
        int hoursBeforeNow,min,max;
        
        DataPoint createdDataPoint = new DataPoint();
        
        createdDataPoint.setId(nodeId);
        createdDataPoint.setPromptType(DataPoint.PromptType.hours_before_now);
        createdDataPoint.setDisplayType(displayType);
        
        // Find the min and max properties
        Node propertyNode = currentNode.query("properties").get(0);
        nodeProperties = extractProperties(propertyNode);
        
        min = Integer.parseInt(nodeProperties.get("min"));
        max = Integer.parseInt(nodeProperties.get("max"));
        
        // Find a random int between max and min
        hoursBeforeNow = ValueCreator.randomPositiveIntModulus(max - min) + min;
        
        createdDataPoint.setValue(new Integer(hoursBeforeNow));
        
        if (_logger.isDebugEnabled()) {
            _logger.debug("Creating an hoursBeforeNow data point with id " + nodeId + " min " + min + " max " + max + " value " + hoursBeforeNow);
        }
        
        return createdDataPoint;
    }

}
