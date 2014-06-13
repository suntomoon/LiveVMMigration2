
public class Message {
	public static String buildMessage(String opt, String param) {
		return String.format(opt + " -p %s", param);
	}
	
	public static String buildMessage(String opt, String server, String vm) {
		return String.format(opt + " -s %s -v %s", server, vm);
	}
	
	public static String buildMessage(String opt, String server, String vm, String target) {
		return String.format(opt + " -s %s -v %s -t %s", server, vm, target);
	}
	
	public static String buildInternalMessage(String opt, String server, String vm, String param1) {
		return String.format(opt + " -s %s -v %s -p %s", server, vm, param1);
	}
	
	public static String buildInternalMessage(String opt, String server, String vm, String param1, String param2) {
		return String.format(opt + " -s %s -v %s -p %s -d %s", server, vm, param1, param2);
	}
}
