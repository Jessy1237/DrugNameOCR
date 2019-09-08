package com.Jessy1237.DrugNameOCR.Handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.Jessy1237.DrugNameOCR.Models.BoundingBox;

public class XMLHandler extends DefaultHandler
{
    private List<BoundingBox> boxes = new ArrayList<BoundingBox>();

    @Override
    public void startElement( String uri, String localname, String qName, Attributes attributes )
    {

        //These next two lines define how a bounding box is defined to be a paragraph with a qName of p and the class attribute being "ocr_par".
        if ( qName.equalsIgnoreCase( "p" ) )
        {
            if ( attributes.getValue( "class" ).equalsIgnoreCase( "ocr_par" ) )
            {
                BoundingBox temp = new BoundingBox();
                temp.setId( attributes.getValue( "id" ) );

                addBounds( temp, attributes.getValue( "title" ) );

                if ( temp.isValid() )
                    boxes.add( temp );
            }
        }

    }

    public List<BoundingBox> getBoxes()
    {
        return boxes;
    }

    /**
     * Converts the XML string from hOCR to the coordinates for the bounding box
     * 
     * @param box The BoundingBox to set the corner values of
     * @param str The string containing the xml title which contains the coordinate information
     */
    private void addBounds( BoundingBox box, String str )
    {
        StringTokenizer st = new StringTokenizer( str );
        if ( st.nextToken().equalsIgnoreCase( "bbox" ) )
        {
            box.setMinX( Integer.parseInt( st.nextToken() ) );
            box.setMinY( Integer.parseInt( st.nextToken() ) );
            box.setMaxX( Integer.parseInt( st.nextToken() ) );

            String temp = st.nextToken();
            temp.replace( ';', ' ' );
            box.setMaxY( Integer.parseInt( temp.trim() ) );
        }
    }
}
