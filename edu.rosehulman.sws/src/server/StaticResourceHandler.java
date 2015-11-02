/*
 * StaticResourceHandler.java
 * Oct 25, 2015
 *
 * Simple Web Server (SWS) for EE407/507 and CS455/555
 * 
 * Copyright (C) 2011 Chandan Raj Rupakheti, Clarkson University
 * 
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/lgpl.html>.
 * 
 * Contact Us:
 * Chandan Raj Rupakheti (rupakhcr@clarkson.edu)
 * Department of Electrical and Computer Engineering
 * Clarkson University
 * Potsdam
 * NY 13699-5722
 * http://clarkson.edu/~rupakhcr
 */
 
package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.HttpResponseFactory;
import protocol.Protocol;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class StaticResourceHandler extends AbstractRequestHandler {
	
	private Server server;
	private String uri;
	private String rootDirectory;
	private File file;
	
	public StaticResourceHandler(Server server){
		this.server = server;
	}

	@Override
	public HttpResponse doPost(HttpRequest request){
		HttpResponse response = null;
		// Map<String, String> header = request.getHeader();
		// String date = header.get("if-modified-since");
		// String hostName = header.get("host");
		//
		// Handling GET request here
		// Get relative URI path from request
		uri = request.getUri();
		// Get root directory path from server
		rootDirectory = server.getRootDirectory();
		// Combine them together to form absolute file path
		file = new File(rootDirectory + uri);
		// Check if the file exists
		if (file.exists()) {
			if (file.isDirectory()) {
				// Look for default index.html file in a directory
				String location = rootDirectory + uri + System.getProperty("file.separator") + Protocol.DEFAULT_FILE;
				file = new File(location);
				if (file.exists()) {
					// Lets create 200 OK response
					response = postFileDoesExistResponse(request);
				} else {
					response = postFileDoesNotExistResponse(request);
				}
			} else { // Its a file
				response = postFileDoesExistResponse(request);
			}
		} else {
			response = postFileDoesNotExistResponse(request);
		}
		return response;
	}

	@Override
	public HttpResponse doDelete(HttpRequest request){
		HttpResponse response = null;
		// Map<String, String> header = request.getHeader();
		// String date = header.get("if-modified-since");
		// String hostName = header.get("host");
		//
		// Handling GET request here
		// Get relative URI path from request
		uri = request.getUri();
		// Get root directory path from server
		rootDirectory = server.getRootDirectory();
		// Combine them together to form absolute file path
		file = new File(rootDirectory + uri);
		Path path = file.toPath();
		try {
		    Files.delete(path);
			response = HttpResponseFactory.create204NoContent(Protocol.CLOSE);
		} catch (NoSuchFileException x) {
		    System.err.format("%s: no such" + " file or directory%n", path);
			response = HttpResponseFactory.create304NotModified(Protocol.CLOSE);
		} catch (DirectoryNotEmptyException x) {
		    System.err.format("%s not empty%n", path);
			response = HttpResponseFactory.create403Forbidden(Protocol.CLOSE);
		} catch (IOException x) {
			response = HttpResponseFactory.create403Forbidden(Protocol.CLOSE);
		}
		return response;
	}

	@Override
	public HttpResponse doGet(HttpRequest request){
		HttpResponse response = null;
		// Map<String, String> header = request.getHeader();
		// String date = header.get("if-modified-since");
		// String hostName = header.get("host");
		//
		// Handling GET request here
		// Get relative URI path from request
		String uri = request.getUri();
		// Get root directory path from server
		String rootDirectory = server.getRootDirectory();
		// Combine them together to form absolute file path
		File file = new File(rootDirectory + uri);
		// Check if the file exists
		if (file.exists()) {
			if (file.isDirectory()) {
				// Look for default index.html file in a directory
				String location = rootDirectory + uri + System.getProperty("file.separator") + Protocol.DEFAULT_FILE;
				file = new File(location);
				if (file.exists()) {
					// Lets create 200 OK response
					response = HttpResponseFactory.create200OK(file, Protocol.CLOSE);
				} else {
					// File does not exist so lets create 404 file not found
					// code
					response = HttpResponseFactory.create404NotFound(Protocol.CLOSE);
				}
			} else { // Its a file
						// Lets create 200 OK response
				response = HttpResponseFactory.create200OK(file, Protocol.CLOSE);
			}
		} else {
			// File does not exist so lets create 404 file not found code
			response = HttpResponseFactory.create404NotFound(Protocol.CLOSE);
		}
		return response;
	}
	
	@Override
	public HttpResponse doPut(HttpRequest request){
		HttpResponse response = null;
		// Map<String, String> header = request.getHeader();
		// String date = header.get("if-modified-since");
		// String hostName = header.get("host");
		//
		// Handling GET request here
		// Get relative URI path from request
		uri = request.getUri();
		// Get root directory path from server
		rootDirectory = server.getRootDirectory();
		// Combine them together to form absolute file path
		file = new File(rootDirectory + uri);
		// Check if the file exists
		if (file.exists()) {
			if (file.isDirectory()) {
				// Look for default index.html file in a directory
				String location = rootDirectory + uri + System.getProperty("file.separator") + Protocol.DEFAULT_FILE;
				file = new File(location);
				if (file.exists()) {
					// Lets create 200 OK response
					response = putFileDoesExistResponse(request);
				} else {
					response = putFileDoesNotExistResponse(request);
				}
			} else { // Its a file
				response = putFileDoesExistResponse(request);
			}
		} else {
			response = putFileDoesNotExistResponse(request);
		}
		return response;
	}
	
	/**
	 * @param request
	 * @return
	 */
	private HttpResponse putFileDoesExistResponse(HttpRequest request) {
		HttpResponse response = null;
		try {
			BufferedWriter writer = Files.newBufferedWriter(file.toPath(), 
                StandardCharsets.UTF_8);
			writer.write(request.getBody());
			writer.close();
			response = HttpResponseFactory.create200OK(file, Protocol.CLOSE);
		} catch (IOException e) {
			System.out.println("could not write to file");
			response = HttpResponseFactory.create304NotModified(Protocol.CLOSE);
		}
		return response;
	}

	/**
	 * @param request
	 * @return
	 */
	private HttpResponse putFileDoesNotExistResponse(HttpRequest request) {
		HttpResponse response = null;
		try {
			BufferedWriter writer = Files.newBufferedWriter(file.toPath(), 
                StandardCharsets.UTF_8);
			writer.write(request.getBody());
			writer.close();
			response = HttpResponseFactory.create201OK(file, Protocol.CLOSE);
		} catch (IOException e) {
			System.out.println("could not write to file");
			response = HttpResponseFactory.create304NotModified(Protocol.CLOSE);
		}
		return response;
	}
	
	/**
	 * @param request
	 * @return
	 */
	private HttpResponse postFileDoesExistResponse(HttpRequest request) {
		HttpResponse response = null;
		try {
			Files.write(file.toPath(), new String(request.getBody()).getBytes(), StandardOpenOption.APPEND);
			response = HttpResponseFactory.create200OK(file, Protocol.CLOSE);
		} catch (IOException e) {
			System.out.println("could not write to file");
			response = HttpResponseFactory.create304NotModified(Protocol.CLOSE);
		}
		return response;
	}

	/**
	 * @param request
	 * @return
	 */
	private HttpResponse postFileDoesNotExistResponse(HttpRequest request) {
		byte dataToWrite[] = new String(request.getBody()).getBytes();
		FileOutputStream out;
		HttpResponse response = null;
		try {
			out = new FileOutputStream(rootDirectory + uri);
			out.write(dataToWrite);
			out.close();
			response = HttpResponseFactory.create201OK(file, Protocol.CLOSE);
		} catch (IOException e) {
			System.out.println("could not write to file");
			response = HttpResponseFactory.create304NotModified(Protocol.CLOSE);
		}
		return response;
	}
}
