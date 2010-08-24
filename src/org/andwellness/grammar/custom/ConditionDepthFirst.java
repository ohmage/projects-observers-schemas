package org.andwellness.grammar.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.andwellness.grammar.syntaxtree.NodeToken;
import org.andwellness.grammar.syntaxtree.condition;
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
	// private static Logger _logger = Logger.getLogger(ConditionDepthFirst.class);
	private String _currentId;
	private ConditionValuePair _currentPair;
	
    /**
     * f0 -> <TEXT>
     * 
     * Adds an entry for the id token into the provided map, if the entry doesn't already exist. 
     * map must be a non-null Map<String, List<String>>.
     */
    @Override
    public void visit(id n, A map) {
    	
    	// Lazy null check only occurs on the id node because if it is non-null here, it will be non-null throughout the rest
    	// of the visitor process
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
     * f0 -> "=="
     *       | "!="
     *       | "<"
     *       | ">"
     *       | "<="
     *       | ">="
     */
    public void visit(condition n, A map) {
        String tokenImage = (((NodeToken) n.f0.choice).tokenImage); // ugly cast, but it's the only way to get the value
        ConditionValuePair pair = new ConditionValuePair();
        pair.setCondition(tokenImage);
        _currentPair = pair;
         
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
    	List<ConditionValuePair> valueList = ((Map<String, List<ConditionValuePair>>) map).get(_currentId);
    	_currentPair.setValue(n.f0.tokenImage);
    	valueList.add(_currentPair);
    	
        n.f0.accept(this, map);
    }
}
