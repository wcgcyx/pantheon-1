/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package poc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * The main class.
 */
public class SideChainCreator {

    /**
     * Main process.
     * @param args - Token file name, Key file name, self domain name.
     * @throws Exception - If Read or Write failure occurs.
     */
    public static void main(String args[]) throws Exception {

        System.err.println("Sidechain Creator started with parameters:");

        if (args.length != 4) {
            System.err.println("Usage: poc tokenFileName keyFileName selfDomainName");
            return;
        }
        System.err.println(" Infura ProjectID / token file name: " + args[0]);
        System.err.println(" Private Key file name: " + args[1]);
        System.err.println(" Root ERA Address file name: " + args[2]);
        System.err.println(" Domain name of node: " + args[3]);

        // Determine available port number (range 1025 to 1124 inclusive)
        int port = 1025;
        ServerSocket serverSocket = null;
        for (int i = 0; i < 100; i++) {
            try {
                serverSocket = new ServerSocket(port);
                break;
            } catch (IOException e) {
                serverSocket = null;
            }
            port += 1;
        }

        if (serverSocket == null) {
            System.out.println("Can't allocate listening port number");
            return;
        }

        //Start ERA controller
        EraController eraController;
        try {
            eraController = new EraController(args[0], args[1], args[2]);
        } catch (Exception e) {
            System.err.println("ERA Controller load error");
            return;
        }

        Map<Integer, ChainBuilder> activeChains = new HashMap<>();
        Semaphore semaphore = new Semaphore(1);

        //Start Controller thread
        new Controller(activeChains, semaphore, eraController, args[3], port);

        //Start listening
        while (true) {
            Socket socket = serverSocket.accept();
            new Listener(socket, activeChains, semaphore, eraController, args[3]);   //Create a new thread to deal with the incoming connection.
        }
    }
}