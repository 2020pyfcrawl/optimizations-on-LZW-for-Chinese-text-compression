import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 */

/**
 * Used for dictionary of encoding 
 * 
 * @author pyf19
 *
 */
public class LZW_dictionary {
	
	/* encoding table */
	private HashMap<String, Integer> dictionary = new HashMap<String, Integer>();
	
	int max_size_bit = 12; 	// the max bit length of the dictionary
	int current_size = 0;	// current number of words inserted dictionary
	int current_size_bit = 0;	//current number of bits used to represent all indexes, equals log2(current_size)
	int output_code_length = 12;//fixed output code length for each index in traditional LZW
	
	/* used for write index (encoding stream) out in bits stream */
	private BinaryFileOut binary_output = new BinaryFileOut(); 
	
	
	/* set output file of binary output */
	public boolean setOutputFile(String outputFilePath) throws FileNotFoundException {
		return binary_output.setOutputStream(outputFilePath);
	}
	
	/**
	 * flush the output and print out current_size
	 * @return
	 */
	public boolean outputDone() {
		 try {
			 System.out.println("dictionary size = " + current_size + 
					 " the full is "+ isFull());
			return binary_output.outputEnd();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * write out the binary form of the index value
	 * according to the output bit length
	 * @param binary_bits
	 * @return
	 */
	public boolean bitsOut(String binary_bits) {
		
		int bits_length = binary_bits.length();
		/* get output length for this index */
		int output_length = setProperLength(bits_length);
		int interval = output_length - bits_length;
		for(int position = 0; position < output_length; position++) {
			if(interval > position ) {
				/* zero fill at the beginning */
				binary_output.writeBit(false);
			}
			else {
				char c = binary_bits.charAt(position - interval);
				if(c == '0') {
					/* write 0 */
					binary_output.writeBit(false);
				}else {
					/* write 1 */
					binary_output.writeBit(true);
				}
			}
		}		
		return true;		
	}
	
	public boolean bitsOut_Opt(String binary_bits) {
		
		int bits_length = binary_bits.length();
		/* get output length for this index */
		int []length_opt = new int[1];
		int output_FirstChar = setProperLength_Opt(bits_length,length_opt);
		int output_length = length_opt[0];
		int interval = output_length - bits_length;
		if(output_FirstChar == -1) {
			//do nothing
		}
		else if(output_FirstChar == 0) {
			binary_output.writeBit(false);
			interval -= 1;
			output_length -= 1;
		}
		else if(output_FirstChar == 1) {
			binary_output.writeBit(true);
			interval -= 1;
			output_length -= 1;
		}
		for(int position = 0; position < output_length; position++) {
			if(interval > position ) {
				/* zero fill at the beginning */
				binary_output.writeBit(false);
			}
			else {
				char c = binary_bits.charAt(position - interval);
				if(c == '0') {
					/* write 0 */
					binary_output.writeBit(false);
				}else {
					/* write 1 */
					binary_output.writeBit(true);
				}
			}
		}		
		return true;		
	}
	


	/**
	 * @param bits_length
	 * @return
	 */
	private int setProperLength_Opt(int bits_length,int []length) {
		// TODO Auto-generated method stub
		if(current_size_bit < 14 ) {
			length[0] = current_size_bit;
			return -1;
		}
		else if (current_size_bit == 14) {
			if(bits_length <= 12) {
				length[0] = 13;
				return 0;
			}
			else {
				length[0] = 15;
				return 1;
			}
		}
		else if(current_size_bit < 18) {
			if(bits_length <= current_size_bit - 3) {
				length[0] = current_size_bit - 2;
				return 0;
			}
			else {
				length[0] = current_size_bit + 1;
				return 1;
			}
		}
		else if(current_size_bit < 20) {
			if(bits_length <= current_size_bit - 4) {
				length[0] = current_size_bit - 3;
				return 0;
			}
			else {
				length[0] = current_size_bit + 1;
				return 1;
			}
		}
		
		return -1;
	}

	/**
	 * the parameter can be used to judge the exact output length in the future
	 * @param bits_length
	 * @return
	 */
	private int setProperLength(int bits_length) {
		
		/* fixed length output --- traditional */
		//return output_code_length;
		
		/* variable length output */
		return current_size_bit;
	}
	
	public boolean outputCode(String prefix) {
		return outputCode(prefix,0);
	}
	
	/**
	 * output index codestream according the prefix
	 * @param prefix
	 * @return
	 */
	public boolean outputCode(String prefix, int LZW_CH_NUM) {
		Integer number = getPrefixIndex(prefix);
		/* convert the number to a string, which is easier for bit write */
		String binary_bits = Integer.toBinaryString(number);
		if(binary_bits.length() > output_code_length) {
			System.out.println("Code length is longer than output_code_length, cannot output: " + prefix);
		}
		/* bitsOut is called */
		if(LZW_CH_NUM == 6) {
			if (bitsOut_Opt(binary_bits))
				return true;
			else 
				return false;
		}
		else {
			if (bitsOut(binary_bits))
				return true;
			else 
				return false;
		}
		
	}
	
	/**
	 *  set output length 
	 *  usually connected with fixed length output
	 */
	public void setOutputCodeLength(int length) {
		if( length > 0) {
			output_code_length = length;
		}
			
	}
	
	/* find if this prefix exists in the dictionary */
	public Integer getPrefixIndex(String prefix) {
		Integer value = dictionary.get(prefix);
		if( value == null) {
			return -1;
		}else {		
			return value;
		}		
	}
	
	/* set the max size of the dictionary */
	public void setDicSize(int size_bit) {
		if(size_bit > 9 ) {
			max_size_bit = size_bit;
			output_code_length = max_size_bit;
		}
		
	}
	
	/* initialize the dictionary according to some prefilling methods */
	public void preFillDic() {
		
		preFillDic_EN();
	}
	
	/* prifill the 0-255 ASCII code sets */
	public void preFillDic_EN() {
		
		for (int i = 0; i < 256; i++) {
			char ch = (char) i;
			String st = ch + "";
			dictionary.put(st, i);
		}
		updateSize();
		System.out.println("填充完成，字典大小现在为" + dictionary.size());
	}
	
	/* used for test of the example in the book */
	public void preFillDic_ABC() {

		dictionary.put("A", 0);
		dictionary.put("B", 1);
		dictionary.put("C", 2);
		updateSize();
		System.out.println("填充完成，字典大小现在为" + dictionary.size());
	}
	
	/* preserved Chinese prefill */
	public void preFillDic_CN_LEVEL1() throws UnsupportedEncodingException {
		//
		int size = current_size;
		byte[] by = new byte[2];
		for (int b1 = 16 ; b1 < 56; b1++) {
          by[0] = (byte) (b1 + 0xA0);
          for (int b2 = 0xA1; b2 < 0XFF; b2++) {
              if( b1 == 55 && b2 >= 0xFA) {
            	  break;
              }
        	  by[1] = (byte) b2;
              String str = new String(by, "GB2312");
              if(dictionary.get(str) == null) {
					dictionary.put(str,size);
					size++;
				}
          }
		}
		updateSize();		
	}
	
	public void preFillDic_CN_LEVEL2() throws UnsupportedEncodingException {
		//
		int size = current_size;
		byte[] by = new byte[2];
		for (int b1 = 56 ; b1 < 88; b1++) {
          by[0] = (byte) (b1 + 0xA0);
          for (int b2 = 0xA1; b2 < 0XFF; b2++) {
        	  by[1] = (byte) b2;
              String str = new String(by, "GB2312");
              if(dictionary.get(str) == null) {
					dictionary.put(str,size);
					size++;
				}
          }
		}
		updateSize();		
	}
	
	public void preFillDic_CN_CHARAREA13() throws UnsupportedEncodingException {
		//
		int size = current_size;
		byte[] by = new byte[2];
		for (int b1 = 1 ; b1 < 4; b1++) {
			if(b1 ==2) {
				continue;
			}
			by[0] = (byte) (b1 + 0xA0);
			for (int b2 = 0xA1; b2 < 0XFF; b2++) {
				by[1] = (byte) b2;
				String str = new String(by, "GB2312");
				if(dictionary.get(str) == null) {
					dictionary.put(str,size);
					size++;
				}
			}
		}
		if(dictionary.get("—") == null) {
			dictionary.put("—",size);
			size++;
		}
		updateSize();		
	}
	
	/**
	 * all no-existed char turned to GB2312 String will be [-3,-1]
	 * @throws UnsupportedEncodingException
	 */
	public void preFillDic_CN_CHARAREA_ALL() throws UnsupportedEncodingException {
		//
		int size = current_size;
		byte[] by = new byte[2];
		for (int b1 = 1 ; b1 < 10; b1++) {
			by[0] = (byte) (b1 + 0xA0);
			for (int b2 = 0xA1; b2 < 0XFF; b2++) {
				by[1] = (byte) b2;
				String str = new String(by, "GB2312");
				if(dictionary.get(str) == null) {
					dictionary.put(str,size);
					size++;
				}
			}
		}
		//String dash = 
		if(dictionary.get("—") == null) {
			dictionary.put("—",size);
			size++;
		}
		updateSize();		
	}
	
	public int getSize() {
		return current_size;
	}
	
	/* adapt current size of dictionary and the number of bits needed to represent */
	private void updateSize() {
		current_size = dictionary.size(); 
		current_size_bit = (int) Math.ceil(log2(current_size));
	}
	
	public double log2(double N) {
	    return Math.log(N)/Math.log(2);
	}
	
	/* insert the word to the dictionary */
	public boolean putWord(String word) {
		if(isFull()) {
			//do nothing
			//System.out.println("dictionary is full!");
			return false;
		}
		else {
			dictionary.put(word,current_size);
			updateSize();
			return true;
		}
		
	}
	
	/* test if the dictionary is full */
	public boolean isFull() {
		return (current_size > Math.pow(2,max_size_bit) - 1);
	}
	
	/* clear the dictionary */
	public boolean flush() {
		dictionary.clear();
		return true;
	}
	
}
