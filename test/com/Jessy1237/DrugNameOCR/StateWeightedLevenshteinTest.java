package com.Jessy1237.DrugNameOCR;

import com.Jessy1237.DrugNameOCR.Util.CharacterCorruption;
import com.Jessy1237.DrugNameOCR.SpellCorrection.StateWeightedLevenshtein;

import info.debatty.java.stringsimilarity.CharacterSubstitutionInterface;
import info.debatty.java.stringsimilarity.WeightedLevenshtein;

public class StateWeightedLevenshteinTest
{

    public static void main( String[] args )
    {

        if ( args.length != 2 )
        {
            System.out.println( "ARGS: \"<string 1>\" \"<string 2>\"" );
        }
        else
        {
            Util util = new Util();
            StateWeightedLevenshtein swl = new StateWeightedLevenshtein( util );
            WeightedLevenshtein wl = new WeightedLevenshtein( new CharacterSubstitutionInterface() {

                public double cost( char c1, char c2 )
                {
                    String s1 = "" + c1;
                    s1 = s1.toLowerCase();

                    String s2 = "" + c2;
                    s2 = s2.toLowerCase();

                    // The cost for substituting a known corruption should be considered smaller as these are known errors for OCR.
                    if ( CharacterCorruption.findCharacterCorruption( s1 ).containsCorruption( s2 ) )
                    {
                        return 0.5;
                    }

                    //For most other cases the cost should be 1.0 for substituting 2 characters
                    return 1.0;
                }

            } );

            System.out.println( String.format( "Word Similarity using State Weighted Levenshtein of '%s' and '%s' is %f%%", args[0], args[1], swl.similarityPercentage( args[0], args[1] ) ) );

            int length = ( args[0].length() >= args[1].length() ? args[0].length() : args[1].length() );
            double percent = ( ( 1.0 - wl.distance( args[0], args[1] ) / ( double ) length ) * 100.0 );
            System.out.println( String.format( "Word Similarity using Weighted Levenshtein of '%s' and '%s' is %f%%", args[0], args[1], percent ) );
        }

    }

}
