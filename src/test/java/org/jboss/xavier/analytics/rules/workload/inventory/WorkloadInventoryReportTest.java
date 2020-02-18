package org.jboss.xavier.analytics.rules.workload.inventory;

import org.jboss.xavier.analytics.pojo.input.workload.inventory.VMWorkloadInventoryModel;
import org.jboss.xavier.analytics.pojo.output.workload.inventory.WorkloadInventoryReportModel;
import org.jboss.xavier.analytics.rules.BaseIntegrationTest;
import org.jboss.xavier.analytics.test.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.internal.command.CommandFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.instanceOf;

public class WorkloadInventoryReportTest extends BaseIntegrationTest {

    public WorkloadInventoryReportTest()
    {
        super("WorkloadInventoryKSession0", "org.jboss.xavier.analytics.rules.workload.inventory", 34);
    }

    @Test
    public void testNoFlagsSupportedOS() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        //Flags
        vmWorkloadInventoryModel.setNicsCount(2);
        vmWorkloadInventoryModel.setHasRdmDisk(false);
        List<String> vmDiskFilenames = new ArrayList<>();

        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);
        Map<String, String> files = new HashMap<>();
        files.put("file.txt", "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat");
        vmWorkloadInventoryModel.setFiles(files);

        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(5, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags
                // Target
                "Target_RHV", "Target_OSP",
                // Complexity
                "No_Flag_Supported_OS",
                // Workloads
                "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        Set<String> flagsIMS = workloadInventoryReportModel.getFlagsIMS();
        Assert.assertNull(flagsIMS);
        // Targets
        Set<String> targets = workloadInventoryReportModel.getRecommendedTargetsIMS();
        Assert.assertEquals(2, targets.size());
        Assert.assertTrue(targets.contains("RHV"));
        Assert.assertTrue(targets.contains("OSP"));
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_EASY,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());

    }

    @Test
    public void testOneFlagSupportedOS() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        //Flags
        vmWorkloadInventoryModel.setNicsCount(5);

        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);
        Map<String, String> files = new HashMap<>();
        files.put("file.txt", "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat");
        vmWorkloadInventoryModel.setFiles(files);

        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(5, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags
                "Flag_Nics",
                // Target
                "Target_RHV",
                // Complexity
                "One_Flag_Supported_OS",
                // Workloads
                "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        Set<String> flagsIMS = workloadInventoryReportModel.getFlagsIMS();
        Assert.assertNotNull(flagsIMS);
        Assert.assertEquals(1, flagsIMS.size());
        Assert.assertTrue(flagsIMS.contains(WorkloadInventoryReportModel.MORE_THAN_4_NICS_FLAG_NAME));
        // Targets
        Set<String> targets = workloadInventoryReportModel.getRecommendedTargetsIMS();
        Assert.assertEquals(1, targets.size());
        Assert.assertTrue(targets.contains("RHV"));
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_MEDIUM,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }

    @Test
    public void testMoreThanOneFlagSupportedOS() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        //Flags
        vmWorkloadInventoryModel.setNicsCount(5);
        vmWorkloadInventoryModel.setHasRdmDisk(true);
        List<String> vmDiskFilenames = new ArrayList<>();
        vmDiskFilenames.add("/path/to/disk.vdmk");
        vmWorkloadInventoryModel.setVmDiskFilenames(vmDiskFilenames);
        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);
        Map<String, String> files = new HashMap<>();
        files.put("file.txt", "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat");
        vmWorkloadInventoryModel.setFiles(files);

        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(6, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
       Utils.verifyRulesFiredNames(this.agendaEventListener,
            // BasicFields
            "Copy basic fields and agenda controller",
            // Flags
           "Flag_Nics", "Flag_Rdm_Disk",
            // Target
           "Target_RHV",
            // Complexity
           "More_Than_One_Flag_Supported_OS",
           // Workloads
           "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        Set<String> flagsIMS = workloadInventoryReportModel.getFlagsIMS();
        Assert.assertNotNull(flagsIMS);
        Assert.assertEquals(2, flagsIMS.size());
        Assert.assertTrue(flagsIMS.contains(WorkloadInventoryReportModel.MORE_THAN_4_NICS_FLAG_NAME));
        Assert.assertTrue(flagsIMS.contains(WorkloadInventoryReportModel.RDM_DISK_FLAG_NAME));
        // Targets
        Set<String> targets = workloadInventoryReportModel.getRecommendedTargetsIMS();
        Assert.assertEquals(1, targets.size());
        Assert.assertTrue(targets.contains("RHV"));
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_HARD,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }

    @Test
    public void testNoFlagsConvertibleOS() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Oracle Linux");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("Oracle");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        //Flags
        vmWorkloadInventoryModel.setNicsCount(2);
        vmWorkloadInventoryModel.setHasRdmDisk(false);
        List<String> vmDiskFilenames = new ArrayList<>();

        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);
        Map<String, String> files = new HashMap<>();
        files.put("file.txt", "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat");
        vmWorkloadInventoryModel.setFiles(files);

        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(6, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags

                // Target
                "Target_RHV", "Target_OSP", "Target_RHEL",
                // Complexity
                "No_Flag_Convertible_OS",
                // Workloads
                "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Oracle Linux",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("Oracle",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        Set<String> flagsIMS = workloadInventoryReportModel.getFlagsIMS();
        Assert.assertNull(flagsIMS);
        // Targets
        Set<String> targets = workloadInventoryReportModel.getRecommendedTargetsIMS();
        Assert.assertEquals(3, targets.size());
        Assert.assertTrue(targets.contains("RHV"));
        Assert.assertTrue(targets.contains("OSP"));
        Assert.assertTrue(targets.contains("RHEL"));
        // Complexity
        //Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_MEDIUM,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }

    @Test
    public void testFlagsCentOS() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("CentOS Enterprise Linux");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("CentOS");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        //Flags
        vmWorkloadInventoryModel.setNicsCount(5);
        vmWorkloadInventoryModel.setHasRdmDisk(true);
        List<String> vmDiskFilenames = new ArrayList<>();

        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);
        Map<String, String> files = new HashMap<>();
        files.put("file.txt", "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat");
        vmWorkloadInventoryModel.setFiles(files);

        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(7, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags
                "Flag_Nics", "Flag_Rdm_Disk",
                // Target
                "Target_RHV", "Target_RHEL",
                // Complexity
                "One_Or_More_Flags_Convertible_OS",
                // Workloads
                "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("CentOS Enterprise Linux",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("CentOS",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        Set<String> flagsIMS = workloadInventoryReportModel.getFlagsIMS();
        Assert.assertEquals(2, flagsIMS.size());
        // Targets
        Set<String> targets = workloadInventoryReportModel.getRecommendedTargetsIMS();
        Assert.assertEquals(2, targets.size());
        Assert.assertTrue(targets.contains("RHV"));
        Assert.assertTrue(targets.contains("RHEL"));
        // Complexity
        //Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_HARD,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }

    @Test
    public void testOneOrMoreFlagsUnsupported_OS() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Debian Linux Server");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("debian");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        //Flags
        vmWorkloadInventoryModel.setNicsCount(5);

        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);
        Map<String, String> files = new HashMap<>();
        files.put("file.txt", "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat");
        vmWorkloadInventoryModel.setFiles(files);

        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(8, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // ReasonableDefaults
                "Fill 'datacenter' field with reasonable default", "Fill 'cluster' field with reasonable default", "Fill 'host_name' field with reasonable default",
                // Flags
                "Flag_Nics",
                // Target
                "Target_None",
                // Complexity
                "One_Or_More_Flags_Not_Supported_OS",
                // Workloads
                "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Debian Linux Server",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("debian",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // ReasonableDefaults
        Assert.assertEquals(WorkloadInventoryReportModel.DATACENTER_DEFAULT_VALUE,workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals(WorkloadInventoryReportModel.CLUSTER_DEFAULT_VALUE,workloadInventoryReportModel.getCluster());
        Assert.assertEquals(WorkloadInventoryReportModel.HOST_NAME_DEFAULT_VALUE, workloadInventoryReportModel.getHost_name());
        // Flags
        Set<String> flagsIMS = workloadInventoryReportModel.getFlagsIMS();
        Assert.assertNotNull(flagsIMS);
        Assert.assertEquals(1, flagsIMS.size());
        Assert.assertTrue(flagsIMS.contains(WorkloadInventoryReportModel.MORE_THAN_4_NICS_FLAG_NAME));
        // Targets
        Set<String> targets = workloadInventoryReportModel.getRecommendedTargetsIMS();
        Assert.assertEquals(1, targets.size());
        Assert.assertTrue(targets.contains("None"));
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_UNSUPPORTED,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }

    @Test
    public void testUndetectedOS() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        //set to empty string because basicfields.drl doesn't allow nulls through
        vmWorkloadInventoryModel.setGuestOSFullName("");
        //set to empty string because basicfields.drl doesn't allow nulls through
        vmWorkloadInventoryModel.setOsProductName("");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        //Flags
        vmWorkloadInventoryModel.setNicsCount(2);
        vmWorkloadInventoryModel.setHasRdmDisk(false);
        List<String> vmDiskFilenames = new ArrayList<>();

        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);
        Map<String, String> files = new HashMap<>();
        files.put("file.txt", "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat");
        vmWorkloadInventoryModel.setFiles(files);

        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(4, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags
                // Target
                "Target_None",
                // Complexity
                "Not_Detected_OS",
                // Workloads
                "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        Set<String> flagsIMS = workloadInventoryReportModel.getFlagsIMS();
        Assert.assertNull(flagsIMS);
        // Targets
        Assert.assertEquals(1,workloadInventoryReportModel.getRecommendedTargetsIMS().size());
        Assert.assertTrue(workloadInventoryReportModel.getRecommendedTargetsIMS().contains("None"));
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_UNKNOWN,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }

    @Test
    public void testTomcatWorkload() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        //Flags
        vmWorkloadInventoryModel.setNicsCount(5);
        vmWorkloadInventoryModel.setHasRdmDisk(true);
        List<String> vmDiskFilenames = new ArrayList<>();
        vmDiskFilenames.add("/path/to/disk.vdmk");
        vmWorkloadInventoryModel.setVmDiskFilenames(vmDiskFilenames);
        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        systemServicesNames.add("tomcat");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);
        Map<String, String> files = new HashMap<>();
        files.put("file.txt", "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat");
        vmWorkloadInventoryModel.setFiles(files);

        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(7, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags
                "Flag_Nics", "Flag_Rdm_Disk",
                // Target
                "Target_RHV",
                // Complexity
                "More_Than_One_Flag_Supported_OS",
                // Workloads
                "Workloads_Tomcat", "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        Set<String> flagsIMS = workloadInventoryReportModel.getFlagsIMS();
        Assert.assertNotNull(flagsIMS);
        Assert.assertEquals(2, flagsIMS.size());
        Assert.assertTrue(flagsIMS.contains(WorkloadInventoryReportModel.MORE_THAN_4_NICS_FLAG_NAME));
        Assert.assertTrue(flagsIMS.contains(WorkloadInventoryReportModel.RDM_DISK_FLAG_NAME));
        // Targets
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_HARD,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertNotNull(workloadInventoryReportModel.getWorkloads());
        Assert.assertEquals(1, workloadInventoryReportModel.getWorkloads().size());
        Assert.assertTrue(workloadInventoryReportModel.getWorkloads().stream().anyMatch(workload -> workload.toLowerCase().contains("tomcat")));
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }

    @Test
    public void testEAPWorkload() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        List<String> vmDiskFilenames = new ArrayList<>();
        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        systemServicesNames.add("jboss");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);


        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(6, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags
                // Target
                "Target_RHV", "Target_OSP",
                // Complexity
                "No_Flag_Supported_OS",
                // Workloads
                "Workloads_EAP", "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        // Targets
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_EASY,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertNotNull(workloadInventoryReportModel.getWorkloads());
        Assert.assertEquals(1, workloadInventoryReportModel.getWorkloads().size());
        Assert.assertTrue(workloadInventoryReportModel.getWorkloads().stream().anyMatch(workload -> workload.toLowerCase().contains("Red Hat JBoss EAP".toLowerCase())));
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }

    @Test
    public void testWebsphereWorkload() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        List<String> vmDiskFilenames = new ArrayList<>();
        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        systemServicesNames.add("Dmgr_was.init");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);


        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(6, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags
                // Target
                "Target_RHV", "Target_OSP",
                // Complexity
                "No_Flag_Supported_OS",
                // Workloads
                "Workloads_Websphere", "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        // Targets
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_EASY,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertNotNull(workloadInventoryReportModel.getWorkloads());
        Assert.assertEquals(1, workloadInventoryReportModel.getWorkloads().size());
        Assert.assertTrue(workloadInventoryReportModel.getWorkloads().stream().anyMatch(workload -> workload.toLowerCase().contains("IBM Websphere App Server".toLowerCase())));
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }

    @Test
    public void testWeblogicWorkload() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        List<String> vmDiskFilenames = new ArrayList<>();
        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        systemServicesNames.add("wls_adminmanager");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);


        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(6, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags
                // Target
                "Target_RHV", "Target_OSP",
                // Complexity
                "No_Flag_Supported_OS",
                // Workloads
                "Workloads_Weblogic", "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        // Targets
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_EASY,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertNotNull(workloadInventoryReportModel.getWorkloads());
        Assert.assertEquals(1, workloadInventoryReportModel.getWorkloads().size());
        Assert.assertTrue(workloadInventoryReportModel.getWorkloads().stream().anyMatch(workload -> workload.toLowerCase().contains("Oracle Weblogic".toLowerCase())));
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }

    @Test
    public void testOracleDBWorkload() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        List<String> vmDiskFilenames = new ArrayList<>();
        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        systemServicesNames.add("dbora");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);


        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(6, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags
                // Target
                "Target_RHV", "Target_OSP",
                // Complexity
                "No_Flag_Supported_OS",
                // Workloads
                "Workloads_Oracle_DB", "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        // Targets
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_EASY,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertNotNull(workloadInventoryReportModel.getWorkloads());
        Assert.assertEquals(1, workloadInventoryReportModel.getWorkloads().size());
        Assert.assertTrue(workloadInventoryReportModel.getWorkloads().stream().anyMatch(workload -> workload.toLowerCase().contains("Oracle Database".toLowerCase())));
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }

    @Test
    public void testClickhouseWorkload() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        List<String> vmDiskFilenames = new ArrayList<>();
        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        systemServicesNames.add("clickhouse-server");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);


        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(6, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags
                // Target
                "Target_RHV", "Target_OSP",
                // Complexity
                "No_Flag_Supported_OS",
                // Workloads
                "Workloads_Clickhouse_Server", "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        // Targets
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_EASY,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertNotNull(workloadInventoryReportModel.getWorkloads());
        Assert.assertEquals(1, workloadInventoryReportModel.getWorkloads().size());
        Assert.assertTrue(workloadInventoryReportModel.getWorkloads().stream().anyMatch(workload -> workload.toLowerCase().contains("Clickhouse Server".toLowerCase())));
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }

    @Test
    public void testSAP_HANA_Workload() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        List<String> vmDiskFilenames = new ArrayList<>();
        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        systemServicesNames.add("sapinit");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);


        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(6, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags
                // Target
                "Target_RHV", "Target_OSP",
                // Complexity
                "No_Flag_Supported_OS",
                // Workloads
                "Workloads_SAP_HANA", "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        // Targets
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_EASY,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertNotNull(workloadInventoryReportModel.getWorkloads());
        Assert.assertEquals(1, workloadInventoryReportModel.getWorkloads().size());
        Assert.assertTrue(workloadInventoryReportModel.getWorkloads().stream().anyMatch(workload -> workload.toLowerCase().contains("SAP HANA".toLowerCase())));
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }

    @Test
    public void testSQLServerOnLinux() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        List<String> vmDiskFilenames = new ArrayList<>();
        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        systemServicesNames.add("mssql-server");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);


        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(6, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags
                // Target
                "Target_RHV", "Target_OSP",
                // Complexity
                "No_Flag_Supported_OS",
                // Workloads
                "Workloads_Microsoft_SQL_Server_On_Linux", "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        // Targets
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_EASY,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertNotNull(workloadInventoryReportModel.getWorkloads());
        Assert.assertEquals(1, workloadInventoryReportModel.getWorkloads().size());
        Assert.assertTrue(workloadInventoryReportModel.getWorkloads().stream().anyMatch(workload -> workload.toLowerCase().contains("Microsoft SQL Server".toLowerCase())));
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }

    @Test
    public void testSQLServerOnWindows() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("NOTwas");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);
        Map<String, String> files = new HashMap<>();
        // TODO remove the wrong test file once it will clear how the rule should work
        // files.put("MSSQLSERVERHOME", "C:\\Program Files\\Microsoft SQL Server");
        files.put("C:\\Program Files\\Microsoft SQL Server", null);
        vmWorkloadInventoryModel.setFiles(files);

        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(6, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags
                // Target
                "Target_RHV", "Target_OSP",
                // Complexity
                "No_Flag_Supported_OS",
                // Workloads
                "Workloads_Microsoft_SQL_Server_On_Windows", "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        // Targets
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_EASY,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertNotNull(workloadInventoryReportModel.getWorkloads());
        Assert.assertEquals(1, workloadInventoryReportModel.getWorkloads().size());
        Assert.assertTrue(workloadInventoryReportModel.getWorkloads().stream().anyMatch(workload -> workload.toLowerCase().contains("Microsoft SQL Server".toLowerCase())));
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }



    @Test
    public void testSQLServerOnWindows2() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        List<String> vmDiskFilenames = new ArrayList<>();
        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);
        Map<String, String> files = new HashMap<>();
        // TODO remove the wrong test file once it will clear how the rule should work
        // files.put("MSSQLSERVERHOME", "C:\\Program Files\\Microsoft SQL Server");
        files.put("C:/Program Files/Microsoft SQL Server", null);
        vmWorkloadInventoryModel.setFiles(files);

        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(9, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // ReasonableDefaults
                "Fill 'datacenter' field with reasonable default", "Fill 'cluster' field with reasonable default", "Fill 'host_name' field with reasonable default",
                // Flags
                // Target
                "Target_RHV", "Target_OSP",
                // Complexity
                "No_Flag_Supported_OS",
                // Workloads
                "Workloads_Microsoft_SQL_Server_On_Windows","SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // ReasonableDefaults
        Assert.assertEquals(WorkloadInventoryReportModel.DATACENTER_DEFAULT_VALUE,workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals(WorkloadInventoryReportModel.CLUSTER_DEFAULT_VALUE,workloadInventoryReportModel.getCluster());
        Assert.assertEquals(WorkloadInventoryReportModel.HOST_NAME_DEFAULT_VALUE, workloadInventoryReportModel.getHost_name());
        // Flags
        // Targets
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_EASY,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertNotNull(workloadInventoryReportModel.getWorkloads());
        Assert.assertEquals(1, workloadInventoryReportModel.getWorkloads().size());
        Assert.assertTrue(workloadInventoryReportModel.getWorkloads().stream().anyMatch(workload -> workload.toLowerCase().contains("Microsoft SQL Server".toLowerCase())));
    }

    @Test
    public void testArtifactoryWorkload() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        List<String> vmDiskFilenames = new ArrayList<>();
        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        systemServicesNames.add("artifactory");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);


        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(6, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags
                // Target
                "Target_RHV", "Target_OSP",
                // Complexity
                "No_Flag_Supported_OS",
                // Workloads
                "Workloads_Artifactory", "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        // Targets
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_EASY,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertNotNull(workloadInventoryReportModel.getWorkloads());
        Assert.assertEquals(1, workloadInventoryReportModel.getWorkloads().size());
        Assert.assertTrue(workloadInventoryReportModel.getWorkloads().stream().anyMatch(workload -> workload.toLowerCase().contains("Artifactory".toLowerCase())));
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }

    @Test
    public void testF5Workload() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        List<String> vmDiskFilenames = new ArrayList<>();
        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        systemServicesNames.add("f5functions");
        systemServicesNames.add("f5dirs");
        systemServicesNames.add("f5-swap-eth");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);


        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(6, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags
                // Target
                "Target_RHV", "Target_OSP",
                // Complexity
                "No_Flag_Supported_OS",
                // Workloads
                "Workloads_F5", "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        // Targets
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_EASY,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertNotNull(workloadInventoryReportModel.getWorkloads());
        Assert.assertEquals(1, workloadInventoryReportModel.getWorkloads().size());
        Assert.assertTrue(workloadInventoryReportModel.getWorkloads().stream().anyMatch(workload -> workload.toLowerCase().contains("F5".toLowerCase())));
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }

    @Test
    public void testIncompleteF5ServicesWorkload() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        List<String> vmDiskFilenames = new ArrayList<>();
        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        systemServicesNames.add("f5dirs");
        systemServicesNames.add("f5-swap-eth");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);


        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(5, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags
                // Target
                "Target_RHV", "Target_OSP",
                // Complexity
                "No_Flag_Supported_OS",
                // Workloads
                "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        // Targets
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_EASY,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertNull(workloadInventoryReportModel.getWorkloads());
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }


    @Test
    public void testNoServicesAndNoFiles() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        List<String> vmDiskFilenames = new ArrayList<>();
        vmDiskFilenames.add("/path/to/disk.vdmk");
        vmWorkloadInventoryModel.setVmDiskFilenames(vmDiskFilenames);
