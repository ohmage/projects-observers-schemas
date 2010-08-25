package org.andwellness.xml.datagenerator;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import nu.xom.Node;
import nu.xom.Nodes;

public abstract class DataPointCreator {
    protected Logger _logger = Logger.getLogger(DataPointCreator.class);
    
    public abstract DataPoint create(Node currentNode);
    
    /**
     * Will extract k/v pairs from a Node of type properties.
     * 
     * @param properties A Node containing a list of nodes of type p, each with a k and v node.
     * @return A Map containing the k/v pairs.
     */
    protected Map<String, String> extractProperties(Node propertyNode) {
        Map<String, String> extractedProperties = new HashMap<String, String>();
        
        Nodes pNodes = propertyNode.query("p");
        int pNodesSize = pNodes.size();
        for (int i = 0; i < pNodesSize; ++i) {
            // Each pNodes should contain a key/value pair
            // Since this is already validated we don't do any checking here
            Node pNode = pNodes.get(i);
            String nodeKey = pNode.query("k").get(0).getValue();
            String nodeValue = pNode.query("v").get(0).getValue();
            
            extractedProperties.put(nodeKey, nodeValue);
        }
        
        return extractedProperties;
    }

}
