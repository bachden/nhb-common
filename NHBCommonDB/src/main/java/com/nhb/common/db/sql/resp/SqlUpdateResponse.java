package com.nhb.common.db.sql.resp;

public class SqlUpdateResponse {

	private boolean success = false;
	private Throwable exception;
	private int rowEffected;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable exception) {
		this.exception = exception;
	}

	public int getRowEffected() {
		return rowEffected;
	}

	public void setRowEffected(int rowEffected) {
		this.rowEffected = rowEffected;
	}
}
