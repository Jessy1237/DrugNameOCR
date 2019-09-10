package com.Jessy1237.DrugNameOCR;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.opencv.core.Core;

import com.Jessy1237.DrugNameOCR.Handlers.ImageHandler;
import com.Jessy1237.DrugNameOCR.Handlers.OCRHandler;
import com.Jessy1237.DrugNameOCR.Handlers.OCRHandlerFactory;
import com.Jessy1237.DrugNameOCR.Models.BoundingBox;
import com.Jessy1237.DrugNameOCR.Models.Model;
import com.Jessy1237.DrugNameOCR.Models.ModelManager;
import com.Jessy1237.DrugNameOCR.Models.RegionOfInterest;
import com.Jessy1237.DrugNameOCR.Rest.UMLSManager;
import com.Jessy1237.DrugNameOCR.SpellCorrection.HMM;

public class DrugNameOCR
{

    private final static int MIN_NUM_ARGS = 5;

    public static void main( String[] args )
    {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

        Util util = new Util();

        if ( args.length < MIN_NUM_ARGS )
        {
            System.out
                    .println( "Need at least five arguments to run. You can have multiple of the two minimum arguments to allow for multiple images to be processed in one execution.\nMinimum Arguments: \"<models directory>\" \"<HMM path>\" \"<UMLS API key>\" <handler specifier> -I=\"<image path>\"\nFor Example: \"models/\" -AG -I=\"C:\\img.jpg\"" );
        }
        else
        {
            HashMap<String, List<String>> ocrRequests = util.parseArgumentsToStrings( Arrays.copyOfRange( args, MIN_NUM_ARGS - 2, args.length ) );

            if ( ocrRequests.isEmpty() )
            {
                System.out.println( "No valid OCR Requests were found" );
            }
            else
            {
                try
                {
                    ModelManager mm = new ModelManager( args[0] );
                    System.out.println( "Reading models....." );
                    mm.readModels();

                    System.out.println( "Loading HMM....." );
                    HMM hmm = new HMM( new File( args[1] ) );

                    System.out.println( "Loading UMLS Rest API....." );
                    UMLSManager um = new UMLSManager( args[2], util );

                    for ( String specifier : ocrRequests.keySet() )
                    {
                        for ( String path : ocrRequests.get( specifier ) )
                        {
                            ImageHandler ih = new ImageHandler( path, true );
                            System.out.println( "Readying img '" + path + "'......" );
                            ih.run();

                            System.out.println( "Finding bounding boxes....." );
                            List<BoundingBox> combined = util.combineOverlapBB( ih.findBindingBoxes( ih.getCurrentImage() ), ( int ) ( ih.getCurrentImage().width() * 0.015 ), ( int ) ( ih.getCurrentImage().height() * 0.01 ) ); //have the combination tolerance as 1.5% of the image width and 1.0% for the image height

                            System.out.println( "Finding the best model....." );
                            Model m = mm.findBestModel( combined, ih.getCurrentImage().width(), ih.getCurrentImage().height() );

                            if ( m == null )
                            {
                                System.out.println( "No viable model was found....." );
                            }
                            else
                            {
                                System.out.println( "Found best model as '" + m.getId() + "'" );

                                OCRHandler ocrh = OCRHandlerFactory.createOCRHandler( specifier, ih, m );
                                System.out.println( "Finding ocr text and spell correcting....." );
                                ocrh.run();

                                //roi X bb X lines
                                String[][][] text = new String[m.getRegionOfInterests().size()][][];
                                //roi x bb x lines x word
                                double[][][][] sims = new double[m.getRegionOfInterests().size()][][][];

                                for ( int i = 0; i < m.getRegionOfInterests().size(); i++ )
                                {
                                    RegionOfInterest roi = m.getRegionOfInterests().get( i );

                                    text[i] = ocrh.getTextFromROIs().get( roi.getId() );
                                    sims[i] = new double[text[i].length][][];
                                    sims[i][0] = util.spellCorrectOCRLines( hmm, text[i][0] );

                                    if ( text[i].length == 2 )
                                    {
                                        sims[i][1] = util.spellCorrectOCRLines( hmm, text[i][1] );
                                    }
                                }

                                System.out.println( "Locating drug names....." );
                                //roi x lines x wordInLine x NameType
                                String[][][][] drugNames = new String[m.getRegionOfInterests().size()][][][];
                                for ( int i = 0; i < m.getRegionOfInterests().size(); i++ )
                                {
                                    RegionOfInterest roi = m.getRegionOfInterests().get( i );
                                    System.out.println( "Finding Drug Names in roi '" + roi.getId() + "'" );
                                    List<String[]>[] tempDrugNames = util.findAllDrugNames( um, text[i][0], roi.getRestSearchType() );

                                    drugNames[i] = new String[text[i][0].length][][];
                                    for ( int j = 0; j < text[i][0].length; j++ )
                                    {
                                        drugNames[i][j] = new String[tempDrugNames[j].size()][];
                                        for ( int k = 0; k < tempDrugNames[j].size(); k++ )
                                        {
                                            drugNames[i][j][k] = tempDrugNames[j].get( k );
                                        }
                                    }
                                }

                                System.out.println( "Writing OCR Results to file....." );
                                util.writeResultsToFile( m, text, sims, drugNames, ih.getImageName() );

                                System.out.println( "----------------------------------------OCR TEXT----------------------------------------" );
                                for ( int i = 0; i < m.getRegionOfInterests().size(); i++ )
                                {
                                    for ( int j = 0; j < text[i][0].length; j++ )//Assuming paired box has same number of lines as the main box
                                    {
                                        System.out.println( "line " + j + " main text bb: " + text[i][0][j] );
                                        System.out.print( "Found the following drug names '<text name>':'<umls name>': " );

                                        for ( int k = 0; k < drugNames[i][j].length; k++ )
                                        {
                                            System.out.println( "'" + drugNames[i][j][k][0] + "':'" + drugNames[i][j][k][1] + "' " );
                                        }
                                        System.out.print( "\n" );

                                        if ( text.length == 2 )
                                        {
                                            System.out.println( "line " + j + " pair text bb: " + text[i][1][j] );
                                        }
                                    }
                                }

                            }
                        }
                    }

                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
