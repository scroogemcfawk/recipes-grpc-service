import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecipeClient {
    private static final Logger logger = Logger.getLogger(RecipeClient.class.getName());

    private final RecipeServerGrpc.RecipeServerBlockingStub blockingStub;

    public RecipeClient(Channel channel) {
        blockingStub = RecipeServerGrpc.newBlockingStub(channel);
    }

    public void getRecipes(String query) {
        logger.info("Will try to get " + query + " recipe...");
        ServerRequest request = ServerRequest.newBuilder().setQuery(query).build();
        ServerResponse response;
        try {
            response = blockingStub.getRecipes(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Result: " + response.getResult(0));
    }


    public static void main(String[] args) throws Exception {
        String target = "localhost:50052";

        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                                         .build();
        try {
            RecipeClient client = new RecipeClient(channel);
            client.getRecipes("grech");
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
