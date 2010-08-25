package org.andwellness.xml.datagenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nu.xom.Node;

public class MultiChoiceDataPointCreator extends DataPointCreator {

    @Override
    public DataPoint create(Node currentNode) {
        Map<String, String> nodeProperties;
        List<String> createdNodeValue = new ArrayList<String>();

        // Grab necessary values from the currentNode
        String displayType = currentNode.query("displayType").get(0).getValue();
        String nodeId = currentNode.query("id").get(0).getValue();
        
        DataPoint createdDataPoint = new DataPoint();
        
        createdDataPoint.setId(nodeId);
        createdDataPoint.setPromptType(DataPoint.PromptType.single_choice);
        createdDataPoint.setDisplayType(displayType);

        // Find all node properties, for each, randomly choose to either select
        // or not select the node
        Node propertyNode = currentNode.query("properties").get(0);
        nodeProperties = extractProperties(propertyNode);  
        
        Iterator<String> nodePropertyKeyIterator = nodeProperties.keySet().iterator();
        while (nodePropertyKeyIterator.hasNext()) {
            String key = nodePropertyKeyIterator.next();
            // To choose or not to choose this key?
            if (ValueCreator.randomBoolean()) {
                createdNodeValue.add(key);
            }
        }

        createdDataPoint.setValue(createdNodeValue);
        
        return createdDataPoint;
    }

}
