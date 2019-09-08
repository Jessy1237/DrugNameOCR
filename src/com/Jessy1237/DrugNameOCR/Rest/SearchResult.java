package com.Jessy1237.DrugNameOCR.Rest;

import com.github.cliftonlabs.json_simple.JsonObject;

public class SearchResult
{

    private String ui;
    private String name;
    private String uri;
    private String rootSource;
    private String closestWordInLine;

    /**
     * Builds a SearchResult from the json object representation
     * 
     * @param jo The json object that represents the search result
     */
    public SearchResult( JsonObject jo )
    {
        ui = ( String ) jo.get( "ui" );
        rootSource = ( String ) jo.get( "rootSource" );
        uri = ( String ) jo.get( "uri" );
        name = ( String ) jo.get( "name" );

        if ( ui == null || rootSource == null || uri == null || name == null )
            throw new IllegalArgumentException( "Bad Json String, was unable to fill the SearchResult" );
    }

    /**
     * Builds the search reseult from the json object but also sets the closestWordInLine variable
     * 
     * @param jo The json object to build the search result from
     * @param closestWordInLine Generally is the word used to search for the json object to start with
     */
    public SearchResult( JsonObject jo, String closestWordInLine )
    {
        ui = ( String ) jo.get( "ui" );
        rootSource = ( String ) jo.get( "rootSource" );
        uri = ( String ) jo.get( "uri" );
        name = ( String ) jo.get( "name" );

        if ( ui == null || rootSource == null || uri == null || name == null )
            throw new IllegalArgumentException( "Bad Json String, was unable to fill the SearchResult" );
    }

    public String getUi()
    {

        return this.ui;
    }

    public String getName()
    {

        return this.name;
    }

    public String getUri()
    {

        return this.uri;
    }

    public String getRootSource()
    {

        return this.rootSource;
    }

    public String getClosestWordInLine()
    {
        if ( closestWordInLine == null )
        {
            return name;
        }
        else
        {
            return closestWordInLine;
        }
    }

    public void setUi( String ui )
    {

        this.ui = ui;
    }

    public void setName( String name )
    {

        this.name = name;
    }

    public void setUri( String uri )
    {

        this.uri = uri;
    }

    public void setRootSource( String rootSource )
    {

        this.rootSource = rootSource;
    }

    public void setClosestWordInLine( String closestWordInLine )
    {
        this.closestWordInLine = closestWordInLine;
    }
}
