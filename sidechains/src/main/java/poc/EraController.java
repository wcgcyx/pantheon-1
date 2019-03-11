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

import era.*;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * The controller to interact with ERA.
 */
public class EraController {

    /* The default address for finder deployed in rinkeby testnet. */
    private static final String DEFAULT_FINDER_ADDRESS = "0xb417f71159ff9ffd583c840d4717d384d5977f16";

    /* The default address for the top level era deployed in rinkeby testnet. */
//    private static final String DEFAULT_TOP_ERA_ADDRESS = "0x81d5fc4038318142f85131bff07c2405f38f16e2";

    // In typical deployments, there could be more than one root ERA. The make things simpler, we will
    // just use one.
    private String rootEraAddress;

    /* The empty address. */
    private static final String EMPTY_ADDRESS = "0x0000000000000000000000000000000000000000";

    /* The finder instance. */
    private Finder finder;

    /* The ERA wrapper interface instance. */
    private EthereumRegistrationAuthorityFactory factory;

    public static String NONE = "NONE";


    /**
     * Constructor.
     * @param tokenFile - File stores the infura token for rinkeby remote node.
     * @param privateKeyFile - File stores the private key.
     * @paeram rootEraAddressFile - File containing the address of the Root ERA.
     * @throws Exception - If Read or Write failure occurs.
     */
    public EraController (String tokenFile, String privateKeyFile, String rootEraAddressFile) throws Exception {
        // Read in Infura Project ID / token file
        BufferedReader br = new BufferedReader(new FileReader(tokenFile));
        String token = br.readLine();
        br.close();

        // Read in ECC private key which is related to an account with Ether in it on Rinkeby.
        br = new BufferedReader(new FileReader(privateKeyFile));
        String privateKey = br.readLine();
        br.close();

        // Read in the Root ERA
        br = new BufferedReader(new FileReader(rootEraAddressFile));
        this.rootEraAddress = br.readLine();
        br.close();

        Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/" + token));
        Credentials credentials = Credentials.create(privateKey);
        factory = new EthereumRegistrationAuthorityFactory(web3j, credentials);
        finder = factory.finderAtAddressRead(DEFAULT_FINDER_ADDRESS);
    }

