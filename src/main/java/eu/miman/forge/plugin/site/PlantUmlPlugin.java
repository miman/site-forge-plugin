package eu.miman.forge.plugin.site;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresProject;
import org.jboss.forge.shell.plugins.SetupCommand;

import se.miman.forge.plugin.util.VelocityUtil;
import eu.miman.forge.plugin.site.facet.PlantUmlFacet;

@Alias("plantuml")
@Help("A plugin that adds PlantUML image generation support to a project")
@RequiresProject
public class PlantUmlPlugin implements Plugin {
	@Inject
	private Event<InstallFacets> event;

	@Inject
	private Project project;

	private final VelocityEngine velocityEngine;
	private VelocityUtil velocityUtil;

	public PlantUmlPlugin() {
		super();
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
	@Command(value = "setup", help = "Adds the maven plugin needed for plantuml")
	public void setup(PipeOut out) {
		if (!project.hasFacet(ResourceFacet.class)) {
			event.fire(new InstallFacets(ResourceFacet.class));
		}

		if (!project.hasFacet(PlantUmlFacet.class))
			event.fire(new InstallFacets(PlantUmlFacet.class));
		else
			ShellMessages.info(out,
					"Project is already a PlantUML enabled project.");
	}

	/**
	 * Command to add PlantUml dependencies and needed files to the project
	 * 
	 * @param out
	 *            Error info statements are written to this pipe to be displayed
	 *            to the user
	 */
	@Command(value = "add-example-uml-files", help = "Adds example PlantUml files")
	public void addExampleFiles(PipeOut out) {

		// Make sure
		if (!project.hasFacet(PlantUmlFacet.class)) {
			event.fire(new InstallFacets(PlantUmlFacet.class));
		}

		createPlantUmlExampleFiles("example_flow_1.puml");
	}

	private void createPlantUmlExampleFiles(String filename) {
		String sourceUri = "/template-files/src/site/uml/" + filename;
		String targetUri = "../../site/uml/" + filename;

		Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
		// velocityPlaceholderMap.put("prj-name", prjName);

		VelocityContext velocityContext = velocityUtil
				.createVelocityContext(velocityPlaceholderMap);
		velocityUtil.createResourceAbsolute(sourceUri, velocityContext,
				targetUri, project, velocityEngine);
	}
}
