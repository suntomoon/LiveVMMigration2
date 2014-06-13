

public class Migration {
	private String pageQueue, memory, pc;
	
	public void runPostCopy(ServerThread sourceThread, ServerThread targetThread, String source, String vm, String target) throws Exception {
		targetThread.sendMessage(Message.buildMessage("setpolicy", "postcopy"));
		
		sourceThread.sendMessage(Message.buildMessage("stop", source, vm));
		sourceThread.receiveMessage();
		
		sourceThread.sendMessage(Message.buildInternalMessage("get", source, vm, "pq"));
		pageQueue = sourceThread.receiveMessage();
		
		sourceThread.sendMessage(Message.buildInternalMessage("get", source, vm, "me"));
		memory = sourceThread.receiveMessage();
		
		sourceThread.sendMessage(Message.buildInternalMessage("get", source, vm, "pc"));
		pc = sourceThread.receiveMessage();
		
		targetThread.sendMessage(Message.buildInternalMessage("create", target, vm, "migrate"));
		targetThread.receiveMessage();
		
		targetThread.sendMessage(Message.buildInternalMessage("set", target, vm, "pq", pageQueue));
		targetThread.sendMessage(Message.buildInternalMessage("set", target, vm, "pc", pc));
		targetThread.sendMessage(Message.buildInternalMessage("set", target, vm, "me", memory));
		
		targetThread.sendMessage(Message.buildInternalMessage("start", target, vm, "migrate"));
		targetThread.receiveMessage();
	}
	
	public void runLazyCopy(ServerThread sourceThread, ServerThread targetThread, String source, String vm, String target) throws Exception {
		targetThread.sendMessage(Message.buildMessage("setpolicy", "lazycopy"));
		
		sourceThread.sendMessage(Message.buildMessage("stop", source, vm));
		sourceThread.receiveMessage();
		
		sourceThread.sendMessage(Message.buildInternalMessage("get", source, vm, "pq"));
		pageQueue = sourceThread.receiveMessage();
		
		sourceThread.sendMessage(Message.buildInternalMessage("get", source, vm, "me"));
		memory = sourceThread.receiveMessage();
		
		sourceThread.sendMessage(Message.buildInternalMessage("get", source, vm, "pc"));
		pc = sourceThread.receiveMessage();
		
		targetThread.sendMessage(Message.buildInternalMessage("create", target, vm, "migrate"));
		targetThread.receiveMessage();
		
		targetThread.sendMessage(Message.buildInternalMessage("set", target, vm, "pq", pageQueue));
		targetThread.sendMessage(Message.buildInternalMessage("set", target, vm, "pc", pc));
		targetThread.sendMessage(Message.buildInternalMessage("set", target, vm, "me", memory));
		
		sourceThread.sendMessage(Message.buildMessage("start", source, vm));
		sourceThread.receiveMessage();
		
		targetThread.sendMessage(Message.buildInternalMessage("start", target, vm, "migrate"));
		targetThread.receiveMessage();
		
		sourceThread.sendMessage(Message.buildMessage("stop", source, vm));
		sourceThread.receiveMessage();
	}
}
