/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.smoketest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.collect.Lists;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BSMAdminIT extends OpenNMSSeleniumTestCase {

    /**
     * Class to control the inputs of the "Business Service Edit"-Window
     */
    public static class BsmAdminPageEditWindow {
        private final OpenNMSSeleniumTestCase testCase;
        private final String businessServiceName;

        private BsmAdminPageEditWindow(OpenNMSSeleniumTestCase testCase) {
            this(testCase, null);
        }

        private BsmAdminPageEditWindow(final OpenNMSSeleniumTestCase testCase, final String businessServiceName) {
            this.testCase = Objects.requireNonNull(testCase);
            this.businessServiceName = businessServiceName;
        }

        public BsmAdminPage save() {
            clickElementUntilItDisappears(testCase, By.id("saveButton"));
            testCase.wait.until(testCase.pageContainsText(businessServiceName));
            return new BsmAdminPage(testCase).open();
        }

        public BsmAdminPage cancel() {
            clickElementUntilItDisappears(testCase, By.id("cancelButton"));
            testCase.wait.until(ExpectedConditions.elementToBeClickable(By.id("createButton")));
            return new BsmAdminPage(testCase);
        }

        public BsmAdminPageEditWindow name(String newName) {
            WebElement nameField = testCase.findElementById("nameField");
            nameField.clear();
            nameField.sendKeys(newName);
            nameField.sendKeys(Keys.ENTER);
            return new BsmAdminPageEditWindow(testCase, newName);
        }

        public BsmAdminPageEdgeEditWindow newEdgeWindow() {
            testCase.findElementById("addEdgeButton").click();
            testCase.wait.until(testCase.pageContainsText("Business Service Edge Edit"));
            return new BsmAdminPageEdgeEditWindow(testCase);
        }

        public BsmAdminPageAttributeEditWindow newAttributeWindow() {
            testCase.findElementById("addAttributeButton").click();
            testCase.wait.until(testCase.pageContainsText("Attribute"));
            return new BsmAdminPageAttributeEditWindow(testCase);
        }

        public BsmAdminPageAttributeEditWindow editAttributeWindow() {
            testCase.findElementById("editAttributeButton").click();
            testCase.wait.until(testCase.pageContainsText("Attribute"));
            return new BsmAdminPageAttributeEditWindow(testCase);
        }

        public BsmAdminPageEdgeEditWindow editEdgeWindow() {
            testCase.findElementById("editEdgeButton").click();
            testCase.wait.until(testCase.pageContainsText("Business Service Edge Edit"));
            return new BsmAdminPageEdgeEditWindow(testCase);
        }

        public BsmAdminPageEdgeEditWindow addChildEdge(String childServiceText, String mapFunctionText, int weight) throws InterruptedException {
            BsmAdminPageEdgeEditWindow editPage = newEdgeWindow()
                    .selectMapFunction(mapFunctionText)
                    .selectChildService(childServiceText)
                    .weight(weight)
                    .confirm();
            testCase.wait.until(ExpectedConditions.elementToBeClickable(By.id("addEdgeButton")));
            return editPage;
        }

        public BsmAdminPageEditWindow addReductionKeyEdge(String reductionKeyText, String mapFunctionText, int weight) throws InterruptedException {
            return addReductionKeyEdge(reductionKeyText, mapFunctionText, weight, null);
        }

        public BsmAdminPageEditWindow addReductionKeyEdge(String reductionKeyText, String mapFunctionText, int weight, String friendlyName) throws InterruptedException {
            newEdgeWindow()
                    .selectMapFunction(mapFunctionText)
                    .reductionKey(reductionKeyText)
                    .friendlyName(friendlyName)
                    .weight(weight)
                    .confirm();
            testCase.wait.until(ExpectedConditions.elementToBeClickable(By.id("addEdgeButton")));
            return this;
        }

        public BsmAdminPageEditWindow addAttribute(String key, String value) throws InterruptedException {
            newAttributeWindow()
                    .key(key)
                    .value(value)
                    .confirm();
            return this;
        }

        public BsmAdminPageEditWindow editAttribute(String key, String value) throws InterruptedException {
            getSelectWebElement(testCase, "attributeList").selectByVisibleText(key);
            editAttributeWindow()
                    .value(value)
                    .confirm();
            return this;
        }

        public BsmAdminPageEditWindow editEdge(String edgeValueString, String mapFunctionText, int weight) throws InterruptedException {
            getSelectWebElement(testCase, "edgeList").selectByVisibleText(edgeValueString);
            editEdgeWindow()
                    .selectMapFunction(mapFunctionText)
                    .weight(weight)
                    .confirm();
            testCase.wait.until(ExpectedConditions.elementToBeClickable(By.id("editEdgeButton")));
            return this;
        }

        public BsmAdminPageEditWindow editEdge(String edgeValueString, String mapFunctionText, int weight, String friendlyName) throws InterruptedException {
            getSelectWebElement(testCase, "edgeList").selectByVisibleText(edgeValueString);
            editEdgeWindow()
                    .selectMapFunction(mapFunctionText)
                    .friendlyName(friendlyName)
                    .weight(weight)
                    .confirm();
            testCase.wait.until(ExpectedConditions.elementToBeClickable(By.id("editEdgeButton")));
            return this;
        }

        public BsmAdminPageEditWindow addIpServiceEdge(String ipServiceText, String mapFunctionText, int weight) throws InterruptedException {
            return addIpServiceEdge(ipServiceText, mapFunctionText, weight, null);
        }

        public BsmAdminPageEditWindow addIpServiceEdge(String ipServiceText, String mapFunctionText, int weight, String friendlyName) throws InterruptedException {
            newEdgeWindow()
                    .selectIpService(ipServiceText)
                    .friendlyName(friendlyName)
                    .selectMapFunction(mapFunctionText)
                    .weight(weight)
                    .confirm();
            testCase.wait.until(ExpectedConditions.elementToBeClickable(By.id("addEdgeButton")));
            return this;
        }

        public BsmAdminPageEditWindow removeEdge(String edgeValueString) {
            getSelectWebElement(testCase, "edgeList").selectByVisibleText(edgeValueString);
            testCase.findElementById("removeEdgeButton").click();
            return this;
        }

        public BsmAdminPageEditWindow setReductionFunction(final String reductionFunctionValueString) {
            getSelectWebElement(testCase, "reduceFunctionNativeSelect").selectByVisibleText(reductionFunctionValueString);
            return this;
        }

        public BsmAdminPageEditWindow setThreshold(final float threshold) {
            testCase.findElementById("thresholdTextField").clear();
            testCase.findElementById("thresholdTextField").sendKeys(String.valueOf(threshold));
            return this;
        }

        public BsmAdminPageEditWindow setThresholdStatus(String thresholdStatusString) {
            getSelectWebElement(testCase, "thresholdStatusSelect").selectByVisibleText(thresholdStatusString);
            return this;
        }
    }

    /**
     * Class to control the inputs and workflow of the "Business Service Admin" Page
     */
    public static class BsmAdminPage {
        private final OpenNMSSeleniumTestCase testCase;
        private final String bsmAdminUrl;

        public BsmAdminPage(final OpenNMSSeleniumTestCase testCase) {
            this.testCase = Objects.requireNonNull(testCase);
            this.bsmAdminUrl = testCase.getBaseUrl() + "opennms/admin/bsm/adminpage.jsp";
        }

        public BsmAdminPage open() {
            testCase.m_driver.get(bsmAdminUrl);
            switchToVaadinFrame(testCase);
            return this;
        }

        public BsmAdminPageEditWindow openNewDialog(String businessServiceName) throws InterruptedException {
            testCase.findElementById("createButton").click();
            testCase.wait.until(testCase.pageContainsText("Business Service Edit")); // we wait until the edit dialog appears
            return new BsmAdminPageEditWindow(testCase).name(businessServiceName);
        }

        public BsmAdminPageEditWindow openEditDialog(String businessServiceName) {
            testCase.findElementById("editButton-" + businessServiceName).click();
            testCase.wait.until(testCase.pageContainsText("Business Service Edit"));
            return new BsmAdminPageEditWindow(testCase, businessServiceName);
        }

        public void delete(String businessServiceName) {
            delete(businessServiceName, false);
        }

        public void rename(String serviceName, String newServiceName) {
            openEditDialog(serviceName).name(newServiceName).save();
        }

        public void delete(String serviceName, boolean withConfirmDialog) {
            findDeleteButton(testCase, serviceName).click();
            if (withConfirmDialog) { // we remove the parent element first, the confirm dialog must be present
                testCase.findElementById("confirmationDialog.button.ok").click();
            }
            verifyElementNotPresent(testCase, By.id("deleteButton-" + serviceName));
        }

        public void collapseAll() {
            testCase.findElementById("collapseButton").click();
        }

        public void expandAll() {
            testCase.findElementById("expandButton").click();
        }

        public void reloadDaemon() {
            testCase.findElementById("reloadButton").click();
        }
    }

    public static class BsmAdminPageEdgeEditWindow {
        private final OpenNMSSeleniumTestCase testCase;

        private BsmAdminPageEdgeEditWindow(final OpenNMSSeleniumTestCase testCase) {
            this.testCase = Objects.requireNonNull(testCase);
        }

        private BsmAdminPageEdgeEditWindow selectEdgeType(String edgeType) {
            getSelectWebElement(testCase, "edgeTypeSelector").selectByVisibleText(edgeType);
            return this;
        }

        public BsmAdminPageEdgeEditWindow selectIpService(String ipServiceText) {
            selectEdgeType("IP Service");
            testCase.enterText(By.xpath("//div[@id='ipServiceList']/input[1]"), ipServiceText);
            // Click on the item that appears
            testCase.findElementByXpath("//span[text()='" + ipServiceText + "']").click();
            return this;
        }

        public BsmAdminPageEdgeEditWindow selectChildService(String childServiceText) {
            selectEdgeType("Child Service");
            testCase.enterText(By.xpath("//div[@id='childServiceList']/input[1]"), childServiceText);
            // Click on the item that appears
            testCase.findElementByXpath("//span[text()='" + childServiceText + "']").click();
            return this;
        }

        public BsmAdminPageEdgeEditWindow selectMapFunction(String mapFunctionText) {
            getSelectWebElement(testCase, "mapFunctionSelector").selectByVisibleText(mapFunctionText);
            return this;
        }

        public BsmAdminPageEdgeEditWindow friendlyName(String friendlyName) throws InterruptedException {
            testCase.enterText(By.id("friendlyNameField"), friendlyName != null ? friendlyName : "");
            testCase.findElementById("friendlyNameField").sendKeys(Keys.ENTER);
            return this;
        }

        public BsmAdminPageEdgeEditWindow reductionKey(String reductionKey) throws InterruptedException {
            selectEdgeType("Reduction Key");
            testCase.enterText(By.id("reductionKeyField"), reductionKey);
            testCase.findElementById("reductionKeyField").sendKeys(Keys.ENTER);
            return this;
        }

        public BsmAdminPageEdgeEditWindow weight(int weight) {
            testCase.enterText(By.id("weightField"), String.valueOf(weight));
            testCase.findElementById("weightField").sendKeys(Keys.ENTER);
            return this;
        }

        public BsmAdminPageEdgeEditWindow confirm() {
            clickElementUntilItDisappears(testCase, By.id("saveEdgeButton"));
            testCase.wait.until(testCase.pageContainsText("Business Service Edit"));
            return new BsmAdminPageEdgeEditWindow(testCase);
        }
    }

    public static class BsmAdminPageAttributeEditWindow {
        private final OpenNMSSeleniumTestCase testCase;

        private BsmAdminPageAttributeEditWindow(final OpenNMSSeleniumTestCase testCase) {
            this.testCase = Objects.requireNonNull(testCase);
        }

        public BsmAdminPageAttributeEditWindow key(String key) throws InterruptedException {
            testCase.findElementById("keyField").clear();
            testCase.findElementById("keyField").sendKeys(key);
            return this;
        }

        public BsmAdminPageAttributeEditWindow value(String value) {
            testCase.findElementById("valueField").clear();
            testCase.findElementById("valueField").sendKeys(value);
            return this;
        }

        public BsmAdminPageEdgeEditWindow confirm() {
            testCase.findElementById("okBtn").click();
            testCase.wait.until(testCase.pageContainsText("Business Service Edit"));
            return new BsmAdminPageEdgeEditWindow(testCase);
        }
    }

    private BsmAdminPage bsmAdminPage;

    private void createTestSetup() throws Exception {
        String foreignSourceXML = "<foreign-source name=\"" + OpenNMSSeleniumTestCase.REQUISITION_NAME + "\">\n" +
                "<scan-interval>1d</scan-interval>\n" +
                "<detectors/>\n" +
                "<policies/>\n" +
                "</foreign-source>";
        createForeignSource(REQUISITION_NAME, foreignSourceXML);

        String requisitionXML = "<model-import foreign-source=\"" + OpenNMSSeleniumTestCase.REQUISITION_NAME + "\">" +
                "   <node foreign-id=\"NodeA\" node-label=\"NodeA\">" +
                "       <interface ip-addr=\"::1\" status=\"1\" snmp-primary=\"N\">" +
                "           <monitored-service service-name=\"AAA\"/>" +
                "           <monitored-service service-name=\"BBB\"/>" +
                "       </interface>" +
                "       <interface ip-addr=\"127.0.0.1\" status=\"1\" snmp-primary=\"N\">" +
                "           <monitored-service service-name=\"CCC\"/>" +
                "           <monitored-service service-name=\"DDD\"/>" +
                "       </interface>" +
                "   </node>" +
                "</model-import>";
        createRequisition(REQUISITION_NAME, requisitionXML, 1);
    }

    private void removeTestSetup() throws Exception {
        deleteTestRequisition();
    }

    @Before
    public void before() throws Exception {
        bsmAdminPage = new BsmAdminPage(this);
        bsmAdminPage.open();
    }

    @After
    public void after() throws Exception {
        switchToDefaultFrame(this);
    }

    @Test
    public void testCanCreateAndDeleteBusinessService() throws InterruptedException {
        final String businessServiceName = createUniqueBusinessServiceName();
        bsmAdminPage.openNewDialog(businessServiceName).save();
        bsmAdminPage.delete(businessServiceName);
    }

    @Test
    public void testCanCreateAndDeleteBusinessServiceWithThreshold() throws InterruptedException {
        final String businessServiceName = createUniqueBusinessServiceName();
        bsmAdminPage.openNewDialog(businessServiceName)
                    .setReductionFunction("Threshold")
                    .setThreshold(0.25f)
                    .save();

        bsmAdminPage.delete(businessServiceName);
    }

    @Test
    public void testCanRenameBusinessService() throws InterruptedException {
        final String serviceName = createUniqueBusinessServiceName();
        bsmAdminPage.openNewDialog(serviceName).save();

        // rename business Service
        final String RENAMED_SERVICE_NAME = "renamed_service" + createUniqueBusinessServiceName();
        bsmAdminPage.rename(serviceName, RENAMED_SERVICE_NAME);

        // verify that element was deleted
        verifyElementNotPresent(this, By.id("deleteButton-" + serviceName));
        pageContainsText(RENAMED_SERVICE_NAME);
        bsmAdminPage.delete(RENAMED_SERVICE_NAME);
    }

    @Test
    public void testCanAddIpServiceEdge() throws Exception {
        try {
            createTestSetup();

            // Create a BusinessService and open editor
            final String serviceName = createUniqueBusinessServiceName();
            BsmAdminPageEditWindow bsmAdminPageEditWindow = bsmAdminPage.openNewDialog(serviceName);

            BsmAdminPageEdgeEditWindow bsmAdminPageEdgeEditWindow = bsmAdminPageEditWindow.newEdgeWindow();
            bsmAdminPageEdgeEditWindow.selectMapFunction("Increase");
            bsmAdminPageEdgeEditWindow.selectEdgeType("IP Service");

            // All of these IP services should be available for selection from the drop-down
            final List<String> ipServices = Lists.newArrayList(
                    "NodeA /0:0:0:0:0:0:0:1 AAA",
                    "NodeA /0:0:0:0:0:0:0:1 BBB",
                    "NodeA /127.0.0.1 DDD",
                    "NodeA /127.0.0.1 CCC");
            // Try selecting each of the services, we'll use the last one in the list
            for (String ipService : ipServices) {
                bsmAdminPageEdgeEditWindow.selectIpService(ipService);
            }

            // save
            bsmAdminPageEdgeEditWindow.confirm();
            bsmAdminPageEditWindow.save();

            // Verify
            bsmAdminPage.openEditDialog(serviceName);
            wait.until(pageContainsText("IPSvc: NodeA /127.0.0.1 CCC, Map: Increase, Weight: 1"));

            // Close dialog and delete BusinessService
            bsmAdminPageEditWindow.cancel();
            bsmAdminPage.delete(serviceName);
        } finally {
            removeTestSetup();
        }
    }

    @Test
    public void testCanAddBusinessServiceEdge() throws Exception {
        // Create a bunch of business services
        final String serviceName = createUniqueBusinessServiceName();
        final String[] serviceNames = new String[]{
                serviceName + "_1",
                serviceName + "_2",
                serviceName + "_3"};
        for (String eachServiceName : serviceNames) {
            bsmAdminPage.openNewDialog(eachServiceName).save();
        }

        // Open Edit Dialog for 1st Business Service which was just created
        final BsmAdminPageEditWindow bsmAdminPageEditWindow = bsmAdminPage.openEditDialog(serviceNames[0]);
        bsmAdminPageEditWindow.addChildEdge(serviceNames[1], "Decrease", 2);
        bsmAdminPageEditWindow.addChildEdge(serviceNames[2], "Ignore", 3);

        // verify
        wait.until(pageContainsText(String.format("Child: %s, Map: Decrease, Weight: 2", serviceNames[1])));
        wait.until(pageContainsText(String.format("Child: %s, Map: Ignore, Weight: 3", serviceNames[2])));
        bsmAdminPageEditWindow.save();
        Thread.sleep(500); // pause

        // Verify that business service are gone
        // we have to delete backwards
        for (int i = 0; i < serviceNames.length; i++) {
            final String eachServiceName = serviceNames[i];
            bsmAdminPage.delete(eachServiceName, i==0);
        }
    }

    @Test
    public void testCanAddReductionKeyEdge() throws InterruptedException {
        final String serviceName = createUniqueBusinessServiceName();
        BsmAdminPageEditWindow bsmAdminPageEditWindow = bsmAdminPage.openNewDialog(serviceName);
        bsmAdminPageEditWindow.addReductionKeyEdge("test.rk.1", "Increase", 17);

        // verify edit
        wait.until(pageContainsText("ReKey: test.rk.1, Map: Increase, Weight: 17"));
        bsmAdminPageEditWindow.save();

        // verify save
        bsmAdminPage.openEditDialog(serviceName);
        wait.until(pageContainsText("ReKey: test.rk.1, Map: Increase, Weight: 17"));
        bsmAdminPageEditWindow.cancel();

        // clean up
        bsmAdminPage.delete(serviceName);
    }

    @Test
    public void testCanAddMixedTypeEdges() throws Exception {
        try {
            // Initialize services
            createTestSetup();
            final String parentServiceName = createUniqueBusinessServiceName();
            final String child1 = parentServiceName + "_child1";
            final String child2 = parentServiceName + "_child2";
            bsmAdminPage.openNewDialog(parentServiceName).save();
            bsmAdminPage.openNewDialog(child1).save();
            bsmAdminPage.openNewDialog(child2).save();

            // define edges
            final BsmAdminPageEditWindow bsmAdminPageEditWindow = bsmAdminPage.openEditDialog(parentServiceName);
            bsmAdminPageEditWindow.addChildEdge(child1, "Identity", 1);
            bsmAdminPageEditWindow.addChildEdge(child2, "Ignore", 1);
            bsmAdminPageEditWindow.addIpServiceEdge("NodeA /0:0:0:0:0:0:0:1 BBB", "Identity", 1);
            bsmAdminPageEditWindow.addIpServiceEdge("NodeA /127.0.0.1 CCC", "Ignore", 1);
            bsmAdminPageEditWindow.addReductionKeyEdge("test.rk.1", "Decrease", 1);
            bsmAdminPageEditWindow.addReductionKeyEdge("test.rk.2", "Increase", 1);

            // verify edit dialog
            wait.until(pageContainsText(String.format("Child: %s, Map: Identity, Weight: 1", child1)));
            wait.until(pageContainsText(String.format("Child: %s, Map: Ignore, Weight: 1", child2)));
            wait.until(pageContainsText("IPSvc: NodeA /0:0:0:0:0:0:0:1 BBB, Map: Identity, Weight: 1"));
            wait.until(pageContainsText("IPSvc: NodeA /127.0.0.1 CCC, Map: Ignore, Weight: 1"));
            wait.until(pageContainsText("ReKey: test.rk.1, Map: Decrease, Weight: 1"));
            wait.until(pageContainsText("ReKey: test.rk.2, Map: Increase, Weight: 1"));

            // verify after save
            bsmAdminPageEditWindow.save();
            bsmAdminPage.openEditDialog(parentServiceName);
            wait.until(pageContainsText(String.format("Child: %s, Map: Identity, Weight: 1", child1)));
            wait.until(pageContainsText(String.format("Child: %s, Map: Ignore, Weight: 1", child2)));
            wait.until(pageContainsText("IPSvc: NodeA /0:0:0:0:0:0:0:1 BBB, Map: Identity, Weight: 1"));
            wait.until(pageContainsText("IPSvc: NodeA /127.0.0.1 CCC, Map: Ignore, Weight: 1"));
            wait.until(pageContainsText("ReKey: test.rk.1, Map: Decrease, Weight: 1"));
            wait.until(pageContainsText("ReKey: test.rk.2, Map: Increase, Weight: 1"));
            bsmAdminPageEditWindow.cancel();

            // cleanup
            bsmAdminPage.expandAll();
            bsmAdminPage.delete(child2, true);
            bsmAdminPage.delete(child1, true);
            bsmAdminPage.delete(parentServiceName);
        } finally {
            removeTestSetup();
        }
    }

    @Test
    public void testCanRemovePersistedEdge() throws InterruptedException {
        // create Business Service with one Edge and persist
        final String serviceName = createUniqueBusinessServiceName();
        BsmAdminPageEditWindow bsmAdminPageEditWindow = bsmAdminPage.openNewDialog(serviceName).addReductionKeyEdge("some.reduction.key", "Increase", 1);
        wait.until(pageContainsText("ReKey: some.reduction.key, Map: Increase, Weight: 1"));
        bsmAdminPageEditWindow.save();

        // remove persisted edge
        bsmAdminPageEditWindow = bsmAdminPage.openEditDialog(serviceName);
        bsmAdminPageEditWindow.removeEdge("ReKey: some.reduction.key, Map: Increase, Weight: 1");
        verifyElementNotPresent("ReKey: some.reduction.key, Map: Increase, Weight: 1");
        bsmAdminPageEditWindow.save();

        // clean up afterwards
        bsmAdminPage.delete(serviceName);
    }

    @Test
    public void testCanRemoveTransientEdge() throws InterruptedException {
        // create Business Service with one Edge and persist
        final String serviceName = createUniqueBusinessServiceName();
        BsmAdminPageEditWindow bsmAdminPageEditWindow = bsmAdminPage.openNewDialog(serviceName).addReductionKeyEdge("some.reduction.key", "Increase", 1);
        wait.until(pageContainsText("ReKey: some.reduction.key, Map: Increase, Weight: 1"));
        // remove transient edge
        bsmAdminPageEditWindow.removeEdge("ReKey: some.reduction.key, Map: Increase, Weight: 1");
        verifyElementNotPresent("ReKey: some.reduction.key, Map: Increase, Weight: 1");
        bsmAdminPageEditWindow.cancel();

        verifyElementNotPresent(serviceName);
    }

    @Test
    public void testCanCancelEdit() throws InterruptedException {
        // create service to edit
        final String serviceName = createUniqueBusinessServiceName();
        bsmAdminPage.openNewDialog(serviceName).save();

        // edit
        BsmAdminPageEditWindow bsmAdminPageEditWindow = bsmAdminPage.openEditDialog(serviceName);
        bsmAdminPageEditWindow.addReductionKeyEdge("test.rk.1", "Identity", 1);
        bsmAdminPageEditWindow.addReductionKeyEdge("test.rk.2", "Identity", 1);
        verifyElementPresent("ReKey: test.rk.1, Map: Identity, Weight: 1");
        verifyElementPresent("ReKey: test.rk.2, Map: Identity, Weight: 1");

        // cancel
        bsmAdminPageEditWindow.cancel();

        // verify that cancel worked
        bsmAdminPageEditWindow = bsmAdminPage.openEditDialog(serviceName);
        verifyElementNotPresent("ReKey: test.rk.1, Map: Identity, Weight: 1");
        verifyElementNotPresent("ReKey: test.rk.2, Map: Identity, Weight: 1");
        bsmAdminPageEditWindow.cancel();

        // clean up
        bsmAdminPage.delete(serviceName);
    }

    @Test
    public void testCanSetHighestSeverityAbove() throws InterruptedException {
        // Create
        final String serviceName = createUniqueBusinessServiceName();
        BsmAdminPageEditWindow editWindow = bsmAdminPage.openNewDialog(serviceName);
        editWindow.setReductionFunction("HighestSeverityAbove");
        editWindow.setThresholdStatus("Major");
        editWindow.save();

        // Verify Reduce Function selection
        BsmAdminPageEditWindow editDialog = bsmAdminPage.openEditDialog(serviceName);
        List<WebElement> reduceFunctionSelect = getSelectWebElement(this, "reduceFunctionNativeSelect").getAllSelectedOptions();
        Assert.assertEquals(1, reduceFunctionSelect.size());
        Assert.assertEquals("HighestSeverityAbove", reduceFunctionSelect.get(0).getText());

        // Verify Reduce Function Status Threshold
        List<WebElement> thresholdStatusSelect = getSelectWebElement(this, "thresholdStatusSelect").getAllSelectedOptions();
        Assert.assertEquals(1, thresholdStatusSelect.size());
        Assert.assertEquals("Major", thresholdStatusSelect.get(0).getText());
        editDialog.cancel();

        // Clean up
        bsmAdminPage.delete(serviceName);
    }

    // switches to the embedded vaadin iframe
    private static void switchToVaadinFrame(OpenNMSSeleniumTestCase testCase) {
        testCase.m_driver.switchTo().frame(testCase.findElementByXpath("/html/body/div/iframe"));
    }

    // go back to the content "frame"
    private static void switchToDefaultFrame(OpenNMSSeleniumTestCase testCase) {
        testCase.m_driver.switchTo().defaultContent();
    }

    // If we use the same name over and over again tests may pass, even if we did not create/delete items,
    // therefore we create a unique business service name all the time
    public static String createUniqueBusinessServiceName() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date());
    }

    private static WebElement findDeleteButton(OpenNMSSeleniumTestCase testCase, String serviceName) {
        return testCase.findElementById("deleteButton-" + serviceName);
    }

    private void verifyElementNotPresent(String text) {
        final String escapedText = text.replace("\'", "\\\'");
        final String xpathExpression = "//*[contains(., \'" + escapedText + "\')]";
        verifyElementNotPresent(this, By.xpath(xpathExpression));
    }

    private void verifyElementPresent(String text) {
        final String escapedText = text.replace("\'", "\\\'");
        final String xpathExpression = "//*[contains(., \'" + escapedText + "\')]";
        Assert.assertNotNull("element with xpath is not present: " + xpathExpression, findElementByXpath(xpathExpression));
    }

    /**
     * Verifies that the provided element is not present.
     * @param by
     */
    private static void verifyElementNotPresent(OpenNMSSeleniumTestCase testCase, final By by) {
        new WebDriverWait(testCase.m_driver, 5 /* seconds */).until(
                ExpectedConditions.not(new ExpectedCondition<Boolean>() {
                    @Nullable
                    @Override
                    public Boolean apply(@Nullable WebDriver input) {
                        try {
                            // the default implicit wait timeout is too long, make it shorter
                            input.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
                            WebElement elementFound = input.findElement(by);
                            return elementFound != null;
                        } catch (NoSuchElementException ex) {
                            return false;
                        } finally {
                            // set the implicit wait timeout back to the value it has been before
                            input.manage().timeouts().implicitlyWait(LOAD_TIMEOUT, TimeUnit.MILLISECONDS);
                        }
                    }
                })
        );
    }

    @Test
    public void testCanEditTransientEdge() throws InterruptedException {
        // create Business Service with one Edge and persist
        final String serviceName = createUniqueBusinessServiceName();
        BsmAdminPageEditWindow bsmAdminPageEditWindow = bsmAdminPage.openNewDialog(serviceName).addReductionKeyEdge("some.reduction.key", "Increase", 1);
        wait.until(pageContainsText("ReKey: some.reduction.key, Map: Increase, Weight: 1"));
        // remove transient edge
        bsmAdminPageEditWindow.editEdge("ReKey: some.reduction.key, Map: Increase, Weight: 1", "Decrease", 2);
        verifyElementPresent("ReKey: some.reduction.key, Map: Decrease, Weight: 2");
        bsmAdminPageEditWindow.cancel();
    }

    @Test
    public void testCanEditPersistedEdge() throws InterruptedException {
        // create Business Service with one Edge and persist
        final String serviceName = createUniqueBusinessServiceName();
        BsmAdminPageEditWindow bsmAdminPageEditWindow = bsmAdminPage.openNewDialog(serviceName).addReductionKeyEdge("some.reduction.key", "Increase", 1);
        wait.until(pageContainsText("ReKey: some.reduction.key, Map: Increase, Weight: 1"));
        bsmAdminPageEditWindow.save();

        // remove persisted edge
        bsmAdminPageEditWindow = bsmAdminPage.openEditDialog(serviceName);
        bsmAdminPageEditWindow.editEdge("ReKey: some.reduction.key, Map: Increase, Weight: 1", "Decrease", 2);
        verifyElementPresent("ReKey: some.reduction.key, Map: Decrease, Weight: 2");
        bsmAdminPageEditWindow.save();

        // clean up afterwards
        bsmAdminPage.delete(serviceName);
    }

    @Test
    public void testCanEditTransientReductionKeyFriendlyName() throws InterruptedException {
        // create Business Service with one Edge and persist
        final String serviceName = createUniqueBusinessServiceName();
        BsmAdminPageEditWindow bsmAdminPageEditWindow = bsmAdminPage.openNewDialog(serviceName).addReductionKeyEdge("some.reduction.key", "Increase", 1, "so-friendly");
        wait.until(pageContainsText("ReKey: some.reduction.key (so-friendly), Map: Increase, Weight: 1"));
        // remove transient edge
        bsmAdminPageEditWindow.editEdge("ReKey: some.reduction.key (so-friendly), Map: Increase, Weight: 1", "Decrease", 2, "very-friendly");
        verifyElementPresent("ReKey: some.reduction.key (very-friendly), Map: Decrease, Weight: 2");
        bsmAdminPageEditWindow.cancel();
    }

    @Test
    public void testCanEditPersistedReductionKeyFriendlyName() throws InterruptedException {
        // create Business Service with one Edge and persist
        final String serviceName = createUniqueBusinessServiceName();
        BsmAdminPageEditWindow bsmAdminPageEditWindow = bsmAdminPage.openNewDialog(serviceName).addReductionKeyEdge("some.reduction.key", "Increase", 1, "so-friendly");
        wait.until(pageContainsText("ReKey: some.reduction.key (so-friendly), Map: Increase, Weight: 1"));
        bsmAdminPageEditWindow.save();

        // remove persisted edge
        bsmAdminPageEditWindow = bsmAdminPage.openEditDialog(serviceName);
        bsmAdminPageEditWindow.editEdge("ReKey: some.reduction.key (so-friendly), Map: Increase, Weight: 1", "Decrease", 2, "very-friendly");
        verifyElementPresent("ReKey: some.reduction.key (very-friendly), Map: Decrease, Weight: 2");
        bsmAdminPageEditWindow.save();

        // clean up afterwards
        bsmAdminPage.delete(serviceName);
    }

    @Test
    public void testCanEditTransientAttribute() throws InterruptedException {
        // create Business Service with one attribute and persist
        final String serviceName = createUniqueBusinessServiceName();
        BsmAdminPageEditWindow bsmAdminPageEditWindow = bsmAdminPage.openNewDialog(serviceName).addAttribute("foo", "bar");
        wait.until(pageContainsText("foo=bar"));
        bsmAdminPageEditWindow.save();

        // add another attribute
        bsmAdminPageEditWindow = bsmAdminPage.openEditDialog(serviceName).editAttribute("foo=bar", "123");
        verifyElementPresent("foo=123");

        // do not persist
        bsmAdminPageEditWindow.cancel();

        // checke  that the attribute is not set
        bsmAdminPageEditWindow = bsmAdminPage.openEditDialog(serviceName);
        verifyElementNotPresent("foo=123");

        bsmAdminPageEditWindow.cancel();

        // clean up afterwards
        bsmAdminPage.delete(serviceName);
    }

    @Test
    public void testCanEditPersistedAttribute() throws InterruptedException {
        // create Business Service with one attribute and persist
        final String serviceName = createUniqueBusinessServiceName();
        BsmAdminPageEditWindow bsmAdminPageEditWindow = bsmAdminPage.openNewDialog(serviceName).addAttribute("foo", "bar");
        wait.until(pageContainsText("foo=bar"));
        bsmAdminPageEditWindow.save();

        // edit attribute
        bsmAdminPageEditWindow = bsmAdminPage.openEditDialog(serviceName).editAttribute("foo=bar", "123");
        verifyElementPresent("foo=123");
        bsmAdminPageEditWindow.save();

        // clean up afterwards
        bsmAdminPage.delete(serviceName);
    }

    @Test
    public void testCanExpandAndCollapse() throws Exception {
        // Create two business services
        final String serviceName = createUniqueBusinessServiceName();
        final String[] serviceNames = new String[]{
                serviceName + "_1",
                serviceName + "_2"};
        for (String eachServiceName : serviceNames) {
            bsmAdminPage.openNewDialog(eachServiceName).save();
        }

        // Set the second business service as a child of the first
        final BsmAdminPageEditWindow bsmAdminPageEditWindow = bsmAdminPage.openEditDialog(serviceNames[0]);
        bsmAdminPageEditWindow.addChildEdge(serviceNames[1], "Ignore", 1);
        bsmAdminPageEditWindow.save();

        // If we collapse, we should not be able to see the child
        bsmAdminPage.collapseAll();
        verifyElementNotPresent(this, By.id("deleteButton-" + serviceNames[1]));

        // If we expand, we should be able to see the child
        bsmAdminPage.expandAll();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("deleteButton-" + serviceNames[1])));

        // Delete the business services
        for (int i = 0; i < serviceNames.length; i++) {
            final String eachServiceName = serviceNames[i];
            bsmAdminPage.delete(eachServiceName, i==0);
        }
    }

    /**
     * Vaadin usually wraps the select elements around a div element.
     * This method considers this.
     *
     * @param id
     * @return
     */
    private static Select getSelectWebElement(OpenNMSSeleniumTestCase testCase, String id) {
        return new Select(testCase.findElementByXpath("//*[@id=\"" + id + "\"]/select"));
    }

    /**
     * In some cases, Vaadin doesn't register our clicks,
     * so this method keeps click until the given element
     * is no longer found.
     *
     * @param by selector
     */
    private static void clickElementUntilItDisappears(OpenNMSSeleniumTestCase testCase, By by) {
        try {
            testCase.setImplicitWait(100, TimeUnit.MILLISECONDS);
            testCase.wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    try {
                        driver.findElement(by).click();
                        return false;
                    } catch (NoSuchElementException e) {
                        return true;
                    }
                }
            });
        } finally {
            testCase.setImplicitWait();
        }
    }
}
