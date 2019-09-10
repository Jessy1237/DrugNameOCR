package com.Jessy1237.DrugNameOCR.Handlers;

import com.Jessy1237.DrugNameOCR.Models.Model;

public class OCRHandlerFactory
{

    private final static String TESSERACT = "-AT";
    private final static String GOOGLE = "-MG";

    public static OCRHandler createOCRHandler( String specifier, ImageHandler ih, Model m, String credentials ) throws IllegalArgumentException
    {
        OCRHandler ocrh = null;
        if ( specifier.isEmpty() )
        {
            throw new IllegalArgumentException( "The cropping specifier for the OCR Handler was empty." );
        }

        if ( specifier.equalsIgnoreCase( TESSERACT ) )
        {
            ocrh = new TesseractOCRHandler( ih, m );
        }
        else if ( specifier.equalsIgnoreCase( GOOGLE ) )
        {
            ocrh = new GoogleOCRHandler( ih, m, credentials );
        }
        else
        {
            throw new IllegalArgumentException( "Unknown OCR Handler Specifier: " + specifier );
        }

        return ocrh;
    }
}
