import java.util.Random;


public class VM extends Thread {
	private String serverName;
	private String vmName;
	private Boolean running = true;
	private Boolean sleeping = true;
	private int memorySize = 10;
	private int[] pageQueue = new int[100];
	private int[] memory = new int[memorySize];
	private int[] copyMemoryTrack = new int[memorySize];
	private int pc = 0;
	private static int count = 0;
	private Boolean isMigration = false;
	private Boolean isPageFault = false;
	private int pageFaultCount = 0;
	private int faultPage = -1;
	private Boolean isWait = false;
	
	
	public VM(String serverName, String vmName) {
		this.serverName = serverName;
		this.vmName = vmName;
		this.running = true;
		this.sleeping = true;
		
		Random random = new Random();
		
		for(int i=0; i<100; i++) {
			pageQueue[i] = Math.abs(random.nextInt()) % 50 + 1;
		}
		
//		if(!isMigration) {
//			for(int i=0; i<100; i++) {
//				System.out.print(pageQueue[i]);
//	    		if(i!=pageQueue.length - 1) System.out.print(", ");
//			}
//		}
		
		for(int i=0; i<memorySize; i++) {
			copyMemoryTrack[i] = 0;
		}
		
		System.out.println();
	}
	
    public void run() {
    	while(running) {
    		if(sleeping) {
    			System.out.println("VM " + vmName + " is running on " + serverName +"!");
    			
    			synchronized(this) {
    				memoryReplacementLRU();
    			}
    			
    			try {
					Thread.sleep(4000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		} else {
    			//printMemory();
	    		try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
    		
    		if(sleeping) printMemory();
    	}
    }

	private void memoryReplacementLRU() {
		int pos = findPageInMemory(pageQueue[pc]);
		
		if(pos == -1) {
			if(isMigration) {
				isPageFault = true;
				isWait = true;
				pageFaultCount++;
				faultPage = pageQueue[pc];
				try {
					System.out.println("Page fault: Page" + faultPage + " is requried.");
					
					while(isWait) {
						System.out.println("Page fault: VMThread(" + Thread.currentThread().getId() + ") is waiting!");
						wait();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {				
				if(count < memorySize) {
					memory[count] = pageQueue[pc];
					count++;
				} else {
					for(int i=0; i<memorySize - 1; i++) {
						memory[i] = memory[i+1];
					}
					
					memory[memorySize - 1] = pageQueue[pc];
				}
			}
		} else {
			System.out.println("Page hit: Page" + memory[pos] + " is in memory!");

			int temp = memory[pos];
			int index;
			
			if(count < memorySize)
			{
				index = count;
			} else {
				index = memorySize;
			}
			
			for(int i=pos; i<index-1; i++) {
				memory[i] = memory[i+1];
			}
			
			memory[index-1] = temp;
		}
		
		pc++;
		
		if(pc >= 100) {
			pc = pc % 100;
		}
	}
    
    public void printMemory() {
    	for(int i=0; i<memory.length; i++)
    	{
    		System.out.print(memory[i]);
    		if(i!=memory.length - 1) System.out.print(", ");
    	}
    	
    	System.out.println();
    	System.out.println("current PC is "+ pc);
    }
    
    public void setStop() {
    	this.running = false;
    }
    
    public void setStart() {
    	this.running = true;
    }
    
    public void setSleep() {
    	this.sleeping = false;
    }
    
    public void setResume() {
    	this.sleeping = true;
    }
    
    public int findPageInMemory(int page) {
    	for(int i=0; i<memorySize; i++) {
			if(memory[i] == page) return i;
		}
    	
    	return -1;
    }
    
    public String getPC() {
    	return String.valueOf(pc);
    }
    
    public void setPC(String pc) {
    	this.pc = Integer.parseInt(pc);
    	
    	System.out.println("Current PC is " + this.pc);
    }
    
    public String getMemory() {
    	String temp = "";
    	for(int i=0; i<memorySize; i++) {
    		temp += memory[i];
    		if(i != memory.length - 1) temp+=",";
    	}
    	
    	return temp;
    }
    
    public void setMemory(String memory) {
    	String[] temp = memory.split(",");
    	
    	for(int i=0; i<memorySize; i++) {
    		this.memory[i] = Integer.parseInt(temp[i]);
    	}
    }
    
    public int getMemorySize() {
    	return memorySize;
    }
    
    public String getPageQueue() {
    	String temp = "";
    	for(int i=0; i<pageQueue.length; i++) {
    		temp += pageQueue[i];
    		if(i != pageQueue.length - 1) temp+=",";
    	}
    	
    	return temp;
    }
    
    public void setPageQueue(String pageQueue) {
    	String[] temp = pageQueue.split(",");
    	
    	for(int i=0; i<this.pageQueue.length; i++) {
    		this.pageQueue[i] = Integer.parseInt(temp[i]);
    		if(i != this.pageQueue.length - 1) System.out.print(this.pageQueue[i] + ",");
    		else System.out.print(this.pageQueue[i]);
    	}
    	
    	System.out.println();
    }
    
    public void setIsMigration(Boolean isMigration) {
    	this.isMigration = isMigration;
    }
    
    public Boolean getIsMigration() {
    	return isMigration;
    }
    
    public void setIsFaultPageNotInMemory(Boolean isFaultPageNotInMemory) {
    }
    
    public void setIsPageFault(Boolean isPageFault) {
    	this.isPageFault = isPageFault;
    }
    
    public void setWait() {
    	this.isWait = true;
    }
    
    public synchronized void setNotify() {
    	this.isWait = false;
    	notifyAll();
    }
    
    public Boolean getIsPageFault() {
    	return isPageFault;
    }
    
    public int getFaultPage() {
    	return faultPage;
    }
    
    public int getPageFaultCount() {
    	return pageFaultCount;
    }
      
    public synchronized void loadPageFromDisk(int page) {	
		if(count < memorySize) {
			memory[count] = page;
			count++;
		} else {
			for(int i=0; i<memorySize - 1; i++) {
				memory[i] = memory[i+1];
			}
			
			memory[memorySize - 1] = page;
		}
		
		pc++;
		
		if(pc >= 100) {
			pc = pc % 100;
		}
		
		System.out.println("PF Handle: Page" + faultPage + " is loaded from disk!");
		
		try {
			Thread.sleep(11000);
			isPageFault = false;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

	public synchronized void copyPage(int [] memory, int pageIndex) {
		if(copyMemoryTrack[pageIndex] == 0) {
			if(count < memorySize) {
				this.memory[count] = memory[pageIndex];
				copyMemoryTrack[pageIndex] = 1;
				count++;
			} else {
				for(int j=0; j<memorySize - 1; j++) {
					this.memory[j] = memory[j+1];
				}
				
				this.memory[memorySize - 1] = memory[pageIndex];
			}
			
			System.out.println("PF Handle: [PFThread] Page" + memory[pageIndex] + " is copied.");
			printMemory();
			
			try {
				Thread.sleep(8000);
				isPageFault = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public synchronized void copyPage(int page) {
		
			if(count < memorySize) {
				this.memory[count] = page;
				
			} else {
				for(int j=0; j<memorySize - 1; j++) {
					this.memory[j] = memory[j+1];
				}
				
				this.memory[memorySize - 1] = page;
			}
			
			count++;
			
			System.out.println("[Future CPThread] Page" + page + " is copied.");
			printMemory();
			
			try {
				wait(8000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
	}
	
	public synchronized void copy(int [] newMemory) {
		int i = 0;
		
		while(true) {
			if(isWait) {
				try {
					System.out.println("Page fault: CPThread(" + Thread.currentThread().getId() + ") is waiting!");
					
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				if(i >= newMemory.length || newMemory[i] == 0) {
					setIsMigration(false);
					break;
				} else {
					if(copyMemoryTrack[i] == 0) {
						if(count < memorySize ) {
							memory[count] = newMemory[i];
						} else {
							for(int j=0; j<memorySize - 1; j++) {
								memory[j] = memory[j+1];
							}
							
							memory[memorySize - 1] = newMemory[i];
						}
						
						
						System.out.println("[CPThread] Page" + newMemory[i] + " is copied.");
						printMemory();
						
						copyMemoryTrack[i] = 1;
						count++;
						i++;
						
						try {
							wait(8000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}