/*******************************************************************************
 * Copyright (c) 2007, 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.actions;

import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.core.MavenLogger;
import org.maven.ide.eclipse.embedder.ArtifactKey;
import org.maven.ide.eclipse.embedder.MavenModelManager;
import org.maven.ide.eclipse.index.IIndex;
import org.maven.ide.eclipse.index.IndexedArtifactFile;
import org.maven.ide.eclipse.ui.dialogs.MavenRepositorySearchDialog;


public class AddPluginAction extends MavenActionSupport implements IWorkbenchWindowActionDelegate {

  public static final String ID = "org.maven.ide.eclipse.addPluginAction";

  public void run(IAction action) {
    IFile file = getPomFileFromPomEditorOrViewSelection();

    if(file == null) {
      return;
    }

    MavenRepositorySearchDialog dialog = new MavenRepositorySearchDialog(getShell(), "Add Plugin", IIndex.SEARCH_PLUGIN, Collections.<ArtifactKey> emptySet());
    if(dialog.open() == Window.OK) {
      final IndexedArtifactFile indexedArtifactFile = (IndexedArtifactFile) dialog.getFirstResult();
      if(indexedArtifactFile != null) {
        try {
          MavenModelManager modelManager = MavenPlugin.getDefault().getMavenModelManager();
          modelManager.updateProject(file, new MavenModelManager.PluginAdder( //
              indexedArtifactFile.group, //
              indexedArtifactFile.artifact, //
              indexedArtifactFile.version));
        } catch(Exception ex) {
          MavenLogger.log("Can't add dependency to " + file, ex);
        }
      }
    }
  }

  public void dispose() {
  }

  public void init(IWorkbenchWindow window) {
  }
}