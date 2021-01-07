package thedarkdnktv.fwtool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * @author TheDarkDnKTv
 *
 */
public class FWTool {
	public static final Pattern HEADER_PATTERN = Pattern.compile("^[0-9]{4}.{12}([A-Z]{2,4})");

	public static void main(String[] args) {
		System.out.println("========= Firmare parse tool 1.0 =========");
		System.out.println("=========  Made by TheDarkDnKTv  =========");
		System.out.println("= https://github.com/TheDarkDnKTv/fwtool =\n\n");
		
		if (args.length == 0) {
			System.out.println("Usage: <input> [--o <output>] [--pure]");
			System.out.printf("\t%-10s%s\n", "input", "Path to firmware binary or filename");
			System.out.printf("\t%-10s%s\n", "--o <output>", "Path to output folder [optional]");
		} else {
			Path rom = Paths.get(args[0]);
			Path output = null;
			
			if (Files.notExists(rom))
				exit("Wrong input file!!!");
			
			if (args.length >= 3 && args[1].equalsIgnoreCase("--o")) {
				output = Paths.get(args[2]);
				if (Files.notExists(output))
					exit("Wrong output directory!!!");
			} else {
				output = Paths.get("output");
				try {
					if (Files.notExists(output)) Files.createDirectory(output);
				} catch (IOException e) {
					e.printStackTrace();
					exit();
				}
			}
			
			try {
				SeekableByteChannel in = FileChannel.open(rom);
				ByteBuffer buf = ByteBuffer.allocate(4);
				
				if (in.read(buf) == 4) {
					String type = new String(buf.array());
					System.out.println("Detected FW for SAS" + type);
					System.out.print("Press ENTER to continue or a valid controller type: ");
					
					BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
					while (true) {
						String input = reader.readLine();
						if (input.isEmpty()) {
							break;
						} else {
							try {
								Integer.parseInt(input);
							} catch (Throwable e) {
								System.out.println("Please enter a valid numeric type of controller");
								continue;
							}
							
							type = input;
							break;
						}
					}
					in.position(0);
					
					System.out.println("\nFile size: " + new DecimalFormat("###,###.###", DecimalFormatSymbols.getInstance(Locale.ENGLISH)).format(in.size()) + " bytes");
					if (in.size() > Integer.MAX_VALUE) exit("FW file too big!!!");
					
					buf = ByteBuffer.allocate((int)in.size());
					while (buf.hasRemaining())
						in.read(buf);
					in.close();
					buf.position(0);
					
					List<Integer> headers = FWTool.processRom(buf, output, type);
					FWTool.parseParts(buf, headers, output);
					
					System.out.println("Done. Found " + headers.size() + " parts of firmware.");
				} else {
					exit("Wrong FW file");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	static List<Integer> processRom(ByteBuffer buf, Path out, String type) throws IOException {
		System.out.println("Processing firmware for controller SAS" + type + "\n");
		List<Integer> headers = new ArrayList<>();
		
		while (buf.remaining() > 32) {
			int idx = -1;
			byte[] arr = new byte[32];
			buf.get(arr);
			
			if ((idx = new String(arr, StandardCharsets.US_ASCII).indexOf(type)) >= 0) {
				if (idx != 0) {
					if (buf.capacity() < buf.position() + idx + 20) {
						break;
					}
					
					buf.position(buf.position() + idx - 32);
					arr = new byte[20];
					buf.get(arr);
					idx = 0;
				}
				
				if (HEADER_PATTERN.matcher(new String(arr, StandardCharsets.US_ASCII)).find()) {
					headers.add(buf.position() - arr.length);
				}
			}
		}
		
		buf.position(0);
		return headers;
	}
	
	static void parseParts(ByteBuffer buf, List<Integer> headers, Path output) {
		for (int i = 0; i < headers.size(); i++) {
			int dataSize = i == headers.size() - 1 ? buf.capacity() - headers.get(i) : headers.get(i + 1) - headers.get(i);
			byte data[] = new byte[dataSize];
			buf.get(data);
			FWPart part = FWPart.parse(data);
			part.print();
			part.writeToFile(output, String.format("%02d-%s", i + 1, part.getType()));
		}
	}
	
	// For debugging
	static void print(byte[] arr) {
		System.out.print('[');
		for (int i = 0; i < arr.length; i++) {
			System.out.print(arr[i]);
			if (i < arr.length - 1) System.out.print(", ");
		}
		System.out.println(']');
	}
	
	static void exit() {
		System.exit(0);
	}
	
	static void exit(String msg) {
		System.out.print(msg);
		exit();
	}
}
