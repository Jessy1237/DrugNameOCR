package com.Jessy1237.DrugNameOCR;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;

import com.Jessy1237.DrugNameOCR.Handlers.ImageHandler;

public class ModelSimilarityTest
{

    public static void main( String[] args )
    {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        try
        {
            if ( args.length != 3 )
            {
                System.out.println( "USAGE: <image> <model directory> <model name>" );
            }
            else
            {
                Util util = new Util();
                ImageHandler ih = new ImageHandler( args[0], true );
                ih.run();

                List<BoundingBox> combined = util.combineOverlapBB( ih.findBindingBoxes( ih.getCurrentImage() ), ( int ) ( ih.getCurrentImage().width() * 0.015 ), ( int ) ( ih.getCurrentImage().height() * 0.005 ) ); //have the combination tolerance as 2% of the image width and 0.005% for the image height
                ih.drawBoundingBoxes( combined, 2 );
                List<String> names = new ArrayList<String>();
                names.add( args[2] );
                ModelManager mm = new ModelManager( args[1], names );
                mm.readModels();
                double sim = ( ( mm.getModels() ).get( 0 ) ).calculateSimilarity( combined, ih.getCurrentImage().width(), ih.getCurrentImage().height() );
                System.out.println( "Similarity is: " + ( sim * 100f ) + "%" );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

}
