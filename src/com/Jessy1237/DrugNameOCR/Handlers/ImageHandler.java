package com.Jessy1237.DrugNameOCR.Handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.Jessy1237.DrugNameOCR.Models.BoundingBox;

public class ImageHandler implements Runnable
{

    private final int BILATERAL_FILTER_KERNEL_LENGTH = 40;

    private Mat original;
    private Mat current = null;
    private boolean createImages;
    private String currentSuffix;
    private String extension;
    private String dir;
    private String imgName;

    public ImageHandler( String dir, String fileName, boolean createImages ) throws IllegalArgumentException
    {
        this.original = Imgcodecs.imread( dir + fileName, Imgcodecs.IMREAD_COLOR );

        if ( original.empty() )
        {
            throw new IllegalArgumentException( "The following image was unable to be loaded: " + dir + fileName );
        }

        this.createImages = createImages;
        this.currentSuffix = "";
        this.dir = dir;

        int periodIndex = fileName.lastIndexOf( '.' );
        this.imgName = fileName.substring( 0, periodIndex );
        this.extension = fileName.substring( periodIndex, fileName.length() );
    }

    public ImageHandler( String fileName, boolean createImages ) throws IllegalArgumentException
    {
        this.original = Imgcodecs.imread( fileName, Imgcodecs.IMREAD_COLOR );

        if ( original.empty() )
        {
            throw new IllegalArgumentException( "The following image was unable to be loaded: " + fileName );
        }

        this.createImages = createImages;
        this.currentSuffix = "";
        this.dir = "";

        int periodIndex = fileName.lastIndexOf( '.' );
        this.imgName = fileName.substring( 0, periodIndex );
        this.extension = fileName.substring( periodIndex, fileName.length() );
    }

