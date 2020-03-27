package org.jboss.xavier.analytics.rules.workload.inventory.targets;

import org.jboss.xavier.analytics.pojo.output.workload.inventory.WorkloadInventoryReportModel;
import org.jboss.xavier.analytics.rules.BaseTest;
import org.jboss.xavier.analytics.test.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.io.ResourceType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TargetsReevaluateTest extends BaseTest {

    public TargetsReevaluateTest() {
        super("/org/jboss/xavier/analytics/rules/workload/inventory/targets/TargetsReevaluate.drl", ResourceType.DRL,
                "org.jboss.xavier.analytics.rules.workload.inventory.targets", 1);
    }

    @Test
    public void testFlagSharedDiskAndTargetCNV() {
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "TargetsReevaluate");

        WorkloadInventoryReportModel workloadInventoryReportModel = new WorkloadInventoryReportModel();
        workloadInventoryReportModel.setOsDescription("Red Hat Enterprise Linux v7.6");
        workloadInventoryReportModel.addFlagIMS(WorkloadInventoryReportModel.SHARED_DISK_FLAG_NAME);
        workloadInventoryReportModel.addRecommendedTargetsIMS(WorkloadInventoryReportModel.TARGET_CNV);

        facts.put("workloadInventoryReportModel",workloadInventoryReportModel);

        Map<String, Object> results = createAndExecuteCommandsAndGetResults(facts);

        Assert.assertEquals(2, results.get(NUMBER_OF_FIRED_RULE_KEY));
        Utils.verifyRulesFiredNames(this.agendaEventListener, "AgendaFocusForTest", "Target_CNV_Reevaluate");

        List<WorkloadInventoryReportModel> reports = Utils.extractModels(GET_OBJECTS_KEY, results, WorkloadInventoryReportModel.class);

        // just one report has to be created
        Assert.assertEquals(1, reports.size());
        WorkloadInventoryReportModel report = reports.get(0);
        Assert.assertNotNull(report.getRecommendedTargetsIMS());
        Assert.assertEquals(0, report.getRecommendedTargetsIMS().size());
    }

    @Test
    public void testFlagSharedDiskAndNoTargetCNV() {
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "TargetsReevaluate");

        WorkloadInventoryReportModel workloadInventoryReportModel = new WorkloadInventoryReportModel();
        workloadInventoryReportModel.setOsDescription("CentOS");
        workloadInventoryReportModel.addFlagIMS(WorkloadInventoryReportModel.RDM_DISK_FLAG_NAME);
        workloadInventoryReportModel.addFlagIMS(WorkloadInventoryReportModel.SHARED_DISK_FLAG_NAME);
        workloadInventoryReportModel.addRecommendedTargetsIMS("RHV");

        facts.put("workloadInventoryReportModel",workloadInventoryReportModel);

        Map<String, Object> results = createAndExecuteCommandsAndGetResults(facts);

        Assert.assertEquals(1, results.get(NUMBER_OF_FIRED_RULE_KEY));

        Utils.verifyRulesFiredNames(this.agendaEventListener, "AgendaFocusForTest");


        List<WorkloadInventoryReportModel> reports = Utils.extractModels(GET_OBJECTS_KEY, results, WorkloadInventoryReportModel.class);

        // just one report has to be created
        Assert.assertEquals(1, reports.size());
        WorkloadInventoryReportModel report = reports.get(0);
        Assert.assertNotNull(report.getRecommendedTargetsIMS());
        Assert.assertEquals(1, report.getRecommendedTargetsIMS().size());
        Assert.assertTrue(report.getRecommendedTargetsIMS().contains("RHV"));
    }

    @Test
    public void testNoFlagSharedDiskAndTargetCNV() {
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "TargetsReevaluate");

        WorkloadInventoryReportModel workloadInventoryReportModel = new WorkloadInventoryReportModel();
        workloadInventoryReportModel.setOsDescription("Red Hat Enterprise Linux v7.6");
        workloadInventoryReportModel.addRecommendedTargetsIMS(WorkloadInventoryReportModel.TARGET_CNV);

        facts.put("workloadInventoryReportModel",workloadInventoryReportModel);

        Map<String, Object> results = createAndExecuteCommandsAndGetResults(facts);

        Assert.assertEquals(1, results.get(NUMBER_OF_FIRED_RULE_KEY));
        Utils.verifyRulesFiredNames(this.agendaEventListener, "AgendaFocusForTest");

        List<WorkloadInventoryReportModel> reports = Utils.extractModels(GET_OBJECTS_KEY, results, WorkloadInventoryReportModel.class);

        // just one report has to be created
        Assert.assertEquals(1, reports.size());
        WorkloadInventoryReportModel report = reports.get(0);
        Assert.assertNotNull(report.getRecommendedTargetsIMS());
        Assert.assertEquals(1, report.getRecommendedTargetsIMS().size());
        Assert.assertTrue(report.getRecommendedTargetsIMS().contains(WorkloadInventoryReportModel.TARGET_CNV));
    }

    @Test
    public void testNoFlagSharedDiskAndNoTargetCNV() {
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "TargetsReevaluate");

        WorkloadInventoryReportModel workloadInventoryReportModel = new WorkloadInventoryReportModel();
        workloadInventoryReportModel.setOsDescription("Ubuntu");
        workloadInventoryReportModel.addRecommendedTargetsIMS("None");
        workloadInventoryReportModel.addFlagIMS(WorkloadInventoryReportModel.MORE_THAN_4_NICS_FLAG_NAME);

        facts.put("workloadInventoryReportModel",workloadInventoryReportModel);

        Map<String, Object> results = createAndExecuteCommandsAndGetResults(facts);

        Assert.assertEquals(1, results.get(NUMBER_OF_FIRED_RULE_KEY));
        Utils.verifyRulesFiredNames(this.agendaEventListener, "AgendaFocusForTest");


        List<WorkloadInventoryReportModel> reports = Utils.extractModels(GET_OBJECTS_KEY, results, WorkloadInventoryReportModel.class);

        // just one report has to be created
        Assert.assertEquals(1, reports.size());
        WorkloadInventoryReportModel report = reports.get(0);
        Assert.assertNotNull(report.getRecommendedTargetsIMS());
        Assert.assertEquals(1, report.getRecommendedTargetsIMS().size());
        Assert.assertTrue(report.getRecommendedTargetsIMS().contains("None"));
    }

}
