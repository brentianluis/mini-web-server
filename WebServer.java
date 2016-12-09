/*
 * Author: Luis, Brent Ian
 * Student Number: 2012-46101
 * Lab Section: CD-3L
 * 
 * CMSC 137 Project 2:
 * Implements a mini-webserver using only socket programming
 */

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

// establishes connection with the client then starts a separate thread to handle the request
public class WebServer {
  
  private static ServerSocket serverSocket;

  public static void main(String[] args) throws IOException {
    
    serverSocket = new ServerSocket(8080);
    
    while (true) {
      try {
        Socket s = serverSocket.accept();
        new ClientHandler(s);
      } catch (Exception e) { }
    }//while
    
  }//main
  
}//WebServer

//reads an HTTP request and responds
class ClientHandler extends Thread {

  private Socket socket;

  public ClientHandler(Socket s) {
    socket = s;
    start();
  }//constructor

  @SuppressWarnings("resource")
    public void run() {
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      PrintStream out = new PrintStream(new BufferedOutputStream(socket.getOutputStream()));

      String s = in.readLine();
      System.out.println(s);

      String filename="";
      StringTokenizer st=new StringTokenizer(s);

      try {
        //parses filename
        if (st.hasMoreElements() && st.nextToken().equalsIgnoreCase("GET") && st.hasMoreElements()){
          filename=st.nextToken();
        } else{
          throw new FileNotFoundException();
        }//else

        if (filename.endsWith("/")){
          filename += "index.html";
        }//if

        while (filename.indexOf("/") == 0){
          filename = filename.substring(1);
        }//while

        filename = filename.replace('/', File.separator.charAt(0));
        

        /*
         * writes index.html content to temp.html with appended GET keys and values
         */
        File temp = new File("temp.html");
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        BufferedWriter writer = new BufferedWriter(new FileWriter(temp));

        String line;

        while((line = reader.readLine()) != null){
          if(line.contains("<BODY>")){
            String keys;
            writer.write("<table>\n");

            while((keys = in.readLine()) != null && !keys.equals("")){
              StringTokenizer values = new StringTokenizer(keys, ":");
              writer.write("<tr>\n");
              
              while (values.hasMoreTokens()) {
                 System.out.println();
                 writer.write("<td>" + values.nextToken() + "</td>\n");
              }//while

              writer.write("</tr>\n");              
              System.out.println(keys + "-");      
            }//while

            writer.write("</table>\n");       
          }//if

          writer.write(line);
          writer.write("\n");
        }//while

        writer.close(); 
        reader.close();       

        InputStream f = new FileInputStream(temp);

        //determines MIME type and print HTTP header
        String mimeType = "text/plain";
        if(filename.endsWith(".html")){
          mimeType="text/html";
        }//if

        out.print("HTTP/1.0 200 OK\r\n" + "Content-type: "+ mimeType + "\r\n\r\n");    

        byte[] a = new byte[4096];
        int n;

        //sends file contents to client
        while ((n = f.read(a))>0)
          out.write(a, 0, n);        
        out.close();
      } catch (FileNotFoundException e) {
        out.println("HTTP/1.0 404 Not Found\r\n" + "Content-type: text/html\r\n\r\n" + "<html><head></head><body>" + filename + " not found</body></html>\n");
        out.close();
      }//catch
    } catch (IOException e) { }

  }//run

}//WebServer