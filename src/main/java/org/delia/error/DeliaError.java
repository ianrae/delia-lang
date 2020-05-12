package org.delia.error;

import java.util.Date;

/**
 * An error in the Delia language. Includes syntax errors and runtime errors.
 * 
 * @author Ian Rae
 *
 */
public class DeliaError {
	private String id;
	private String msg;
	private Date timestamp = new Date();
	private String area; //descriptive name. eg. pipeline name
	private String arg1;
	private String arg2;
	private String arg3;
	private int lineNum;
	private int pos;

	public DeliaError(String id, String msg) {
		this.id = id;
		this.msg = msg;
	}
	
	public void setLineAndPos(int lineNum, int pos) {
		this.lineNum = lineNum;
		this.pos = pos;
	}
	
	@Override
	public String toString() {
		return String.format("%s: %s", id, msg);
	}
	public String getId() {
		return id;
	}
	public String getMsg() {
		return msg;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getArg1() {
		return arg1;
	}
	public void setArg1(String arg1) {
		this.arg1 = arg1;
	}
	public String getArg2() {
		return arg2;
	}
	public void setArg2(String arg2) {
		this.arg2 = arg2;
	}
	public String getArg3() {
		return arg3;
	}
	public void setArg3(String arg3) {
		this.arg3 = arg3;
	}
	public void setMessage(String msg) {
		this.msg = msg;
	}

	public int getLineNum() {
		return lineNum;
	}

	public int getPos() {
		return pos;
	}
}