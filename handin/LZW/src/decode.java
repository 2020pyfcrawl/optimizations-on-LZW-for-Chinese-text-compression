import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;




/**
 * decode class
 * @author pyf19
 *
 */
public class decode {
	
	
	/**
	 * decode in byte stream
	 * call bytesOut to write file
	 * @param inputBytes
	 * @param outputFilePath
	 * @param dic_size
	 * @throws IOException
	 */
	public void decode_ENCN_FromBytes(byte[] inputBytes, String outputFilePath, int dic_size) throws IOException {
		
		/* output part */
		BinaryFileOut t = new BinaryFileOut();
		t.setOutputStream(outputFilePath);

		/* dictionary part */
		LZW_de_dictionary dic_decode = new LZW_de_dictionary();
		/* pre filling */
		dic_decode.preFillDic();
		/* set dictionary size */
		dic_decode.setDicSize(dic_size);
		
		/*
		 * Algorithm:
		 * 
		 * 	BEGIN
				s = NIL;
				while not EOF
				{
					k = next input code;
					entry = dictionary entry for k;
			
					if (entry == NULL)
						entry = s + s[0];
					output entry;
					if (s != NIL)
						add string s + entry[0] to dictionary with a new code;
					s = entry;
				}
			END
		 */
		
		/* get the index stream according to teh adaptive extended coding */
		ArrayList<Integer> numlist = dic_decode.AdaptdecodeStringtoIndex(inputBytes);
		
		int entry_index = 0;
		String entry;
		String s = null;
		for(int i = 0; i < numlist.size(); i++) {
			entry_index = numlist.get(i);
			entry = dic_decode.getEntry(entry_index);
			if(entry == null & s != null) {
				entry = s + s.charAt(0);
			}
			/* output entry */ 
			bytesOut(entry, t);
			
			if( s != null) {
				dic_decode.putWord(s + entry.charAt(0));
			}
			s = entry;
			
		}
		System.out.println("decode finished!");
			
	}

	/**
	 * 
	 * get bytes from string will cause some error when the byte value is 128-255 (may turn to 3F? )
	 * so there we use get chars 
	 * @param binary_bits
	 * @param binary_out
	 * @return
	 */
	public boolean bytesOut(String binary_bits, BinaryFileOut binary_out) {
		
		char []chars = new char[binary_bits.length()];
		binary_bits.getChars(0, binary_bits.length(), chars, 0);
		/*byte[] bytes = binary_bits.getBytes();  deprecated */
		
		String s;
		for(int index = 0; index < chars.length ; index ++) {
			s = Integer.toBinaryString(chars[index]);
			int bits_length = s.length();
			int interval = 8 - bits_length;
			for(int position = 0; position < 8; position++) {
				if(interval > position ) {
					/* zero fill at the beginning */
					binary_out.writeBit(false);
				}
				else {
					char c = s.charAt(position - interval);
					if(c == '0') {
						/* write 0 */
						binary_out.writeBit(false);
					}else {
						/* write 1 */
						binary_out.writeBit(true);
					}
				}
			}
		}			
		return true;
	}
	
	
	/**
	 * decode Chinese chars by calling CharsOut
	 * @param inputBytes
	 * @param outputFilePath
	 * @param dic_size
	 * @param LZW_CH_NUM
	 * @throws IOException
	 */
	public void decode_CN_FromChars(byte[] inputBytes, String outputFilePath, int dic_size,int LZW_CH_NUM) throws IOException {
		
		/* output part */
		BinaryFileOut t = new BinaryFileOut();
		t.setOutputStream(outputFilePath);
		
		
		/* dictionary part */
		LZW_de_dictionary dic_decode = new LZW_de_dictionary();
		/* set dictionary size */
		dic_decode.setDicSize(dic_size);
		
		/* pre filling */
		dic_decode.preFillDic();	//byte value 0-255
		System.out.println("0-255 in, now size = " + dic_decode.getSize());
		dic_decode.preFillDic_CN_LEVEL1();
		System.out.println("LEVEL 1 in, now size = " + dic_decode.getSize());
		if(LZW_CH_NUM == 2) {
			dic_decode.preFillDic_CN_LEVEL2();
			System.out.println("LEVEL 2 in, now size = " + dic_decode.getSize());
		}
		if(LZW_CH_NUM == 2 | LZW_CH_NUM == 4) {
			dic_decode.preFillDic_CN_CHARAREA_ALL();
			System.out.println("CHARACTER AREA in, now size = " + dic_decode.getSize());
		}
		if(LZW_CH_NUM == 5 | LZW_CH_NUM == 6) {
			dic_decode.preFillDic_CN_CHARAREA13();
			System.out.println("CHARACTER AREA13 in, now size = " + dic_decode.getSize());
		}
		
		/*
		 * Algorithm:
		 * 
		 * 	BEGIN
				s = NIL;
				while not EOF
				{
					k = next input code;
					entry = dictionary entry for k;
			
					if (entry == NULL)
						entry = s + s[0];
					output entry;
					if (s != NIL)
						add string s + entry[0] to dictionary with a new code;
					s = entry;
				}
			END
		 */
		
		/* get the index stream according to teh adaptive extended coding */
		ArrayList<Integer> numlist = null;
		if(LZW_CH_NUM == 6) {
			numlist = dic_decode.AdaptdecodeStringtoIndex_Opt(inputBytes);
		}
		else {
			numlist = dic_decode.AdaptdecodeStringtoIndex(inputBytes);
		}
		
		
		int entry_index = 0;
		String entry;
		String s = null;
		for(int i = 0; i < numlist.size(); i++) {
			entry_index = numlist.get(i);
			entry = dic_decode.getEntry(entry_index);
			if(entry == null & s != null) {
				/* do not use GBK to re-code, this will make the byte lost */
//				byte []tmp = s.getBytes();
//				String stmp = new String(tmp,"GBK");
				entry = s + s.charAt(0);
			}
			/* output entry */ 
			CharsOut(entry, t);

			if( s != null) {
				dic_decode.putWord(s + entry.charAt(0));
			}
			s = entry;
			
		}
		
		System.out.println("decode finished!");
			
	}
	
