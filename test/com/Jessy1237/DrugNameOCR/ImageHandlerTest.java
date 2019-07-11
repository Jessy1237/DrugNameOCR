package com.Jessy1237.DrugNameOCR;

import org.opencv.core.Core;

public class ImageHandlerTest
{

    public static void main( String[] args )
    {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        try
        {
            ImageHandler ih = new ImageHandler( args[0], true );
            ih.run();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

}
