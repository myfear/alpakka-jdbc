# Introduction to JDBC Streaming for Java Developers

The Alpakka Slick (JDBC) connector helps you build highly resilient integrations between your applications and various relational databases such as DB2, Oracle, SQL Server etc. 

## Run with

```
mvn exec:java
```

Navigate to `http://localhost:8080/`and see a list of User objects being streamed 
from the database via a WebSocket connection to the browsers `<textarea>`.
Navigate to `http://localhost:8080/more` and populate another 50 users to the database. 
Refresh `http://localhost:8080/` and see the updated list.