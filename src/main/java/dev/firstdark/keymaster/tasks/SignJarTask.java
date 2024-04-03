package dev.firstdark.keymaster.tasks;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.jvm.tasks.Jar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

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
    private String keyStorePass;
    private String keyStore;
    private String keyStoreAlias;
    private String keyPass;

    // Set the output directory. Defaults to build/libs
    private String outputDirectory = getProject().getBuildDir() + "/libs";

    @Input
    public Object getArtifactInput() {
        return resolveFile(getProject(), this.artifactInput);
    }

    @Input
    public String getOutputFileName() {
        return this.outputFileName;
    }

    @Input
    public String getKeyStorePass() {
        return this.keyStorePass;
    }

    @Input
    public String getKeyStore() {
        return this.keyStore;
    }

    @Input
    public String getKeyStoreAlias() {
        return this.keyStoreAlias;
    }

    @Input
    public String getKeyPass() {
        return this.keyPass;
    }

    @Input
    public String getOutputDirectory() {
        return this.outputDirectory;
    }

    @OutputFile
    public File getOutputFile() {
        return new File(outputDirectory, outputFileName);
    }

    /**
     * Main Task Logic
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @TaskAction
    public void doTask() {
        // Check that input is supplied
        if (artifactInput == null) {
            getProject().getLogger().error("Input cannot be null!");
            return;
        }

        // Check that all the required values are supplied
        if (isNullOrBlank(keyPass) || isNullOrBlank(keyStore) || isNullOrBlank(keyStoreAlias) || isNullOrBlank(keyStorePass)) {
            getLogger().error("Please provide all required parameters: keyStore, keyStoreAlias, keyStorePass, keyPass");
            return;
        }

        // Create temporary directory for working
        File tempDir = new File(getProject().getBuildDir(), "signing");
        tempDir.mkdirs();

        try {
            processArtifact(artifactInput, tempDir);
        } catch (Exception e) {
            getLogger().error("Failed to sign artifact {}", artifactInput, e);
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
    private void processArtifact(Object input, File tempDir) throws IOException {
        // Set up the input file
        File inputFile = resolveFile(getProject(), input);

        // Check if the output file is specified. If not, default to the input file
        outputFileName = outputFileName.equalsIgnoreCase("signed.jar") ? inputFile.getName() : outputFileName + ".jar";

        // Copy the original input file to the temporary processing folder
        File tempInput = new File(tempDir, inputFile.getName());
        Files.copy(inputFile.toPath(), tempInput.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Create a temporary output jar
        File tempOutput = new File(tempDir, outputFileName);

        // Configure the jar signing
        Map<String, Object> map = new HashMap<>();
        map.put("alias", keyStoreAlias);
        map.put("storePass", keyStorePass);
        map.put("jar", tempInput.getAbsolutePath());
        map.put("signedJar", tempOutput.getAbsolutePath());
        map.put("keypass", keyPass);
        map.put("keyStore", resolveFile(getProject(), keyStore).getAbsolutePath());

        // SIGN IT
        getProject().getAnt().invokeMethod("signjar", map);

        // Copy the signed jar to the libs folder
        Files.copy(tempOutput.toPath(), getOutputFile().toPath(), StandardCopyOption.REPLACE_EXISTING);

        getProject().getLogger().lifecycle("Signed " + getOutputFile().getName() + " successfully");

        // Cleanup the temporary files
        tempOutput.delete();
        tempInput.delete();
    }

    /**
     * Helper method to check if a supplied string is null or empty
     * @param s The string to test
     * @return True if null or empty
     */
    private boolean isNullOrBlank(String s) {
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
    private File resolveFile(Project project, Object obj) {
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
