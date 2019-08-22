package com.Jessy1237.DrugNameOCR;

import java.util.StringTokenizer;

public class BoundingBox implements Cloneable
{
    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private String id;

    public BoundingBox()
    {
        minX = -1;
        minY = -1;
        maxX = -1;
        maxY = -1;
        id = "blank";
    }

    public BoundingBox( int minX, int minY, int maxX, int maxY, String id )
    {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.id = id;
    }

    public BoundingBox( String bb )
    {
        minX = -2;
        minY = -2;
        maxX = -2;
        maxY = -2;
        id = "";

        StringTokenizer st = new StringTokenizer( bb, "'" );
        while ( st.hasMoreTokens() )
        {
            String token = st.nextToken().trim();
            token = token.replace( "BoundingBox:", "" );
            token = token.trim();

            if ( token.equalsIgnoreCase( "id=" ) )
            {
                id = st.nextToken().trim();
            }

            if ( token.equalsIgnoreCase( "minX=" ) )
            {
                minX = Integer.parseInt( st.nextToken().trim() );
            }

            if ( token.equalsIgnoreCase( "minY=" ) )
            {
                minY = Integer.parseInt( st.nextToken().trim() );
            }

            if ( token.equalsIgnoreCase( "maxX=" ) )
            {
                maxX = Integer.parseInt( st.nextToken().trim() );
            }

            if ( token.equalsIgnoreCase( "maxY=" ) )
            {
                maxY = Integer.parseInt( st.nextToken().trim() );
            }
        }

        if ( minX == -2 || maxX == -2 || minY == -2 | maxY == -2 || id.isEmpty() )
        {
            throw new IllegalArgumentException( "Unable to parse the input string into a bounding box" );
        }
    }

    public int getMinX()
    {
        return minX;
    }

    public int getMaxX()
    {
        return maxX;
    }

    public int getMinY()
    {
        return minY;
    }

    public int getMaxY()
    {
        return maxY;
    }

    public String getId()
    {
        return id;
    }

    public void setMinX( int minX )
    {
        this.minX = minX;
    }

    public void setMaxX( int maxX )
    {
        this.maxX = maxX;
    }

    public void setMinY( int minY )
    {
        this.minY = minY;
    }

    public void setMaxY( int maxY )
    {
        this.maxY = maxY;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public boolean isValid()
    {
        return ( minX != -1 && minY != -1 && maxX != -1 && maxY != -1 );
    }

    public void addId( String id )
    {
        this.id += "-" + id;
    }

    /**
     * Compares the similarity to the supplied BB. i.e. finds the percentage overlap.
     * 
     * @param bb The bounding box to compare to
     * @return The percentage of overlap
     */
    public double compareSimilarity( BoundingBox bb )
    {
        System.out.println( id + " " + bb.getId() + " " + ( maxX - minX ) + " " + ( maxY - minY ) );
        int area = ( maxX - minX ) * ( maxY - minY );
        int overlapArea = 0;
        int overlapWidth = 0;
        int overlapHeight = 0;

        //Find the width of the overlapping area
        if ( bb.getMinX() <= minX && bb.getMaxX() > minX )
        {
            if ( bb.getMaxX() <= maxX )
            {
                overlapWidth = bb.getMaxX() - minX;
            }
            else
            {
                overlapWidth = maxX - minX;
            }
        }
        else if ( minX <= bb.getMinX() && maxX > bb.getMinX() )
        {
            if ( bb.getMaxX() <= maxX )
            {
                overlapWidth = bb.getMaxX() - bb.getMinX();
            }
            else
            {
                overlapWidth = maxX - bb.getMinX();
            }
        }

        //Find the height of the overlapping area
        if ( bb.getMinY() <= minY && bb.getMaxY() > minY )
        {
            if ( bb.getMaxY() <= maxY )
            {
                overlapHeight = bb.getMaxY() - minY;
            }
            else
            {
                overlapHeight = maxY - minY;
            }
        }
        else if ( minY <= bb.getMinY() && maxY > bb.getMinY() )
        {
            if ( bb.getMaxY() <= maxY )
            {
                overlapHeight = bb.getMaxY() - bb.getMinY();
            }
            else
            {
                overlapHeight = maxY - bb.getMinY();
            }
        }

        overlapArea = overlapWidth * overlapHeight;

        return ( double ) ( overlapArea / area );
    }

    @Override
    public String toString()
    {
        return "BoundingBox: id='" + id + "' minX='" + minX + "' minY='" + minY + "' maxX='" + maxX + "' maxY='" + maxY + "'";
    }

    @Override
    public boolean equals( Object obj )
    {
        boolean equal = false;

        if ( obj != null )
        {
            if ( obj instanceof BoundingBox )
            {
                BoundingBox bb = ( BoundingBox ) obj;
                if ( bb.getMinX() == minX && bb.getMaxX() == maxX && bb.getMinY() == minY && bb.getMaxY() == maxY && bb.getId().equalsIgnoreCase( id ) )
                {
                    equal = true;
                }
            }
        }

        return equal;
    }

    @Override
    public Object clone()
    {
        BoundingBox bb = new BoundingBox( minX, minY, maxX, maxY, id );
        return bb;
    }
}
