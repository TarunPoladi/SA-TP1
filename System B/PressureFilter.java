/**
 * File: TemperatureFilter.java
 * 
 * Project: Assignment 1
 * 
 * Description:
 * This class creates a pressure filter. The pressure filter filters values that are not in the
 * specified range
 * 
 * Parameters: 		None
 * 
 * Internal Methods:
 * transformTemperature(long measure)
 */

import java.util.ArrayList;

public class PressureFilter extends FilterFramework {
    /**
     * CONSTRUCTOR
     * @param input: Number of input ports
     * @param output: Number of output ports
     */
    PressureFilter(int input, int output) {
        super(input, output);
    }

    public void run() {

        int MeasurementLength = 8;        // This is the length of all measurements (including time) in bytes
        int IdLength = 4;                // This is the length of IDs in the byte stream

        byte databyte = 0;                // This is the data byte read from the stream
        int bytesread = 0;                // This is the number of bytes read from the stream

        long measurement;                // This is the word used to store all measurements - conversions are illustrated.
        int id;                            // This is the measurement id
        int i;                            // This is a loop counter

        int frameId[] = {0, 1, 2, 3, 4};           // This is where the id's are stored
        long frame[] = new long[5];           // This is where the frame is stored

        ArrayList<long[]> frames = new ArrayList<>(); // This is where the list of frames are stored
        boolean wildpoint = false;              // Tells if a frame has a wildpoint
        long lastValidPoint = 0;                // Keeps track of the last valid point
        boolean hasFirstPoint = false;          // Tells if it already read a valid point

        /**
         *	First we announce to the world that we are alive...
         */

        System.out.println(this.getName() + "::Pressure Reading ");

        while (true) {
            try {

                /**
                 * We know that the first data coming to this filter is going to be an ID and
                 * that it is IdLength long. So we first decommutate the ID bytes.
                 */

                id = 0;

                for (i = 0; i < IdLength; i++) {
                    databyte = ReadFilterInputPort(0);    // This is where we read the byte from the stream...

                    id = id | (databyte & 0xFF);        // We append the byte on to ID...

                    if (i != IdLength - 1)                // If this is not the last byte, then slide the
                    {                                    // previously appended byte to the left by one byte
                        id = id << 8;                    // to make room for the next byte we append to the ID

                    } // if

                    bytesread++;                        // Increment the byte count

                } // for

                /**
                 * Here we read measurements. All measurement data is read as a stream of bytes
                 * and stored as a long value. This permits us to do bitwise manipulation that
                 * is neccesary to convert the byte stream into data words. Note that bitwise
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
                    measurement = measurement | (databyte & 0xFF);    // We append the byte on to measurement...

                    if (i != MeasurementLength - 1)                    // If this is not the last byte, then slide the
                    {                                                // previously appended byte to the left by one byte
                        measurement = measurement << 8;                // to make room for the next byte we append to the
                        // measurement
                    } // if

                    bytesread++;                                    // Increment the byte count

                }// for

                /**
                 * Store the frame's data.
                 */
                if (id == 0) {
                    frame[0] = measurement;
                }

                if (id == 1) {
                    frame[1] = measurement;
                }

                if (id == 2) {
                    frame[2] = measurement;
                }
                if (id == 3) {
                    frame[3] = measurement;
                    double pressure = Double.longBitsToDouble(measurement);
                    // Checks if its a wildpoint
                    wildpoint = pressure < 50 || pressure > 80;

                }
                if (id == 4) {
                    frame[4] = measurement;
                    /**
                     * If its a wildpoint, add to it the list and send it to the SinkFilterWild
                     */
                    if (wildpoint) {
                        sendToSinkWild(frame[0], frame[3]);
                        frames.add(frame.clone());

                    }
                    /**
                     * Not a wildpoint
                     */
                    else {
                        // Check if there is any frame on the list
                        if (!frames.isEmpty()) {
                            /**
                             * if this is the first valid point substitute the pressure on the frame's list
                             * with the current value.
                             */
                            if (!hasFirstPoint) {
                                for (long[] frame1 : frames) {
                                    sendToNormalSink(frame1, frameId, frame[3], true);
                                }
                            }
                            /**
                             * If this is not the first valid point, interpolate the value with the current
                             * pressure value and the last valid value.
                             */
                            else {
                                double last = Double.longBitsToDouble(lastValidPoint);
                                double current = Double.longBitsToDouble(frame[3]);
                                double value = (last + current) / 2;

                                for (long[] frame1 : frames) {
                                    sendToNormalSink(frame1, frameId, Double.doubleToLongBits(value), true);
                                }
                            }
                            //clear the list
                            frames.clear();
                        }
                        // Send current value
                        sendToNormalSink(frame, frameId, frame[3], false);
                        lastValidPoint = frame[3];
                        hasFirstPoint = true;
                    } // else

                } // if

            } // try

            /**
             *	The EndOfStreamException below is thrown when you reach end of the input
             *	stream (duh). At this point, the filter ports are closed and a message is
             *	written letting the user know what is going on.
             */
            catch (EndOfStreamException e) {

                System.out.println(this.getName() + "::Pressure finished reading input; bytes read: " + bytesread);
                break;

            } // catch

        } // while

        /**
         * If after receiving all frames, there is still wildpoints to be dealt with, substitute
         * them with the last valid point.
         */
        if (!frames.isEmpty()) {
            for (long[] frame1 : frames) {
                sendToNormalSink(frame1, frameId, lastValidPoint, true);
            }
        }

        ClosePorts();
        System.out.println(this.getName()+"::Pressure finished sending;");
    } // run

