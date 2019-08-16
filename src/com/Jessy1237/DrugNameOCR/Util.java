package com.Jessy1237.DrugNameOCR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Util
{

    public final String IMAGE_TAG = "-I=";

    public Util()
    {

    }

    /**
     * Finds specified strings within the java args array and collates them into a proper string object and then categorises them into their specified type. i.e. finds and collates image paths This
     * method allows the functionality to parse multiple images with different handlers in one execution.
     * 
     * @param args The array of strings to parse into the map of Handler specifiers to Image Paths.
     * @return The HashMap of entries to create an OCR handler and its respective Image Handlers.
     */
    public HashMap<String, List<String>> parseArgumentsToStrings( String[] args )
    {
        HashMap<String, List<String>> strings = new HashMap<String, List<String>>();

        boolean foundSpecifier = false;
        String tempSpecifier = "";

        for ( int i = 0; i < args.length; i++ )
        {
            if ( args[i].substring( 0, 3 ).equalsIgnoreCase( IMAGE_TAG ) && foundSpecifier )
            {
                String tempPath = args[i].substring( 3 );

                if ( !tempPath.isEmpty() )
                {
                    addPathToMap( strings, tempSpecifier, tempPath );
                    foundSpecifier = false;
                }
            }
            else if ( !foundSpecifier )
            {
                tempSpecifier = args[i];
                foundSpecifier = true;
            }
            else //If we reach this else statement then we found a OCR Handler specifier but not an image to process so we will skip this specifier
            {
                foundSpecifier = false;
            }
        }

        return strings;
    }

    /**
     * This method searches through the list of bounding boxes and merges/combined any overlapping bounding boxes into one, larger bounding box.
     * 
     * @param boxes The original list of bounding boxes to check for overlaps
     * @param tolerance the +- value to check around the bounding box
     * @return The list of combined/merged bounding boxes
     */
    public Set<BoundingBox> combineOverlapBB( List<BoundingBox> boxes, int tolerance )
    {
        LinkedHashMap<BoundingBox, BoundingBox> mapBBtoCombinedBB = new LinkedHashMap<BoundingBox, BoundingBox>();
        LinkedHashSet<BoundingBox> combinedBBs = new LinkedHashSet<BoundingBox>();
        boolean stillOverlappingBB = true;

        combinedBBs.addAll( boxes );

        while ( stillOverlappingBB )
        {
            stillOverlappingBB = false;

            for ( BoundingBox b1 : combinedBBs )
            {
                BoundingBox combinedB1 = mapBBtoCombinedBB.get( b1 );

                if ( combinedB1 == null )
                {
                    combinedB1 = ( BoundingBox ) b1.clone();
                    mapBBtoCombinedBB.put( b1, combinedB1 );
                }
                else
                {
                    if ( !combinedB1.equals( b1 ) )
                    {
                        continue;
                    }
                }

                for ( BoundingBox b2 : combinedBBs )
                {
                    if ( !b2.equals( b1 ) )
                    {
                        BoundingBox combinedB2 = mapBBtoCombinedBB.get( b2 );

                        if ( combinedB2 == null )
                        {
                            combinedB2 = b2;
                        }
                        else
                        {
                            if ( !b2.equals( combinedB2 ) )
                            {
                                continue;
                            }
                        }

                        if ( combinedB2.equals( combinedB1 ) )
                        {
                            continue;
                        }

                        if ( checkBBOverlap( combinedB1, combinedB2, tolerance ) )
                        {
                            mapBBtoCombinedBB.put( combinedB1, combinedB1 );
                            mapBBtoCombinedBB.put( b1, combinedB1 );
                            mapBBtoCombinedBB.put( b2, combinedB1 );
                            stillOverlappingBB = true;
                        }
                    }
                }
            }

            combinedBBs.clear();
            for ( BoundingBox bb : mapBBtoCombinedBB.keySet() )
            {
                combinedBBs.add( mapBBtoCombinedBB.get( bb ) );
            }

            mapBBtoCombinedBB.clear();
        }

        return combinedBBs;
    }

    /**
     * Checks if b2 is inside of b1 from reference to the bottom left corner of the bounding box. It then combines the two bounding boxes into the first bounding box so that the combined bounding box
     * now covers the two overlapping bounding boxes.
     * 
     * @param b1 The bounding box to see if it is the bottom left most bounding box but overlaps with b2
     * @param b2 The bounding box to see if it is within b1
     * @param tolerance The tolerance value to check +- of the boundary of the bounding boxes
     * @return true if b2 was merged into b1 otherwise false.
     */
    private boolean checkBBOverlap( BoundingBox b1, BoundingBox b2, int tolerance )
    {
        boolean merged = false;
        if ( b1.getMinX() - tolerance <= b2.getMinX() && b1.getMaxX() + tolerance >= b2.getMinX() )
        {
            if ( b1.getMinY() - tolerance <= b2.getMinY() && b1.getMaxY() + tolerance >= b2.getMinY() )
            {
                combineBBX( b1, b2 );
                combineBBY( b1, b2, b1 );
                b1.addId( b2.getId() );
                merged = true;
            }
            else if ( b2.getMinY() - tolerance <= b1.getMinY() && b2.getMaxY() + tolerance >= b1.getMinY() )
            {
                combineBBX( b1, b2 );
                combineBBY( b2, b1, b1 );
                b1.addId( b2.getId() );
                merged = true;
            }
        }

        return merged;
    }

    /**
     * Combines the two bounding box X values together into b1. The combined minX value will be the same as b1 but the maxX value will be determined as whichever is greater between the two.
     * 
     * @param b1 The bounding box with the smaller min x value
     * @param b2 The bounding box with the larger min x value
     */
    private void combineBBX( BoundingBox b1, BoundingBox b2 )
    {
        b1.setMinX( b1.getMinX() );
        if ( b1.getMaxX() >= b2.getMaxX() )
        {
            b1.setMaxX( b1.getMaxX() );
        }
        else
        {
            b1.setMaxX( b2.getMaxX() );
        }
    }

    /**
     * Combines the two bounding box Y values together into the supplied combined bounding box. The combined minY value will be the same as b1 but the maxY value will be determined as whichever is
     * greater between the two.
     * 
     * @param b1 The bounding box with the smallest min y value
     * @param b2 The bounding box with the larger min y value
     * @param combined the bounding box to combine the y values into
     */
    private void combineBBY( BoundingBox b1, BoundingBox b2, BoundingBox combined )
    {
        combined.setMinY( b1.getMinY() );
        if ( b1.getMaxY() >= b2.getMaxY() )
        {
            combined.setMaxY( b1.getMaxY() );
        }
        else
        {
            combined.setMaxY( b2.getMaxY() );
        }
    }

    /**
     * Adds the following entry to the HashMap or if the key already exists then it adds the path to the list of paths for that key
     * 
     * @param strings The HashMap to add the entry to
     * @param tempSpecifier The specifier for the entry (the key in the hashmap)
     * @param tempPath The path entry to add to the hash map
     */
    private void addPathToMap( HashMap<String, List<String>> strings, String tempSpecifier, String tempPath )
    {
        ArrayList<String> tempList = null;

        if ( strings.containsKey( tempSpecifier ) )
        {
            tempList = ( ArrayList<String> ) strings.get( tempSpecifier );
        }
        else
        {
            tempList = new ArrayList<String>();
        }

        tempList.add( tempPath );
        strings.put( tempSpecifier, tempList );
    }
}
