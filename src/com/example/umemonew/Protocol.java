package com.example.umemonew;

public class Protocol {
	private static final String head_of_username="username:";
	private static final String head_of_sendmsgs="sendmsgs:";
	private static final String head_of_addition="addition:";

	//differ the message
	public int differMessage(String message){
		String head=message.substring(0,9);
		if(head.equals(head_of_sendmsgs))
			return 1;
		else if(head.equals(head_of_addition))
			return 2;
		else return 0;
	}

	public String generateUsername(String username){
		String message=head_of_username;
		message+=toString(username.length());
		message+=username;
		return message;
	}

	//send a verify message to a friend...
	public String generateAddition(String username,String friendName,String headImage,String nickName,String signature,char ch){
		String message=head_of_addition;

		//from whom
		message+=toString(username.length());
		message+=username;

		//to whom
		message+=toString(friendName.length());
		message+=friendName;

		//the code of the head_image
		message+=headImage;

		//nickname
		message+=toString(nickName.length());
		message+=nickName;
		
		//signature
		message+=signature;

		message+=ch;		//'A' means a verify message
		return message;
	}

	//the message sent to the friend...
	public String generateMessages(String username,String friendName,String title,String date,String time,String note){
		String message=head_of_sendmsgs;

		//from whom
		message+=toString(username.length());
		message+=username;

		//to whom
		message+=toString(friendName.length());
		message+=friendName;

		//title
		message+=toString(title.length());
		message+=title;

		//date and time	(16 characters: 2014-12-12|08:00)
		message+=date+"|"+time;

		//note
		message+=toString(note.length());
		message+=note;

		return message;
	}

	public static String username_of_message = null;
	public static String friend_name = null;
	public static String title = null;
	public static String date = null;
	public static String time = null;
	public static String note = null;


	public void handleMessageDetail(String message)			//for sending messages
	{
		int length=0;

		String usernameLength=message.substring(0,4);		//get username_of_message
		int length1=Integer.parseInt(usernameLength);
		username_of_message=message.substring(4,4+length1);		

		String friendNameLength=message.substring(length1+4,length1+8);	//get friend_name
		int length2=Integer.parseInt(friendNameLength);
		friend_name=message.substring(length1+8,length1+length2+8);

		length=length1+length2+8;										//get title
		length1=Integer.parseInt(message.substring(length,length+4));	
		title=message.substring(length+4,length+length1+4);

		length=length+length1+4;									//get date and time
		date=message.substring(length,length+10);
		time=message.substring(length+11,length+16);

		length=length+16;
		length1=Integer.parseInt(message.substring(length,length+4));	//get note
		note=message.substring(length+4,length+length1+4);
	}

	public static String signature = null;
	public static String head_number = null;
	public static String nick_name = null;

	public void handleAdditionDetail(String message)
	{
		int length=0;

		String usernameLength=message.substring(0,4);		//get username_of_message
		int length1=Integer.parseInt(usernameLength);
		username_of_message=message.substring(4,4+length1);		

		String friendNameLength=message.substring(length1+4,length1+8);	//get friend_name
		int length2=Integer.parseInt(friendNameLength);
		friend_name=message.substring(length1+8,length1+length2+8);

		length=length1+length2+8;										//get head code
		head_number=message.substring(length,length+2);
		
		length=length+2;												//get nickname
		length1=Integer.parseInt(message.substring(length,length+4));	
		nick_name=message.substring(length+4,length+length1+4);
		
		length=length+length1+4;
		signature=message.substring(length,message.length()-1);
	}
	
	public String swapMessage(String message)
	{
		String usernameLength=message.substring(0,4);		//get username_of_message
		int length1=Integer.parseInt(usernameLength);
		String tmp_username=usernameLength+message.substring(4,4+length1);
		
		String friendNameLength=message.substring(length1+4,length1+8);	//get friend_name
		int length2=Integer.parseInt(friendNameLength);
		String tmp_friend=friendNameLength+message.substring(length1+8,length1+length2+8);
		
		int length=length1+length2+8;
		String new_message=tmp_friend+tmp_username+message.substring(length);
		return new_message;
	}

	//agree to add friends
	public String agreeToAdd(String message){
		int length=message.length();
		StringBuffer sb = new StringBuffer(message);
		sb.setCharAt(length-1,'Y');
		return sb.toString();
	}

	//refuse to add friends
	public String refuseToAdd(String message){
		int length=message.length();
		StringBuffer sb = new StringBuffer(message);
		sb.setCharAt(length-1,'N');
		return sb.toString();
	}

	private String toString(int len){
		String number=Integer.toString(len);
		int length=number.length();
		for(int i=length;i<4;i++)
			number="0"+number;
		return number;
	}
}
