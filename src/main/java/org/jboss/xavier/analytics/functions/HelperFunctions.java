package org.jboss.xavier.analytics.functions;

import org.jboss.xavier.analytics.pojo.output.workload.inventory.WorkloadInventoryReportModel;

import java.util.Arrays;

public class HelperFunctions
{
    public static int round(double value)
    {
        return (int) Math.round(value);
    }

    public static boolean isSupportedOS(String osToCheck)
    {
        return Arrays.stream(OSSupport.values()).anyMatch(value -> osToCheck.toLowerCase().contains(value.getName().toLowerCase()) && value.isSupported()
                                                                        && !isBlacklistedOS(osToCheck));
    }

    public static boolean isConvertibleOS(String osToCheck)
    {
        return Arrays.stream(OSSupport.values()).anyMatch(value -> (osToCheck.toLowerCase().contains(value.getName().toLowerCase()) && !value.isSupported())
                                                                        && !isBlacklistedOS(osToCheck));
    }

    public static boolean isUnsupportedOS(String osToCheck)
    {
        if(osToCheck.equals(""))
        {
            return false;
        }
        else
        {
            return Arrays.stream(OSSupport.values()).noneMatch(value -> osToCheck.toLowerCase().contains(value.getName().toLowerCase())
                                                                        && !isBlacklistedOS(osToCheck));
        }
    }

    public static boolean isUndetectedOS(String osToCheck)
    {
        return osToCheck == null || osToCheck.trim().isEmpty();
    }

    //Any input OS string which matches a blacklisted OS value
    //will override if they otherwise match as supported or convertible and mark them as Unsupported
    private static boolean isBlacklistedOS(String osToCheck)
    {
        return Arrays.stream(OSSupport.values()).anyMatch(value -> osToCheck.toLowerCase().contains(value.getName().toLowerCase()) && value.isBlacklisted());
    }

    public enum OSSupport{

        RHEL("Red Hat Enterprise Linux", true, false),
        SUSE("SUSE", true, false),
        WINDOWS("Windows",true, false),
        ORACLE("Oracle Enterprise Linux",false, false),
        CENTOS("CentOS",false, false),
        WINDOWS_XP("XP", false, true);

        

        private final String name;
        private final boolean isSupported;
        private final boolean isBlacklisted;

        OSSupport(String name, boolean isSupported, boolean isBlacklisted)
        {
            this.name = name;
            this.isSupported = isSupported;
            this.isBlacklisted  = isBlacklisted;
        }

        boolean isSupported()
        {
            return this.isSupported;
        }

        public String getName()
        {
            return this.name;
        }

        boolean isBlacklisted() {return this.isBlacklisted;}
    }

    public enum FlagUnsuitabilityForOSPTarget{
        RDM_DISK(WorkloadInventoryReportModel.RDM_DISK_FLAG_NAME, true),
        TOO_MANY_NICS(WorkloadInventoryReportModel.MORE_THAN_4_NICS_FLAG_NAME, true),
        SHARED_DISK(WorkloadInventoryReportModel.SHARED_DISK_FLAG_NAME,true);

        private final String name;
        private final boolean isUnsuitable;

        FlagUnsuitabilityForOSPTarget(String name, boolean isUnsuitable)
        {
            this.name = name;
            this.isUnsuitable = isUnsuitable;
        }

        boolean isUnsuitable()
        {
            return this.isUnsuitable;
        }

        public String getName()
        {
            return this.name;
        }
    }

    public static boolean isUnsuitableFlag(String flagToCheck)
    {
        return Arrays.stream(FlagUnsuitabilityForOSPTarget.values()).anyMatch(value -> flagToCheck.toLowerCase().contains(value.getName().toLowerCase()) && value.isUnsuitable());
    }
}
