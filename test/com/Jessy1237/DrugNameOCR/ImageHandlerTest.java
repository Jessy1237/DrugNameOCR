package com.Jessy1237.DrugNameOCR;

import org.opencv.core.Core;

import com.Jessy1237.DrugNameOCR.Handlers.ImageHandler;

public class ImageHandlerTest
{

    public static void main( String[] args )
    {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        try
        {
            if ( args.length != 1 )
            {
                System.out.println( "USAGE: <image>" );
            }
            else
            {
                ImageHandler ih = new ImageHandler( args[0], true );
                ih.run();
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

}
