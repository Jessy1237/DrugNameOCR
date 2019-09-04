package com.Jessy1237.DrugNameOCR;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class SpellCorrectionTest
{

    public static void main( String[] args )
    {
        if ( args.length != 2 )
        {
            System.out.println( "USAGE: <hmm file> <SpellCorrectionTest.txt>" );
        }
        else
        {
            Util util = new Util();
            HMM hmm = new HMM( new File( args[0] ) );
            int correct = 0;
            int numWords = 0;

            try ( BufferedReader br = new BufferedReader( new FileReader( args[1] ) ) )
            {
                System.out.println( "#Incorrect Word, Corrected Word, Correct Word" );
                String line = br.readLine();
                while ( line != null )
                {

                    if ( line.startsWith( "#" ) )
                    {
                        line = br.readLine();
                        continue;
                    }

                    numWords++;

                    String incorrectWord = line.split( " " )[0].toLowerCase();
                    String correctWord = line.split( " " )[1].toLowerCase();
                    String correctedWord = util.spellCorrectOCRResult( hmm, incorrectWord );

                    System.out.println( incorrectWord + ", " + correctedWord + ", " + correctWord );

                    if ( correctWord.equalsIgnoreCase( correctedWord ) )
                        correct++;

                    line = br.readLine();
                }

                br.close();

                System.out.println( "------------------------------------" );
                System.out.println( String.format( "Read in %d test words and successfully corrected %d (%.2f%%)", numWords, correct, ( double ) ( ( double ) correct / ( double ) numWords * 100.0 ) ) );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
    }
}
