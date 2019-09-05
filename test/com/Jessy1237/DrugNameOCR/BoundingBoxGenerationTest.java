package com.Jessy1237.DrugNameOCR;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;

import com.Jessy1237.DrugNameOCR.Handlers.ImageHandler;

public class BoundingBoxGenerationTest
{

    public static void main( String[] args )
    {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        try
        {
            if ( args.length != 2 )
            {
                System.out.println( "ARGS: <image directory> <image name>" );
            }
            else
            {
                Util util = new Util();
                ImageHandler ih = new ImageHandler( args[0], args[1], true );
                ih.run();

                //Uncomment this chunk and comment the rest if you want to test the tesseract hOCR segmentation method
                //OCRHandler ocrh = OCRHandlerFactory.createOCRHandler( "-AT", ih );
                //ocrh.run();

                //SAXParserFactory spf = SAXParserFactory.newInstance();
                //SAXParser sp = spf.newSAXParser();
                //XMLHandler xmlh = new XMLHandler();
                //sp.parse( new InputSource( new StringReader( ocrh.getString() ) ), xmlh );
                //ih.drawBoundingBoxes( xmlh.getBoxes(), 3 );
                //System.out.println( ocrh.getString() );

                List<BoundingBox> bbs = ih.findBindingBoxes( ih.getCurrentImage() );

                //Uncomment this next line if you want to check the combining of the overlapping bounding boxes
                ih.drawBoundingBoxes( bbs, 2 );

                List<BoundingBox> combined = util.combineOverlapBB( bbs, ( int ) ( ih.getCurrentImage().width() * 0.015 ), ( int ) ( ih.getCurrentImage().height() * 0.01 ) );//have the combination tolerance as 1.5% of the image width and 1.0% for the image height
                ih.drawBoundingBoxes( combined, 2 );

                ModelManager mm = new ModelManager( "models" );
                mm.writeModelFile( new Model( ih.getImageName(), new ArrayList<BoundingBox>( combined ), new BoundingBox(), ih.getCurrentImage().width(), ih.getCurrentImage().height() ) );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

}
