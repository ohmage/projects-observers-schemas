package org.andwellness.xml.datagenerator;

public class DataPointCreatorFactory {

    /**
     * Initialize and return a DataPointCreator based on the passed prompt type
     * 
     * @param promptType The type of DataPoint to create.  Possibilities are in DataPoint.PromptType.
     * @return A DataPointCreator
     */
    public static DataPointCreator getDataPointCreator(String promptType) {
        if (DataPoint.PromptType.timestamp.toString().equals(promptType)) {
            return new TimeStampDataPointCreator();
        }
        else if (DataPoint.PromptType.number.toString().equals(promptType)) {
            return new NumberDataPointCreator();
        }
        else if (DataPoint.PromptType.single_choice.toString().equals(promptType)) {
            return new SingleChoiceDataPointCreator();
        }
        else if (DataPoint.PromptType.hours_before_now.toString().equals(promptType)) {
            return new HoursBeforeNowDataPointCreator();
        }
        else {
            throw new IllegalArgumentException("No DataPointCreator for promptType " + promptType);
        }
    }

}
