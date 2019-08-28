package com.Jessy1237.DrugNameOCR;

import java.io.File;

public class SpellCorrectionTest
{

    public static void main( String[] args )
    {
        if ( args.length != 3 )
        {
            System.out.println( "USAGE: <hmm file> <correct spelt word> <misspelled word>" );
        }
        else
        {
            Util util = new Util();
            HMM hmm = new HMM( new File( args[0] ) );
            String incorrectWord = args[2].toLowerCase();
            String correctedWord = incorrectWord;
            String correctWord = args[1].toLowerCase();
            String tempWord;
            int iterations = 0;

            do
            {
                iterations++;
                tempWord = correctedWord;
                hmm.setEmissionSequence( util.convertStringToStates( tempWord ) );
                correctedWord = util.convertStatesToString( hmm.getProbableStates() );
            }
            while ( iterations < 5 && !tempWord.equalsIgnoreCase( correctedWord ) );

            hmm.setEmissionSequence( util.convertStringToStates( incorrectWord ) );
            hmm.setEmissionSequence( hmm.getProbableStates() );
            System.out.println( "Inputting misspelled word '" + incorrectWord.toLowerCase() + "' expecting corrected '" + correctWord + "' and got: '" + correctedWord + "'" );
            System.out.println( "It took " + iterations + " iterations to reach this correct spelt word" );
        }
    }
}
