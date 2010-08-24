package org.andwellness.xml.datagenerator;

import nu.xom.Node;

public interface DataPointCreator {

    DataPoint create(Node currentNode);

}