    /**
     * Runs the pre-processing on the supplied image file so that the image is ready for OCR. If the createImages boolean of the image handler is true then each effect applied to the image will be
     * written to file
     * 
     * @return The pre-processed image.
     */
    public void run()
    {

        currentSuffix = "";

        //Make a copy of our original image so we can preprocess it
        Mat temp = new Mat();

        temp = prepareImage( original );

        temp = deskew( temp );

        temp = morphTransform( temp );

        temp = cropImage( temp );

        writeImage( temp, "C" );

        current = temp;

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
     * Gets the current path to the current image after applying all effects etc.
     * 
     * @return The path to the current image
     */
    public String getCurrentImagePath()
    {
        return dir + imgName + currentSuffix + extension;
    }

    public String getImageName()
    {
        return imgName;
    }

    /**
     * Finds regions of text in an image, i.e. generally paragraphs/sections. It does this by dilating the text and finding the minimum bounding box to surround a contour.
     * 
     * @param img The image to find the text regions in
     * @return a list of bounding boxes for each contour found in the image
     */
    public List<BoundingBox> findBindingBoxes( Mat img )
    {
        ArrayList<BoundingBox> boxes = new ArrayList<BoundingBox>();
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Mat temp = new Mat();
        Mat hierarchy = new Mat();

        Core.bitwise_not( img, temp ); //Img needs to have text as white to find the contours.

        int iterations = 10 * ( int ) Math.sqrt( ( ( double ) temp.width() * ( double ) temp.height() / 1000000f ) ); //Model images were approx 2000000 pixels^2 and we wanted roughly 15 iterations and test images were 8000000 and we wanted roughly 20

        //Now we limit the iterations to be between 10 and 20
        if ( iterations < 10 )
        {
            iterations = 10;
        }
        else if ( iterations > 20 )
        {
            iterations = 20;
        }

        Imgproc.dilate( temp, temp, Imgproc.getStructuringElement( Imgproc.MORPH_CROSS, new Size( 3, 3 ) ), new Point( 1, 1 ), iterations );

        writeImage( temp, "D" );

        Imgproc.findContours( temp, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE );

        int i = 0;
        for ( Mat contour : contours ) //convert the contours in surrounding bounding boxes
        {
            Rect rect = Imgproc.boundingRect( contour );

            //Dont include boxes that are too small or too large. less than 10% of the image or greater than 80% as these are most likely noise in the image. Also saves computation time.
            if ( ( rect.width < img.width() * 0.1 && rect.height < img.height() * 0.1 ) || ( rect.width > img.width() * 0.8 && rect.height > img.height() * 0.8 ) )
            {
                continue;
            }

            boxes.add( new BoundingBox( rect.x, rect.y, rect.x + rect.width, rect.y + rect.height, "" + i ) );
            i++;
        }

        //boxes.sort( new BoundingBox() );

        return boxes;
    }

    /**
     * Creates an image with overlaying bounding boxes as defined in the list. The created image is written to the image directory but is not set to the current image of the handler. The colour of the
     * bounding boxes is pseudo random using the java random class, however the seed is set to the current image path, so if the method is used again on the same image handler the same coloured boxes
     * are generated. <b>NOTE:</b> This method does nothing if the image handler has not previously run before.
     * 
     * @param boxes The list of Bounding Boxes to draw on the image
     */
    public void drawBoundingBoxes( Collection<BoundingBox> boxes, int thickness )
    {
        if ( hasRun() )
        {
            Mat temp = new Mat();
            Imgproc.cvtColor( current, temp, Imgproc.COLOR_GRAY2RGB );
            Random rand = new Random();
            rand.setSeed( getCurrentImagePath().hashCode() );

            for ( BoundingBox bb : boxes )
            {
                //Get the random colour values for each channel
                int R = rand.nextInt( 256 );
                int G = rand.nextInt( 256 );
                int B = rand.nextInt( 256 );

                int minX = bb.getMinX() - thickness;
                int maxX = bb.getMaxX() + thickness;
                int minY = bb.getMinY() - thickness;
                int maxY = bb.getMaxY() + thickness;

                //make sure our values are within the image dimensions
                if ( minX < 0 )
                    minX = 0;

                if ( maxX > temp.width() )
                    maxX = temp.width();

                if ( minY < 0 )
                    minY = 0;

                if ( maxY > temp.height() )
                    maxY = temp.height();

                for ( int x = minX; x <= maxX; x++ )
                {

                    if ( x <= bb.getMinX() || x >= bb.getMaxX() ) //The left or right line of the BB
                    {
                        for ( int y = minY; y <= maxY; y++ )
                        {
                            temp.put( y, x, new double[] { B, G, R } );
                        }
                    }
                    else
                    {
                        for ( int dy = 0; dy <= thickness; dy++ ) //The top and bottom line of the BB
                        {
                            int y = bb.getMinY() - dy;

                            if ( y < 0 )
                                y = 0;

                            temp.put( y, x, new double[] { B, G, R } );

                            y = bb.getMaxY() + dy;

                            if ( y > temp.height() )
                                y = temp.height();

                            temp.put( y, x, new double[] { B, G, R } );
                        }
                    }
                }
            }

            writeImage( temp, "BB" );
        }
    }

    /**
     * Saves a cropped version of the current image to file based off of the supplied bounding box
     * 
     * @param bb The bounding box of the section of the image that you want to save to file
     * @param suffix The suffix to be hyphenated to the end of the image name
     * @return The path to the saved image file
     */
    public String saveCroppedImage( BoundingBox bb, String suffix )
    {
        Mat img = current.submat( new Rect( bb.getMinX(), bb.getMinY(), bb.getMaxX() - bb.getMinX(), bb.getMaxY() - bb.getMinY() ) );
        String path = dir + imgName + "-" + suffix + extension;
        Imgcodecs.imwrite( path, img );
        return path;
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

        if ( isGrayscaleImage( img ) ) //If the image is grayscale then we don't need to apply all the filtering and contrast improvement methods as these hinder grayscale images.
        {
            Imgproc.cvtColor( img, temp, Imgproc.COLOR_RGB2GRAY ); //We do however have to change the type of image to grayscale as we read it in as a colour image so it has 3 channels but we need it in 2 channels
        }
        else
        {
            //Apply bilateral Filtering
            Imgproc.bilateralFilter( img, temp, BILATERAL_FILTER_KERNEL_LENGTH, BILATERAL_FILTER_KERNEL_LENGTH * 2, BILATERAL_FILTER_KERNEL_LENGTH / 2 );

            writeImage( temp, "BF" );

            //Convert the image to greyscale
            Imgproc.cvtColor( temp, temp, Imgproc.COLOR_RGB2GRAY );

            writeImage( temp, "GS" );

            MatOfDouble mean = new MatOfDouble();
            MatOfDouble stddev = new MatOfDouble();

            Core.meanStdDev( temp, mean, stddev ); //gets the parameters of the greyscaled image, essentially the mean and std of the histogram as we only have one channel now.

            //System.out.println( "stddev: " + stddev.toList().get( 0 ) );

            //TODO: 27 seems to be a good value for now but future work is to investigate this value further. Max value is 255 so we're checking that the histogram is quite spread hence there is good contrasting so Hist. Eq. is not required.
            if ( stddev.toList().get( 0 ) < 27 )
            {
                //Correct the contrast with histogram equalisation
                Imgproc.equalizeHist( temp, temp );

                writeImage( temp, "HE" );

            }
        }

        //Apply adaptive thresholding to get the binarised image
        //Adaptive mean seems to give the better result when compared to gaussian which has some missing text.
        //also lowering the C value to 15 seemed to fix the issue of the finger shadow thresholding out the nearing text as it lowered the value that is minused from the mean.
        Imgproc.adaptiveThreshold( temp, temp, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 15 );

        writeImage( temp, "B" );

        return temp;
    }

    /**
     * Calculates the skew of the supplied binary inverse image.
     * 
     * @param inverse The image to calculate the angle of skew from.
     * @return The angle at which the image is skewed.
     */
    private double calcSkew( Mat inverse )
    {
        Mat whiteLoc = Mat.zeros( inverse.size(), inverse.type() );
        Core.findNonZero( inverse, whiteLoc );

        //Create an empty Mat and pass it to the function
        MatOfPoint matOfPoint = new MatOfPoint( whiteLoc );

        //Translate MatOfPoint to MatOfPoint2f in order to use at a next step
        MatOfPoint2f mat2f = new MatOfPoint2f();
        matOfPoint.convertTo( mat2f, CvType.CV_32FC2 );

        //Get rotated rect of white pixels
        RotatedRect rotatedRect = Imgproc.minAreaRect( mat2f );

        Point[] vertices = new Point[4];
        rotatedRect.points( vertices );

        if ( rotatedRect.size.width > rotatedRect.size.height && inverse.height() > inverse.width() )
        {
            rotatedRect.angle += 90.f;
        }

        return rotatedRect.angle;
    }

    private Mat cropImage( Mat img )
    {
        Mat inverse = new Mat();
        Core.bitwise_not( img, inverse );

        Mat whiteLoc = Mat.zeros( inverse.size(), inverse.type() );
        Core.findNonZero( inverse, whiteLoc );

        //Create an empty Mat and pass it to the function
        MatOfPoint matOfPoint = new MatOfPoint( whiteLoc );

        //Translate MatOfPoint to MatOfPoint2f in order to use at a next step
        MatOfPoint2f mat2f = new MatOfPoint2f();
        matOfPoint.convertTo( mat2f, CvType.CV_32FC2 );

        //Get rotated rect of white pixels
        RotatedRect rect = Imgproc.minAreaRect( mat2f );

        return img.submat( rect.boundingRect() );
    }

    /**
     * Deskews the given image by the given skew angle. It will also write the deskewed image to file if the createImages boolean of the image handler is true
     * 
     * @param src The image to deskew
     * @param angle The angle to deskew by
     * @return Returns the deskewed image
     */
    private Mat deskew( Mat img )
    {
        Point center = new Point( img.width() / 2, img.height() / 2 );

        Mat inverse = new Mat();

        Core.bitwise_not( img, inverse );

        writeImage( inverse, "I" );

        Mat rotImage = Imgproc.getRotationMatrix2D( center, calcSkew( inverse ), 1.0 );
        //1.0 means 100 % scale
        Size size = new Size( img.width(), img.height() );
        Imgproc.warpAffine( inverse, inverse, rotImage, size, Imgproc.INTER_LINEAR ); //Allows for the image to be rotated so that it fits the original image size
        Core.bitwise_not( inverse, img );

        writeImage( img, "DS" );

        return img;
    }

    /**
     * Applies the required morphological transformations to the image. It will also write the transformed image to file if the createImages boolean of the image handler is true
     * 
     * @param img The image to apply the transformations to.
     * @return The image after it has been transformed.
     */
    private Mat morphTransform( Mat img )
    {
        Imgproc.dilate( img, img, Imgproc.getStructuringElement( Imgproc.MORPH_RECT, new Size( 2, 2 ) ) );

        writeImage( img, "MTD" );

        Imgproc.erode( img, img, Imgproc.getStructuringElement( Imgproc.MORPH_RECT, new Size( 3, 3 ) ) );

        writeImage( img, "MTE" );

        return img;
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
            Imgcodecs.imwrite( dir + imgName + currentSuffix + extension, img );
        }
    }

    /**
     * Checks if the 3 channel image is grayscale "black and white" by checking the absolute difference between the rgb channels in pairs per pixel. i.e. rg rb and gb. If the float value is close to 0
     * then it is already grayscale if it is greater than 0 it is a colour pic or for example a pic of a document taken with a camera. Essentially this method finds out if the image was from a fax
     * machine or a scanner.
     * 
     * @param img An rgb image to check
     * @return True if the image is grayscale but read in as rgb image
     */
    private boolean isGrayscaleImage( Mat img )
    {
        double diff = 0.0;

        for ( int x = 0; x < img.width(); x++ )
        {
            for ( int y = 0; y < img.height(); y++ )
            {
                double[] bgr = img.get( y, x );
                double rg = Math.abs( bgr[2] - bgr[1] );
                double rb = Math.abs( bgr[2] - bgr[0] );
                double gb = Math.abs( bgr[1] - bgr[0] );
                diff += rg + rb + gb;
            }
        }

        //System.out.println( "Greyscale: " + diff / ( img.height() * img.width() ) );

        return ( diff / ( img.height() * img.width() ) < 0.00001 );
    }
}
