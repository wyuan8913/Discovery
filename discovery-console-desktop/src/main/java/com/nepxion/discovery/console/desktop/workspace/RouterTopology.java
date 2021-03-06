package com.nepxion.discovery.console.desktop.workspace;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import twaver.BlinkingRule;
import twaver.Element;
import twaver.Generator;
import twaver.TWaverConst;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.util.List;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.nepxion.cots.twaver.element.TLink;
import com.nepxion.cots.twaver.element.TNode;
import com.nepxion.cots.twaver.graph.TGraphControlBar;
import com.nepxion.cots.twaver.graph.TGraphManager;
import com.nepxion.discovery.console.desktop.controller.ServiceController;
import com.nepxion.discovery.console.desktop.entity.InstanceEntity;
import com.nepxion.discovery.console.desktop.entity.RouterEntity;
import com.nepxion.discovery.console.desktop.icon.ConsoleIconFactory;
import com.nepxion.discovery.console.desktop.locale.ConsoleLocale;
import com.nepxion.discovery.console.desktop.workspace.topology.AbstractTopology;
import com.nepxion.discovery.console.desktop.workspace.topology.TopologyEntity;
import com.nepxion.discovery.console.desktop.workspace.topology.TopologyEntityType;
import com.nepxion.swing.action.JSecurityAction;
import com.nepxion.swing.button.ButtonManager;
import com.nepxion.swing.button.JBasicToggleButton;
import com.nepxion.swing.button.JClassicButton;
import com.nepxion.swing.combobox.JBasicComboBox;
import com.nepxion.swing.handle.HandleManager;
import com.nepxion.swing.listener.DisplayAbilityListener;
import com.nepxion.swing.locale.SwingLocale;
import com.nepxion.swing.optionpane.JBasicOptionPane;
import com.nepxion.swing.textfield.JBasicTextField;

public class RouterTopology extends AbstractTopology {
    private static final long serialVersionUID = 1L;

    private int nodeStartX = 100;
    private int nodeStartY = 150;
    private int nodeHorizontalGap = 200;
    private int nodeVerticalGap = 0;

    private TopologyEntity serviceNodeEntity = new TopologyEntity(TopologyEntityType.SERVICE, true, true);

    private JBasicTextField textField = new JBasicTextField();
    private JBasicComboBox comboBox = new JBasicComboBox();

    private InstanceEntity instance;

    public RouterTopology() {
        initializeToolBar();
        initializeTopology();
    }

    private void initializeToolBar() {
        textField.setPreferredSize(new Dimension(650, textField.getPreferredSize().height));

        JToolBar toolBar = getGraph().getToolbar();
        toolBar.addSeparator();
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(new JLabel(ConsoleLocale.getString("service_list")));
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(comboBox);
        toolBar.add(new JClassicButton(createAddServiceAction()));
        toolBar.add(textField);
        toolBar.add(new JClassicButton(createExecuteRouterAction()));
        toolBar.add(new JClassicButton(createClearRouterAction()));

        ButtonManager.updateUI(toolBar);
    }

    private void initializeTopology() {
        graph.setBlinkingRule(new BlinkingRule() {
            public boolean isBodyBlinking(Element element) {
                return element.getAlarmState().getHighestNativeAlarmSeverity() != null || element.getClientProperty(TWaverConst.PROPERTYNAME_RENDER_COLOR) != null;
            }

            public boolean isOutlineBlinking(Element element) {
                return element.getAlarmState().getPropagateSeverity() != null || element.getClientProperty(TWaverConst.PROPERTYNAME_STATE_OUTLINE_COLOR) != null;
            }
        });
        graph.setElementStateOutlineColorGenerator(new Generator() {
            public Object generate(Object object) {
                return null;
            }
        });

        addHierarchyListener(new DisplayAbilityListener() {
            public void displayAbilityChanged(HierarchyEvent e) {
                TGraphControlBar graphControlBar = (TGraphControlBar) graph.getControlBarInternalFrame().getContent();
                JBasicToggleButton toggleButton = (JBasicToggleButton) graphControlBar.getViewToolBar().getViewOutlook().getComponent(10);
                toggleButton.setSelected(true);

                TGraphManager.layout(graph);
                graph.getLayoutInternalFrame().setLocation(3000, 3000);
                // graph.adjustComponentPosition(graph.getLayoutInternalFrame());

                removeHierarchyListener(this);
            }
        });
    }

