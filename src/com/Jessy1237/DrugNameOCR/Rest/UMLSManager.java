package com.Jessy1237.DrugNameOCR.Rest;

import static io.restassured.RestAssured.given;

import java.util.Iterator;
import java.util.List;

import com.Jessy1237.DrugNameOCR.Util;
import com.Jessy1237.DrugNameOCR.SpellCorrection.StateWeightedLevenshtein;
import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

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
    public SearchResult findDrugInformation( String word, RestSearchType rst )
    {
        SearchResult result = null;
        String tempURI = RestAssured.baseURI;
        RestAssured.baseURI = "https://uts-ws.nlm.nih.gov";

        Response response = given().request().with().param( "ticket", ticketClient.getST( tgt ) ).param( "string", word ).param( "inputType", "atom" ).param( "sabs", "AOD,CHV,NCI,PDQ,MED-RT,MMSL" ).param( "pageSize", "3" ).param( "searchType", rst.name().toLowerCase() ).when()
                .get( "/rest/search/current" );

        if ( response.getStatusCode() == 200 )
        {

            String output = response.getBody().asString();
            try
            {
                result = getRestSearchResult( word, output );
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

    /**
     * Returns the closest match found test result to the original word found within the json string from a rest search get. If the similarity is below 50% then it will return null.
     * 
     * @param word The word used to find the rest search result
     * @param jsonString The json string to parse
     * @return The first search result or null if no search results found
     * @throws JsonException if the json string is invalid
     */
    private SearchResult getRestSearchResult( String word, String jsonString ) throws JsonException
    {
        JsonObject jo = ( JsonObject ) Jsoner.deserialize( jsonString );
        jo = ( JsonObject ) jo.get( "result" );

        JsonArray ja = ( JsonArray ) jo.get( "results" );
        Iterator<?> itr = ja.iterator();
        SearchResult sr = null;
        double sim = 50.0;
        StateWeightedLevenshtein swl = new StateWeightedLevenshtein( util );

        while ( itr.hasNext() )
        {
            jo = ( JsonObject ) itr.next();

            if ( !( ( ( String ) jo.get( "ui" ) ).equalsIgnoreCase( "NONE" ) || ( ( String ) jo.get( "name" ) ).equalsIgnoreCase( "NO RESULTS" ) ) )
            {
                SearchResult temp = new SearchResult( jo );
                double tempSim = 0.0;
                if ( word.contains( " " ) )
                {
                    String[] split = word.split( " " );
                    for ( int i = 0; i < split.length; i++ )
                    {
                        double tempSimWord = swl.similarityPercentage( split[i], temp.getName() );
                        if ( tempSimWord > tempSim )
                        {
                            tempSim = tempSimWord;
                            temp.setClosestWordInLineIndex( i );
                        }
                    }
                }
                else
                {
                    tempSim = swl.similarityPercentage( word, temp.getName() );
                    temp.setSimilarity( tempSim );
                }

                if ( tempSim > sim )
                {
                    sim = tempSim;
                    sr = temp;
                }
            }
        }

        return sr;
    }

    public enum RestSearchType
    {
        WORDS, //Use this if you want the whole line/sentence searched into the UMLS database
        APPROXIMATE, //Use this if you want each separate word searched into the UMLS database with an approximate match
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
