package com.github.quorum.interfacee;

import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;

public interface TransactionManager {

    /**
     * Send transaction to blockchain and wait for transaction receipt.
     *
     * @param gasPrice
     *          - amount of gas for each operation executed in smart-contract
     *          NOTE: for Quorum blockchain this parameter will always be equal to 0
     * @param gasLimit
     *          - limit of gas that will be used for transaction
     * @param to
     *          - address of smart-contract where transaction will be sent
     * @param data
     *          - transaction converted to binary format
     * @return {@link TransactionReceipt} object with information about transaction
     */
    TransactionReceipt executeTransaction(BigInteger gasPrice, BigInteger gasLimit, String to, String data);
}
