package era.internal;

import io.reactivex.Flowable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint16;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.0.1.
 */
public class AutoGeneratedEra extends Contract {
    private static final String BINARY = "608060405260008054600160a060020a0319163317905561060c806100256000396000f3fe608060405260043610610092577c010000000000000000000000000000000000000000000000000000000060003504630d8e6e2c811461009757806315d46474146100c35780637032a9b5146100ef5780637d760cac1461012d5780638da5cb5b14610173578063f114652c14610188578063f2581ea4146101b2578063f2fde38b146101fd578063ffae2c5b14610230575b600080fd5b3480156100a357600080fd5b506100ac61025a565b6040805161ffff9092168252519081900360200190f35b3480156100cf57600080fd5b506100ed600480360360208110156100e657600080fd5b503561025f565b005b3480156100fb57600080fd5b506101196004803603602081101561011257600080fd5b5035610352565b604080519115158252519081900360200190f35b34801561013957600080fd5b506101576004803603602081101561015057600080fd5b50356103b4565b60408051600160a060020a039092168252519081900360200190f35b34801561017f57600080fd5b506101576103cf565b34801561019457600080fd5b50610157600480360360208110156101ab57600080fd5b50356103de565b3480156101be57600080fd5b506100ed600480360360808110156101d557600080fd5b50803590600160a060020a0360208201358116916040810135821691606090910135166103f9565b34801561020957600080fd5b506100ed6004803603602081101561022057600080fd5b5035600160a060020a031661053e565b34801561023c57600080fd5b506101576004803603602081101561025357600080fd5b50356105c5565b600190565b600054600160a060020a0316331461027657600080fd5b60405181907fcce18fd5b44201ef757e5a5c2419f05436d510eaeef8bd79c4b8192ccfde1ca590600090a2600081815260016020526040902054600160a060020a0316156102db5760008181526001602052604090208054600160a060020a03191690555b600081815260026020526040902054600160a060020a0316156103155760008181526002602052604090208054600160a060020a03191690555b600081815260036020526040902054600160a060020a03161561034f5760008181526003602052604090208054600160a060020a03191690555b50565b600081815260016020526040812054600160a060020a031615801561038c5750600082815260026020526040902054600160a060020a0316155b80156103ad5750600082815260036020526040902054600160a060020a0316155b1592915050565b600090815260026020526040902054600160a060020a031690565b600054600160a060020a031681565b600090815260036020526040902054600160a060020a031690565b6000848152600360205260409020548490600160a060020a03168015156104285750600054600160a060020a03165b600160a060020a038116331461043d57600080fd5b60408051600160a060020a0380881682528087166020830152851681830152905187917f5d28b685a5507df96a8217266b9b466a4375cec79c06805292bf45ab9b1c950d919081900360600190a2600160a060020a0385166001146104c45760008681526001602052604090208054600160a060020a031916600160a060020a0387161790555b600160a060020a0384166001146104fd5760008681526002602052604090208054600160a060020a031916600160a060020a0386161790555b600160a060020a0383166001146105365760008681526003602052604090208054600160a060020a031916600160a060020a0385161790555b505050505050565b600054600160a060020a0316331461055557600080fd5b600160a060020a038116151561056a57600080fd5b60008054604051600160a060020a03808516939216917f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e091a360008054600160a060020a031916600160a060020a0392909216919091179055565b600090815260016020526040902054600160a060020a03169056fea165627a7a723058200cab5457739d2f450c5661062242985380a96f2a545ccbb15e1c9f149110621b0029";

    public static final String FUNC_GETVERSION = "getVersion";

    public static final String FUNC_REMOVEDOMAIN = "removeDomain";

    public static final String FUNC_HASDOMAIN = "hasDomain";

    public static final String FUNC_GETDOMAININFO = "getDomainInfo";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_GETDOMAINOWNER = "getDomainOwner";

