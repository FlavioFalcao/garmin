/* Copyright 2012 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class Gsort {
	
	public static void main(String[] args) {

		class Way {
			public ArrayList<String> tags;
			public Way() {
				tags = new ArrayList<String>();
			}
		}
		
		class Type {
			public String key;
			public String val;
			public ArrayList<Way> objs;
			public Type() {
				key = "";
				val = "";
				objs = new ArrayList<Way>();
			}
		}
		
		ArrayList<Type> types = new ArrayList<Type>();
		
		if (args.length == 2) {
			try {
				FileInputStream sort = new FileInputStream(args[0]);
			  BufferedReader buff = new BufferedReader(new InputStreamReader(sort));
			  String line;
			  while ((line = buff.readLine()) != null) {
			  	line = line.trim();
			  	if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
			  		String keyval[] = line.split("=");
			  		if (keyval.length == 2) {
			  			keyval[0] = keyval[0].trim();
			  			keyval[1] = keyval[1].trim();
			  			if (!keyval[0].isEmpty() && !keyval[1].isEmpty()) {
			  				Type newtype = new Type();
			  				newtype.key = keyval[0];
			  				newtype.val = keyval[1];
			  				types.add(newtype);
			  			} else {
								System.out.println("Empty string: " + line);
							  sort.close();
				  			return;
			  			}
			  		} else {
							System.out.println("Malformed data: " + line);
						  sort.close();
			  			return;
			  		}
			  	}
			  }
			  buff.close();
			  sort.close();
			} catch (IOException e) {
				System.out.println("File I/O error: " + args[0]);
				return;
			}
			
			try {
				File infile = new File(args[1]);
			  BufferedReader inbuff = new BufferedReader(new InputStreamReader(new FileInputStream(infile), "UTF-8"));
				File outfile = new File("tmp");
			  BufferedWriter outbuff = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF-8"));
			  String inline;
			  Boolean inway = false;
			  Type match = null;
			  Way way = new Way();
			  while ((inline = inbuff.readLine()) != null) {
			  	while (!inline.trim().endsWith(">")) {
			  		inline += inbuff.readLine();
			  	}
			  	if (inway) {
			  		if (inline.contains("</way>")) {
			  			inway = false;
			  		} else {
				  		if (inline.contains("<tag")) {
				  			int beg = inline.indexOf("k=", 0) + 2;
				  			char quote = inline.charAt(beg);
				  			int end = inline.indexOf(quote, beg+1);
				  			String key = inline.substring(beg+1, end);
				  			beg = inline.indexOf("v=", 0) + 2;
				  			quote = inline.charAt(beg);
				  			end = inline.indexOf(quote, beg+1);
				  			String value = inline.substring(beg+1, end);
				  			for (Type type : types) {
				  				if (type.key.equals(key) && type.val.equals(value)) {
				  					match = type;
				  				}
				  			}
				  		}
			  		}
		  			way.tags.add(inline);
		  			if (!inway) {
		  				if (match != null) {
		  					match.objs.add(way);
		  				  way = new Way();
		  				} else {
		  					while (way.tags.size() > 0) {
		  						outbuff.write(way.tags.remove(0));
							  	outbuff.newLine();
		  					}
		  				}
		  			}
			  	} else {
			  		if (inline.contains("<way")) {
			  			inway = true;
			  			match = null;
			  			way.tags.add(inline);
			  		} else {
			  			if (inline.contains("<relation") || inline.contains("</osm")) {
			  				while (types.size() > 0) {
			  					Type type = types.remove(0);
			  					while (type.objs.size() > 0) {
			  						Way item = type.objs.remove(0);
				  					while (item.tags.size() > 0) {
				  						outbuff.write(item.tags.remove(0));
									  	outbuff.newLine();
				  					}
			  					}
			  				}
			  			}
					  	outbuff.write(inline);
					  	outbuff.newLine();
			  		}
			  	}
			  }
				System.out.println("gsort: " + args[1] + " finished");
				inbuff.close();
		  	outbuff.flush();
				outbuff.close();
				infile.delete();
				outfile.renameTo(infile);
			} catch (IOException e) {
				System.out.println("File I/O error: " + args[1]);
				return;
			}
		} else {
			System.out.println("Usage: java -jar gsort.jar <path/to/sort/file> <path/to/osm/file>");
		}
	}

}
