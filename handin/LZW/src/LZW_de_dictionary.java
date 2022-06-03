import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;



/**
 * Used for dictionary of index-decoding
 * 
 * @author pyf19
 *
 */
public class LZW_de_dictionary {
	
	/* decoding table */
	private HashMap<Integer,String > de_dictionary = new HashMap<Integer,String >();
	private HashMap<String, Integer> dictionary = new HashMap<String, Integer>();
	
	int max_size_bit = 12;	// the max bit length of the dictionary
	int current_size = 0;	// current number of words inserted dictionary
	int decode_size_bit = 0;	//current number of bits used to represent all indexes, equals log2(current_size)
	int output_code_length = 12;//fixed output code length for each index in traditional LZW
	int decode_dic_size = 0;	//current number of words inserted dictionary while decoding index
	
	/* in this part, we first decode all the index from codestream, it is not suitable 
	 * if we want to flush the dictionary and rebuild the dictionary
	 * So I think it is not perfect, my first thought is to separate 
	 * decoding index from decoding plain text and it is proved to be 
	 * not the good choice, but I can still use it if I do not rebuild the dictionary
	 */

	/**
	 * Use codeStream 
	 * @param codeStream
	 * @return
	 */
	public ArrayList<Integer> AdaptdecodeStringtoIndex(byte[] codeStream) {
		
		/* store code index */
		ArrayList<Integer> numlist = new ArrayList<>();
		
		/* insert 256 numbers of value as EN dictionary */
		initialize_De_Size_EN(current_size);
				
		int length = codeStream.length;
		int value = 0;	// each index value decoded
		byte b;	// each byte read
		int index;
		/* left offset of the byte read */
		int left_offset = 8;	
		/* the length of the next index to decode */
		int output_length = decode_size_bit;	
		/* the left length of this index to be decode entirely */
		int left_length = output_length;
		b = (byte) ((codeStream[0]) & 0xFF);
		
		for(index = 0; index < length ; ) {
			/* each time decode an index, it will put a word to the dictionary */
			if( index != 0) {
				updata_De_Size(1);
			}
			
			output_length = decode_size_bit;
			/* start a new word */
			left_length = output_length;
			/* initialize the value */
			value = 0;
			
			while(left_length >= left_offset) {
				/* all remaining part of this byte is used */
				left_length -= left_offset;
				value += (b & ((int)Math.pow(2, left_offset) - 1)) << left_length;
				
				/* read the next byte */
				index++;
				if(index < length) {
					b = (byte) ((codeStream[index]) & 0xFF);
					left_offset = 8;
				}				
			}
			if(left_length > 0) {
				/* this index still needs some bits */
				value += ((b& 0xFF) >> (left_offset - left_length)) & ((int)Math.pow(2, left_offset) - 1) ;
				left_offset -= left_length;
			}
			/* add this value (index) to the list */
			numlist.add(value);
			
			
			/* check if there is no more index to decode */
			if(left_offset + (length-index-1) * 8 < decode_size_bit) {
				break;
			}
		}
		/* after the last one, it will not insert, according to the last output of the encoding part */
//		if()
//		updata_De_Size(-1);
		/* give a hint */
		System.out.println("dictionary size = " + decode_dic_size  );
		return numlist;
	}
	
	
	
	/**
	 * Use codeStream in LZW_CH6 when the code word is re-coded
	 * @param codeStream
	 * @return
	 */
	public ArrayList<Integer> AdaptdecodeStringtoIndex_Opt(byte[] codeStream) {
		
		/* store code index */
		ArrayList<Integer> numlist = new ArrayList<>();
		
		/* insert 256 numbers of value as EN dictionary */
		initialize_De_Size_EN(current_size);
				
		int length = codeStream.length;
		int value = 0;	// each index value decoded
		byte b;	// each byte read
		int index;
		/* left offset of the byte read */
		int left_offset = 8;	
		/* the length of the next index to decode */
		int output_length = decode_size_bit;
		boolean first_char = false;
		//需要知道当前位置是1还是0，以及当前的size_bit，得到编码的长度
		/* the left length of this index to be decode entirely */
		int left_length = output_length;
		b = (byte) ((codeStream[0]) & 0xFF);
		
		for(index = 0; index < length ; ) {
			/* each time decode an index, it will put a word to the dictionary */
			if( index != 0) {
				updata_De_Size(1);
			}
			first_char = ((b >> (left_offset - 1)) & 0x1) == 1;//判断首位是否为1
			//left_offset -= 1;
			int []length_opt = new int[1];
			int output_FirstChar = getLength_Opt(first_char,length_opt);
			output_length = length_opt[0];
			/* start a new word */
			left_length = output_length;
			left_offset -= output_FirstChar;
			/* initialize the value */
			value = 0;
			
			while(left_length >= left_offset) {
				/* all remaining part of this byte is used */
				left_length -= left_offset;
				value += (b & ((int)Math.pow(2, left_offset) - 1)) << left_length;
				
				/* read the next byte */
				index++;
				if(index < length) {
					b = (byte) ((codeStream[index]) & 0xFF);
					left_offset = 8;
				}				
			}
			if(left_length > 0) {
				/* this index still needs some bits */
				value += ((b& 0xFF) >> (left_offset - left_length)) & ((int)Math.pow(2, left_offset) - 1) ;
				left_offset -= left_length;
			}
			/* add this value (index) to the list */
			numlist.add(value);
			
			
			/* check if there is no more index to decode */
			if(left_offset + (length-index-1) * 8 < decode_size_bit) {
				break;
			}
		}
		
		/* give a hint */
		System.out.println("dictionary size = " + decode_dic_size  );
		return numlist;
	}
	
