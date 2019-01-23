package loader;

import javax.ejb.ApplicationException;

@ApplicationException(rollback=true)
public class ObjectNotFoundException extends RuntimeException {

	String msg;
	public ObjectNotFoundException(String msg) {
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
