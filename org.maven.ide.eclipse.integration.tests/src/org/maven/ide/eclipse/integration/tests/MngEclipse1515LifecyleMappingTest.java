package org.maven.ide.eclipse.integration.tests;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.editor.pom.MavenPomEditor;
import org.maven.ide.eclipse.project.IMavenProjectFacade;

import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.swt.condition.SWTIdleCondition;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.FilteredTreeItemLocator;
import com.windowtester.runtime.swt.locator.LabeledLabelLocator;
import com.windowtester.runtime.swt.locator.LabeledTextLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;

public class MngEclipse1515LifecyleMappingTest extends UIIntegrationTestCase {

  public MngEclipse1515LifecyleMappingTest(){
    super();
    this.setSkipIndexes(true);
  }
  
	/**
	 * Main test method.
	 */
	public void testMgnEclipse1515() throws Exception {
	  
		IUIContext ui = getUI();
    File tempDir = importMavenProjects("projects/someproject.zip");
    IMavenProjectFacade mavenProject = MavenPlugin.getDefault().getMavenProjectManager().getMavenProject("org.sonatype.test", "someproject", "0.0.1-SNAPSHOT");
    assertNotNull(mavenProject);
    
    //open project prefs, navigate to maven->lifecycle mapping, make sure that the 'generic' lifecycle mapping is showing
    showGenericLifecycle();
    
    
    openPomFile("someproject/pom.xml");
    ui.click(new CTabItemLocator("pom.xml"));
    
    //then set to customizable and make sure that one is showing
    findText("</project");
    getUI().keyClick(SWT.ARROW_LEFT);
    
    getUI().enterText("<build><plugins><plugin> <groupId>org.maven.ide.eclipse</ <artifactId>lifecycle-mapping</ <version>0.9.9-SNAPSHOT</  <configuration><mappingId>customizable</ <configurators></ <mojoExecutions></ </ </</</");
    getUI().keyClick(SWT.MOD1, 's');
    waitForAllBuildsToComplete();
    showCustomizableLifecycle();

    //then, back to generic
    openPomFile("someproject/pom.xml");
    ui.click(new CTabItemLocator("pom.xml"));
    replaceTextWithWrap("customizable", "generic", true);
    getUI().wait(new ShellDisposedCondition(FIND_REPLACE));
    getUI().keyClick(SWT.MOD1, 's');
    waitForAllBuildsToComplete();
    showGenericLifecycle();
    
    //then switch to empty lifecycle mapping
    openPomFile("someproject/pom.xml");
    ui.click(new CTabItemLocator("pom.xml"));
    replaceTextWithWrap("generic", "NULL", true);
    getUI().wait(new ShellDisposedCondition(FIND_REPLACE));
    getUI().keyClick(SWT.MOD1, 's');
    waitForAllBuildsToComplete();
    showEmptyLifecycle();
	}
	
  protected void selectEditorTab(final String id) throws Exception {
    final MavenPomEditor editor = (MavenPomEditor) getActivePage().getActiveEditor();
    Display.getDefault().syncExec(new Runnable() {
      public void run() {
        editor.setActivePage(id);
      }
    });
    getUI().wait(new SWTIdleCondition());
  }
  /**
   * @throws WidgetSearchException 
   * 
   */
  private void showEmptyLifecycle() throws WidgetSearchException {
    showLifecyclePropsPage();
    IWidgetLocator widgetLocator = getUI().find(new NamedWidgetLocator("noInfoLabel"));
    assertNotNull(widgetLocator);
    hideLifecyclePropsPage();
  }


  /**
   * @throws WidgetSearchException 
   * 
   */
  private void showCustomizableLifecycle() throws WidgetSearchException {
    showLifecyclePropsPage();
    IWidgetLocator widgetLocator = getUI().find(new NamedWidgetLocator("projectConfiguratorsTable"));
    assertNotNull(widgetLocator);
    hideLifecyclePropsPage();
  }

  private void hideLifecyclePropsPage() throws WidgetSearchException{
    getUI().click(new ButtonLocator("Cancel"));
    getUI().wait(new ShellDisposedCondition("Properties for someproject")); 
  }
  /**
   * @throws WidgetSearchException 
   * 
   */
  private void showLifecyclePropsPage() throws WidgetSearchException {
    getUI().contextClick(new TreeItemLocator("someproject", new ViewLocator("org.eclipse.jdt.ui.PackageExplorer")), "Properties");
    getUI().wait(new ShellShowingCondition("Properties for someproject"));
    getUI().click(new FilteredTreeItemLocator("Maven/Lifecycle Mapping"));
  }

  /**
   * @throws WidgetSearchException 
   * 
   */
  private void showGenericLifecycle() throws WidgetSearchException {
    showLifecyclePropsPage();
    IWidgetLocator widgetLocator = getUI().find(new NamedWidgetLocator("goalsText"));
    assertNotNull(widgetLocator);
    hideLifecyclePropsPage();
  }

}