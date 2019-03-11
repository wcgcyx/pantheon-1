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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The class is used to connect to remote peers.
 */
public class Sender {

    /* The max attempts used in chain id negotiation. */
    private final static int MAX_ATTEMPTS = 10;

    /* A list of ips to connect to. */
    private List<InetSocketAddress> peerIPs;

    /**
     * Constructor.
     * @param domainNames - The domain names seperated by space.
     * @param eraController - The era controller.
     */
    public Sender(String domainNames, EraController eraController) {
        //Split the domain names and add to list.
        domainNames = domainNames.replaceAll("\\s{2,}", " ").trim();
        String domains[] = domainNames.split(" ");
        peerIPs = new ArrayList<>();
        for (String domain : domains) {
            //For each domain, get the ip address from era.
            String address = eraController.getValue(domain, "ip");
            String port = eraController.getValue(domain, "port");
            int portNo = -1;

            // TODO: Not sure validIP test is required. If it is wrong then it just won't work anyway.
            if (validIP(address)) {
                try {
                    portNo = Integer.parseInt(port);
                    //IP is valid, add to the list.
                    peerIPs.add(new InetSocketAddress(address, portNo));
                } catch (NumberFormatException e) {
                    System.out.println("Port: " + port + " is not valid");
                }
            } else {
                System.out.println("Domain: " + domain + " is not valid");
            }
        }
    }

