package com.Jessy1237.DrugNameOCR.Models;

import java.util.StringTokenizer;

import com.Jessy1237.DrugNameOCR.Rest.UMLSManager.RestSearchType;

public class RegionOfInterest
{

    private BoundingBox main;
    private BoundingBox pair;
    private RestSearchType rst;
    private String id;

    public RegionOfInterest()
    {
        main = new BoundingBox();
        pair = null;
        rst = RestSearchType.WORD;
        id = " ";
    }

    public RegionOfInterest( String id, BoundingBox main, BoundingBox pair, RestSearchType rst )
    {
        this.main = main;
        this.pair = pair;
        this.rst = rst;
        this.id = id;
    }

    public RegionOfInterest( String roi )
    {
        StringTokenizer st = new StringTokenizer( roi, "`" );
        while ( st.hasMoreTokens() )
        {
            String token = st.nextToken();
            token = token.replace( "RegionOfInterest:", "" );
            token = token.trim();

            if ( token.equalsIgnoreCase( "id=" ) )
            {
                id = st.nextToken();
            }

            if ( token.equalsIgnoreCase( "main=" ) )
            {
                main = new BoundingBox( st.nextToken() );
            }

            if ( token.equalsIgnoreCase( "rst=" ) )
            {
                rst = RestSearchType.valueOf( st.nextToken() );
            }

            if ( token.equalsIgnoreCase( "pair=" ) )
            {
                String temp = st.nextToken();
                if ( temp.equalsIgnoreCase( "null" ) )
                {
                    pair = null;
                }
                else
                {
                    pair = new BoundingBox( temp );
                }
            }
        }
    }

    public String getId()
    {
        return id;
    }

    public BoundingBox getMainBoundingBox()
    {
        return main;
    }

    public BoundingBox getPairBoundingBox()
    {
        return pair;
    }

    public RestSearchType getRestSearchType()
    {
        return rst;
    }

    public boolean isValid()
    {
        boolean valid = main.isValid();

        if ( pair != null )
        {
            valid = valid && pair.isValid();
        }

        return valid;
    }

    @Override
    public String toString()
    {
        String pairStr = ( pair == null ? "null" : pair.toString() );
        return "RegionOfInterest: id=`" + id + "` main=`" + main.toString() + "` rst=`" + rst.name() + "` pair=`" + pairStr + "`";
    }
}
