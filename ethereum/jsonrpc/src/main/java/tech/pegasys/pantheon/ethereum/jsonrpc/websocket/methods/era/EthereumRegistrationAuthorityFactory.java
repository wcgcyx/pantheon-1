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
package tech.pegasys.pantheon.ethereum.jsonrpc.websocket.methods.era;

import tech.pegasys.pantheon.ethereum.jsonrpc.websocket.methods.era.internal.AutoGeneratedDomainInfo;
import tech.pegasys.pantheon.ethereum.jsonrpc.websocket.methods.era.internal.AutoGeneratedEra;
import tech.pegasys.pantheon.ethereum.jsonrpc.websocket.methods.era.internal.AutoGeneratedFinder;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

/**
 * Ethereum Registration Authority main class. This class can be used to deploy contracts and
 * gain access to read or writable instances of the contracts: EthereumRegistrationAuthority,
 * DomainInfo, and Finder.
 */
public class EthereumRegistrationAuthorityFactory {

    private Web3j web3j;
    private Credentials credentials;
    private ContractGasProvider contractGasProvider;

    public EthereumRegistrationAuthorityFactory(Web3j web3j, Credentials credentials) {
        this(web3j, credentials, new DefaultGasProvider());
    }

    public EthereumRegistrationAuthorityFactory(Web3j web3j) {
        this(web3j, null, new DefaultGasProvider());
    }

    public EthereumRegistrationAuthorityFactory(Web3j web3j, ContractGasProvider contractGasProvider) {
        this(web3j, null, contractGasProvider);
    }

    public EthereumRegistrationAuthorityFactory(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        this.web3j = web3j;
        this.credentials = credentials;
        this.contractGasProvider = contractGasProvider;
    }


    public void setGasProvider(ContractGasProvider contractGasProvider) {
        this.contractGasProvider = contractGasProvider;
    }


    public EthereumRegistrationAuthority deployEra() throws Exception {
        if (this.credentials == null) {
            throw new RuntimeException("Unexpectedly no credentials available for deployment of ERA");
        }
        return new EthereumRegistrationAuthority(AutoGeneratedEra.deploy(this.web3j, this.credentials, this.contractGasProvider));

    }

    public EthereumRegistrationAuthority eraAtAddressRead(String contractAddress) {
        return new EthereumRegistrationAuthority(AutoGeneratedEra.load(
                contractAddress, this.web3j, new ReadonlyTransactionManager(this.web3j, contractAddress), this.contractGasProvider));
    }

    public EthereumRegistrationAuthority eraAtAddressWrite(String contractAddress) {
        if (this.credentials == null) {
            throw new RuntimeException("Unexpectedly no credentials available to create a writable instance of ERA");
        }
        return new EthereumRegistrationAuthority(AutoGeneratedEra.load(
                contractAddress, this.web3j, new RawTransactionManager(this.web3j, this.credentials), this.contractGasProvider));
    }

    public DomainInfo deployDomainInfo() throws Exception {
        if (this.credentials == null) {
            throw new RuntimeException("Unexpectedly no credentials available for deployment of DomainInfo");
        }
        return new DomainInfo(AutoGeneratedDomainInfo.deploy(this.web3j, this.credentials, this.contractGasProvider));
    }

    public DomainInfo domainInfoAtAddressRead(String contractAddress) {
        return new DomainInfo(AutoGeneratedDomainInfo.load(
            contractAddress, this.web3j, new ReadonlyTransactionManager(this.web3j, contractAddress), this.contractGasProvider));
    }

    public DomainInfo domainInfoAtAddressWrite(String contractAddress) {
        if (this.credentials == null) {
            throw new RuntimeException("Unexpectedly no credentials available to create a writable instance of DomainInfo");
        }
        return new DomainInfo(AutoGeneratedDomainInfo.load(
            contractAddress, this.web3j, new RawTransactionManager(this.web3j, this.credentials), this.contractGasProvider));
    }

    public Finder deployFinder() throws Exception {
        if (this.credentials == null) {
            throw new RuntimeException("Unexpectedly no credentials available for deployment of Finder");
        }
        return new Finder(AutoGeneratedFinder.deploy(this.web3j, this.credentials, this.contractGasProvider));
    }

    public Finder finderAtAddressRead(String contractAddress) {
        return new Finder(AutoGeneratedFinder.load(
                contractAddress, this.web3j, new ReadonlyTransactionManager(this.web3j, contractAddress), this.contractGasProvider));
    }
}