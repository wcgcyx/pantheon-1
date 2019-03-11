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

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

/**
 * This class is used to interact with user.
 */
public class Controller implements Runnable {
    // TODO create this type of constant for all other commands - and use them in the code.
    public static final String CMD_INFO = "info ";


    /* The domain name of self. */
    private String selfDomainName;

    /* The era controller. */
    private EraController eraController;

    /* A map of chains that are active. */
    private Map<Integer, ChainBuilder> activeChains;

    /* Semaphore used to synchronise threads. */
    private Semaphore semaphore;

    /* Address of self. */
    private InetSocketAddress myNetworkAddress = null;

    /* The scanner to read user input. */
    private Scanner scanner;

    /**
     * Constructor.
     * @param activeChains - A map of chains that are active.
     * @param semaphore - Semaphore used to synchronise threads.
     * @param eraController - The era controller.
     * @param selfDomainName - The domain name of self.
     * @throws Exception - If Read or Write failure occurs.
     */
    public Controller (Map<Integer, ChainBuilder> activeChains, Semaphore semaphore,
                       EraController eraController, String selfDomainName, int myPort) throws Exception {
        System.out.println("Starting controller...");
        this.activeChains = activeChains;
        this.semaphore = semaphore;
        this.eraController = eraController;
        this.selfDomainName = selfDomainName;
        this.myNetworkAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), myPort);
        this.scanner = new Scanner(System.in);

        // Search the ERA system to see if the Domain Info contract for the domain can be found.
        boolean domainInfoFound = eraController.hasDomainInfo(selfDomainName);
        if (!domainInfoFound) {
            // The domain info couldn't be found using Finder.
            // See whether any of the parent domains are registered.
            String registeredDomain = eraController.searchAndRegisterDomains(selfDomainName, false);
            if (registeredDomain.equalsIgnoreCase(EraController.NONE)) {
                System.out.println("No registered domain or parent domains found");
                System.out.println("Do you want to register the domain and parent domains? (yes/no)");
            }
            else {
                System.out.println("Domain and parent domains registered to: " + registeredDomain);
                System.out.println("Do you want to register domains below this? (yes/no)");
            }
            String command = scanner.nextLine();
            if (!command.equals("yes")) {
                throw new Exception();
            }
            //Search domain with registration.
            eraController.searchAndRegisterDomains(selfDomainName, true);
        }

        //Configure my IP in ERA
        System.out.println("Set up domain info...");
        String domainInfoIP = eraController.getValue(selfDomainName, "ip");
        if (!this.myNetworkAddress.getHostString().equals(domainInfoIP)) {
            try {
                eraController.setValue(selfDomainName, "ip", this.myNetworkAddress.getHostString());
            } catch (Exception ex) {
                throw new Exception("Exception whilst attempting to set value (IP) on DomainInfo contract", ex);
            }

            // Check that the value was successfully set.
            domainInfoIP = eraController.getValue(selfDomainName, "ip");
            if (!this.myNetworkAddress.getHostString().equals(domainInfoIP)) {
                throw new Exception("The IP address has not been successfully registered in the ERA system.");
            }
        }

        //Configure my port in ERA
        System.out.println("Set up port number info...");
        String domainInfoPort = eraController.getValue(selfDomainName, "port");
        if (!domainInfoPort.equals(Integer.toString(this.myNetworkAddress.getPort()))) {
            try {
                eraController.setValue(selfDomainName, "port", Integer.toString(this.myNetworkAddress.getPort()));
            } catch (Exception ex) {
                throw new Exception("Exception whilst attempting to set value (port) on DomainInfo contract", ex);
            }

            // Check that the value was successfully set.
            domainInfoPort = eraController.getValue(selfDomainName, "port");
            if (!domainInfoPort.equals(Integer.toString(this.myNetworkAddress.getPort()))) {
                throw new Exception("The port number has not been successfully registered in the ERA system.");
            }
        }

        Thread thread = new Thread(this, "Controller");
        thread.start();
        System.out.println("Chain creator deployed, with address " + this.myNetworkAddress.toString());
    }

    /**
     * Thread running schedule.
     */
    public void run() {
        try {
            while (true) {
                String command = scanner.nextLine();
                semaphore.acquire();
                if (command.startsWith("connect ")) {
                    // TODO prevent connecting to the domain name of this node. That is, don't try to connect to self.
                    //Command starts with connect, try to start a sender to connect to the given peers.
                    Sender sender = new Sender(command.substring(8), eraController);
                    if (sender.send(activeChains, myNetworkAddress.getHostString(), scanner, selfDomainName)) {
                        //Connection is established.
                        System.out.println("Established blockchain with peer.");
                    } else {
                        //Connection failed.
                        System.out.println("Something went wrong, try again.");
                    }
                } else if (command.startsWith("add ")) {
                    // TODO prevent connecting to the domain name of this node. That is, don't try to connect to self.
                    //Command starts with add, get chain id and peers' domains.
                    command = command.substring(4);
                    String id = command.substring(0, command.indexOf(" "));
                    String domainNames = command.substring(command.indexOf(" ") + 1);
                    int chainID;
                    try {
                        chainID = Integer.parseInt(id);
                        if (!this.activeChains.containsKey(chainID)) {
                            System.out.println(" Sidechain ID invalid");
                        }
                        else {
                            //If the given id is in active chains, start a sender to add peers.
                            Sender sender = new Sender(domainNames, eraController);
                            sender.addPeers(activeChains, myNetworkAddress.getHostString(), chainID, selfDomainName);
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                } else if (command.equals("ls")) {
                    //List all active chains.
                    System.out.println("Active chains:");
                    for (Integer id : activeChains.keySet()) {
                        System.out.println(String.format("Chain\t%d\tRPC Port\t%d",
                                id, activeChains.get(id).getRpcPort()));
                    }
                } else if (command.startsWith("kill ")) {
                    //Kill the given chain by its chain id.
                    int id;
                    try {
                        id = Integer.parseInt(command.substring(5));
                        ChainBuilder chain = activeChains.get(id);
                        if (chain == null) {
                            System.out.println(" Sidechain ID invalid");
                        }
                        else {
                            Process pr = chain.getProcess();
                            pr.destroy();
                            activeChains.remove(id);
                            System.out.println(id + " killed.");
                        }
                    } catch (Exception e) {
                        System.out.println("Error killing chain.");
                    }
                } else if (command.startsWith("set ")) {
                    //Set the ip address to be the IP provided.
                    String newIP = command.substring(4);
                    String domainInfoIP = this.eraController.getValue(this.selfDomainName, "ip");
                    if (!newIP.equals(domainInfoIP)) {
                        try {
                            this.eraController.setValue(this.selfDomainName, "ip", newIP);
                            System.out.println("Set IP to be: " + this.myNetworkAddress.getHostString());
                            this.myNetworkAddress = new InetSocketAddress(newIP, this.myNetworkAddress.getPort());
                        } catch (Exception ex) {
                            System.out.println("Set ip failed, ip remains to be: " + domainInfoIP);
                            this.myNetworkAddress = new InetSocketAddress(domainInfoIP, this.myNetworkAddress.getPort());
                        }
                    } else {
                        System.out.println("The IP in the DomainInfo contract is already: " + myNetworkAddress.getHostString());
                    }
                } else if (command.startsWith(CMD_INFO)) {
                    int id;
                    try {
                        id = Integer.parseInt(command.substring(CMD_INFO.length()));
                        System.out.println("Sidechain ID: " + id);
                        ChainBuilder chain = activeChains.get(id);
                        if (chain == null) {
                            System.out.println(" Sidechain ID invalid");
                        }
                        else {
                            int port = chain.getRpcPort();
                            System.out.println("port: " + port);
                            Web3j web3j = Web3j.build(new HttpService("http://localhost:" + port + "/"));
                            System.out.println(" NetPeerCount: " + web3j.netPeerCount().send().getQuantity());
                            System.out.println(" NetVersion: " + web3j.netVersion().send().getNetVersion());
                            System.out.println(" BlockNumber: " + web3j.ethBlockNumber().send().getBlockNumber());
                            System.out.println(" Eth Protocol Version: " + web3j.ethProtocolVersion().send().getProtocolVersion());
                            System.out.println(" Web3 Client Version: " + web3j.web3ClientVersion().send().getWeb3ClientVersion());
                            web3j.shutdown();
                        }
                    } catch (Exception e) {
                        System.out.println("Error connecting to pantheon node: " + e.toString());
                    }

                } else if (command.startsWith("?") || command.startsWith("help")) {
                    showHelp();
                } else {
                    System.out.println("Unknown command: " + command);
                    showHelp();
                }

                semaphore.release();
            }
        } catch (Exception e) {
            scanner.close();
            System.out.println("Controller Exiting...");
        }
    }

    private static void showHelp() {
        System.out.println("Available commands:");
        System.out.println(" connect  domain1 [domain2] .... [domainN]");
        System.out.println("  Establish a sidechain between this node and one or more other nodes indicated by domain names.");
        System.out.println(" add sidechainID domain1 [domain2] .... [domainN]");
        System.out.println("  Add one or more nodes to the sidechain specified by sidechainID.");
        System.out.println(" ls");
        System.out.println("  List all sidechains.");
        System.out.println(" kill sidechainID");
        System.out.println("  Stop the sidechain node belonging to the specified sidechainID.");
        System.out.println(" kill IP");
        System.out.println("  Set the IP address of this node.");
        System.out.println(" " + CMD_INFO + "sidechainID");
        System.out.println("  Connect to the local Pantheon node for the SidechainID and dump information about a sidechain.");
        System.out.println(" ?");
        System.out.println("  Show this help screen.");
    }


}