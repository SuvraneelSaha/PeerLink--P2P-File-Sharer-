package P2P.controller;

import P2P.service.FileSharer;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileController {
    private final FileSharer fileSharer;
    private final HttpServer server;
    private final String uploadDir;
    // client will be sending file and that file will be stored in temp dir ;
    private final ExecutorService executorService;

    public FileController(int port) throws IOException{
        // port -- whichever client is calling the server for request will give the port no.
        this.fileSharer = new FileSharer();
        this.server = HttpServer.create(new InetSocketAddress(port) , 0 );
        this.uploadDir = System.getProperty("java.io.tmpdir") + File.separator + "peerlink-uploads";
        // we will be using tmpDir -- as soon as the file streams are closed , it will be deleted
        // we will be making a sub dir inside of our tmpdir
        this.executorService = Executors.newFixedThreadPool(10);
        // 10 clients at a time we can handle in parallel with context switch

        File uploadDirFile = new File(uploadDir);
        if(!uploadDirFile.exists()){
            uploadDirFile.mkdirs();
        }
        // if the dir is not opened we will open a dir ;
        // and also if not created we will create the dir ;


        // making endpoints to our server ;
        server.createContext("/upload", new UploadHandler());
        server.createContext("/download", new DownloadHandler());
        server.createContext("/", new CORSHandler());
        // for health API ; for example if the client ie frontend is on 1 server ;
        // and the server is in another 1 server ; we need to communicate via a single api or
        // Internet or IP address and that help is being done by /
        // CORS - cross orign request

        server.setExecutor(executorService);
        // server will use the threadpool via this

    }

    public void start(){
        server.start();
        // start the server ;
        System.out.println("API Server started on port "+server.getAddress().getPort());
        // starting of the server
    }

    public void stop(){
        server.stop(0);
        executorService.shutdown();
        System.out.println("Executor Service - Thread POOL Stopped and API Server Stopped" + server.getAddress().getPort());

    }

    // all the /upload , /download are the endpoints ;
    // all these are handlers ;

     private class CORSHandler implements HttpHandler {
        // this will tell the compiler that these are the endpoints you need to map to ;
         // we have to define whatever the methods that are present inside of a interface

         @Override
         public void handle(HttpExchange exchange) throws IOException{
             // throw error when you dont want to handle inside of that func only
             Headers headers = exchange.getResponseHeaders();
             // request and response headers are the onses you have to put
             // when a client is requesting to a server ;
             // we need to make sure that the client is passing through the CORSHandler
             // so that we can set some headers to that of the request so that any client can connect to
             // out server ;
             headers.add("Access-Control-Allow-Origin" , "*");
             // this is a key value pair
             headers.add("Access-Control-Allow-Methods" , "GET, POST, OPTIONS");
             headers.add("Access-Control-Allow-Headers" , "Content-Type,Authorization");

             if(exchange.getRequestMethod().equals("OPTIONS")){
                 exchange.sendResponseHeaders(204,-1);
                 return;
                 // ie if a request has a request method of options then
                 // we will send a 204 -- no content result back to the client
                 // who has requested the server via a request
             }

             String response = "NOT FOUND";
             exchange.sendResponseHeaders(404,response.getBytes().length);
             // meaning if any requestMethods other than OPTIONS comes then it will be handled
             // by some other handlers ;
             // not by this CORSHandler
             try(OutputStream oos = exchange.getResponseBody()){
                 oos.write(response.getBytes());
//                 so no need to write oos.close() 
             }
             // we want to propagate the exception to the client that is calling the method
             // whenever we are using fileStream , Socket , OutputStream
             // after using we need to close it ;
             // close() will automatically call when we are using it inside of the try ()

         }

     }
}
