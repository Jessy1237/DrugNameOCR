package com.Jessy1237.DrugNameOCR;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

public class ModelManager
{

    private String modelDirectory;
    private List<File> modelFiles;
    private List<Model> models;

    public ModelManager( String modelDirectory )
    {
        this.modelDirectory = modelDirectory;

        if ( !modelDirectory.endsWith( File.separator ) )
            this.modelDirectory += File.separator;

        File[] files = new File( this.modelDirectory ).listFiles( new FileFilter() {

            @Override
            public boolean accept( File pathname )
            {
                return pathname.getName().toLowerCase().endsWith( "model" );
            }

        } );

        if ( files != null )
        {
            modelFiles = new ArrayList<File>( Arrays.asList( files ) );
        }
        else
        {
            modelFiles = new ArrayList<File>();
        }

        models = new ArrayList<Model>();
    }

    public void init()
    {
        models = new ArrayList<Model>();
    }

    /**
     * Gets the list of models that have been previously read by this manager
     * 
     * @return list of models
     */
    public List<Model> getModels()
    {
        return models;
    }

    /**
     * Reads all the model files as specified by the list of model names in the constructor.
     * 
     * @return The list of models
     * @throws IOException
     * @throws JsonException
     */
    public List<Model> readModels() throws IOException, JsonException
    {
        init();

        for ( File f : modelFiles )
        {
            Model model = readModelFile( f );

            if ( model != null )
            {
                models.add( model );
            }
        }

        return models;
    }

    private Model readModelFile( File file ) throws IOException, JsonException
    {
        Model model = null;

        if ( file.exists() )
        {
            Model temp = new Model();
            JsonObject jo = ( JsonObject ) Jsoner.deserialize( new FileReader( file ) );

            temp.setId( ( String ) jo.get( "id" ) );

            JsonArray ja = ( JsonArray ) jo.get( "rois" );
            Iterator<?> itr = ja.iterator();

            while ( itr.hasNext() )
            {
                temp.getRegionOfInterests().add( new RegionOfInterest( ( String ) itr.next() ) );
            }

            temp.setWidth( Integer.parseInt( ( String ) jo.get( "width" ) ) );
            temp.setHeight( Integer.parseInt( ( String ) jo.get( "height" ) ) );

            ja = ( JsonArray ) jo.get( "boxes" );
            itr = ja.iterator();

            while ( itr.hasNext() )
            {
                temp.getBoundingBoxes().add( new BoundingBox( ( String ) itr.next() ) );
            }

            model = temp;
        }

        return model;
    }

    /**
     * Writes a model to json file
     * 
     * @param model The model to be written to file
     * @throws IOException
     */
    public void writeModelFile( Model model ) throws IOException
    {
        File dir = new File( modelDirectory );
        dir.mkdirs();

        PrintWriter pw = new PrintWriter( modelDirectory + model.getId() + ".model" );
        pw.write( model.toJSONString() );
        pw.flush();
        pw.close();
    }
}
