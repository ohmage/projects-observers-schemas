package org.andwellness.xml.datagenerator;

import nu.xom.Node;

public class HoursBeforeNowDataPointCreator implements DataPointCreator {

    @Override
    public DataPoint create(Node currentNode) {
        // Grab necessary values from the currentNode
        String displayType = currentNode.query("displayType").get(0).getValue();
        String nodeId = currentNode.query("id").get(0).getValue();
        
        
        DataPoint createdDataPoint = new DataPoint();
        
        createdDataPoint.setId(nodeId);
        createdDataPoint.setPromptType(DataPoint.PromptType.hours_before_now);
        createdDataPoint.setDisplayType(displayType);
        createdDataPoint.setValue(new Integer(ValueCreator.randomPositiveIntModulus(10)));
        
        return createdDataPoint;
    }

}
