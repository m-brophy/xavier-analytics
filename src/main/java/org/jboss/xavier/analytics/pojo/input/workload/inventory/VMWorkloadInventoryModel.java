package org.jboss.xavier.analytics.pojo.input.workload.inventory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class VMWorkloadInventoryModel
{
    //common/name
    private String provider;
    //common/ems_clusters/v_parent_datacenter
    private String datacenter;
    //common/ems_clusters/name
    private String cluster;
    //vms/name
    private String vmName;
    //sum of vms/hardware/disks/size_on_disk
    private Long diskSpace;
    //vms/ram_size_in_bytes
    private Long memory;
    //vms/num_cpu
    private Integer cpuCores;
    //vms/operating_system/product_name
    private String osProductName;
    //hardware/guest_os_full_name
    private String guestOSFullName;
    //vms/has_rdm_disk
    private boolean hasRdmDisk;
    //count of nics object within the vms/hardware
    private Integer nicsCount;
    private String product;
    private String version;
    private String host_name;
    private boolean cpuAffinityNotNull;
    private boolean hasUEFIBoot;

    private Date scanRunDate;

    //hardware/disks/filename
    private Collection<String> vmDiskFilenames;
    private Collection<String> systemServicesNames;
    private Map<String, String> files;

    private Boolean hasMemoryHotAdd;
    private Boolean hasCpuHotAdd;
    private Boolean hasCpuHotRemove;

    public VMWorkloadInventoryModel()
    {
        this.systemServicesNames = new ArrayList<>();
        this.files = new HashMap<>();
        this.vmDiskFilenames = new ArrayList<>();
        nicsCount = 0;
        diskSpace = new Long(0);
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

    public Long getDiskSpace() {
        return diskSpace;
    }

    public void setDiskSpace(Long diskSpace) {
        this.diskSpace = diskSpace;
    }

    public void addDiskSpace(Long nextDiskSpace) {
        this.diskSpace = Long.sum(this.diskSpace,nextDiskSpace);
    }

    public Long getMemory() {
        return memory;
    }

    public void setMemory(Long memory) {
        this.memory = memory;
    }

    public Integer getCpuCores() {
        return cpuCores;
    }

    public void setCpuCores(Integer cpuCores) {
        this.cpuCores = cpuCores;
    }

    public String getOsProductName() {
        return osProductName;
    }

    public void setOsProductName(String osProductName) {
        this.osProductName = osProductName;
    }

    public String getGuestOSFullName() {
        return guestOSFullName;
    }

    public void setGuestOSFullName(String guestOSFullName) {
        this.guestOSFullName = guestOSFullName;
    }

    public boolean isHasRdmDisk() {
        return hasRdmDisk;
    }

    public void setHasRdmDisk(boolean hasRdmDisk) {
        this.hasRdmDisk = hasRdmDisk;
    }

    public Integer getNicsCount() {
        return nicsCount;
    }

    public void setNicsCount(Integer nicsCount) {
        this.nicsCount = nicsCount;
    }

    public void addNicsCount() {
        this.nicsCount++;
    }

    public Collection<String> getVmDiskFilenames() {
        return vmDiskFilenames;
    }

    public void setVmDiskFilenames(Collection<String> vmDiskFilenames) {
        this.vmDiskFilenames = vmDiskFilenames;
    }

    public void addVmDiskFilename(String vmDiskFilename) {
        this.vmDiskFilenames.add(vmDiskFilename);
    }

    public Collection<String> getSystemServicesNames() {
        return systemServicesNames;
    }

    public void setSystemServicesNames(Collection<String> systemServicesNames) {
        this.systemServicesNames = systemServicesNames;
    }

    public void addSystemService(String systemServiceName) {
        this.systemServicesNames.add(systemServiceName);
    }

    public Map<String, String> getFiles() {
        return files;
    }


    public void setFiles(Map<String, String> files) {
        this.files = files;
    }

    public void addFile(String name, String contents) {
        this.files.put(name,contents);
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getHost_name() {
        return host_name;
    }

    public void setHost_name(String host_name) {
        this.host_name = host_name;
    }

    public Date getScanRunDate() {
        return scanRunDate;
    }

    public void setScanRunDate(Date scanRunDate) {
        this.scanRunDate = scanRunDate;
    }
  
    public Boolean isHasMemoryHotAdd() {
        return hasMemoryHotAdd;
    }

    public void setHasMemoryHotAdd(Boolean hasMemoryHotAdd) {
        this.hasMemoryHotAdd = hasMemoryHotAdd;
    }

    public Boolean isHasCpuHotAdd() {
        return hasCpuHotAdd;
    }

    public void setHasCpuHotAdd(Boolean hasCpuHotAdd) {
        this.hasCpuHotAdd = hasCpuHotAdd;
    }

    public Boolean isHasCpuHotRemove() {
        return hasCpuHotRemove;
    }

    public void setHasCpuHotRemove(Boolean hasCpuHotRemove) {
        this.hasCpuHotRemove = hasCpuHotRemove;
    }


    public boolean isCpuAffinityNotNull() {
        return cpuAffinityNotNull;
    }

    public void setCpuAffinityNotNull(boolean cpuAffinityNotNull) {
        this.cpuAffinityNotNull = cpuAffinityNotNull;
    }

    public boolean isHasUEFIBoot() {
        return hasUEFIBoot;
    }

    public void setHasUEFIBoot(boolean hasUEFIBoot) {
        this.hasUEFIBoot = hasUEFIBoot;
    }
}
