package eu.miman.forge.plugin.site;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.maven.model.Model;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresProject;
import org.jboss.forge.shell.plugins.SetupCommand;

import eu.miman.forge.plugin.site.completer.SiteDocFormatCompleter;
import eu.miman.forge.plugin.site.completer.SiteDocFormatType;
import eu.miman.forge.plugin.site.facet.SiteFacet;
import eu.miman.forge.plugin.site.facet.UmlGraphFacet;
import eu.miman.forge.plugin.util.VelocityUtil;
import eu.miman.forge.plugin.util.helpers.DomFileHelper;
import eu.miman.forge.plugin.util.helpers.DomFileHelperImpl;

@Alias("documentation")
@Help("A plugin that adds site documentation support to a project")
@RequiresProject
public class ProjectDocumentationPlugin implements Plugin {
	@Inject
	private Event<InstallFacets> event;

	@Inject
	private Project project;
	
	SiteFacet siteFacet;

	DomFileHelper domFileHelper;
	private final VelocityEngine velocityEngine;
	private VelocityUtil velocityUtil;
	
	public ProjectDocumentationPlugin() {
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

	@SetupCommand
	@Command(value = "setup", help = "Adds the maven dependencies and core files needed for a site & Javadoc")
	public void setup(
			@Option(name = "prjName", shortName = "prj", required = true) String prjName,
			@Option(name = "organization", shortName = "org") String organization,
			@Option(name = "format", completer = SiteDocFormatCompleter.class, required = true) String format,
			PipeOut out) {
		if (!project.hasFacet(ResourceFacet.class)) {
			event.fire(new InstallFacets(ResourceFacet.class));
		}

		if (organization == null) {
			organization = "";
		}
		SiteFacet.organization = organization;
		SiteFacet.prjName = prjName;
		SiteFacet.docFormat = SiteDocFormatType.from(format);
		if (!project.hasFacet(SiteFacet.class))
//			event.fire(new InstallFacets(SiteFacet.class));
			project.installFacet(new SiteFacet());
		else
			ShellMessages.info(out,
					"Project is already a Site enabled project.");

		Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
		velocityPlaceholderMap.put("prj-name", prjName);
		velocityPlaceholderMap.put("organization", organization);
		velocityPlaceholderMap.put("date",
				new Date(System.currentTimeMillis()).toString());
		
		createSiteDocFile("index", "Home", velocityPlaceholderMap, SiteFacet.docFormat);
	}

	@Command(value = "add-umlgraph", help = "Adds UmlGraph support")
	public void addUmlGraphSupport(PipeOut out) {

		// Make sure
		if (!project.hasFacet(UmlGraphFacet.class)) {
			event.fire(new InstallFacets(UmlGraphFacet.class));
		}
	}

	//===================================================
	// Helper functions
	/**
	 * Copies the template document file from the correct template (based on the given codFormat) to the site folder.
	 * 
	 * We also add a link to this document in the site menu
	 * 
	 * @param filename	The name of the file to copy 
	 * @param linkName	The link name to be added to the site.xml file referring to this file
	 * @param velocityPlaceholderMap	Velocity map containing items to replace
	 * @param docFormat	The document format we want to use.
	 */
	private void createSiteDocFile(String filename, String linkName,
			Map<String, Object> velocityPlaceholderMap, SiteDocFormatType docFormat) {
		String sourceUri = buildSourceUri(filename, docFormat);
		String targetUri = buildTargetUri(filename, docFormat);

		VelocityContext velocityContext = velocityUtil
				.createVelocityContext(velocityPlaceholderMap);
		velocityUtil.createResourceAbsolute(sourceUri, velocityContext,
				targetUri, project, velocityEngine);
		
		updateSiteXmlFileWithDocLink(linkName, filename + ".html");
	}

	private String buildSourceUri(String filename, SiteDocFormatType docFormat) {
		if (docFormat.equals(SiteDocFormatType.MARKDOWN)) {
			return "/template-files/src/site/markdown/" + filename + ".md";
		} else {
			return "/template-files/src/site/apt/" + filename + ".apt";
		}
	}

	private String buildTargetUri(String filename, SiteDocFormatType docFormat) {
		if (docFormat.equals(SiteDocFormatType.MARKDOWN)) {
			return "../../site/markdown/" + filename + ".md";
		} else {
			return "../../site/apt/" + filename + ".apt";
		}
	}
	
	void updateSiteXmlFileWithDocLink(String name, String href) {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();

		Xpp3Dom nodeToAdd = new Xpp3Dom("item");
		nodeToAdd.setAttribute("name", name);
		nodeToAdd.setAttribute("href", href);
		
		// Add to the application context
		String applicationContextPath = pom.getProjectDirectory().getAbsolutePath() + "/src/site/site.xml";
		try {
			Xpp3Dom appContextDom = domFileHelper
					.readXmlFile(applicationContextPath);
			
			Xpp3Dom body = appContextDom.getChild("body");
			Xpp3Dom menu = body.getChild("menu");
			
			menu.addChild(nodeToAdd);
			domFileHelper.writeXmlFile(applicationContextPath,
					appContextDom);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
