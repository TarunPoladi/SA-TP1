/**
 * File: Splitter.java
 * Project: Assignment 1
 * 
 * Description:
 * This class creates a splitter. A splitter sends the stream to all its output ports.
 * 
 * Parameters:
 * outputs: Number of output ports.
 * 
 * Internal Methods: None
 */

public class Splitter extends FilterFramework {

    private int outputs;

    /**
     * CONSTRUCTOR
     * @param input: Number of input ports
     * @param output: Number of output ports
     */
    Splitter(int input, int output) {
        super(input, output);
        this.outputs = output;
    }

    public void run() {

        int bytesread = 0;                    // Number of bytes read from the input file.
        int byteswritten = 0;                // Number of bytes written to the stream.
        byte databyte = 0;                    // The byte of data read from the file

        // Next we write a message to the terminal to let the world know we are alive...

        System.out.print("\n" + this.getName() + "::Splitter Reading ");

        while (true) {
            /**
             *	Here we read a byte and write a byte to all output ports
             */

            try {

                databyte = ReadFilterInputPort(0);
                bytesread++;

                // Write data to each output
                for (int i = 0; i < outputs; i++) {
                    WriteFilterOutputPort(databyte, i);
                    byteswritten++;
                }


            } // try

            catch (EndOfStreamException e) {
                ClosePorts();
                System.out.print("\n" + this.getName() + "::Splitter Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten);
                break;

            } // catch

        } // while

    } // run

} // MiddleFilter
