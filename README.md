# Introduction to JDBC Streaming for Java Developers

With Microservices gaining a lot more traction in recent years, traditional enterprises running large, monolithic Java EE applications have been forced to rethink what they’ve been doing for nearly two decades. The need to modernise existing applications combined with new business requirements ultimately leads to changing technology stacks. Especially the hunt for high throughput and low resource consumption makes Reactive applications and Fast Data solutions extreme attractive. Instead of a greenfield development, most companies will work their steps towards these new challenges in risk free, smaller steps. A key to success with this approach is to find the suitable integration scenarios that support and protect both worlds. This is the first recipe in a series looking at Alpakka connectors and example integrations.

## Introduction
Before diving into the code example, a little bit of background on the relevance of streaming and interaction with existing applications will help with identifying the overall architectural use-cases.

### Streams in a monolithic world
Classical Big Data solutions have a couple of components. Ingesting events through a message broker, processing them in some kind of engine and storing them in a database or distributed filesystem. Monolithic applications can access the crunched data in the format that is needed. While these kind of architectures have many names (Data Warehousing, Analytics, etc) and come in a variety of styles (Hadoop, MapReduce, ETL, etc) they have one thing in common which is that they process workloads in batches.
![alt text](images/figure_1.gif "Classical messaging solutions")
This approach worked just fine until a couple of years ago. With the growing amount of data coming in from even more connected devices, the incremental updates or hourly batch runs are no longer sufficient enough to give a competitive advantage or even worse, won't allow the implementation of new business models. For example problems that are more
obviously “real time,” like detecting fraudulent financial activity as it happens can only be solved with low latency stream processing. However, streaming imposes new challenges that go far beyond just making batch systems run faster or more frequently. Streaming introduces new semantics for analytics. It also raises new operational challenges.
A complete reference architecture for Fast Data can be seen in the free O'Reilly Report by Dean Wampler: [Fast Data Architectures For Streaming Applications](https://info.lightbend.com/COLL-20XX-Fast-Data-Architectures-for-Streaming-Apps_LP.html).

The implications of these new architectures for classical monoliths are dramatic. Even if some platforms support asynchronous processing via REST or bidirectional streaming communication with e.g. WebSockets the majority of centralised systems is based on a blocking and synchronous server model (eg. Java EE). To modernise these systems in order to stay competitive the strangler pattern is an often used approach.
![alt text](images/figure_2.gif "Classical messaging solutions")
Mixing fast and slow producers and consumers is the biggest challenge here. And just increasing buffer sizes until some hardware limit is reached, eg. memory isn't a solution. This is where backpressure comes into play. It helps to avoid unbounded buffering across asynchronous boundaries. You can learn more about [Akka Streams, backpressure and asynchronous architectures](https://www.lightbend.com/blog/understanding-akka-streams-back-pressure-and-asynchronous-architectures) in a talk by Konrad Malawski.
Also, looking at the database and the JDBC specification it quickly becomes clear, that most databases don't support  non-blocking, asynchronous calls. Introducing blocking code in a stream based solution won't work.

### Slick for JDBC to the rescue
[Slick](http://slick.lightbend.com/) is easy to use in asynchronous, non-blocking application designs, and supports building applications according to the [Reactive Manifesto](http://www.reactivemanifesto.org/). Unlike simple wrappers around traditional, blocking database APIs, Slick gives you:
* Clean separation of I/O and CPU-intensive code: Isolating I/O allows you to keep your main thread pool busy with CPU-intensive parts of the application while waiting for I/O in the background.
* Resilience under load: When a database cannot keep up with the load of your application, Slick will not create more and more threads (thus making the situation worse) or lock out all kinds of I/O. Back-pressure is controlled efficiently through a queue (of configurable size) for database I/O actions, allowing a certain number of requests to build up with very little resource usage and failing immediately once this limit has been reached.
* Reactive Streams for asynchronous streaming.
* Efficient utilization of database resources: Slick can be tuned easily and precisely for the parallelism (number of concurrent active jobs) and resource ussage (number of currently suspended database sessions) of your database server.
But instead of using Slick directly, we want to take a closer look at Akka Streams and Alpakka first.

### What is Akka Streams?
The purpose is to offer an intuitive and safe way to formulate stream processing setups such that can then be executed  efficiently and with bounded resource usage. That means there are no more OutOfMemoryErrors. In order to achieve this  streams need to be able to limit the buffering that they employ, they also need to be able to slow down producers if the consumers cannot keep up. This feature is called back-pressure and is at the core of the Reactive Streams initiative of which Akka is a founding member. You can learn more in the article [Reactive Streams for Java Developers](https://developer.lightbend.com/blog/2017-08-18-introduction-to-reactive-streams-for-java-developers/index.html). This means that the hard problem of propagating and reacting to back-pressure has been incorporated in the design of Akka Streams already, so you have one less thing to worry about; it also means that Akka Streams interoperate seamlessly with all other Reactive Streams implementations

### Relationship with Reactive Streams
The Akka Streams API is completely decoupled from the Reactive Streams interfaces. While Akka Streams focus on the formulation of transformations on data streams the scope of Reactive Streams is just to define a common mechanism of how to move data across an asynchronous boundary without losses, buffering or resource exhaustion.

The relationship between these two is that the Akka Streams API is geared towards end-users while the Akka Streams implementation uses the Reactive Streams interfaces internally to pass data between the different processing stages. For this reason you will not find any resemblance between the Reactive Streams interfaces and the Akka Streams API. This is in line with the expectations of the Reactive Streams project, whose primary purpose is to define interfaces such that different streaming implementation can interoperate; it is not the purpose of Reactive Streams to describe an end-user API.


### What is Alpakka?
[Alpakka](https://developer.lightbend.com/docs/alpakka/current/) is an initiative, which harbours various [Akka Streams](https://doc.akka.io/docs/akka/current/stream/index.html?language=java) connectors, integration patterns, and data transformations for integration use cases. Akka Streams already has a lot of functionality that is useful for integrations. The Akka Streams DSL is able to define processing pipelines which are needed for operations on streaming data that don't fit in memory as a whole. It handles backpressure in an efficient non-blocking way that prevents out-of-memory errors, which is a typical problem when using unbounded buffering with producers that are faster than consumers. But instead of implementing everything low-level yourself, Alpakka has a growing number of [existing connectors](https://developer.lightbend.com/docs/alpakka/current/connectors.html) that can be used out of the box.

Finally, the [Alpakka Slick (JDBC)](https://developer.lightbend.com/docs/alpakka/current/slick.html) connector brings all these technologies together and helps you build highly resilient integrations between your applications and various relational databases such as DB2, Oracle, SQL Server etc.


## Walk through the example

### tbd


## Run the example

```
mvn exec:java
```

### Use
Navigate to `http://localhost:8080/`and see a list of User objects being streamed
from the database via a WebSocket connection to the browsers `<textarea>`.

Navigate to `http://localhost:8080/more` and populate another 50 users to the database.
Refresh `http://localhost:8080/` and see the updated list.