/*        List<String> systemServicesNames = new ArrayList<>();
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);*/
        vmWorkloadInventoryModel.setSystemServicesNames(null);
        /*Map<String, String> files = new HashMap<>();
        vmWorkloadInventoryModel.setFiles(files);*/
        vmWorkloadInventoryModel.setFiles(null);

        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(5, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags

                // Target
                "Target_RHV", "Target_OSP",
                // Complexity
                "No_Flag_Supported_OS",
                // Workloads
                "SsaDisabled_System_Services_Not_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        // Targets
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_EASY,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertFalse(workloadInventoryReportModel.getSsaEnabled());
    }



    @Test
    public void testESXiOS() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("VMware ESXi 5.x");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("ESXi");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        //Flags

        Map<String, String> files = new HashMap<>();
        files.put("file.txt", "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat");
        vmWorkloadInventoryModel.setFiles(files);

        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(7, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // ReasonableDefaults
                "Fill 'datacenter' field with reasonable default", "Fill 'cluster' field with reasonable default", "Fill 'host_name' field with reasonable default",
                // Flags
                // Target
                "Target_None",
                // Complexity
                "No_Flags_Not_Supported_OS",
                // Workloads
                "SsaDisabled_System_Services_Not_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("VMware ESXi 5.x",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("ESXi",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // ReasonableDefaults
        Assert.assertEquals(WorkloadInventoryReportModel.DATACENTER_DEFAULT_VALUE,workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals(WorkloadInventoryReportModel.CLUSTER_DEFAULT_VALUE,workloadInventoryReportModel.getCluster());
        Assert.assertEquals(WorkloadInventoryReportModel.HOST_NAME_DEFAULT_VALUE, workloadInventoryReportModel.getHost_name());
        // Flags
        Set<String> flagsIMS = workloadInventoryReportModel.getFlagsIMS();
        Assert.assertNull(flagsIMS);
        // Targets
        Set<String> targets = workloadInventoryReportModel.getRecommendedTargetsIMS();
        Assert.assertEquals(1, targets.size());
        Assert.assertTrue(targets.contains("None"));
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_UNSUPPORTED,workloadInventoryReportModel.getComplexity());
        // Workloads
    }

    @Test
    public void testUbuntuOS() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Ubuntu");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("ubuntu");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        //Flags

        Map<String, String> files = new HashMap<>();
        files.put("file.txt", "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat");
        vmWorkloadInventoryModel.setFiles(files);

        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(7, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // ReasonableDefaults
                "Fill 'datacenter' field with reasonable default", "Fill 'cluster' field with reasonable default", "Fill 'host_name' field with reasonable default",
                // Flags
                // Target
                "Target_None",
                // Complexity
                "No_Flags_Not_Supported_OS",
                // Workloads
                "SsaDisabled_System_Services_Not_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Ubuntu",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("ubuntu",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // ReasonableDefaults
        Assert.assertEquals(WorkloadInventoryReportModel.DATACENTER_DEFAULT_VALUE,workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals(WorkloadInventoryReportModel.CLUSTER_DEFAULT_VALUE,workloadInventoryReportModel.getCluster());
        Assert.assertEquals(WorkloadInventoryReportModel.HOST_NAME_DEFAULT_VALUE, workloadInventoryReportModel.getHost_name());
        // Flags
        Set<String> flagsIMS = workloadInventoryReportModel.getFlagsIMS();
        Assert.assertNull(flagsIMS);
        // Targets
        Set<String> targets = workloadInventoryReportModel.getRecommendedTargetsIMS();
        Assert.assertEquals(1, targets.size());
        Assert.assertTrue(targets.contains("None"));
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_UNSUPPORTED,workloadInventoryReportModel.getComplexity());
        // Workloads
    }

    @Test
    public void testPhotonOS() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("VMware Photon");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("photon");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        //Flags

        Map<String, String> files = new HashMap<>();
        files.put("file.txt", "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat");
        vmWorkloadInventoryModel.setFiles(files);

        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(7, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // ReasonableDefaults
                "Fill 'datacenter' field with reasonable default", "Fill 'cluster' field with reasonable default", "Fill 'host_name' field with reasonable default",
                // Flags
                // Target
                "Target_None",
                // Complexity
                "No_Flags_Not_Supported_OS",
                // Workloads
                "SsaDisabled_System_Services_Not_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("VMware Photon",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("photon",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // ReasonableDefaults
        Assert.assertEquals(WorkloadInventoryReportModel.DATACENTER_DEFAULT_VALUE,workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals(WorkloadInventoryReportModel.CLUSTER_DEFAULT_VALUE,workloadInventoryReportModel.getCluster());
        Assert.assertEquals(WorkloadInventoryReportModel.HOST_NAME_DEFAULT_VALUE, workloadInventoryReportModel.getHost_name());
        // Flags
        Set<String> flagsIMS = workloadInventoryReportModel.getFlagsIMS();
        Assert.assertNull(flagsIMS);
        // Targets
        Set<String> targets = workloadInventoryReportModel.getRecommendedTargetsIMS();
        Assert.assertEquals(1, targets.size());
        Assert.assertTrue(targets.contains("None"));
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_UNSUPPORTED,workloadInventoryReportModel.getComplexity());
        // Workloads
    }

    @Test
    public void testFreeBSDOS() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("FreeBSD");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("freebsd");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        //Flags

        Map<String, String> files = new HashMap<>();
        files.put("file.txt", "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat");
        vmWorkloadInventoryModel.setFiles(files);

        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(7, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // ReasonableDefaults
                "Fill 'datacenter' field with reasonable default", "Fill 'cluster' field with reasonable default", "Fill 'host_name' field with reasonable default",
                // Flags
                // Target
                "Target_None",
                // Complexity
                "No_Flags_Not_Supported_OS",
                // Workloads
                "SsaDisabled_System_Services_Not_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("FreeBSD",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("freebsd",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // ReasonableDefaults
        Assert.assertEquals(WorkloadInventoryReportModel.DATACENTER_DEFAULT_VALUE,workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals(WorkloadInventoryReportModel.CLUSTER_DEFAULT_VALUE,workloadInventoryReportModel.getCluster());
        Assert.assertEquals(WorkloadInventoryReportModel.HOST_NAME_DEFAULT_VALUE, workloadInventoryReportModel.getHost_name());
        // Flags
        Set<String> flagsIMS = workloadInventoryReportModel.getFlagsIMS();
        Assert.assertNull(flagsIMS);
        // Targets
        Set<String> targets = workloadInventoryReportModel.getRecommendedTargetsIMS();
        Assert.assertEquals(1, targets.size());
        Assert.assertTrue(targets.contains("None"));
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_UNSUPPORTED,workloadInventoryReportModel.getComplexity());
        // Workloads
    }

    @Test
    public void testSuseOS() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("SUSE");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("suse");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        //Flags

        Map<String, String> files = new HashMap<>();
        files.put("file.txt", "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat");
        vmWorkloadInventoryModel.setFiles(files);

        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(8, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // ReasonableDefaults
                "Fill 'datacenter' field with reasonable default", "Fill 'cluster' field with reasonable default", "Fill 'host_name' field with reasonable default",
                // Flags
                // Target
                "Target_RHV", "Target_OSP",
                // Complexity
                "No_Flag_Supported_OS",
                // Workloads
                "SsaDisabled_System_Services_Not_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("SUSE",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("suse",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // ReasonableDefaults
        Assert.assertEquals(WorkloadInventoryReportModel.DATACENTER_DEFAULT_VALUE,workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals(WorkloadInventoryReportModel.CLUSTER_DEFAULT_VALUE,workloadInventoryReportModel.getCluster());
        Assert.assertEquals(WorkloadInventoryReportModel.HOST_NAME_DEFAULT_VALUE, workloadInventoryReportModel.getHost_name());
        // Flags
        Set<String> flagsIMS = workloadInventoryReportModel.getFlagsIMS();
        Assert.assertNull(flagsIMS);
        // Targets
        Set<String> targets = workloadInventoryReportModel.getRecommendedTargetsIMS();
        Assert.assertEquals(2, targets.size());
        Assert.assertTrue(targets.contains("RHV"));
        Assert.assertTrue(targets.contains("OSP"));
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_EASY,workloadInventoryReportModel.getComplexity());
        // Workloads
    }

    @Test
    public void testSolarisOS() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Oracle Solaris");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("solaris");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        //Flags

        Map<String, String> files = new HashMap<>();
        files.put("file.txt", "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat");
        vmWorkloadInventoryModel.setFiles(files);

        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(7, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // ReasonableDefaults
                "Fill 'datacenter' field with reasonable default", "Fill 'cluster' field with reasonable default", "Fill 'host_name' field with reasonable default",
                // Flags
                // Target
                "Target_None",
                // Complexity
                "No_Flags_Not_Supported_OS",
                // Workloads
                "SsaDisabled_System_Services_Not_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Oracle Solaris",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("solaris",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // ReasonableDefaults
        Assert.assertEquals(WorkloadInventoryReportModel.DATACENTER_DEFAULT_VALUE,workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals(WorkloadInventoryReportModel.CLUSTER_DEFAULT_VALUE,workloadInventoryReportModel.getCluster());
        Assert.assertEquals(WorkloadInventoryReportModel.HOST_NAME_DEFAULT_VALUE, workloadInventoryReportModel.getHost_name());
        // Flags
        Set<String> flagsIMS = workloadInventoryReportModel.getFlagsIMS();
        Assert.assertNull(flagsIMS);
        // Targets
        Set<String> targets = workloadInventoryReportModel.getRecommendedTargetsIMS();
        Assert.assertEquals(1, targets.size());
        Assert.assertTrue(targets.contains("None"));
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_UNSUPPORTED,workloadInventoryReportModel.getComplexity());
        // Workloads
    }

    @Test
    public void testXPOS() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Microsoft Windows XP Professional (32-bit)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("Microsoft Windows XP Professional (32-bit)");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        //Flags

        Map<String, String> files = new HashMap<>();
        files.put("file.txt", "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat");
        vmWorkloadInventoryModel.setFiles(files);

        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(7, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // ReasonableDefaults
                "Fill 'datacenter' field with reasonable default", "Fill 'cluster' field with reasonable default", "Fill 'host_name' field with reasonable default",
                // Flags
                // Target
                "Target_None",
                // Complexity
                "No_Flags_Not_Supported_OS",
                // Workloads
                "SsaDisabled_System_Services_Not_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Microsoft Windows XP Professional (32-bit)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("Microsoft Windows XP Professional (32-bit)",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // ReasonableDefaults
        Assert.assertEquals(WorkloadInventoryReportModel.DATACENTER_DEFAULT_VALUE,workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals(WorkloadInventoryReportModel.CLUSTER_DEFAULT_VALUE,workloadInventoryReportModel.getCluster());
        Assert.assertEquals(WorkloadInventoryReportModel.HOST_NAME_DEFAULT_VALUE, workloadInventoryReportModel.getHost_name());
        // Flags
        Set<String> flagsIMS = workloadInventoryReportModel.getFlagsIMS();
        Assert.assertNull(flagsIMS);
        // Targets
        Set<String> targets = workloadInventoryReportModel.getRecommendedTargetsIMS();
        Assert.assertEquals(1, targets.size());
        Assert.assertTrue(targets.contains("None"));
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_UNSUPPORTED,workloadInventoryReportModel.getComplexity());
        // Workloads
    }

    @Test
    public void testDebianOS() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Debian");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("debian");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        //Flags

        Map<String, String> files = new HashMap<>();
        files.put("file.txt", "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat");
        vmWorkloadInventoryModel.setFiles(files);

        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(7, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // ReasonableDefaults
                "Fill 'datacenter' field with reasonable default", "Fill 'cluster' field with reasonable default", "Fill 'host_name' field with reasonable default",
                // Flags
                // Target
                "Target_None",
                // Complexity
                "No_Flags_Not_Supported_OS",
                // Workloads
                "SsaDisabled_System_Services_Not_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Debian",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("debian",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // ReasonableDefaults
        Assert.assertEquals(WorkloadInventoryReportModel.DATACENTER_DEFAULT_VALUE,workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals(WorkloadInventoryReportModel.CLUSTER_DEFAULT_VALUE,workloadInventoryReportModel.getCluster());
        Assert.assertEquals(WorkloadInventoryReportModel.HOST_NAME_DEFAULT_VALUE, workloadInventoryReportModel.getHost_name());
        // Flags
        Set<String> flagsIMS = workloadInventoryReportModel.getFlagsIMS();
        Assert.assertNull(flagsIMS);
        // Targets
        Set<String> targets = workloadInventoryReportModel.getRecommendedTargetsIMS();
        Assert.assertEquals(1, targets.size());
        Assert.assertTrue(targets.contains("None"));
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_UNSUPPORTED,workloadInventoryReportModel.getComplexity());
        // Workloads
    }

    @Test
    public void testCitrixUnidesk_1() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        List<String> vmDiskFilenames = new ArrayList<>();
        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        systemServicesNames.add("unidesk-xenserver-connector");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);


        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(6, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags
                // Target
                "Target_RHV", "Target_OSP",
                // Complexity
                "No_Flag_Supported_OS",
                // Workloads
                "Workloads_Citrix_Unidesk", "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        // Targets
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_EASY,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertNotNull(workloadInventoryReportModel.getWorkloads());
        Assert.assertEquals(1, workloadInventoryReportModel.getWorkloads().size());
        Assert.assertTrue(workloadInventoryReportModel.getWorkloads().stream().anyMatch(workload -> workload.toLowerCase().contains("Citrix Unidesk".toLowerCase())));
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }

    @Test
    public void testCitrixUnidesk_2() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        List<String> vmDiskFilenames = new ArrayList<>();
        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        systemServicesNames.add("unidesk-vsphere-connector");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);


        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(6, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags
                // Target
                "Target_RHV", "Target_OSP",
                // Complexity
                "No_Flag_Supported_OS",
                // Workloads
                "Workloads_Citrix_Unidesk", "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        // Targets
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_EASY,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertNotNull(workloadInventoryReportModel.getWorkloads());
        Assert.assertEquals(1, workloadInventoryReportModel.getWorkloads().size());
        Assert.assertTrue(workloadInventoryReportModel.getWorkloads().stream().anyMatch(workload -> workload.toLowerCase().contains("Citrix Unidesk".toLowerCase())));
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }

    @Test
    public void testCitrixUnidesk_3() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        List<String> vmDiskFilenames = new ArrayList<>();
        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        systemServicesNames.add("unidesk-pvs-connector");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);


        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(6, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags
                // Target
                "Target_RHV", "Target_OSP",
                // Complexity
                "No_Flag_Supported_OS",
                // Workloads
                "Workloads_Citrix_Unidesk", "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        // Targets
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_EASY,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertNotNull(workloadInventoryReportModel.getWorkloads());
        Assert.assertEquals(1, workloadInventoryReportModel.getWorkloads().size());
        Assert.assertTrue(workloadInventoryReportModel.getWorkloads().stream().anyMatch(workload -> workload.toLowerCase().contains("Citrix Unidesk".toLowerCase())));
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }

    @Test
    public void testCitrixUnidesk_4() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        List<String> vmDiskFilenames = new ArrayList<>();
        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        systemServicesNames.add("unidesk-nutanix-connector");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);


        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(6, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags
                // Target
                "Target_RHV", "Target_OSP",
                // Complexity
                "No_Flag_Supported_OS",
                // Workloads
                "Workloads_Citrix_Unidesk", "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        // Targets
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_EASY,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertNotNull(workloadInventoryReportModel.getWorkloads());
        Assert.assertEquals(1, workloadInventoryReportModel.getWorkloads().size());
        Assert.assertTrue(workloadInventoryReportModel.getWorkloads().stream().anyMatch(workload -> workload.toLowerCase().contains("Citrix Unidesk".toLowerCase())));
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }

    @Test
    public void testCitrixUnidesk_5() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        List<String> vmDiskFilenames = new ArrayList<>();
        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        systemServicesNames.add("unidesk-hyperv-connector");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);


        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(6, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags
                // Target
                "Target_RHV", "Target_OSP",
                // Complexity
                "No_Flag_Supported_OS",
                // Workloads
                "Workloads_Citrix_Unidesk", "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        // Targets
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_EASY,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertNotNull(workloadInventoryReportModel.getWorkloads());
        Assert.assertEquals(1, workloadInventoryReportModel.getWorkloads().size());
        Assert.assertTrue(workloadInventoryReportModel.getWorkloads().stream().anyMatch(workload -> workload.toLowerCase().contains("Citrix Unidesk".toLowerCase())));
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }

    @Test
    public void testCitrixUnidesk_6() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        List<String> vmDiskFilenames = new ArrayList<>();
        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        systemServicesNames.add("unidesk-azure-connector");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);


        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(6, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags
                // Target
                "Target_RHV", "Target_OSP",
                // Complexity
                "No_Flag_Supported_OS",
                // Workloads
                "Workloads_Citrix_Unidesk", "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        // Targets
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_EASY,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertNotNull(workloadInventoryReportModel.getWorkloads());
        Assert.assertEquals(1, workloadInventoryReportModel.getWorkloads().size());
        Assert.assertTrue(workloadInventoryReportModel.getWorkloads().stream().anyMatch(workload -> workload.toLowerCase().contains("Citrix Unidesk".toLowerCase())));
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }

    @Test
    public void testCiscoCallManager() throws ParseException {
        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();

        //Basic Fields
        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setProvider("IMS vCenter");
        vmWorkloadInventoryModel.setDatacenter("V2V-DC");
        vmWorkloadInventoryModel.setCluster("Cluster 1");
        vmWorkloadInventoryModel.setVmName("vm tests");
        vmWorkloadInventoryModel.setDiskSpace(100000001L);
        vmWorkloadInventoryModel.setMemory(4096L);
        vmWorkloadInventoryModel.setCpuCores(4);
        vmWorkloadInventoryModel.setGuestOSFullName("Red Hat Enterprise Linux Server release 7.6 (Maipo)");
        // keep it lower case to check that the rules evaluate it ignoring the case
        vmWorkloadInventoryModel.setOsProductName("rhel");
        vmWorkloadInventoryModel.setProduct("VMware vCenter");
        vmWorkloadInventoryModel.setVersion("6.5");
        vmWorkloadInventoryModel.setHost_name("esx13.v2v.bos.redhat.com");
        vmWorkloadInventoryModel.setScanRunDate(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"));

        List<String> vmDiskFilenames = new ArrayList<>();
        List<String> systemServicesNames = new ArrayList<>();
        systemServicesNames.add("unix_service");
        systemServicesNames.add("cisco_history_log");
        vmWorkloadInventoryModel.setSystemServicesNames(systemServicesNames);

        Map<String, String> files = new HashMap<>();
        files.put("/etc/group", "ccmservice");
        vmWorkloadInventoryModel.setFiles(files);
        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // add the query to retrieve the report we want
        commands.add(CommandFactory.newQuery(QUERY_IDENTIFIER, "GetWorkloadInventoryReports"));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(6, results.get(NUMBER_OF_FIRED_RULE_KEY));
        // check the names of the rules fired are what you expect
        Utils.verifyRulesFiredNames(this.agendaEventListener,
                // BasicFields
                "Copy basic fields and agenda controller",
                // Flags
                // Target
                "Target_RHV", "Target_OSP",
                // Complexity
                "No_Flag_Supported_OS",
                // Workloads
                "Workloads_Cisco_CallManager", "SsaEnabled_System_Services_Present"
        );

        // retrieve the QueryResults that was available in the working memory from the results
        QueryResults queryResults= (QueryResults) results.get(QUERY_IDENTIFIER);

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, queryResults.size());

        // Check that the object is of the expected type and with the expected identifier (i.e. "report")
        QueryResultsRow queryResultsRow = queryResults.iterator().next();
        Assert.assertThat(queryResultsRow.get("report"), instanceOf(WorkloadInventoryReportModel.class));

        // Check that the object has exactly the fields that the rules tested should add/change
        WorkloadInventoryReportModel workloadInventoryReportModel = (WorkloadInventoryReportModel) queryResultsRow.get("report");
        // BasicFields
        Assert.assertEquals("IMS vCenter",workloadInventoryReportModel.getProvider());
        Assert.assertEquals("V2V-DC",workloadInventoryReportModel.getDatacenter());
        Assert.assertEquals("Cluster 1",workloadInventoryReportModel.getCluster());
        Assert.assertEquals("vm tests",workloadInventoryReportModel.getVmName());
        Assert.assertEquals(100000001L,workloadInventoryReportModel.getDiskSpace(), 0);
        Assert.assertEquals(4096,workloadInventoryReportModel.getMemory().intValue());
        Assert.assertEquals(4,workloadInventoryReportModel.getCpuCores().intValue());
        Assert.assertEquals("Red Hat Enterprise Linux Server release 7.6 (Maipo)",workloadInventoryReportModel.getOsDescription());
        Assert.assertEquals("rhel",workloadInventoryReportModel.getOsName());
        Assert.assertEquals("VMware vCenter", workloadInventoryReportModel.getProduct());
        Assert.assertEquals("6.5", workloadInventoryReportModel.getVersion());
        Assert.assertEquals("esx13.v2v.bos.redhat.com", workloadInventoryReportModel.getHost_name());
        Assert.assertEquals(new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss.S").parse("2019-09-18T14:52:45.871Z"), workloadInventoryReportModel.getCreationDate());
        // Flags
        // Targets
        // Complexity
        Assert.assertEquals(WorkloadInventoryReportModel.COMPLEXITY_EASY,workloadInventoryReportModel.getComplexity());
        // Workloads
        Assert.assertNotNull(workloadInventoryReportModel.getWorkloads());
        Assert.assertEquals(1, workloadInventoryReportModel.getWorkloads().size());
        Assert.assertTrue(workloadInventoryReportModel.getWorkloads().stream().anyMatch(workload -> workload.toLowerCase().contains("Cisco CallManager".toLowerCase())));
        Assert.assertTrue(workloadInventoryReportModel.getSsaEnabled());
    }
}
