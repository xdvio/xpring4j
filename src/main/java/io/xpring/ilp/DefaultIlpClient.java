package io.xpring.ilp;

import org.interledger.spsp.server.grpc.AccountServiceGrpc;
import org.interledger.spsp.server.grpc.BalanceServiceGrpc;
import org.interledger.spsp.server.grpc.CreateAccountRequest;
import org.interledger.spsp.server.grpc.CreateAccountResponse;
import org.interledger.spsp.server.grpc.GetAccountRequest;
import org.interledger.spsp.server.grpc.GetAccountResponse;
import org.interledger.spsp.server.grpc.GetBalanceRequest;
import org.interledger.spsp.server.grpc.GetBalanceResponse;
import org.interledger.spsp.server.grpc.IlpOverHttpServiceGrpc;
import org.interledger.spsp.server.grpc.SendPaymentRequest;
import org.interledger.spsp.server.grpc.SendPaymentResponse;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.xpring.ilp.grpc.IlpJwtCallCredentials;
import io.xpring.ilp.model.PaymentRequest;
import io.xpring.ilp.model.PaymentResult;
import io.xpring.ilp.model.AccountBalance;
import io.xpring.xrpl.XpringException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * A client that can get balances and send ILP payments on a connector.
 */
public class DefaultIlpClient implements IlpClientDecorator {

    private static final Logger logger = LoggerFactory.getLogger(DefaultIlpClient.class);

    private final BalanceServiceGrpc.BalanceServiceBlockingStub balanceServiceStub;
    private final IlpOverHttpServiceGrpc.IlpOverHttpServiceBlockingStub ilpOverHttpServiceStub;

    /**
     * Initialize a new client with a configured URL
     * @param grpcUrl : The gRPC URL exposed by Hermes
     */
    protected DefaultIlpClient(String grpcUrl) {
        this(ManagedChannelBuilder
          .forTarget(grpcUrl)
          .usePlaintext()
          .build()
        );
    }

    /**
     * Required-args Constructor, currently for testing.
     *
     * @param channel A {@link ManagedChannel}.
     */
    public DefaultIlpClient(final ManagedChannel channel) {
        this.balanceServiceStub = BalanceServiceGrpc.newBlockingStub(channel);
        this.ilpOverHttpServiceStub = IlpOverHttpServiceGrpc.newBlockingStub(channel);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            channel.shutdown();
            try {
                channel.awaitTermination(5, TimeUnit.SECONDS);
            }
            catch (Exception timedOutException) {
                try {
                    channel.shutdownNow();
                }
                catch (Exception e) {
                    // nothing more can be done
                }
            }
        }));
    }

    @Override
    public AccountBalance getBalance(final String accountId, final String bearerToken) throws XpringException {
        GetBalanceRequest request = GetBalanceRequest.newBuilder()
          .setAccountId(accountId)
          .build();

        try {
            GetBalanceResponse response = this.balanceServiceStub
              .withCallCredentials(IlpJwtCallCredentials.build(bearerToken))
              .getBalance(request);

            // Convert protobuf response to AccountBalanceResponse
            return AccountBalance.from(response);
        } catch (StatusRuntimeException e) {
            throw new XpringException(String.format("Unable to get balance for account %s.  %s", accountId, e.getStatus()));
        }
    }

    @Override
    public PaymentResult sendPayment(final PaymentRequest paymentRequest,
                                     final String bearerToken) throws XpringException {
        try {
            // Convert paymentRequest to a protobuf object
            SendPaymentRequest request = paymentRequest.toProto();

            SendPaymentResponse protoResponse =
              ilpOverHttpServiceStub
                .withCallCredentials(IlpJwtCallCredentials.build(bearerToken))
                .sendMoney(request);

            return PaymentResult.from(protoResponse);

        } catch (StatusRuntimeException e) {
            throw new XpringException("Unable to send payment. " + e.getStatus());
        }
    }
}