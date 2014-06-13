public class CopyThread extends Thread {
	private VM vm;
	private int[] memory, pageQueue;
	private int pageIndex, pc;
	private Boolean isPageFault = false;
	private Boolean isFutureCopy = false;
	
	public CopyThread(VM vm, int[] memory) {
		this.vm = vm;
		this.memory = memory;
	}
	
	public CopyThread(VM vm, int[] memory, int pageIndex, Boolean isPageFault) {
		this.vm = vm;
		this.memory = memory;
		this.pageIndex = pageIndex; 
		this.isPageFault = isPageFault;
	}
	
	public CopyThread(VM vm, int[] pageQueue, int pc, Boolean isFutureCopy, Boolean isPageFault) {
		this.vm = vm;
		this.pageQueue = pageQueue;
		this.isPageFault = isPageFault;
		this.isFutureCopy = isFutureCopy;
		this.pc = pc;
	}
	
    public void run() {
    	if(isPageFault) {
    		vm.copyPage(memory, pageIndex);
    	} else if(isFutureCopy) {
    		for(int i=pc; i< pageQueue.length; i++) {
    			vm.copyPage(pageQueue[i]);
    		}
    	} else {
    		System.out.println("[CPThread] copy memory start!");
    		vm.copy(memory);
    		System.out.println("[CPThread] copy memory done!");
    	}
    }
}