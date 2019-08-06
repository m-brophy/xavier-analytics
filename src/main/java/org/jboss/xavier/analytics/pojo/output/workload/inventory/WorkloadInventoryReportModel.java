package org.jboss.xavier.analytics.pojo.output.workload.inventory;

import org.jboss.xavier.analytics.pojo.output.AnalysisModel;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class WorkloadInventoryReportModel
{
    static final long serialVersionUID = 1L;

    public static final String COMPLEXITY_EASY = "COMPLEXITY_EASY";
    public static final String COMPLEXITY_MEDIUM = "COMPLEXITY_MEDIUM";
    public static final String COMPLEXITY_HARD = "COMPLEXITY_HARD";
    public static final String COMPLEXITY_UNKNOWN = "COMPLEXITY_UNKNOWN";

    public static enum OSSupport{
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



        public static boolean isSupportedOS(String osToCheck)
        {
            return Arrays.stream(OSSupport.values()).anyMatch(value -> value.name().equals(osToCheck) && value.isSupported());
        }

        public static boolean isUnsupportedOS(String osToCheck)
        {
            return Arrays.stream(OSSupport.values()).anyMatch(value -> value.name().equals(osToCheck) && !value.isSupported());
        }

        public static boolean isUndetectedOS(String osToCheck)
        {
            return Arrays.stream(OSSupport.values()).noneMatch(value -> value.name().equals(osToCheck));
        }

        public String getName()
        {
            return this.name;
        }
    }

    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO, generator = "WORKLOADINVENTORYREPORTMODEL_ID_GENERATOR")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id")
    private AnalysisModel analysis;

    private String provider;
    private String datacenter;
    private String cluster;
    private String vmName;
    private String osName;
    private String osDescription;
    private BigDecimal diskSpace;
    private Integer memory;
    private Integer cpuCores;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> workloads;
    private String complexity;
    // with "IMS" suffix in case one day we will have
    // their "AMM" counterparts
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> recommendedTargetsIMS;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> flagsIMS;
    private Date creationDate;

    public WorkloadInventoryReportModel() {}

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AnalysisModel getAnalysis() {
        return analysis;
    }

    public void setAnalysis(AnalysisModel analysis) {
        this.analysis = analysis;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getDatacenter() {
        return datacenter;
    }

    public void setDatacenter(String datacenter) {
        this.datacenter = datacenter;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsDescription() {
        return osDescription;
    }

    public void setOsDescription(String osDescription) {
        this.osDescription = osDescription;
    }

    public BigDecimal getDiskSpace() {
        return diskSpace;
    }

    public void setDiskSpace(BigDecimal diskSpace) {
        this.diskSpace = diskSpace;
    }

    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer memory) {
        this.memory = memory;
    }

    public Integer getCpuCores() {
        return cpuCores;
    }

    public void setCpuCores(Integer cpuCores) {
        this.cpuCores = cpuCores;
    }

    public Set<String> getWorkloads() {
        return workloads;
    }

    public void setWorkloads(Set<String> workloads) {
        this.workloads = workloads;
    }

    public void addWorkload(String workload)
    {
        if (this.workloads == null) this.workloads = new HashSet<>();
        this.workloads.add(workload);
    }

    public String getComplexity() {
        return complexity;
    }

    public void setComplexity(String complexity) {
        this.complexity = complexity;
    }

    public Set<String> getRecommendedTargetsIMS() {
        return recommendedTargetsIMS;
    }

    public void setRecommendedTargetsIMS(Set<String> recommendedTargetsIMS) {
        this.recommendedTargetsIMS = recommendedTargetsIMS;
    }

    public void addRecommendedTargetsIMS(String recommendedTargetIMS)
    {
        if (this.recommendedTargetsIMS == null) this.recommendedTargetsIMS = new HashSet<>();
        this.recommendedTargetsIMS.add(recommendedTargetIMS);
    }

    public Set<String> getFlagsIMS() {
        return flagsIMS;
    }

    public void setFlagsIMS(Set<String> flagsIMS) {
        this.flagsIMS = flagsIMS;
    }

    public void addFlagIMS(String flagIMS)
    {
        if (this.flagsIMS == null) this.flagsIMS = new HashSet<>();
        this.flagsIMS.add(flagIMS);
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
