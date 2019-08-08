package org.jboss.xavier.analytics.functions;

import java.util.Arrays;

public class HelperFunctions
{
    public static int round(double value)
    {
        return (int) Math.round(value);
    }

    public static boolean isSupportedOS(String osToCheck)
    {
        return Arrays.stream(OSSupport.values()).anyMatch(value -> value.name().equalsIgnoreCase(osToCheck) && value.isSupported());
    }

    public static boolean isUnsupportedOS(String osToCheck)
    {
        return Arrays.stream(OSSupport.values()).anyMatch(value -> value.name().equalsIgnoreCase(osToCheck) && !value.isSupported());
    }

    public static boolean isUndetectedOS(String osToCheck)
    {
        return Arrays.stream(OSSupport.values()).noneMatch(value -> value.name().equalsIgnoreCase(osToCheck));
    }

    public enum OSSupport{
        RHEL("RHEL", true),
        SUSE("Suse", true),
        WINDOWS("Windows",true),
        ORACLE("Oracle Enterprise Linux",false),
        CENTOS("CentOS",false),
        DEBIAN("Debian",false),
        UBUNTU("Ubuntu",false);

        private final String name;
        private final boolean isSupported;

        OSSupport(String name, boolean isSupported)
        {
            this.name = name;
            this.isSupported = isSupported;
        }

        boolean isSupported()
        {
            return this.isSupported;
        }

        public String getName()
        {
            return this.name;
        }
    }
}
