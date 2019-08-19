package com.Jessy1237.DrugNameOCR;

import java.util.ArrayList;

public class ModelManagerTest
{

    public static void main( String[] args )
    {

        try
        {
            ArrayList<String> names = new ArrayList<String>();
            for ( int i = 1; i < args.length; i++ )
            {
                names.add( args[i] );
                System.out.println( args[i] );
            }
            ModelManager mm = new ModelManager( args[0], names );

            mm.readModels();

            for ( Model model : mm.getModels() )
            {
                System.out.println( model.toJSONString() );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

}
