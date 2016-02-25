/**
 * File:MergerA.java
 * 
 * Description:
 * This class creates a merger. A merger receives streams from its input ports and merges the data.
 * 
 * Parameters:
 * inputs: Number of input ports
 * 
 * Internal Methods: None
 */

public class MergerC extends FilterFramework {
    private int inputs;

    /**
     * CONSTRUCTOR
     *
     * @param input:  Number of input ports
     * @param output: Number of output ports
     */
    MergerC(int input, int output) {
        super(input, output);
        this.inputs = input;
    }

    public void run() {


        int bytesread = 0;                    // Number of bytes read from the input file.
        int byteswritten = 0;                // Number of bytes written to the stream.
        byte databyte;                    // The byte of data read from the file
        int MeasurementLength = 8;          // This is the length of all measurements (including time) in bytes
        int IdLength = 4;                   // This is the length of IDs in the byte stream

        // Next we write a message to the terminal to let the world know we are alive...

        System.out.println(this.getName() + "::Merger Reading ");

        while (true) {

            try {
                /**
                 * Merge one frame at a time for every input
                 */
                for (int i = 0; i < inputs; i++) {
                    for (int k = 0; k < 5; k++) {
                        for (int j = 0; j < IdLength; j++) {
                            databyte = ReadFilterInputPort(i);      // This is where we read the byte from the stream...
                            WriteFilterOutputPort(databyte, 0);

                            bytesread++;                            // Increment the byte count
                            byteswritten++;

                        } // for

                        for (int j = 0; j < MeasurementLength; j++) {
                            databyte = ReadFilterInputPort(i);

                            bytesread++;                                    // Increment the byte count
                            WriteFilterOutputPort(databyte, 0);
                            byteswritten++;
                        } // for

                    }// for
                }

            } // try

            catch (EndOfStreamException e) {
                ClosePorts();
                System.out.println(this.getName() + "::Merger Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten);
                break;

            } // catch

        } // while

    } // run

} // Merger