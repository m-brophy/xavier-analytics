package org.jboss.xavier.analytics.rules.initialcostsaving;

import org.jboss.xavier.analytics.pojo.output.EnvironmentModel;
import org.jboss.xavier.analytics.pojo.output.InitialSavingsEstimationReportModel;
import org.jboss.xavier.analytics.pojo.output.RHVOrderFormModel;
import org.jboss.xavier.analytics.pojo.output.RHVRampUpCostsModel;
import org.jboss.xavier.analytics.pojo.support.initialcostsaving.PricingDataModel;
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

public class RHVOrderFormTest extends BaseTest
{
    public RHVOrderFormTest()
    {
        // provide the name of the DRL file you want to test
        super("/org/jboss/xavier/analytics/rules/initialcostsaving/RHVOrderForm.drl", ResourceType.DRL);
    }

    @Test
    public void testVSphereNoSubsOrGrowth()
    {
        // check that the numbers of rule from the DRL file is the number of rules loaded
        Utils.checkLoadedRulesNumber(kieSession, "org.jboss.xavier.analytics.rules.initialcostsaving", 1);

        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "RHVOrderForm");

        // define the objects needed to define the "When" side of the test
        PricingDataModel pricingDataModel = new PricingDataModel();
        pricingDataModel.setRhvConsultValue(288600.0);
        pricingDataModel.setRhLearningSubsValue(28000.0);
        pricingDataModel.setRhvTAndEValue(55000.0);
        pricingDataModel.setRhvValue(0.73);
        pricingDataModel.setRhvListValue(1498.0);
        pricingDataModel.setRhVirtValue(0.40);
        pricingDataModel.setRhVirtListValue(2798.0);
        // and add each object to the Map
        facts.put("pricingDataModel", pricingDataModel);

        EnvironmentModel environmentModel = new EnvironmentModel();
        environmentModel.setSourceProductIndicator(1);

        RHVRampUpCostsModel rhvRampUpCostsModel = new RHVRampUpCostsModel();
        rhvRampUpCostsModel.setYear1RhvPaidSubs(0);
        rhvRampUpCostsModel.setYear1RhvPaidSubsGrowth(0);
        rhvRampUpCostsModel.setYear2RhvPaidSubs(0);
        rhvRampUpCostsModel.setYear2RhvPaidSubsGrowth(0);
        rhvRampUpCostsModel.setYear3RhvPaidSubs(0);
        rhvRampUpCostsModel.setYear3RhvPaidSubsGrowth(0);


        InitialSavingsEstimationReportModel reportModel = new InitialSavingsEstimationReportModel();
        reportModel.setEnvironmentModel(environmentModel);
        reportModel.setRhvRampUpCostsModel(rhvRampUpCostsModel);
        // added to the facts Map
        facts.put("reportModel", reportModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // last create the command to retrieve the objects available in
        // the working memory at the end of the rules' execution
        commands.add(CommandFactory.newGetObjects(GET_OBJECTS_KEY));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(2, results.get(NUMBER_OF_FIRED_RULE_KEY));

        // retrieve the List of Objects that were available in the working memory from the results
        List<Object> objects = (List<Object>) results.get((GET_OBJECTS_KEY));
        // filter the type of object you're interested in checking (e.g. InitialSavingsEstimationReportModel)
        List<InitialSavingsEstimationReportModel> reports = objects.stream()
                .filter(object -> object instanceof InitialSavingsEstimationReportModel)
                .map(object -> (InitialSavingsEstimationReportModel) object)
                .collect(Collectors.toList());

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, reports.size());

