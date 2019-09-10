package com.Jessy1237.DrugNameOCR.Handlers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.Jessy1237.DrugNameOCR.Models.Model;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;

public class GoogleOCRHandler extends OCRHandler
{

    String credentialsPath;

    public GoogleOCRHandler( ImageHandler ih, Model m, String credentialsPath )
    {
        super( ih, m );
        this.credentialsPath = credentialsPath;
    }

    @Override
    protected String[] getText( String imgLoc )
    {
        List<AnnotateImageRequest> requests = new ArrayList<>();

        ByteString imgBytes;
        try
        {
            imgBytes = ByteString.readFrom( new FileInputStream( imgLoc ) );

            Image img = Image.newBuilder().setContent( imgBytes ).build();
            Feature feat = Feature.newBuilder().setType( Type.TEXT_DETECTION ).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures( feat ).setImage( img ).build();
            requests.add( request );

            GoogleCredentials credentials = GoogleCredentials.fromStream( new FileInputStream( credentialsPath ) ).createScoped( Lists.newArrayList( "https://www.googleapis.com/auth/cloud-platform" ) );
            try ( ImageAnnotatorClient client = ImageAnnotatorClient.create( ImageAnnotatorSettings.newBuilder().setCredentialsProvider( FixedCredentialsProvider.create( credentials ) ).build() ) )
            {
                BatchAnnotateImagesResponse response = client.batchAnnotateImages( requests );
                List<AnnotateImageResponse> responses = response.getResponsesList();

                for ( AnnotateImageResponse res : responses )
                {
                    if ( res.hasError() )
                    {
                        System.out.printf( "Error: %s\n", res.getError().getMessage() );
                        return new String[] {};
                    }

                    //Now remove blank lines from our output string and then save the lines into an array
                    ArrayList<String> outputList = new ArrayList<String>();
                    for ( String str : res.getFullTextAnnotation().getText().toLowerCase().split( "\n" ) )
                    {
                        if ( !str.trim().isEmpty() )
                        {
                            outputList.add( str );
                        }
                    }

                    String[] outputArray = new String[outputList.size()];
                    outputArray = outputList.toArray( outputArray );

                    return outputArray;
                }
            }
        }
        catch ( IOException e )
        {

        }

        //Return an empty array if we encounter an error
        return new String[] {};
    }
}
