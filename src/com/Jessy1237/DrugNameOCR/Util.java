package com.Jessy1237.DrugNameOCR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
