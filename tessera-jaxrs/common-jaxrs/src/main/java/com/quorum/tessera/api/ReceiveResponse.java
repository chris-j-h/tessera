package com.quorum.tessera.api;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlMimeType;

/**
 * Model representation of a JSON body on outgoing HTTP requests
 *
 * <p>Contains a Base64 encoded string that is the decrypting payload of a transaction
 */
public class ReceiveResponse {

    @Schema(description = "decrypted ciphertext payload", type = "string", format = "base64")
    @XmlMimeType("base64Binary")
    private byte[] payload;

    @Schema(
            description =
                    "the privacy mode of the transaction\n* 0 = standard private\n* 1 = party protection\n* 3 = private-state validation",
            allowableValues = {"0", "1", "3"})
    private int privacyFlag;

    @ArraySchema(
            arraySchema =
                    @Schema(
                            description =
                                    "encoded payload hashes identifying all affected private contracts after tx simulation"),
            schema = @Schema(format = "base64"))
    private String[] affectedContractTransactions;

    @Schema(
            description = "execution hash; merkle root of all affected contracts after tx simulation",
            format = "base64")
    private String execHash;

    public ReceiveResponse() {}

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(final byte[] payload) {
        this.payload = payload;
    }

    public int getPrivacyFlag() {
        return privacyFlag;
    }

    public void setPrivacyFlag(int privacyFlag) {
        this.privacyFlag = privacyFlag;
    }

    public String[] getAffectedContractTransactions() {
        return affectedContractTransactions;
    }

    public void setAffectedContractTransactions(String[] affectedContractTransactions) {
        this.affectedContractTransactions = affectedContractTransactions;
    }

    public String getExecHash() {
        return execHash;
    }

    public void setExecHash(String execHash) {
        this.execHash = execHash;
    }
}
