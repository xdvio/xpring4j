package io.xpring.javascript;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

/** Provides JavaScript based Utils functionality. */
public class JavaScriptUtils {
    /**
     * An reference to the underlying JavaScript Utils object.
     */
    private Value javaScriptUtils;

    /**
     * @throws JavaScriptLoaderException If the underlying JavaScript was missing or malformed.
     */
    public JavaScriptUtils() throws JavaScriptLoaderException {
        Context context = JavaScriptLoader.getContext();
        Value javaScriptUtils = JavaScriptLoader.loadResource("Utils", context);

        this.javaScriptUtils = javaScriptUtils;
    }

    /**
     * Check if the given string is a valid address on the XRP Ledger.
     *
     * @param address: A string to validate
     * @return A boolean indicating whether this was a valid address.
     */
    public boolean isValidAddress(String address) {
        Value isValidAddressFunction = javaScriptUtils.getMember("isValidAddress");
        return isValidAddressFunction.execute(address).asBoolean();
    }

    /**
     * Check if the given string is a valid X-Address on the XRP Ledger.
     *
     * @param address: A string to validate
     * @return A boolean indicating whether this was a valid X-Address.
     */
    public boolean isValidXAddress(String address) {
        Value isValidXAddressFunction = javaScriptUtils.getMember("isValidXAddress");
        return isValidXAddressFunction.execute(address).asBoolean();
    }

    /**
     * Check if the given string is a valid classic address on the XRP Ledger.
     *
     * @param address: A string to validate
     * @return A boolean indicating whether this was a valid clssic address.
     */
    public boolean isValidClassicAddress(String address) {
        Value isValidClassicAddressFunction = javaScriptUtils.getMember("isValidClassicAddress");
        return isValidClassicAddressFunction.execute(address).asBoolean();
    }

    /**
     * Convert the given transaction blob to a transaction hash.
     *
     * @param transactionBlobHex  A hexadecimal encoded transaction blob.
     * @return  A hex encoded hash if the input was valid, otherwise null.
     */
    public String toTransactionHash(String transactionBlobHex) {
        Value transactionBlobToTransactionHashFunction = javaScriptUtils.getMember("transactionBlobToTransactionHash");
        Value hash = transactionBlobToTransactionHashFunction.execute(transactionBlobHex);
        return hash.isNull() ? null : hash.toString();
    }
}
