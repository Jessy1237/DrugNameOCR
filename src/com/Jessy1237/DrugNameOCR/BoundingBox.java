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
        id = " ";
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
        StringTokenizer st = new StringTokenizer( bb );

        minX = -2;
        minY = -2;
        maxX = -2;
        maxY = -2;
        id = "";

        while ( st.hasMoreTokens() )
        {
            String token = st.nextToken();

            if ( token.contains( "minX=" ) )
            {
                String[] split = token.split( "=" );
                if ( split.length == 2 )
                {
                    minX = Integer.parseInt( split[1] );
                }
            }

            if ( token.contains( "minY=" ) )
            {
                String[] split = token.split( "=" );
                if ( split.length == 2 )
                {
                    minY = Integer.parseInt( split[1] );
                }
            }

            if ( token.contains( "maxX=" ) )
            {
                String[] split = token.split( "=" );
                if ( split.length == 2 )
                {
                    maxX = Integer.parseInt( split[1] );
                }
            }

            if ( token.contains( "maxY=" ) )
            {
                String[] split = token.split( "=" );
                if ( split.length == 2 )
                {
                    maxY = Integer.parseInt( split[1] );
                }
            }
        }

        st = new StringTokenizer( bb, "\"" );
        while ( st.hasMoreTokens() )
        {
            String token = st.nextToken();
            if ( token.equalsIgnoreCase( "BoundingBox: id=" ) )
            {
                id = st.nextToken();
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

    @Override
    public String toString()
    {
        return "BoundingBox: id=\"" + id + "\" minX=" + minX + " minY=" + minY + " maxX=" + maxX + " maxY=" + maxY;
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
