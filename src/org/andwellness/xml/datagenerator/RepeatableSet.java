package org.andwellness.xml.datagenerator;

import java.util.Iterator;
import java.util.List;

/**
 * A repeatable set is a set of metadata (mainly the id), and a double array of DataPoints.
 * 
 * @author jhicks
 *
 */
public class RepeatableSet implements Response {
    private String id;
    private List<List<DataPoint>> dataPointList;
    
    public RepeatableSet() {};
    
    public void setId(String _id) {
        id = _id;
    }
    
    public String getId() {
        return id;
    }
    
    /**
     * Add a list of resposnes that represent a single run of the
     * repeatableSet.
     * 
     * @param set A List of DataPoints to represent one set of responses to the repeatableSet
     */
    public void addSet(List<DataPoint> set) {
        dataPointList.add(set);
    }
    
    public Iterator<List<DataPoint>> getRepeatableSetListIterator() {
        return dataPointList.iterator();
    }
}
