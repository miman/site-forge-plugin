package eu.miman.forge.plugin.site.facet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;

/**
 * This Facet makes sure the plugin is configured ok to generate UmlGraph images for the Java doc.
 * 
 * An example of how the pom structure looks that is generated can be found at the end of his file.
 * 
 */
@Alias("umlgraph-facet")
@RequiresFacet({ MavenCoreFacet.class, DependencyFacet.class})
public class UmlGraphFacet extends BaseFacet {

	public UmlGraphFacet() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.forge.project.Facet#install()
	 */
	public boolean install() {
		configureProject();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.forge.project.Facet#isInstalled()
	 */
	public boolean isInstalled() {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();

		if (pom.getBuild() == null || pom.getBuild().getPlugins() == null) {
			return false;
		}
		
		List<Plugin> deps = pom.getBuild().getPlugins();
		for (Plugin plugin : deps) {
			if (plugin.getArtifactId().equals("maven-javadoc-plugin")) {
				Xpp3Dom dom = (Xpp3Dom)plugin.getConfiguration();
				if (dom == null || dom.getChild("doclet") == null) {
					return false;
				}
				Xpp3Dom doclet = dom.getChild("doclet");
				if ("org.umlgraph.doclet.UmlGraphDoc".compareTo(doclet.getValue()) == 0) {
					return true;
				}
			}
		}

		return false;
	}

	// Helper functions ****************************************
	/**
	 * Adds the necessary dependencies to the pom.xml file.
	 */
	private void configureProject() {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();

		if (pom.getBuild() == null) {
			pom.setBuild(new Build());
		}
		if (pom.getBuild().getPlugins() == null) {
			pom.getBuild().setPlugins(new ArrayList<Plugin>());
		}
		
		Map<String, Plugin> pluginMap = pom.getBuild().getPluginsAsMap();
		Plugin plugin = pluginMap.get("org.apache.maven.plugins:maven-javadoc-plugin");
		if (plugin == null) {
			plugin = new Plugin();
			plugin.setGroupId("org.apache.maven.plugins");
			plugin.setArtifactId("maven-javadoc-plugin");
			plugin.setVersion("2.9.1");
			pom.getBuild().addPlugin(plugin);
		}
		mergePluginConfiguration(plugin);
		mvnFacet.setPOM(pom);
	}

	
	private void mergePluginConfiguration(Plugin plugin) {
		Xpp3Dom conf = (Xpp3Dom)plugin.getConfiguration();
		if (conf == null) {
			conf = new Xpp3Dom("configuration");
		}

		Xpp3Dom doclet = conf.getChild("doclet");
		if (doclet == null) {
			doclet = new Xpp3Dom("doclet");
			conf.addChild(doclet);
		}
		doclet.setValue("org.umlgraph.doclet.UmlGraphDoc");

		Xpp3Dom aggregate = conf.getChild("aggregate");
		if (aggregate == null) {
			aggregate = new Xpp3Dom("aggregate");
			conf.addChild(aggregate);
		}
		aggregate.setValue("true");
	
		Xpp3Dom show = conf.getChild("show");
		if (show == null) {
			show = new Xpp3Dom("show");
			conf.addChild(show);
		}
		show.setValue("private");
	
		Xpp3Dom docletArtifact = conf.getChild("docletArtifact");
		if (docletArtifact == null) {
			docletArtifact = new Xpp3Dom("docletArtifact");
			conf.addChild(docletArtifact);
		}
		
		Xpp3Dom groupId = docletArtifact.getChild("groupId");
		if (groupId == null) {
			groupId = new Xpp3Dom("groupId");
			docletArtifact.addChild(groupId);
		}
		groupId.setValue("org.umlgraph");
		
		Xpp3Dom artifactId = docletArtifact.getChild("artifactId");
		if (artifactId == null) {
			artifactId = new Xpp3Dom("artifactId");
			docletArtifact.addChild(artifactId);
		}
		artifactId.setValue("umlgraph");
		
		Xpp3Dom version = docletArtifact.getChild("version");
		if (version == null) {
			version = new Xpp3Dom("version");
			docletArtifact.addChild(version);
		}
		version.setValue("5.6.6");
	
		Xpp3Dom charset = conf.getChild("charset");
		if (charset == null) {
			charset = new Xpp3Dom("charset");
			conf.addChild(charset);
		}
		charset.setValue("UTF-8");
	
		Xpp3Dom encoding = conf.getChild("encoding");
		if (encoding == null) {
			encoding = new Xpp3Dom("encoding");
			conf.addChild(encoding);
		}
		encoding.setValue("UTF-8");
	
		Xpp3Dom docencoding = conf.getChild("docencoding");
		if (docencoding == null) {
			docencoding = new Xpp3Dom("docencoding");
			conf.addChild(docencoding);
		}
		docencoding.setValue("UTF-8");
	
		Xpp3Dom additionalparam = conf.getChild("additionalparam");
		if (additionalparam == null) {
			additionalparam = new Xpp3Dom("additionalparam");
			conf.addChild(additionalparam);
		}
		additionalparam.setValue(" -inferrel -attributes -types -visibility -inferdep -quiet -hide java.* -collpackages java.util.* -qualify -postfixpackage -nodefontsize 9 -nodefontpackagesize 7 -nodefontabstractname Sans");
	
		plugin.setConfiguration(conf);
	}
}

/*
 * This is what is added to the pom.xml
	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-javadoc-plugin</artifactId>
				<version>2.9.1</version>
				<configuration>
					<aggregate>true</aggregate>
					<show>private</show>
					<doclet>org.umlgraph.doclet.UmlGraphDoc</doclet>
					<docletArtifact>
						<groupId>org.umlgraph</groupId>
						<artifactId>umlgraph</artifactId>
						<version>5.6.6</version>
					</docletArtifact>
					<charset>UTF-8</charset>
					<encoding>UTF-8</encoding>
					<docencoding>UTF-8</docencoding>
					<!-- -nodefontabstractname was added to remove compile warnings about 
						missing font -->
					<additionalparam> -inferrel -attributes
						-types -visibility -inferdep
						-quiet -hide java.* -collpackages
						java.util.*
						-qualify
						-postfixpackage -nodefontsize 9 -nodefontpackagesize 7
						-nodefontabstractname Sans
					</additionalparam>
				</configuration>
			</plugin>
		</plugins>
	</build>
 */
