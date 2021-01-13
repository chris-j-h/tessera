package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.EncryptorException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.exception.RecipientKeyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class EncodedPayloadManagerImpl implements EncodedPayloadManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncodedPayloadManagerImpl.class);

    private final Enclave enclave;

    private final PrivacyHelper privacyHelper;

    private final PayloadDigest payloadDigest;

    public EncodedPayloadManagerImpl(
            final Enclave enclave, final PrivacyHelper privacyHelper, final PayloadDigest payloadDigest) {
        this.enclave = Objects.requireNonNull(enclave);
        this.privacyHelper = Objects.requireNonNull(privacyHelper);
        this.payloadDigest = Objects.requireNonNull(payloadDigest);
    }

    @Override
    public EncodedPayload create(final SendRequest request) {
        final PublicKey senderPublicKey = request.getSender();
        LOGGER.debug("Sender for payload: {}", request.getSender().encodeToBase64());

        final Set<PublicKey> recipientSet = new HashSet<>();
        recipientSet.add(senderPublicKey);
        recipientSet.addAll(request.getRecipients());
        recipientSet.addAll(enclave.getForwardingKeys());

        final List<PublicKey> recipientListNoDuplicate = new ArrayList<>(recipientSet);
        LOGGER.debug("Recipients for payload: {}", recipientListNoDuplicate);

        final PrivacyMode privacyMode = request.getPrivacyMode();
        LOGGER.debug("Privacy mode for payload: {}", request.getPrivacyMode());
        LOGGER.debug("ExecHash for payload: {}", new String(request.getExecHash()));

        final List<AffectedTransaction> affectedContractTransactions =
                privacyHelper.findAffectedContractTransactionsFromSendRequest(
                        request.getAffectedContractTransactions());

        LOGGER.debug("Validating request against affected contracts");
        privacyHelper.validateSendRequest(privacyMode, recipientListNoDuplicate, affectedContractTransactions);
        LOGGER.debug("Successful validation against affected contracts");

        final PrivacyMetaData.Builder metaDataBuilder =
                PrivacyMetaData.Builder.create()
                        .withPrivacyMode(privacyMode)
                        .withAffectedTransactions(affectedContractTransactions)
                        .withExecHash(request.getExecHash());
        request.getPrivacyGroupId().ifPresent(metaDataBuilder::withPrivacyGroupId);

        return enclave.encryptPayload(
                request.getPayload(), senderPublicKey, recipientListNoDuplicate, metaDataBuilder.build());
    }

    @Override
    public ReceiveResponse decrypt(final EncodedPayload payload, final PublicKey maybeDefaultRecipient) {
        final MessageHash customPayloadHash = new MessageHash(payloadDigest.digest(payload.getCipherText()));
        LOGGER.debug("Decrypt request for custom message with hash {}", customPayloadHash);

        final PublicKey recipientKey =
                Optional.ofNullable(maybeDefaultRecipient)
                        .orElseGet(
                                () ->
                                        searchForRecipientKey(payload)
                                                .orElseThrow(
                                                        () ->
                                                                new RecipientKeyNotFoundException(
                                                                        "No suitable recipient keys found to decrypt payload for "
                                                                                + customPayloadHash)));
        LOGGER.debug("Decryption key found: {}", recipientKey.encodeToBase64());

        final byte[] decryptedTransactionData = enclave.unencryptTransaction(payload, recipientKey);

        final Set<MessageHash> txns =
                payload.getAffectedContractTransactions().keySet().stream()
                        .map(TxHash::getBytes)
                        .map(MessageHash::new)
                        .collect(Collectors.toSet());

        ReceiveResponse.Builder builder =
                ReceiveResponse.Builder.create()
                        .withUnencryptedTransactionData(decryptedTransactionData)
                        .withPrivacyMode(payload.getPrivacyMode())
                        .withAffectedTransactions(txns)
                        .withExecHash(payload.getExecHash())
                        .withSender(payload.getSenderKey());

        payload.getPrivacyGroupId().ifPresent(builder::withPrivacyGroupId);

        return builder.build();
    }

    private Optional<PublicKey> searchForRecipientKey(final EncodedPayload payload) {
        final MessageHash customPayloadHash = new MessageHash(payloadDigest.digest(payload.getCipherText()));
        for (final PublicKey potentialMatchingKey : enclave.getPublicKeys()) {
            try {
                LOGGER.debug(
                        "Attempting to decrypt {} using key {}",
                        customPayloadHash,
                        potentialMatchingKey.encodeToBase64());
                enclave.unencryptTransaction(payload, potentialMatchingKey);
                LOGGER.debug(
                        "Succeeded decrypting {} using key {}",
                        customPayloadHash,
                        potentialMatchingKey.encodeToBase64());
                return Optional.of(potentialMatchingKey);
            } catch (EnclaveException | IndexOutOfBoundsException | EncryptorException ex) {
                LOGGER.debug("Attempted payload decryption using wrong key, discarding.");
            }
        }
        return Optional.empty();
    }
}
