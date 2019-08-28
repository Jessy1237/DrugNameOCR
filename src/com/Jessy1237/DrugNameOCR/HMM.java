package com.Jessy1237.DrugNameOCR;

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
    private double[] c;
    private double[][] alpha;
    private double[][] beta;
    private double[][][] diGamma;
    private double[][] gamma;
    private int[] dimTrans;
    private int[] dimEm;
    private int[] dimInit;
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

                dimTrans = getDimensions( lines[0] );
                dimEm = getDimensions( lines[1] );
                dimInit = getDimensions( lines[2] );

                transMatrix = new double[dimTrans[0]][dimTrans[1]];
                emissionMatrix = new double[dimEm[0]][dimEm[1]];
                initialState = new double[dimInit[0]][dimInit[1]];
                c = new double[emissionSequence.length];

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
     * Creates an empty HMM with the specified dimensions for the matrices.
     * 
     * @param dimTrans The dimensions for the transition matrix
     * @param dimEm The dimensions for the emission matrix
     * @param dimInit The dimensions for the initial state matrix
     */
    public HMM( int[] dimTrans, int[] dimEm, int[] dimInit )
    {
        transMatrix = new double[dimTrans[0]][dimTrans[1]];
        emissionMatrix = new double[dimEm[0]][dimEm[1]];
        initialState = new double[dimInit[0]][dimInit[1]];
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

    public HMM( double[][] a, double[][] b, double[][] pi, int[] obs )
    {
        transMatrix = a;
        emissionMatrix = b;
        initialState = pi;
        emissionSequence = obs;
        c = new double[obs.length];
        alpha = new double[a.length][obs.length];
        beta = new double[a.length][obs.length];
        diGamma = new double[a.length][a.length][obs.length];
        gamma = new double[a.length][obs.length];
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

    /**
     * Estimate and update model parameters (a,b, and pi) updates this instance of hmm model iteratively according to the observation sequence given
     */
    public void learnHMM()
    {

        // re-estimate the model until it converges
        // decreased # iterations for duck hunt to run faster
        int iterations = 10000;
        int step = 0;
        boolean searching = true;
        double oldLogProb = -Double.MAX_VALUE;
        double logProb;
        double epsilon = 0.0002;

        while ( ( step < iterations ) && searching )
        {
            // re-estimate model
            calcAlpha();
            calcBeta();
            calcDiGamma();
            calcGamma();
            recalcTransMatrix();
            recalcEmissionMatrix();
            recalcInitialState();

            // check for convergence
            logProb = computeLogProb();
            if ( Math.abs( logProb - oldLogProb ) <= epsilon )
            {
                searching = false;
            }
            oldLogProb = logProb;
            step++;
        }
    }

    /**
     * calculate the alpha matrix and c matrix for this hmm instance and emissionSequence
     * 
     * @return the probability of getting emissionSequence givin the model scaled by natural log
     */
    public double computeLogProb()
    {
        // assuming alpha and c matrices are up-to-date
        // update the conversion matrix c
        //calcAlpha();

        double logProb = 0.0;
        for ( int t = 0; t < emissionSequence.length; t++ )
        {
            //if ( c[t] != 0 )
            logProb += Math.log( c[t] );
        }
        return -logProb;
    }

    /**
     * Overloaded method that will set the emissionSequence then call learnHMM() above to recalculate the HMM matrices
     *
     * @param an array of the emissions
     */
    public void learnHMM( int[] emissions )
    {
        emissionSequence = emissions;
        alpha = new double[initialState[0].length][emissions.length];
        c = new double[emissionSequence.length];
        beta = new double[initialState[0].length][emissions.length];
        diGamma = new double[initialState[0].length][initialState[0].length][emissions.length];
        gamma = new double[initialState[0].length][emissions.length];
        learnHMM();
    }

    /**
     * use calcAlpha to get the probability of these observations occuring, but dont save them
     * 
     * @param an array of the emissions
     * @return the probability of the emission sequence given the hmm matrices
     */
    public double getProbability( int[] observations )
    {
        double[][] alphaTemp = new double[initialState[0].length][observations.length];
        double[] cTemp = new double[observations.length];

        cTemp[0] = 0.0;
        for ( int i = 0; i < initialState[0].length; i++ )
        {
            alphaTemp[i][0] = emissionMatrix[i][observations[0]] * initialState[0][i];
            cTemp[0] += alphaTemp[i][0];
        }

        if ( cTemp[0] == 0.0 )
        {
            cTemp[0] = 1e-10;
        }
        cTemp[0] = 1 / cTemp[0];

        for ( int t = 1; t < observations.length; t++ )
        {
            cTemp[t] = 0.0;
            for ( int i = 0; i < initialState[0].length; i++ )
            {
                double sum = 0.0;
                for ( int j = 0; j < initialState[0].length; j++ )
                {
                    sum += transMatrix[j][i] * alphaTemp[j][t - 1];
                }

                alphaTemp[i][t] = emissionMatrix[i][observations[t]] * sum;
                cTemp[t] += alphaTemp[i][t];
            }
            cTemp[t] = 1 / cTemp[t];

        }
        double prob = 0.0;
        for ( int i = 0; i < initialState[0].length; i++ )
        {
            prob += alphaTemp[i][observations.length - 1];
        }

        // returns in the scaled format still!
        return prob;
    }

    /**
     * Writes the HMM to the file specified at creation of the HMM instance
     * 
     * @throws FileNotFoundException
     */
    public void writeToFile() throws FileNotFoundException
    {
        PrintWriter pw = new PrintWriter( new File( path ) );
        pw.print( dimTrans[0] + " " + dimTrans[1] );
        for ( int j = 0; j < dimTrans[1]; j++ )
        {
            for ( int i = 0; i < dimTrans[0]; i++ )
            {
                pw.print( " " + transMatrix[j][i] );
            }
        }
        pw.println();

        pw.print( dimEm[0] + " " + dimEm[1] );
        for ( int j = 0; j < dimEm[1]; j++ )
        {
            for ( int i = 0; i < dimEm[0]; i++ )
            {
                pw.print( " " + emissionMatrix[j][i] );
            }
        }
        pw.println();

        pw.print( dimInit[0] + " " + dimInit[1] );
        for ( int j = 0; j < dimInit[1]; j++ )
        {
            for ( int i = 0; i < dimInit[0]; i++ )
            {
                pw.print( " " + initialState[j][i] );
            }
        }
        pw.println();

        pw.flush();
        pw.close();
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
     * Calculates the alpha matrix using the baum-welch forward algorithm.
     */

    private void calcAlpha()
    {
        // initialize scaler to prevent underflow errors later
        c[0] = 0.0;
        for ( int i = 0; i < initialState[0].length; i++ )
        {
            alpha[i][0] = emissionMatrix[i][emissionSequence[0]] * initialState[0][i];
            c[0] += alpha[i][0];
        }

        // rescale initial alphas
        c[0] = 1 / c[0];
        for ( int i = 0; i < initialState[0].length; i++ )
        {
            this.alpha[i][0] = this.alpha[i][0] * c[0];
        }

        for ( int t = 1; t < emissionSequence.length; t++ )
        {
            c[t] = 0.0;
            for ( int i = 0; i < initialState[0].length; i++ )
            {
                double sum = 0.0;
                for ( int j = 0; j < initialState[0].length; j++ )
                {
                    sum += transMatrix[j][i] * alpha[j][t - 1];
                }

                alpha[i][t] = emissionMatrix[i][emissionSequence[t]] * sum;
                c[t] += alpha[i][t];
            }

            // rescale final alphas
            c[t] = 1 / c[t];
            for ( int i = 0; i < initialState[0].length; i++ )
            {
                alpha[i][t] *= c[t];
            }
        }
    }

    /**
     * Calculates the beta matrix using the backwards algorithm.
     */
    private void calcBeta()
    {
        for ( int i = 0; i < initialState[0].length; i++ )
        {
            beta[i][emissionSequence.length - 1] = c[emissionSequence.length - 1];
        }
        for ( int t = emissionSequence.length - 2; t >= 0; t-- )
        {
            for ( int i = 0; i < initialState[0].length; i++ )
            {
                double sum = 0.0;
                for ( int j = 0; j < initialState[0].length; j++ )
                {
                    sum += transMatrix[i][j] * beta[j][t + 1] * emissionMatrix[j][emissionSequence[t + 1]];
                }

                // use the same scaler from calcAlpha() to scale beta
                beta[i][t] = sum * c[t];
            }
        }
    }

    /**
     * Calculates the di-gamma probability matrix using Baum-Welch alg
     */
    private void calcDiGamma()
    {
        for ( int t = 0; t < emissionSequence.length - 1; t++ )
        {
            for ( int i = 0; i < initialState[0].length; i++ )
            {
                for ( int j = 0; j < initialState[0].length; j++ )
                {
                    diGamma[i][j][t] = alpha[i][t] * transMatrix[i][j] * emissionMatrix[j][emissionSequence[t + 1]] * beta[j][t + 1];
                }
            }
        }
    }

    /**
     * Calculates the gamma function using Baum-Welch alg
     */
    private void calcGamma()
    {
        for ( int t = 0; t < emissionSequence.length - 1; t++ )
        {
            for ( int i = 0; i < initialState[0].length; i++ )
            {
                gamma[i][t] = 0.0;
                for ( int j = 0; j < initialState[0].length; j++ )
                {
                    gamma[i][t] += diGamma[i][j][t];
                }
            }
        }

        for ( int i = 0; i < initialState[0].length; i++ )
        {
            gamma[i][emissionSequence.length - 1] = alpha[i][emissionSequence.length - 1];
        }
    }

    /**
     * uses the di-gamma and gamma distribution to update this hmm instance's transition state probability matrix
     */
    private void recalcTransMatrix()
    {

        for ( int i = 0; i < initialState[0].length; i++ )
        {
            for ( int j = 0; j < initialState[0].length; j++ )
            {
                double numerator = 0.0;
                double denominator = 0.0;
                for ( int t = 0; t < emissionSequence.length - 1; t++ )
                {
                    numerator += diGamma[i][j][t];
                    denominator += gamma[i][t];
                }

                transMatrix[i][j] = numerator / denominator;
            }
        }
    }

    /**
     * uses the gamma distribution to update this hmm instance's emmision probability matrix
     */
    private void recalcEmissionMatrix()
    {

        for ( int i = 0; i < initialState[0].length; i++ )
        {
            for ( int j = 0; j < emissionMatrix[0].length; j++ )
            {
                double numerator = 0.0;
                double denominator = 0.0;
                for ( int t = 0; t < emissionSequence.length; t++ )
                {
                    if ( emissionSequence[t] == j )
                        numerator += gamma[i][t];

                    denominator += gamma[i][t];
                }

                emissionMatrix[i][j] = numerator / denominator;
            }
        }
    }

    /**
     * uses the gamma distribution to update this hmm instance's initial state matrix
     */
    private void recalcInitialState()
    {
        for ( int i = 0; i < initialState[0].length; i++ )
        {
            initialState[0][i] = gamma[i][0];
        }
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
