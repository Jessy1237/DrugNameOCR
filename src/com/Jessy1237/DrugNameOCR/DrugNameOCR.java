package com.Jessy1237.DrugNameOCR;

import java.io.File;
import java.io.FileNotFoundException;
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
import com.Jessy1237.DrugNameOCR.Rest.UMLSManager.RestSearchType;
import com.Jessy1237.DrugNameOCR.SpellCorrection.HMM;
import com.Jessy1237.DrugNameOCR.SpellCorrection.SpellCorrectionMap;
import com.Jessy1237.DrugNameOCR.SpellCorrection.StateWeightedLevenshtein;

public class DrugNameOCR
{

    private final static int MIN_NUM_ARGS = 7;
    private final static String SPELLING_ADDITION = "SA";
    private final static String CANDIDATE_CHECK = "CC";
    private final static String OCR = "OCR";

    public static void main( String[] args )
    {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

        if ( args.length == 0 )
        {
            System.out.println( "Not enough arguments. Need at least an execution type argument. i.e. SA, CC or OCR" );
        }
        else
        {
            if ( args[0].equalsIgnoreCase( SPELLING_ADDITION ) )
            {
                executeSpellingAddition( Arrays.copyOfRange( args, 1, args.length ) );
            }
            else if ( args[0].equalsIgnoreCase( CANDIDATE_CHECK ) )
            {
                executeCandidateCheck( Arrays.copyOfRange( args, 1, args.length ) );
            }
            else if ( args[0].equalsIgnoreCase( OCR ) )
            {
                executeOCR( Arrays.copyOfRange( args, 1, args.length ) );
            }
        }
    }

    public static void executeSpellingAddition( String[] args )
    {
        if ( args.length != 3 )
        {
            System.out.println( "Min Arguments: <exec type> \"<SpellCorrectionMap path>\" <ocr result> <correct spelling>" );
        }
        else
        {
            System.out.println( "Loading the spell correction map....." );
            SpellCorrectionMap map = new SpellCorrectionMap( args[0] );
            System.out.println( "Adding '" + args[2] + "' as correct spelling for '" + args[1] + "' to the map" );
            map.addNewSpellCorrection( args[1], args[2] );
            System.out.println( "Saving the spell correction map....." );
            map.save();
        }
    }

    public static void executeCandidateCheck( String[] args )
    {
        if ( args.length < 2 )
        {
            System.out.println( "Arguments: <exec type> <ocr result> <candidate 1> <candidate 2>....." );
        }
        else
        {
            Util util = new Util();
            double[] sims = new double[args.length - 1];
            StateWeightedLevenshtein swl = new StateWeightedLevenshtein( util );
            for ( int i = 1; i < args.length; i++ )
            {
                sims[i - 1] = swl.similarityPercentage( args[0], args[i] );
            }

            try
            {
                util.writeCandidateCheckToFile( args, sims );
            }
            catch ( FileNotFoundException e )
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method executes the OCR path of the program
     * 
     * @param args The arguments required for this execution path
     */
    public static void executeOCR( String[] args )
    {
        if ( args.length < MIN_NUM_ARGS )
        {
            System.out
                    .println( "Need at least 6 arguments to run. You can have multiple of the two minimum arguments to allow for multiple images to be processed in one execution.\nMinimum Arguments: <exec type> \"<models directory>\" \"<HMM path>\" \"<SpellCorrectionsMap path\" \"<UMLS API key>\" \"<path to google credentials json>\" <handler specifier> -I=\"<image path>\"\nFor Example:OCR \"models/\" \"DrugName.hmm\" \"key\" -AG -I=\"C:\\img.jpg\"" );
        }
        else
        {
            Util util = new Util();
            HashMap<String, List<String>> ocrRequests = util.parseArgumentsToStrings( Arrays.copyOfRange( args, 5, args.length ) );

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

                    System.out.println( "Loading Spell Corrections map....." );
                    SpellCorrectionMap map = new SpellCorrectionMap( args[2] );

                    System.out.println( "Loading UMLS Rest API....." );
                    UMLSManager um = new UMLSManager( args[3], util );

                    for ( String specifier : ocrRequests.keySet() )
                    {
                        for ( String path : ocrRequests.get( specifier ) )
                        {
                            ImageHandler ih = new ImageHandler( path, true );
                            System.out.println( "Readying img '" + path + "'......" );
                            ih.run();

                            if ( specifier.startsWith( "-A" ) ) //Automic Cropping
                            {
                                System.out.println( "Finding bounding boxes....." );
                                List<BoundingBox> combined = util.combineOverlapBB( ih.findBindingBoxes( ih.getCurrentImage() ), ( int ) ( ih.getCurrentImage().width() * 0.015 ), ( int ) ( ih.getCurrentImage().height() * 0.01 ) ); //have the combination tolerance as 1.5% of the image width and 1.0% for the image height

                                System.out.println( "Finding the best model....." );
                                Model m = mm.findBestModel( combined, ih.getCurrentImage().width(), ih.getCurrentImage().height() );

                                if ( m == null )
                                {
                                    System.out.println( "No viable model was found....." );
                                    util.writeNoModelFound( ih.getImageName() );
                                }
                                else
                                {
                                    System.out.println( "Found best model as '" + m.getId() + "'" );
                                    processOCRText( util, mm, hmm, map, um, specifier, ih, m, args[4] );
                                }
                            }
                            else if ( specifier.startsWith( "-M" ) ) //Manual Cropping
                            {
                                RegionOfInterest roi = new RegionOfInterest( "Whole Image", new BoundingBox( 0, 0, ih.getCurrentImage().width(), ih.getCurrentImage().height(), "Whole Image" ), null, RestSearchType.WORDS );
                                Model m = new Model( "NO MODEL", roi, ih.getCurrentImage().width(), ih.getCurrentImage().height() );
                                processOCRText( util, mm, hmm, map, um, specifier, ih, m, args[4] );
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

    public static void processOCRText( Util util, ModelManager mm, HMM hmm, SpellCorrectionMap map, UMLSManager um, String specifier, ImageHandler ih, Model m, String googleCredentialsPath ) throws FileNotFoundException
    {

        OCRHandler ocrh = OCRHandlerFactory.createOCRHandler( specifier, ih, m, googleCredentialsPath );
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
            sims[i][0] = util.spellCorrectOCRLines( hmm, text[i][0], map );

            if ( text[i].length == 2 )
            {
                sims[i][1] = util.spellCorrectOCRLines( hmm, text[i][1], map );
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
    }

}
