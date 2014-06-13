import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

public class Server {
	private static HashMap<String, VM> vmPool = new HashMap<String, VM>();
	private static String serverName;
	private static PrintWriter os;
	private static BufferedReader is;
	private static Socket socket;
	private static int[] vmMemory;
	private static String policy = "postcopy";
	private static long startTime;
	private static long endTime;

	public static void main(String[] args) {
		try {
			socket = new Socket("127.0.0.1", 4700);

			os = new PrintWriter(socket.getOutputStream());

			is = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			serverName = "Server" + System.currentTimeMillis() % 20;

			System.out.println(serverName + " is running!");

			os.println(serverName);
			os.flush();

			while (true) {
				String strCommand = is.readLine();
				String[] opts = strCommand.split(" ");
				String opt = opts[0];
				String server = "";
				String vmName = "";
				
				if(!opt.equals("setpolicy")) {
					server = opts[2];
					vmName = opts[4];
				}

				if (opt.equals("create")) {
					if (!vmPool.containsKey(vmName)) {
						
						VM vm = new VM(server, vmName);
						
						vmPool.put(vmName, vm);

						sendMessage("create vm '" + vmName + "' on " + server);

						// if not from migration, start the vm
						if (opts.length == 5) {
							vm.start();
						}
						
						vmMemory = new int [vm.getMemorySize()];
					} else {
						sendMessage("VM '" + vmName + "' is existing on " + server);
					}
				} else if (opt.equals("start")) {
					if (vmPool.containsKey(vmName)) {
						
						VM vm = vmPool.get(vmName);
						
						// if not from migration, resume it, else start it
						if(opts.length == 5) {
							vm.setResume();
						} else {
							startTime = System.currentTimeMillis();
							CopyThread cpThread3 = new CopyThread(vm, getPageQueue(vm), Integer.parseInt(vm.getPC()), true, false);
							
							if(policy.equals("lazycopy")) {
								cpThread3.start();
							
								Thread.sleep(24000);
							}
							
							vm.start();
							
							CopyThread cpThread = new CopyThread(vm, vmMemory);
							cpThread.start();
							
							while(true){
								if(vm.getIsPageFault()) {
									int faultPageIndex = findInVMMemory(vm.getFaultPage());
									
									if(faultPageIndex == -1) {
										System.out.println("PF Handle: Page" + vm.getFaultPage() + " is not in source memory!");
										
										vm.loadPageFromDisk(vm.getFaultPage());
									} else {
										System.out.println("PF Handle: Page" + vm.getFaultPage() + " is in source memeory!");
										
										CopyThread cpThread2 = new CopyThread(vm, vmMemory, faultPageIndex, true);
										cpThread2.start();
										
										while(vm.getIsPageFault()) Thread.sleep(5000);
									}
								} else {
									Thread.sleep(5000);
									vm.setNotify();
									Thread.sleep(5000);
								}
								
								if(!vm.getIsMigration()) {
									System.out.println("Migration is done!");
									if(policy.equals("lazycopy")) {
										cpThread3.stop();
									}
									break;
								}
							}
							
							endTime = System.currentTimeMillis();
							
							System.out.println("Total Page Fault: " + vm.getPageFaultCount());
							System.out.println("Total Migration Time: " + (endTime - startTime));
						}
						
						sendMessage("VM '" + vmName + "' starts on " + server);
					} else {
						sendMessage("VM '" + vmName + "' is not existing on " + server);
					}
				} else if (opt.equals("stop")) {
					if (vmPool.containsKey(vmName)) {
						
						VM vm = vmPool.get(vmName);
						vm.setSleep();
						
						sendMessage("VM '" + vmName + "' stops on " + server);
					} else if (vmName.equals("all")) {
						for (VM vm : vmPool.values()) {
							vm.setStop();
						}

						sendMessage("VM '" + vmName + "' stops on " + server);
					} else {
						sendMessage("VM '" + vmName + "' is not existing on " + server);
					}
				} else if (opt.equals("get")) {
					String flag = opts[6];

					if (vmPool.containsKey(vmName)) {
						VM vm = vmPool.get(vmName);

						if (flag.equals("me")) {
							sendMessage(vm.getMemory());
						} else if (flag.equals("pc")) {
							sendMessage(vm.getPC());
						} else if (flag.equals("pq")) {
							sendMessage(vm.getPageQueue());
						}
					}
				} else if (opt.equals("set")) {
					String flag = opts[6];
					String data = opts[8];
					
					if (vmPool.containsKey(vmName)) {
						VM vm = vmPool.get(vmName);

						if (flag.equals("pc")) {
							vm.setPC(data);
							vm.setIsMigration(true);
						} else if (flag.equals("pq")) {
							vm.setPageQueue(data);
						} else if (flag.equals("me")) {
							String[] pages = data.split(",");
							
							for(int i=0; i<pages.length; i++) {
								vmMemory[i] = Integer.valueOf(pages[i]);
							}
						}
					}
				} else if (opt.equals("setpolicy")) {
					policy = opts[2];
				} else {
					sendMessage("VM '" + vmName + "' is not existing on " + server);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static int findInVMMemory(int faultPage) {
		int index = -1;
		
		for(int i=0; i<vmMemory.length; i++) {
			if(vmMemory[i] == faultPage) {
				index = i;
				break;
			}
		}
		
		return index;
	}

	private static void sendMessage(String message) {
		System.out.println(message);
		os.println(message);
		os.flush();
	}
	
	private static int[] getPageQueue(VM vm) {
		String[] pagesString = vm.getPageQueue().split(",");
		
		int[] pages = new int[pagesString.length];
		for(int i=0; i<pages.length; i++) {
			pages[i] = Integer.parseInt(pagesString[i]);
		}
		
		return pages;
	}
}
