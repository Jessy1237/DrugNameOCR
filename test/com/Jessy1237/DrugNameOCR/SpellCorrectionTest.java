package com.Jessy1237.DrugNameOCR;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;

import com.Jessy1237.DrugNameOCR.Util.CharacterCorruption;
import com.Jessy1237.DrugNameOCR.SpellCorrection.HMM;

public class SpellCorrectionTest
{

    private final static int NUMBER_OF_LINES_TO_READ = 9000;

    public static void main( String[] args )
    {
        if ( args.length != 2 )
        {
            System.out.println( "ARGS: <hmm file> <PBS csv file>" );
        }
        else
        {
            Util util = new Util();
            HMM hmm = new HMM( new File( args[0] ) );
            int correct = 0;
            int numWords = 0;

            try ( BufferedReader br = new BufferedReader( new FileReader( args[1] ) ) )
            {
                br.readLine(); //First line of the csv file is the headers of each column so skip it
                for ( int i = 0; i < NUMBER_OF_LINES_TO_READ; i++ )
                {
                    String line = br.readLine().toLowerCase();
                    String words = line.split( "," )[3].toLowerCase();

                    for ( String correctWord : words.split( " " ) )
                    {
                        if ( correctWord == null )
                            continue;

                        correctWord = correctWord.trim();
                        if ( correctWord.isEmpty() )
                            continue;

                        numWords++;
                        int[] stateSeq = util.convertStringToStates( correctWord ); //The brand name is the 4th column in the csv hence index 3 split on commas
                        int[] emissionSeq = corruptSequence( stateSeq, util );
                        String incorrectWord = util.convertStatesToString( emissionSeq );
                        String correctedWord = util.spellCorrectOCRResult( hmm, incorrectWord ).split( "`" )[0];

                        System.out.println( incorrectWord + ", " + correctedWord + ", " + correctWord );

                        if ( correctWord.equalsIgnoreCase( correctedWord ) )
                            correct++;

                    }
                }

                br.close();

                System.out.println( String.format( "Read in %d test words and successfully corrected %d (%.2f%%)", numWords, correct, ( double ) ( ( double ) correct / ( double ) numWords * 100.0 ) ) );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    public static int[] corruptSequence( int[] sequence, Util util )
    {
        ArrayList<Integer> states = new ArrayList<Integer>();

        for ( int i = 0; i < sequence.length; i++ )
        {
            Random rand = new Random();

            if ( sequence[i] == -1 ) //Skip if its a space
            {
                states.add( -1 );
                continue;
            }

            int currState = sequence[i];
            String[] corruptions = CharacterCorruption.values()[sequence[i]].getCorruptions();
            int num = rand.nextInt( 100 );

            if ( num < 9 ) //10% chance to corrupt the character
            {
                if ( corruptions.length > 0 )
                {
                    currState = util.convertStringToStates( corruptions[rand.nextInt( corruptions.length )] )[0]; //corrupt the state in the sequence
                }
            }

            states.add( currState );
        }

        //Now convert our list to an array
        int[] stateSeq = new int[states.size()];
        for ( int i = 0; i < states.size(); i++ )
        {
            stateSeq[i] = states.get( i );
        }

        return stateSeq;
    }
}
