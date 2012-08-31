
/*
 * file:	LogManager.java
 * 
 * author: 	@xxleite
 * 
 * date: 	09-26-2011 -03 GMT
 * 
 * detais:	log facilities for appengine
 * 
 *	----------------------------------------------------------------------------
 * 	"THE BEER-WARE LICENSE" (Revision 42.1):
 * 	<xxleite@gmail.com> wrote this file. As long as you retain this notice you
 * 	can do whatever you want with this stuff. If we meet some day, and you think
 * 	this stuff is worth it, you can buy me a beer or more in return 
 *  ----------------------------------------------------------------------------
 */

package logger;

public class LogManager {
	
	public void logError( String logMessage ){ 
		
		String className = Thread.currentThread().getStackTrace()[2].getClassName();
		String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
		
		System.err.println("["+ className +"] "+ methodName +"\n" + logMessage); 
	}
	
	public void logInfo( String logMessage ){ 
		
		String className = Thread.currentThread().getStackTrace()[2].getClassName();
		String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
		
		System.out.println("["+ className +"] "+ methodName +"\n" + logMessage); 
	}
	
	public void logExc( Object exception ) { 
		
		Exception ex = (Exception) exception;
		String className = Thread.currentThread().getStackTrace()[2].getClassName();
		String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
		StackTraceElement[] exceptionStack = ex.getStackTrace();
		
		int i = 0;
		int len = exceptionStack.length;
		String smartStack = " ============ exception ============ \n";
		
		smartStack += "["+ ex.getStackTrace()[0].getClassName() +"] " + ex.getMessage() +"\n";
		
		for( i=1; i<len; ++i ){
			
			if( exceptionStack[i].getLineNumber()==-1 ){ continue; }
			
			smartStack += "["+ ex.getStackTrace()[i].getClassName() +"] "+ exceptionStack[i].getMethodName() +":"+ exceptionStack[i].getLineNumber() +"\n";
			
			if( exceptionStack[i].getClassName()==className && exceptionStack[i].getMethodName()==methodName ){ break; }
		}
		
		System.err.println( smartStack );
		
	}
	
}