    /**
     * METHOD:: sendToNormalSink
     * Purpose: Send frames to the SinkFilter.
     *
     * @param frame: frame data
     * @param frameId: id values
     * @param pressure: value for the pressure
     * @param wild: indicates if this is a wildpoint or not
     */
    void sendToNormalSink(long frame[], int frameId[], long pressure, boolean wild) {
        int IdLength = 4;
        int MeasurementLength = 8;
        byte databyte = 0;

        for (int i = 0; i < 5; i++) {
            // Send id
            for (int n = IdLength - 1; n >= 0; n--) {
                databyte = (byte) (frameId[i] >> 8 * n);
                WriteFilterOutputPort(databyte, 0);

            }
            // Send measure
            for (int n = MeasurementLength - 1; n >= 0; n--) {
                if (i != 3)
                    databyte = (byte) (frame[i] >> 8 * n);
                else
                    databyte = (byte) (pressure >> 8 * n);

                WriteFilterOutputPort(databyte, 0);
            }
            // Send extra byte that indicates if this is a wildpoint
            if (i == 3) {
                databyte = (byte) (wild ? 1 : 0);
                WriteFilterOutputPort(databyte, 0);
            }
        }
    }

    /**
     * METHOD:: sendToSinkWild
     * Purpose: Send wildpoints to the proper filter
     *
     * @param time: time value
     * @param pressure: wildpoint value
     */
    void sendToSinkWild(long time, long pressure) {
        int IdLength = 4;
        int MeasurementLength = 8;
        byte databyte = 0;

        int id = 0;
        // Send id
        for (int n = IdLength - 1; n >= 0; n--) {
            databyte = (byte) (id >> 8 * n);
            WriteFilterOutputPort(databyte, 1);

        }
        // Send time
        for (int i = MeasurementLength - 1; i >= 0; i--) {
            databyte = (byte) (time >> 8 * i);
            WriteFilterOutputPort(databyte, 1);
        }

        id = 3;
        // Send id
        for (int n = IdLength - 1; n >= 0; n--) {
            databyte = (byte) (id >> 8 * n);
            WriteFilterOutputPort(databyte, 1);
        }
        // Send wildpoint
        for (int i = MeasurementLength - 1; i >= 0; i--) {
            databyte = (byte) (pressure >> 8 * i);
            WriteFilterOutputPort(databyte, 1);
        }
    }

} // PressureFilter
