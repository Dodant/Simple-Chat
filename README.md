# Simple Chat
Java Networking Project


## 개요
- 여러 사람이 참여할 수 있는 채팅은 하나의 서버 프로세스와 다수 클라이언트 프로세스로 구성한다. 
- 서버는 채팅에 참여하지 않으며, 클라이언트들은 메시지를 직접 주고받지 않고 서버의 중계를 받아 대화한다. 
  - 클라이언트가 다른 클라이언트에 보낼 메시지가 있으면 서버로 보내고 서버가 이를 모든 클라이언트에게 방송하는 방식을 사용한다.

## 서버
- 클라이언트와의 연결은 Vector에 저장하여 관리한다.
- ChatServer의 main() 메소드에서는 단순히 ChatServer 타입의 객체를 하나 만드는 작업만 진행하며 객체가 만들어지는 생성자에서 서버의 각종 초기화 작업이 진행된다. 
- 맨 먼저 ServerSocket 객체를 만들고 accept()를 실행한다. 한 클라이언트와의 통신은 스레드에게 맡긴다. 
- accept()가 리턴하면 소켓을 인자로 클라이언트와 통신을 담당할 HandlingClient라는 스레드 객체를 생성하여 start시킨다. 이 HandlingClient 타입의 객체를 client라는 Vector에 원소로 추가하고 client 벡터의 현재 크기를 화면에 출력한다. 이상의 작업은 다시 accept()를 호출하여 새로운 클라이언트 기다리기를 반복한다.

- 각 클라이언트와 통신을 담당하는 HandlingClient의 생성자에서는 인자로 받은 소켓으로부터 입력과 출력 스트림을 생성한다. 
- 클라이언트와의 통신을 위한 필터스트림으로는 입출력 모두 Data 스트림을 사용한다. 
- 스레드가 실행할 작업이 구현되어 있는 run()의 주된 내용은 클라이언트로부터 보내오는 자료를 읽어들이는 것이다
- 클라이언트로부터의 입력은 DataInputStream dis의 readUTF()로 메시지를 읽는다. 만약 상대방 클라이언트가 메시지를 보내지 않고 있으면 프로그램은 여기에서 기다리게 되는데 그렇더라도 이 스레드만 블록되는 것이고 채팅 프로그램 전체가 블록되지는 않는다.

- 만약 클라이언트가 채팅을 정상적으로 종료하지 않고 채팅을 탈퇴하면 readUTF() 실행 중에 IOException이 발생하는데 이 때 해당 클라이언트가 채팅을 탈퇴한 것을 가정하고 이를 처리하는 메소드 exitClient()를 호출한다.
- 만일 클라이언트가 “BYE”라는 메시지를 보내왔으면 정상적인 채팅 탈퇴를 처리하며 그 이외의 메시지는 모든 채팅 가입자들에게 broadcast() 라는 메소드를 이용하여 방송하는데 전송할 메시지를 broadcast() 메소드의 인자로 전달한다.

- 아래는 모든 클라이언트에게 메시지를 방송하는 broadcast() 메소드인데 먼저 현재 채팅 가입자 수를 Vector 클래스가 제공하는 size()로 알아낸 다음, client 벡터의 각 원소 즉 HandlingClient 스레드 객체를 가리키는 참조(아래 코드에서는 h)를 얻은 다음 각 스레드의 출력 스트림으로 메시지를 전송한다.
```
void broadcast(String msg) {
	int numberOfConnect = client.size();
	for(int i = 0; i < numberOfConnect; i++) {
		try {
			HandlingClient h = client.elementAt(i);
			h.dos.writeUTF(msg);;
			h.dos.flush();	
		}catch(IOException e){
			System.out.println("Broadcast error");
		}
	}
}
```

## 클라이언트 
- 클라이언트가 하는 일은 크게 2가지이다. 하나는 사용자의 메시지를 입력 받아서 서버로 전달하는 것과 서버에서 보내오는 채팅 메시지들을 화면에 보여 주는 것이다.
- 사용자의 입력 메시지 처리는 사용자가 엔터를 입력했을 때만 처리하기로 하고ActionEvent 처리 루틴에 포함시킨다. 
- 서버에서 보내오는 메시지 수신은 소켓을 계속 관찰해야 하는 작업이므로 readMessage라는 스레드를 만들어서 전담시킨다. 이 스레드가 할 일은 서버로부터 보내오는 메시지에 ‘\n’을 추가하여 TextArea에 쌓는 것이다.

- Connect 버튼과 연결된 ActionEvent 핸들러에는 connect-flag라는 플래그가 있는데 이 플래그의 목적은 이미 연결되어 있는데 상황에서 다시 connect 버튼이 눌리면 이를 무시하기 위함이다. 
- 송수신용 스트림으로는 Data Stream을 사용하였다.
- 사용자가 채팅을 탈퇴하느 방법은 Disconnect 버튼을 누르면 된다.




