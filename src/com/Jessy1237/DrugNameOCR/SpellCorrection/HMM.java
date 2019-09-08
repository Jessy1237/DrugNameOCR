package com.Jessy1237.DrugNameOCR.SpellCorrection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * This class is based off of a class called "myHMM" which I used in an assignment at KTH University.
 * 
 * @author Jesse Sieunarine
 * @since 21/09/2018
 */
public class HMM
{
    private double[][] transMatrix;
    private double[][] emissionMatrix;
    private double[][] initialState;
    private int[] emissionSequence;
    private String path;

    public HMM( File file )
    {
        if ( !file.exists() )
        {
            throw new IllegalArgumentException( "Unable to find the HMM file" );
        }
        else
        {
            try ( BufferedReader br = new BufferedReader( new FileReader( file ) ) )
            {
                path = file.getPath();

                String[] lines = new String[3];
                lines[0] = br.readLine();
                lines[1] = br.readLine();
                lines[2] = br.readLine();
                br.close();

                int[] dimTrans = getDimensions( lines[0] );
                int[] dimEm = getDimensions( lines[1] );
                int[] dimInit = getDimensions( lines[2] );

                transMatrix = new double[dimTrans[0]][dimTrans[1]];
                emissionMatrix = new double[dimEm[0]][dimEm[1]];
                initialState = new double[dimInit[0]][dimInit[1]];

                updateTransMatrix( lines[0] );
                updateEmissionMatrix( lines[1] );
                updateInitialState( lines[2] );
            }
            catch ( Exception e )
            {
                throw new IllegalArgumentException( "Unable to read the HMM file" );
            }
        }
    }

    /**
     * Creates the HMM based off the matrices that are already known and the path to where to save the HMM to file
     * 
     * @param a The transition matrix
     * @param b The emission Matrix
     * @param pi The initial state matrix
     * @param path The path too where to save the HMM
     */
    public HMM( double[][] a, double[][] b, double[][] pi, String path )
    {
        transMatrix = a;
        emissionMatrix = b;
        initialState = pi;
        this.path = path;
    }

    public double[][] getTransMatrix()
    {
        return transMatrix;
    }

    public double[][] getEmissionMatrix()
    {
        return emissionMatrix;
    }

    public double[][] getInitialState()
    {
        return initialState;
    }

    public int[] getEmissionSequence()
    {
        return emissionSequence;
    }

    /**
     * Updates the transition matrix
     * 
     * @param line The line to read the matrix from
     */
    public void updateTransMatrix( String line )
    {
        //As we know the format of the file we can skip the first 3 characters
        // as they are the dimensions
        updateMatrix( line, transMatrix );
    }

    public void updateTransMatrix()
    {
        //As we know the format of the file we can skip the first 3 characters
        // as they are the dimensions
        randomlyUpdateMatrix( transMatrix );
    }

    public void updateEmissionMatrix( String line )
    {
        updateMatrix( line, emissionMatrix );
    }

    public void updateEmissionMatrix()
    {
        randomlyUpdateMatrix( emissionMatrix );
    }

    public void updateInitialState( String line )
    {
        updateMatrix( line, initialState );
    }

    public void updateInitialState()
    {
        randomlyUpdateMatrix( initialState );
    }

    public void updateEmissionSequence( String line )
    {
        updateVector( line, emissionSequence );
    }

    public void setEmissionSequence( int[] emissionSequence )
    {
        this.emissionSequence = emissionSequence;
    }

    /**
     * print the most likely states sequence given a hmm instance using Viterbi algorithm
     */
    public int[] getProbableStates()
    {
        double[][] delta = new double[emissionSequence.length][initialState[0].length];
        int[][] delta2 = new int[initialState[0].length][emissionSequence.length];

        // inititalize deltas for the first time step for all possible states
        for ( int i = 0; i < initialState[0].length; i++ )
        {
            delta[0][i] = initialState[0][i] * emissionMatrix[i][emissionSequence[0]];
            delta2[i][0] = i;
        }

        // update t>1 and keep track of the best path
        for ( int t = 1; t < emissionSequence.length; t++ )
        {
            int[][] newpath = new int[initialState[0].length][emissionSequence.length];
            for ( int j = 0; j < initialState[0].length; j++ )
            {
                double prob = -1.0;
                int state;

                for ( int i = 0; i < initialState[0].length; i++ )
                {
                    double temp = delta[t - 1][i] * transMatrix[i][j] * emissionMatrix[j][emissionSequence[t]];
                    // found a more likely path
                    if ( temp > prob )
                    {
                        prob = temp;
                        state = i;
                        delta[t][j] = prob;
                        System.arraycopy( delta2[state], 0, newpath[j], 0, t );
                        newpath[j][t] = j;
                    }
                }
            }
            delta2 = newpath;
        }

        double prob = -1.0;
        int state = 0;
        for ( int i = 0; i < initialState[0].length; i++ )
        {
            if ( delta[emissionSequence.length - 1][i] > prob )
            {
                prob = delta[emissionSequence.length - 1][i];
                state = i;
            }
        }

        return delta2[state];
    }

