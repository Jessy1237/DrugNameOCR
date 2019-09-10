package com.Jessy1237.DrugNameOCR;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;

import com.Jessy1237.DrugNameOCR.Handlers.ImageHandler;
import com.Jessy1237.DrugNameOCR.Models.BoundingBox;
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

public class GoogleOCRTest
{

    public static void main( String[] args ) throws IOException
    {
        if ( args.length != 1 )
        {
            System.out.println( "ARGS: \"<image path>\" \"<google credentials json path>\"" );
        }
        else
        {

            System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
            ImageHandler ih = new ImageHandler( args[0], true );
            ih.run();

            List<AnnotateImageRequest> requests = new ArrayList<>();

            ByteString imgBytes = ByteString.readFrom( new FileInputStream( ih.saveCroppedImage( new BoundingBox( 0, 0, ih.getCurrentImage().width(), ih.getCurrentImage().height(), " " ), "-main" ) ) );

            Image img = Image.newBuilder().setContent( imgBytes ).build();
            Feature feat = Feature.newBuilder().setType( Type.TEXT_DETECTION ).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures( feat ).setImage( img ).build();
            requests.add( request );

            GoogleCredentials credentials = GoogleCredentials.fromStream( new FileInputStream( args[1] ) ).createScoped( Lists.newArrayList( "https://www.googleapis.com/auth/cloud-platform" ) );
            try ( ImageAnnotatorClient client = ImageAnnotatorClient.create( ImageAnnotatorSettings.newBuilder().setCredentialsProvider( FixedCredentialsProvider.create( credentials ) ).build() ) )
            {
                BatchAnnotateImagesResponse response = client.batchAnnotateImages( requests );
                List<AnnotateImageResponse> responses = response.getResponsesList();

                for ( AnnotateImageResponse res : responses )
                {
                    if ( res.hasError() )
                    {
                        System.out.printf( "Error: %s\n", res.getError().getMessage() );
                        return;
                    }

                    System.out.print( res.getFullTextAnnotation().getText() );
                }
            }
        }
    }

}
