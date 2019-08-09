package com.Jessy1237.DrugNameOCR;

public class BoundingBox
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

    @Override
    public String toString()
    {
        return "BoundingBox: id=\"" + id + "\" minX=" + minX + " minY=" + minY + " maxX=" + maxX + " maxY=" + maxY;
    }
}
