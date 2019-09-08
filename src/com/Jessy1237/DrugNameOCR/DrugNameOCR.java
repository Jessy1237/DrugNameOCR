package com.Jessy1237.DrugNameOCR;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.opencv.core.Core;

import com.Jessy1237.DrugNameOCR.Handlers.ImageHandler;
import com.Jessy1237.DrugNameOCR.Handlers.OCRHandler;
import com.Jessy1237.DrugNameOCR.Handlers.OCRHandlerFactory;
import com.Jessy1237.DrugNameOCR.Models.BoundingBox;
import com.Jessy1237.DrugNameOCR.Models.Model;
import com.Jessy1237.DrugNameOCR.Models.ModelManager;
import com.Jessy1237.DrugNameOCR.Models.RegionOfInterest;

public class DrugNameOCR
{

    public static void main( String[] args )
    {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

        Util util = new Util();

        if ( args.length < 3 )
        {
            System.out
                    .println( "Need at least two arguments to run. You can have multiple of the two minimum arguments to allow for multiple images to be processed in one execution.\nMinimum Arguments: \"<models directory>\" <handler specifier> -I=\"<image path>\"\nFor Example: \"models/\" -AG -I=\"C:\\img.jpg\"" );
        }
        else
        {
            for ( int i = 0; i < args.length; i++ )
                System.out.println( args[i] );

            HashMap<String, List<String>> ocrRequests = util.parseArgumentsToStrings( Arrays.copyOfRange( args, 1, args.length ) );

            if ( ocrRequests.isEmpty() )
            {
                System.out.println( "No valid OCR Requests were found" );
            }
            else
            {

                try
                {
                    ModelManager mm = new ModelManager( args[0] );
                    System.out.println( "Reading models....." );
                    mm.readModels();

                    for ( String specifier : ocrRequests.keySet() )
                    {
                        for ( String path : ocrRequests.get( specifier ) )
                        {
                            ImageHandler ih = new ImageHandler( path, true );
                            System.out.println( "Readying img '" + path + "'......" );
                            ih.run();

                            System.out.println( "Finding bounding boxes....." );
                            List<BoundingBox> combined = util.combineOverlapBB( ih.findBindingBoxes( ih.getCurrentImage() ), ( int ) ( ih.getCurrentImage().width() * 0.015 ), ( int ) ( ih.getCurrentImage().height() * 0.01 ) ); //have the combination tolerance as 1.5% of the image width and 1.0% for the image height

                            System.out.println( "Finding the best model....." );
                            Model m = mm.findBestModel( combined, ih.getCurrentImage().width(), ih.getCurrentImage().height() );

                            if ( m == null )
                            {
                                System.out.println( "No viable model was found....." );
                            }
                            else
                            {
                                System.out.println( "Found best model as '" + m.getId() + "'" );

                                OCRHandler ocrh = OCRHandlerFactory.createOCRHandler( specifier, ih, m );
                                System.out.println( "Finding ocr text....." );
                                ocrh.run();

                                System.out.println( "----------------------------------------OCR TEXT----------------------------------------" );
                                for ( RegionOfInterest roi : m.getRegionOfInterests() )
                                {
                                    String[][] text = ocrh.getTextFromROIs().get( roi.getId() );
                                    for ( int i = 0; i < text[0].length; i++ )//Assuming paired box has same number of lines as the main box
                                    {
                                        System.out.println( "line " + i + " main text bb: " + text[0][i] );

                                        if ( text.length == 2 )
                                        {
                                            System.out.println( "line " + i + " pair text bb: " + text[1][i] );
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
