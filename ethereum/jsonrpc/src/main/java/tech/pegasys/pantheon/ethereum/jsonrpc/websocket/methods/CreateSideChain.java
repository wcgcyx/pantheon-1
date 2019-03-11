/*
 * Copyright 2018 ConsenSys AG.
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

package tech.pegasys.pantheon.ethereum.jsonrpc.websocket.methods;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.results.Quantity;
import tech.pegasys.pantheon.ethereum.jsonrpc.websocket.methods.era.EthereumRegistrationAuthorityFactory;
import tech.pegasys.pantheon.ethereum.jsonrpc.websocket.methods.era.Finder;
import tech.pegasys.pantheon.ethereum.jsonrpc.websocket.subscription.SubscriptionManager;
import tech.pegasys.pantheon.ethereum.jsonrpc.websocket.subscription.request.SubscriptionRequestMapper;

import java.util.ArrayList;
import java.util.List;

public class CreateSideChain extends AbstractSubscriptionMethod {

    private static final String DEFAULT_TOP_ERA_ADDRESS = "0x81d5fc4038318142f85131bff07c2405f38f16e2";
    private static final String DEFAULT_FINDER_ADDRESS = "0xb417f71159ff9ffd583c840d4717d384d5977f16";
    private static final String DEFAULT_TOKEN = "3fb2c983929549e6b75fb252ed9a62a8";

    CreateSideChain(
            final SubscriptionManager subscriptionManager, final SubscriptionRequestMapper mapper) {
        super(subscriptionManager, mapper);
    }

    @Override
    public String getName() {
        return "sidechains_create";
    }

    @Override
    public JsonRpcResponse response(final JsonRpcRequest req) {
        Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/" + DEFAULT_TOKEN));
        EthereumRegistrationAuthorityFactory factory = new EthereumRegistrationAuthorityFactory(web3j);
        Finder finder = factory.finderAtAddressRead(DEFAULT_FINDER_ADDRESS);
        String address;
        try {
            address = getDomainInfoAddress(finder, String.valueOf(req.getParams()[0]));
        } catch (Exception e) {
            address = "Exception";
        }

        return new JsonRpcSuccessResponse(req.getId(), address);
    }

    private String getDomainInfoAddress(Finder finder, String domainName) throws Exception {
        String p1DomainName = domainName.substring(domainName.indexOf(".") + 1);
        String p2DomainName = p1DomainName.substring(p1DomainName.indexOf(".") + 1);
        String p3DomainName = p2DomainName.substring(p2DomainName.indexOf(".") + 1);
        List<String> eraList = new ArrayList<>();
        eraList.add(DEFAULT_TOP_ERA_ADDRESS);
        return finder.resolveDomain(eraList,
                domainName, p1DomainName, p2DomainName, p3DomainName);
    }
}
