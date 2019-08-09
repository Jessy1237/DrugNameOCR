package com.Jessy1237.DrugNameOCR.Handlers;

public abstract class OCRHandler implements Runnable
{

    protected ImageHandler ih;
    protected String outputString;
    
    public OCRHandler( ImageHandler ih )
    {
        this.ih = ih;
        outputString = "";
    }

    public ImageHandler getImageHandler()
    {
        return ih;
    }
    
    public String getString()
    {
        return outputString;
    }
    
    public boolean hasRun()
    {
        return !outputString.isEmpty();
    }

    public abstract void run();
}
