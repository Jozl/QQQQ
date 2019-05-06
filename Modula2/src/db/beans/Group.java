package db.beans;

//我也不知道该不该放这个包里
public class Group extends User{
//	private int userNumber;
	
	public Group(String groupAccount, String groupName) {
		super(groupAccount,null,groupName, null);
	}
	
	public Group() {
		super();
	}
	
	public void test() {
		System.out.println("java 继承 特性测试");
	}
}
