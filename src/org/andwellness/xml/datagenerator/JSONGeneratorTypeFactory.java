package org.andwellness.xml.datagenerator;

public class JSONGeneratorTypeFactory {
    // Prevent instantiation of the factory
    private JSONGeneratorTypeFactory() {};
    
    // Grab the correct generator type
    public static JSONGeneratorType getGenerator(String generatorType) {
        if (generatorType == null) {
            throw new IllegalArgumentException("cannot create a JSONGeneratorType for a missing generator type.");
        }
        
        if (generatorType.equals("in")) {
            return new InJSONGeneratorType();
        }
        else if (generatorType.equals("out")) {
            return new OutJSONGeneratorType();
        }
        else {
            throw new IllegalArgumentException("Cannot create a JSONGeneratorType for type " + generatorType);
        }
    }
}
