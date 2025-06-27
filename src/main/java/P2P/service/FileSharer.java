package P2P.service;

import P2P.utils.UploadUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class FileSharer {

    private HashMap<Integer,String> availableFiles;
//    Port , FileName

    public FileSharer(){
        availableFiles = new HashMap<>();
//        whenever an object will be made the hashmap will be initialized
    }

    public int offerFile(String filePath){
        // generate a port based on the uploaded file
        int port;

        while (true){
            port = UploadUtils.generateCode();
            // i want to find a port that is not being used / reserved for any file which
            // is being served by my fileSharer class
            if(!availableFiles.containsKey(port)){
                // fill the port no.
                availableFiles.put(port,filePath);
                return port ;
            }
        }
    }

    public void startFileServer(int port){
        // 1st- fileController /download api - hit
        // 2nd - one thread will be responsible for sharing a file through the network
        String filePath = availableFiles.get(port);
        if(filePath==null){
            System.out.println("No file is associated with the port Number "+port);
            return;
        }

        try(ServerSocket serverSocket = new ServerSocket(port)){
            // when you are sending or receiving anything you must use try catch block for it
             // if you open a socket inside the try - it will be automatically be closed once you go out of it

            System.out.println("Serving File "+new File(filePath).getName() + " on Port Number : "+port);
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client Connection " + clientSocket.getInetAddress());
            new Thread(new FileSenderHandler(clientSocket,filePath)).start();


        } catch (IOException ex) {
            System.out.println("Error handling file server on port " + port);
        }
    }

    private static class FileSenderHandler implements Runnable{
// it is now a runnable

        private final Socket clientSocket ;
        private final String filePath ;

        public FileSenderHandler(Socket clientSocket , String filePath){
            this.clientSocket = clientSocket;
            this.filePath = filePath;

        }
        @Override
        public void run(){
            // doing the custom implementation of the Runnable interface here ;
            //
            try(FileInputStream fis = new FileInputStream(filePath)){
                // streams -- every io happen through streams ;
                // even socket is a stream

                OutputStream oos = clientSocket.getOutputStream();
                String fileName = new File(filePath).getName();
                String header = "Filename : " + fileName +"\n";
                oos.write(header.getBytes());
                // header.getBytes() -- it is the raw data that we are sending

                byte[] buffer = new byte[4096];
                int byteRead ;
                while(byteRead = fis.read(buffer) != -1 ){
                    oos.write(buffer,0,byteRead);

                }
                System.out.println("File " + fileName + " sent to " +clientSocket.getInetAddress());

            }catch (Exception ex){
                System.out.println("Error while sending file to the client " + ex.getMessage());
            }
            finally {
                try{
                    clientSocket.close();
                } catch (Exception ex) {
                    System.out.println("Error while closing Socket " +ex.getMessage()) ;
                }
            }


        }
    }
}
