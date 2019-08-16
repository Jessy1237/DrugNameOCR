package com.Jessy1237.DrugNameOCR;

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
            Util util = new Util();
            ImageHandler ih = new ImageHandler( args[0], true );
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
            ih.drawBoundingBoxes( util.combineOverlapBB( bbs, 5 ), 2 );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

}
