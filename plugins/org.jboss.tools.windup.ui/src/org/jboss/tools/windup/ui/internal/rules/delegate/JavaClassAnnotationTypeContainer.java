/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.windup.ui.internal.rules.delegate;

import java.util.List;
import java.util.Optional;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.modelquery.ModelQuery;
import org.eclipse.xtext.util.Pair;
import org.jboss.tools.windup.ui.WindupUIPlugin;
import org.jboss.tools.windup.ui.internal.Messages;
import org.jboss.tools.windup.ui.internal.RuleMessages;
import org.jboss.tools.windup.ui.internal.editor.AddNodeAction;
import org.jboss.tools.windup.ui.internal.editor.DeleteNodeAction;
import org.jboss.tools.windup.ui.internal.editor.RulesetElementUiDelegateFactory;
import org.jboss.tools.windup.ui.internal.editor.RulesetElementUiDelegateFactory.IElementUiDelegate;
import org.jboss.tools.windup.ui.internal.editor.RulesetElementUiDelegateFactory.RulesetConstants;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;

@SuppressWarnings({"restriction"})
public class JavaClassAnnotationTypeContainer {
	
	private static final int MIN_WIDTH = 350;
	
	private Element element;
	private IStructuredModel model;
	private ModelQuery modelQuery;
	private CMElementDeclaration elementDeclaration;

	private FormToolkit toolkit;
	private Composite parentControl;
	private ScrolledComposite scroll;
	private ListContainer locationListContainer;
	private RulesetElementUiDelegateFactory uiDelegateFactory;
	private IEclipseContext context;
	
	public JavaClassAnnotationTypeContainer(Element element, IStructuredModel model, ModelQuery modelQuery, CMElementDeclaration elementDeclaration, 
			FormToolkit toolkit, RulesetElementUiDelegateFactory uiDelegateFactory, IEclipseContext context) {
		this.element = element;
		this.model = model;
		this.modelQuery = modelQuery;
		this.elementDeclaration = elementDeclaration;
		this.toolkit = toolkit;
		this.uiDelegateFactory = uiDelegateFactory;
		this.context = context;
	}
	
	public void bind() {
		loadLocations();
		scroll.setMinHeight(locationListContainer.computeHeight());
		int width = locationListContainer.getItemCount() > 0 ? MIN_WIDTH : 0;
		scroll.setMinWidth(width);
		parentControl.getParent().getParent().getParent().layout(true, true);
	}
	
	private void loadLocations() {
		locationListContainer.createControls(parentControl, collectLocations());
		locationListContainer.bind();
	}
	
	private List<Element> collectLocations() {
		List<Element> links = Lists.newArrayList();
		NodeList list = element.getElementsByTagName(RulesetConstants.JAVA_CLASS_ANNOTATION_TYPE);
		for (int i = 0; i < list.getLength(); i++) {
			links.add((Element)list.item(i));
		}
		return links;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private CMElementDeclaration getLocationCmNode() {
		List candidates = modelQuery.getAvailableContent(element, elementDeclaration, 
				ModelQuery.VALIDITY_STRICT);
		Optional<CMElementDeclaration> found = candidates.stream().filter(candidate -> {
			if (candidate instanceof CMElementDeclaration) {
				return RulesetConstants.JAVA_CLASS_ANNOTATION_TYPE.equals(((CMElementDeclaration)candidate).getElementName());
			}
			return false;
		}).findFirst();
		if (found.isPresent()) {
			return found.get();
		}
		return null;
	}
	
	public Section createControls(Composite parent) {
		Pair<Section, Composite> result = ElementDetailsSection.createScrolledSection(toolkit, parent,
				RuleMessages.javaclass_annotation_type_sectionTitle, RuleMessages.javaclass_annotation_type_description,
				ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | Section.NO_TITLE_FOCUS_BOX | Section.TWISTIE);
		Section section = result.getFirst();
		Composite client = result.getSecond();
		this.scroll = (ScrolledComposite)section.getClient();
		this.parentControl = client;
		this.locationListContainer = createLocationListContainer();
		createSectionToolbar(section);
		return section;
 	}
	
	private void createSectionToolbar(Section section) {
		ToolBar toolbar = new ToolBar(section, SWT.FLAT|SWT.HORIZONTAL);
		ToolItem addItem = new ToolItem(toolbar, SWT.PUSH);
		addItem.setImage(WindupUIPlugin.getDefault().getImageRegistry().get(WindupUIPlugin.IMG_ADD));
		addItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CMElementDeclaration linkCmNode = getLocationCmNode();
				AddNodeAction action = (AddNodeAction)ElementUiDelegate.createAddElementAction(
						model, element, linkCmNode, element.getChildNodes().getLength(), null);
				action.run();
			}
		});
		section.setTextClient(toolbar);
	}
	
	private ListContainer createLocationListContainer() {
		ListContainer listContainer = new ListContainer() {
			@Override
			protected ListItem createListItem(Composite parent, Element listElement) {
				ListItem item = new ListItem(parent, listElement) {
					@Override
					protected void createControls() {
						CMElementDeclaration ed = modelQuery.getCMElementDeclaration(itemElement);
						
						Group group = new Group(this, SWT.SHADOW_IN);
						group.setBackground(toolkit.getColors().getBackground());
						group.setForeground(toolkit.getColors().getBackground());
						GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
						group.setLayout(new FormLayout());
						
						Composite left = new Composite(group, SWT.NONE);
						GridLayoutFactory.fillDefaults().numColumns(2).applyTo(left);
						FormData leftData = new FormData();
						leftData.left = new FormAttachment(0);
						left.setLayoutData(leftData);
						
						Composite right = new Composite(group, SWT.NONE);
						GridLayoutFactory.fillDefaults().numColumns(1).applyTo(right);
						FormData rightData = new FormData();
						rightData.right = new FormAttachment(100);
						rightData.bottom = new FormAttachment(73);
						right.setLayoutData(rightData);
						
						leftData.right = new FormAttachment(right);
						
						createToolbar(right, this);
						IElementUiDelegate delegate = uiDelegateFactory.createElementUiDelegate(listElement, context);
						delegate.createControls(left, listElement, ed, rows);
					}
				};
				return item;
			}
		};
		return listContainer;
	}
	
	private Control createToolbar(Composite parent, ListItem thisItem) {
		ToolBar toolbar = new ToolBar(parent, SWT.FLAT|SWT.VERTICAL);
		ToolItem deleteLinkItem = new ToolItem(toolbar, SWT.PUSH);
		deleteLinkItem.setToolTipText(Messages.RulesetEditor_remove);
		deleteLinkItem.setImage(WindupUIPlugin.getDefault().getImageRegistry().get(WindupUIPlugin.IMG_DELETE_CONFIG));
		//deleteLinkItem.getControl().setCursor(parent.getShell().getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		deleteLinkItem.addSelectionListener(new SelectionAdapter() {
	    		@Override
	    		public void widgetSelected(SelectionEvent e) {
    				DeleteNodeAction deleteAction = new DeleteNodeAction(model, Lists.newArrayList(thisItem.getLinkElement()));
	    			deleteAction.run();
	    		}
		});
	    GridLayoutFactory.fillDefaults().applyTo(toolbar);
	    return toolbar;
	}
}