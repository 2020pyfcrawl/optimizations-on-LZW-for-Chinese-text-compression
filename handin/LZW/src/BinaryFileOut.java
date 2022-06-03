import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Java does not support bit stream writer, so there we create a class to do this
 * @author pyf19
 *
 */
public final class BinaryFileOut {

    private BufferedOutputStream data_output = null;
    private String outFilePath = null;
    private FileOutputStream outputFile = null;
    private int buffer = 0;     // 8-bit buffer of bits to write out
    private int n = 0;          // number of bits remaining in buffer 


    public BinaryFileOut() { }
    
    /**
     * End the output stream
     * @return
     * @throws IOException
     */
    public boolean outputEnd() throws IOException {
    	
    	/* output the remaining bits */
    	clearBuffer();
    	data_output.flush();
		data_output.close();
		outputFile.close();
    	return true;   	
    }
    
    /**
     * set the output stream
     * @param outputFilePath
     * @return
     * @throws FileNotFoundException
     */
    public boolean setOutputStream(String outputFilePath) throws FileNotFoundException {
    	
    	if(data_output == null) {
    		this.outFilePath = outputFilePath;
    		outputFile = new FileOutputStream(outputFilePath);
        	data_output = new BufferedOutputStream(outputFile);
        	System.out.println("output file path:" + outputFilePath + " is determined, cannot be changed!");
        	return true;
    	}
    	System.out.println("output file path:" + outFilePath + " is determined, you cannot change it!");
    	return false;
    }
    
   /**
     * Write the specified bit to standard output.
     */
    public void writeBit(boolean bit) {
        
    	/* add bit to buffer */
        buffer <<= 1;
        if (bit) buffer |= 1;

        /* if buffer is full (8 bits), write out as a single byte */
        n++;
        if (n == 8) clearBuffer();
    } 

    /* clear the buffer */
    private void clearBuffer() {
        if (n == 0) return;
        if (n > 0) buffer <<= (8 - n);
        try {
        	data_output.write(buffer);
        	data_output.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        n = 0;
        buffer = 0;
    }
}