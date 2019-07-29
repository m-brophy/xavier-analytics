package org.jboss.xavier.analytics.rules.workload.inventory;

import org.jboss.xavier.analytics.pojo.input.workload.inventory.VMWorkloadInventoryModel;
import org.jboss.xavier.analytics.pojo.output.workload.inventory.WorkloadInventoryReportModel;
import org.jboss.xavier.analytics.rules.BaseTest;
import org.jboss.xavier.analytics.test.Utils;


import org.junit.Assert;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.api.io.ResourceType;
import org.kie.internal.command.CommandFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FlagsTest extends BaseTest {

    public FlagsTest() {
        super("/org/jboss/xavier/analytics/rules/workload/inventory/Flags.drl", ResourceType.DRL);
    }

    @Test
    public void test() {
        Utils.checkLoadedRulesNumber(kieSession, "org.jboss.xavier.analytics.rules.workload.inventory", 1);

        Map<String, Object> facts = new HashMap<>();

        VMWorkloadInventoryModel vmWorkloadInventoryModel = new VMWorkloadInventoryModel();
        vmWorkloadInventoryModel.setNicsCount(5);
        vmWorkloadInventoryModel.setHasRdmDisk(true);
        facts.put("vmWorkloadInventoryModel", vmWorkloadInventoryModel);

        WorkloadInventoryReportModel workloadInventoryReportModel = new WorkloadInventoryReportModel();

        facts.put("workloadInventoryReportModel",workloadInventoryReportModel);

        List<Command> commands = new ArrayList<>();
        commands.addAll(Utils.newInsertCommands(facts));
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        commands.add(CommandFactory.newGetObjects(GET_OBJECTS_KEY));

        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        Assert.assertEquals(1, results.get(NUMBER_OF_FIRED_RULE_KEY));
        Utils.verifyRulesFiredNames(this.agendaEventListener, "Flags");

        List<Object> objects = (List<Object>) results.get((GET_OBJECTS_KEY));
        List<WorkloadInventoryReportModel> reports = objects.stream()
                .filter(object -> object instanceof WorkloadInventoryReportModel)
                .map(object -> (WorkloadInventoryReportModel) object)
                .collect(Collectors.toList());

        // just one report has to be created
        Assert.assertEquals(1, reports.size());
        WorkloadInventoryReportModel report = reports.get(0);
        Assert.assertEquals(2,report.getFlagsIMS().size());
        Assert.assertTrue(report.getFlagsIMS().contains(WorkloadInventoryReportModel.MORE_THAN_4_NICS_FLAG_NAME));
        Assert.assertTrue(report.getFlagsIMS().contains(WorkloadInventoryReportModel.RDM_DISK_FLAG_NAME));

    }
}

