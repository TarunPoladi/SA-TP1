# <div align="center">Software Architecture course</div>

## <div align="center">Practical Work 1</div>

#### Objectives
You will be provided a working sample system that uses a (coding) framework supporting the pipe-and-filter paradigm. The application domain for this assignment is signal processing applications, as described below. Your task is to extend the existing framework to architect and build the systems specified in the requirements below.

#### Business Context and Key Architectural Approaches
The principle stakeholder for this system is an organization that builds instrumentation systems. Instrumentation is a typical kind of signal processing application where streams of data are read, processed in a variety of ways, and displayed or stored for later use. A key part of modern instrumentation systems is the software that is used to process byte streams of data. The organization would like to create flexible software that can be reconfigured for a variety of applications and platform (for our purposes, we can think of "platforms" as processors). For example, one application might be to support instrumentation for an automobile that would include data streams that originate with sensors and terminate in the cabin of the auto with a display of temperature, oil pressure, velocity and so forth. Some subset of filters for this application might be used in aviation, space, or maritime applications. Another applications might be the lab reading streams of data from a file, processing the stream, and storing the data in a file. This would support the development and debugging of instrumentation systems. While it is critically important to support reconfiguration, the system must also process streams of data as quickly as possible. To meet these challenges, the architect has decided to design the system around a pipe-and-filter architectural pattern. From a dynamic perspective, systems would be structured as shown in the following examples. 	

![](https://dl.dropboxusercontent.com/u/15756440/AS%20pipe%20and%20filter1.png)

The "data sources" in these systems are special filter that read data from sensors, files, or that generate data internally within the filter. All filter networks must start with a source. The "filters" shown in these examples are standard filters that read data from an upstream pipe, transform the data, and write data to a downstream pipe. The "data sinks" are special filters that read data from an upstream filter, but write data to a file or device of some kind. All filter networks must terminate with a sink that consumes the final data. Note that streams can be split and merged as shown in these examples.

The organization's architect has developed a set of classes to facilitate the rapid development of filters and applications that can be quickly tested and deployed. These libraries have been provived to you. In addition there are several examples that have been provived to illustrate the use of these classes. The class structure (static perspective) for filters is as follows:

![](https://dl.dropboxusercontent.com/u/15756440/AS%20pipe%20and%20filter2.png)

The FilterFramework class is the base class for all filters. It contains methods for managing the connections to pipes, writing and reading data to and from pipes, and setting up filters as separate threads. three filter "templates" have been established to ease the work of creating cource, sink, and standard filters in a consistent way. Each of these filter templates describes how to write code for the three basic types of filters. Note that the current framework does not support splitting or merging the data stream. A fourth template, called the "PlumberTemplate" shows how pipe-and-filter networks can be set up from the filters created by developers. The "Plumber" is responsible for instantiating the filters and connecting them together. Once done with this, the plumber exits.

#### Data Stream Format
The system's data streams will follow a predetermined format of measurement ID and data point. Each measurement has a unique ID beginning with zero. The ID of zero is always associated with time. Test files have been provided that contain test flight data that you will use for the project. The file data is in binary format. The table below lists the measurements, IDs, and byte sizes of the data in these files.

| ID  | Data Descriptions and Units | Type | Number of Bytes |
| :---: | :--- | :---: | :---: |
| N/A  | Measurement ID: Each measurement has an ID wich indicates the type of measurement. The Measurement IDs are listed in this table in the left column | Integer | 4 |
| 000 | Time: This is the number of milliseconds since the Epoch (00:00:00 GMT on January 1, 1970). | Long Integer | 8 |
| 001 | Velocity: This is the airspeed of the vehicle. It is measured in knots per hour. | Double | 8 |
| 002 | Altitude: This is the vehicle's distance from the surface of the earth. It is measured in feet. | Double | 8 |
| 003 | Pressure: This is atmospheric pressure external to the vehicle. It is measured in PSI. | Double | 8 |
| 004 | Temperature: This is the temperature of the vehicle's hull. It is measured in degrees Fahrenheit. | Double | 8 |
| 005 | Pitch: This is the angle of the nose of the vehicle relative to the surface of the earth. A pitch of 0 indicates that the vehicle is traveling level with respect to the earth. A positive value indicates that the vehicle is climbing. A negative value indicates that the vehicle is descending. | Double | 8 |

Data in the stream is recorded in frames beginning with time, and followed by data with IDs between 1 and n, with n≤5. A set of time and data is called a frame. The time corresponds to when the data in the frame was recorded. This pattern is repeated until the end of stream is reached. Each frame is written in a stream as follows:

![](https://dl.dropboxusercontent.com/u/15756440/AS%20pipe%20and%20filter4.png)

#### Installing the Source Code
The Templates directory contains the cource code templates for the filters described above. The DataSets directory has all of the test data that you will need. The Sample directory contains a working pipe-and-filter network example that illustrates the basic framework. To compile the example in the Sample directory, open a command prompt window (or start a Linux comand Line terminal), change the working directory to Sample, and type the following:

`
$ javac *.java
`

The compile process above creates the class files. After you compile the system, you can execute it by typing the following:

`
$ java Plumber > Outuput.dat
`

Sample is a basic pipe-and-filter network that shows how to instantiate and connect filters, how to read data from Flightdata.dat file, and how to extract measurements from the data stream.

#### Design and Construction
Your task is to use the existing framework as a basis for creating three new systems. Each new system has one or more requirements. In each system, please adhere to the pipe-and-filter architectural pattern as closely as possible. Make sure that you use good programming practices including comments that describe the role and function of any new modules, as well as describing how you changed the base system modules.

## System A
Create a pipe-and-filter network that will read the data stream in Flightdata.dat file, convert the temperature measurements from Fahrenheit to Celsius, and convert altitude from feet to meters. Filter out the other measurements and write the output to a text file called OutputA.dat. Format the output as follows:

![](https://dl.dropboxusercontent.com/u/15756440/AS%20pipe%20and%20filterA.png)

## Sistema B
Create a pipe-and-filter network that does what System A does but includes pressure data. In addition, System B should filter "wild points" out of the data stream for pressure measurements. A wild point is any pressure data measurement that exceeds 80 psi, or is less than 50 psi. For wild points encountered in the stream, interpolate a replacement value by using the last known valid measurement and the next valid measument in the stream. Interpolate the replacement value by computing the average of the last valid measurement and the next valid measurement in the stream. If a wild point occurs at the beginning of the stream, replace it with the first valid value. If a wild point occurs at the end of the stream, replace it with the last valid value. Write the output to a text file called OutputB.dat and format the output as shown below - denote any interpolated values with an asterisk by the value as shown below for the second pressure measurement:

![](https://dl.dropboxusercontent.com/u/15756440/AS%20pipe%20and%20filterB1.png)

Write the rejected wild point values and the time that they occurred to a second text file called WildPoints.dat using a similar format as follows:

![](https://dl.dropboxusercontent.com/u/15756440/AS%20pipe%20and%20filterB2.png)

## Sistema C
Create a pipe-and-filter network that merges two data streams. The system should take as input the SubSetA.dat file and the SubSetB.dat. The system should merge these two streams together and time-align the data - that is, when the files are merged, the single output stream's time data should be monotonically increasing. This is illustrated bellow with a simple example. Here Stream C represents the merger of Stream A and Stream B.

![](https://dl.dropboxusercontent.com/u/15756440/AS%20pipe%20and%20filterC.png)
