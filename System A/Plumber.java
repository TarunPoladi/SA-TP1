package system;

/**
 * File: Plumber.java
 * Project: Assignment 1
 *
 * Description:
 * This class is the main thread that instantiates and connects a set of filters.
 *
 * Parameters:      None
 *
 * Internal Methods:    None
 */

public class Plumber {
    public static void main(String argv[]) {
        /**
         * Here we instantiate the filters. The constructor accepts an integer type as the number of
         * input ports of the filter and another integer type as the number of output ports of the
         * filter.
         */

        SourceFilter source = new SourceFilter(0, 1);

        Splitter split = new Splitter(3);

        FilterTemplate filter1 = new FilterTemplate(1,1);
        FilterTemplate filter2 = new FilterTemplate(1,1);
        FilterTemplate filter3 = new FilterTemplate(1,1);

        Merger merge = new Merger(3);

        SinkFilterA sink = new SinkFilterA(1, 0);


        /**
         * Here we connect the filters starting with the last filter (the sink filter) and
         * working backwards to the source. We connect the input of each filter to the up-stream
         * filter's output until we get to the source filter. Filter has a Connect() method which
         * accepts an integer type as the input port of the filter, a FilterFramework type as the
         * filter to which is going to be connected to and an integer type as the output port of the
         * other filter. For example: filter1.connect(1, filter2, 0) means that the input port number 1
         * of filter1 is going to be connected to the output port number 0 of filter2.
         */

        sink.Connect(0, merge, 0);

        merge.Connect(0, filter1, 0);
        merge.Connect(1, filter2, 0);
        merge.Connect(2, filter3, 0);

        filter1.Connect(0, split, 0);
        filter2.Connect(0, split, 1);
        filter3.Connect(0, split, 2);

        split.Connect(0, source, 0);

        /**
         * Here we start the filters up. All-in-all,... its really kind of boring.
         */

        source.start();
        split.start();

        filter1.start();
        filter2.start();
        filter3.start();

        merge.start();
        sink.start();




    } // main

} // Plumber