package com.Jessy1237.DrugNameOCR.Models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

public class Model
{
    private List<BoundingBox> boxes;
    private String id;
    private List<RegionOfInterest> rois;
    private int width;
    private int height;

    public Model()
    {
        id = "";
        boxes = new ArrayList<BoundingBox>();
        rois = new ArrayList<RegionOfInterest>();
        width = -1;
        height = -1;
    }

    public Model( String id, List<BoundingBox> boxes, List<RegionOfInterest> rois, int width, int height )
    {
        this.id = id;
        this.boxes = boxes;
        this.rois = rois;
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

    public List<RegionOfInterest> getRegionOfInterests()
    {
        return rois;
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

    public void setRegionOfInterests( List<RegionOfInterest> rois )
    {
        this.rois = rois;
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
        boolean valid = !( boxes.isEmpty() || id.isEmpty() );

        for ( RegionOfInterest roi : rois )
        {
            valid = valid && roi.isValid();
        }

        return valid;
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

        JsonArray ja = new JsonArray();
        for ( RegionOfInterest roi : rois )
        {
            ja.add( roi.toString() );
        }

        jo.put( "rois", ja );
        jo.put( "width", width + "" );
        jo.put( "height", height + "" );

        ja = new JsonArray();

        for ( BoundingBox bb : boxes )
        {
            ja.add( bb.toString() );
        }

        jo.put( "boxes", ja );

        return Jsoner.prettyPrint( jo.toJson() );
    }

    //This was the old code which recursively checked every possible combination to find the max overlapping similarity. Later realised it would compute over a trillion combinations so this method isn't viable.
    //    /**
    //     * Calculates the percentage similarity between the given bounding boxes and this model. It does this by recursively pairing a bounding box with a box in the model until it finds the combination
    //     * that achieves the highest percentage of overlapping ("similar") bounding boxes. When the bounding boxes are compared a copy of them is scaled to fit the dimensions of this model.
    //     * 
    //     * @param bbs The collection of BBs which represent the text regions in a given image.
    //     * @param imgW The image width from which the collection of BBs was formed
    //     * @param imgH The image height from which the collection of BBs was formed
    //     * @return The percentage similarity
    //     */
    //    public double calculateSimilarity( Collection<BoundingBox> bbs, int imgW, int imgH )
    //    {
    //        double similarity = 0.0f;
    //
    //        List<BoundingBox> scaledBBs = scaleBBs( bbs, imgW, imgH );
    //
    //        similarity = calculateMaxSimilarity( scaledBBs, boxes, 0 );
    //
    //        return similarity;
    //    }
    //
    //    /**
    //     * Finds the bounding box in the list of model boxes that gives the max similarity.
    //     * 
    //     * @param modelBBs The list of model bounding boxes
    //     * @param bb The bounding box to compare to the list of model boxes
    //     * @return A result object which contains the max similarity and the model box that gave this max similarity
    //     */
    //    private Result findMaxSimilarity( List<BoundingBox> modelBBs, BoundingBox bb )
    //    {
    //        double maxSim = -1.0f;
    //        BoundingBox maxBB = null;
    //
    //        for ( BoundingBox modelBB : modelBBs )
    //        {
    //            double sim = bb.compareSimilarity( modelBB );
    //
    //            if ( sim > maxSim )
    //            {
    //                maxSim = sim;
    //                maxBB = modelBB;
    //            }
    //        }
    //
    //        return new Result( maxBB, maxSim );
    //    }
    //
    //    /**
    //     * This is the private recursive method to calculate the max similarity of each combination of boxes from the images to the boxes from the model.
    //     * 
    //     * @param bbs The list of bounding boxes found from the image
    //     * @param modelBBs The bounding boxes in the model
    //     * @param n The current recursive level you are in. i.e. start at 0
    //     * @return The max similarity found by comparing the image bounding boxes to the model boxes
    //     */
    //    private double calculateMaxSimilarity( List<BoundingBox> bbs, List<BoundingBox> modelBBs, int n )
    //    {
    //
    //        double maxSim = 0.0f;
    //
    //        if ( bbs.isEmpty() || modelBBs.isEmpty() )
    //        {
    //            maxSim = 0.0f;
    //        }
    //        else
    //        {
    //
    //            for ( BoundingBox bb : bbs )
    //            {
    //                Result res = findMaxSimilarity( modelBBs, bb );
    //
    //                List<BoundingBox> tempBBs = new ArrayList<BoundingBox>( bbs );
    //                List<BoundingBox> tempModelBBs = new ArrayList<BoundingBox>( modelBBs );
    //                tempBBs.remove( bb );
    //                tempModelBBs.remove( res.getBB() );
    //
    //                double tempSim = res.getSim() + calculateMaxSimilarity( tempBBs, tempModelBBs, n + 1 );
    //
    //                if ( tempSim > maxSim )
    //                {
    //                    maxSim = tempSim;
    //                }
    //            }
    //        }
    //
    //        if ( n == 0 )
    //        {
    //            maxSim /= ( double ) bbs.size();
    //        }
    //
    //        return maxSim;
    //    }

    public double calculateSimilarity( Collection<BoundingBox> bbs, int imgW, int imgH )
    {
        double sim = 0.0f;
        List<BoundingBox> scaledBBs = scaleBBs( bbs, imgW, imgH );
        int length = boxes.size();

        if ( isValid() && !scaledBBs.isEmpty() )
        {
            if ( scaledBBs.size() == boxes.size() )
            {
                int totalOverlapArea = 0;
                int totalArea = 0;
                for ( int i = 0; i < length; i++ )
                {
                    int[] result = boxes.get( i ).findOverlapArea( scaledBBs.get( i ) );
                    totalOverlapArea += result[0];
                    totalArea += result[1];
                }
                sim = ( double ) totalOverlapArea / ( double ) totalArea;
            }
            else if ( scaledBBs.size() > boxes.size() )
            {
                for ( int skip = 0; skip < scaledBBs.size(); skip++ )
                {
                    double tempSim = 0.0f;
                    int totalOverlapArea = 0;
                    int totalArea = 0;

                    for ( int i = 0; i < length; i++ )
                    {
                        int[] result;
                        if ( i < skip )
                        {
                            result = boxes.get( i ).findOverlapArea( scaledBBs.get( i ) );
                        }
                        else
                        {
                            result = boxes.get( i ).findOverlapArea( scaledBBs.get( i + 1 ) );
                        }

                        totalOverlapArea += result[0];
                        totalArea += result[1];
                    }

                    tempSim = ( double ) totalOverlapArea / ( double ) totalArea;

                    if ( tempSim > sim )
                    {
                        sim = tempSim;
                    }
                }
            }
            else if ( boxes.size() > scaledBBs.size() )
            {
                length = scaledBBs.size();
                for ( int skip = 0; skip < boxes.size(); skip++ )
                {
                    double tempSim = 0.0f;
                    int totalOverlapArea = 0;
                    int totalArea = 0;

                    for ( int i = 0; i < length; i++ )
                    {
                        int[] result;
                        if ( i < skip )
                        {
                            result = boxes.get( i ).findOverlapArea( scaledBBs.get( i ) );
                        }
                        else
                        {
                            result = boxes.get( i + 1 ).findOverlapArea( scaledBBs.get( i ) );
                        }

                        totalOverlapArea += result[0];
                        totalArea += result[1];
                    }

                    tempSim = ( double ) totalOverlapArea / ( double ) totalArea;

                    if ( tempSim > sim )
                    {
                        sim = tempSim;
                    }
                }
            }
        }

        return sim;
    }

    /**
     * Scales the given collection of bounding box dimensions to fit the model dimensions.
     * 
     * @param bbs The collection of bounding boxes to scale to the model dimensions
     * @param imgW The width of the image that the bounding boxes were from
     * @param imgH The height of the image that the bounding boxes were from
     * @return The list of scaled bounding boxes
     */
    private List<BoundingBox> scaleBBs( Collection<BoundingBox> bbs, int imgW, int imgH )
    {
        ArrayList<BoundingBox> scaledBBs = new ArrayList<BoundingBox>();

        for ( BoundingBox bb : bbs )
        {
            scaledBBs.add( scaleBB( bb, imgW, imgH ) );
        }

        return scaledBBs;
    }

    /**
     * Scales the given bounding box dimensions to fit the model dimensions.
     * 
     * @param bb The bounding box to scale to the model dimensions
     * @param imgW The width of the image that the bounding box was from
     * @param imgH The height of the image that the bounding box was from
     * @return The scaled bounding box
     */
    private BoundingBox scaleBB( BoundingBox bb, int imgW, int imgH )
    {
        return new BoundingBox( ( int ) ( ( double ) ( bb.getMinX() / ( double ) imgW * ( double ) width ) ), ( int ) ( ( double ) ( bb.getMinY() / ( double ) imgH * ( double ) height ) ), ( int ) ( ( double ) ( bb.getMaxX() / ( double ) imgW
                * ( double ) width ) ), ( int ) ( ( double ) ( bb.getMaxY() / ( double ) imgH * ( double ) height ) ), bb.getId() );
    }

    //    private class Result
    //    {
    //
    //        private BoundingBox bb;
    //        private double sim;
    //
    //        public Result( BoundingBox bb, double sim )
    //        {
    //            this.bb = bb;
    //            this.sim = sim;
    //        }
    //
    //        public BoundingBox getBB()
    //        {
    //            return bb;
    //        }
    //
    //        public double getSim()
    //        {
    //            return sim;
    //        }
    //    }
}
