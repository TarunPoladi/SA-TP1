/******************************************************************************************************************
* File: SortFilter.java
*
* Description:
* This class creates a sort filter. The sort filter sorts the input
*
* Parameters:       None
*
* Internal Methods: None
*
******************************************************************************************************************/

// This class is used to interpret time words
import java.util.ArrayList;
import java.util.Collections;

public class SortFilter extends FilterFramework {
    SortFilter(int input, int output) {
        super(input, output);
    }

    public void run() {
        ArrayList<Long> TimeStampList = new ArrayList<>();

        int MeasurementLength = 8;  // This is the length of all measurements (including time) in bytes
        int IdLength = 4;           // This is the length of IDs in the byte stream

        int bytesread = 0;          // Number of bytes read from the input file.
        int byteswritten = 0;       // Number of bytes written to the stream.
        byte databyte = 0;          // The byte of data read from the file

        long measurement;           // This is the word used to store all measurements - conversions are illustrated.
        int i;                      // For loop variable
        int id;                     // This is the measurement id

      

        // Next we write a message to the terminal to signal Thread start

        System.out.println(this.getName() + "::SortFilter Reading ");

        while (true) {
            /**
             * Here we read a byte
             */

            try {
                id = 0;

                for (i = 0; i < IdLength; i++) {

                    databyte = ReadFilterInputPort(0); // This is where we read the byte from the stream...

                    id = id | (databyte & 0xFF); // We append the byte on to ID...

                    if (i != IdLength - 1)  // If this is not the last byte, then slide the
                    {                       // previously appended byte to the left by one byte
                        id = id << 8;       // to make room for the next byte we append to the ID

                    } // if

                    bytesread++; // Increment the byte count
                    
                } // for

                // measurement
                measurement = 0;

                for (i = 0; i < MeasurementLength; i++) {

                    databyte = ReadFilterInputPort(0);
                    measurement = measurement | (databyte & 0xFF); // We append the byte on to measurement...

                    if (i != MeasurementLength - 1)     // If this is not the last byte, then slide the
                    {                                   // previously appended byte to the left by one byte
                        measurement = measurement << 8; // to make room for the next byte we append to the measurement
                    } // if

                    bytesread++; // Increment the byte count
                    
                } // if

                // time
                if (id == 0) {
                    // Add timestamp to ArrayList
                    
                    TimeStampList.add(measurement);
                } // if

            } // try

            catch (EndOfStreamException e) {
                System.out.println(this.getName() + "::SortFilter finished reading input; bytes read: " + bytesread);
                break;

            } // catch

        } // read input while

        
        // Sort
        Collections.sort(TimeStampList);

        id=0;
        // Send bytes to next filter
        for (int j = 0; j< TimeStampList.size(); j++) {

            measurement = TimeStampList.get(j).longValue();

            for (int n = IdLength - 1; n >= 0; n--) {
                databyte = (byte) (id >> 8 * n);
                WriteFilterOutputPort(databyte, 0);

                byteswritten++;
            }
            for (i = MeasurementLength - 1; i >= 0; i--) {
                databyte = (byte) (measurement >> 8 * i);
                WriteFilterOutputPort(databyte, 0);

                byteswritten++;
            }

        } // send input while

        // Shutdown routine
        ClosePorts();
        System.out.println("::SortFilter finished sending; bytes written: " + byteswritten);

    } // run

} // SortFilter