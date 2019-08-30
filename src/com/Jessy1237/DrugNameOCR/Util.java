package com.Jessy1237.DrugNameOCR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
     * This method searches through the list of bounding boxes and merges/combined any overlapping bounding boxes into one, larger bounding box.
     * 
     * @param boxes The original list of bounding boxes to check for overlaps
     * @param toleranceX the +- value to add to the X values
     * @param toleranceY the +- value to add to the Y values
     * @return The list of combined/merged bounding boxes
     */
    public List<BoundingBox> combineOverlapBB( List<BoundingBox> boxes, int toleranceX, int toleranceY )
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

                        if ( checkBBOverlap( combinedB1, combinedB2, toleranceX, toleranceY ) )
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

        ArrayList<BoundingBox> combinedList = new ArrayList<BoundingBox>( combinedBBs );
        //combinedList.sort( new BoundingBox() );

        return combinedList;
    }

    /**
     * Converts the array of states into a string
     * 
     * @param states the states from the hmm
     * @return The word representing the states
     */
    public String convertStatesToString( int[] states )
    {
        String word = "";
        for ( int i = 0; i < states.length; i++ )
        {
            if ( states[i] == -1 )
            {
                word += ' ';
                continue;
            }
            word += CharacterCorruption.getString( states[i] );
        }

        return word;
    }

    public int[] convertStringToStates( String word )
    {
        ArrayList<Integer> states = new ArrayList<Integer>();

        word = word.toLowerCase();

        for ( int i = 0; i < word.length(); i++ )
        {
            char c = word.charAt( i );
            char nextC = 0;

            if ( i + 1 < word.length() )
            {
                nextC = word.charAt( i + 1 );
            }

            if ( c == 'l' && nextC == 'o' )
            {
                states.add( 56 );
                i++;
            }
            else if ( c == 'l' && nextC == 'a' )
            {
                states.add( 55 );
                i++;
            }
            else if ( c == 'c' && nextC == 'l' )
            {
                states.add( 54 );
                i++;
            }
            else if ( c == 'o' && nextC == 'l' )
            {
                states.add( 53 );
                i++;
            }
            else if ( c >= '!' && c <= ';' ) //!-; in ASCII table
            {
                states.add( 26 + c - 33 );
            }
            else if ( c == ' ' )
            {
                states.add( -1 );
            }
            else //a-z
            {
                states.add( c - 97 );
            }
        }

        int[] stateSeq = new int[states.size()];
        for ( int i = 0; i < states.size(); i++ )
        {
            stateSeq[i] = states.get( i );
        }

        return stateSeq;
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
    private boolean checkBBOverlap( BoundingBox b1, BoundingBox b2, int toleranceX, int toleranceY )
    {
        boolean merged = false;
        if ( b1.getMinX() - toleranceX <= b2.getMinX() && b1.getMaxX() + toleranceX >= b2.getMinX() )
        {
            if ( b1.getMinY() - toleranceY <= b2.getMinY() && b1.getMaxY() + toleranceY >= b2.getMinY() )
            {
                combineBBX( b1, b2 );
                combineBBY( b1, b2, b1 );
                b1.addId( b2.getId() );
                merged = true;
            }
            else if ( b2.getMinY() - toleranceY <= b1.getMinY() && b2.getMaxY() + toleranceY >= b1.getMinY() )
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

    //FUTURE WORK:Can investigate and prepare a proper corpus with misspelled emissions instead of doing this random supervised training, a proper corpus of supervised training would realistically achieve better spell correction results
    public enum CharacterCorruption
    {
        a( "a", new String[] { "o", "e", "u" } ),
        b( "b", new String[] { "o", "3", "6", "8", "lo", "la" } ),
        c( "c", new String[] { "e" } ),
        d( "d", new String[] { "ol", "al", "cl" } ),
        e( "e", new String[] { "o", "c" } ),
        f( "f", new String[] { "t" } ),
        g( "g", new String[] { "j", "q", "9", "y" } ),
        h( "h", new String[] { "n", "m" } ),
        i( "i", new String[] { "r", "j", "1", "!" } ),
        j( "j", new String[] { "i", } ),
        k( "k", new String[] { "x" } ),
        l( "l", new String[] { "i", "1", "!" } ),
        m( "m", new String[] { "n", "h" } ),
        n( "n", new String[] { "m", "h" } ),
        o( "o", new String[] { "a", "0" } ),
        p( "p", new String[] { "o" } ),
        q( "q", new String[] { "g", "y", "9" } ),
        r( "r", new String[] { "i" } ),
        s( "s", new String[] { "5", "$" } ),
        t( "t", new String[] { "i", "-", "f", "7", "+" } ),
        u( "u", new String[] { "a", "v" } ),
        v( "v", new String[] { "u", "w" } ),
        w( "w", new String[] { "v" } ),
        x( "x", new String[] { "k" } ),
        y( "y", new String[] { "q", "g" } ),
        z( "z", new String[] { "2" } ),
        exclamation( "!", new String[] { "i", "l" } ),
        invCommas( "\"", new String[] {} ),
        hash( "#", new String[] {} ),
        dollar( "$", new String[] { "s" } ),
        percent( "%", new String[] {} ),
        ampersand( "&", new String[] {} ),
        apostrophe( "'", new String[] {} ),
        bracketL( "(", new String[] {} ),
        bracketR( ")", new String[] {} ),
        star( "*", new String[] {} ),
        plus( "+", new String[] { "t" } ),
        comma( ",", new String[] { "." } ),
        hyphen( "-", new String[] { "t" } ),
        period( ".", new String[] { "," } ),
        slash( "/", new String[] {} ),
        zero( "0", new String[] { "o" } ),
        one( "1", new String[] { "i", "l" } ),
        two( "2", new String[] { "z" } ),
        three( "3", new String[] { "b" } ),
        four( "4", new String[] {} ),
        five( "5", new String[] { "s" } ),
        six( "6", new String[] { "b" } ),
        seven( "7", new String[] { "t" } ),
        eight( "8", new String[] { "b" } ),
        nine( "9", new String[] { "g", "q" } ),
        colon( ":", new String[] { ";" } ),
        semiColon( ";", new String[] { ":" } ),
        ol( "ol", new String[] { "d" } ),
        cl( "cl", new String[] { "d" } ),
        la( "la", new String[] { "b" } ),
        lo( "lo", new String[] { "b" } );

        CharacterCorruption( String name, String[] corruptions )
        {
            this.corruptions = corruptions;
            this.name = name;
        }

        private String[] corruptions;
        private String name;

        public String[] getCorruptions()
        {
            return this.corruptions;
        }

        /**
         * Find the enum CharacterCorruption based off of the supplied char. Symbols can't be mapped to names directly like a char can be so we statically map them after z.
         * 
         * @param c The character that represents the CharacterCorruption
         * @return The CharacterCorruption associated with that character
         */
        public static CharacterCorruption findCharacterCorruption( String s )
        {
            if ( s.length() == 1 )
            {
                char c = s.charAt( 0 );
                if ( c >= '!' && c <= ';' )
                {
                    return values()[c - 33 + 26];
                }
                else
                {
                    return valueOf( c + "" );
                }
            }
            else
            {
                return valueOf( s );
            }
        }

        public static String getString( int state )
        {
            return values()[state].name;
        }
    }
}
