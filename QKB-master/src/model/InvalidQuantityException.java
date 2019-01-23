package model;
import javax.ejb.ApplicationException;

@ApplicationException(rollback=true)
public class InvalidQuantityException extends RuntimeException {

	String msg;
	public InvalidQuantityException(String msg) {
		this.msg= msg;
	}

	@Override
	public String getMessage(){
		return msg;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}