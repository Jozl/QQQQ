package db.beans;

//��Ҳ��֪���ò��÷��������
public class Group extends User{
//	private int userNumber;
	
	public Group(String groupAccount, String groupName) {
		super(groupAccount,null,groupName, null);
	}
	
	public Group() {
		super();
	}
	
	public void test() {
		System.out.println("java �̳� ���Բ���");
	}
}
