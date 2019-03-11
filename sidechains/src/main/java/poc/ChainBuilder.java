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
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.ServerSocket;

/**
 * This class is used to build a private chain by
 * starting a pantheon client.
 */
public class ChainBuilder {

    /* The chain id of this private chain. */
    private int chainID;

    /* The RPC port for this chain. */
    private int rpcPort;

    /* The discovery port for this chain. */
    private int discPort;

    /* The process of the pantheon client. */
    private Process pr;

    /* Domain name of organisation I represent */
    private String selfDomainName;

    /* Potentially configurable path to pantheon executable */
    private String pantheonExecutable;

    /**
     * Constructor.
     * @param chainID - The chain id of this private chain.
     */
    public ChainBuilder(int chainID, String selfDomainName) {
        this.chainID = chainID;
        this.selfDomainName = selfDomainName;
        rpcPort = getPort();
        discPort = getPort();
        pantheonExecutable = calculatePantheonExecutable();
    }

    /**
     * Get the RPC port for this chain.
     * @return - The RPC port.
     */
    public int getRpcPort() {
        return rpcPort;
    }

    /**
     * Get the process of the pantheon client.
     * @return - The process.
     */
    public Process getProcess() {
        return pr;
    }

    private String dataDirectory() {
        return "./chain" + chainID + "/" + selfDomainName;
    }

    /**
     * Read configurable pantheon path. Fall back to 'pantheon' in absence of file.
     * Fallback is used with docker.
     *
     * Example contents of pantheon_executable.txt
     * ./../pantheon-0.8.5/bin/pantheon
     */
    private String calculatePantheonExecutable() {
        File file = new File("./pantheon_executable.txt");
        if (file.exists()) {
            String path = "";
            try {
                BufferedReader br = null;
                br = new BufferedReader(new FileReader(file));
                path = br.readLine();
                br.close();
                return path;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "pantheon";
    }

    /**
     * Write the genesis block file.
     * @throws Exception - If file can't be created.
     */
    private void writeGenesisFile() throws Exception {
        //Write file under ./chainID
        File dir = new File(dataDirectory());
        if (!dir.mkdirs()) throw new Exception();
        PrintWriter out = new PrintWriter(dataDirectory() + "/genesis.json");
        out.println("{\n" +
                "  \"config\": {\n" +
                "    \"ethash\" : {\n" +
                "    }\n" +
                "  },\n" +
                "  \"nonce\": \"0x42\",\n" +
                "  \"gasLimit\": \"0x1000000\",\n" +
                "  \"difficulty\": \"0x10000\",\n" +
                "  \"alloc\": {}\n" +
                "}");
        out.flush();
        out.close();
        System.out.println("RPC Port: " + rpcPort);
    }

    /**
     * Start the pantheon client by bootnode.
     * @throws Exception - If Read or Write failure occurs.
     */
    public void bootnode() throws Exception {
        if (rpcPort == -1 || discPort == -1) throw new Exception();
        writeGenesisFile();
        //Init Genesis block
        ProcessBuilder pb = new ProcessBuilder(pantheonExecutable, "--genesis", dataDirectory() + "/genesis.json", "--network-id", "" + chainID,
                "--datadir", "chain" + chainID + "/" + selfDomainName, "export-pub-key", dataDirectory() + "/bootnode");
        pb.directory(new File("."));
        Process pr = pb.start();
        pr.waitFor();
        pb = new ProcessBuilder(pantheonExecutable, "--bootnodes", "--genesis", dataDirectory() + "/genesis.json", "--network-id", "" + chainID,
                "--datadir", dataDirectory(), "--rpc-enabled", "--rpc-listen", "127.0.0.1:" + rpcPort, "--rpc-api", "ADMIN,ETH,WEB3,NET", "--p2p-listen", "127.0.0.1:" + discPort);
        pb.directory(new File("."));
        this.pr = pb.start();
        System.out.println("Launched pantheon with: " + pb.command());
    }

    /**
     * Start the pantheon client by joining an existing chain.
     * @param enodeAddress - The enode address to launch with.
     * @throws Exception - If Read or Write failure occurs.
     */
    public void join(String enodeAddress) throws Exception {
        if (rpcPort == -1 || discPort == -1) throw new Exception();
        writeGenesisFile();
        final String dir = System.getProperty("user.dir");
        ProcessBuilder pb = new ProcessBuilder(pantheonExecutable, "--genesis", dataDirectory() + "/genesis.json", "--network-id", "" + chainID,
                "--datadir", dataDirectory(), "--rpc-enabled", "--rpc-listen", "127.0.0.1:" + rpcPort, "--rpc-api", "ADMIN,ETH,WEB3,NET", "--p2p-listen", "127.0.0.1:" + discPort,
                "--bootnodes", enodeAddress);
        pb.directory(new File("."));
        pr = pb.start();
        System.out.println("Launched pantheon with: " + pb.command());
    }

    /**
     * Get the enode address of this chain.
     * @require Used only when this chainbuilder is bootnode.
     * @param ip - The ip address of self.
     * @return - The enode address.
     * @throws Exception - If Read of Write failure occurs.
     */
    public String getMessage(String ip) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(dataDirectory() + "/bootnode"));
        String enode = br.readLine();
        br.close();
        enode = enode.substring(2);
        return "enode://" + enode + "@" + ip + ":" + discPort;
    }

    /**
     * Get a free port that can be used.
     * @return - A free port or -1 if can't find any.
     */
    private int getPort() {
        try {
            ServerSocket s = new ServerSocket(0);
            int port = s.getLocalPort();
            s.close();
            return port;
        } catch (Exception e) {
            return -1;
        }
    }
}
