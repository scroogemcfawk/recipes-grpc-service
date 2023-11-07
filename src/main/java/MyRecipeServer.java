import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Server that manages startup/shutdown of a {@code Greeter} server.
 */
public class MyRecipeServer {
    private static final Logger logger = Logger.getLogger(MyRecipeServer.class.getName());

    private Server server;

    private static ArrayList<Recipe> recipes;

    private void start() throws IOException {
        /* The port on which the server should run */

        recipes = new ArrayList<>();
        recipes.add(
                Recipe.newBuilder()
                        .setName("Grechka")
                        .addIngredients(
                                Recipe.Ingredient.newBuilder()
                                        .setProduct("Grechka")
                                        .setWeight(250f)
                                        .build()
                        )
                        .addIngredients(
                                Recipe.Ingredient.newBuilder()
                                        .setProduct("Water")
                                        .setWeight(500f)
                                        .build()
                        )
                        .build()
        );

        int port = 50052;
        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                         .addService(new RecipeServerImpl())
                         .build()
                         .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    MyRecipeServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final MyRecipeServer server = new MyRecipeServer();
        server.start();
        server.blockUntilShutdown();
    }

    static private class RecipeServerImpl extends RecipeServerGrpc.RecipeServerImplBase {
        @Override
        public void getRecipes(ServerRequest request, StreamObserver<ServerResponse> responseObserver)
        {
            ArrayList<Recipe> resList = new ArrayList<>();
            for (var r : recipes) {
                if (r.getName().toLowerCase().contains(request.getQuery().toLowerCase())) {
                    resList.add(r);
                }
            }
            ServerResponse res = ServerResponse.newBuilder().addAllResult(resList).build();
            responseObserver.onNext(res);
            responseObserver.onCompleted();
        }
    }
}
