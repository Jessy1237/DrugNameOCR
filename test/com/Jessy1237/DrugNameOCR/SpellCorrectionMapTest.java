package com.Jessy1237.DrugNameOCR;

import com.Jessy1237.DrugNameOCR.SpellCorrection.SpellCorrectionMap;

public class SpellCorrectionMapTest
{

    public static void main( String[] args )
    {
        if ( args.length != 3 )
        {
            System.out.println( "ARGS: \"<SpellCorrectionMap path>\" \"<incorrect spelling>\" \"<correct spelling>\"" );
        }
        else
        {
            System.out.println( "Loading map from file....." );
            SpellCorrectionMap scm = new SpellCorrectionMap( args[0] );
            System.out.println( "Adding mapping to map and saving map to file....." );
            scm.addNewSpellCorrection( args[1], args[2] );
            scm.save();

            System.out.println( "Reloading map from file....." );
            scm = new SpellCorrectionMap( args[0] );
            String correct = scm.getPreviousSpellCorrection( args[1] );

            if ( correct != null )
            {
                if ( correct.equalsIgnoreCase( args[2] ) )
                {
                    System.out.println( "SUCCESS: Found the correct spelling" );
                }
                else
                {
                    System.out.println( "Was Unable to find the correct Spelling" );
                }
            }
            else
            {
                System.out.println( "Was Unable to find the correct Spelling" );
            }
        }
    }

}
