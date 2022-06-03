import java.io.IOException;


/**
 * encode class
 * @author pyf19
 *
 */
public class encode {	
	
	/**
	 * Get input bytes;
	 * Generate output code while filling the dictionary.
	 * @param inputBytes
	 * @param outputFilePath
	 * @throws IOException
	 */
	public void encode_EN(byte[] inputBytes, String outputFilePath, int dic_size) throws IOException {
				
		/* dictionary part */
		LZW_dictionary dic_encode = new LZW_dictionary();
		/* pre filling */
		dic_encode.preFillDic();
		/* set output path */
		dic_encode.setOutputFile(outputFilePath);
		/* set dictionary size */
		dic_encode.setDicSize(dic_size);
		
		/*
		 *  Algorithm:
		 * 
		 * 	Dictionary[j] ¡û all n single-character£¬ j£½1, 2£¬ ¡­£¬n 
		 	j ¡û n+1
		 	Prefix ¡û read first Character in Charstream
		 	while((C ¡û next Character)!=NULL) 
			 	Begin 
			 	If Prefix.C is in Dictionary
			 		Prefix ¡û Prefix.C
			 	else 
			 		Codestream ¡û cW for Prefix
			 		Dictionary[j] ¡û Prefix.C 
			 		j ¡û n+1
			 		Prefix ¡û C
			 end 
			 Codestream ¡û cW for Prefix
		 */	
		
		String prefix = "";
		String prefix_c = "";
		char current_char;
		for( int position = 0; position < inputBytes.length; position++) {
			
			/* byte stream, each char is one byte */
			current_char = (char) (inputBytes[position] & 0xff);
			prefix_c = prefix + current_char ;
			if(dic_encode.getPrefixIndex(prefix_c) == -1) {
				/* not exist, output prefix's index */
				dic_encode.outputCode(prefix);
				
				/* fill the new word to the dictionary */
				/* return true if fill succeed, or false if full */
				dic_encode.putWord(prefix_c); 
				
				prefix = current_char + "";
			}
			else {
				prefix = prefix_c;				
			}
		}
		/* all input bytes over, output the remaining part */
		dic_encode.outputCode(prefix);
		
		/* close and flush, necessary */
		dic_encode.outputDone();
		
		/* hint */
		System.out.println("encode finished!");
				
	}
	
	/* the intermediate products which assumes all chars are in dictionary */
	/* unused */
	public void encode_CN_indictionary(byte[] inputBytes, String outputFilePath, int dic_size) throws IOException {
		
		/* dictionary part */
		LZW_dictionary dic_encode = new LZW_dictionary();
		
		/* set dictionary size */
		dic_encode.setDicSize(dic_size);
		/* pre filling */
		dic_encode.preFillDic();
		System.out.println(dic_encode.getSize());
		dic_encode.preFillDic_CN_LEVEL1();
		System.out.println(dic_encode.getSize());
		dic_encode.preFillDic_CN_LEVEL2();
		System.out.println(dic_encode.getSize());
		dic_encode.preFillDic_CN_CHARAREA_ALL();
		System.out.println(dic_encode.getSize());
		
		/* set output path */
		dic_encode.setOutputFile(outputFilePath);
		
		
		/*
		 *  Algorithm:
		 * 
		 * 	Dictionary[j] ¡û all n single-character£¬ j£½1, 2£¬ ¡­£¬n 
		 	j ¡û n+1
		 	Prefix ¡û read first Character in Charstream
		 	while((C ¡û next Character)!=NULL) 
			 	Begin 
			 	If Prefix.C is in Dictionary
			 		Prefix ¡û Prefix.C
			 	else 
			 		Codestream ¡û cW for Prefix
			 		Dictionary[j] ¡û Prefix.C 
			 		j ¡û n+1
			 		Prefix ¡û C
			 end 
			 Codestream ¡û cW for Prefix
		 */	
		String inputString = new String (inputBytes,"GB2312");
		String prefix = "";
		String prefix_c = "";
		char current_char = 0;
		char next_char;
		int sign = 0;
		int length = 0;
		byte []tmp = new byte[10];
		for( int position = 0; position < inputString.length(); position++) {
			
			/* byte stream, each char is one byte */
			current_char = inputString.charAt(position);
			
			prefix_c = prefix + current_char ;
			if(dic_encode.getPrefixIndex(prefix_c) == -1) {
				/* not exist, output prefix's index */
				dic_encode.outputCode(prefix);
				
				/* fill the new word to the dictionary */
				/* return true if fill succeed, or false if full */
				dic_encode.putWord(prefix_c); 
				
				prefix = current_char + "";
			}
			else {
				prefix = prefix_c;				
			}
		}
		/* all input bytes over, output the remaining part */
		dic_encode.outputCode(prefix);
		
		/* close and flush, necessary */
		dic_encode.outputDone();
		
		/* hint */
		System.out.println("encode finished!");
				
	}
	
	
	
