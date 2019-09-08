package com.Jessy1237.DrugNameOCR.Handlers;

import java.util.HashMap;

import com.Jessy1237.DrugNameOCR.Models.Model;
import com.Jessy1237.DrugNameOCR.Models.RegionOfInterest;

public abstract class OCRHandler implements Runnable
{

    protected ImageHandler ih;
    protected HashMap<String, String[][]> outputStrings;
    protected Model m;

    public OCRHandler( ImageHandler ih, Model m )
    {
        this.ih = ih;
        this.m = m;
        outputStrings = new HashMap<String, String[][]>();
    }

    public ImageHandler getImageHandler()
    {
        return ih;
    }

    /**
     * The key is the id of the region of interest. The value is a string array with the first element being the text from the main bounding box in the ROI while the second element is the text from
     * the pair bounding box if it isn't null
     * 
     * @return The map of the roi IDs to strings from bbs.
     */
    public HashMap<String, String[][]> getTextFromROIs()
    {
        return outputStrings;
    }

    public boolean hasRun()
    {
        return !outputStrings.isEmpty();
    }

    public void run()
    {
        for ( RegionOfInterest roi : m.getRegionOfInterests() )
        {
            String[][] text;
            if ( roi.getPairBoundingBox() != null )
            {
                text = new String[2][];
                text[0] = getText( ih.saveCroppedImage( roi.getMainBoundingBox().getScaledBB( ih.getCurrentImage().width(), ih.getCurrentImage().height(), m.getWidth(), m.getHeight() ), roi.getId() + "-main" ) );
                text[1] = getText( ih.saveCroppedImage( roi.getPairBoundingBox().getScaledBB( ih.getCurrentImage().width(), ih.getCurrentImage().height(), m.getWidth(), m.getHeight() ), roi.getId() + "-pair" ) );
            }
            else
            {
                text = new String[1][];
                text[0] = getText( ih.saveCroppedImage( roi.getMainBoundingBox().getScaledBB( ih.getCurrentImage().width(), ih.getCurrentImage().height(), m.getWidth(), m.getHeight() ), roi.getId() + "-main" ) );
            }

            outputStrings.put( roi.getId(), text );
        }
    }

    /**
     * This is abstract method should be filed in to get every line of text from the supplied image from the OCR engine in order in the an element string array
     * 
     * @param imgLoc The location of the image to get the text from
     * @return An array holding each line of text found in the image.
     */
    protected abstract String[] getText( String imgLoc );
}
