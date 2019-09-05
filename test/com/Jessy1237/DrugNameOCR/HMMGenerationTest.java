package com.Jessy1237.DrugNameOCR;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;

import com.Jessy1237.DrugNameOCR.Util.CharacterCorruption;

public class HMMGenerationTest
{

    private final static int NUMBER_OF_LINES_TO_READ = 9000;
    private final static double PERCENT_OF_LINES_TO_CORRUPT = 0.4;

    public static void main( String[] args )
    {
        if ( args.length != 2 )
        {
            System.out.println( "ARGS: <HMM file path> <PBS csv path>" );
        }
        else
        {
            int[] numChars = new int[57];
            int[] numEmissionChars = new int[57];
            int numWords = 0;
            double[][] transMatrix = new double[57][57];
            double[][] emissionsMatrix = new double[57][57];
            double[][] initialState = new double[1][57];
            Random rand = new Random();
            Util util = new Util();

            try ( BufferedReader br = new BufferedReader( new FileReader( args[1] ) ) )
            {
                br.readLine(); //First line of the csv file is the headers of each column so skip it
                for ( int i = 0; i < NUMBER_OF_LINES_TO_READ; i++ )
                {
                    String line = br.readLine();
                    String stateWord = line.split( "," )[3].toLowerCase();
                    int[] stateSeq = util.convertStringToStates( stateWord ); //The brand name is the 4th column in the csv hence index 3 split on commas
                    int[] emissionSeq;

                    if ( rand.nextInt( 100 ) < 100 * PERCENT_OF_LINES_TO_CORRUPT ) //Corrupt the word with any of the possible OCR errors for mis character recognisation
                    {
                        emissionSeq = corruptSequence( stateSeq, util );
                    }
                    else
                    {
                        emissionSeq = stateSeq;
                    }

                    System.out.println( util.convertStatesToString( stateSeq ) );
                    System.out.println( util.convertStatesToString( emissionSeq ) );

                    //Now we can process the words and find the state transitions, emissions and the initial state of each word.
                    int currChar = 0, prevChar = 0, emissionChar;
                    for ( int j = 0; j < stateSeq.length; j++ )
                    {
                        currChar = stateSeq[j];
                        emissionChar = emissionSeq[j];

                        if ( currChar != -1 ) //Skip if we encounter a space. i.e. its a new word after this character
                        {
                            if ( j == 0 || prevChar == -1 ) //Either the first character of the string or the last character was a space
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

            if ( num < 29 ) //20% chance to corrupt the character
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

    public static void divideMatrixByArray( double[][] matrix, int[] num )
    {
        for ( int j = 0; j < matrix.length; j++ )
        {
            for ( int i = 0; i < matrix[0].length; i++ )
            {
                if ( num[j] == 0 || matrix[j][i] == 0 ) //Avoid NaN by setting it to 0
                {
                    matrix[j][i] = 0;
                }
                else
                {
                    matrix[j][i] /= ( double ) num[j];
                }
            }
        }
    }

    public static void divideMatrixByNum( double[][] matrix, int num )
    {
        for ( int j = 0; j < matrix.length; j++ )
        {
            for ( int i = 0; i < matrix[0].length; i++ )
            {
                if ( num == 0 || matrix[j][i] == 0 ) //Avoid NaN by setting it to 0
                {
                    matrix[j][i] = 0;
                }
                else
                {
                    matrix[j][i] /= ( double ) num;
                }
            }
        }
    }
}
