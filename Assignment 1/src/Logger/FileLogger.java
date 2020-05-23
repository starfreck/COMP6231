package Logger;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class FileLogger {
    
	String path;
	String fileName;
	
	
	public FileLogger(String path, String fileName){
		this.path = path;
		this.fileName = fileName;
	}
	
    public boolean write(String message) {  

        Logger logger = Logger.getLogger("MyLog");  
        FileHandler fh;

        //To remove the console handler, use
        logger.setUseParentHandlers(false);
        //since the ConsoleHandler is registered with the parent logger from which all the loggers derive.
        
        try {  
    
            // This block configure the logger with handler and formatter
        	File file = new File(path+fileName);
        	file.getParentFile().mkdirs();
        	file.createNewFile(); // if file already exists will do nothing 
        	
            fh = new FileHandler(path+fileName, true);  
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();  
            fh.setFormatter(formatter);  
    
            // the following statement is used to log any messages  
            logger.info(message+"\n"); 
//            logger.severe("severe message");
//            logger.warning("warning message");
//            logger.info("info message");
//            logger.config("config message");
//            logger.fine("fine message");
//            logger.finer("finer message");
//            logger.finest("finest message");
    
        } catch (SecurityException e) {  
            e.printStackTrace();
            return false;  
        } catch (IOException e) {  
            e.printStackTrace();
            return false;  
        }
        return true;  
    }        
}