import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;


public class Manager {
	
    private static HashMap<String, ServerThread> serverPool = new HashMap<String, ServerThread>();
    private static String migrationPolicy = "postcopy";
    private static Migration migration = new Migration();
	
	public static void main(String[] args) throws Exception {
		ServerSocket server = null;
		
		try {
			server = new ServerSocket(4700);
			
			Socket socket = null;
			
			System.out.println("Waiting for server to connect!");
			
			int serverCount = 0;
			
			while(serverCount < 2) {
				socket = server.accept();
				ServerThread serverThread = new ServerThread(socket);
				serverThread.setup();
				 
				serverPool.put(serverThread.getServerName(), serverThread);
				serverCount++;
			 }
			 
			 System.out.println("Two servers are ready!");
			 
			 System.out.println("Usage:\r\n"
			 						 + "create -s <Server> -v <VM>\r\n"
                     			     + "start -s <Server> -v <VM>\r\n"
                                     + "stop -s <Server> -v <VM>\r\n"
                                     + "set -p <MigrationPolicy>\r\n"
                                     + "get -p mp\r\n"
                                     + "migrate -s <Server> -v <VM> -t <Server>");
			 
			 String strCommand = null;
				
			 BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
				
			 while(true) {
				strCommand = stdIn.readLine();
					
				if(checkCommand(strCommand)) {
					System.out.println("Invalid Command, input 'h' for help, 'q' for exit!");
					continue;
				}
					
				String[] opts = strCommand.split(" ");
				
				String action = opts[0];
					
				if(action.equals("h")) {
					System.out.println("Usage:\r\n"
	 						 + "create -s <Server> -v <VM>\r\n"
            			     + "start -s <Server> -v <VM>\r\n"
                             + "stop -s <Server> -v <VM>\r\n"
                             + "set -p <MigrationPolicy>\r\n"
                             + "get -p mp\r\n"
                             + "migrate -s <Server> -v <VM> -t <Server>");
					
					continue;
				}
					
				if(action.equals("q")) {
					break;
				} else if(action.equals("set")) {
					migrationPolicy = opts[2];
					
					System.out.println("Current policy is " + migrationPolicy);
				} else if(action.equals("get")) {
					System.out.println("Current policy is " + migrationPolicy);
				} else if(opts[0].equals("migrate")) {
					String source = opts[2];
					String vmName = opts[4];
					String target = opts[6];
					
					if(migrationPolicy.equals("postcopy")) {
						migration.runPostCopy(serverPool.get(source), serverPool.get(target), source, vmName, target);
					} else if(migrationPolicy.equals("lazycopy")) {
						migration.runLazyCopy(serverPool.get(source), serverPool.get(target), source, vmName, target);
					} else {
						System.out.println("Manager: invalid migration policy!");
					}
				} else {
					String serverName = opts[2];
					
					if(serverPool.containsKey(serverName)) {
						serverPool.get(serverName).sendMessage(strCommand);
					
						System.out.println("Manager: " + strCommand);
						System.out.println(serverName + ": " + serverPool.get(serverName).receiveMessage());
					} else {
						System.out.println("Server '" + serverName + "' is not existing!");
					}
				}
			}
			
			for(ServerThread thread : serverPool.values()) {
				thread.cleanup();
			}
			
            server.close();
        } catch(Exception e){
        	e.printStackTrace();
        }
	}

	private static Boolean checkCommand(final String strCommand) {
		Boolean isInValid = false;
		
		if(strCommand.isEmpty() || strCommand.trim().isEmpty()) {
			isInValid = true;
		} else if(strCommand.equals("h") || strCommand.equals("q")) {
			return isInValid;
		} else if(!strCommand.startsWith("create") && !strCommand.startsWith("start") 
				&& !strCommand.startsWith("stop") && !strCommand.startsWith("set") 
				&& !strCommand.startsWith("get") && !strCommand.startsWith("migrate")) {
			isInValid = true;
		} 
		
		String[] opts = strCommand.split(" ");
		
		if(opts.length != 3 && opts.length != 5 && opts.length != 7 && opts.length != 9) {
			isInValid = true;
		}
		
		if(opts.length == 5 && (!opts[1].equals("-s") || !opts[3].equals("-v"))) {
			isInValid = true;
		}
		
		if(opts.length == 3 && !opts[1].equals("-p")) {
			isInValid = true;
		}
		
		if(opts.length == 7 && (!opts[1].equals("-s") || !opts[3].equals("-v") || (!opts[5].equals("-t") && !opts[5].equals("-p")))) {
			isInValid = true;
		}
		
		if(opts.length == 9 && (!opts[1].equals("-s") || !opts[3].equals("-v") || !opts[5].equals("-p") || !opts[7].equals("-d"))) {
			isInValid = true;
		}
		
		return isInValid;
	}
}
