package com.Jessy1237.DrugNameOCR;

import java.util.HashMap;
import java.util.List;

import org.opencv.core.Core;

import com.Jessy1237.DrugNameOCR.Handlers.ImageHandler;
import com.Jessy1237.DrugNameOCR.Handlers.OCRHandler;
import com.Jessy1237.DrugNameOCR.Handlers.OCRHandlerFactory;

public class DrugNameOCR
{

    public static void main( String[] args )
    {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

        Util util = new Util();

        if ( args.length < 3 )
        {
            throw new IllegalArgumentException( "Need at least two arguments to run. You can have multiple of the two minimum arguments to allow for multiple images to be processed in one execution.\nMinimum Arguments: <handler specifier> -I=\"<image path>\"\nFor Example: -AG -I=\"C:\\img.jpg\"" );
        }

        HashMap<String, List<String>> ocrRequests = util.parseArgumentsToStrings( args );

        if ( ocrRequests.isEmpty() )
        {
            throw new IllegalArgumentException( "No valid OCR Requests were found" );
        }

        try
        {
            for ( String specifier : ocrRequests.keySet() )
            {
                for ( String path : ocrRequests.get( specifier ) )
                {
                    ImageHandler ih = new ImageHandler( path, true );
                    ih.run();

                    OCRHandler ocrh = OCRHandlerFactory.createOCRHandler( specifier, ih );
                    ocrh.run();
                    
                    System.out.println("----------------------------------------OCR TEXT----------------------------------------\n" + ocrh.getString());
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }
}
