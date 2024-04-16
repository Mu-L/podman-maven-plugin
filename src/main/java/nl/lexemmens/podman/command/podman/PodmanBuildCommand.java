package nl.lexemmens.podman.command.podman;

import nl.lexemmens.podman.command.Command;
import nl.lexemmens.podman.config.podman.PodmanConfiguration;
import nl.lexemmens.podman.executor.CommandExecutorDelegate;
import org.apache.maven.plugin.logging.Log;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Implementation of the <code>podman build</code> command
 */
public class PodmanBuildCommand extends AbstractPodmanCommand {

    private static final String PODMAN_ARG_PREFIX = "podman.buildArg.";
    private static final String SQUASH_CMD = "--squash";
    private static final String SQUASH_ALL_CMD = "--squash-all";
    private static final String LAYERS_CMD = "--layers";
    private static final String BUILD_FORMAT_CMD = "--format";
    private static final String CONTAINERFILE_CMD = "--file";
    private static final String PULL_POLICY_CMD = "--pull";
    private static final String NO_CACHE_CMD = "--no-cache";
    private static final String BUILD_ARG_CMD = "--build-arg";
    private static final String PLATFORM_CMD = "--platform";
    private static final String TARGET_STAGE_CMD = "--target";
    private static final String SUBCOMMAND = "build";
    private static final String PODMAN_ULIMITS_PREFIX = "podman.buildUlimits.";
    private static final String ULIMITS_ARG_CMD = "--ulimit";

    private PodmanBuildCommand(Log log, PodmanConfiguration podmanConfig, CommandExecutorDelegate delegate) {
        super(log, podmanConfig, delegate, SUBCOMMAND, false);
    }

    /**
     * Builder class used to create an instance of the {@link PodmanBuildCommand}
     */
    public static class Builder {

        private final PodmanBuildCommand command;

        /**
         * Construct a new instance of this builder
         *
         * @param log          The Maven log
         * @param podmanConfig The Podman configuration
         * @param delegate     The executor delegate
         */
        public Builder(Log log, PodmanConfiguration podmanConfig, CommandExecutorDelegate delegate) {
            this.command = new PodmanBuildCommand(log, podmanConfig, delegate);
        }

        /**
         * Sets whether all layers should be squashed
         *
         * @return This builder instance
         */
        public Builder setSquash() {
            command.withOption(SQUASH_CMD, null);
            return this;
        }

        /**
         * Sets whether all layers should be squashed
         *
         * @return This builder instance
         */
        public Builder setSquashAll() {
            command.withOption(SQUASH_ALL_CMD, null);
            return this;
        }

        /**
         * Sets the layers option
         *
         * @param layers Sets the value of the layers option
         * @return This builder instance
         */
        public Builder setLayers(Boolean layers) {
            command.withOption(LAYERS_CMD, layers.toString());
            return this;
        }

        /**
         * Sets the format of the containers metadata
         *
         * @param format The value of the format option to set
         * @return This builder instance
         */
        public Builder setFormat(String format) {
            command.withOption(BUILD_FORMAT_CMD, format);
            return this;
        }

        /**
         * Sets the context directory to use
         * 
         * @param contextDir the context directory to set
         * @return This builder instance
         */
        public Builder setContextDir(String contextDir) {
	        command.withOption(contextDir, null);
	        return this;
        }
        
        /**
         * Sets the Containerfile that should be build
         *
         * @param containerFile The pointer towards the containerfile to use
         * @return This builder instance
         */
        public Builder setContainerFile(Path containerFile) {
            command.withOption(CONTAINERFILE_CMD, containerFile.toString());
            return this;
        }

        /**
         * Sets whether the base image should be pulled
         *
         * @param pullPolicy Sets whether to pull the image
         * @return This builder instance
         */
        public Builder setPullPolicy(String pullPolicy) {
            command.withOption(PULL_POLICY_CMD, pullPolicy);
            return this;
        }

        /**
         * Sets whether or not to cache intermediate layers
         *
         * @param noCache Sets whether the noCache option should be used
         * @return This builder instance
         */
        public Builder setNoCache(boolean noCache) {
            command.withOption(NO_CACHE_CMD, "" + noCache);
            return this;
        }

        /**
         * Sets the platform for the resulting image rather using the default of the build system
         *
         * @param platform A valid combination of GO OS and GO ARCH for example linux/arm64 (https://golang.org/doc/install/source#environment)
         * @return This builder instance
         */
        public Builder setPlatform(String platform){
            command.withOption(PLATFORM_CMD, platform);
            return this;
        }

        /**
         * Sets the platform for the resulting image rather using the default of the build system
         *
         * @param targetStage Sets the target build stage to build in a multi-stage build.
         * @return This builder instance
         */
        public Builder setTargetStage(String targetStage){
            command.withOption(TARGET_STAGE_CMD, targetStage);
            return this;
        }

        public Builder addBuildArgs(Map<String, String> args) {
            Map<String, String> allBuildArgs = new HashMap<>(args);
            allBuildArgs.putAll(getBuildArgsFromSystem());


            for (Map.Entry<String, String> arg : allBuildArgs.entrySet()) {
                command.withOption(BUILD_ARG_CMD, String.format("%s=%s", arg.getKey(), arg.getValue()));
            }
            return this;
        }

        private Map<String, String> getBuildArgsFromSystem() {
            Map<String, String> buildArgsFromSystem = new HashMap<>();
            Properties properties = System.getProperties();
            for (Object keyObj : properties.keySet()) {
                String key = (String) keyObj;
                if (key.startsWith(PODMAN_ARG_PREFIX)) {
                    String argKey = key.replaceFirst(PODMAN_ARG_PREFIX, "");
                    String value = properties.getProperty(key);

                    if (!isEmpty(value)) {
                        buildArgsFromSystem.put(argKey, value);
                    }
                }
            }

            return buildArgsFromSystem;
        }

        public Builder addUlimitsArgs(Map<String, String> ulimits) {
            Map<String, String> allUlimitsArgs = new HashMap<>(ulimits);
            allUlimitsArgs.putAll(getUlimitsFromSystem());

            for (Map.Entry<String, String> ulimit : allUlimitsArgs.entrySet()) {
                command.withOption(ULIMITS_ARG_CMD, String.format("%s=%s", ulimit.getKey(), ulimit.getValue()));
            }
            return this;
        }

        private Map<String, String> getUlimitsFromSystem() {
            Map<String, String> buildUlimitsFromSystem = new HashMap<>();
            Properties properties = System.getProperties();

            for (Object keyObj : properties.keySet()) {
                String key = (String) keyObj;
                if (key.startsWith(PODMAN_ULIMITS_PREFIX)) {
                    String ulimitKey = key.replaceFirst(PODMAN_ULIMITS_PREFIX, "");
                    String ulimitValue = properties.getProperty(key);

                    if (!isEmpty(ulimitValue)) {
                        buildUlimitsFromSystem.put(ulimitKey, ulimitValue);
                    }
                }
            }

            return buildUlimitsFromSystem;
        }

        /**
         * Returns the constructed command
         *
         * @return The constructed command
         */
        public Command build() {
            return command;
        }


    }


}
