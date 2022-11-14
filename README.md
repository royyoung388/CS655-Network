# PA1 part1
Echo Client-Server Application.

## server
Use while loop to receive message and send it back. Quit when receive 'quit'.  
Input the port number.  
`P1_server [port]`

## client
Send message to server, and print response. Quit when receive 'quit'.   
Input the server address and port number.  
`P1_client [server] [port]`

## Usage
1. start the server with specific port
2. start the client with correct server address and port
3. input anything in client. message will show and send back on server.r.

# PA1 part2
Performing RTT and Throughput Measurements.

## server
Use while loop to accept socket connection, create thread to process client request.
#### usage
input port number   
`P2_server [port]`

## client
Send protocol message according to the parameter.
#### usage
input hostname, port, measurement type(rtt or tput), probes number, msg size, server delay(in ms)  
`P2_client [host] [port] [type] [probes] [size] [delay]`

## Experiment

### environment


| S/C     | location                        | machine     | OS      | network          | method                                                                                                                              |
|---------|---------------------------------|-------------|---------|------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| Client  | home                            | laptop      | Windows | Starry (150Mbps) | test rtt and throughput with different message size and server delay. send message for 20 times, record time span for every message |
| Server  | csa1.bu.edu                     | csa1        | Linux   | Unknown          | follow the protocol to response message                                                                                             |
| Server  | pcvm1-1.instageni.rutgers.edu   | Rutgers     | Unknown | Unknown          | follow the protocol to response message                                                                                             |

### RTT result
1. Line is flat, RTT didn't change too much with different payload size
2. Server delay result in larger RTT
3. RTT for Rutgers server is much larger than BU server
4. Transmission time has less effect compared with propagation time.
   ![rtt](RTT.png)

### Throughput result
1. Line trend is increase. Throughput increase with more payload size
2. Server delay result in smaller throughput, but don't change the trend
3. Throughput for Rutgers server is really low due to large RTT
   ![tput](throughput.png)
