import java.io.* ;
import java.net.* ;
import java.util.* ;

public final class WebServer
{   
    public static Mimes m;
    
    public static void main(String argv[]) throws Exception
    {   
        m = new Mimes("C:\\Users\\Baris\\Documents\\NetBeansProjects\\WebServer\\src\\test\\mime.types");
        m.makeTable();
        
        // Set the port number.
        int port = 6789;
        // Establish the listen socket.
        ServerSocket welcomeSocket = new ServerSocket(port);

        // Process HTTP service requests in an infinite loop.
        while (true) {
                // Listen for a TCP connection request.
                Socket connectionSocket = welcomeSocket.accept();

                // When a connection request is received
                // Construct an object to process the HTTP request message.
                // Create a new thread to process the request.
                // Start the thread.
                HttpRequest request = new HttpRequest(connectionSocket);
                Thread thread = new Thread(request);
                thread.start();
        }
    }
}

final class HttpRequest implements Runnable
{
    final static String CRLF = "\r\n";
    Socket socket;

    // Constructor
    public HttpRequest(Socket socket) throws Exception 
    {  
        this.socket = socket;
    }

    // Implement the run() method of the Runnable interface.
    public void run()
    {
            try {
                    processHttpRequest();
            } catch (Exception e) {
                    System.out.println(e);
            }
    }

    private void processHttpRequest() throws Exception
    {
        // Get a reference to the socket's input and output streams.
        InputStream is = new DataInputStream(socket.getInputStream());
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());

        // Set up input stream filters.
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Get the request line of the HTTP request message.
        String requestLine = br.readLine();

        // Display the request line.
        System.out.println();
        System.out.println(requestLine);

        // Get and display the header lines.
        String clientIP = null;
        String userAgent = null;

        String headerLine = null;
        while ((headerLine = br.readLine()).length() != 0) {
                //System.out.println(headerLine);
                if(headerLine.contains("Host:")){
                        clientIP = headerLine.split(" ")[1];
                }
                if(headerLine.contains("User-Agent:")){
                        userAgent = headerLine.replace("User-Agent:", "");
                }
        }

        // Extract the filename from the request line.
        StringTokenizer tokens = new StringTokenizer(requestLine);
        tokens.nextToken();  // skip over the method, which should be "GET"
        String fileName = tokens.nextToken();

        // Prepend a "." so that file request is within the current directory.
        fileName = "." + fileName;


        // Open the requested file.
        FileInputStream fis = null;
        boolean fileExists = true;
        try {
                fis = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
                fileExists = false;
        }


        // Construct the response message.
        String statusLine = null;
        String contentTypeLine = null;
        String entityBody = null;
        if (fileExists) {
            if(requestLine.contains("invalid")){
                statusLine = "HTTP/1.0 400 Bad Request" + CRLF;
            }else{
                statusLine = "HTTP/1.0 200 OK" + CRLF;
            }
            contentTypeLine = "Content-type: " + contentType( fileName ) + CRLF;
        } else {
            if(requestLine.contains("invalid")){
                statusLine = "HTTP/1.0 404 Bad Request" + CRLF;
            }else{
                statusLine = "HTTP/1.0 400 Not Found" + CRLF;
            }
            contentTypeLine = "Content-type: " + contentType( fileName ) + CRLF;;
            entityBody = "<HTML>" + 
                    "<HEAD><TITLE>404 Not Found</TITLE></HEAD>" +
                    "<BODY>" +
                    "404 Not Found <br>" +
                    clientIP + "<br>" +
                    userAgent +
                    "</BODY></HTML>";
        }

        // Send the status line.
        os.writeBytes(statusLine);
        // Send the content type line.
        os.writeBytes(contentTypeLine);
        // Send a blank line to indicate the end of the header lines.
        os.writeBytes(CRLF);

        // Send the entity body.
        if (fileExists)	{
            sendBytes(fis, os);
            fis.close();
        } else {
            os.writeBytes(entityBody);
        }

        // Close streams and socket.
        os.close();
        br.close();
        socket.close();
    }

    private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception
    {
       // Construct a 1K buffer to hold bytes on their way to the socket.
       byte[] buffer = new byte[1024];
       int bytes = 0;

       // Copy requested file into the socket's output stream.
       while((bytes = fis.read(buffer)) != -1 ) {
              os.write(buffer, 0, bytes);
       }
    }

    private String contentType(String fileName)
    {
        int index = fileName.lastIndexOf(".");
        String s = fileName.substring(index+1);
        return WebServer.m.getValueOfKey(s);
    }
}

final class Mimes{
        Hashtable<String, String> table;
        File f;
        
        public Mimes(String PATH){
            table = new Hashtable<>();
            f = new File(PATH);
        }
        
        
        public void makeTable() throws FileNotFoundException, IOException{
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            while ((line = br.readLine()) != null) {

                StringTokenizer st = new StringTokenizer(line);
                String type;
                if(st.hasMoreTokens()){
                    type = st.nextToken();
                    if(!type.startsWith("#") && !type.startsWith("\n")){
                        while (st.hasMoreTokens()) { 
                            String filetype = st.nextToken();
                            table.put(filetype, type);
                        }
                    }
                }
            }
        }
        
        public String getValueOfKey(String key){
            if(table.get(key) != null){
                return table.get(key); 
            }
            return "application/...";
        }
}
