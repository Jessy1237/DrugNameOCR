package com.Jessy1237.DrugNameOCR;

import java.util.ArrayList;
import java.util.List;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

public class Model
{
    private List<BoundingBox> boxes;
    private String id;
    private BoundingBox regionOfInterest;
    private int width;
    private int height;

    public Model()
    {
        id = "";
        boxes = new ArrayList<BoundingBox>();
        regionOfInterest = new BoundingBox();
        width = -1;
        height = -1;
    }

    public Model( String id, List<BoundingBox> boxes, BoundingBox regionOfInterest, int width, int height )
    {
        this.id = id;
        this.boxes = boxes;
        this.regionOfInterest = regionOfInterest;
        this.width = width;
        this.height = height;
    }

    public List<BoundingBox> getBoundingBoxes()
    {
        return boxes;
    }

    public String getId()
    {
        return id;
    }

    public BoundingBox getRegionOfInterest()
    {
        return regionOfInterest;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setBoundingBoxes( List<BoundingBox> boxes )
    {
        this.boxes = boxes;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public void setRegionOfInterest( BoundingBox roi )
    {
        regionOfInterest = roi;
    }

    public void setWidth( int width )
    {
        this.width = width;
    }

    public void setHeight( int height )
    {
        this.height = height;
    }

    public boolean isValid()
    {
        return !( boxes.isEmpty() || id.isEmpty() ) && regionOfInterest.isValid();
    }

    /**
     * Converts the model into a pretty JSON String ready to be printed to file
     * 
     * @return the pretty json string
     */
    public String toJSONString()
    {
        JsonObject jo = new JsonObject();

        jo.put( "id", id );
        jo.put( "regionOfInterest", regionOfInterest.toString() );
        jo.put( "width", width + "" );
        jo.put( "height", height + "" );

        JsonArray ja = new JsonArray();

        for ( BoundingBox bb : boxes )
        {
            ja.add( bb.toString() );
        }

        jo.put( "boxes", ja );

        return Jsoner.prettyPrint( jo.toJson() );
    }
}
