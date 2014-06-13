import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ServerThread {
    private Socket socket = null;
    private BufferedReader is = null;
	private PrintWriter os = null;
	private String serverName;
	
    public ServerThread(Socket socket){
        this.socket = socket;
    }
    
    public void setup(){
        try {
        	is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			os = new PrintWriter(socket.getOutputStream());
			
			serverName = is.readLine();
			
			System.out.println(serverName + " is up!");
			
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void sendMessage(String message) {
    	System.out.println(message);
    	os.println(message);
    	os.flush();
    	
    	try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    public String receiveMessage() throws Exception {
    	String message = is.readLine();
    	
    	System.out.println(message);
    	
    	return message;
    }
    
    public String getServerName() {
    	return serverName;
    }
    
    public Socket getSocket() {
    	return socket;
    }
    
    public void cleanup() throws Exception {
    	os.close();
        is.close();
        socket.close();
    }
}