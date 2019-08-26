package com.Jessy1237.DrugNameOCR;

public class ModelManagerTest
{

    public static void main( String[] args )
    {

        try
        {
            if ( args.length != 1 )
            {
                System.out.println( "USAGE: <model directory>" );
            }
            else
            {
                ModelManager mm = new ModelManager( args[0] );
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
