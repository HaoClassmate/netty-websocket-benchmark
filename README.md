# netty-websocket-benchmark  
This repository contains a Java JMH-based benchmark suite comparing WebSocket throughput and latency between [Netty](https://netty.io/) and a simple baseline implementation using [`Java-WebSocket`](https://github.com/TooTallNate/Java-WebSocket).  
## Project structure  
` .`  
├── pom.xml                  # Maven build and dependency definitions  
├── src/main/java  
│   └── com/example/bench  
│       ├── NettyWebSocketBenchmark.java     # JMH benchmark for Netty WebSocket  
│       └── JavaWebSocketBenchmark.java      # JMH benchmark for baseline library  
## Building  
This project uses Maven to build the benchmarks. Make sure you have JDK 8+ and Maven installed. To build the benchmarks and generate an executable JMH jar, run:  
```
mvn clean package
```  
This will download dependencies (Netty, Java‑WebSocket, JMH) and create a fat JAR at `target/benchmarks.jar`.  
## Running benchmarks  
After building, run the benchmarks with:  
```
java -jar target/benchmarks.jar
```  
By default this will run all benchmarks and report throughput (ops/s) and average latency (µs) for Netty and the baseline. You can use standard JMH options (e.g., `-wi 3 -i 5` for warmup/measurement iterations, `-f 1` for forks) to control the runs.  
### Example commands  
Run all benchmarks:  
```
java -jar target/benchmarks.jar
```  
Run only the Netty benchmark:  
```
java -jar target/benchmarks.jar -bm throughput -rf json -rff netty-throughput.json -wi 3 -i 5 -f 1 '.*NettyWebSocketBenchmark.*'
```  
Adjust the regex as needed for other modes.  
## Summarized results template  
After running the benchmarks, you can fill in the following table with throughput and latency numbers (higher throughput and lower latency are better):  
| Implementation            | Throughput (ops/s) | Average latency (µs) |  
|--------------------------|--------------------|----------------------|  
| Netty (WebSocket)        |                    |                      |  
| Java‑WebSocket (baseline)|                    |                      |  
## Initial benchmark report  
To provide an initial sense of expected performance, we looked at community reports about Netty’s WebSocket performance. In a discussion on the Netty mailing list, a user reported that a simple Netty TCP echo server can handle about **100 k requests per second on a single channel**, but that aggregate throughput drops to around **15–20 k requests per second when many clients and channels are active simultaneously** ([groups.google.com](https://groups.google.com/g/netty/c/i7a_Y8YuQ2M#:~:text=very%20high%20throughputs,the%20delay%20in%20sending%20successive)). This indicates that Netty can achieve very high throughput in simple scenarios but may experience throughput degradation under heavy concurrency ([groups.google.com](https://groups.google.com/g/netty/c/i7a_Y8YuQ2M#:~:text=very%20high%20throughputs,the%20delay%20in%20sending%20successive)). These figures are provided as a starting point for comparison; your own benchmark results may differ depending on hardware, workload and configuration.
