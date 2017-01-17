package com.zju.hzsz.model;

/**
 * 接口返回基类
 * 
 * @author Robin
 * 
 */
public class BaseRes {
	protected String status;
	protected int code;
	protected String msg;

	public boolean isSuccess() {
		return code == 0;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [status=" + status + ", code=" + code + ", msg=" + msg + "]";
	}
}
