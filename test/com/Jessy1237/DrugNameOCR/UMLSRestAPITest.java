package com.Jessy1237.DrugNameOCR;

import static io.restassured.RestAssured.given;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.Jessy1237.DrugNameOCR.Rest.RestTicketClient;
import com.Jessy1237.DrugNameOCR.Rest.SearchResult;
import com.github.cliftonlabs.json_simple.JsonException;

import io.restassured.RestAssured;
import io.restassured.response.Response;

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
            RestTicketClient ticketClient = new RestTicketClient( args[0] );
            String tgt = ticketClient.getTgt();
            RestAssured.baseURI = "https://uts-ws.nlm.nih.gov";

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
                    System.out.println( "Word:" + word );
                    Response response = given().request().with().param( "ticket", ticketClient.getST( tgt ) ).param( "string", word ).param( "inputType", "atom" ).param( "searchType", "approximate" ).when().get( "/rest/search/current" );

                    if ( response.getStatusCode() == 200 )
                    {

                        String output = response.getBody().asString();
                        SearchResult result = util.getRestSearchResult( output );

                        if ( result == null )
                        {
                            System.out.println( "no results found" );
                        }
                        else
                        {
                            System.out.println( "Associated Name: " + result.getName() );
                            System.out.println( "CUI: " + result.getUi() );
                            
                            response = given().request().with().param( "ticket", ticketClient.getST( tgt ) ).expect().statusCode( 200 ).when().get( "/rest/content/current/CUI/" + result.getUi() );
                            output = response.getBody().asString();
                            System.out.println( "Associated TUIs: " + util.getSemanticTUIs( output ) );
                        }
                    }
                    else
                    {
                        System.out.println( "Page not Found" );
                    }
                }
                line = br.readLine();
            }

            br.close();
        }

    }
}
