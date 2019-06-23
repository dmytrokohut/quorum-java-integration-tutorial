package com.github.quorum.component.dto;

import lombok.Getter;
import lombok.Setter;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class APIResponse {

    private Map<String, Object> data = new HashMap<>();

    public static APIResponse newInstance(final TransactionReceipt txReceipt) {
        final APIResponse apiResponse = new APIResponse();
        apiResponse.data.put("transaction_hash", txReceipt.getTransactionHash());
        apiResponse.data.put("contract_address", txReceipt.getContractAddress());

        return apiResponse;
    }

    public static APIResponse newInstance(final String user) {
        final APIResponse apiResponse = new APIResponse();
        apiResponse.data.put("user", user);

        return apiResponse;
    }

    public static APIResponse newInstance(final Integer code, final String msg) {
        final APIResponse apiResponse = new APIResponse();
        apiResponse.data.put("status_code", code);
        apiResponse.data.put("description", msg);

        return apiResponse;
    }
}
