package org.andwellness.xml.datagenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * A survey consist of metadata, plus a list dataPoints and repeatableSets.
 * A DataPoint has an id, value, prompt type, and display type
 * A repeatableSet is almost a mini survey, and has metadata plus a list of dataPoints
 *
 * @author jhicks
 *
 */
public class Survey {
    // Survey metadata
    private Date creationTime;
    private String id;
    private String title;
    double lat, lon;
    String tz;
    
    // Survey data points, can be either DataPoints OR RepeatableSets
    private List<Response> responseList;
    
    public Survey() {
        responseList = new ArrayList<Response>();
    };
    
    // Setters/getters
    public void setCreationTime(Date _creationTime) {
        creationTime = _creationTime;
    }
    
    public Date getCreationTime() {
        return creationTime;
    }
    
    public void setId(String _id) {
        id = _id;
    }
    
    public String getId() {
        return id;
    }
    
    public void setTitle(String _title) {
        title = _title;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setLat(double _lat) {
        lat = _lat;
    }
    
    public double getLat() {
        return lat;
    }
    
    public void setLon(double _lon) {
        lon = _lon;
    }
    
    public double getLon() {
        return lon;
    }    
    
    public void setTz(String _tz) {
        tz = _tz;
    }
    
    public String getTz() {
        return tz;
    }
    
    public void addResponse(Response _response) {
        responseList.add(_response);
    }
    
    /**
     * Only return DataPoints, not RepeatableSets.  Useful for condition checking
     * as DataPoints cannot condition on resposnes from within RepeatableSets
     * 
     * @return A List of DataPoints from the Survey, RepeatableSets have been removed.
     */
    public List<DataPoint> getCurrentDataPoints() {
        List<DataPoint> toReturn = new ArrayList<DataPoint>();
        
        Iterator<Response> responseListIterator = responseList.iterator();
        while (responseListIterator.hasNext()) {
            Response response = responseListIterator.next();
            
            // Only choose DataPoints to return
            if (response instanceof org.andwellness.xml.datagenerator.DataPoint) {
                toReturn.add((DataPoint) response);
            }
        }
        
        return toReturn;
    }
    
    public List<Response> getResponseList() {
        return responseList;
    }
}
