package org.andwellness.xml.datagenerator;

import java.util.Date;

import org.json.JSONArray;

import nu.xom.Node;

public interface JSONGeneratorType {
    public JSONArray translatePromptToJSONArray(Node promptNode, Date creationDate);
}
