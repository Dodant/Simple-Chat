package CHATTING;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class PrototypeClient extends JFrame implements Runnable {
	final int PORT = 4000;
	String HOST = "localhost";
	Socket s;
	DataInputStream dis;
	DataOutputStream dos;
	JButton connect, disconnect;
	JTextArea memo; // 출력창
	JTextField name; // 참가자 이름
	JTextField message; // 채팅 메시지 입력창
	boolean connect_flag = false; // 서버와 연결 상태

	public static void main(String[] args) {
		new PrototypeClient();
	}

	PrototypeClient() {
		JPanel panel = new JPanel();
		JLabel nameLabel = new JLabel("Chatterer name: ");
		name = new JTextField(12);
		connect = new JButton("Connect");
		connect.addActionListener(new CennectListener());
		disconnect = new JButton("DisConnect");
		disconnect.addActionListener(new DisconnectListener());
		panel.add(nameLabel);
		panel.add(name);
		panel.add(connect);
		panel.add(disconnect);

		memo = new JTextArea(); // 메시지 입출력 창
		memo.setEditable(false);
		message = new JTextField();
		message.addActionListener(new SendListener());

		setLayout(new BorderLayout());
		add(panel, BorderLayout.NORTH);
		add(memo, BorderLayout.CENTER);
		add(message, BorderLayout.SOUTH);

		setSize(500, 800);
		setVisible(true);
	}

	class CennectListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.out.println("서버와 연결을 시작합니다");
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
				new Thread(PrototypeClient.this).start();
				connect_flag = true;// 연결로 표시

				// 자신의 접속 사실을 서버에 메시지로 전송
				try {
					dos.writeUTF("[" + name.getText() + "]" + " 안녕하세요?\n");
					dos.flush();

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
				if (dis != null)
					data = dis.readUTF() + '\n';
			} catch (IOException e) {
				memo.append("read error: 서버로부터 메시지 읽기 실패 *******");
			}
			if (data != null)
				memo.append(data);
		}
		return;
	}

	//채팅 메시지를 서버로 보내는 리스너
	class SendListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.out.println(name.getText() + "가 서버로 메시지 보냄");
			try {
				dos.writeUTF("[" + name.getText() + "]" + message.getText()); // 서버에 메시지 보냄
				dos.flush();
			} catch (IOException ioe) {
				memo.append("Message Sending error\n");
			}
			message.setText("");// 입력 필드 비움
		}
	}

	//disconnect 버튼 처리 리스너
	class DisconnectListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.out.println("DisCennecting start");
			try {
				dos.writeUTF("[" + name.getText() + "]" + "BYE");
				dos.flush();
			} catch (IOException ioe) {
			}
			connect_flag = false;
			try {
				//Client 측 스트림 및 소켓 닫기
				dos.close();
				dis.close();
				s.close();
				System.exit(0);
			} catch (IOException ioe) {
			}
		}
	}
}
