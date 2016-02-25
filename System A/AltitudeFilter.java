/**
 * File: AltitudeFilter.java
 *
 * Project: Assignment 1
 *
 * Description:
 * This class creates a altitude filter. The altitude filter transforms the altitude from
 * Feet to Meters
 *
 * Parameters: 		None
 *
 * Internal Methods:
 * transformAltitude(long measure)
 */

public class AltitudeFilter extends FilterFramework {
    /**
     * CONSTRUCTOR
     * @param input: Number of input ports
     * @param output: Number of output ports
     */
    AltitudeFilter(int input, int output) {
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

        /**
         *	First we announce to the world that we are alive...
         */

        System.out.print("\n" + this.getName() + "::Sink Reading ");

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
                    WriteFilterOutputPort(databyte, 0);
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

                }// if


                if (id == 2) {
                    // Transforms temperature
                    measurement = transformAltitude(measurement);

                    // Writes the result to the stream
                    for (i = MeasurementLength - 1; i >= 0; i--) {
                        databyte = (byte) (measurement >> 8 * i);
                        WriteFilterOutputPort(databyte, 0);
                    }
                }
                // If not temperature, doesn't change the data
                else {
                    for (i = MeasurementLength - 1; i >= 0; i--) {

                        databyte = (byte) (measurement >> 8 * i);
                        WriteFilterOutputPort(databyte, 0);
                    }
                }

            } // try

            /**
             *	The EndOfStreamExeception below is thrown when you reach end of the input
             *	stream (duh). At this point, the filter ports are closed and a message is
             *	written letting the user know what is going on.
             */
            catch (EndOfStreamException e) {
                ClosePorts();
                System.out.println(this.getName() + "::Altitude Exiting; bytes read: " + bytesread);
                break;

            } // catch

        } // while

    } // run

    /**
     *METHOD:: transformAltitude
     * Purpose: Transform altitude from Feet to Meters.
     *
     * @param measurement: Altitude measurement in Feet to be transformed
     * @return Altitude in Meters
     */
    public long transformAltitude(long measurement) {

        double transform = Double.longBitsToDouble(measurement);
        transform = (transform / 3.2808);

        return Double.doubleToLongBits(transform);
    }

} // AltitudeFilter
