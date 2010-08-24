package org.andwellness.xml.datagenerator;

import java.util.Date;

import nu.xom.Node;

public class TimeStampDataPointCreator implements DataPointCreator {

    public DataPoint create(Node currentNode, Date creationTime) {
        // Grab necessary values from the currentNode
        String displayType = currentNode.query("displayType").get(0).getValue();
        String nodeId = currentNode.query("id").get(0).getValue();
        
        
        DataPoint createdDataPoint = new DataPoint();
        
        createdDataPoint.setId(nodeId);
        createdDataPoint.setPromptType(DataPoint.PromptType.timestamp);
        //createdDataPoint.setDisplayType(displayType);
        createdDataPoint.setValue(ValueCreator.date());
        
        return createdDataPoint;
    }

}
