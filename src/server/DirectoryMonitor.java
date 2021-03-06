/*
 * DirectoryMonitor.java
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Scanner;

import JarClassLoader.JarClassLoader;
import protocol.Protocol;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class DirectoryMonitor extends Thread {

    private WatchService watcher;
    private Path dir;
    public String currentDir;
    private Server server;

    public DirectoryMonitor(Server server) {
        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            System.out.println("Error initializing WatchService:\n" + e);
        }
        dir = new File("plugins").toPath();
        currentDir = dir.toUri().toString().substring(5, dir.toUri().toString().length()).replaceAll("%20", " ");
        this.server = server;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        while (true) {
            try {
                WatchKey key = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE);
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();
                    if (!filename.toUri().toString().endsWith(".jar")) {
                        continue;
                    }
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    } else if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        loadplugin(filename.toString());
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        server.removePlugin(filename.toString());
                    }
                }
            } catch (IOException x) {
                System.err.println(x);
            }
        }
    }

    public void loadplugin(String filename) {
        try {
            URL url = new URL("jar:file:" + currentDir + filename + "!/routing.txt");
            InputStream is = url.openStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            JarClassLoader jarLoader = new JarClassLoader(currentDir + filename);
            Class c;
            Scanner scanner = new Scanner(input);
            try {
                while (scanner.hasNextLine()) {
                    String requestType = scanner.next();
                    String relativeURI = scanner.next();
                    String servletClass = scanner.next();
                    IRequestHandler o = null;
                    c = jarLoader.loadClass(servletClass, true);
                    switch(requestType){
                    case Protocol.POST:
                        o = (IPostHandler) c.newInstance();
                        break;
                    case Protocol.PUT:
                        o = (IPutHandler) c.newInstance();
                        break;
                    case Protocol.DELETE:
                        o = (IDeleteHandler) c.newInstance();
                        break;
                    case Protocol.GET:
                        o = (IGetHandler) c.newInstance();
                        break;
                    }
                    server.addPlugin(filename.substring(0, filename.length()-4), relativeURI, requestType, o);
                }
            } catch (ClassNotFoundException | InstantiationException | ClassCastException | IllegalAccessException e) {
                System.err.println("error loading plugin: " + filename + "\n" + e);
            }

            input.close();
        } catch (IOException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
    }

}
