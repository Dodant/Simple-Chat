package CHATTING;

import java.util.Date;

public class SimpleTest {

	public static void main(String[] args) {
		Date now = new Date();
		StringBuffer time = new StringBuffer();
		int hrs = now.getHours();
		int min = now.getMinutes();
		
		if(hrs > 13) time.append(hrs - 12);
 		else time.append(hrs);
		
 		time.append(":");
 		
		if(min < 10) time.append("0" + Integer.toString(min));
		else time.append(min);
		
		System.out.println(time);
	}
}
