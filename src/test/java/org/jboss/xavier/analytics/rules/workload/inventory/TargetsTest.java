package org.jboss.xavier.analytics.rules.workload.inventory;

import org.jboss.xavier.analytics.pojo.output.workload.inventory.WorkloadInventoryReportModel;
import org.jboss.xavier.analytics.rules.BaseTest;
import org.jboss.xavier.analytics.test.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.io.ResourceType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TargetsTest extends BaseTest {

    public TargetsTest() {
        super("/org/jboss/xavier/analytics/rules/workload/inventory/Targets.drl", ResourceType.DRL,
                "org.jboss.xavier.analytics.rules.workload.inventory", 5);
    }

    @Test
    public void testTargetsFlagsExistSupportedOS() {
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "Targets");

        WorkloadInventoryReportModel workloadInventoryReportModel = new WorkloadInventoryReportModel();
        workloadInventoryReportModel.setOsDescription("Red Hat Enterprise Linux v7.6");
        workloadInventoryReportModel.addFlagIMS(WorkloadInventoryReportModel.RDM_DISK_FLAG_NAME);

        facts.put("workloadInventoryReportModel",workloadInventoryReportModel);

        Map<String, Object> results = createAndExecuteCommandsAndGetResults(facts);

        Assert.assertEquals(3, results.get(NUMBER_OF_FIRED_RULE_KEY));
        Utils.verifyRulesFiredNames(this.agendaEventListener, "AgendaFocusForTest", "Target_RHV", "Target_CNV");

        List<WorkloadInventoryReportModel> reports = Utils.extractModels(GET_OBJECTS_KEY, results, WorkloadInventoryReportModel.class);

        // just one report has to be created
        Assert.assertEquals(1, reports.size());
        WorkloadInventoryReportModel report = reports.get(0);
        Assert.assertNotNull(report.getRecommendedTargetsIMS());
        Assert.assertEquals(2, report.getRecommendedTargetsIMS().size());
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHV".toLowerCase())));
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("CNV".toLowerCase())));
    }

    @Test
    public void testTargetsFlagsExistConvertibleOS() {
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "Targets");

        WorkloadInventoryReportModel workloadInventoryReportModel = new WorkloadInventoryReportModel();
        workloadInventoryReportModel.setOsDescription("CentOS");
        workloadInventoryReportModel.addFlagIMS(WorkloadInventoryReportModel.RDM_DISK_FLAG_NAME);
        workloadInventoryReportModel.addFlagIMS(WorkloadInventoryReportModel.SHARED_DISK_FLAG_NAME);

        facts.put("workloadInventoryReportModel",workloadInventoryReportModel);

        Map<String, Object> results = createAndExecuteCommandsAndGetResults(facts);

        Assert.assertEquals(3, results.get(NUMBER_OF_FIRED_RULE_KEY));

        Utils.verifyRulesFiredNames(this.agendaEventListener, "AgendaFocusForTest", "Target_RHV", "Target_RHEL");


        List<WorkloadInventoryReportModel> reports = Utils.extractModels(GET_OBJECTS_KEY, results, WorkloadInventoryReportModel.class);

        // just one report has to be created
        Assert.assertEquals(1, reports.size());
        WorkloadInventoryReportModel report = reports.get(0);
        Assert.assertNotNull(report.getRecommendedTargetsIMS());
        Assert.assertEquals(2, report.getRecommendedTargetsIMS().size());
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHV".toLowerCase())));
    }

    @Test
    public void testTargetsNoFlagsSupportedOS() {
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "Targets");

        WorkloadInventoryReportModel workloadInventoryReportModel = new WorkloadInventoryReportModel();
        workloadInventoryReportModel.setOsDescription("Red Hat Enterprise Linux v7.6");
        workloadInventoryReportModel.setFlagsIMS(null);

        facts.put("workloadInventoryReportModel",workloadInventoryReportModel);

        Map<String, Object> results = createAndExecuteCommandsAndGetResults(facts);

        Assert.assertEquals(4, results.get(NUMBER_OF_FIRED_RULE_KEY));
        Utils.verifyRulesFiredNames(this.agendaEventListener, "AgendaFocusForTest", "Target_RHV", "Target_OSP", "Target_CNV");

        List<WorkloadInventoryReportModel> reports = Utils.extractModels(GET_OBJECTS_KEY, results, WorkloadInventoryReportModel.class);

        // just one report has to be created
        Assert.assertEquals(1, reports.size());
        WorkloadInventoryReportModel report = reports.get(0);
        Assert.assertNotNull(report.getRecommendedTargetsIMS());
        Assert.assertEquals(3, report.getRecommendedTargetsIMS().size());
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHV".toLowerCase())));
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("OSP".toLowerCase())));
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("CNV".toLowerCase())));
    }

    @Test
    public void testTargetsNoFlagsUnsupportedOS() {
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "Targets");

        WorkloadInventoryReportModel workloadInventoryReportModel = new WorkloadInventoryReportModel();
        workloadInventoryReportModel.setOsDescription("Ubuntu");
        workloadInventoryReportModel.setFlagsIMS(null);

        facts.put("workloadInventoryReportModel",workloadInventoryReportModel);

        Map<String, Object> results = createAndExecuteCommandsAndGetResults(facts);

        Assert.assertEquals(2, results.get(NUMBER_OF_FIRED_RULE_KEY));
        Utils.verifyRulesFiredNames(this.agendaEventListener, "AgendaFocusForTest", "Target_None");


        List<WorkloadInventoryReportModel> reports = Utils.extractModels(GET_OBJECTS_KEY, results, WorkloadInventoryReportModel.class);

        // just one report has to be created
        Assert.assertEquals(1, reports.size());
        WorkloadInventoryReportModel report = reports.get(0);
        Assert.assertNotNull(report.getRecommendedTargetsIMS());
        Assert.assertEquals(1, report.getRecommendedTargetsIMS().size());
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("None".toLowerCase())));
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("OSP".toLowerCase())));
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHV".toLowerCase())));
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHEL".toLowerCase())));

    }

    @Test
    public void testTargetsCentOS() {
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "Targets");

        WorkloadInventoryReportModel workloadInventoryReportModel = new WorkloadInventoryReportModel();
        workloadInventoryReportModel.setOsDescription("CentOS");
        workloadInventoryReportModel.addFlagIMS(WorkloadInventoryReportModel.RDM_DISK_FLAG_NAME);

        facts.put("workloadInventoryReportModel",workloadInventoryReportModel);

        Map<String, Object> results = createAndExecuteCommandsAndGetResults(facts);

        Assert.assertEquals(3, results.get(NUMBER_OF_FIRED_RULE_KEY));
        Utils.verifyRulesFiredNames(this.agendaEventListener, "AgendaFocusForTest", "Target_RHV", "Target_RHEL");

        List<WorkloadInventoryReportModel> reports = Utils.extractModels(GET_OBJECTS_KEY, results, WorkloadInventoryReportModel.class);

        // just one report has to be created
        Assert.assertEquals(1, reports.size());
        WorkloadInventoryReportModel report = reports.get(0);
        Assert.assertNotNull(report.getRecommendedTargetsIMS());
        Assert.assertEquals(2, report.getRecommendedTargetsIMS().size());
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHV".toLowerCase())));
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHEL".toLowerCase())));
    }

    @Test
    public void testTargetsOracle() {
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "Targets");

        WorkloadInventoryReportModel workloadInventoryReportModel = new WorkloadInventoryReportModel();
        workloadInventoryReportModel.setOsDescription("Oracle Linux Server");

        facts.put("workloadInventoryReportModel",workloadInventoryReportModel);

        Map<String, Object> results = createAndExecuteCommandsAndGetResults(facts);

        Assert.assertEquals(4, results.get(NUMBER_OF_FIRED_RULE_KEY));
        Utils.verifyRulesFiredNames(this.agendaEventListener, "AgendaFocusForTest", "Target_RHV", "Target_OSP","Target_RHEL");

        List<WorkloadInventoryReportModel> reports = Utils.extractModels(GET_OBJECTS_KEY, results, WorkloadInventoryReportModel.class);

        // just one report has to be created
        Assert.assertEquals(1, reports.size());
        WorkloadInventoryReportModel report = reports.get(0);
        Assert.assertNotNull(report.getRecommendedTargetsIMS());
        Assert.assertEquals(3, report.getRecommendedTargetsIMS().size());
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHV".toLowerCase())));
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("OSP".toLowerCase())));
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHEL".toLowerCase())));
    }

    @Test
    public void testTargetsESXi() {
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "Targets");

        WorkloadInventoryReportModel workloadInventoryReportModel = new WorkloadInventoryReportModel();
        workloadInventoryReportModel.setOsDescription("VMware ESXi 5.x");

        facts.put("workloadInventoryReportModel",workloadInventoryReportModel);

        Map<String, Object> results = createAndExecuteCommandsAndGetResults(facts);

        Assert.assertEquals(2, results.get(NUMBER_OF_FIRED_RULE_KEY));
        Utils.verifyRulesFiredNames(this.agendaEventListener, "AgendaFocusForTest", "Target_None");

        List<WorkloadInventoryReportModel> reports = Utils.extractModels(GET_OBJECTS_KEY, results, WorkloadInventoryReportModel.class);

        // just one report has to be created
        Assert.assertEquals(1, reports.size());
        WorkloadInventoryReportModel report = reports.get(0);
        Assert.assertNotNull(report.getRecommendedTargetsIMS());
        Assert.assertEquals(1, report.getRecommendedTargetsIMS().size());
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHV".toLowerCase())));
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("OSP".toLowerCase())));
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHEL".toLowerCase())));
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("None".toLowerCase())));
    }

    @Test
    public void testTargetsUbuntu() {
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "Targets");

        WorkloadInventoryReportModel workloadInventoryReportModel = new WorkloadInventoryReportModel();
        workloadInventoryReportModel.setOsDescription("Ubuntu");

        facts.put("workloadInventoryReportModel",workloadInventoryReportModel);

        Map<String, Object> results = createAndExecuteCommandsAndGetResults(facts);

        Assert.assertEquals(2, results.get(NUMBER_OF_FIRED_RULE_KEY));
        Utils.verifyRulesFiredNames(this.agendaEventListener, "AgendaFocusForTest", "Target_None");

        List<WorkloadInventoryReportModel> reports = Utils.extractModels(GET_OBJECTS_KEY, results, WorkloadInventoryReportModel.class);

        // just one report has to be created
        Assert.assertEquals(1, reports.size());
        WorkloadInventoryReportModel report = reports.get(0);
        Assert.assertNotNull(report.getRecommendedTargetsIMS());
        Assert.assertEquals(1, report.getRecommendedTargetsIMS().size());
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHV".toLowerCase())));
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("OSP".toLowerCase())));
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHEL".toLowerCase())));
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("None".toLowerCase())));
    }

    @Test
    public void testTargetsPhoton() {
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "Targets");

        WorkloadInventoryReportModel workloadInventoryReportModel = new WorkloadInventoryReportModel();
        workloadInventoryReportModel.setOsDescription("VMware Photon");

        facts.put("workloadInventoryReportModel",workloadInventoryReportModel);

        Map<String, Object> results = createAndExecuteCommandsAndGetResults(facts);

        Assert.assertEquals(2, results.get(NUMBER_OF_FIRED_RULE_KEY));
        Utils.verifyRulesFiredNames(this.agendaEventListener, "AgendaFocusForTest", "Target_None");

        List<WorkloadInventoryReportModel> reports = Utils.extractModels(GET_OBJECTS_KEY, results, WorkloadInventoryReportModel.class);

        // just one report has to be created
        Assert.assertEquals(1, reports.size());
        WorkloadInventoryReportModel report = reports.get(0);
        Assert.assertNotNull(report.getRecommendedTargetsIMS());
        Assert.assertEquals(1, report.getRecommendedTargetsIMS().size());
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHV".toLowerCase())));
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("OSP".toLowerCase())));
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHEL".toLowerCase())));
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("None".toLowerCase())));
    }

    @Test
    public void testTargetsFreeBSD() {
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "Targets");

        WorkloadInventoryReportModel workloadInventoryReportModel = new WorkloadInventoryReportModel();
        workloadInventoryReportModel.setOsDescription("FreeBSD");

        facts.put("workloadInventoryReportModel",workloadInventoryReportModel);

        Map<String, Object> results = createAndExecuteCommandsAndGetResults(facts);

        Assert.assertEquals(2, results.get(NUMBER_OF_FIRED_RULE_KEY));
        Utils.verifyRulesFiredNames(this.agendaEventListener, "AgendaFocusForTest", "Target_None");

        List<WorkloadInventoryReportModel> reports = Utils.extractModels(GET_OBJECTS_KEY, results, WorkloadInventoryReportModel.class);

        // just one report has to be created
        Assert.assertEquals(1, reports.size());
        WorkloadInventoryReportModel report = reports.get(0);
        Assert.assertNotNull(report.getRecommendedTargetsIMS());
        Assert.assertEquals(1, report.getRecommendedTargetsIMS().size());
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHV".toLowerCase())));
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("OSP".toLowerCase())));
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHEL".toLowerCase())));
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("None".toLowerCase())));
    }

    @Test
    public void testTargetsSUSE() {
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "Targets");

        WorkloadInventoryReportModel workloadInventoryReportModel = new WorkloadInventoryReportModel();
        workloadInventoryReportModel.setOsDescription("SUSE");

        facts.put("workloadInventoryReportModel",workloadInventoryReportModel);

        Map<String, Object> results = createAndExecuteCommandsAndGetResults(facts);

        Assert.assertEquals(3, results.get(NUMBER_OF_FIRED_RULE_KEY));
        Utils.verifyRulesFiredNames(this.agendaEventListener, "AgendaFocusForTest", "Target_RHV", "Target_OSP");

        List<WorkloadInventoryReportModel> reports = Utils.extractModels(GET_OBJECTS_KEY, results, WorkloadInventoryReportModel.class);

        // just one report has to be created
        Assert.assertEquals(1, reports.size());
        WorkloadInventoryReportModel report = reports.get(0);
        Assert.assertNotNull(report.getRecommendedTargetsIMS());
        Assert.assertEquals(2, report.getRecommendedTargetsIMS().size());
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHV".toLowerCase())));
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("OSP".toLowerCase())));
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHEL".toLowerCase())));
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("None".toLowerCase())));
    }

    @Test
    public void testTargetsSolaris() {
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "Targets");

        WorkloadInventoryReportModel workloadInventoryReportModel = new WorkloadInventoryReportModel();
        workloadInventoryReportModel.setOsDescription("Oracle Solaris");

        facts.put("workloadInventoryReportModel",workloadInventoryReportModel);

        Map<String, Object> results = createAndExecuteCommandsAndGetResults(facts);

        Assert.assertEquals(2, results.get(NUMBER_OF_FIRED_RULE_KEY));
        Utils.verifyRulesFiredNames(this.agendaEventListener, "AgendaFocusForTest", "Target_None");

        List<WorkloadInventoryReportModel> reports = Utils.extractModels(GET_OBJECTS_KEY, results, WorkloadInventoryReportModel.class);

        // just one report has to be created
        Assert.assertEquals(1, reports.size());
        WorkloadInventoryReportModel report = reports.get(0);
        Assert.assertNotNull(report.getRecommendedTargetsIMS());
        Assert.assertEquals(1, report.getRecommendedTargetsIMS().size());
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHV".toLowerCase())));
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("OSP".toLowerCase())));
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHEL".toLowerCase())));
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("None".toLowerCase())));
    }

    @Test
    public void testTargetsXP() {
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "Targets");

        WorkloadInventoryReportModel workloadInventoryReportModel = new WorkloadInventoryReportModel();
        workloadInventoryReportModel.setOsDescription("Microsoft Windows XP");

        facts.put("workloadInventoryReportModel",workloadInventoryReportModel);

        Map<String, Object> results = createAndExecuteCommandsAndGetResults(facts);

        Assert.assertEquals(2, results.get(NUMBER_OF_FIRED_RULE_KEY));
        Utils.verifyRulesFiredNames(this.agendaEventListener, "AgendaFocusForTest", "Target_None");

        List<WorkloadInventoryReportModel> reports = Utils.extractModels(GET_OBJECTS_KEY, results, WorkloadInventoryReportModel.class);

        // just one report has to be created
        Assert.assertEquals(1, reports.size());
        WorkloadInventoryReportModel report = reports.get(0);
        Assert.assertNotNull(report.getRecommendedTargetsIMS());
        Assert.assertEquals(1, report.getRecommendedTargetsIMS().size());
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHV".toLowerCase())));
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("OSP".toLowerCase())));
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHEL".toLowerCase())));
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("None".toLowerCase())));
    }

    @Test
    public void testTargetsDebian() {
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "Targets");

        WorkloadInventoryReportModel workloadInventoryReportModel = new WorkloadInventoryReportModel();
        workloadInventoryReportModel.setOsDescription("Debian");

        facts.put("workloadInventoryReportModel",workloadInventoryReportModel);

        Map<String, Object> results = createAndExecuteCommandsAndGetResults(facts);

        Assert.assertEquals(2, results.get(NUMBER_OF_FIRED_RULE_KEY));
        Utils.verifyRulesFiredNames(this.agendaEventListener, "AgendaFocusForTest", "Target_None");

        List<WorkloadInventoryReportModel> reports = Utils.extractModels(GET_OBJECTS_KEY, results, WorkloadInventoryReportModel.class);

        // just one report has to be created
        Assert.assertEquals(1, reports.size());
        WorkloadInventoryReportModel report = reports.get(0);
        Assert.assertNotNull(report.getRecommendedTargetsIMS());
        Assert.assertEquals(1, report.getRecommendedTargetsIMS().size());
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHV".toLowerCase())));
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("OSP".toLowerCase())));
        Assert.assertFalse(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHEL".toLowerCase())));
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("None".toLowerCase())));
    }

    @Test
    public void testNoCNVTargetCPUAffinityFlagSupportedOS() {
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "Targets");

        WorkloadInventoryReportModel workloadInventoryReportModel = new WorkloadInventoryReportModel();
        workloadInventoryReportModel.setOsDescription("Red Hat Enterprise Linux v7.6");
        workloadInventoryReportModel.addFlagIMS(WorkloadInventoryReportModel.RDM_DISK_FLAG_NAME);
        workloadInventoryReportModel.addFlagIMS(WorkloadInventoryReportModel.CPU_AFFINITY_FLAG_NAME);

        facts.put("workloadInventoryReportModel",workloadInventoryReportModel);

        Map<String, Object> results = createAndExecuteCommandsAndGetResults(facts);

        Assert.assertEquals(2, results.get(NUMBER_OF_FIRED_RULE_KEY));
        Utils.verifyRulesFiredNames(this.agendaEventListener, "AgendaFocusForTest", "Target_RHV");

        List<WorkloadInventoryReportModel> reports = Utils.extractModels(GET_OBJECTS_KEY, results, WorkloadInventoryReportModel.class);

        // just one report has to be created
        Assert.assertEquals(1, reports.size());
        WorkloadInventoryReportModel report = reports.get(0);
        Assert.assertNotNull(report.getRecommendedTargetsIMS());
        Assert.assertEquals(1, report.getRecommendedTargetsIMS().size());
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHV".toLowerCase())));
    }

    @Test
    public void testNoCNVTargetSharedDiskFlagSupportedOS() {
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "Targets");

        WorkloadInventoryReportModel workloadInventoryReportModel = new WorkloadInventoryReportModel();
        workloadInventoryReportModel.setOsDescription("Red Hat Enterprise Linux v7.6");
        workloadInventoryReportModel.addFlagIMS(WorkloadInventoryReportModel.RDM_DISK_FLAG_NAME);
        workloadInventoryReportModel.addFlagIMS(WorkloadInventoryReportModel.SHARED_DISK_FLAG_NAME);

        facts.put("workloadInventoryReportModel",workloadInventoryReportModel);

        Map<String, Object> results = createAndExecuteCommandsAndGetResults(facts);

        Assert.assertEquals(2, results.get(NUMBER_OF_FIRED_RULE_KEY));
        Utils.verifyRulesFiredNames(this.agendaEventListener, "AgendaFocusForTest", "Target_RHV");

        List<WorkloadInventoryReportModel> reports = Utils.extractModels(GET_OBJECTS_KEY, results, WorkloadInventoryReportModel.class);

        // just one report has to be created
        Assert.assertEquals(1, reports.size());
        WorkloadInventoryReportModel report = reports.get(0);
        Assert.assertNotNull(report.getRecommendedTargetsIMS());
        Assert.assertEquals(1, report.getRecommendedTargetsIMS().size());
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHV".toLowerCase())));
    }



    @Test
    public void testNoCNVTargetHotplugFlagSupportedOS() {
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "Targets");

        WorkloadInventoryReportModel workloadInventoryReportModel = new WorkloadInventoryReportModel();
        workloadInventoryReportModel.setOsDescription("Red Hat Enterprise Linux v7.6");
        workloadInventoryReportModel.addFlagIMS(WorkloadInventoryReportModel.RDM_DISK_FLAG_NAME);
        workloadInventoryReportModel.addFlagIMS(WorkloadInventoryReportModel.CPU_MEMORY_HOTPLUG_FLAG_NAME);

        facts.put("workloadInventoryReportModel",workloadInventoryReportModel);

        Map<String, Object> results = createAndExecuteCommandsAndGetResults(facts);

        Assert.assertEquals(2, results.get(NUMBER_OF_FIRED_RULE_KEY));
        Utils.verifyRulesFiredNames(this.agendaEventListener, "AgendaFocusForTest", "Target_RHV");

        List<WorkloadInventoryReportModel> reports = Utils.extractModels(GET_OBJECTS_KEY, results, WorkloadInventoryReportModel.class);

        // just one report has to be created
        Assert.assertEquals(1, reports.size());
        WorkloadInventoryReportModel report = reports.get(0);
        Assert.assertNotNull(report.getRecommendedTargetsIMS());
        Assert.assertEquals(1, report.getRecommendedTargetsIMS().size());
        Assert.assertTrue(report.getRecommendedTargetsIMS().stream().anyMatch(target -> target.toLowerCase().contains("RHV".toLowerCase())));
    }
}
