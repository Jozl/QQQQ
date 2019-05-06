package com;

import java.sql.Timestamp;
import java.util.Date;

public class Message <T>{
	public final static String M_LOGIN = "M_LOGIN";
	public final static String M_LOGIN_ACCEPT = "M_LOGIN_ACCEPT";
	public final static String M_LOGIN_FAIL = "M_LOGIN_FAIL";
	public final static String M_LOGIN_REPEAT = "M_LOGIN_REPEAT";
	
	public final static String M_REGIST = "M_REGIST";
	public final static String M_REGIST_ACCEPT = "M_REGIST_ACCEPT";
	public final static String M_REGIST_FAIL = "M_REGIST_FAIL";
	
	public final static String M_FILE_ASK = "M_FILE_ASK";
	public final static String M_FILE_ACCEPT = "M_FILE_ACCEPT";
	public final static String M_FILE_ADDRESS = "M_FILE_ADDRESS";
	public final static String M_FILE_READY = "M_FILE_READY";
	public final static String M_FILE_REJECT = "M_FILE_REJECT";
	
	public final static String M_IMAGE = "M_IMAGE";
	public final static String M_IMAGE_REQUEST = "M_IMAGE_REQUEST";
	public final static String M_IMAGE_UPDATE = "M_IMAGE_UPDATE";
	public final static String M_IMAGE_UPDATE_SUCCEED = "M_IMAGE_UPDATE_SUCCEED";
	
	public final static String M_USER_BREATHING = "M_USER_BREATHING";
	
	public final static String M_USER_LOGIN = "M_USER_LOGIN";
	public final static String M_USER_LOGOUT = "M_USER_LOGOUT";
	public final static String M_USER_TO_USER = "M_USER_TO_USER";
	public final static String M_USER_KICK = "M_USER_KICK";
	
	public final static String M_ONLINE = "M_ONLINE";
	public final static String M_ONLINE_LIST = "M_ONLINE_LIST";
	public final static String M_ONLINE_FRIENDS = "M_ONLINE_FRIENDS";

	private String code;
	private String sender;
	private String receiver;
	private Timestamp timestamp;
	private T payload;

	@SuppressWarnings("unchecked")
	private Message(messageBuilder<?> builder) {
		this.code = builder.code;
		this.sender = builder.sender;
		this.receiver = builder.receiver;
		this.payload = (T) builder.payload;
		timestamp = new Timestamp(new Date().getTime());  
	}

	public static class messageBuilder <T> {
		private String code = "";
		private String sender = null;
		private String receiver = null;
		private T payload = null;

		public messageBuilder<T> Code(String code) {
			this.code = code;
			return this;
		}

		public messageBuilder<T> Sender(String sender) {
			this.sender = sender;
			return this;
		}

		public messageBuilder<T> Receiver(String receiver) {
			this.receiver = receiver;
			return this;
		}

		public messageBuilder<T> Payload(T payload) {
			this.payload = payload;
			return this;
		}

		public Message<T> build() {
			return new Message<T>(this);
		}
	}

	public String getCode() {
		return code;
	}

	public Message<T> setCode(String code) {
		this.code = code;
		return this;
	}

	public String getSender() {
		return sender;
	}

	public Message<T> setSender(String sender) {
		this.sender = sender;
		return this;
	}

	public String getReceiver() {
		return receiver;
	}

	public Message<T> setReceiver(String receiver) {
		this.receiver = receiver;
		return this;
	}

	public T getPayload() {
		return payload;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString() {
		return "Message [code=" + code + ", sender=" + sender + ", receiver=" + receiver + ", time="+ timestamp +" payload=" + payload + "]";
	}
}