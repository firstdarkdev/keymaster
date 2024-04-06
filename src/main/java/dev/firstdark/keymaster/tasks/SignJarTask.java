/*
 * This file is part of KeyMaster, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2024 HypherionSA and Contributors
 *
 */
package dev.firstdark.keymaster.tasks;

import dev.firstdark.keymaster.plugin.KeyMasterGradleExtension;
import dev.firstdark.keymaster.utils.PluginUtils;
import lombok.Setter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.Security;

import static dev.firstdark.keymaster.utils.PluginUtils.isNullOrBlank;
import static dev.firstdark.keymaster.utils.PluginUtils.resolveFile;

/**
 * @author HypherionSA
 * Main Sign Task. Can be configured per jar
 */
@Setter
public class SignJarTask extends Jar {

    // Input task, file or file name to sign
    private Object artifactInput;

    // Output file name. Defaults to input file
    private String outputFileName = "signed.jar";

    // KeyStore values
    private String gpgKey;
    private String gpgPassword;
    private Boolean generateSignature = true;

    // Set the output directory. Defaults to build/libs
    private String outputDirectory = getProject().getBuildDir() + "/libs";

    @Nullable
    private final KeyMasterGradleExtension extension;

    public SignJarTask() {
        extension = getProject().getExtensions().findByType(KeyMasterGradleExtension.class);
    }

    @Input
    public Object getArtifactInput() {
        return resolveFile(getProject(), this.artifactInput);
    }

    @Input
    public String getOutputFileName() {
        return this.outputFileName;
    }

    @Input
    @Optional
    @Nullable
    public String getGpgKey() {
        if (!isNullOrBlank(gpgKey))
            return gpgKey;

        if (extension != null && !isNullOrBlank(extension.getGpgKey().getOrNull()))
            return extension.getGpgKey().get();

        return null;
    }

    @Input
    @Optional
    @Nullable
    public String getGpgPassword() {
        if (!isNullOrBlank(gpgPassword))
            return gpgPassword;

        if (extension != null && !isNullOrBlank(extension.getGpgPassword().getOrNull()))
            return extension.getGpgPassword().get();

        return null;
    }

    @Input
    public String getOutputDirectory() {
        if (!isNullOrBlank(outputDirectory))
            return outputDirectory;

        if (extension != null && !isNullOrBlank(extension.getOutputDirectory().getOrNull()))
            return extension.getOutputDirectory().get();

        return outputDirectory;
    }

    @OutputFile
    public File getOutputFile() {
        return new File(outputDirectory, outputFileName);
    }

    @Input
    public Boolean getGenerateSignature() {
        if (extension != null && extension.getGenerateSignature().isPresent())
            return extension.getGenerateSignature().get();

        return generateSignature;
    }

    /**
     * Main Task Logic
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @TaskAction
    public void doTask() {
        // Check that input is supplied
        if (getArtifactInput() == null) {
            getProject().getLogger().error("Input cannot be null!");
            return;
        }

        // Check that all the required values are supplied
        if (isNullOrBlank(getGpgKey()) || isNullOrBlank(getGpgPassword())) {
            getLogger().error("Please provide all required parameters: keyStore, keyPass");
            return;
        }

        // Create temporary directory for working
        File tempDir = new File(getProject().getBuildDir(), "signing");
        tempDir.mkdirs();

        // Try to sign the jar
        try {
            processArtifact(getArtifactInput(), tempDir);
        } catch (Exception e) {
            getLogger().error("Failed to sign artifact {}", getArtifactInput(), e);
        }

        // Remove the temp working dir
        tempDir.delete();
    }

    /**
     * Jar Signing logic. This creates the temporary files, signs it, and copies it to where it should be
     * @param input Input task, file or file name to process
     * @param tempDir The temporary working directory
     * @throws IOException This is mostly thrown when a file copy error occurs
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void processArtifact(Object input, File tempDir) throws IOException, PGPException {
        // Set up the input file
        File inputFile = resolveFile(getProject(), input);

        // Check if the output file is specified. If not, default to the input file
        outputFileName = getOutputFileName().equalsIgnoreCase("signed.jar") ? inputFile.getName() : getOutputFileName() + ".jar";

        // Copy the original input file to the temporary processing folder
        File tempInput = new File(tempDir, inputFile.getName());
        Files.copy(inputFile.toPath(), tempInput.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Create a temporary output jar
        File tempOutput = new File(tempDir, getOutputFileName());
        File sigTempFile = new File(tempDir, tempOutput.getName() + ".sig");
        File outputSigFile = new File(getOutputFile().getParentFile(), getOutputFile().getName() + ".sig");

        // Sign the damn thing
        signGPG(tempInput, sigTempFile, tempOutput);

        // Copy the signed jar to the libs folder
        Files.copy(tempOutput.toPath(), getOutputFile().toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Copy Signature File
        if (getGenerateSignature()) {
            Files.copy(sigTempFile.toPath(), outputSigFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            sigTempFile.delete();
        }

        getProject().getLogger().lifecycle("Signed {} successfully", getOutputFile().getName());

        // Cleanup the temporary files
        tempOutput.delete();
        tempInput.delete();
    }

    /**
     * Main Signing logic. This handles reading the GPG keys, and doing the actual signing
     * @param inputFile The jar to be signed
     * @param signatureFile The GPG private key file or string
     * @param signedOutputFile The output, signed jar file
     * @throws IOException Thrown when a file error occurs
     * @throws PGPException Thrown when a signature error occurs
     */
    private void signGPG(File inputFile, File signatureFile, File signedOutputFile) throws IOException, PGPException {
        // Load Bouncy Castle
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        // Load the GPG private key
        byte[] keyBytes = PluginUtils.resolvePrivateKey(getGpgKey());
        if (keyBytes == null) {
            throw new GradleException("Could not read GPG private key. Signing will fail");
        }

        // Process the private key
        try (ByteArrayInputStream keyInputStream = new ByteArrayInputStream(keyBytes);
             FileOutputStream sigOutputStream = new FileOutputStream(signatureFile);
             FileOutputStream signedOutputStream = new FileOutputStream(signedOutputFile)) {

            PGPSecretKey secretKey = PluginUtils.readSecretKey(keyInputStream);
            PGPPrivateKey privateKey = secretKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider(provider).build(getGpgPassword().toCharArray()));

            PGPSignatureGenerator signature = new PGPSignatureGenerator(
                    new JcaPGPContentSignerBuilder(secretKey.getPublicKey().getAlgorithm(), PGPUtil.SHA1)
                            .setProvider(provider));
            signature.init(PGPSignature.BINARY_DOCUMENT, privateKey);

            // Write signature to output .sig file
            if (getGenerateSignature()) {
                try (FileInputStream inputStream = new FileInputStream(inputFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        signature.update(buffer, 0, bytesRead);
                    }

                    signature.generate().encode(sigOutputStream);
                }
            }

            // Copy the signed content to the output signed jar file
            try (FileInputStream inputStream = new FileInputStream(inputFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    signedOutputStream.write(buffer, 0, bytesRead);
                }
            }
        }
    }
}