    private void route(RouterEntity routerEntity) {
        dataBox.clear();

        int index = 0;

        TNode node = addNode(routerEntity, index);

        index++;

        route(routerEntity, node, index);
    }

    private void route(RouterEntity routerEntity, TNode node, int index) {
        List<RouterEntity> nexts = routerEntity.getNexts();
        if (CollectionUtils.isNotEmpty(nexts)) {
            for (RouterEntity next : nexts) {
                TNode nextNode = addNode(next, index);
                addLink(node, nextNode);

                index++;

                route(next, nextNode, index);
            }
        }
    }

    private String getNodeName(RouterEntity routerEntity) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(routerEntity.getServiceId()).append("\n");
        stringBuilder.append(routerEntity.getHost()).append(":").append(routerEntity.getPort());

        if (StringUtils.isNotEmpty(routerEntity.getVersion())) {
            stringBuilder.append("\n [V").append(routerEntity.getVersion()).append("]");
        }

        return ButtonManager.getHtmlText(stringBuilder.toString());
    }

    private TNode addNode(RouterEntity routerEntity, int index) {
        String nodeName = getNodeName(routerEntity);

        TNode node = createNode(nodeName, serviceNodeEntity, index, nodeStartX, nodeStartY, nodeHorizontalGap, nodeVerticalGap);
        node.setUserObject(routerEntity);

        dataBox.addElement(node);

        return node;
    }

    private void addLink(TNode fromNode, TNode toNode) {
        TLink link = createLink(fromNode, toNode, true);
        link.putLinkToArrowColor(Color.yellow);

        dataBox.addElement(link);
    }

    @SuppressWarnings({ "unchecked" })
    public void setServices(Object[] services) {
        comboBox.setModel(new DefaultComboBoxModel<>(services));
    }

    public void setInstance(InstanceEntity instance) {
        if (this.instance != instance) {
            this.instance = instance;

            textField.setText("");
            dataBox.clear();
        }
    }

    private JSecurityAction createAddServiceAction() {
        JSecurityAction action = new JSecurityAction(ConsoleIconFactory.getSwingIcon("direction_east.png")) {
            private static final long serialVersionUID = 1L;

            public void execute(ActionEvent e) {
                String routerPath = textField.getText();
                String serviceId = comboBox.getSelectedItem().toString();
                if (StringUtils.isNotEmpty(routerPath)) {
                    routerPath = routerPath + ";" + serviceId;
                } else {
                    routerPath = serviceId;
                }
                textField.setText(routerPath);
            }
        };

        return action;
    }

    private JSecurityAction createExecuteRouterAction() {
        JSecurityAction action = new JSecurityAction(ConsoleLocale.getString("execute_router"), ConsoleIconFactory.getSwingIcon("netbean/action_16.png"), ConsoleLocale.getString("execute_router")) {
            private static final long serialVersionUID = 1L;

            public void execute(ActionEvent e) {
                String routerPath = textField.getText();
                if (StringUtils.isEmpty(routerPath)) {
                    JBasicOptionPane.showMessageDialog(HandleManager.getFrame(RouterTopology.this), ConsoleLocale.getString("router_path_invalid"), SwingLocale.getString("warning"), JBasicOptionPane.WARNING_MESSAGE);

                    return;
                }

                RouterEntity routerEntity = ServiceController.routes(instance, routerPath);
                route(routerEntity);
            }
        };

        return action;
    }

    private JSecurityAction createClearRouterAction() {
        JSecurityAction action = new JSecurityAction(ConsoleLocale.getString("clear_router"), ConsoleIconFactory.getSwingIcon("paint.png"), ConsoleLocale.getString("clear_router")) {
            private static final long serialVersionUID = 1L;

            public void execute(ActionEvent e) {
                textField.setText("");
                dataBox.clear();
            }
        };

        return action;
    }
}