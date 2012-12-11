package org.xmeta;


public class XMetaException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public XMetaException(){
        super();
    }

    public XMetaException(String message){
        super(message);
    }

    public XMetaException(String message, Throwable cause){
        super(message, cause);        
    }
    
    public XMetaException(Throwable cause){
        super(cause);        
    }
    
}
