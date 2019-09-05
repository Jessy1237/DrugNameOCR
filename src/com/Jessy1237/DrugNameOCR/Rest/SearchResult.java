package com.Jessy1237.DrugNameOCR.Rest;

import com.github.cliftonlabs.json_simple.JsonObject;

public class SearchResult
{

    private String ui;
    private String name;
    private String uri;
    private String rootSource;

    public SearchResult( JsonObject jo )
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
}
