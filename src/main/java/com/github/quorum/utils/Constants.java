package com.github.quorum.utils;

import java.math.BigInteger;

public interface Constants {

    BigInteger GAS_PRICE = BigInteger.ZERO;
    BigInteger TX_GAS_LIMIT = BigInteger.valueOf(60_000);
    BigInteger DEPLOY_GAS_LIMIT = BigInteger.valueOf(350_000);

    String NONCE_TOO_LOW_ERROR_MESSAGE = "nonce too low";
}
