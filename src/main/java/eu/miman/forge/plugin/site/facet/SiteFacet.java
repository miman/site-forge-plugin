package eu.miman.forge.plugin.site.facet;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;

import eu.miman.forge.plugin.site.completer.SiteDocFormatType;
import eu.miman.forge.plugin.util.MimanBaseFacet;
import eu.miman.forge.plugin.util.VelocityUtil;
import eu.miman.forge.plugin.util.helpers.DomFileHelper;
import eu.miman.forge.plugin.util.helpers.DomFileHelperImpl;

@Alias("site-facet")
@RequiresFacet({ MavenCoreFacet.class, JavaSourceFacet.class,
		DependencyFacet.class })
public class SiteFacet extends MimanBaseFacet {

	DomFileHelper domFileHelper;

	private final VelocityEngine velocityEngine;
	private VelocityUtil velocityUtil;

	public static String prjName;
	public static String organization;
	public static SiteDocFormatType docFormat;

	public SiteFacet() {
		super();
		domFileHelper = new DomFileHelperImpl();

		velocityUtil = new VelocityUtil();

		velocityEngine = new VelocityEngine();
		velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER,
				"classpath");
		velocityEngine.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		velocityEngine.setProperty(
				RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
				"org.apache.velocity.runtime.log.JdkLogChute");
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
			if (plugin.getArtifactId().equals("maven-site-plugin")) {
				dependenciesOk = true;
			}
			// TODO more checks should be added here
		}

		return dependenciesOk;
	}

	// Helper functions ****************************************
	/**
	 * Configures the project to be a JBoss Forge plugin project. Adds the
	 * necessary dependencies to the pom.xml file. Creates the Forge.xml file
	 */
	private void configureProject() {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();

		String packaging = pom.getPackaging();

		mergePomFileWithTemplate(pom);
		
		pom.setPackaging(packaging); 
		
		mvnFacet.setPOM(pom);

		Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
		velocityPlaceholderMap.put("prj-name", prjName);
		velocityPlaceholderMap.put("organization", organization);
		velocityPlaceholderMap.put("date",
				new Date(System.currentTimeMillis()).toString());

		createSiteXmlFile(velocityPlaceholderMap);
	}

	private void createSiteXmlFile(Map<String, Object> velocityPlaceholderMap) {
		String sourceUri = "/template-files/src/site/site.xml";
		String targetUri = "../../site/site.xml";

		VelocityContext velocityContext = velocityUtil
				.createVelocityContext(velocityPlaceholderMap);
		velocityUtil.createResourceAbsolute(sourceUri, velocityContext,
				targetUri, project, velocityEngine);
	}

	@Override
	protected String getTargetPomFilePath() {
		if (docFormat.equals(SiteDocFormatType.MARKDOWN)) {
			return "/template-files/pom_markdown.xml";
		} else {
			return "/template-files/pom_apt.xml";
		}
	}
}
