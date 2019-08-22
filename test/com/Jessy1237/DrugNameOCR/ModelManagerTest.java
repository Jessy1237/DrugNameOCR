package com.Jessy1237.DrugNameOCR;

import java.util.ArrayList;

public class ModelManagerTest
{

    public static void main( String[] args )
    {

        try
        {
            if ( args.length < 2 )
            {
                System.out.println( "USAGE: <model directory> <model 1> <model 2> ..." );
            }
            else
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
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

}
