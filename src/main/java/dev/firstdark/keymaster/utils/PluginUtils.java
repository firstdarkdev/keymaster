/*
 * This file is part of KeyMaster, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2024 HypherionSA and Contributors
 *
 */
package dev.firstdark.keymaster.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * @author HypherionSA
 * Helper Methods used in the plugin
 */
public class PluginUtils {

    /**
     * Helper method to load PGP secrets from the user specified input
     * @param input The InputStream of the file/string to process
     * @return The signing key
     * @throws IOException Thrown when a file error occurs
     * @throws PGPException Thrown when a signature error occurs
     */
    public static PGPSecretKey readSecretKey(InputStream input) throws IOException, PGPException {
        PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(
                PGPUtil.getDecoderStream(input), new JcaKeyFingerprintCalculator());

        Iterator<PGPSecretKeyRing> keyRingIter = pgpSec.getKeyRings();
        while (keyRingIter.hasNext()) {
            PGPSecretKeyRing keyRing = keyRingIter.next();

            Iterator<PGPSecretKey> keyIter = keyRing.getSecretKeys();
            while (keyIter.hasNext()) {
                PGPSecretKey key = keyIter.next();

                if (key.isSigningKey()) {
                    return key;
                }
            }
        }

        throw new IllegalArgumentException("Can't find signing key in key ring.");
    }

    /**
     * Helper method to read a GPG private key from either a file, or String
     * @param input File or String to process
     * @return The read key bytes, or null
     */
    public static byte[] resolvePrivateKey(String input) {
        File f = new File(input);

        if (f.exists() && f.isFile()) {
            try {
                input = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new GradleException("Failed to read GPG Private key", e);
            }
        }

        if (!isNullOrBlank(input)) {
            if (input.startsWith("-----")) {
                String[] parts = input.split("\n");
                StringBuilder sb = new StringBuilder();
                for (String part : parts) {
                    if (!part.startsWith("-----")) {
                        sb.append(part);
                    }
                }
                return Base64.decodeBase64(sb.toString());
            } else {
                return input.getBytes(StandardCharsets.UTF_8);
            }
        }

        return null;
    }

    /**
     * Helper method to check if a supplied string is null or empty
     * @param s The string to test
     * @return True if null or empty
     */
    public static boolean isNullOrBlank(String s) {
        if (s == null)
            return true;

        return StringUtils.isBlank(s);
    }

    /**
     * Resolve an Object to a File
     * @param project The project the file potentially belongs to
     * @param obj The object to process
     * @return A File object, ready to use
     */
    public static File resolveFile(Project project, Object obj) {
        if (obj == null) {
            throw new NullPointerException("Null Path");
        }

        if (obj instanceof Provider) {
            Provider<?> p = (Provider<?>) obj;
            obj = p.get();
        }

        if (obj instanceof File) {
            return (File) obj;
        }

        if (obj instanceof AbstractArchiveTask) {
            return ((AbstractArchiveTask)obj).getArchiveFile().get().getAsFile();
        }

        if (obj instanceof String) {
            return new File(obj.toString());
        }

        return project.file(obj);
    }
}
