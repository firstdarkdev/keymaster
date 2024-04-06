/*
 * This file is part of KeyMaster, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2024 HypherionSA and Contributors
 *
 */
package dev.firstdark.keymaster.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author HypherionSA
 * Main plugin class.
 */
public class KeyMasterGradlePlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project target) {
        target.getLogger().info("KeyMaster Plugin is activated");
        target.getExtensions().create("keymaster", KeyMasterGradleExtension.class);
    }
}
