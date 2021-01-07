package thedarkdnktv.fwtool;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;

/**
 * @author TheDarkDnKTv
 *
 */
public class FWPart {
	
	private final String type;
	private String version;
	private String buildDate;
	private final byte[] data;
	
	private FWPart(byte[] data, String type) {
		this.data = data;
		this.type = type;
	}
	
	public static FWPart parse(byte[] data) {
		byte[] head = new byte[20];
		System.arraycopy(data, 0, head, 0, head.length);
		
		String typeHeader = new String(head, StandardCharsets.US_ASCII);
		Matcher matcher= FWTool.HEADER_PATTERN.matcher(typeHeader);
		matcher.find();
		
		FWPart result = new FWPart(data, matcher.group(1));
		return result;
	}
	
	public void writeToFile(Path dir, String filename) {
		Path res = dir.resolve(filename + ".bin");
		try (FileChannel ch = FileChannel.open(res, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			ch.write(ByteBuffer.wrap(data));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void print() {
		System.out.println("Module " + type);
		System.out.printf("%15s = %s\n", "VERSION", version);
		System.out.printf("%15s = %s\n", "BUILD_DATE", buildDate);
		System.out.println("----------------------------------------------");
	}
	
	public String getType() {
		return type;
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof FWPart && ((FWPart)o).type.equals(type);
	}
}
