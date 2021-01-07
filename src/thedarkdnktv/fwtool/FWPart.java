package thedarkdnktv.fwtool;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author TheDarkDnKTv
 *
 */
public class FWPart {
	private static final Pattern DATES = Pattern.compile("([\\d]{1,2}/[\\d]{1,2}/[\\d]{4})|([a-zA-Z]{3}\s{0,2}[\\d]{1,2}\\s{0,2}[\\d]{4})");
	private static final Pattern VERSIONS = Pattern.compile("([\\d]{1,4}\\.[\\d]{1,4}\\.[\\d]{1,4}[-\\d]{1,4})"
			+ "|([\\d]{1,4}\\.[\\d]{1,4}\\.[\\d]{1,4}\\.[\\d]{1,4}-[\\d]{1,5})"
			+ "|([\\d]{1,4}\\.[\\d]{1,4}-[\\d]{1,5})"
			+ "|(\0[\\d]{3,5}\0)");
	
	
	private final String type;
	private final String version;
	private final String buildDate;
	private final byte[] data;
	
	private FWPart(byte[] data, String type, String buildDate, String version) {
		this.data = data;
		this.type = type;
		this.buildDate = buildDate;
		this.version = version;
	}
	
	public static FWPart parse(byte[] data) {
		byte[] head = new byte[320];
		System.arraycopy(data, 0, head, 0, head.length);
		String typeHeader = new String(head, StandardCharsets.US_ASCII);
		Matcher m0 = FWTool.HEADER_PATTERN.matcher(typeHeader);
		Matcher m1 = DATES.matcher(typeHeader);
		Matcher m2 = VERSIONS.matcher(typeHeader);
		FWPart result = new FWPart(data, m0.find() ? m0.group(1) : "null", m1.find() ? m1.group() : "unknown", m2.find() ? m2.group() : "unknown");
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
		System.out.printf("Found %s, version %s, build date %s\n", type, version, buildDate);
	}
	
	public String getType() {
		return type;
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof FWPart && ((FWPart)o).type.equals(type);
	}
}
