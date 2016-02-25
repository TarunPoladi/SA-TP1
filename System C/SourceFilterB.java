/**
 * File: SourceFilter.java
 *
 * Project: Assignment 1
 *
 * Description:
 * This class creates a source filter. The source filter reads some input from the FlightData.dat
 * file and writes the bytes up stream.
 *
 * Parameters: 		None
 *
 * Internal Methods: None
 */

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;

public class SourceFilterB extends FilterFramework {

    SourceFilterB(int input, int output) {
        super(input, output);
    }

    public void run() {

        String fileName = "SubSetB.dat";    // Input data file.
        int bytesread = 0;					// Number of bytes read from the input file.
        int byteswritten = 0;				// Number of bytes written to the stream.
        DataInputStream in = null;            // File stream reference.
        byte databyte = 0;                    // The byte of data read from the file

        try {
            /**
             *	Here we open the file and write a message to the terminal.
             **/

            in = new DataInputStream(new FileInputStream(fileName));
            System.out.println(this.getName() + "::Source reading file...");

            /**
             *	Here we read the data from the file and send it out the filter's output port one
             * 	byte at a time. The loop stops when it encounters an EOFExeception.
             */

            while (true) {
                databyte = in.readByte();
                bytesread++;
                WriteFilterOutputPort(databyte, 0);
                byteswritten++;
            } // while

        } //try

        /**
         *	The following exception is raised when we hit the end of input file. Once we
         * 	reach this point, we close the input file, close the filter ports and exit.
         */
        catch (EOFException eoferr) {
            System.out.println(this.getName() + "::End of file reached...");
            try {
                in.close();
                ClosePorts();
                System.out.println(this.getName() + "::Read file complete, bytes read::" + bytesread + " bytes written: " + byteswritten );
            }
            /**
             *	The following exception is raised should we have a problem closing the file.
             */ catch (Exception closeerr) {
                System.out.println(this.getName() + "::Problem closing input data file::" + closeerr);

            } // catch

        } // catch

        /**
         *	The following exception is raised should we have a problem opening the file.
         */
        catch (IOException iox) {
            System.out.println(this.getName() + "::Problem reading input data file::" + iox);

        } // catch

    } // run

} // SourceFilter