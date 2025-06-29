package P2P.controller;

import P2P.service.FileSharer;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import java.io.*;
import java.io.ByteArrayOutputStream;
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

    private class UploadHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException{
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin" , "*");

            if(!exchange.getRequestMethod().equalsIgnoreCase("POST")){

                String response = "ONLY Post method is allowed , Not any other method is allowed";
                exchange.sendResponseHeaders(405,response.getBytes().length);
                // 405 - method not allowed
                try(OutputStream oos = exchange.getResponseBody()){
                    oos.write(response.getBytes());

                }
                return;

            }

            Headers requestHeaders = exchange.getRequestHeaders();
            String contentType = requestHeaders.getFirst("Content-Type");
            if(contentType == null || !contentType.startsWith("multipart/form-data")){
                String response = "Bad Request : Content-Type must be multipart/form-data";
                exchange.sendResponseHeaders(400,response.getBytes().length);
                try(OutputStream oos = exchange.getResponseBody()){
                    oos.write(response.getBytes());
                }
                return;
            }
            // if the request headers's content type is ok
            // and the request method is ok ; then we will do parsing of the request from the client side

            try{
                String boundary = contentType.substring(contentType.indexOf("boundary=")+9);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                // array of bytes which can be acted as a stream , read , close , write ,
                // just like input output stream

                IOUtils.copy(exchange.getRequestBody(),baos);
                // whatever i am getting in the request i am getting in a bytes format only
                byte[] requestData = baos.toByteArray();
                // we have converted raw data to byte array

                Multiparser parser = new MultiParser(requestData,boundary);

            } catch (Exception ex) {

            }
        }
    }

    private static class Multiparser{
        private final byte[] data;
        private final String boundary;

        public Multiparser(byte[] data,String boundary){
            this.data = data;
            this.boundary = boundary;
        }
        // parse we need to do it

        public ParseResult parse(){
            // lets now parse the raw data ;
            try{
                String dataAsString = new String(data);
                // only pdf , csv, json ,text file can be shared as it can be directly converted to string
                // this data is pure raw data of byte[]
                // Project Extention Scope - extend it to include video and photo so that we can use it ;
                // needs encoding and generic objects

                String fileNameMarker = "filename=\"";
                        // as "" is a reserved keyword
                // we have to use \
                int fileNameStart = dataAsString.indexOf(fileNameMarker);
                if(fileNameStart == -1 ){
                    return null;
                }
                int fileNameEnd = dataAsString.indexOf( "\"" , fileNameStart);

                String fileName = dataAsString.substring(fileNameStart,fileNameEnd);
                // we got the file name that the client is trying to upload ;
                



            } catch (Exception ex) {

            }

        }

    }


    public static class ParseResult{
        // no tampering allowed
        public final String fileName ;
        // name of the file
        public final byte[] fileContent;
        // all content are in byte[]

        public ParseResult(String fileName , byte[] fileContent){
            this.fileName = fileName;
            this.fileContent = fileContent;
        }
    }



    
}
