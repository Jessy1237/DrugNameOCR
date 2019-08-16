package com.Jessy1237.DrugNameOCR;

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
    }

    public BoundingBox( int minX, int minY, int maxX, int maxY, String id )
    {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.id = id;
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