    /**
     * Writes the HMM to the file specified at creation of the HMM instance
     * 
     * @throws FileNotFoundException
     */
    public void writeToFile() throws FileNotFoundException
    {
        PrintWriter pw = new PrintWriter( new File( path ) );
        pw.print( transMatrix.length + " " + transMatrix[0].length );
        for ( int j = 0; j < transMatrix.length; j++ )
        {
            for ( int i = 0; i < transMatrix[0].length; i++ )
            {
                pw.print( " " + transMatrix[j][i] );
            }
        }
        pw.println();

        pw.print( emissionMatrix.length + " " + emissionMatrix[0].length );
        for ( int j = 0; j < emissionMatrix.length; j++ )
        {
            for ( int i = 0; i < emissionMatrix[0].length; i++ )
            {
                pw.print( " " + emissionMatrix[j][i] );
            }
        }
        pw.println();

        pw.print( initialState.length + " " + initialState[0].length );
        for ( int j = 0; j < initialState.length; j++ )
        {
            for ( int i = 0; i < initialState[0].length; i++ )
            {
                pw.print( " " + initialState[j][i] );
            }
        }
        pw.println();

        pw.flush();
        pw.close();
    }

    public boolean isValid()
    {
        boolean valid = true;

        for ( int j = 0; j < transMatrix.length; j++ )
        {
            double total = 0.0f;
            for ( int i = 0; i < transMatrix[0].length; i++ )
            {
                total += transMatrix[j][i];
            }

            if ( Math.abs( 1 - total ) > 0.00000001 )
            {
                valid = false;
            }
        }

        for ( int j = 0; j < emissionMatrix.length; j++ )
        {
            double total = 0.0f;
            for ( int i = 0; i < emissionMatrix[0].length; i++ )
            {
                total += emissionMatrix[j][i];
            }

            if ( Math.abs( 1 - total ) > 0.00000001 )
            {
                valid = false;
            }
        }

        for ( int j = 0; j < initialState.length; j++ )
        {
            double total = 0.0f;
            for ( int i = 0; i < initialState[0].length; i++ )
            {
                total += initialState[j][i];
            }

            if ( Math.abs( 1 - total ) > 0.00000001 )
            {
                valid = false;
            }
        }

        return valid;
    }

    /**
     * Updates the given matrix with the information from the given line
     * 
     * @param line the line to read the matrix from
     * @param matrix the matrix to update
     */
    private void updateMatrix( String line, double[][] matrix )
    {
        int numColumns = matrix[0].length;
        int index = 0;

        Scanner f = new Scanner( line );

        // ommit the dimensions of the matrix
        f.nextInt();
        f.nextInt();

        while ( f.hasNextDouble() )
        {
            double temp = f.nextDouble();
            int r = index / numColumns;
            int c = index % numColumns;
            matrix[r][c] = temp;
            index++;
        }
        f.close();
    }

    /**
     * Updates the given matrix with random values because it will be easier for learnHMM to converge if the model is randomly generated initially instead of all zeros
     *
     * @param matrix the matrix to update
     */
    private void randomlyUpdateMatrix( double[][] matrix )
    {
        StringBuilder sb = new StringBuilder();
        sb.append( matrix[0].length + " " + matrix.length );

        for ( int i = 0; i < matrix[0].length; ++i )
        {
            for ( int j = 0; j < matrix.length; ++j )
            {
                double temp = 1.0 / matrix.length;
                sb.append( " " + temp );
            }
        }
        updateMatrix( sb.toString(), matrix );
    }

    /**
     * Updates the given vector with the information from the given line
     * 
     * @param line the line to read the matrix from
     * @param vector the vector to update
     */
    private void updateVector( String line, int[] vector )
    {
        int index = 0;
        Scanner f = new Scanner( line );

        // omit the size of the vector
        f.nextInt();

        while ( f.hasNextInt() )
        {
            vector[index] = f.nextInt();
            ;
            index++;
        }
        f.close();
    }

    /**
     * Calculate the dimensions of the matrix given the input line's first ints
     *
     * @param line the line to read the first ints that are the size of the matrix
     */
    private int[] getDimensions( String line )
    {
        int[] dim = new int[2];
        Scanner f = new Scanner( line );
        dim[0] = f.nextInt();
        dim[1] = f.nextInt();
        f.close();
        return dim;
    }
}