    /**
     * Add a peer to the existing chain.
     * @param activeChains - A list of chains that are in active.
     * @param ip - The ip address of self.
     * @param chainID - The chain id to add to.
     * @param selfDomainName - the domain name of self.
     */
    public void addPeers(Map<Integer, ChainBuilder> activeChains, String ip, int chainID, String selfDomainName) {
        List<Socket> sockets = new ArrayList<>();
        List<BufferedReader> ins = new ArrayList<>();
        List<PrintWriter> outs = new ArrayList<>();

        //For each ip, connect to the socket.
        for (InetSocketAddress peerIP : peerIPs) {
            Socket socket;
            BufferedReader in;
            PrintWriter out;
            try {
                socket = new Socket(peerIP.getHostString(), peerIP.getPort());
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (Exception e) { continue; }
            sockets.add(socket);
            ins.add(in);
            outs.add(out);
        }

        //For each peer, send the chain id and add to chain only if it agrees.
        try {
            int count = 0;
            for (int i = 0; i < sockets.size(); i++) {
                if (negotiateChainIDSub(ins.get(i), outs.get(i), selfDomainName, chainID)) {
                    try {
                        outs.get(i).println(activeChains.get(chainID).getMessage(ip));
                        if (ins.get(i).readLine().equals("ACK")) {
                            count++;
                            continue;
                        }
                    } catch (Exception e) { System.err.println(e.getMessage()); }
                }
                System.out.println("Peer " + i + " is unable to connect");
            }
            System.out.println("Chain " + chainID + " add " + count + " peers");
        } finally {
            for (int i = 0; i < sockets.size(); i++) {
                try {
                    ins.get(i).close();
                    outs.get(i).close();
                    sockets.get(i).close();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    /**
     * Create a private chain by send requests to peers.
     * @param activeChains - A list of chains that are in active.
     * @param ip - The ip address of self.
     * @param scanner - The scanner to read user input.
     * @param selfDomainName - The domain of self.
     * @return - True if successful, false otherwise.
     */
    public boolean send(Map<Integer, ChainBuilder> activeChains, String ip, Scanner scanner, String selfDomainName) {

        if (peerIPs.size() == 0) return false;

        List<Socket> sockets = new ArrayList<>();
        List<BufferedReader> ins = new ArrayList<>();
        List<PrintWriter> outs = new ArrayList<>();
        int chainID;

        for (InetSocketAddress peerIP : peerIPs) {
            Socket socket;
            BufferedReader in;
            PrintWriter out;
            try {
                socket = new Socket(peerIP.getHostString(), peerIP.getPort());
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (Exception e) {
                System.out.println(e.toString());
                continue;
            }
            sockets.add(socket);
            ins.add(in);
            outs.add(out);
        }

        //Negotiate the chain id with the first peer.
        chainID = negotiateChainIDMain(ins.get(0), outs.get(0), activeChains, selfDomainName);

        //For the rest of the peers, only attempt once and add peers that agree on this chain id.
        try {
            if (chainID != -1) {
                ChainBuilder builder = new ChainBuilder(chainID, selfDomainName);
                try {
                    builder.bootnode();
                } catch (Exception e) {
                    System.out.println(e.toString());
                    return false;
                }
                try {
                    Thread.sleep(1000);
                    //Confirmation for demo purpose
                    System.out.println("Confirm connection to peer with chain " + chainID + ": (yes)");
                    if (!scanner.nextLine().equals("yes"))
                        throw new Exception();
                    outs.get(0).println(builder.getMessage(ip));
                    if (!ins.get(0).readLine().equals("ACK")) throw new Exception();
                } catch (Exception e) {
                    builder.getProcess().destroy();
                    return false;
                }
                //Connect peers
                int count = 1;
                for (int i = 1; i < sockets.size(); i++) {
                    if (negotiateChainIDSub(ins.get(i), outs.get(i), selfDomainName, chainID)) {
                        try {
                            outs.get(i).println(builder.getMessage(ip));
                            if (ins.get(i).readLine().equals("ACK")) {
                                count++;
                                continue;
                            }
                        } catch (Exception e) { System.err.println(e.getMessage()); }
                    }
                    System.out.println("Peer " + i + " is unable to connect");
                }
                activeChains.put(chainID, builder);
                System.out.println("Chain " + chainID + " is active with " + count + " peers");
            } else {
                return false;
            }
        } finally {
            for (int i = 0; i < sockets.size(); i++) {
                try {
                    ins.get(i).close();
                    outs.get(i).close();
                    sockets.get(i).close();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }

        return true;
    }

    /**
     * Negotiate chain id for one-time attampt.
     * @param in - The input to read from peer.
     * @param out - The output to write to peer.
     * @param selfDomainName - The domain of self.
     * @param chainID - The chain id to send.
     * @return - True if agreed, false otherwise.
     */
    private boolean negotiateChainIDSub(BufferedReader in, PrintWriter out, String selfDomainName, int chainID) {
        try {
            //To verify
            if (in.readLine().equals("ACK")) {
                out.println(selfDomainName);
            }
            if (in.readLine().equals("ACK")) {
                out.println(chainID);
                return in.readLine().equals("ACK");
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    /**
     * Negotiate chain id for multiple attempts.
     * @param in - The input to read from peer.
     * @param out - The output to write to peer.
     * @param activeChains - A list of active chains.
     * @param selfDomainName - The domain of self.
     * @return - The chain id both parties agree on, -1 not found.
     */
    private int negotiateChainIDMain(BufferedReader in, PrintWriter out, Map<Integer, ChainBuilder> activeChains,
                                     String selfDomainName) {
        try {
            //To verify
            if (in.readLine().equals("ACK")) out.println(selfDomainName);
            if (in.readLine().equals("ACK")) {
                int attempt = 0;
                while (attempt++ < MAX_ATTEMPTS) {
                    //Generate a id from 10 to 100000.
                    int id = ThreadLocalRandom.current().nextInt(10, 100000);
                    if (!activeChains.keySet().contains(id)) {
                        //The id is not used in any active chain, continue.
                        out.println(id);
                        if (in.readLine().equals("ACK")) {
                            return id;
                        }
                    }
                }
            }
        } catch (Exception e) {
            return -1;
        }
        return -1;
    }

    /**
     * To check if the given ip is valid.
     * @param ip - The given ip in string format.
     * @return - True if it is valid, false otherwise.
     * @source - https://stackoverflow.com/questions/4581877/validating-ipv4-string-in-java
     */
    private static boolean validIP (String ip) {
        try {
            if ( ip == null || ip.isEmpty() ) {
                return false;
            }

            String[] parts = ip.split( "\\." );
            if ( parts.length != 4 ) {
                return false;
            }

            for ( String s : parts ) {
                int i = Integer.parseInt( s );
                if ( (i < 0) || (i > 255) ) {
                    return false;
                }
            }
            if ( ip.endsWith(".") ) {
                return false;
            }

            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}