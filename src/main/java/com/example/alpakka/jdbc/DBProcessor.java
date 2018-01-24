package com.example.alpakka.jdbc;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.model.ws.Message;
import akka.http.javadsl.model.ws.TextMessage;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;

//#important-imports
import akka.stream.javadsl.*;
import akka.stream.alpakka.slick.javadsl.*;

//#important-imports
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.function.Function;
import java.util.logging.Level;

/**
 *
 * @author myfear
 */
public class DBProcessor {

    private static ActorSystem system = ActorSystem.create();
    private static Materializer materializer = ActorMaterializer.create(system);

    private static final Logger LOGGER = Logger.getLogger("DBProcessor");
    private static final Config CONFIG = ConfigFactory.load();
    // Slick session
    private static final SlickSession SESSION = SlickSession.forConfig("slick-h2");

    //users
    private static final List<User> users = IntStream.range(0, 42).boxed().map((i) -> new User(i, "Name" + i)).collect(Collectors.toList());
    private static final Function<User, String> insertUser = (user) -> "INSERT INTO USERS VALUES (" + user.id + ", '" + user.name + "')";

    //Slick Sources and Sinks
    final static Sink<User, CompletionStage<Done>> usersInsert = Slick.sink(SESSION, 4, insertUser);
    final static Source<User, NotUsed> USERSSOURCE = Source.from(users);
    final static CompletionStage<Done> insertionResultFuture = USERSSOURCE.runWith(usersInsert, materializer);

    final static Source<User, NotUsed> usersStream = Slick.source(
            SESSION,
            "SELECT ID, NAME FROM USERS",
            (SlickRow row) -> new User(row.nextInt(), row.nextString())
    );

    public static void main(String[] args) throws Exception {
        LOGGER.info("Init");

        LOGGER.info("Start Server");
        DBProcessor processor = new DBProcessor();
        Server server = new Server(processor.usersStream);
        server.startServer(CONFIG.getString("server.host"), CONFIG.getInt("server.port"));

    }

}

class Server extends HttpApp {

    private final Source<User, NotUsed> usersStream;

    Server(Source<User, NotUsed> usersStream) {
        this.usersStream = usersStream;
    }

    ;

  @Override
    protected Route routes() {
        return route(
                path("data", () -> {
                    Source<Message, NotUsed> messages
                            = usersStream.map(String::valueOf).map(TextMessage::create);
                    return handleWebSocketMessages(Flow.fromSinkAndSourceCoupled(Sink.ignore(),
                            messages));
                }),
                path("more", ()
                        -> {
                    try {
                        DBProcessor.insertionResultFuture.toCompletableFuture().get(5, TimeUnit.SECONDS);
                    } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return complete(StatusCodes.OK, "Ok");
                }),
                get(()
                        -> pathSingleSlash(()
                        -> getFromResource("index.html")
                )
                )
        );
    }
}
