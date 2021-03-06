package io.xpring.payid.idiomatic;

import io.xpring.common.idiomatic.XrplNetwork;
import io.xpring.payid.PayIDException;

/**
 * An interface for a PayID client.
 */
public interface XrpPayIdClientInterface {
  /**
   * The {@link XrplNetwork} that addresses will be resolved on.
   *
   * @return The {@link XrplNetwork} that addresses will be resolved on.
   */
  XrplNetwork getXrplNetwork();

  /**
   * Resolve the given PayID to an XRP Address.
   *
   * @param payId The payID to resolve for an address.
   * @return An XRP address representing the given PayID.
   * @throws PayIdException if the inputs were invalid.
   */
  String xrpAddressForPayId(String payId) throws PayIdException, PayIDException;
}