	/**
	 * process char stream for Chinese
	 * if there is a char not in the dictionary, split it into bytes
	 * and read bytes to encode
	 * @param inputBytes
	 * @param outputFilePath
	 * @param dic_size
	 * @param LZW_CH_NUM
	 * @throws IOException
	 */
	public void encode_CN(byte[] inputBytes, String outputFilePath, int dic_size, int LZW_CH_NUM) throws IOException {
		
		/* dictionary part */
		LZW_dictionary dic_encode = new LZW_dictionary();
		
		/* set dictionary size */
		dic_encode.setDicSize(dic_size);
		
		/* pre filling */
		dic_encode.preFillDic();
		System.out.println(dic_encode.getSize());
		dic_encode.preFillDic_CN_LEVEL1();
		System.out.println("LEVEL 1 in, now size = " + dic_encode.getSize());
		if(LZW_CH_NUM == 2) {
			dic_encode.preFillDic_CN_LEVEL2();
			System.out.println("LEVEL 2 in, now size = " + dic_encode.getSize());
		}
		if(LZW_CH_NUM == 2 | LZW_CH_NUM == 4) {
			dic_encode.preFillDic_CN_CHARAREA_ALL();
			System.out.println("CHARACTER AREA in, now size = " + dic_encode.getSize());
		}
		if(LZW_CH_NUM == 5 | LZW_CH_NUM == 6 ) {
			dic_encode.preFillDic_CN_CHARAREA13();
			System.out.println("CHARACTER AREA13 in, now size = " + dic_encode.getSize());
		}
		
		/* test one char's position */
		//System.out.println(dic_encode.getPrefixIndex("¡¤"));
		
		/* set output path */
		dic_encode.setOutputFile(outputFilePath);
		
		
		/*
		 *  Algorithm:
		 * 
		 * 	 compress 
			Dictionary pre_fill
			read next prefix as char
			while(EOF) 
			Begin 
				read C from file if all bytes in the array are read
				if C is not in Dictionary
					byte []tmp = prefix.getBytes()
					read C from tmp until all bytes are read (not from file)
				if Prefix.C is in Dictionary
					Prefix ¡û Prefix.C
				else 
					Codestream ¡û cW for Prefix
					Dictionary[j] ¡û Prefix.C 
					j ¡û n+1
					Prefix ¡û C
			end 
			Codestream ¡û cW for Prefix
		 */	
		String inputString = new String (inputBytes,"GBK");
		String prefix = "";
		String prefix_c = "";
		char current_char = 0;
		
		/* if a char is parted into bytes, it is true */
		/* if all bytes are read, it turns to false */
		boolean parting = false;	
		int sign = 0;
		int length = 0;	//length of the bytes
		byte []tmp = new byte[10];
		for( int position = 0; position < inputString.length() | sign != 0; ) {
			
			/* read the char from input file */
			if(sign == 0) {
				current_char = inputString.charAt(position);
				position++;
			}
			
			/* if the sign is not zero, read from the byte array */
			if(sign != 0) {
				current_char = (char) (tmp[length - sign] & 0xff);
				sign --;
			}
			
			if((dic_encode.getPrefixIndex(current_char+ "") == -1 | (dic_encode.getPrefixIndex(current_char+ "") < 256 & dic_encode.getPrefixIndex(current_char+ "") > 128)) & parting == false) {
				/* this char is not in the dictionary */
				char []b = new char[1];
				b[0] = current_char;
				String str = new String (b);
				str = new String(str.getBytes(),"GBK");
				
				/* spilt the char into bytes to process, always two */
				tmp = str.getBytes();
				sign = tmp.length;
				length = sign;
				current_char = (char) (tmp[0] & 0xff);
				sign --;
				parting = true;
			}
			/* set the parting false if sign goes 0 */
			if(sign == 0) {
				parting = false;
			}
			
			prefix_c = prefix + current_char  ;

			if(dic_encode.getPrefixIndex(prefix_c) == -1) {
				/* not exist, output prefix's index */
				dic_encode.outputCode(prefix,LZW_CH_NUM);
				
				/* fill the new word to the dictionary */
				/* return true if fill succeed, or false if full */
				dic_encode.putWord(prefix_c); 
				
				prefix = current_char + "";
			}
			else {
				prefix = prefix_c;				
			}
		}
		/* all input bytes over, output the remaining part */
		dic_encode.outputCode(prefix);
		
		/* close and flush, necessary */
		dic_encode.outputDone();
		
		/* hint */
		System.out.println("encode finished!");
				
	}
	
	
}

