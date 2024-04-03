package dev.firstdark.keymaster.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author HypherionSA
 * Main plugin class. Mostly a dummy for this plugin
 */
public class KeyMasterGradlePlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project target) {
        target.getLogger().info("KeyMaster Plugin is activated");
    }
}
