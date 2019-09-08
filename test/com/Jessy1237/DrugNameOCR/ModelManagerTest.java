package com.Jessy1237.DrugNameOCR;

import com.Jessy1237.DrugNameOCR.Models.Model;
import com.Jessy1237.DrugNameOCR.Models.ModelManager;

public class ModelManagerTest
{

    public static void main( String[] args )
    {

        try
        {
            if ( args.length != 1 )
            {
                System.out.println( "ARGS: <model directory>" );
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
