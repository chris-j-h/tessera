package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.keypairs.*;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneratedKeyPair {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeneratedKeyPair.class);

  // key pair data that can be marshalled and used to update the configfile
  private ConfigKeyPair configKeyPair;

  // key gen metadata in addition to the data you'd find in the configfile
//   TODO(cjh) can I get away with just having version and pub val here? I think all the other info in the metadata is being passed from the user already so doesn't need to come from the KeyVaultServices
//  private Map<String, String> metadata;

  private String publicKey;

//  public GeneratedKeyPair(ConfigKeyPair configKeyPair, Map<String, String> metadata) {
//    this.configKeyPair = configKeyPair;
//    this.metadata = metadata;
//  }

  public GeneratedKeyPair(ConfigKeyPair configKeyPair, String publicKey) {
    this.configKeyPair = configKeyPair;
    this.publicKey = publicKey;
  }

  public ConfigKeyPair getConfigKeyPair() {
    return configKeyPair;
  }

  public String getPublicKey() {
    return publicKey;
  }

  //  public Map<String, String> getMetadata() {
//    return metadata;
//  }

  //  public String toString() {
  //    StringJoiner sj = new StringJoiner(", ");
  //
  //    if (configKeyPair instanceof AWSKeyPair) {
  //
  //    } else if (configKeyPair instanceof AzureVaultKeyPair) {
  //      sj.add("type=azure");
  //      sj.add("name=" + metadata.get("name"));
  //      sj.add("version=" + metadata.get("version"));
  //      sj.add("publicKey=" + metadata.get("publicKey"));
  //    } else if (configKeyPair instanceof FilesystemKeyPair) {
  //
  //    } else if (configKeyPair instanceof HashicorpVaultKeyPair) {
  //
  //    } else {
  //      LOGGER.warn("unknown generated key pair type, publicKey={}",
  // configKeyPair.getPublicKey());
  //      sj.add("type=unknown");
  //      sj.add("publicKey=" + configKeyPair.getPublicKey());
  //    }
  //
  //    return sb.toString();
  //  }

}
