package com.Jessy1237.DrugNameOCR;

import java.util.List;

import org.opencv.core.Core;

import com.Jessy1237.DrugNameOCR.Handlers.ImageHandler;
import com.Jessy1237.DrugNameOCR.Models.BoundingBox;
import com.Jessy1237.DrugNameOCR.Models.Model;
import com.Jessy1237.DrugNameOCR.Models.ModelManager;

public class ModelSimilarityTest
{

    public static void main( String[] args )
    {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        try
        {
            if ( args.length != 2 )
            {
                System.out.println( "ARGS: <image> <model directory>" );
            }
            else
            {
                //Process the image to find the text regions
                Util util = new Util();
                ImageHandler ih = new ImageHandler( args[0], true );
                ih.run();

                List<BoundingBox> combined = util.combineOverlapBB( ih.findBindingBoxes( ih.getCurrentImage() ), ( int ) ( ih.getCurrentImage().width() * 0.015 ), ( int ) ( ih.getCurrentImage().height() * 0.01 ) ); //have the combination tolerance as 1.5% of the image width and 1.0% for the image height
                ih.drawBoundingBoxes( combined, 2 );

                //Load all the models
                ModelManager mm = new ModelManager( args[1] );
                mm.readModels();

                //Find the best suited model to the image
                Model model = mm.findBestModel( combined, ih.getCurrentImage().width(), ih.getCurrentImage().height() );

                System.out.println( "Best model is '" + model.getId() + "' with similarity: " + ( model.getAssociatedSimilarity() * 100f ) + "%" );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

}
