package org.andwellness.xml.datagenerator;

import java.util.Date;

import nu.xom.Node;

public interface DataPointCreator {

    DataPoint create(Node currentNode, Date creationTime);

}
