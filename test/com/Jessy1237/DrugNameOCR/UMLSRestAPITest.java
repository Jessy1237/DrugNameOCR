package com.Jessy1237.DrugNameOCR;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.Jessy1237.DrugNameOCR.Rest.SearchResult;
import com.Jessy1237.DrugNameOCR.Rest.UMLSManager;
import com.github.cliftonlabs.json_simple.JsonException;

public class UMLSRestAPITest
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

            while ( line != null )
            {
                if ( line.startsWith( "#" ) )
                {
                    line = br.readLine();
                    continue;
                }

                for ( String word : line.split( " " ) )
                {
                    SearchResult result = um.findDrugInformation( word );

                    if ( result == null )
                    {
                        System.out.println( "no results found" );
                    }
                    else
                    {
                        System.out.println( "Associated Name: " + result.getName() );
                        System.out.println( "CUI: " + result.getUi() );
                        System.out.println( "Associated TUIs: " + um.findSemanticTUIs( result.getUi() ) );
                    }
                }
                line = br.readLine();
            }

            br.close();
        }
    }
}
