/**
 * File: FilterFramework.java
 * Project: Assignment 1
 *
 * Description:
 *
 * This superclass defines a skeletal filter framework that defines a filter in terms of the input and output
 * ports. All filters must be defined in terms of this framework - that is, filters must extend this class
 * in order to be considered valid system filters. Filters as standalone threads until the inputport no longer
 * has any data - at which point the filter finishes up any work it has to do and then terminates.
 *
 * Parameters:
 *
 * InputReadPort:	This is an ArrayList with the filter's input ports. Essentially each port in this ArrayList is
 * connected to another filter's piped output steam. All filters connect to other filters by
 * connecting their input ports to other filter's output ports. This is handled by the Connect()
 * method.
 *
 * OutputWritePort:	This is an ArrayList with the filter's output ports. Essentially the filter's job is to read
 * data from the input port, perform some operation on the data, then write the transformed data
 * on the output ports.
 *
 * FilterFramework:  This is an array with references to the filters that are connected to the instance filter's
 * input port. This reference is to determine when the upstream filter has stopped sending data
 * along the pipe.
 *
 * Internal Methods:
 *
 * public void Connect( FilterFramework Filter )
 * public byte ReadFilterInputPort()
 * public void WriteFilterOutputPort(byte datum)
 * public boolean EndOfInputStream()
 */

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;

public class FilterFramework extends Thread {

    // Define filter input and output ports
    private ArrayList<PipedInputStream> InputReadPort = new ArrayList<>();
    private ArrayList<PipedOutputStream> OutputWritePort = new ArrayList<>();

    // The following reference to a filter is used because java pipes are able to reliably
    // detect broken pipes on the input port of the filter. This variable will point to
    // the previous filter in the network and when it dies, we know that it has closed its
    // output pipe and will send no more data.
    private FilterFramework[] InputFilter;

    /**
     * CONSTRUCTOR:: FilterFramework
     * Purpose: Creates the the specified number of input ports and output ports of the filter, as well as
     * the array of filter references.
     *
     * @param input:  Number of input ports of the filter
     * @param output: Number of output ports of the filter
     */
    FilterFramework(int input, int output) {
        for (int i = 0; i < input; i++)
            InputReadPort.add(new PipedInputStream());

        for (int i = 0; i < output; i++)
            OutputWritePort.add(new PipedOutputStream());

        this.InputFilter = new FilterFramework[input];

    } // FilterFramework constructor

    /**
     * METHOD:: Connect
     * Purpose: This method connects filters to each other. All connections are through the inputPort of
     * each filter. That is each filter's inputPort is connected to another filter's outputPort through
     * this method.
     *
     * @param inputPort:  The input port of the filter
     * @param Filter:     The filter itself
     * @param outputPort: The output port of the other filter
     */
    void Connect(int inputPort, FilterFramework Filter, int outputPort) {
        try {
            // Connect this filter's input to the upstream pipe's output stream
            InputReadPort.get(inputPort).connect(Filter.OutputWritePort.get(outputPort));
            InputFilter[inputPort] = Filter;
        } catch (Exception Error) {
            System.out.println("\n" + this.getName() + " FilterFramework error connecting::" + Error);
        }

    } // Connect

    /**
     * METHOD:: ReadFilterInputPort
     * Purpose: This method reads data from the input port one byte at a time.
     *
     * @param portNumber: The input port number to which the filter is going to read.
     * @return byte of data read from the input port of the filter.
     * @throws EndOfStreamException
     */
    byte ReadFilterInputPort(int portNumber) throws EndOfStreamException {
        byte datum = 0;
        /**
         *  Since delays are possible on upstream filters, we first wait until
         * there is data available on the input port. We check,... if no data is
         * available on the input port we wait for a quarter of a second and check
         * again. Note there is no timeout enforced here at all and if upstream
         * filters are deadlocked, then this can result in infinite waits in this
         * loop. It is necessary to check to see if we are at the end of stream
         * in the wait loop because it is possible that the upstream filter completes
         * while we are waiting. If this happens and we do not check for the end of
         * stream, then we could wait forever on an upstream pipe that is long gone.
         * Unfortunately Java pipes do not throw exceptions when the input pipe is
         * broken.
         */
        try {
            while (InputReadPort.get(portNumber).available() == 0) {
                if (EndOfInputStream(portNumber)) {
                    throw new EndOfStreamException("End of input stream reached");
                } // if
                sleep(250);
            } // while

        } catch (EndOfStreamException Error) {
            throw Error;
        } catch (Exception Error) {
            System.out.println("\n" + this.getName() + " Error in read port wait loop::" + Error);
        }

        /**
         * If at least one byte of data is available on the input pipe we can read it.
         * We read and write one byte to and from ports.
         */
        try {
            datum = (byte) InputReadPort.get(portNumber).read();
            return datum;
        } catch (Exception Error) {
            System.out.println("\n" + this.getName() + " Pipe read error::" + Error);
            return datum;
        }

    } // ReadFilterPort

    /**
     * METHOD:: WriteFilterOutputPort
     * Purpose: This method writes data to the output port one byte at a time.
     *
     * @param datum:      The byte that will be written on the output port of the filter
     * @param portNumber: The output port number of the filter
     */
    void WriteFilterOutputPort(byte datum, int portNumber) {
        try {
            OutputWritePort.get(portNumber).write((int) datum);
            OutputWritePort.get(portNumber).flush();
        } catch (Exception Error) {
            System.out.println("\n" + this.getName() + " Pipe write error::" + Error);
        }

    } // WriteFilterPort

    /**
     * METHOD:: EndOfInputStream
     * Purpose: This method is used within this framework which is why it is private
     * It returns a true when there is no more data to read on the input port of
     * the instance filter. What it really does is to check if the upstream filter
     * is still alive. This is done because Java does not reliably handle broken
     * input pipes and will often continue to read (junk) from a broken input pipe.
     *
     * @param portNumber: The input port number to which the input filter is associated
     * @return a value of true if the previous filter has stopped sending data,
     * false if it is still alive and sending data.
     */
    private boolean EndOfInputStream(int portNumber) {
        return !InputFilter[portNumber].isAlive();

    } // EndOfInputStream

    /**
     * METHOD:: ClosePorts
     * Purpose: This method is used to close the input and output ports of the
     * filter. It is important that filters close their ports before the filter
     * thread exits.
     */
    void ClosePorts() {
        try {

            for (PipedInputStream inputPort : InputReadPort) inputPort.close();

            for (PipedOutputStream outputPort : OutputWritePort) outputPort.close();

        } catch (Exception Error) {
            System.out.println("\n" + this.getName() + " ClosePorts error::" + Error);
        }

    } // ClosePorts

    /**
     * CONCRETE METHOD:: run
     * Purpose: This is actually an abstract method defined by Thread. It is called
     * when the thread is started by calling the Thread.start() method. In this
     * case, the run() method should be overridden by the filter programmer using
     * this framework superclass.
     */
    public void run() {
        // The run method should be overridden by the subordinate class. Please
        // see the example applications provided for more details.

    } // run

    /**
     * InnerClass:: EndOfStreamExeception
     * Purpose: This
     */
    class EndOfStreamException extends Exception {

        EndOfStreamException() {
            super();
        }

        EndOfStreamException(String s) {
            super(s);
        }

    } // class

} // FilterFramework class
