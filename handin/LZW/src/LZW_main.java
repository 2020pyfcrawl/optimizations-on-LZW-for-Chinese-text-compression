import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


 
/**
 * Main part, call specific encode and decode program
 * encode_EN / decode_EN is primarily the traditional LZW which can compress 
 * both Chinese and English text. This method work as byte stream, 
 * while compressing Chinese, because there are not Chinese chars in the dictionary,
 * we can only process as in byte and output each index of the byte, the byte value
 * 0-255 is all in the dictionary so it can be achieved.
 * Meanwhile, in this part we can set the max size of the dictionary by setting the 
 * number of bit can be used to represent the index. Also, the output code follows the
 * adaptive extended-coding.
 * 
 * encode_CN / decode_CN does some modification to the above byte stream algorithm.
 * It enables to process Chinese chars because some of GB2312 chars are added to the 
 * dictionary. However, the biggest problem, which you can also imagined, is that if 
 * this chars is not in the dictionary, what would we do to process it?
 * I think this is the most difficult part, and the authors of the related thesis 
 * ignored this part. I process this situation by splitting the char into two bytes 
 * and then do bytes encode. This process seems simple but difficult to implement.
 * Some problems are that the traditional algorithm needs to be modified, the char in
 * Java and corresponding bytes and String is quite different from each other, it always
 * get a lot of error while converting this value, which I think java is much more
 * disadvantaged to process bit stream and byte conversion than C.
 * 
 * To be specific, I will show some versions of my algorithm, the algorithm is the same
 * the difference is the words added to the dictionary.
 * LZW_CH_1 ---Traditional algorithm by encode_EN
 * LZW_CH_2 ---New modified LZW algorithm with all chars in GB2312-80 added to the dictionary
 * LZW_CH_3 ---New modified LZW algorithm with LEVEL ONE chars in GB2312-80 added to the dictionary
 * LZW_CH_4 ---New modified LZW algorithm with LEVEL ONE and CHARACTER AREA chars in GB2312-80 added to the dictionary
 * LZW_CH_5 ---New modified LZW algorithm with LEVEL ONE and some CHARACTER AREA(AREA 1+3) chars in GB2312-80 added to the dictionary
 * 
 * Finally, I re-encode to the output code to better compress, this one is the optimal one of the one above.
 * LZW_CH_6 ---LZW_CH_5 with optimal output code.
 * 
 * @author pyf19
 *
 */
public class LZW_main {
		
	public static void main(String[] args) throws IOException{
		
		/* used for defining the size of the dictionary 
		 * -represented by the max number of bits can be used
		 */
		int dic_size_bit = 19;
		
		/* if we use the final optimal method to re-encode the output index 
		 * this is used in LZW_CH
		 */
		boolean final_opt = false;
		
		/* use the specific algorithm */
		int LZW_CH_NUM = 6;
		

		/* you can specify the specific file name in the method, targeting at inputFilePath and outputFilePath*/

		/* byte stream, used in LZW_CH1*/
//		encode_EN(dic_size_bit);
//		decode_EN(dic_size_bit);
		
		/* char stream, used in LZW_CH2-6 */
		encode_CN(dic_size_bit,LZW_CH_NUM);
		decode_CN(dic_size_bit,LZW_CH_NUM);
	}
	
	/**
	 * call LZW_CN_NUM encode program
	 * @throws IOException
	 */
	public static void encode_CN(int sic_size_bit, int LZW_CH_NUM) throws IOException {
		
		long startTime = System.currentTimeMillis();	//start time
		String inputFilePath = "./TEXT/wch.txt";
		String outputFilePath = "./COMPRESS_hex/wch.hex";
		byte[] inputBytes = getInputBytes(inputFilePath);
		encode my_CN = new encode();
		my_CN.encode_CN(inputBytes, outputFilePath,sic_size_bit,LZW_CH_NUM);
		long endTime = System.currentTimeMillis();		//end time
		System.out.println("LZW_CH_" + LZW_CH_NUM + "encode time: " + (endTime - startTime) + "ms");	//running time
	}
	
	/**
	 * call LZW_CN_NUM decode program
	 * @throws IOException
	 */
	public static void decode_CN(int sic_size_bit, int LZW_CH_NUM) throws IOException {
		long startTime = System.currentTimeMillis();
		String inputFilePath = "./COMPRESS_hex/wch.hex";
		String outputFilePath = "./DE_TEXT/de_wch.txt";
		byte[] inputBytes = getInputBytes(inputFilePath);
		decode my_DE = new decode();
		my_DE.decode_CN_FromChars(inputBytes, outputFilePath,sic_size_bit,LZW_CH_NUM);
		long endTime = System.currentTimeMillis(); 
		System.out.println("LZW_CH_" + LZW_CH_NUM + "decode time: " + (endTime - startTime) + "ms");
	}
	
	
	
	/**
	 * call LZW_CNEN encode program
	 * @throws IOException
	 */
	public static void encode_EN(int sic_size_bit) throws IOException {
		long startTime = System.currentTimeMillis(); 
		String inputFilePath = "./TEXT/MGZC.txt";
		String outputFilePath = "./COMPRESS_hex/MGZC.hex";
		byte[] inputBytes = getInputBytes(inputFilePath);
		encode my_EN = new encode();
		my_EN.encode_EN(inputBytes, outputFilePath,sic_size_bit);
		long endTime = System.currentTimeMillis();
		System.out.println("LZW_EN/CH_1" + "encode time: " + (endTime - startTime) + "ms");
	}
	
	/**
	 * call LZW_CNEN decode program
	 * @throws IOException
	 */
	public static void decode_EN(int sic_size_bit) throws IOException {
		long startTime = System.currentTimeMillis();
		String inputFilePath = "./COMPRESS_hex/MGZC.hex";
		String outputFilePath = "./DE_TEXT/de_MGZC.txt";
		byte[] inputBytes = getInputBytes(inputFilePath);
		decode my_DE = new decode();
		my_DE.decode_ENCN_FromBytes(inputBytes, outputFilePath,sic_size_bit);
		long endTime = System.currentTimeMillis();
		System.out.println("LZW_EN/CH_1" + "decode time: " + (endTime - startTime) + "ms");
	}
	
	
	
	/**
	 * get input as String stream, where some bytes value (128-255) may be changed or edited,
	 * so we finally use the below one to get only bytes, which will not change the value.
	 * @param inputFilePath
	 * @return
	 * @throws IOException
	 */
	public static String getInputStr(String inputFilePath) throws IOException {
		InputStream input = new FileInputStream(inputFilePath);
		byte[] buffer = new byte[input.available()];
		input.read(buffer);
		String inputstr = new String(buffer);
		input.close(); 
		System.out.println(inputstr);
		return inputstr;
	}

	/**
	 * get input as bytes stream, do bytes analysis based on 0-255 pre-dictionary
	 * byte analysis is suitable for English because its character is byte stream.
	 * Meanwhile, it is useful to handle Chinese on a byte basis in UTF-8, where each 
	 * Chinese character occupies 3 bytes because we have not include Chinese in 
	 * pre-dictionary.
	 * @param inputFilePath
	 * @return
	 * @throws IOException
	 */
	public static byte[] getInputBytes(String inputFilePath) throws IOException {
		InputStream input = new FileInputStream(inputFilePath);
		byte[] buffer = new byte[input.available()];	// create buffer 
		input.read(buffer);	// read all bytes
		input.close();
		return buffer;
	}
	
}


