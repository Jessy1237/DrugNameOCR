package com.Jessy1237.DrugNameOCR.Handlers;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.leptonica.global.lept;
import org.bytedeco.tesseract.TessBaseAPI;

import com.Jessy1237.DrugNameOCR.Models.Model;

class TesseractOCRHandler extends OCRHandler
{

    public TesseractOCRHandler( ImageHandler ih, Model m )
    {
        super( ih, m );
    }

    @Override
    protected String[] getText( String imgLoc )
    {
        BytePointer outText;

        TessBaseAPI api = new TessBaseAPI();

        // Initialize tesseract-ocr with English tessdata
        if ( api.Init( "./tessdata", "eng" ) != 0 )
        {
            System.err.println( "Could not initialize tesseract." );
            System.exit( 1 );
        }

        //Load the image into a format tesseract can use
        PIX image = lept.pixRead( imgLoc );
        image.xres( 300 ); //min res wanted
        image.yres( 300 );
        api.SetImage( image );

        //Get OCR result
        outText = api.GetUTF8Text();
        String outputString = "";
        try
        {
            outputString = outText.getString( "UTF-8" ).toLowerCase();
        }
        catch ( UnsupportedEncodingException e )
        {
            e.printStackTrace();
        }

        //Destroy used object and release memory
        api.End();
        outText.deallocate();
        lept.pixDestroy( image );
        api.close();

        //Now remove blank lines from our output string and then save the lines into an array
        ArrayList<String> outputList = new ArrayList<String>();
        for ( String str : outputString.split( "\n" ) )
        {
            if ( !str.trim().isEmpty() )
            {
                outputList.add( str );
            }
        }

        String[] outputArray = new String[outputList.size()];
        outputArray = outputList.toArray( outputArray );

        return outputArray;
    }
}
