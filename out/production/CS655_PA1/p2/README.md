PA1 part2

# How to use

## server

input port number P2_server [port]

## client

input hostname, port, measurement type(rtt or tput), probes number, msg size, server delay(in ms)
`P2_client [host] [port] [type] [probes] [size] [delay]`

# Experiment

## environment
1. see figure ex1

| S/C     | location     | machine | OS      | network | method                                                                                                                             |
|---------|--------------|---------|---------|---------|------------------------------------------------------------------------------------------------------------------------------------|
| Client  | home         | laptop  | Windows | starry | test rtt and troughput with different message size and server delay. send message for 20 times, record time span for every message |
| Server  | csa1.bu.edu  | csa1    | Linux   | Unknow | follow the protocol to response message                                                                                            |

2. see figure ex2

| S/C     | location                       | machine   | OS      | network | method                                                                                                             |
|---------|--------------------------------|-----------|---------|---------|--------------------------------------------------------------------------------------------------------------------|
| Client  | home                           | laptop    | Windows | starry | test rtt and troughput with different server delay. send message for 20 times, record time span for every message  |
| Server  | pcvm1-1.instageni.rutgers.edu  | Rutgers   | Unknow  | Unknow | follow the protocol to response message                                                                            |

## result
