package com.Jessy1237.DrugNameOCR;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.Jessy1237.DrugNameOCR.Rest.UMLSManager;
import com.Jessy1237.DrugNameOCR.Rest.UMLSManager.RestSearchType;
import com.github.cliftonlabs.json_simple.JsonException;

public class FindAllDrugInformationTest
{
    public static void main( String[] args ) throws JsonException, IOException
    {
        if ( args.length != 2 )
        {
            System.out.println( "ARGS: \"<UMLS API Key>\" <DrugName List text file>" );
        }
        else
        {
            Util util = new Util();
            UMLSManager um = new UMLSManager( args[0], util );
            BufferedReader br = new BufferedReader( new FileReader( args[1] ) );
            String line = br.readLine();
            ArrayList<String> lines = new ArrayList<String>();

            while ( line != null )
            {
                if ( line.startsWith( "#" ) )
                {
                    line = br.readLine();
                    continue;
                }

                lines.add( line );
                line = br.readLine();
            }

            String[] linesArray = new String[lines.size()];
            linesArray = lines.toArray( linesArray );

            List<String[]>[] drugNames = util.findAllDrugNames( um, linesArray, RestSearchType.WORDS );

            for ( int i = 0; i < drugNames.length; i++ )
            {
                System.out.print( "At line " + i + " found the following drugs ('<name>':'<associated name>'): " );
                if ( drugNames[i].size() == 0 )
                {
                    System.out.println( "NOTHING" );
                }
                else
                {
                    System.out.println();
                }

                for ( String[] names : drugNames[i] )
                {
                    System.out.println( "'" + names[0] + "':'" + names[1] + "'" );
                }
            }

            br.close();
        }
    }
}
