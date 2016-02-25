/**
 * File:MergerB.java
 * 
 * Description:
 * This class creates a merger. A merger receives streams from its input ports and merges the data.
 * 
 * Parameters:
 * inputs: Number of input ports
 * 
 * Internal Methods: None
 */

public class MergerB extends FilterFramework {
    private int inputs;

    /**
     * CONSTRUCTOR
     *
     * @param input:  Number of input ports
     * @param output: Number of output ports
     */
    MergerB(int input, int output) {
        super(input, output);
        this.inputs = input;
    }

    public void run() {


        int bytesread = 0;                    // Number of bytes read from the input file.
        int byteswritten = 0;                // Number of bytes written to the stream.
        byte databyte;                    // The byte of data read from the file
        int MeasurementLength = 8;          // This is the length of all measurements (including time) in bytes
        int IdLength = 4;                   // This is the length of IDs in the byte stream
        long measurement[] = new long[inputs];                   // This is the word used to store all measurements from all inputs

        int id[] = new int[inputs];                             // This is the measurement id for the inputs

        int frameId[] = new int[5];           // This is where the id's are stored
        long frame[] = new long[5];           // This is where the frame is stored

        // Next we write a message to the terminal to let the world know we are alive...

        System.out.println(this.getName() + "::Merger Reading ");

        while (true) {

            try {
                for (int i = 0; i < inputs; i++) {
                    id[i] = 0;
                    for (int j = 0; j < IdLength; j++) {
                        databyte = ReadFilterInputPort(i);      // This is where we read the byte from the stream...
                        id[i] = id[i] | (databyte & 0xFF);            // We append the byte on to ID...

                        if (j != IdLength - 1) {                // If this is not the last byte, then slide the
                            id[i] = id[i] << 8;                       // previously appended byte to the left by one byte
                            // to make room for the next byte we append to the ID
                        } // if

                        bytesread++;                            // Increment the byte count

                    } // for

                    measurement[i] = 0;

                    for (int j = 0; j < MeasurementLength; j++) {
                        databyte = ReadFilterInputPort(i);
                        measurement[i] = measurement[i] | (databyte & 0xFF);      // We append the byte on to measurement...
                        if (j != MeasurementLength - 1) {                   // If this is not the last byte, then slide the
                            measurement[i] = measurement[i] << 8;                 // previously appended byte to the left by one byte
                            // to make room for the next byte we append to the
                            // measurement
                        } // if

                        bytesread++;                                    // Increment the byte count

                    } // for

                }// for

                /**
                 * Store the frame and its id's.
                 * In this case we store the transformed temperature from the input port number 1,
                 * the Altitude from the input port number 0 and the Pressure from the input port
                 * number 2.
                 */
                if (id[0] == 0) {
                    frameId[0] = id[0];
                    frame[0] = measurement[0];

                }
                if (id[0] == 1) {
                    frameId[1] = id[0];
                    frame[1] = measurement[0];

                }
                if (id[0] == 2) {
                    frameId[2] = id[0];
                    frame[2] = measurement[0];

                }
                if (id[2] == 3) {
                    frameId[3] = id[2];
                    frame[3] = measurement[2];

                }
                if (id[1] == 4) {
                    frameId[4] = id[1];
                    frame[4] = measurement[1];

                    //send the merged frame
                    for (int k = 0; k < 5; k++) {
                        for (int n = IdLength - 1; n >= 0; n--) {
                            databyte = (byte) (frameId[k] >> 8 * n);
                            WriteFilterOutputPort(databyte, 0);

                            byteswritten++;
                        }
                        for (int n = MeasurementLength - 1; n >= 0; n--) {
                            databyte = (byte) (frame[k] >> 8 * n);
                            WriteFilterOutputPort(databyte, 0);

                            byteswritten++;
                        }
                    }
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