package eu.miman.forge.plugin.site.facet;

import java.io.File;
import java.util.List;

import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;

import se.miman.forge.plugin.util.MimanBaseFacet;

@Alias("plantuml-facet")
@RequiresFacet({ MavenCoreFacet.class, DependencyFacet.class, SiteFacet.class})
public class PlantUmlFacet extends MimanBaseFacet {

	private static final String SRC_SITE_UML = "/src/site/uml";

	public PlantUmlFacet() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.forge.project.Facet#install()
	 */
	@Override
	public boolean install() {
		configureProject();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.forge.project.Facet#isInstalled()
	 */
	@Override
	public boolean isInstalled() {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();

		if (pom.getBuild() == null || pom.getBuild().getPlugins() == null) {
			return false;
		}
		
		List<Plugin> deps = pom.getBuild().getPlugins();
		boolean dependenciesOk = false;
		for (Plugin plugin : deps) {
			if (plugin.getGroupId().equals("com.github.jeluard") 
					&& plugin.getArtifactId().equals("maven-plantuml-plugin")) {
				dependenciesOk = true;
			}
		}

		return dependenciesOk;
	}

	// Helper functions ****************************************
	/**
	 * Adds the necessary dependencies to the pom.xml file.
	 */
	private void configureProject() {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();

		mergePomFileWithTemplate(pom);
		mvnFacet.setPOM(pom);
		
		createNeededDirs(pom.getProjectDirectory().getAbsolutePath());
	}

	/**
	 * Creates the directories where to place the UML files
	 */
	private void createNeededDirs(String prjPath) {
		new File(prjPath + SRC_SITE_UML).mkdir();
		
	}

	@Override
	protected String getTargetPomFilePath() {
		return "/template-files/pom_plantuml.xml";
	}
}
