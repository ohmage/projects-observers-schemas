package org.andwellness.xml.datagenerator;

import java.util.Date;

import nu.xom.Node;

public class NumberDataPointCreator implements DataPointCreator {

    @Override
    public DataPoint create(Node currentNode, Date creationTime) {
        // Grab necessary values from the currentNode
        String displayType = currentNode.query("displayType").get(0).getValue();
        String nodeId = currentNode.query("id").get(0).getValue();
        
        
        DataPoint createdDataPoint = new DataPoint();
        
        createdDataPoint.setId(nodeId);
        createdDataPoint.setPromptType(DataPoint.PromptType.number);
        //createdDataPoint.setDisplayType(displayType);
        // TODO Set this from the max/min properties
        createdDataPoint.setValue(ValueCreator.randomPositiveIntModulus(10));
        
        return createdDataPoint;
    }

}
