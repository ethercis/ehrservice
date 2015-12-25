/*
 * Copyright (c) 2015 Christian Chevalley
 * This file is part of Project Ethercis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ethercis.ehr.encode;

import org.apache.commons.io.IOUtils;

import java.io.*;

public class HashedDirectory {

	/**
	 * Generate best performance directory structure
	 * 
	 * @param name
	 * @return
	 */
	public static String hashDir(String refId, String name) {
		String fileName = refId +name;

		int hashcode = fileName.hashCode();
		int mask = 255;
		int firstDir = hashcode & mask;
		int secondDir = (hashcode >> 8) & mask;

		StringBuilder sb = new StringBuilder(File.separator);
		sb.append(String.format("%02x", firstDir));
		sb.append(File.separator);
		sb.append(String.format("%02x", secondDir));
		sb.append(File.separator);
		sb.append(name);
		return sb.toString();
	}

	/**
	 * 
	 * @param path
	 * @param is
	 * @throws java.io.FileNotFoundException
	 * @throws java.io.IOException
	 */
	public static synchronized void save(String path, InputStream is)
			throws FileNotFoundException, IOException {
		String baseDir = System.getProperty("user.home") + File.separator
				+ ".ethercis" + File.separator + "content" + File.separator;
		String dirStr = path.substring(0, path.lastIndexOf(File.separator));
		String filename = path.substring(path.lastIndexOf(File.separator));
		File dir = new File(baseDir + dirStr);
		if(!dir.exists()){
			dir.mkdirs();
		}
		IOUtils.copyLarge(is, new FileOutputStream(new File(dir, filename)));

	}
	
	public static InputStream loadContent(String path) throws FileNotFoundException{
		String baseDir = System.getProperty("user.home") + File.separator
				+ ".ethercis" + File.separator + "content" + File.separator;
		return new FileInputStream(new File(baseDir, path));
	}

}
