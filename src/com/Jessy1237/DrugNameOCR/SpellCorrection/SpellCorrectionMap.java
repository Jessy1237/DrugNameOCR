package com.Jessy1237.DrugNameOCR.SpellCorrection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

public class SpellCorrectionMap implements Serializable
{
    private static final long serialVersionUID = -1210659449512298100L;
    HashMap<String, String> previousResults;
    String path;

    @SuppressWarnings( "unchecked" )
    public SpellCorrectionMap( String path )
    {
        this.path = path;
        File f = new File( path );

        if ( f.exists() )
        {
            try ( ObjectInputStream is = new ObjectInputStream( new FileInputStream( f ) ) )
            {
                previousResults = ( HashMap<String, String> ) is.readObject();
            }
            catch ( Exception e )
            {
                previousResults = new HashMap<String, String>();
            }
        }
        else
        {
            previousResults = new HashMap<String, String>();
        }
    }

    public void save()
    {
        try ( ObjectOutputStream os = new ObjectOutputStream( new FileOutputStream( path ) ) )
        {
            os.writeObject( previousResults );
        }
        catch ( IOException e )
        {
            //Unable to save map
        }
    }

    /**
     * Gets the previous correct spelling for this word.
     * 
     * @param word The word to check for previous correct spelling
     * @return the previous correct spelling if it exists or null if none exists
     */
    public String getPreviousSpellCorrection( String word )
    {
        return previousResults.get( word );
    }

    public void addNewSpellCorrection( String word, String correction )
    {
        previousResults.put( word, correction );
    }
}
