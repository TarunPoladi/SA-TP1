/**
 * File:SinkFilter.java
 * Project: Assignment 1
 * 
 * Description:
 * This class creates a sink filter. The sink filter reads some input from the filter's input port
 * and does the following:
 * 
 * 1) It parses the input stream and "decommutates" the measurement ID
 * 2) It parses the input steam for measurements and "decommutates" measurements, storing the bits in a long word.
 * 
 * Parameters: 	None
 * 
 * Internal Methods: None
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SinkFilterC extends FilterFramework {

    /**
     * CONSTRUCTOR
     * @param input: Number of input ports
     * @param output: Number of output ports
     */
    SinkFilter(int input, int output) {
        super(input, output);
    }

    public void run() {
        /**
         *	TimeStamp is used to compute time using java.util's Calendar class.
         * 	TimeStampFormat is used to format the time value so that it can be easily printed
         *	to the file.
         */

        Calendar TimeStamp = Calendar.getInstance();
        SimpleDateFormat TimeStampFormat = new SimpleDateFormat("hh:mm:ss.SSS");

        int MeasurementLength = 8;          // This is the length of all measurements (including time) in bytes
        int IdLength = 4;                   // This is the length of IDs in the byte stream

        byte databyte = 0;                  // This is the data byte read from the stream
        int bytesread = 0;                  // This is the number of bytes read from the stream

        long measurement;                   // This is the word used to store all measurements
        int id;                             // This is the measurement id
        int i;                              // This is a loop counter


        PrintWriter out = null;             // This is the file printer reference

        /**
         * Initialize the printer to write to the specified file name
         */
        try {
            out = new PrintWriter("OutputA.dat", "UTF-8");

        } catch (IOException e) {
            System.out.println(this.getName() + "::Problem creating output data file::" + e);
        }
        /**
         *	First we announce to the world that we are alive...
         */
        System.out.println(this.getName() + "::Sink Reading ");

        while (true) {
            try {
                /**
                 * We know that the first data coming to this filter is going to be an ID and
                 * that it is IdLength long. So we first decommutate the ID bytes.
                 */
                id = 0;

                for (i = 0; i < IdLength; i++) {
                    databyte = ReadFilterInputPort(0);      // This is where we read the byte from the stream...
                    id = id | (databyte & 0xFF);            // We append the byte on to ID...

                    if (i != IdLength - 1) {                 // If this is not the last byte, then slide the
                        id = id << 8;                       // previously appended byte to the left by one byte
                        // to make room for the next byte we append to the ID
                    } // if

                    bytesread++;                            // Increment the byte count

                } // for

                /**
                 * Here we read measurements. All measurement data is read as a stream of bytes
                 * and stored as a long value. This permits us to do bitwise manipulation that
                 * is necessary to convert the byte stream into data words. Note that bitwise
                 * manipulation is not permitted on any kind of floating point types in Java.
                 * If the id = 0 then this is a time value and is therefore a long value - no
                 * problem. However, if the id is something other than 0, then the bits in the
                 * long value is really of type double and we need to convert the value using
                 * Double.longBitsToDouble(long val) to do the conversion which is illustrated.
                 * below.
                 */

                measurement = 0;

                for (i = 0; i < MeasurementLength; i++) {
                    databyte = ReadFilterInputPort(0);
                    measurement = measurement | (databyte & 0xFF);      // We append the byte on to measurement...
                    if (i != MeasurementLength - 1) {                    // If this is not the last byte, then slide the
                        measurement = measurement << 8;                 // previously appended byte to the left by one byte
                        // to make room for the next byte we append to the
                        // measurement
                    } // if

                    bytesread++;                                    // Increment the byte count

                } // for

                // Catch time
                if (id == 0) {
                    TimeStamp.setTimeInMillis(measurement);
                    out.println(TimeStampFormat.format(TimeStamp.getTime()));
                }

            } // try

            /**
             *	The EndOfStreamException below is thrown when you reach end of the input
             *	stream. At this point, the filter ports are closed and a message is
             *	written letting the user know what is going on.
             */
            catch (EndOfStreamException e) {
                if (out != null) {
                    out.close();
                }
                ClosePorts();
                System.out.println(this.getName() + "::Sink Exiting; bytes read: " + bytesread);

                break;

            } // catch

        } // while

    } // run

} // SinkFilter