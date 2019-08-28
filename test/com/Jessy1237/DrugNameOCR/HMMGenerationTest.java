package com.Jessy1237.DrugNameOCR;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

public class HMMGenerationTest
{

    private final static int NUMBER_OF_LINES_TO_READ = 1000;
    private final static double PERCENT_OF_LINES_TO_CORRUPT = 0.2;

    public static void main( String[] args )
    {
        if ( args.length != 2 )
        {
            System.out.println( "USAGE: <HMM file path> <PBS csv path>" );
        }
        else
        {
            int[] numChars = new int[37];
            int[] numEmissionChars = new int[37];
            int numWords = 0;
            double[][] transMatrix = new double[37][37];
            double[][] emissionsMatrix = new double[37][37];
            double[][] initialState = new double[1][37];
            Random rand = new Random();

            try ( BufferedReader br = new BufferedReader( new FileReader( args[1] ) ) )
            {
                br.readLine(); //First line of the csv file is the headers of each column so skip it
                for ( int i = 0; i < NUMBER_OF_LINES_TO_READ; i++ )
                {
                    String line = br.readLine();
                    String stateWord = line.split( "," )[3].toLowerCase(); //The brand name is the 4th column in the csv hence index 3 split on commas
                    String emissionWord = stateWord;

                    if ( rand.nextInt( 100 ) < 100 * PERCENT_OF_LINES_TO_CORRUPT ) //Corrupt the word with any of the possible OCR errors for mis character recognisation
                    {
                        emissionWord = corruptWord( stateWord );
                    }

                    //Now we can process the words and find the state transitions, emissions and the initial state of each word.
                    int currChar = 0, prevChar = 0, emissionChar;
                    for ( int j = 0; j < stateWord.length(); j++ )
                    {
                        currChar = stateWord.charAt( j ) - 97;
                        emissionChar = emissionWord.charAt( j ) - 97;

                        if ( currChar != -65 ) //Skip if we encounter a space. i.e. its a new word after this character
                        {

                            if ( stateWord.charAt( j ) >= '0' && stateWord.charAt( j ) <= '9' )
                            {
                                currChar = 26 + stateWord.charAt( j ) - 48;
                            }

                            if ( emissionWord.charAt( j ) >= '0' && emissionWord.charAt( j ) <= '9' )
                            {
                                emissionChar = 26 + emissionWord.charAt( j ) - 48;
                            }

                            if ( stateWord.charAt( j ) == '-' )
                            {
                                currChar = 36;
                            }

                            if ( emissionWord.charAt( j ) == '-' )
                            {
                                emissionChar = 36;
                            }

                            if ( j == 0 || prevChar == -65 ) //Either the first character of the string or the last character was a space
                            {
                                initialState[0][currChar]++;
                                numWords++;
                            }
                            else
                            {
                                transMatrix[prevChar][currChar]++;
                                emissionsMatrix[currChar][emissionChar]++;
                                numEmissionChars[currChar]++;
                                numChars[prevChar]++;
                            }
                        }

                        prevChar = currChar;
                    }
                }

                //Now we divide the matrices by the number of data points used to form each of them
                divideMatrixByArray( transMatrix, numChars );
                divideMatrixByArray( emissionsMatrix, numEmissionChars );
                divideMatrixByNum( initialState, numWords );

                HMM hmm = new HMM( transMatrix, emissionsMatrix, initialState, args[0] );
                hmm.writeToFile();

                if ( !hmm.isValid() )
                {
                    System.out.println( "HMM was not valid. i.e. rows or cols didn't equal 1" );
                }
                else
                {
                    System.out.println( "HMM is valid and saved to '" + args[0] + "'" );
                }
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    public static String corruptWord( String word )
    {
        String corruptedWord = "";

        for ( int i = 0; i < word.length(); i++ )
        {
            Random rand = new Random();
            char currChar = word.charAt( i );

            if ( currChar == ' ' ) //Skip if its a space
            {
                corruptedWord += ' ';
                continue;
            }

            char[] corruptions = CharacterCorruption.findCharacterCorruption( currChar ).getCorruptions();
            int num = rand.nextInt( 100 );

            if ( num < 9 ) //10% chance to corrupt the character
            {
                if ( corruptions.length > 0 )
                {
                    currChar = corruptions[rand.nextInt( corruptions.length )];
                }
            }

            corruptedWord += currChar;
        }

        return corruptedWord;
    }

    public static void divideMatrixByArray( double[][] matrix, int[] num )
    {
        for ( int j = 0; j < matrix.length; j++ )
        {
            for ( int i = 0; i < matrix[0].length; i++ )
            {
                matrix[j][i] /= ( double ) num[j];
            }
        }
    }

    public static void divideMatrixByNum( double[][] matrix, int num )
    {
        for ( int j = 0; j < matrix.length; j++ )
        {
            for ( int i = 0; i < matrix[0].length; i++ )
            {
                matrix[j][i] /= ( double ) num;
            }
        }
    }

    private enum CharacterCorruption
    {
        a( new char[] { 'o', 'e', 'u' } ),
        b( new char[] { 'o', '3', '6', '8' } ),
        c( new char[] { 'e' } ),
        d( new char[] { 'o' } ),
        e( new char[] { 'o', 'c' } ),
        f( new char[] { 't' } ),
        g( new char[] { 'j', 'q', '9', 'y' } ),
        h( new char[] { 'n', 'm' } ),
        i( new char[] { 'r', 'j', '1' } ),
        j( new char[] { 'i', } ),
        k( new char[] { 'x' } ),
        l( new char[] { 'i', '1' } ),
        m( new char[] { 'n', 'h' } ),
        n( new char[] { 'm', 'h' } ),
        o( new char[] { 'a', '0' } ),
        p( new char[] { 'o' } ),
        q( new char[] { 'g', 'y', '9' } ),
        r( new char[] { 'i' } ),
        s( new char[] { '5' } ),
        t( new char[] { 'i', '-', 'f', '7' } ),
        u( new char[] { 'a', 'v' } ),
        v( new char[] { 'u', 'w' } ),
        w( new char[] { 'v' } ),
        x( new char[] { 'k' } ),
        y( new char[] { 'q', 'g' } ),
        z( new char[] { '2' } ),
        zero( new char[] { 'o' } ),
        one( new char[] { 'i', 'l' } ),
        two( new char[] { 'z' } ),
        three( new char[] { 'b' } ),
        four( new char[] {} ),
        five( new char[] { 's' } ),
        six( new char[] { 'b' } ),
        seven( new char[] { 't' } ),
        eight( new char[] { 'b' } ),
        nine( new char[] { 'g', 'q' } ),
        hyphen( new char[] { 't' } );

        CharacterCorruption( char[] corruptions )
        {
            this.corruptions = corruptions;
        }

        private char[] corruptions;

        public char[] getCorruptions()
        {
            return this.corruptions;
        }

        /**
         * Find the enum CharacterCorruption based off of the supplied char. Symbols can't be mapped to names directly like a char can be so we statically map them after z.
         * 
         * @param c The character that represents the CharacterCorruption
         * @return The CharacterCorruption associated with that character
         */
        public static CharacterCorruption findCharacterCorruption( char c )
        {
            if ( c == '-' )
            {
                return hyphen;
            }
            else if ( c >= '0' && c <= '9' )
            {
                return values()[c - 48 + 26];
            }
            else
            {
                return valueOf( c + "" );
            }
        }
    }
}
