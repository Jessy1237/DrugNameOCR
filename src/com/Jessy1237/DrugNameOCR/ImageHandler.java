package com.Jessy1237.DrugNameOCR;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageHandler
{

    private final int BILATERAL_FILTER_KERNEL_LENGTH = 40;

    private Mat original;
    private Mat current = null;
    private boolean createImages;
    private String currentSuffix;
    private String fileName;
    private String extension;

    public ImageHandler( String imgLoc, boolean createImages ) throws IllegalArgumentException
    {
        this.original = Imgcodecs.imread( imgLoc, Imgcodecs.IMREAD_COLOR );

        if ( original.empty() )
        {
            throw new IllegalArgumentException( "The following image was unable to be loaded: " + imgLoc );
        }

        this.createImages = createImages;
        this.currentSuffix = "";

        int periodIndex = imgLoc.lastIndexOf( '.' );
        this.fileName = imgLoc.substring( 0, periodIndex );
        this.extension = imgLoc.substring( periodIndex, imgLoc.length() );
    }

    /**
     * Runs the pre-processing on the supplied image file so that the image is ready for OCR. If the createImages boolean of the image handler is true then each effect applied to the image will be
     * written to file
     * 
     * @return The pre-processed image.
     */
    public Mat run()
    {

        currentSuffix = "";

        //Make a copy of our original image so we can preprocess it
        Mat temp = new Mat();
        temp = prepareImage( original );

        temp = deskew( temp, calcSkew( temp ) );

        current = temp;

        return current;

    }

    public boolean hasRun()
    {
        return current != null;
    }

    /**
     * Gets the current effected image of the image handler
     * 
     * @return The current effected image from the original, will return now if hasRun returns false as no effects have been applied
     */
    public Mat getCurrentImage()
    {
        return current;
    }

    /**
     * Prepares the given image by applying a pre-process for OCR. It will write each effect applied to the image to file if the createImages boolean of the image handler is true.
     * 
     * @param img The image to pre-process
     * @return Returns the prepared image
     */
    private Mat prepareImage( Mat img )
    {

        Mat temp = new Mat();
        //Apply bilateral Filtering
        Imgproc.bilateralFilter( img, temp, BILATERAL_FILTER_KERNEL_LENGTH, BILATERAL_FILTER_KERNEL_LENGTH * 2, BILATERAL_FILTER_KERNEL_LENGTH / 2 );

        writeImage( temp, "BF" );

        //Convert the image to greyscale
        Imgproc.cvtColor( temp, temp, Imgproc.COLOR_RGB2GRAY );

        writeImage( temp, "GS" );

        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();

        Core.meanStdDev( temp, mean, stddev ); //gets the parameters of the greyscaled image, essentially the mean and std of the histogram as we only have one channel now.

        if ( stddev.toList().get( 0 ) < 40 ) //TODO: 40 seems to be a good value for now but future work is to investigate this value further. Max value is 255 so we're checking that the histrogram is quite spread hence there is good contrasting so Hist. Eq. is not required.
        {
            //Correct the contrast with histogram equalisation
            Imgproc.equalizeHist( temp, temp );

            writeImage( temp, "HE" );

        }
        //Apply adaptive thresholding to get the binarised image
        //Adaptive mean seems to give the better result when compared to gaussian which has some missing text.
        //also lowering the C value to 15 seemed to fix the issue of the finger shadow thresholding out the nearing text as it lowered the value that is minused from the mean.
        Imgproc.adaptiveThreshold( temp, temp, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 15 );

        writeImage( temp, "B" );

        return temp;
    }

    /**
     * Calculates the skew of the supplied image
     * 
     * @param skewed The image to calculate the angle of skew from.
     * @return The angle at which the image is skewed.
     */
    private double calcSkew( Mat skewed )
    {
        Mat inverse = new Mat();

        Core.bitwise_not( skewed, inverse );
        Mat element = Imgproc.getStructuringElement( Imgproc.MORPH_RECT, new Size( 3, 3 ) );

        //We can now perform our erosion, we must declare our rectangle-shaped structuring element and call the erode function
        Imgproc.erode( inverse, inverse, element );

        writeImage( inverse, "I" );

        Mat whiteLoc = Mat.zeros( inverse.size(), inverse.type() );
        Core.findNonZero( inverse, whiteLoc );

        //Create an empty Mat and pass it to the function
        MatOfPoint matOfPoint = new MatOfPoint( whiteLoc );

        //Translate MatOfPoint to MatOfPoint2f in order to user at a next step
        MatOfPoint2f mat2f = new MatOfPoint2f();
        matOfPoint.convertTo( mat2f, CvType.CV_32FC2 );

        //Get rotated rect of white pixels
        RotatedRect rotatedRect = Imgproc.minAreaRect( mat2f );

        Point[] vertices = new Point[4];
        rotatedRect.points( vertices );

        if ( rotatedRect.size.width > rotatedRect.size.height )
        {
            rotatedRect.angle += 90.f;
        }

        return rotatedRect.angle;
    }

    /**
     * Deskews the given image by the given skew angle. It will also write the deskewed image to file if the createImages boolean of the image handler is true
     * 
     * @param src The image to deskew
     * @param angle The angle to deskew by
     * @return Returns the deskewed image
     */
    public Mat deskew( Mat src, double angle )
    {
        Point center = new Point( src.width() / 2, src.height() / 2 );
        Mat rotImage = Imgproc.getRotationMatrix2D( center, angle, 1.0 );
        System.out.println( angle );
        //1.0 means 100 % scale
        Size size = new Size( src.width(), src.height() );
        Imgproc.warpAffine( src, src, rotImage, size, Imgproc.INTER_LINEAR ); //Allows for the image to be rotated so that it fits the original image size

        writeImage( src, "DS" );

        return src;
    }

    /**
     * Write the Mat to the current directory of the image handler with the added suffix if the createImages boolean is true, otherwise nothing happens
     * 
     * @param img The image to be written to file
     * @param suffix The suffix to be hyphenated to the end of the current file name of the image handler.
     */
    private void writeImage( Mat img, String suffix )
    {
        if ( createImages )
        {
            currentSuffix += "-" + suffix;
            Imgcodecs.imwrite( fileName + currentSuffix + extension, img );
        }
    }
}
