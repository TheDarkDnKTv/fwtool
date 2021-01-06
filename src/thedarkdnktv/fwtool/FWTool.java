package thedarkdnktv.fwtool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author TheDarkDnKTv
 *
 */
public class FWTool {
	public static void main(String[] args) {
		// <file/path> [out path] [--pure](only binary without header) 
		
		System.out.println("========= Firmare parse tool 1.0 =========");
		System.out.println("=========  Made by TheDarkDnKTv  =========");
		System.out.println("= https://github.com/TheDarkDnKTv/fwtool =\n\n");
		
		if (args.length == 0) {
			System.out.println("Usage: <input> [--o <output>] [--pure]");
			System.out.printf("\t%-10s%s\n", "input", "Path to firmware binary or filename");
			System.out.printf("\t%-10s%s\n", "--o <output>", "Path to output folder [optional]");
			System.out.printf("\t%-10s%s\n", "--pure", "Make pure binaries, without header (Use only if planning to flash directly to chip) [optional]");
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
					System.out.print("Type Y to continue or other controller type number: ");
					
					BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
					while (true) {
						String input = reader.readLine();
						if (input.equalsIgnoreCase("y")) {
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
					FWTool.processRom(in, output, type);
				} else {
					exit("Wrong FW file");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	static void processRom(SeekableByteChannel in, Path out, String type) throws IOException {
		System.out.println("\nProcessing firmware for controller SAS" + type);
		
		
	}
	
	static void exit() {
		System.exit(0);
	}
	
	static void exit(String msg) {
		System.out.print(msg);
		exit();
	}
}
