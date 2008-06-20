/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.editor.composites;

import static org.maven.ide.eclipse.editor.pom.FormUtils.nvl;
import static org.maven.ide.eclipse.editor.pom.FormUtils.setButton;
import static org.maven.ide.eclipse.editor.pom.FormUtils.setText;

import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.maven.ide.components.pom.Build;
import org.maven.ide.components.pom.ExcludesType;
import org.maven.ide.components.pom.Filters;
import org.maven.ide.components.pom.IncludesType;
import org.maven.ide.components.pom.Model;
import org.maven.ide.components.pom.PomFactory;
import org.maven.ide.components.pom.PomPackage;
import org.maven.ide.components.pom.Resource;
import org.maven.ide.components.pom.Resources;
import org.maven.ide.components.pom.TestResources;
import org.maven.ide.eclipse.editor.MavenEditorImages;
import org.maven.ide.eclipse.editor.pom.FormUtils;
import org.maven.ide.eclipse.editor.pom.MavenPomEditorPage;
import org.maven.ide.eclipse.editor.pom.ValueProvider;

/**
 * @author Eugene Kuleshov
 */
public class BuildComposite extends Composite {

  protected static PomPackage POM_PACKAGE = PomPackage.eINSTANCE;
  
  private MavenPomEditorPage parent;
  
  private FormToolkit toolkit = new FormToolkit(Display.getCurrent());
  
  // controls
  private Text defaultGoalText;
  private Text directoryText;
  private Text finalNameText;

  private ListEditorComposite<String> filtersEditor;

  private ListEditorComposite<Resource> resourcesEditor;
  private ListEditorComposite<Resource> testResourcesEditor;

  private Text resourceDirectoryText;
  private Text resourceTargetPathText;
  private ListEditorComposite<String> resourceIncludesEditor;
  private ListEditorComposite<String> resourceExcludesEditor;

  private Button resourceFilteringButton;
  private Section resourceDetailsSection;
  
  // model
  private Model model;

  private Resource currentResource;

  private boolean changingSelection = false;

  
  public BuildComposite(Composite parent, int flags) {
    super(parent, flags);
    
    toolkit.adapt(this);
  
    GridLayout layout = new GridLayout();
    layout.marginWidth = 0;
    layout.makeColumnsEqualWidth = true;
    setLayout(layout);
  
    createBuildSection();
  }

