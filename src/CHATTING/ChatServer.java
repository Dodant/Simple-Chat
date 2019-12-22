package CHATTING;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {

	ServerSocket ss = null;
	Socket s = null;
	Vector<HandlingClient> client = new Vector<HandlingClient>();

	public static void main(String[] args) {
		new ChatServer(args);
	}

	ChatServer(String[] args) {
		try {
			ss = new ServerSocket(4000);
		} catch (IOException e) {
			System.out.println("Can't open ServerSocket");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("Chat Server가 시작되어 클라이언트를 기다립니다 ....");
		try {
			while (true) {
				s = ss.accept();
				HandlingClient h = new HandlingClient(s);
				client.addElement(h);
				System.out.println("새 손님 입장-" + s + " 전체 참여자 수 = " + client.size());
				h.start();
			}
		} catch (IOException e) {
		}
	}

	void broadcast(String msg) {
		for (int i = 0; i < client.size(); i++) {
			try {
				HandlingClient h = client.elementAt(i);
				h.dos.writeUTF(msg);
				;
				h.dos.flush();
				// System.out.println(msg + " will be broadcated to " + h);
			} catch (IOException e) {
				System.out.println("Broadcast error");
			}
		}
	}

	void deleteClient(HandlingClient h) {
		System.out.println("Client " + h + " will be deleted.");
		int i = client.indexOf(h); // 없으면 -1
		if (i >= 0) {
			client.removeElementAt(i);
			try {
				// Server의 스트림 및 소켓 닫기
				if (h.dis != null)
					h.dis.close();
				if (h.dos != null)
					h.dos.close();
				if (h.socket != null)
					h.socket.close();
			} catch (IOException e) {
				System.out.println("Client close error");
			}
			System.out.println("A client has been deleted. remained clint = " + client.size());
		} else {
			System.out.println("No such client in this server");
		}
	} // deleteClient

	class HandlingClient extends Thread {
		Socket socket = null;
		DataInputStream dis;
		DataOutputStream dos;

		HandlingClient(Socket s) {
			socket = s;
			try {
				dis = new DataInputStream(socket.getInputStream());
				dos = new DataOutputStream(socket.getOutputStream());

			} catch (IOException e) {
				System.out.println("Can't open streams!");

			}
		}

		public void run() {
			String msg;
			while (true) {
				try {
					msg = dis.readUTF();
				} catch (IOException e) {
					System.out.println("클라이언트로부터 메시지를 읽는 과정에서 에러 발생 (이 클라이언트 스레드는 종료됨)");
					return; // Thread 종료
				}
				int index = msg.indexOf("BYE");
				if (index >= 0) {
					deleteClient(this); // 채팅 탈퇴 처리
					broadcast(msg.substring(0, index) + "님이 나갔습니다");
					return;
				} else {
					broadcast(msg);// 읽어 들인 메시지를 모든 clients에게 방송
				}
				yield();
			}
		}
	} // end HandlingClient
} // end ChatServer
