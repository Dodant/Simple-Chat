package CHATTING;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.Date;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class ChatClient extends JFrame implements Runnable {
	final int PORT = 4000;
	String HOST = "localhost";
	Socket s;
	DataInputStream dis;
	DataOutputStream dos;
	JButton connect, disconnect;
	JTextArea memo; // 출력창
	JTextField name; // 참가자 이름
	JTextField message; // 채팅 메시지 입력창
	JMenuBar mb;
	JMenu screen;
	boolean connect_flag = false; // 서버와 연결 상태

	public static void main(String[] args) {
		new ChatClient();
	}

	ChatClient() {
		JPanel panel = new JPanel();
		JLabel nameLabel = new JLabel("Chatterer name: ");
		name = new JTextField(12);
		connect = new JButton("Connect");
		disconnect = new JButton("Disconnect");
		connect.addActionListener(new CennectListener());
		disconnect.addActionListener(new DisconnectListener());
		disconnect.setForeground(Color.RED);
		panel.add(nameLabel);
		panel.add(name);
		panel.add(connect);
		panel.add(disconnect);

		memo = new JTextArea(); // 메시지 입출력 창
		memo.setEditable(false);
		DefaultCaret caret = (DefaultCaret)memo.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane memosc = new JScrollPane(memo);
		message = new JTextField();
		message.addActionListener(new SendListener());
		
		mb = new JMenuBar();
		screen = new JMenu("Screen");
		JMenuItem clear = new JMenuItem("Clear");
		clear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				memo.setText("");
			}
		});
		JMenuItem dm = new JMenuItem("Dark Mode");
		dm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				connect.setBackground(new Color(44, 49, 53));
				connect.setForeground(Color.WHITE);
				connect.setOpaque(true);
				connect.setBorderPainted(false);
				
				disconnect.setBackground(new Color(44, 49, 53));
				disconnect.setOpaque(true);
				disconnect.setBorderPainted(false);
				
				memo.setBackground(new Color(62, 70, 76));
				memo.setForeground(Color.WHITE);
				memo.setOpaque(true);
				
				panel.setBackground(new Color(22, 31, 34));
				panel.setOpaque(true);
				nameLabel.setForeground(Color.WHITE);
			}
		});
		screen.add(clear);
		screen.add(dm);
		mb.add(screen);
		setJMenuBar(mb);

		setLayout(new BorderLayout());
		add(panel, BorderLayout.NORTH);
		add(memosc, BorderLayout.CENTER);
		add(message, BorderLayout.SOUTH);
		
		setSize(500, 800);
		setVisible(true);
	}

	class CennectListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.out.println("서버와 연결을 시작합니다");
			// 참가자 이름을 제시하지 않았을 시 리턴 
			if (name.getText().equals("")) return;
			if (connect_flag == false) {
				try {
					s = new Socket(HOST, PORT);
					dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
					dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
					System.out.println("서버와 연결 완료");
				} catch (IOException ioe) {
					memo.append("Socket 생성 실패 \n");
					return;
				}
				memo.append(name.getText() + "의 Socket 연결 성공\n");
				// 통신할 스레드 생성 및 발진
				new Thread(ChatClient.this).start();
				connect_flag = true;// 연결로 표시

				// 자신의 접속 사실을 서버에 메시지로 전송
				try {
					dos.writeUTF(Timer.getTime() + " :: [" + name.getText() + "]님이 입장하였습니다 \n");
					dos.flush();
					setTitle(name.getText() + "님의 채팅방");
					name.setEditable(false);
				} catch (IOException ioe) {
					memo.append("Connect error\n");
				}

			} else {
				// 중복 연결 시도
				memo.append("이미 연결되어 있습니다.\n");
			}
		}
	} // end CennectListener
	//채팅 메시지를 읽어 TextArea에 갈무리

	public void run() {
		while (connect_flag) {
			String data = null;
			try {
				if (dis != null) data = dis.readUTF() + '\n';
			} catch (IOException e) {
				memo.append("read error: 서버로부터 메시지 읽기 실패 *******");
			}
			if (data != null) memo.append(data);
		}
		return;
	}

	//채팅 메시지를 서버로 보내는 리스너
	class SendListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.out.println(name.getText() + "가 서버로 메시지 보냄");
			try {
				dos.writeUTF(Timer.getTime() + " :: [" + name.getText() + "] - " + message.getText()); // 서버에 메시지 보냄
				dos.flush();
			} catch (IOException ioe) {
				memo.append("Message Sending Error\n");
			}
			message.setText("");// 입력 필드 비움
		}
	}

	//disconnect 버튼 처리 리스너
	class DisconnectListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.out.println("Discennecting start");
			try {
				dos.writeUTF(Timer.getTime() + " :: [" + name.getText() + "] - " + "BYE");
				dos.flush();
			} catch (IOException ioe) {}
			connect_flag = false;
			try {
				//Client 측 스트림 및 소켓 닫기
				dos.close();
				dis.close();
				s.close();
				System.exit(0);
			} catch (IOException ioe) {}
		}
	}
	
	static class Timer {
		public static String getTime() {
			StringBuffer time = new StringBuffer();
			Date now = new Date();
			int hrs = now.getHours();
			int min = now.getMinutes();
			
			if(hrs > 13) time.append(hrs - 12);
	 		else time.append(hrs);
	 		time.append(":");
			if(min < 10) time.append("0" + Integer.toString(min));
			else time.append(min);
			
			return time.toString();
		}
	}
}