  private void createBuildSection() {
    SashForm horizontalSash = new SashForm(this, SWT.NONE);
    toolkit.adapt(horizontalSash);
    horizontalSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    
    Section buildSection = toolkit.createSection(horizontalSash, Section.TITLE_BAR);
    buildSection.setText("Build");
  
    Composite composite = toolkit.createComposite(buildSection, SWT.NONE);
    GridLayout compositeLayout = new GridLayout(2, false);
    compositeLayout.marginWidth = 1;
    compositeLayout.marginHeight = 2;
    composite.setLayout(compositeLayout);
    toolkit.paintBordersFor(composite);
    buildSection.setClient(composite);
  
    toolkit.createLabel(composite, "Default Goal:", SWT.NONE);
  
    defaultGoalText = toolkit.createText(composite, null, SWT.NONE);
    defaultGoalText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
  
    toolkit.createLabel(composite, "Directory:", SWT.NONE);
  
    directoryText = toolkit.createText(composite, null, SWT.NONE);
    directoryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
  
    toolkit.createLabel(composite, "Final Name:", SWT.NONE);
  
    finalNameText = toolkit.createText(composite, null, SWT.NONE);
    finalNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
  
    Label filtersLabel = toolkit.createLabel(composite, "Filters:", SWT.NONE);
    filtersLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
  
    filtersEditor = new ListEditorComposite<String>(composite, SWT.NONE);
    GridData filtersEditorData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
    filtersEditorData.heightHint = 47;
    filtersEditor.setLayoutData(filtersEditorData);
    toolkit.adapt(filtersEditor);
    toolkit.paintBordersFor(filtersEditor);

    filtersEditor.setContentProvider(new ListEditorContentProvider<String>());
    filtersEditor.setLabelProvider(new StringLabelProvider(MavenEditorImages.IMG_FILTER));
    
    filtersEditor.setAddListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        CompoundCommand compoundCommand = new CompoundCommand();
        EditingDomain editingDomain = parent.getEditingDomain();
        
        Build build = model.getBuild();
        if(build == null) {
          build = PomFactory.eINSTANCE.createBuild();
          Command command = SetCommand.create(editingDomain, model, POM_PACKAGE.getModel_Build(), build);
          compoundCommand.append(command);
        }
        
        Filters filters = build.getFilters();
        if(filters==null) {
          filters = PomFactory.eINSTANCE.createFilters();
          Command command = SetCommand.create(editingDomain, build, POM_PACKAGE.getBuild_Filters(), filters);
          compoundCommand.append(command);
        }
        
        String filter = "?";
        Command addCommand = AddCommand.create(editingDomain, filters, POM_PACKAGE.getFilters_Filter(), filter);
        compoundCommand.append(addCommand);
        
        editingDomain.getCommandStack().execute(compoundCommand);
        filtersEditor.setSelection(Collections.singletonList(filter));
      }
    });
    
    filtersEditor.setRemoveListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        CompoundCommand compoundCommand = new CompoundCommand();
        EditingDomain editingDomain = parent.getEditingDomain();
 
        List<String> selection = filtersEditor.getSelection();
        for(String filter : selection) {
          Command removeCommand = RemoveCommand.create(editingDomain, model.getBuild().getFilters(), //
              POM_PACKAGE.getFilters_Filter(), filter);
          compoundCommand.append(removeCommand);
        }
        
        editingDomain.getCommandStack().execute(compoundCommand);
      }
    });
    
    filtersEditor.setCellModifier(new ICellModifier() {
      public boolean canModify(Object element, String property) {
        return true;
      }
 
      public Object getValue(Object element, String property) {
        return element;
      }
 
      public void modify(Object element, String property, Object value) {
        int n = filtersEditor.getViewer().getTable().getSelectionIndex();
        Filters filters = model.getBuild().getFilters();
        if(!value.equals(filters.getFilter().get(n))) {
          EditingDomain editingDomain = parent.getEditingDomain();
          Command command = SetCommand.create(editingDomain, filters, //
              POM_PACKAGE.getFilters_Filter(), value, n);
          editingDomain.getCommandStack().execute(command);
          filtersEditor.refresh();
        }
      }
    });
    
    ///
    
    SashForm verticalSash = new SashForm(horizontalSash, SWT.VERTICAL);

    createResourceSection(verticalSash);
    createTestResourcesSection(verticalSash);

    verticalSash.setWeights(new int[] {1, 1});

    createResourceDetailsSection(horizontalSash);

    horizontalSash.setWeights(new int[] {1, 1, 1});
  }

  private void createResourceDetailsSection(SashForm horizontalSash) {
    resourceDetailsSection = toolkit.createSection(horizontalSash, Section.TITLE_BAR);
    resourceDetailsSection.setText("Resource Details");
  
    Composite resourceDetailsComposite = toolkit.createComposite(resourceDetailsSection, SWT.NONE);
    GridLayout gridLayout = new GridLayout(2, false);
    gridLayout.marginWidth = 1;
    gridLayout.marginHeight = 2;
    resourceDetailsComposite.setLayout(gridLayout);
    toolkit.paintBordersFor(resourceDetailsComposite);
    resourceDetailsSection.setClient(resourceDetailsComposite);
  
    Label resourceDirectoryLabel = toolkit.createLabel(resourceDetailsComposite, "Directory:", SWT.NONE);
    resourceDirectoryLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
  
    resourceDirectoryText = toolkit.createText(resourceDetailsComposite, null, SWT.NONE);
    resourceDirectoryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
  
    Label resourceTargetPathLabel = toolkit.createLabel(resourceDetailsComposite, "Target Path:", SWT.NONE);
    resourceTargetPathLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
  
    resourceTargetPathText = toolkit.createText(resourceDetailsComposite, null, SWT.NONE);
    resourceTargetPathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
  
    resourceFilteringButton = toolkit.createButton(resourceDetailsComposite, "Filtering", SWT.CHECK);
    resourceFilteringButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
  
    Label includesLabel = toolkit.createLabel(resourceDetailsComposite, "Includes:", SWT.NONE);
    includesLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
  
    resourceIncludesEditor = new ListEditorComposite<String>(resourceDetailsComposite, SWT.NONE);
    GridData includesEditorData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
    includesEditorData.heightHint = 60;
    resourceIncludesEditor.setLayoutData(includesEditorData);
    toolkit.adapt(resourceIncludesEditor);
    toolkit.paintBordersFor(resourceIncludesEditor);
  
    resourceIncludesEditor.setContentProvider(new ListEditorContentProvider<String>());
    resourceIncludesEditor.setLabelProvider(new StringLabelProvider(MavenEditorImages.IMG_INCLUDE));
    
    resourceIncludesEditor.setAddListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        CompoundCommand compoundCommand = new CompoundCommand();
        EditingDomain editingDomain = parent.getEditingDomain();

        IncludesType includes = currentResource.getIncludes();
        if(includes == null) {
          includes = PomFactory.eINSTANCE.createIncludesType();
          Command command = SetCommand.create(editingDomain, currentResource, POM_PACKAGE.getResource_Includes(), includes);
          compoundCommand.append(command);
        }

        String include = "?";
        Command addCommand = AddCommand.create(editingDomain, includes, POM_PACKAGE.getIncludesType_Include(), include);
        compoundCommand.append(addCommand);
        
        editingDomain.getCommandStack().execute(compoundCommand);
        resourceIncludesEditor.setSelection(Collections.singletonList(include));
      }
    });
    
    resourceIncludesEditor.setRemoveListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        CompoundCommand compoundCommand = new CompoundCommand();
        EditingDomain editingDomain = parent.getEditingDomain();
 
        List<String> selection = resourceIncludesEditor.getSelection();
        for(String include : selection) {
          Command removeCommand = RemoveCommand.create(editingDomain, currentResource.getIncludes(), //
              POM_PACKAGE.getIncludesType_Include(), include);
          compoundCommand.append(removeCommand);
        }
        
        editingDomain.getCommandStack().execute(compoundCommand);
      }
    });
    
    resourceIncludesEditor.setCellModifier(new ICellModifier() {
      public boolean canModify(Object element, String property) {
        return true;
      }
 
      public Object getValue(Object element, String property) {
        return element;
      }
 
      public void modify(Object element, String property, Object value) {
        int n = resourceIncludesEditor.getViewer().getTable().getSelectionIndex();
        IncludesType includes = currentResource.getIncludes();
        if(!value.equals(includes.getInclude().get(n))) {
          EditingDomain editingDomain = parent.getEditingDomain();
          Command command = SetCommand.create(editingDomain, includes, //
              POM_PACKAGE.getIncludesType_Include(), value, n);
          editingDomain.getCommandStack().execute(command);
          resourceIncludesEditor.refresh();
        }
      }
    });
    
    Label excludesLabel = toolkit.createLabel(resourceDetailsComposite, "Excludes:", SWT.NONE);
    excludesLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
  
    resourceExcludesEditor = new ListEditorComposite<String>(resourceDetailsComposite, SWT.NONE);
    GridData excludesEditorData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
    excludesEditorData.heightHint = 60;
    resourceExcludesEditor.setLayoutData(excludesEditorData);
    toolkit.adapt(resourceExcludesEditor);
    toolkit.paintBordersFor(resourceExcludesEditor);
    
    resourceExcludesEditor.setContentProvider(new ListEditorContentProvider<String>());
    resourceExcludesEditor.setLabelProvider(new StringLabelProvider(MavenEditorImages.IMG_EXCLUDE));
    
    resourceExcludesEditor.setAddListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        CompoundCommand compoundCommand = new CompoundCommand();
        EditingDomain editingDomain = parent.getEditingDomain();

        ExcludesType excludes = currentResource.getExcludes();
        if(excludes == null) {
          excludes = PomFactory.eINSTANCE.createExcludesType();
          Command command = SetCommand.create(editingDomain, currentResource, POM_PACKAGE.getResource_Excludes(), excludes);
          compoundCommand.append(command);
        }

        String exclude = "?";
        Command addCommand = AddCommand.create(editingDomain, excludes, POM_PACKAGE.getExcludesType_Exclude(), exclude);
        compoundCommand.append(addCommand);
        
        editingDomain.getCommandStack().execute(compoundCommand);
        resourceExcludesEditor.setSelection(Collections.singletonList(exclude));
      }
    });
    
    resourceExcludesEditor.setRemoveListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        CompoundCommand compoundCommand = new CompoundCommand();
        EditingDomain editingDomain = parent.getEditingDomain();
 
        List<String> selection = resourceExcludesEditor.getSelection();
        for(String exclude : selection) {
          Command removeCommand = RemoveCommand.create(editingDomain, currentResource.getExcludes(), //
              POM_PACKAGE.getExcludesType_Exclude(), exclude);
          compoundCommand.append(removeCommand);
        }
        
        editingDomain.getCommandStack().execute(compoundCommand);
      }
    });
    
    resourceExcludesEditor.setCellModifier(new ICellModifier() {
      public boolean canModify(Object element, String property) {
        return true;
      }
 
      public Object getValue(Object element, String property) {
        return element;
      }
 
      public void modify(Object element, String property, Object value) {
        int n = resourceExcludesEditor.getViewer().getTable().getSelectionIndex();
        ExcludesType excludes = currentResource.getExcludes();
        if(!value.equals(excludes.getExclude().get(n))) {
          EditingDomain editingDomain = parent.getEditingDomain();
          Command command = SetCommand.create(editingDomain, excludes, //
              POM_PACKAGE.getExcludesType_Exclude(), value, n);
          editingDomain.getCommandStack().execute(command);
          resourceExcludesEditor.refresh();
        }
      }
    });
    
  }

  private void createResourceSection(SashForm verticalSash) {
    Section resourcesSection = toolkit.createSection(verticalSash, Section.TITLE_BAR);
    resourcesSection.setText("Resources");
  
    resourcesEditor = new ListEditorComposite<Resource>(resourcesSection, SWT.NONE);
    resourcesSection.setClient(resourcesEditor);
    toolkit.adapt(resourcesEditor);
    toolkit.paintBordersFor(resourcesEditor);
    
    resourcesEditor.setContentProvider(new ListEditorContentProvider<Resource>());
    resourcesEditor.setLabelProvider(new ResourceLabelProvider());
    
    resourcesEditor.addSelectionListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        List<Resource> selection = resourcesEditor.getSelection();
        loadResourceDetails(selection.size()==1 ? selection.get(0) : null);
        
        if(!selection.isEmpty()) {
          changingSelection = true;
          try {
            testResourcesEditor.setSelection(Collections.<Resource>emptyList());
          } finally {
            changingSelection = false;
          }
        }
      }
    });
    
    resourcesEditor.setAddListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        CompoundCommand compoundCommand = new CompoundCommand();
        EditingDomain editingDomain = parent.getEditingDomain();
        
        Build build = model.getBuild();
        if(build == null) {
          build = PomFactory.eINSTANCE.createBuild();
          Command command = SetCommand.create(editingDomain, model, POM_PACKAGE.getModel_Build(), build);
          compoundCommand.append(command);
        }
        
        Resources resources = build.getResources();
        if(resources==null) {
          resources = PomFactory.eINSTANCE.createResources();
          Command command = SetCommand.create(editingDomain, build, POM_PACKAGE.getBuild_Resources(), resources);
          compoundCommand.append(command);
        }
        
        Resource resource = PomFactory.eINSTANCE.createResource();        
        Command addCommand = AddCommand.create(editingDomain, resources, POM_PACKAGE.getResources_Resource(), resource);
        compoundCommand.append(addCommand);
        
        editingDomain.getCommandStack().execute(compoundCommand);
        resourcesEditor.setSelection(Collections.singletonList(resource));
      }
    });
    
    resourcesEditor.setRemoveListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        CompoundCommand compoundCommand = new CompoundCommand();
        EditingDomain editingDomain = parent.getEditingDomain();
 
        List<Resource> selection = resourcesEditor.getSelection();
        for(Resource resource : selection) {
          Command removeCommand = RemoveCommand.create(editingDomain, model.getBuild().getResources(), //
              POM_PACKAGE.getResources_Resource(), resource);
          compoundCommand.append(removeCommand);
        }
        
        editingDomain.getCommandStack().execute(compoundCommand);
      }
    });
  }

  private void createTestResourcesSection(SashForm verticalSash) {
    Section testResourcesSection = toolkit.createSection(verticalSash, Section.TITLE_BAR);
    testResourcesSection.setText("Test Resources");
    toolkit.adapt(verticalSash, true, true);
    
    testResourcesEditor = new ListEditorComposite<Resource>(testResourcesSection, SWT.NONE);
    testResourcesSection.setClient(testResourcesEditor);
    toolkit.adapt(testResourcesEditor);
    toolkit.paintBordersFor(testResourcesEditor);

    testResourcesEditor.setContentProvider(new ListEditorContentProvider<Resource>());
    testResourcesEditor.setLabelProvider(new ResourceLabelProvider());

    testResourcesEditor.addSelectionListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        List<Resource> selection = testResourcesEditor.getSelection();
        loadResourceDetails(selection.size()==1 ? selection.get(0) : null);
        
        if(!selection.isEmpty()) {
          changingSelection = true;
          try {
            resourcesEditor.setSelection(Collections.<Resource>emptyList());
          } finally {
            changingSelection = false;
          }
        }
      }
    });
    
    testResourcesEditor.setAddListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        CompoundCommand compoundCommand = new CompoundCommand();
        EditingDomain editingDomain = parent.getEditingDomain();
        
        Build build = model.getBuild();
        if(build == null) {
          build = PomFactory.eINSTANCE.createBuild();
          Command command = SetCommand.create(editingDomain, model, POM_PACKAGE.getModel_Build(), build);
          compoundCommand.append(command);
        }
        
        Resources resources = build.getResources();
        if(resources==null) {
          resources = PomFactory.eINSTANCE.createResources();
          Command command = SetCommand.create(editingDomain, build, POM_PACKAGE.getBuild_TestResources(), resources);
          compoundCommand.append(command);
        }
        
        Resource resource = PomFactory.eINSTANCE.createResource();        
        Command addCommand = AddCommand.create(editingDomain, resources, POM_PACKAGE.getTestResources_TestResource(), resource);
        compoundCommand.append(addCommand);
        
        editingDomain.getCommandStack().execute(compoundCommand);
        resourcesEditor.setSelection(Collections.singletonList(resource));
      }
    });
    
    testResourcesEditor.setRemoveListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        CompoundCommand compoundCommand = new CompoundCommand();
        EditingDomain editingDomain = parent.getEditingDomain();
 
        List<Resource> selection = testResourcesEditor.getSelection();
        for(Resource resource : selection) {
          Command removeCommand = RemoveCommand.create(editingDomain, model.getBuild().getTestResources(), //
              POM_PACKAGE.getTestResources_TestResource(), resource);
          compoundCommand.append(removeCommand);
        }
        
        editingDomain.getCommandStack().execute(compoundCommand);
      }
    });
  }
  
  public void loadData(MavenPomEditorPage editorPage) {
    parent = editorPage;
    model = editorPage.getModel();
    
    Build build = model.getBuild();
    loadBuild(build);
    loadResources(build);
    loadTestResources(build);
    loadResourceDetails(null);
    
    filtersEditor.setReadOnly(parent.isReadOnly());    
    resourcesEditor.setReadOnly(parent.isReadOnly());    
    testResourcesEditor.setReadOnly(parent.isReadOnly());
    
    resourceIncludesEditor.setReadOnly(parent.isReadOnly());
    resourceExcludesEditor.setReadOnly(parent.isReadOnly());
  }

  public void updateView(MavenPomEditorPage editorPage, Notification notification) {
    Object object = notification.getNotifier();
    
    if(object instanceof Filters) {
      filtersEditor.refresh();
    }
    
    if(object instanceof Resources) {
      resourcesEditor.refresh();
    }
    
    if(object instanceof TestResources) {
      testResourcesEditor.refresh();
    }
    
    if(object instanceof Resource) {
      resourcesEditor.refresh();
      testResourcesEditor.refresh();
      if(object==currentResource) {
        loadResourceDetails(currentResource);
      }
    }
    
    if(object instanceof IncludesType) {
      resourceIncludesEditor.refresh();
    }

    if(object instanceof ExcludesType) {
      resourceExcludesEditor.refresh();
    }

    // XXX handle other notification types
  }
  
  private void loadBuild(Build build) {
    if(build!=null) {
      defaultGoalText.setText(nvl(build.getDefaultGoal()));
      directoryText.setText(nvl(build.getDirectory()));
      finalNameText.setText(nvl(build.getFinalName()));
    } else {
      defaultGoalText.setText("");
      directoryText.setText("");
      finalNameText.setText("");
    }
    
    filtersEditor.setInput(build == null //
        || build.getFilters() == null ? null : build.getFilters().getFilter());
  }
  
  private void loadResources(Build build) {
    resourcesEditor.setInput(build == null //
        || build.getResources() == null ? null : build.getResources().getResource());
  }
  
  private void loadTestResources(Build build) {
    testResourcesEditor.setInput(build == null //
        || build.getTestResources() == null ? null : build.getTestResources().getTestResource());
  }

  private void loadResourceDetails(Resource resource) {
    if(changingSelection) {
      return;
    }
    
    currentResource = resource;
    
    if(parent!=null) {
      parent.removeNotifyListener(resourceDirectoryText);
      parent.removeNotifyListener(resourceTargetPathText);
      parent.removeNotifyListener(resourceFilteringButton);
    }
    
    if(resource == null) {
      FormUtils.setEnabled(resourceDetailsSection, false);
      
      setText(resourceDirectoryText, "");
      setText(resourceTargetPathText, "");
      setButton(resourceFilteringButton, false);
      
      resourceIncludesEditor.setInput(null);
      resourceExcludesEditor.setInput(null);
      
      return;
    }

    FormUtils.setEnabled(resourceDetailsSection, true);
    FormUtils.setReadonly(resourceDetailsSection, parent.isReadOnly());
    
    setText(resourceDirectoryText, resource.getDirectory());
    setText(resourceTargetPathText, resource.getTargetPath());
    setButton(resourceFilteringButton, "true".equals(resource.getFiltering()));
    
    resourceIncludesEditor.setInput(resource.getIncludes()==null ? null : resource.getIncludes().getInclude());
    resourceExcludesEditor.setInput(resource.getExcludes()==null ? null : resource.getExcludes().getExclude());
    
    ValueProvider<Resource> provider = new ValueProvider.DefaultValueProvider<Resource>(resource);
    parent.setModifyListener(resourceDirectoryText, provider, POM_PACKAGE.getResource_Directory(), "");
    parent.setModifyListener(resourceTargetPathText, provider, POM_PACKAGE.getResource_TargetPath(), "");
    parent.setModifyListener(resourceFilteringButton, provider, POM_PACKAGE.getResource_Filtering(), "false");
    
    parent.registerListeners();
  }

  /**
   * Label provider for {@link Resource}
   */
  public class ResourceLabelProvider extends LabelProvider {

    public String getText(Object element) {
      if(element instanceof Resource) {
        return ((Resource) element).getDirectory();
      }
      return super.getText(element);
    }
    
    public Image getImage(Object element) {
      return MavenEditorImages.IMG_RESOURCE;
    }
    
  }
  
}
