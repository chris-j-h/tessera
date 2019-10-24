package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.config.vault.data.AzureGetSecretData;
import com.quorum.tessera.config.vault.data.AzureSetSecretData;
import com.quorum.tessera.config.vault.data.HashicorpGetSecretData;
import com.quorum.tessera.config.vault.data.HashicorpSetSecretData;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.KeyVaultServiceFactory;
import com.quorum.tessera.encryption.EncryptorFactory;
import com.quorum.tessera.passwords.PasswordReaderFactory;
import java.util.Objects;

public class DefaultKeyGeneratorFactory implements KeyGeneratorFactory {

    @Override
    public KeyGenerator create(KeyVaultConfig keyVaultConfig, EncryptorConfig encryptorConfig) {

        Objects.requireNonNull(encryptorConfig, "No encryptor config defined. ");

        final EncryptorFactory encryptorFactory = EncryptorFactory.newFactory(encryptorConfig.getType().name());
        final Encryptor encryptor = encryptorFactory.create(encryptorConfig.getProperties());

        if (keyVaultConfig != null) {
            final KeyVaultServiceFactory keyVaultServiceFactory =
                    KeyVaultServiceFactory.getInstance(keyVaultConfig.getKeyVaultType());

            final Config config = new Config();
            final KeyConfiguration keyConfiguration = new KeyConfiguration();

            if (keyVaultConfig.getKeyVaultType().equals(KeyVaultType.AZURE)) {
                keyConfiguration.setAzureKeyVaultConfig((AzureKeyVaultConfig) keyVaultConfig);

                config.setKeys(keyConfiguration);

                final KeyVaultService<AzureSetSecretData, AzureGetSecretData> keyVaultService =
                        keyVaultServiceFactory.create(config, new EnvironmentVariableProvider());

                return new AzureVaultKeyGenerator(encryptor, keyVaultService);

            } else {
                keyConfiguration.setHashicorpKeyVaultConfig((HashicorpKeyVaultConfig) keyVaultConfig);

                config.setKeys(keyConfiguration);

                final KeyVaultService<HashicorpSetSecretData, HashicorpGetSecretData> keyVaultService =
                        keyVaultServiceFactory.create(config, new EnvironmentVariableProvider());

                return new HashicorpVaultKeyGenerator(encryptor, keyVaultService);
            }
        }

        KeyEncryptor keyEncyptor = KeyEncryptorFactory.newFactory().create(encryptorConfig);

        return new FileKeyGenerator(encryptor, keyEncyptor, PasswordReaderFactory.create());
    }
}
