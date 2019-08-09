package com.Jessy1237.DrugNameOCR.Handlers;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.leptonica.global.lept;
import org.bytedeco.tesseract.TessBaseAPI;

class TesseractOCRHandler extends OCRHandler
{

    public TesseractOCRHandler( ImageHandler ih )
    {
        super( ih );
    }

    @Override
    public void run()
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
        PIX image = lept.pixRead( ih.getCurrentImagePath() );
        image.xres( 300 ); //min res wanted
        image.yres( 300 );
        api.SetImage( image );

        //Get OCR result
        outText = api.GetHOCRText( 0 );
        outputString = outText.getString();

        //Destroy used object and release memory
        api.End();
        outText.deallocate();
        lept.pixDestroy( image );
        api.close();
    }
}