	/**
	 * output char out
	 * if the char is Chinese char, output two bytes
	 * else the char is always begin with the first all zero, then only output the second byte
	 * @param binary_bits
	 * @param binary_out
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public boolean CharsOut(String binary_bits, BinaryFileOut binary_out) throws UnsupportedEncodingException {
		
		char []chars1 = new char[binary_bits.length()];
		binary_bits.getChars(0, binary_bits.length(), chars1, 0);
		
		String s;
		for(int index = 0; index < chars1.length ; index ++) {
			int f1 = (chars1[index] & 0xff00) >> 8;
			int f2 = chars1[index] & 0xff;
			if(f1 != 0 ) {
				char []chartmp = new char[1];
				chartmp[0] = chars1[index];
				String strtmp = new String(chartmp);
				strtmp = new String(strtmp.getBytes(),"GBK");
				byte []bytetmp = strtmp.getBytes();

				for(int i = 0 ; i < bytetmp.length ; i++) {
					s = Integer.toBinaryString(bytetmp[i] & 0xff);
					int bits_length = s.length();
					int interval = 8 - bits_length;
					for(int position = 0; position < 8; position++) {
						if(interval > position ) {
							/* zero fill at the beginning */
							binary_out.writeBit(false);
						}
						else {
							char c = s.charAt(position - interval);
							if(c == '0') {
								/* write 0 */
								binary_out.writeBit(false);
							}else {
								/* write 1 */
								binary_out.writeBit(true);
							}
						}
					}
				}
				
			}
			else {
				s = Integer.toBinaryString(f2);
				int bits_length = s.length();
				int interval = 8 - bits_length;
				for(int position = 0; position < 8; position++) {
					if(interval > position ) {
						/* zero fill at the beginning */
						binary_out.writeBit(false);
					}
					else {
						char c = s.charAt(position - interval);
						if(c == '0') {
							/* write 0 */
							binary_out.writeBit(false);
						}else {
							/* write 1 */
							binary_out.writeBit(true);
						}
					}
				}
			}
			
		}			
		return true;
	}
}













