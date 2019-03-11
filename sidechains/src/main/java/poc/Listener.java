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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * The class is used to handle incoming connection.
 */
public class Listener implements Runnable {

    /* The default error message. */
    private static final String ERROR = "Error communicating with peers...";

    /* The era controller. */
    private EraController eraController;

    /* A map of chains that are active. */
    private Map<Integer, ChainBuilder> activeChains;

    /* Semaphore used to synchronise threads. */
    private Semaphore semaphore;

    /* The socket used to handle the incoming connection. */
    private Socket socket;

    private String selfDomainName;

    /**
     * Constructor.
     * @param socket - The socket used to handle the incoming connection.
     * @param activeChains - A map of chains that are active.
     * @param semaphore - Semaphore used to synchronise threads.
     * @param eraController - The era controller.
     * @param selfDomainName - Domain name of the organisation I represent
     */
    public Listener (Socket socket, Map<Integer, ChainBuilder> activeChains, Semaphore semaphore, EraController eraController, String selfDomainName) {
        System.out.println("New incoming connection...");
        this.socket = socket;
        this.activeChains = activeChains;
        this.semaphore = semaphore;
        this.eraController = eraController;
        this.selfDomainName = selfDomainName;
        Thread thread = new Thread(this, "Listener");
        thread.start();
    }

    /**
     * Thread running schedule.
     */
    public void run() {

        BufferedReader in;
        PrintWriter out;
        int chainID;

        try {
            semaphore.acquire();
        } catch (InterruptedException ie) {
            System.out.println("ERROR: " + ie.toString());
            return;
        }

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (Exception e) {
            System.err.println(e.toString());
            semaphore.release();
            return;
        }

        //Try to negotiate a chain id with peer.
        chainID = negotiateChainID(in, out);

        try {
            if (chainID != -1) {
                //Chain id is valid. Try to build a private chain.
                ChainBuilder builder = new ChainBuilder(chainID, selfDomainName);
                String enodeAddress;
                try {
                    //Get the enode address.
                    enodeAddress = in.readLine();
                    out.println("ACK");
                } catch (Exception e) {
                    System.err.println(ERROR);
                    semaphore.release();
                    return;
                }
                try {
                    //Join the existing chain.
                    builder.join(enodeAddress);
                } catch (Exception e) {
                    System.err.println(ERROR);
                    semaphore.release();
                    return;
                }
                //Add to the map.
                activeChains.put(chainID, builder);
                System.out.println("Chain " + chainID + " is active");
            } else {
                System.err.println(ERROR);
            }
        } finally {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        semaphore.release();
    }

    /**
     * Negotiate the chain id with peer.
     * @param in - The input to read from.
     * @param out - The output to write to.
     * @return - The chain id or -1 if negotiation fails.
     */
    private int negotiateChainID (BufferedReader in, PrintWriter out) {
        out.println("ACK");
        boolean trigger = true;
        int id = -1;
        try {
            //To verify, this won't cost any gas.
            String peerDomain = in.readLine();
            String ip = eraController.getValue(peerDomain, "ip");
            String ipFromSocket = socket.getRemoteSocketAddress().toString();
            if (!ip.equals(ipFromSocket.substring(1, ipFromSocket.indexOf(":")))) {
                System.out.println("ERROR: Contract address doesn't match connection socket address.");
                return -1;
            }

            out.println("ACK");
            while (trigger) {
                id = Integer.parseInt(in.readLine());
                if (activeChains.containsKey(id)) {
                    out.println("NAK");
                } else {
                    out.println("ACK");
                    trigger = false;
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.toString());
            return -1;
        }
        return id;
    }
}