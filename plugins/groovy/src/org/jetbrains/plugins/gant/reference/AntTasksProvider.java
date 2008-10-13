package org.jetbrains.plugins.gant.reference;

import com.intellij.ProjectTopics;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.psi.SearchUtils;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * @author ilyas
 */
public class AntTasksProvider implements ProjectComponent {

  @NonNls private static final String ANT_TASK_CLASS = "org.apache.tools.ant.Task";

  private ArrayList<PsiClass> myAntTaks = null;
  private final Project myProject;
  private MessageBusConnection myRootConnection;

  public static AntTasksProvider getInstance(Project project) {
    return project.getComponent(AntTasksProvider.class);
  }

  public AntTasksProvider(Project project) {
    myProject = project;
  }

  public void projectOpened() {
    myRootConnection = myProject.getMessageBus().connect();
    myRootConnection.subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {

      public void beforeRootsChange(ModuleRootEvent event) {
      }

      public void rootsChanged(final ModuleRootEvent event) {
        myAntTaks = new ArrayList<PsiClass>();
        myAntTaks = findAntTasks(myProject);
      }
    });

    StartupManager.getInstance(myProject).registerPostStartupActivity(new Runnable() {
      public void run() {
        myAntTaks = new ArrayList<PsiClass>();
        myAntTaks = findAntTasks(myProject);
      }
    });
  }

  public void projectClosed() {
    myRootConnection.disconnect();
    myAntTaks.clear();
  }

  @NotNull
  public String getComponentName() {
    return "AntTasksProvider";
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }

  public ArrayList<PsiClass> getAntTasks() {
    if (myAntTaks == null) {
      myAntTaks = findAntTasks(myProject);
    }
    return myAntTaks;
  }

  public void init() {
    findAntTasks(myProject);
  }

  private static ArrayList<PsiClass> findAntTasks(Project project) {
    final JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
    final PsiClass taskClass = facade.findClass(ANT_TASK_CLASS, GlobalSearchScope.allScope(project));

    if (taskClass != null) {
      final Iterable<PsiClass> inheritors = SearchUtils.findClassInheritors(taskClass, true);
      final ArrayList<PsiClass> classes = new ArrayList<PsiClass>();
      for (PsiClass inheritor : inheritors) {
        if (!inheritor.hasModifierProperty(PsiModifier.ABSTRACT)) {
          classes.add(inheritor);
        }
      }
      return classes;
    }

    return new ArrayList<PsiClass>(0);
  }
}
