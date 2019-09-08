package com.Jessy1237.DrugNameOCR.SpellCorrection;

import com.Jessy1237.DrugNameOCR.Util;
import com.Jessy1237.DrugNameOCR.Util.CharacterCorruption;

/**
 * This class is based off of the WeightedLevenshtein class from
 * <a href="https://github.com/tdebatty/java-string-similarity/blob/master/src/main/java/info/debatty/java/stringsimilarity/WeightedLevenshtein.java"> java-string-similarity</a> by tdebatty. However
 * instead of comparing characters we will be comparing the states from the spell correction HMM, as we treat insertions and deletions as substitutions of 1 char with 2 or 2 chars with 1 respectively.
 * 
 * @author Jesse Sieunarine
 */
public class StateWeightedLevenshtein
{

    private Util util;

    public StateWeightedLevenshtein( Util util )
    {
        this.util = util;
    }

    /**
     * Equivalent to distance(s1, s2, Double.MAX_VALUE).
     */
    public final double similarityPercentage( final String s1, final String s2 )
    {
        return similarityPercentage( s1, s2, Double.MAX_VALUE );
    }

    public final double similarityPercentage( final String str1, final String str2, final double limit )
    {
        if ( str1 == null )
        {
            throw new NullPointerException( "s1 must not be null" );
        }

        if ( str2 == null )
        {
            throw new NullPointerException( "s2 must not be null" );
        }

        if ( str1.equalsIgnoreCase( str2 ) )
        {
            return 100.0;
        }

        int[] states1 = util.convertStringToStates( str1 );
        int[] states2 = util.convertStringToStates( str2 );

        double distance = distance( states1, states2, limit );
        int length = ( states1.length >= states2.length ? states1.length : states2.length );

        return ( ( 1.0 - distance / ( double ) length ) * 100.0 );

    }

    /**
     * Equivalent to distance(s1, s2, Double.MAX_VALUE).
     */
    public final double distance( final String s1, final String s2 )
    {
        return distance( s1, s2, Double.MAX_VALUE );
    }

    /**
     * Compute Levenshtein distance using provided weights for substitution. Instead of comparing characters as you normally would for Levenshtein distance we are instead using the same process but
     * comparing our character states, which include insertions and deletions already.
     * 
     * @param s1 The first string to compare.
     * @param s2 The second string to compare.
     * @param limit The maximum result to compute before stopping. This means that the calculation can terminate early if you only care about strings with a certain similarity. Set this to
     *            Double.MAX_VALUE if you want to run the calculation to completion in every case.
     * @return The computed weighted Levenshtein distance.
     * @throws NullPointerException if s1 or s2 is null.
     */
    public final double distance( final String str1, final String str2, final double limit )
    {
        if ( str1 == null )
        {
            throw new NullPointerException( "s1 must not be null" );
        }

        if ( str2 == null )
        {
            throw new NullPointerException( "s2 must not be null" );
        }

        if ( str1.equalsIgnoreCase( str2 ) )
        {
            return 0;
        }

        int[] states1 = util.convertStringToStates( str1 );
        int[] states2 = util.convertStringToStates( str2 );

        return distance( states1, states2, limit );
    }

    private double distance( int[] states1, int[] states2, final double limit )
    {
        if ( states1.length == 0 )
        {
            return states2.length;
        }

        if ( states2.length == 0 )
        {
            return states1.length;
        }

        // create two work vectors of floating point (i.e. weighted) distances
        double[] v0 = new double[states2.length + 1];
        double[] v1 = new double[states2.length + 1];
        double[] vtemp;

        // initialize v0 (the previous row of distances)
        // this row is A[0][i]: edit distance for an empty s1
        // the distance is the cost of inserting each character of s2
        v0[0] = 0;
        for ( int i = 1; i < v0.length; i++ )
        {
            v0[i] = v0[i - 1] + 1.0;
        }

        for ( int i = 0; i < states1.length; i++ )
        {
            int s1i = states1[i];
            double deletion_cost = 1.0;

            // calculate v1 (current row distances) from the previous row v0
            // first element of v1 is A[i+1][0]
            // Edit distance is the cost of deleting characters from s1
            // to match empty t.
            v1[0] = v0[0] + deletion_cost;

            double minv1 = v1[0];

            // use formula to fill in the rest of the row
            for ( int j = 0; j < states2.length; j++ )
            {
                int s2j = states2[j];
                double cost = 0;
                if ( s1i != s2j )
                {
                    cost = substitutionCost( s1i, s2j );
                }
                double insertion_cost = 1.0;
                v1[j + 1] = Math.min( v1[j] + insertion_cost, // Cost of insertion
                        Math.min( v0[j + 1] + deletion_cost, // Cost of deletion
                                v0[j] + cost ) ); // Cost of substitution

                minv1 = Math.min( minv1, v1[j + 1] );
            }

            if ( minv1 >= limit )
            {
                return limit;
            }

            // copy v1 (current row) to v0 (previous row) for next iteration
            //System.arraycopy(v1, 0, v0, 0, v0.length);
            // Flip references to current and previous row
            vtemp = v0;
            v0 = v1;
            v1 = vtemp;

        }

        return v0[states2.length];
    }

    private double substitutionCost( int state1, int state2 )
    {
        // The cost for substituting a known corruption should be considered smaller as these are known errors for OCR.
        if ( CharacterCorruption.values()[state1].containsCorruption( util.convertStatesToString( new int[] { state2 } ) ) )
        {
            return 0.5;//TODO: Investigate an optimal weighted value for valid spell corrected emissions/states
        }

        //For most other cases the cost should be 1.0 for substituting 2 characters
        return 1.0;
    }
}