	/**
	 * used in code word recoding to get output length 
	 * @param first_char
	 * @param length_opt 
	 * @return
	 */
	private int getLength_Opt(boolean first_char, int[] length) {
		// TODO Auto-generated method stub
		if(decode_size_bit < 14 ) {
			length[0] = decode_size_bit;
			return 0;
		}
		else if (decode_size_bit == 14) {
			if(first_char) {
				length[0] = 14;
				return 1;
			}
			else {
				length[0] = 12;
				return 1;
			}
		}
		else if(decode_size_bit < 18) {
			if(first_char) {
				length[0] = decode_size_bit;
				return 1;
			}
			else {
				length[0] = decode_size_bit - 3;
				return 1;
			}
		}
		else if(decode_size_bit < 20) {
			if(first_char) {
				length[0] = decode_size_bit;
				return 1;
			}
			else {
				length[0] = decode_size_bit - 4;
				return 1;
			}
		}
		return 0;
	}




	
	/* get the String according to the index */
	public String getEntry(int entry_index) {
		String entry = de_dictionary.get(entry_index);
		if( entry == null) {
			return null;
		}else {		
			return entry;
		}
	}
	
	/**
	 * the parameter can be used to judge the exact output length in the future
	 * @param bits_length
	 * @return
	 */
	private int setProperLength(int bits_length) {
		return output_code_length;
	}
	
	/**
	 *  set output length 
	 *  usually connected with fixed length output
	 */
	public void setOutputCodeLength(int length) {
		if( length > 0)
			output_code_length = length;
		
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
			de_dictionary.put(i, st);
			dictionary.put(st, i);
		}
		updateSize();
		System.out.println("填充完成，字典大小现在为" + de_dictionary.size());
	}
	
	/* preserved Chinese prefill */
	public void preFillDic_CN1() {
		//
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
					de_dictionary.put(size,str);
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
					de_dictionary.put(size,str);
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
					de_dictionary.put(size,str);
					size++;
				}
			}
		}
		if(dictionary.get("—") == null) {
			de_dictionary.put(size,"—");
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
					de_dictionary.put(size,str);
					size++;
				}
				
			}
		}
		if(dictionary.get("—") == null) {
			de_dictionary.put(size,"—");
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
		current_size = de_dictionary.size(); 
		//decode_size_bit = (int) Math.ceil(log2(current_size));
	}
	
	/**
	 * update the index-decode dictionary size
	 * @param value
	 * @return
	 */
	private boolean updata_De_Size(int value) {
		if((decode_dic_size > Math.pow(2,max_size_bit) - value)) return false;
		decode_dic_size += value;
		decode_size_bit = (int) Math.ceil(log2(decode_dic_size));
		return true;
	}
	
	/* initialize the index-decode dictionary size */
	private void initialize_De_Size_EN(int value) {
		decode_dic_size += value;
		decode_size_bit = (int) Math.ceil(log2(decode_dic_size));
	}
	
	//3755
	private void initialize_De_Size_CN_LEVEL1(int value) {
		decode_dic_size += value;
		decode_size_bit = (int) Math.ceil(log2(decode_dic_size));
	}
	
	//3008
	private void initialize_De_Size_CN_LEVEL2(int value) {
		decode_dic_size += value;
		decode_size_bit = (int) Math.ceil(log2(decode_dic_size));
	}
	
	//188
	private void initialize_De_Size_CN_CHARAREA13(int value) {
		decode_dic_size += value;
		decode_size_bit = (int) Math.ceil(log2(decode_dic_size));
	}
	
	//684
	private void initialize_De_Size_CN_CHARAREA_ALL(int value) {
		decode_dic_size += value;
		decode_size_bit = (int) Math.ceil(log2(decode_dic_size));
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
			de_dictionary.put(current_size,word);
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
		de_dictionary.clear();
		return true;
	}
	
}
