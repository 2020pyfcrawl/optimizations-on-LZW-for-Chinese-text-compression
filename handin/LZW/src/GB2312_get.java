import java.io.File;
import java.io.FileWriter;

/**
 * 
 */

/**
 * @author pyf19
 *
 */
public class GB2312_get {
	public static void main(String[] args) throws Exception {
        StringBuffer buffer = new StringBuffer();
        byte[] by = new byte[2];
        for (int b1 = 0xC4; b1 < 248; b1++) {
            by[0] = (byte) b1;
            for (int b2 = 161; b2 < 255; b2++) {
                by[1] = (byte) b2;
                String str = "";
                buffer.append(new String(by, "GB2312"));
            }
            System.out.print(buffer.toString());
            FileWriter writer=new FileWriter(new File("GB2312.txt"));
            writer.write(buffer.toString());
            writer.flush();
            writer.close();
        }
    }

}
