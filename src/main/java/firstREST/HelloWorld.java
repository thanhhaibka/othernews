package firstREST;

import org.restlet.data.Protocol;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.restlet.Server;

public class HelloWorld extends ServerResource{

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        // Create the HTTP server and listen on port
        new Server(Protocol.HTTP, 3333, HelloWorld.class).start();
    }

    @Get
    public String present(){
        return "hello, world";
    }

}