    /**
     * Set key value pair in the domain info linked with the given domain.
     * @param domainName - The domain to search for.
     * @param key - The key.
     * @param value - The value.
     */
    public void setValue(String domainName, String key, String value) throws Exception {
        DomainInfo domainInfo = factory.domainInfoAtAddressWrite(getDomainInfoAddress(domainName));
        domainInfo.setValue(key, value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Get the value by the given key in the domain info linked with the given domain.
     * @param domainName - The domain to search for.
     * @param key - The key.
     * @return - The value.
     */
    public String getValue(String domainName, String key) {
        String value;
        try {
            DomainInfo domainInfo = factory.domainInfoAtAddressRead(getDomainInfoAddress(domainName));
            value = new String(domainInfo.getValue(key), StandardCharsets.UTF_8);
        } catch (Exception e) { return ""; }
        return value;
    }

    /**
     * Check if the given domain has a domain info attached to it.
     * @param domainName - The domain to search for.
     * @return - True if domain info existed, false otherwise.
     */
    public boolean hasDomainInfo(String domainName) throws Exception {
        return !getDomainInfoAddress(domainName).equalsIgnoreCase(EMPTY_ADDRESS);
    }

    /**
     * Add a domain info to the given domain.
     * @param domainName - The domain to add domain info to.
     * @return - True if successful, false otherwise.
     * @throws Exception - If Read or Write failure occurs.
     */
//    public boolean addDomainInfo(String domainName) throws Exception {
//        try {
//            //Divide the domainName.
//            int p3Index = domainName.lastIndexOf(".");
//            int p2Index = domainName.lastIndexOf(".", p3Index - 1);
//            int p1Index = domainName.lastIndexOf(".", p2Index - 1);
//            String p3DomainName = domainName.substring(p3Index + 1);
//            String p2DomainName = domainName.substring(p2Index + 1);
//            String p1DomainName = domainName.substring(p1Index + 1);
//            EthereumRegistrationAuthority era = factory.eraAtAddressWrite(this.rootEraAddress);
//            //Start check from p3 domain all the way to original domain.
//            if (era.hasDomain(p3DomainName)) {
//                if (p3DomainName.equals(domainName)) {
//                    era.addUpdateDomain(domainName, era.getAuthority(domainName),
//                            factory.deployDomainInfo().getContractAddress(), "0x01");
//                    return true;
//                } else {
//                    era = factory.eraAtAddressWrite(era.getAuthority(p3DomainName));
//                    if (era.hasDomain(p2DomainName)) {
//                        if (p2DomainName.equals(domainName)) {
//                            era.addUpdateDomain(domainName, era.getAuthority(domainName),
//                                    factory.deployDomainInfo().getContractAddress(), "0x01");
//                            return true;
//                        } else {
//                            era = factory.eraAtAddressWrite(era.getAuthority(p2DomainName));
//                            if (era.hasDomain(p1DomainName)) {
//                                if (p1DomainName.equals(domainName)) {
//                                    era.addUpdateDomain(domainName, era.getAuthority(domainName),
//                                            factory.deployDomainInfo().getContractAddress(), "0x01");
//                                    return true;
//                                } else {
//                                    era = factory.eraAtAddressWrite(era.getAuthority(p1DomainName));
//                                    if (era.hasDomain(domainName)) {
//                                        era.addUpdateDomain(domainName, era.getAuthority(domainName),
//                                                factory.deployDomainInfo().getContractAddress(), "0x01");
//                                        return true;
//                                    }
//                                    return false;
//                                }
//                            }
//                            return false;
//                        }
//                    }
//                    return false;
//                }
//            }
//            return false;
//        } catch (Exception e) { return false; }
//    }

    /**
     * Search the given domain, register if not found and register has been set to true.
     * @param domainName - The domain to search for.
     * @param register - True if need to register if the domain has not found.
     * @return - The last parent domain which has been registered (Will return the original domain if found).
     */
    public String searchAndRegisterDomains(String domainName, boolean register) {
        String lastFoundRegisteredDomain = NONE;
        try {
            EthereumRegistrationAuthority era = factory.eraAtAddressWrite(this.rootEraAddress);
            String[] domains = EraUtils.domainToDomainList(domainName);
            System.out.println("Searching for domain and parent-domains");
            for (int i = domains.length-1; i >= 0; i--) {
                String domain = domains[i];
                System.out.println(" Domain: " + domain);
                if (era.hasDomain(domain)) {
                    lastFoundRegisteredDomain = domain;
                    System.out.println("  found in ERA: " + era.getContractAddress());
                    String authorityAddress = era.getAuthority(domain);
                    System.out.println("  ERA which contains sub-domain information: " + authorityAddress);
                    String domainInfoAddress = era.getDomainInfo(domain);
                    System.out.println("  Domain Info contract: " + domainInfoAddress);
                    String ownerAddress = era.getDomainOwner(domain);
                    System.out.println("  Owner of this ERA entry: " + ownerAddress);
                    era = factory.eraAtAddressWrite(authorityAddress);

                    // There is an entry in the ERA for the domain, but there is no domain info contract.
                    // And the domain is the full domain.
                    if (register) {
                        if (domainInfoAddress.equalsIgnoreCase("0x0000000000000000000000000000000000000000") && i == 0) {
                            registerDomain(era, domains, 0);
                        }
                    }
                }
                else {
                    System.out.println("  not found in ERA: " + era.getContractAddress());
                    if (register) {
                        registerDomain(era, domains, i);
                        return domain;
                    }

                }
            }
            return lastFoundRegisteredDomain;




//            int p3Index = domainName.lastIndexOf(".");
//            int p2Index = domainName.lastIndexOf(".", p3Index - 1);
//            int p1Index = domainName.lastIndexOf(".", p2Index - 1);
//            String p3DomainName = domainName.substring(p3Index + 1);
//            String p2DomainName = domainName.substring(p2Index + 1);
//            String p1DomainName = domainName.substring(p1Index + 1);
//
//            System.out.println("Searching for domain and sub-domains");
//            System.out.println(" Domain: " + domainName);
//            System.out.println(" P1 Domain: " + p1DomainName);
//            System.out.println(" P2 Domain: " + p2DomainName);
//            System.out.println(" P3 Domain: " + p3DomainName);
//
//            List<String> domains = new ArrayList<>();
//            domains.add(p3DomainName);
//            domains.add(p2DomainName);
//            domains.add(p1DomainName);
//            domains.add(domainName);
//            EthereumRegistrationAuthority era = factory.eraAtAddressWrite(DEFAULT_TOP_ERA_ADDRESS);
//            //Start from search p3 domain.
//            if (era.hasDomain(p3DomainName)) {
//                era = factory.eraAtAddressWrite(era.getAuthority(p3DomainName));
//                if (era.hasDomain(p2DomainName)) {
//                    era = factory.eraAtAddressWrite(era.getAuthority(p2DomainName));
//                    if (era.hasDomain(p1DomainName)) {
//                        era = factory.eraAtAddressWrite(era.getAuthority(p1DomainName));
//                        if (era.hasDomain(domainName)) {
//                            return domainName;
//                        } else {
//                            if (register) registerDomain(era, domains.subList(3, domains.size()));
//                            return p1DomainName;
//                        }
//                    } else {
//                        if (register) registerDomain(era, domains.subList(2, domains.size()));
//                        return p2DomainName;
//                    }
//                } else {
//                    if (register) registerDomain(era, domains.subList(1, domains.size()));
//                    return p3DomainName;
//                }
//            } else {
//                if (register) registerDomain(era, domains);
//                return "";
//            }
        } catch (Exception e) {
            return ".ERROR";
        }
    }

    /**
     * Helper function to get domain info address attached to the given domain.
     * @param domainName - The domain to search for.
     * @return - The domain info address attached.
     * @throws Exception - If Read or Write failure occurs.
     */
    private String getDomainInfoAddress(String domainName) throws Exception {
        String[] domains = EraUtils.domainToDomainList(domainName, true);
        List<String> eraList = new ArrayList<>();
        eraList.add(this.rootEraAddress);

        System.out.println("Finder: Searching for Domain Info for domain: " + domainName);
        System.out.print(" Finder search parameters: " + domainName);
        for (int i = 1; i < domains.length; i++) {
            if (domains[i] != null) {
                System.out.print(", " + domains[i]);
            }
        }
        System.out.println();

        String domainInfoAddress = this.finder.resolveDomain(eraList, domainName, domains[1], domains[2], domains[3]);
        System.err.println(" Finder result: " + domainInfoAddress);
        return domainInfoAddress;
    }

    /**
     * Register the domains in the list to the era. Deploy a domain info contract for the full domain name.
     *
     * @param era - The ERA to register the first domain in.
     * @param domains - A list of domains need to be registered.
     * @param index - Offset into the domains array from which to start registering.
     * @throws Exception - If Read or Write failure occurs.
     */
    private void registerDomain(EthereumRegistrationAuthority era, String[] domains, int index) throws Exception {
        EthereumRegistrationAuthority newEra;
        for (int i = index; i >= 0; i--) {
            if (i > 0) {
                System.out.println("Registering and deploying ERA contract for: " + domains[i]);
                // This is an intermediate domain. For the purposes of the demo, put each domain level
                // in a different ERA.
                newEra = this.factory.deployEra();
                era.addUpdateDomain(domains[i], newEra.getContractAddress(), "0x01", "0x01");
                era = newEra;
            }
            else {
                System.out.println("Registering and deploying Domain Info contract for: " + domains[i]);
                // This is the full domain. Do not create a subordinate ERA. Deploy a Domain Info contract for the domain.
                DomainInfo newDomainInfo = this.factory.deployDomainInfo();
                era.addUpdateDomain(domains[i], "0x01", newDomainInfo.getContractAddress(), "0x01");
            }
            System.out.println("Done");
        }
    }
}