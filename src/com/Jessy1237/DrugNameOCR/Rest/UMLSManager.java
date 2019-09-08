package com.Jessy1237.DrugNameOCR.Rest;

import static io.restassured.RestAssured.given;

import java.util.List;

import com.Jessy1237.DrugNameOCR.Util;
import com.github.cliftonlabs.json_simple.JsonException;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class UMLSManager
{
    RestTicketClient ticketClient;
    String tgt;
    Util util;

    public UMLSManager( String apiKey, Util util )
    {
        ticketClient = new RestTicketClient( apiKey );
        tgt = ticketClient.getTgt();
        this.util = util;
    }

    /**
     * Finds the Drug Information for the supplied word for the closest 3 matches. If the word cannot be found in the UMLS database then a null search result is returned.
     * 
     * @param word The word to search for in the UMLS database
     * @return The search result holding all the information from the UMLS database or null, if no result is found for the word, a https issue or there is a JSON error
     */
    public SearchResult findDrugInformation( String word )
    {
        SearchResult result = null;
        String tempURI = RestAssured.baseURI;
        RestAssured.baseURI = "https://uts-ws.nlm.nih.gov";

        Response response = given().request().with().param( "ticket", ticketClient.getST( tgt ) ).param( "string", word ).param( "inputType", "atom" ).param( "pageSize", "3" ).param( "searchType", "approximate" ).when().get( "/rest/search/current" );

        if ( response.getStatusCode() == 200 )
        {

            String output = response.getBody().asString();
            try
            {
                result = util.getRestSearchResult( word, output );
            }
            catch ( JsonException e )
            {
                result = null;
            }
        }

        RestAssured.baseURI = tempURI;
        return result;
    }

    /**
     * Finds the semantic type identifiers for the given concept indentifier in the UMLS database.
     * 
     * @param cui The concept indentifier
     * @return A list containing all the TUIs or null, if there is a https issue or a json error
     */
    public List<String> findSemanticTUIs( String cui )
    {
        List<String> tuis = null;
        String tempURI = RestAssured.baseURI;
        RestAssured.baseURI = "https://uts-ws.nlm.nih.gov";

        Response response = given().request().with().param( "ticket", ticketClient.getST( tgt ) ).when().get( "/rest/content/current/CUI/" + cui );

        if ( response.getStatusCode() == 200 )
        {
            String output = response.getBody().asString();
            try
            {
                tuis = util.getSemanticTUIs( output );
            }
            catch ( JsonException e )
            {
                tuis = null;
            }
        }

        RestAssured.baseURI = tempURI;
        return tuis;
    }

    public enum RestSearchType
    {
        WORD, //Use this if you want the whole line/sentence searched into the UMLS database
        APPROX, //Use this if you want each separate word searched into the UMLS database with an approximate match
        EXACT; //Use this if you want each separate word searched into the UMLS database with an exact match

        RestSearchType()
        {
        }
    }

    //TODO: Could alternatively change this to be a hashmap loaded in at runtime of mapping of TUI to Name.
    public enum DrugTUIs
    {
        ANTIBIOTIC( "T195" ),
        CLINICAL_DRUG( "T200" ),
        PHARMACOLOGIC_SUBSTANCE( "T121" ),
        VITAMIN( "T127" );

        private String tui;

        DrugTUIs( String tui )
        {
            this.tui = tui;
        }

        public String getTUI()
        {
            return tui;
        }

        public static boolean contains( String tui )
        {
            boolean contains = false;
            for ( DrugTUIs dt : values() )
            {
                if ( dt.getTUI().equalsIgnoreCase( tui ) )
                {
                    contains = true;
                }
            }

            return contains;
        }
    }
}
