/*
 * This file is part of KeyMaster, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2024 HypherionSA and Contributors
 *
 */
package dev.firstdark.keymaster.plugin;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;

/**
 * @author HypherionSA
 * Plugin Extension for sharing common configs between multiple tasks
 */
@Getter
@Setter
public class KeyMasterGradleExtension {

    // Default properties. These are overridden by the values specified on the task
    private final Property<String> gpgKey;
    private final Property<String> gpgPassword;
    private final Property<Boolean> generateSignature;
    private final Property<String> outputDirectory;

    public KeyMasterGradleExtension(Project project) {
        this.gpgKey = project.getObjects().property(String.class);
        this.gpgPassword = project.getObjects().property(String.class);
        this.generateSignature = project.getObjects().property(Boolean.class).convention(true);
        this.outputDirectory = project.getObjects().property(String.class).convention(project.getBuildDir() + "/libs");
    }

}
