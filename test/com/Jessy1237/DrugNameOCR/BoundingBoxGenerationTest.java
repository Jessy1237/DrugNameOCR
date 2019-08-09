package com.Jessy1237.DrugNameOCR;

import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.opencv.core.Core;
import org.xml.sax.InputSource;

import com.Jessy1237.DrugNameOCR.Handlers.ImageHandler;
import com.Jessy1237.DrugNameOCR.Handlers.OCRHandler;
import com.Jessy1237.DrugNameOCR.Handlers.OCRHandlerFactory;
import com.Jessy1237.DrugNameOCR.Handlers.XMLHandler;

public class BoundingBoxGenerationTest
{

    public static void main( String[] args )
    {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        try
        {
            ImageHandler ih = new ImageHandler( args[0], true );
            ih.run();

            OCRHandler ocrh = OCRHandlerFactory.createOCRHandler( "-AT", ih );
            ocrh.run();

            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLHandler xmlh = new XMLHandler();
            sp.parse( new InputSource( new StringReader( ocrh.getString() ) ), xmlh );

            ih.drawBoundingBoxes( xmlh.getBoxes(), 3 );

            System.out.println( ocrh.getString() );

        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

}
