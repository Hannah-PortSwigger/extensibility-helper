package utils;

import burp.api.montoya.core.Version;

import static burp.api.montoya.core.BurpSuiteEdition.PROFESSIONAL;

public class Capabilities
{
    public enum Capability {
        BAMBDA_IMPORT(false, 20250400000000000L),
        BCHECKS(true, 20231201000000000L);

        private final boolean proOnly;
        private final long minimumSupportedBuildNumber;

        Capability(boolean proOnly, long minimumSupportedBuildNumber) {
            this.proOnly = proOnly;
            this.minimumSupportedBuildNumber = minimumSupportedBuildNumber;
        }
    }

    private final Version version;

    public Capabilities(Version version) {
        this.version = version;
    }

    public boolean hasCapability(Capability capability) {
        return version.buildNumber() >= capability.minimumSupportedBuildNumber && (!capability.proOnly || version.edition() == PROFESSIONAL);
    }
}