    public static final String FUNC_ADDUPDATEDOMAIN = "addUpdateDomain";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final String FUNC_GETAUTHORITY = "getAuthority";

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event DOMAINADDUPDATE_EVENT = new Event("DomainAddUpdate", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Address>() {}));
    ;

    public static final Event DOMAINREMOVED_EVENT = new Event("DomainRemoved", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}));
    ;

    @Deprecated
    protected AutoGeneratedEra(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected AutoGeneratedEra(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected AutoGeneratedEra(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected AutoGeneratedEra(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteCall<BigInteger> getVersion() {
        final Function function = new Function(FUNC_GETVERSION, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint16>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> removeDomain(BigInteger _domainHash) {
        final Function function = new Function(
                FUNC_REMOVEDOMAIN, 
                Arrays.<Type>asList(new Uint256(_domainHash)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Boolean> hasDomain(BigInteger _domainHash) {
        final Function function = new Function(FUNC_HASDOMAIN,
                Arrays.<Type>asList(new Uint256(_domainHash)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<String> getDomainInfo(BigInteger _domainHash) {
        final Function function = new Function(FUNC_GETDOMAININFO,
                Arrays.<Type>asList(new Uint256(_domainHash)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<String> owner() {
        final Function function = new Function(FUNC_OWNER,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<String> getDomainOwner(BigInteger _domainHash) {
        final Function function = new Function(FUNC_GETDOMAINOWNER,
                Arrays.<Type>asList(new Uint256(_domainHash)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> addUpdateDomain(BigInteger _domainHash, String _domainAuthority, String _domainInfo, String _domainOwner) {
        final Function function = new Function(
                FUNC_ADDUPDATEDOMAIN,
                Arrays.<Type>asList(new Uint256(_domainHash),
                new Address(_domainAuthority),
                new Address(_domainInfo),
                new Address(_domainOwner)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> transferOwnership(String newOwner) {
        final Function function = new Function(
                FUNC_TRANSFEROWNERSHIP,
                Arrays.<Type>asList(new Address(newOwner)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<String> getAuthority(BigInteger _domainHash) {
        final Function function = new Function(FUNC_GETAUTHORITY,
                Arrays.<Type>asList(new Uint256(_domainHash)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = extractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, OwnershipTransferredEventResponse>() {
            @Override
            public OwnershipTransferredEventResponse apply(Log log) {
                EventValuesWithLog eventValues = extractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, log);
                OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
                typedResponse.log = log;
                typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
        return ownershipTransferredEventFlowable(filter);
    }

    public List<DomainAddUpdateEventResponse> getDomainAddUpdateEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = extractEventParametersWithLog(DOMAINADDUPDATE_EVENT, transactionReceipt);
        ArrayList<DomainAddUpdateEventResponse> responses = new ArrayList<DomainAddUpdateEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            DomainAddUpdateEventResponse typedResponse = new DomainAddUpdateEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._domainHash = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._domainAuthority = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse._orgInfo = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse._owner = (String) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<DomainAddUpdateEventResponse> domainAddUpdateEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, DomainAddUpdateEventResponse>() {
            @Override
            public DomainAddUpdateEventResponse apply(Log log) {
                EventValuesWithLog eventValues = extractEventParametersWithLog(DOMAINADDUPDATE_EVENT, log);
                DomainAddUpdateEventResponse typedResponse = new DomainAddUpdateEventResponse();
                typedResponse.log = log;
                typedResponse._domainHash = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._domainAuthority = (String) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse._orgInfo = (String) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse._owner = (String) eventValues.getNonIndexedValues().get(2).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<DomainAddUpdateEventResponse> domainAddUpdateEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DOMAINADDUPDATE_EVENT));
        return domainAddUpdateEventFlowable(filter);
    }

    public List<DomainRemovedEventResponse> getDomainRemovedEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = extractEventParametersWithLog(DOMAINREMOVED_EVENT, transactionReceipt);
        ArrayList<DomainRemovedEventResponse> responses = new ArrayList<DomainRemovedEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            DomainRemovedEventResponse typedResponse = new DomainRemovedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._domainHash = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<DomainRemovedEventResponse> domainRemovedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, DomainRemovedEventResponse>() {
            @Override
            public DomainRemovedEventResponse apply(Log log) {
                EventValuesWithLog eventValues = extractEventParametersWithLog(DOMAINREMOVED_EVENT, log);
                DomainRemovedEventResponse typedResponse = new DomainRemovedEventResponse();
                typedResponse.log = log;
                typedResponse._domainHash = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<DomainRemovedEventResponse> domainRemovedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DOMAINREMOVED_EVENT));
        return domainRemovedEventFlowable(filter);
    }

    @Deprecated
    public static AutoGeneratedEra load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new AutoGeneratedEra(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static AutoGeneratedEra load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new AutoGeneratedEra(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static AutoGeneratedEra load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new AutoGeneratedEra(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static AutoGeneratedEra load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new AutoGeneratedEra(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<AutoGeneratedEra> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(AutoGeneratedEra.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<AutoGeneratedEra> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(AutoGeneratedEra.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<AutoGeneratedEra> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(AutoGeneratedEra.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<AutoGeneratedEra> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(AutoGeneratedEra.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static class OwnershipTransferredEventResponse {
        public Log log;

        public String previousOwner;

        public String newOwner;
    }

    public static class DomainAddUpdateEventResponse {
        public Log log;

        public BigInteger _domainHash;

        public String _domainAuthority;

        public String _orgInfo;

        public String _owner;
    }

    public static class DomainRemovedEventResponse {
        public Log log;

        public BigInteger _domainHash;
    }
}
