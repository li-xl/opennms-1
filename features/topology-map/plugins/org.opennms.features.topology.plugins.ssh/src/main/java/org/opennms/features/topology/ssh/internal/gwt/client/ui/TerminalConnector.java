package org.opennms.features.topology.ssh.internal.gwt.client.ui;

import org.opennms.features.topology.ssh.internal.SSHTerminal;

import com.vaadin.client.ui.LegacyConnector;
import com.vaadin.shared.ui.Connect;

@Connect(SSHTerminal.class)
public class TerminalConnector extends LegacyConnector {

	@Override
    public VTerminal getWidget() {
        return (VTerminal) super.getWidget();
    }
}
