package com.Jessy1237.DrugNameOCR;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

public class ModelManager
{

    private String modelDirectory;
    private List<String> modelNames;
    private List<Model> models;

    public ModelManager( String modelDirectory, List<String> modelNames )
    {
        this.modelDirectory = modelDirectory;
        this.modelNames = modelNames;

        if ( modelNames == null )
            modelNames = new ArrayList<String>();

        if ( !modelDirectory.endsWith( File.separator ) )
            modelDirectory += File.separator;

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

        for ( String name : modelNames )
        {
            Model model = readModelFile( new File( modelDirectory, name + ".model" ) );

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
            temp.setRegionOfInterest( new BoundingBox( ( String ) jo.get( "regionOfInterest" ) ) );
            temp.setWidth( Integer.parseInt( ( String ) jo.get( "width" ) ) );
            temp.setHeight( Integer.parseInt( ( String ) jo.get( "height" ) ) );

            JsonArray ja = ( JsonArray ) jo.get( "boxes" );
            Iterator<?> itr = ja.iterator();

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
     * @throws FileNotFoundException If the model cannot be written to file
     */
    public void writeModelFile( Model model ) throws FileNotFoundException
    {
        PrintWriter pw = new PrintWriter( modelDirectory + model.getId() + ".model" );
        pw.write( model.toJSONString() );
        pw.flush();
        pw.close();
    }
}
