package org.maven.ide.eclipse.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.project.IMavenProjectFacade;
import org.maven.ide.eclipse.project.MavenProjectInfo;
import org.maven.ide.eclipse.project.ProjectImportConfiguration;
import org.maven.ide.eclipse.project.ResolverConfiguration;

public class ProjectConfigurationManagerTest extends AsbtractMavenProjectTestCase {

  public void testBasedirRenameRequired() throws Exception {
    testBasedirRename(MavenProjectInfo.RENAME_REQUIRED);

    IWorkspaceRoot root = workspace.getRoot();
    IProject project = root.getProject("maven-project");
    assertNotNull(project);
    IMavenProjectFacade facade = plugin.getMavenProjectManager().getMavenProject("MNGECLIPSE-1793_basedirRename", "maven-project", "0.0.1-SNAPSHOT");
    assertNotNull(facade);
    assertEquals(project, facade.getProject());
  }

  public void testBasedirRenameNo() throws Exception {
    testBasedirRename(MavenProjectInfo.RENAME_NO);

    IWorkspaceRoot root = workspace.getRoot();
    IProject project = root.getProject("mavenNNNNNNN");
    assertNotNull(project);
    IMavenProjectFacade facade = plugin.getMavenProjectManager().getMavenProject("MNGECLIPSE-1793_basedirRename", "maven-project", "0.0.1-SNAPSHOT");
    assertNotNull(facade);
    assertEquals(project, facade.getProject());
  }

  private void testBasedirRename(int renameRequired) throws IOException, CoreException {
    IWorkspaceRoot root = workspace.getRoot();
    final MavenPlugin plugin = MavenPlugin.getDefault();

    String pathname = "projects/MNGECLIPSE-1793_basedirRename/mavenNNNNNNN";
    File src = new File(pathname);
    File dst = new File(root.getLocation().toFile(), src.getName());
    copyDir(src, dst);

    final ArrayList<MavenProjectInfo> projectInfos = new ArrayList<MavenProjectInfo>();
    projectInfos.add(new MavenProjectInfo("label", new File(dst, "pom.xml"), null, null));
    projectInfos.get(0).setBasedirRename(renameRequired);

    final ProjectImportConfiguration importConfiguration = new ProjectImportConfiguration(new ResolverConfiguration());

    workspace.run(new IWorkspaceRunnable() {
      public void run(IProgressMonitor monitor) throws CoreException {
        plugin.getProjectConfigurationManager().importProjects(projectInfos, importConfiguration, monitor);
      }
    }, plugin.getProjectConfigurationManager().getRule(), IWorkspace.AVOID_UPDATE, monitor);
  }

}