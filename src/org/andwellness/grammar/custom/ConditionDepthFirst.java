package org.andwellness.grammar.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.andwellness.grammar.syntaxtree.id;
import org.andwellness.grammar.syntaxtree.value;
import org.andwellness.grammar.visitor.GJVoidDepthFirst;

/**
 * Simple visitor for adding condition ids and their associated values to a Map.
 * 
 * @author selsky
 * @param <A> must be a Map<String, List<String>>
 */
public class ConditionDepthFirst<A> extends GJVoidDepthFirst<A> {
	public String _currentId;
	
    /**
     * f0 -> <TEXT>
     * 
     * Adds an entry for the id token into the provided map, if the entry doesn't already exist. 
     * map must be a non-null Map<String, List<String>>.
     */
    @Override
    public void visit(id n, A map) {
    	
    	if(! (map instanceof Map<?, ?>)) {
    		throw new IllegalArgumentException("argu parameter must be a Map");
    	}
    	if(null == map) {
    		throw new IllegalArgumentException("argu parameter must be non-null");
    	}
    	
    	String tokenImage = n.f0.tokenImage;
    	_currentId = tokenImage;
    	
    	Map<String, List<String>> idValueMap = (Map<String, List<String>>) map; 
    	if(! idValueMap.containsKey(tokenImage)) {
    		idValueMap.put(tokenImage, new ArrayList<String>());
    	}
    	
        n.f0.accept(this, map);
    }
   
    /**
     * f0 -> <TEXT>
     * 
     * Adds an entry for the value token to the List retrieved using the current id (the last id token seen during 
     * the parse) from the provided map.
     * argu must be a non-null Map. 
     */
    public void visit(value n, A map) {
    	
    	if(! (map instanceof Map<?, ?>)) {
    		throw new IllegalArgumentException("argu parameter must be a Map");
    	}
    	if(null == map) {
    		throw new IllegalArgumentException("argu parameter must be non-null");
    	}
   	
    	List<String> valueList = ((Map<String, List<String>>) map).get(_currentId);
    	valueList.add(n.f0.tokenImage);
    	
        n.f0.accept(this, map);
    }
}