        // Check that the object has exactly the fields that the rule tested should add/change
        InitialSavingsEstimationReportModel report = reports.get(0);
        RHVOrderFormModel rhvOrderFormModel = report.getRhvOrderFormModel();
        Assert.assertEquals("", rhvOrderFormModel.getYear1RhvOrderSku());
        Assert.assertEquals(0, rhvOrderFormModel.getYear1RhvOrder().doubleValue(),0.1);
        Assert.assertEquals(0, rhvOrderFormModel.getYear1RhvOrderListValue().doubleValue(),0.1);
        Assert.assertEquals(0, rhvOrderFormModel.getYear1RhvOrderDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(0, rhvOrderFormModel.getYear1RhvOrderTotalValue().doubleValue(),0.1);


        Assert.assertEquals(2886, rhvOrderFormModel.getYear1RhvOrderConsult().doubleValue(),0.1);
        Assert.assertEquals(100, rhvOrderFormModel.getYear1RhvOrderConsultListValue().doubleValue(),0.1);
        Assert.assertEquals(100, rhvOrderFormModel.getYear1RhvOrderConsultDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(288600, rhvOrderFormModel.getYear1RhvOrderConsultTotalValue().doubleValue(),0.1);
        Assert.assertEquals(1, rhvOrderFormModel.getYear1RhvOrderTAndE().doubleValue(),0.1);
        Assert.assertEquals(55000, rhvOrderFormModel.getYear1RhvOrderTAndEValue().doubleValue(),0.1);
        Assert.assertEquals(55000, rhvOrderFormModel.getYear1RhvOrderTAndEDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(55000, rhvOrderFormModel.getYear1RhvOrderTAndETotalValue().doubleValue(),0.1);
        Assert.assertEquals(7000, rhvOrderFormModel.getYear1RhvOrderLearningSubsListValue().doubleValue(),0.1);
        Assert.assertEquals(4, rhvOrderFormModel.getYear1RhvOrderLearningSubs().doubleValue(),0.1);
        Assert.assertEquals(7000, rhvOrderFormModel.getYear1RhvOrderLearningSubsDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(28000, rhvOrderFormModel.getYear1RhvOrderLearningSubsTotalValue().doubleValue(),0.1);
        Assert.assertEquals(371600, rhvOrderFormModel.getYear1RhvOrderGrandTotal().doubleValue(),0.1);


        Assert.assertEquals("", rhvOrderFormModel.getYear2RhvOrderSku());
        Assert.assertEquals(0, rhvOrderFormModel.getYear2RhvOrder().doubleValue(),0.1);
        Assert.assertEquals(0, rhvOrderFormModel.getYear2RhvOrderListValue().doubleValue(),0.1);
        Assert.assertEquals(0, rhvOrderFormModel.getYear2RhvOrderDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(0, rhvOrderFormModel.getYear2RhvOrderTotalValue().doubleValue(),0.1);
        Assert.assertEquals(0, rhvOrderFormModel.getYear2RhvOrderGrandTotal().doubleValue(),0.1);


        Assert.assertEquals("", rhvOrderFormModel.getYear3RhvOrderSku());
        Assert.assertEquals(0, rhvOrderFormModel.getYear3RhvOrder().doubleValue(),0.1);
        Assert.assertEquals(0, rhvOrderFormModel.getYear3RhvOrderListValue().doubleValue(),0.1);
        Assert.assertEquals(0, rhvOrderFormModel.getYear3RhvOrderDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(0, rhvOrderFormModel.getYear3RhvOrderTotalValue().doubleValue(),0.1);
        Assert.assertEquals(0, rhvOrderFormModel.getYear3RhvOrderGrandTotal().doubleValue(),0.1);

    }

    @Test
    public void testVSpherePaidSubs()
    {
// check that the numbers of rule from the DRL file is the number of rules loaded
        Utils.checkLoadedRulesNumber(kieSession, "org.jboss.xavier.analytics.rules.initialcostsaving", 1);

        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "RHVOrderForm");

        // define the objects needed to define the "When" side of the test
        PricingDataModel pricingDataModel = new PricingDataModel();
        pricingDataModel.setRhvConsultValue(288600.0);
        pricingDataModel.setRhLearningSubsValue(28000.0);
        pricingDataModel.setRhvTAndEValue(55000.0);
        pricingDataModel.setRhvValue(0.73);
        pricingDataModel.setRhvListValue(1498.0);
        pricingDataModel.setRhVirtValue(0.40);
        pricingDataModel.setRhVirtListValue(2798.0);
        // and add each object to the Map
        facts.put("pricingDataModel", pricingDataModel);

        EnvironmentModel environmentModel = new EnvironmentModel();
        environmentModel.setSourceProductIndicator(1);

        RHVRampUpCostsModel rhvRampUpCostsModel = new RHVRampUpCostsModel();
        rhvRampUpCostsModel.setYear1RhvPaidSubs(250);
        rhvRampUpCostsModel.setYear1RhvPaidSubsGrowth(25);
        rhvRampUpCostsModel.setYear2RhvPaidSubs(400);
        rhvRampUpCostsModel.setYear2RhvPaidSubsGrowth(51);
        rhvRampUpCostsModel.setYear3RhvPaidSubs(500);
        rhvRampUpCostsModel.setYear3RhvPaidSubsGrowth(79);


        InitialSavingsEstimationReportModel reportModel = new InitialSavingsEstimationReportModel();
        reportModel.setEnvironmentModel(environmentModel);
        reportModel.setRhvRampUpCostsModel(rhvRampUpCostsModel);
        // added to the facts Map
        facts.put("reportModel", reportModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // last create the command to retrieve the objects available in
        // the working memory at the end of the rules' execution
        commands.add(CommandFactory.newGetObjects(GET_OBJECTS_KEY));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(2, results.get(NUMBER_OF_FIRED_RULE_KEY));

        // retrieve the List of Objects that were available in the working memory from the results
        List<Object> objects = (List<Object>) results.get((GET_OBJECTS_KEY));
        // filter the type of object you're interested in checking (e.g. InitialSavingsEstimationReportModel)
        List<InitialSavingsEstimationReportModel> reports = objects.stream()
                .filter(object -> object instanceof InitialSavingsEstimationReportModel)
                .map(object -> (InitialSavingsEstimationReportModel) object)
                .collect(Collectors.toList());

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, reports.size());

        // Check that the object has exactly the fields that the rule tested should add/change
        InitialSavingsEstimationReportModel report = reports.get(0);
        RHVOrderFormModel rhvOrderFormModel = report.getRhvOrderFormModel();
        Assert.assertEquals("RV0213787", rhvOrderFormModel.getYear1RhvOrderSku());
        Assert.assertEquals(275, rhvOrderFormModel.getYear1RhvOrder().doubleValue(),0.1);
        Assert.assertEquals(1498, rhvOrderFormModel.getYear1RhvOrderListValue().doubleValue(),0.1);
        Assert.assertEquals(404.46, rhvOrderFormModel.getYear1RhvOrderDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(111226.5, rhvOrderFormModel.getYear1RhvOrderTotalValue().doubleValue(),0.1);


        Assert.assertEquals(2886, rhvOrderFormModel.getYear1RhvOrderConsult().doubleValue(),0.1);
        Assert.assertEquals(100, rhvOrderFormModel.getYear1RhvOrderConsultListValue().doubleValue(),0.1);
        Assert.assertEquals(100, rhvOrderFormModel.getYear1RhvOrderConsultDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(288600, rhvOrderFormModel.getYear1RhvOrderConsultTotalValue().doubleValue(),0.1);
        Assert.assertEquals(1, rhvOrderFormModel.getYear1RhvOrderTAndE().doubleValue(),0.1);
        Assert.assertEquals(55000, rhvOrderFormModel.getYear1RhvOrderTAndEValue().doubleValue(),0.1);
        Assert.assertEquals(55000, rhvOrderFormModel.getYear1RhvOrderTAndEDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(55000, rhvOrderFormModel.getYear1RhvOrderTAndETotalValue().doubleValue(),0.1);
        Assert.assertEquals(7000, rhvOrderFormModel.getYear1RhvOrderLearningSubsListValue().doubleValue(),0.1);
        Assert.assertEquals(4, rhvOrderFormModel.getYear1RhvOrderLearningSubs().doubleValue(),0.1);
        Assert.assertEquals(7000, rhvOrderFormModel.getYear1RhvOrderLearningSubsDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(28000, rhvOrderFormModel.getYear1RhvOrderLearningSubsTotalValue().doubleValue(),0.1);
        Assert.assertEquals(482826.5, rhvOrderFormModel.getYear1RhvOrderGrandTotal().doubleValue(),0.1);


        Assert.assertEquals("RV0213787", rhvOrderFormModel.getYear2RhvOrderSku());
        Assert.assertEquals(451, rhvOrderFormModel.getYear2RhvOrder().doubleValue(),0.1);
        Assert.assertEquals(1498, rhvOrderFormModel.getYear2RhvOrderListValue().doubleValue(),0.1);
        Assert.assertEquals(404.46, rhvOrderFormModel.getYear2RhvOrderDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(182411.46, rhvOrderFormModel.getYear2RhvOrderTotalValue().doubleValue(),0.1);
        Assert.assertEquals(182411.46, rhvOrderFormModel.getYear2RhvOrderGrandTotal().doubleValue(),0.1);


        Assert.assertEquals("RV0213787", rhvOrderFormModel.getYear3RhvOrderSku());
        Assert.assertEquals(579, rhvOrderFormModel.getYear3RhvOrder().doubleValue(),0.1);
        Assert.assertEquals(1498, rhvOrderFormModel.getYear3RhvOrderListValue().doubleValue(),0.1);
        Assert.assertEquals(404.46, rhvOrderFormModel.getYear3RhvOrderDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(234182.34, rhvOrderFormModel.getYear3RhvOrderTotalValue().doubleValue(),0.1);
        Assert.assertEquals(234182.34, rhvOrderFormModel.getYear3RhvOrderGrandTotal().doubleValue(),0.1);
    }

    @Test
    public void testVSphereNoPaidSubsButGrowth()
    {
// check that the numbers of rule from the DRL file is the number of rules loaded
        Utils.checkLoadedRulesNumber(kieSession, "org.jboss.xavier.analytics.rules.initialcostsaving", 1);

        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "RHVOrderForm");

        // define the objects needed to define the "When" side of the test
        PricingDataModel pricingDataModel = new PricingDataModel();
        pricingDataModel.setRhvConsultValue(288600.0);
        pricingDataModel.setRhLearningSubsValue(28000.0);
        pricingDataModel.setRhvTAndEValue(55000.0);
        pricingDataModel.setRhvValue(0.73);
        pricingDataModel.setRhvListValue(1498.0);
        pricingDataModel.setRhVirtValue(0.40);
        pricingDataModel.setRhVirtListValue(2798.0);
        // and add each object to the Map
        facts.put("pricingDataModel", pricingDataModel);

        EnvironmentModel environmentModel = new EnvironmentModel();
        environmentModel.setSourceProductIndicator(1);

        RHVRampUpCostsModel rhvRampUpCostsModel = new RHVRampUpCostsModel();
        rhvRampUpCostsModel.setYear1RhvPaidSubs(0);
        rhvRampUpCostsModel.setYear1RhvPaidSubsGrowth(25);
        rhvRampUpCostsModel.setYear2RhvPaidSubs(0);
        rhvRampUpCostsModel.setYear2RhvPaidSubsGrowth(51);
        rhvRampUpCostsModel.setYear3RhvPaidSubs(0);
        rhvRampUpCostsModel.setYear3RhvPaidSubsGrowth(79);


        InitialSavingsEstimationReportModel reportModel = new InitialSavingsEstimationReportModel();
        reportModel.setEnvironmentModel(environmentModel);
        reportModel.setRhvRampUpCostsModel(rhvRampUpCostsModel);
        // added to the facts Map
        facts.put("reportModel", reportModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // last create the command to retrieve the objects available in
        // the working memory at the end of the rules' execution
        commands.add(CommandFactory.newGetObjects(GET_OBJECTS_KEY));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(2, results.get(NUMBER_OF_FIRED_RULE_KEY));

        // retrieve the List of Objects that were available in the working memory from the results
        List<Object> objects = (List<Object>) results.get((GET_OBJECTS_KEY));
        // filter the type of object you're interested in checking (e.g. InitialSavingsEstimationReportModel)
        List<InitialSavingsEstimationReportModel> reports = objects.stream()
                .filter(object -> object instanceof InitialSavingsEstimationReportModel)
                .map(object -> (InitialSavingsEstimationReportModel) object)
                .collect(Collectors.toList());

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, reports.size());

        // Check that the object has exactly the fields that the rule tested should add/change
        InitialSavingsEstimationReportModel report = reports.get(0);
        RHVOrderFormModel rhvOrderFormModel = report.getRhvOrderFormModel();
        Assert.assertEquals("RV0213787", rhvOrderFormModel.getYear1RhvOrderSku());
        Assert.assertEquals(25, rhvOrderFormModel.getYear1RhvOrder().doubleValue(),0.1);
        Assert.assertEquals(1498, rhvOrderFormModel.getYear1RhvOrderListValue().doubleValue(),0.1);
        Assert.assertEquals(404.46, rhvOrderFormModel.getYear1RhvOrderDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(10111.5, rhvOrderFormModel.getYear1RhvOrderTotalValue().doubleValue(),0.1);


        Assert.assertEquals(2886, rhvOrderFormModel.getYear1RhvOrderConsult().doubleValue(),0.1);
        Assert.assertEquals(100, rhvOrderFormModel.getYear1RhvOrderConsultListValue().doubleValue(),0.1);
        Assert.assertEquals(100, rhvOrderFormModel.getYear1RhvOrderConsultDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(288600, rhvOrderFormModel.getYear1RhvOrderConsultTotalValue().doubleValue(),0.1);
        Assert.assertEquals(1, rhvOrderFormModel.getYear1RhvOrderTAndE().doubleValue(),0.1);
        Assert.assertEquals(55000, rhvOrderFormModel.getYear1RhvOrderTAndEValue().doubleValue(),0.1);
        Assert.assertEquals(55000, rhvOrderFormModel.getYear1RhvOrderTAndEDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(55000, rhvOrderFormModel.getYear1RhvOrderTAndETotalValue().doubleValue(),0.1);
        Assert.assertEquals(7000, rhvOrderFormModel.getYear1RhvOrderLearningSubsListValue().doubleValue(),0.1);
        Assert.assertEquals(4, rhvOrderFormModel.getYear1RhvOrderLearningSubs().doubleValue(),0.1);
        Assert.assertEquals(7000, rhvOrderFormModel.getYear1RhvOrderLearningSubsDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(28000, rhvOrderFormModel.getYear1RhvOrderLearningSubsTotalValue().doubleValue(),0.1);
        Assert.assertEquals(381711.5, rhvOrderFormModel.getYear1RhvOrderGrandTotal().doubleValue(),0.1);


        Assert.assertEquals("RV0213787", rhvOrderFormModel.getYear2RhvOrderSku());
        Assert.assertEquals(51, rhvOrderFormModel.getYear2RhvOrder().doubleValue(),0.1);
        Assert.assertEquals(1498, rhvOrderFormModel.getYear2RhvOrderListValue().doubleValue(),0.1);
        Assert.assertEquals(404.46, rhvOrderFormModel.getYear2RhvOrderDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(20627.46, rhvOrderFormModel.getYear2RhvOrderTotalValue().doubleValue(),0.1);
        Assert.assertEquals(20627.46, rhvOrderFormModel.getYear2RhvOrderGrandTotal().doubleValue(),0.1);


        Assert.assertEquals("RV0213787", rhvOrderFormModel.getYear3RhvOrderSku());
        Assert.assertEquals(79, rhvOrderFormModel.getYear3RhvOrder().doubleValue(),0.1);
        Assert.assertEquals(1498, rhvOrderFormModel.getYear3RhvOrderListValue().doubleValue(),0.1);
        Assert.assertEquals(404.46, rhvOrderFormModel.getYear3RhvOrderDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(31952.34, rhvOrderFormModel.getYear3RhvOrderTotalValue().doubleValue(),0.1);
        Assert.assertEquals(31952.34, rhvOrderFormModel.getYear3RhvOrderGrandTotal().doubleValue(),0.1);
    }

    @Test
    public void testNotVSpherePaidSubs()
    {
// check that the numbers of rule from the DRL file is the number of rules loaded
        Utils.checkLoadedRulesNumber(kieSession, "org.jboss.xavier.analytics.rules.initialcostsaving", 1);

        // create a Map with the facts (i.e. Objects) you want to put in the working memory
        Map<String, Object> facts = new HashMap<>();
        // always add a String fact with the name of the agenda group defined in the DRL file (e.g. "SourceCosts")
        facts.put("agendaGroup", "RHVOrderForm");

        // define the objects needed to define the "When" side of the test
        PricingDataModel pricingDataModel = new PricingDataModel();
        pricingDataModel.setRhvConsultValue(288600.0);
        pricingDataModel.setRhLearningSubsValue(28000.0);
        pricingDataModel.setRhvTAndEValue(55000.0);
        pricingDataModel.setRhvValue(0.73);
        pricingDataModel.setRhvListValue(1498.0);
        pricingDataModel.setRhVirtValue(0.40);
        pricingDataModel.setRhVirtListValue(2798.0);
        // and add each object to the Map
        facts.put("pricingDataModel", pricingDataModel);

        EnvironmentModel environmentModel = new EnvironmentModel();
        environmentModel.setSourceProductIndicator(2);

        RHVRampUpCostsModel rhvRampUpCostsModel = new RHVRampUpCostsModel();
        rhvRampUpCostsModel.setYear1RhvPaidSubs(250);
        rhvRampUpCostsModel.setYear1RhvPaidSubsGrowth(25);
        rhvRampUpCostsModel.setYear2RhvPaidSubs(400);
        rhvRampUpCostsModel.setYear2RhvPaidSubsGrowth(51);
        rhvRampUpCostsModel.setYear3RhvPaidSubs(500);
        rhvRampUpCostsModel.setYear3RhvPaidSubsGrowth(79);

        InitialSavingsEstimationReportModel reportModel = new InitialSavingsEstimationReportModel();
        reportModel.setEnvironmentModel(environmentModel);
        reportModel.setRhvRampUpCostsModel(rhvRampUpCostsModel);
        // added to the facts Map
        facts.put("reportModel", reportModel);

        // define the list of commands you want to be executed by Drools
        List<Command> commands = new ArrayList<>();
        // first generate and add all of the facts created above
        commands.addAll(Utils.newInsertCommands(facts));
        // then generate the 'fireAllRules' command
        commands.add(CommandFactory.newFireAllRules(NUMBER_OF_FIRED_RULE_KEY));
        // last create the command to retrieve the objects available in
        // the working memory at the end of the rules' execution
        commands.add(CommandFactory.newGetObjects(GET_OBJECTS_KEY));

        // execute the commands in the KIE session and get the results
        Map<String, Object> results = Utils.executeCommandsAndGetResults(kieSession, commands);

        // check that the number of rules fired is what you expect
        Assert.assertEquals(2, results.get(NUMBER_OF_FIRED_RULE_KEY));

        // retrieve the List of Objects that were available in the working memory from the results
        List<Object> objects = (List<Object>) results.get((GET_OBJECTS_KEY));
        // filter the type of object you're interested in checking (e.g. InitialSavingsEstimationReportModel)
        List<InitialSavingsEstimationReportModel> reports = objects.stream()
                .filter(object -> object instanceof InitialSavingsEstimationReportModel)
                .map(object -> (InitialSavingsEstimationReportModel) object)
                .collect(Collectors.toList());

        // Check that the number of object is the right one (in this case, there must be just one report)
        Assert.assertEquals(1, reports.size());

        // Check that the object has exactly the fields that the rule tested should add/change
        InitialSavingsEstimationReportModel report = reports.get(0);
        RHVOrderFormModel rhvOrderFormModel = report.getRhvOrderFormModel();
        Assert.assertEquals("RV00012", rhvOrderFormModel.getYear1RhvOrderSku());
        Assert.assertEquals(275, rhvOrderFormModel.getYear1RhvOrder().doubleValue(),0.1);
        Assert.assertEquals(2798, rhvOrderFormModel.getYear1RhvOrderListValue().doubleValue(),0.1);
        Assert.assertEquals(1678.8, rhvOrderFormModel.getYear1RhvOrderDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(461670, rhvOrderFormModel.getYear1RhvOrderTotalValue().doubleValue(),0.1);


        Assert.assertEquals(2886, rhvOrderFormModel.getYear1RhvOrderConsult().doubleValue(),0.1);
        Assert.assertEquals(100, rhvOrderFormModel.getYear1RhvOrderConsultListValue().doubleValue(),0.1);
        Assert.assertEquals(100, rhvOrderFormModel.getYear1RhvOrderConsultDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(288600, rhvOrderFormModel.getYear1RhvOrderConsultTotalValue().doubleValue(),0.1);
        Assert.assertEquals(1, rhvOrderFormModel.getYear1RhvOrderTAndE().doubleValue(),0.1);
        Assert.assertEquals(55000, rhvOrderFormModel.getYear1RhvOrderTAndEValue().doubleValue(),0.1);
        Assert.assertEquals(55000, rhvOrderFormModel.getYear1RhvOrderTAndEDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(55000, rhvOrderFormModel.getYear1RhvOrderTAndETotalValue().doubleValue(),0.1);
        Assert.assertEquals(7000, rhvOrderFormModel.getYear1RhvOrderLearningSubsListValue().doubleValue(),0.1);
        Assert.assertEquals(4, rhvOrderFormModel.getYear1RhvOrderLearningSubs().doubleValue(),0.1);
        Assert.assertEquals(7000, rhvOrderFormModel.getYear1RhvOrderLearningSubsDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(28000, rhvOrderFormModel.getYear1RhvOrderLearningSubsTotalValue().doubleValue(),0.1);
        Assert.assertEquals(833270, rhvOrderFormModel.getYear1RhvOrderGrandTotal().doubleValue(),0.1);


        Assert.assertEquals("RV00012", rhvOrderFormModel.getYear2RhvOrderSku());
        Assert.assertEquals(451, rhvOrderFormModel.getYear2RhvOrder().doubleValue(),0.1);
        Assert.assertEquals(2798, rhvOrderFormModel.getYear2RhvOrderListValue().doubleValue(),0.1);
        Assert.assertEquals(1678.8, rhvOrderFormModel.getYear2RhvOrderDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(757138.8, rhvOrderFormModel.getYear2RhvOrderTotalValue().doubleValue(),0.1);
        Assert.assertEquals(757138.8, rhvOrderFormModel.getYear2RhvOrderGrandTotal().doubleValue(),0.1);


        Assert.assertEquals("RV00012", rhvOrderFormModel.getYear3RhvOrderSku());
        Assert.assertEquals(579, rhvOrderFormModel.getYear3RhvOrder().doubleValue(),0.1);
        Assert.assertEquals(2798, rhvOrderFormModel.getYear3RhvOrderListValue().doubleValue(),0.1);
        Assert.assertEquals(1678.8, rhvOrderFormModel.getYear3RhvOrderDiscountValue().doubleValue(),0.1);
        Assert.assertEquals(972025.2, rhvOrderFormModel.getYear3RhvOrderTotalValue().doubleValue(),0.1);
        Assert.assertEquals(972025.2, rhvOrderFormModel.getYear3RhvOrderGrandTotal().doubleValue(),0.1);
    }

}
