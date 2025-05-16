package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.plugin.use.PluginDependency;
import org.gradle.api.artifacts.ExternalModuleDependencyBundle;
import org.gradle.api.artifacts.MutableVersionConstraint;
import org.gradle.api.provider.Provider;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory;
import org.gradle.api.internal.catalog.DefaultVersionCatalog;
import java.util.Map;
import org.gradle.api.internal.attributes.AttributesFactory;
import org.gradle.api.internal.artifacts.dsl.CapabilityNotationParser;
import javax.inject.Inject;

/**
 * A catalog of dependencies accessible via the {@code libs} extension.
 */
@NonNullApi
public class LibrariesForLibs extends AbstractExternalDependencyFactory {

    private final AbstractExternalDependencyFactory owner = this;
    private final ComLibraryAccessors laccForComLibraryAccessors = new ComLibraryAccessors(owner);
    private final CommonsLibraryAccessors laccForCommonsLibraryAccessors = new CommonsLibraryAccessors(owner);
    private final OrgLibraryAccessors laccForOrgLibraryAccessors = new OrgLibraryAccessors(owner);
    private final VersionAccessors vaccForVersionAccessors = new VersionAccessors(providers, config);
    private final BundleAccessors baccForBundleAccessors = new BundleAccessors(objects, providers, config, attributesFactory, capabilityNotationParser);
    private final PluginAccessors paccForPluginAccessors = new PluginAccessors(providers, config);

    @Inject
    public LibrariesForLibs(DefaultVersionCatalog config, ProviderFactory providers, ObjectFactory objects, AttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) {
        super(config, providers, objects, attributesFactory, capabilityNotationParser);
    }

    /**
     * Group of libraries at <b>com</b>
     */
    public ComLibraryAccessors getCom() {
        return laccForComLibraryAccessors;
    }

    /**
     * Group of libraries at <b>commons</b>
     */
    public CommonsLibraryAccessors getCommons() {
        return laccForCommonsLibraryAccessors;
    }

    /**
     * Group of libraries at <b>org</b>
     */
    public OrgLibraryAccessors getOrg() {
        return laccForOrgLibraryAccessors;
    }

    /**
     * Group of versions at <b>versions</b>
     */
    public VersionAccessors getVersions() {
        return vaccForVersionAccessors;
    }

    /**
     * Group of bundles at <b>bundles</b>
     */
    public BundleAccessors getBundles() {
        return baccForBundleAccessors;
    }

    /**
     * Group of plugins at <b>plugins</b>
     */
    public PluginAccessors getPlugins() {
        return paccForPluginAccessors;
    }

    public static class ComLibraryAccessors extends SubDependencyFactory {
        private final ComGithubLibraryAccessors laccForComGithubLibraryAccessors = new ComGithubLibraryAccessors(owner);
        private final ComJaywayLibraryAccessors laccForComJaywayLibraryAccessors = new ComJaywayLibraryAccessors(owner);
        private final ComPuppycrawlLibraryAccessors laccForComPuppycrawlLibraryAccessors = new ComPuppycrawlLibraryAccessors(owner);

        public ComLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>com.github</b>
         */
        public ComGithubLibraryAccessors getGithub() {
            return laccForComGithubLibraryAccessors;
        }

        /**
         * Group of libraries at <b>com.jayway</b>
         */
        public ComJaywayLibraryAccessors getJayway() {
            return laccForComJaywayLibraryAccessors;
        }

        /**
         * Group of libraries at <b>com.puppycrawl</b>
         */
        public ComPuppycrawlLibraryAccessors getPuppycrawl() {
            return laccForComPuppycrawlLibraryAccessors;
        }

    }

    public static class ComGithubLibraryAccessors extends SubDependencyFactory {
        private final ComGithubStefanbirknerLibraryAccessors laccForComGithubStefanbirknerLibraryAccessors = new ComGithubStefanbirknerLibraryAccessors(owner);

        public ComGithubLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>com.github.stefanbirkner</b>
         */
        public ComGithubStefanbirknerLibraryAccessors getStefanbirkner() {
            return laccForComGithubStefanbirknerLibraryAccessors;
        }

    }

    public static class ComGithubStefanbirknerLibraryAccessors extends SubDependencyFactory {
        private final ComGithubStefanbirknerSystemLibraryAccessors laccForComGithubStefanbirknerSystemLibraryAccessors = new ComGithubStefanbirknerSystemLibraryAccessors(owner);

        public ComGithubStefanbirknerLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>com.github.stefanbirkner.system</b>
         */
        public ComGithubStefanbirknerSystemLibraryAccessors getSystem() {
            return laccForComGithubStefanbirknerSystemLibraryAccessors;
        }

    }

    public static class ComGithubStefanbirknerSystemLibraryAccessors extends SubDependencyFactory {

        public ComGithubStefanbirknerSystemLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>rules</b> with <b>com.github.stefanbirkner:system-rules</b> coordinates and
         * with version reference <b>com.github.stefanbirkner.system.rules</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getRules() {
            return create("com.github.stefanbirkner.system.rules");
        }

    }

    public static class ComJaywayLibraryAccessors extends SubDependencyFactory {
        private final ComJaywayJsonpathLibraryAccessors laccForComJaywayJsonpathLibraryAccessors = new ComJaywayJsonpathLibraryAccessors(owner);

        public ComJaywayLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>com.jayway.jsonpath</b>
         */
        public ComJaywayJsonpathLibraryAccessors getJsonpath() {
            return laccForComJaywayJsonpathLibraryAccessors;
        }

    }

    public static class ComJaywayJsonpathLibraryAccessors extends SubDependencyFactory {
        private final ComJaywayJsonpathJsonLibraryAccessors laccForComJaywayJsonpathJsonLibraryAccessors = new ComJaywayJsonpathJsonLibraryAccessors(owner);

        public ComJaywayJsonpathLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>com.jayway.jsonpath.json</b>
         */
        public ComJaywayJsonpathJsonLibraryAccessors getJson() {
            return laccForComJaywayJsonpathJsonLibraryAccessors;
        }

    }

    public static class ComJaywayJsonpathJsonLibraryAccessors extends SubDependencyFactory {
        private final ComJaywayJsonpathJsonPathLibraryAccessors laccForComJaywayJsonpathJsonPathLibraryAccessors = new ComJaywayJsonpathJsonPathLibraryAccessors(owner);

        public ComJaywayJsonpathJsonLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>com.jayway.jsonpath.json.path</b>
         */
        public ComJaywayJsonpathJsonPathLibraryAccessors getPath() {
            return laccForComJaywayJsonpathJsonPathLibraryAccessors;
        }

    }

    public static class ComJaywayJsonpathJsonPathLibraryAccessors extends SubDependencyFactory implements DependencyNotationSupplier {

        public ComJaywayJsonpathJsonPathLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>path</b> with <b>com.jayway.jsonpath:json-path</b> coordinates and
         * with version reference <b>com.jayway.jsonpath.json</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> asProvider() {
            return create("com.jayway.jsonpath.json.path");
        }

        /**
         * Dependency provider for <b>assert</b> with <b>com.jayway.jsonpath:json-path-assert</b> coordinates and
         * with version reference <b>com.jayway.jsonpath.json</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getAssert() {
            return create("com.jayway.jsonpath.json.path.assert");
        }

    }

    public static class ComPuppycrawlLibraryAccessors extends SubDependencyFactory {
        private final ComPuppycrawlToolsLibraryAccessors laccForComPuppycrawlToolsLibraryAccessors = new ComPuppycrawlToolsLibraryAccessors(owner);

        public ComPuppycrawlLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>com.puppycrawl.tools</b>
         */
        public ComPuppycrawlToolsLibraryAccessors getTools() {
            return laccForComPuppycrawlToolsLibraryAccessors;
        }

    }

    public static class ComPuppycrawlToolsLibraryAccessors extends SubDependencyFactory {

        public ComPuppycrawlToolsLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>checkstyle</b> with <b>com.puppycrawl.tools:checkstyle</b> coordinates and
         * with version reference <b>com.puppycrawl.tools.checkstyle</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getCheckstyle() {
            return create("com.puppycrawl.tools.checkstyle");
        }

    }

    public static class CommonsLibraryAccessors extends SubDependencyFactory {
        private final CommonsIoLibraryAccessors laccForCommonsIoLibraryAccessors = new CommonsIoLibraryAccessors(owner);

        public CommonsLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>commons.io</b>
         */
        public CommonsIoLibraryAccessors getIo() {
            return laccForCommonsIoLibraryAccessors;
        }

    }

    public static class CommonsIoLibraryAccessors extends SubDependencyFactory {
        private final CommonsIoCommonsLibraryAccessors laccForCommonsIoCommonsLibraryAccessors = new CommonsIoCommonsLibraryAccessors(owner);

        public CommonsIoLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>commons.io.commons</b>
         */
        public CommonsIoCommonsLibraryAccessors getCommons() {
            return laccForCommonsIoCommonsLibraryAccessors;
        }

    }

    public static class CommonsIoCommonsLibraryAccessors extends SubDependencyFactory {

        public CommonsIoCommonsLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>io</b> with <b>commons-io:commons-io</b> coordinates and
         * with version reference <b>commons.io.commons.io</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getIo() {
            return create("commons.io.commons.io");
        }

    }

    public static class OrgLibraryAccessors extends SubDependencyFactory {
        private final OrgApacheLibraryAccessors laccForOrgApacheLibraryAccessors = new OrgApacheLibraryAccessors(owner);
        private final OrgNiisLibraryAccessors laccForOrgNiisLibraryAccessors = new OrgNiisLibraryAccessors(owner);
        private final OrgProjectlombokLibraryAccessors laccForOrgProjectlombokLibraryAccessors = new OrgProjectlombokLibraryAccessors(owner);
        private final OrgSpringframeworkLibraryAccessors laccForOrgSpringframeworkLibraryAccessors = new OrgSpringframeworkLibraryAccessors(owner);
        private final OrgWiremockLibraryAccessors laccForOrgWiremockLibraryAccessors = new OrgWiremockLibraryAccessors(owner);
        private final OrgXmlunitLibraryAccessors laccForOrgXmlunitLibraryAccessors = new OrgXmlunitLibraryAccessors(owner);

        public OrgLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.apache</b>
         */
        public OrgApacheLibraryAccessors getApache() {
            return laccForOrgApacheLibraryAccessors;
        }

        /**
         * Group of libraries at <b>org.niis</b>
         */
        public OrgNiisLibraryAccessors getNiis() {
            return laccForOrgNiisLibraryAccessors;
        }

        /**
         * Group of libraries at <b>org.projectlombok</b>
         */
        public OrgProjectlombokLibraryAccessors getProjectlombok() {
            return laccForOrgProjectlombokLibraryAccessors;
        }

        /**
         * Group of libraries at <b>org.springframework</b>
         */
        public OrgSpringframeworkLibraryAccessors getSpringframework() {
            return laccForOrgSpringframeworkLibraryAccessors;
        }

        /**
         * Group of libraries at <b>org.wiremock</b>
         */
        public OrgWiremockLibraryAccessors getWiremock() {
            return laccForOrgWiremockLibraryAccessors;
        }

        /**
         * Group of libraries at <b>org.xmlunit</b>
         */
        public OrgXmlunitLibraryAccessors getXmlunit() {
            return laccForOrgXmlunitLibraryAccessors;
        }

    }

    public static class OrgApacheLibraryAccessors extends SubDependencyFactory {
        private final OrgApacheTomcatLibraryAccessors laccForOrgApacheTomcatLibraryAccessors = new OrgApacheTomcatLibraryAccessors(owner);

        public OrgApacheLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.apache.tomcat</b>
         */
        public OrgApacheTomcatLibraryAccessors getTomcat() {
            return laccForOrgApacheTomcatLibraryAccessors;
        }

    }

    public static class OrgApacheTomcatLibraryAccessors extends SubDependencyFactory {
        private final OrgApacheTomcatEmbedLibraryAccessors laccForOrgApacheTomcatEmbedLibraryAccessors = new OrgApacheTomcatEmbedLibraryAccessors(owner);

        public OrgApacheTomcatLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.apache.tomcat.embed</b>
         */
        public OrgApacheTomcatEmbedLibraryAccessors getEmbed() {
            return laccForOrgApacheTomcatEmbedLibraryAccessors;
        }

    }

    public static class OrgApacheTomcatEmbedLibraryAccessors extends SubDependencyFactory {
        private final OrgApacheTomcatEmbedTomcatLibraryAccessors laccForOrgApacheTomcatEmbedTomcatLibraryAccessors = new OrgApacheTomcatEmbedTomcatLibraryAccessors(owner);

        public OrgApacheTomcatEmbedLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.apache.tomcat.embed.tomcat</b>
         */
        public OrgApacheTomcatEmbedTomcatLibraryAccessors getTomcat() {
            return laccForOrgApacheTomcatEmbedTomcatLibraryAccessors;
        }

    }

    public static class OrgApacheTomcatEmbedTomcatLibraryAccessors extends SubDependencyFactory {
        private final OrgApacheTomcatEmbedTomcatEmbedLibraryAccessors laccForOrgApacheTomcatEmbedTomcatEmbedLibraryAccessors = new OrgApacheTomcatEmbedTomcatEmbedLibraryAccessors(owner);

        public OrgApacheTomcatEmbedTomcatLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.apache.tomcat.embed.tomcat.embed</b>
         */
        public OrgApacheTomcatEmbedTomcatEmbedLibraryAccessors getEmbed() {
            return laccForOrgApacheTomcatEmbedTomcatEmbedLibraryAccessors;
        }

    }

    public static class OrgApacheTomcatEmbedTomcatEmbedLibraryAccessors extends SubDependencyFactory {

        public OrgApacheTomcatEmbedTomcatEmbedLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>jasper</b> with <b>org.apache.tomcat.embed:tomcat-embed-jasper</b> coordinates and
         * with version reference <b>org.apache.tomcat.embed.tomcat.embed.jasper</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getJasper() {
            return create("org.apache.tomcat.embed.tomcat.embed.jasper");
        }

    }

    public static class OrgNiisLibraryAccessors extends SubDependencyFactory {
        private final OrgNiisXrd4jLibraryAccessors laccForOrgNiisXrd4jLibraryAccessors = new OrgNiisXrd4jLibraryAccessors(owner);

        public OrgNiisLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.niis.xrd4j</b>
         */
        public OrgNiisXrd4jLibraryAccessors getXrd4j() {
            return laccForOrgNiisXrd4jLibraryAccessors;
        }

    }

    public static class OrgNiisXrd4jLibraryAccessors extends SubDependencyFactory {

        public OrgNiisXrd4jLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>client</b> with <b>org.niis.xrd4j:client</b> coordinates and
         * with version reference <b>org.niis.xrd4j</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getClient() {
            return create("org.niis.xrd4j.client");
        }

        /**
         * Dependency provider for <b>common</b> with <b>org.niis.xrd4j:common</b> coordinates and
         * with version reference <b>org.niis.xrd4j</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getCommon() {
            return create("org.niis.xrd4j.common");
        }

        /**
         * Dependency provider for <b>rest</b> with <b>org.niis.xrd4j:rest</b> coordinates and
         * with version reference <b>org.niis.xrd4j</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getRest() {
            return create("org.niis.xrd4j.rest");
        }

        /**
         * Dependency provider for <b>server</b> with <b>org.niis.xrd4j:server</b> coordinates and
         * with version reference <b>org.niis.xrd4j</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getServer() {
            return create("org.niis.xrd4j.server");
        }

    }

    public static class OrgProjectlombokLibraryAccessors extends SubDependencyFactory {

        public OrgProjectlombokLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>lombok</b> with <b>org.projectlombok:lombok</b> coordinates and
         * with version reference <b>org.projectlombok.lombok</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getLombok() {
            return create("org.projectlombok.lombok");
        }

    }

    public static class OrgSpringframeworkLibraryAccessors extends SubDependencyFactory {
        private final OrgSpringframeworkBootLibraryAccessors laccForOrgSpringframeworkBootLibraryAccessors = new OrgSpringframeworkBootLibraryAccessors(owner);

        public OrgSpringframeworkLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.springframework.boot</b>
         */
        public OrgSpringframeworkBootLibraryAccessors getBoot() {
            return laccForOrgSpringframeworkBootLibraryAccessors;
        }

    }

    public static class OrgSpringframeworkBootLibraryAccessors extends SubDependencyFactory {
        private final OrgSpringframeworkBootSpringLibraryAccessors laccForOrgSpringframeworkBootSpringLibraryAccessors = new OrgSpringframeworkBootSpringLibraryAccessors(owner);

        public OrgSpringframeworkBootLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.springframework.boot.spring</b>
         */
        public OrgSpringframeworkBootSpringLibraryAccessors getSpring() {
            return laccForOrgSpringframeworkBootSpringLibraryAccessors;
        }

    }

    public static class OrgSpringframeworkBootSpringLibraryAccessors extends SubDependencyFactory {
        private final OrgSpringframeworkBootSpringBootLibraryAccessors laccForOrgSpringframeworkBootSpringBootLibraryAccessors = new OrgSpringframeworkBootSpringBootLibraryAccessors(owner);

        public OrgSpringframeworkBootSpringLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.springframework.boot.spring.boot</b>
         */
        public OrgSpringframeworkBootSpringBootLibraryAccessors getBoot() {
            return laccForOrgSpringframeworkBootSpringBootLibraryAccessors;
        }

    }

    public static class OrgSpringframeworkBootSpringBootLibraryAccessors extends SubDependencyFactory {
        private final OrgSpringframeworkBootSpringBootStarterLibraryAccessors laccForOrgSpringframeworkBootSpringBootStarterLibraryAccessors = new OrgSpringframeworkBootSpringBootStarterLibraryAccessors(owner);

        public OrgSpringframeworkBootSpringBootLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.springframework.boot.spring.boot.starter</b>
         */
        public OrgSpringframeworkBootSpringBootStarterLibraryAccessors getStarter() {
            return laccForOrgSpringframeworkBootSpringBootStarterLibraryAccessors;
        }

    }

    public static class OrgSpringframeworkBootSpringBootStarterLibraryAccessors extends SubDependencyFactory {

        public OrgSpringframeworkBootSpringBootStarterLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>aop</b> with <b>org.springframework.boot:spring-boot-starter-aop</b> coordinates and
         * with version reference <b>org.springframework.boot.spring.boot</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getAop() {
            return create("org.springframework.boot.spring.boot.starter.aop");
        }

        /**
         * Dependency provider for <b>test</b> with <b>org.springframework.boot:spring-boot-starter-test</b> coordinates and
         * with version reference <b>org.springframework.boot.spring.boot</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getTest() {
            return create("org.springframework.boot.spring.boot.starter.test");
        }

        /**
         * Dependency provider for <b>tomcat</b> with <b>org.springframework.boot:spring-boot-starter-tomcat</b> coordinates and
         * with version reference <b>org.springframework.boot.spring.boot</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getTomcat() {
            return create("org.springframework.boot.spring.boot.starter.tomcat");
        }

        /**
         * Dependency provider for <b>web</b> with <b>org.springframework.boot:spring-boot-starter-web</b> coordinates and
         * with version reference <b>org.springframework.boot.spring.boot</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getWeb() {
            return create("org.springframework.boot.spring.boot.starter.web");
        }

    }

    public static class OrgWiremockLibraryAccessors extends SubDependencyFactory {
        private final OrgWiremockIntegrationsLibraryAccessors laccForOrgWiremockIntegrationsLibraryAccessors = new OrgWiremockIntegrationsLibraryAccessors(owner);

        public OrgWiremockLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.wiremock.integrations</b>
         */
        public OrgWiremockIntegrationsLibraryAccessors getIntegrations() {
            return laccForOrgWiremockIntegrationsLibraryAccessors;
        }

    }

    public static class OrgWiremockIntegrationsLibraryAccessors extends SubDependencyFactory {
        private final OrgWiremockIntegrationsWiremockLibraryAccessors laccForOrgWiremockIntegrationsWiremockLibraryAccessors = new OrgWiremockIntegrationsWiremockLibraryAccessors(owner);

        public OrgWiremockIntegrationsLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.wiremock.integrations.wiremock</b>
         */
        public OrgWiremockIntegrationsWiremockLibraryAccessors getWiremock() {
            return laccForOrgWiremockIntegrationsWiremockLibraryAccessors;
        }

    }

    public static class OrgWiremockIntegrationsWiremockLibraryAccessors extends SubDependencyFactory {
        private final OrgWiremockIntegrationsWiremockSpringLibraryAccessors laccForOrgWiremockIntegrationsWiremockSpringLibraryAccessors = new OrgWiremockIntegrationsWiremockSpringLibraryAccessors(owner);

        public OrgWiremockIntegrationsWiremockLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.wiremock.integrations.wiremock.spring</b>
         */
        public OrgWiremockIntegrationsWiremockSpringLibraryAccessors getSpring() {
            return laccForOrgWiremockIntegrationsWiremockSpringLibraryAccessors;
        }

    }

    public static class OrgWiremockIntegrationsWiremockSpringLibraryAccessors extends SubDependencyFactory {

        public OrgWiremockIntegrationsWiremockSpringLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>boot</b> with <b>org.wiremock.integrations:wiremock-spring-boot</b> coordinates and
         * with version reference <b>org.wiremock.integrations.wiremock.spring.boot</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getBoot() {
            return create("org.wiremock.integrations.wiremock.spring.boot");
        }

    }

    public static class OrgXmlunitLibraryAccessors extends SubDependencyFactory {
        private final OrgXmlunitXmlunitLibraryAccessors laccForOrgXmlunitXmlunitLibraryAccessors = new OrgXmlunitXmlunitLibraryAccessors(owner);

        public OrgXmlunitLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>org.xmlunit.xmlunit</b>
         */
        public OrgXmlunitXmlunitLibraryAccessors getXmlunit() {
            return laccForOrgXmlunitXmlunitLibraryAccessors;
        }

    }

    public static class OrgXmlunitXmlunitLibraryAccessors extends SubDependencyFactory {

        public OrgXmlunitXmlunitLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>assertj</b> with <b>org.xmlunit:xmlunit-assertj</b> coordinates and
         * with version reference <b>org.xmlunit.xmlunit</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getAssertj() {
            return create("org.xmlunit.xmlunit.assertj");
        }

        /**
         * Dependency provider for <b>core</b> with <b>org.xmlunit:xmlunit-core</b> coordinates and
         * with version reference <b>org.xmlunit.xmlunit</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getCore() {
            return create("org.xmlunit.xmlunit.core");
        }

        /**
         * Dependency provider for <b>matchers</b> with <b>org.xmlunit:xmlunit-matchers</b> coordinates and
         * with version reference <b>org.xmlunit.xmlunit</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getMatchers() {
            return create("org.xmlunit.xmlunit.matchers");
        }

    }

    public static class VersionAccessors extends VersionFactory  {

        private final ComVersionAccessors vaccForComVersionAccessors = new ComVersionAccessors(providers, config);
        private final CommonsVersionAccessors vaccForCommonsVersionAccessors = new CommonsVersionAccessors(providers, config);
        private final OrgVersionAccessors vaccForOrgVersionAccessors = new OrgVersionAccessors(providers, config);
        public VersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>dependencyManagement</b> with value <b>1.0.11.RELEASE</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getDependencyManagement() { return getVersion("dependencyManagement"); }

        /**
         * Version alias <b>springBoot</b> with value <b>3.4.4</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getSpringBoot() { return getVersion("springBoot"); }

        /**
         * Group of versions at <b>versions.com</b>
         */
        public ComVersionAccessors getCom() {
            return vaccForComVersionAccessors;
        }

        /**
         * Group of versions at <b>versions.commons</b>
         */
        public CommonsVersionAccessors getCommons() {
            return vaccForCommonsVersionAccessors;
        }

        /**
         * Group of versions at <b>versions.org</b>
         */
        public OrgVersionAccessors getOrg() {
            return vaccForOrgVersionAccessors;
        }

    }

    public static class ComVersionAccessors extends VersionFactory  {

        private final ComGithubVersionAccessors vaccForComGithubVersionAccessors = new ComGithubVersionAccessors(providers, config);
        private final ComJaywayVersionAccessors vaccForComJaywayVersionAccessors = new ComJaywayVersionAccessors(providers, config);
        private final ComPuppycrawlVersionAccessors vaccForComPuppycrawlVersionAccessors = new ComPuppycrawlVersionAccessors(providers, config);
        public ComVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.com.github</b>
         */
        public ComGithubVersionAccessors getGithub() {
            return vaccForComGithubVersionAccessors;
        }

        /**
         * Group of versions at <b>versions.com.jayway</b>
         */
        public ComJaywayVersionAccessors getJayway() {
            return vaccForComJaywayVersionAccessors;
        }

        /**
         * Group of versions at <b>versions.com.puppycrawl</b>
         */
        public ComPuppycrawlVersionAccessors getPuppycrawl() {
            return vaccForComPuppycrawlVersionAccessors;
        }

    }

    public static class ComGithubVersionAccessors extends VersionFactory  {

        private final ComGithubStefanbirknerVersionAccessors vaccForComGithubStefanbirknerVersionAccessors = new ComGithubStefanbirknerVersionAccessors(providers, config);
        public ComGithubVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.com.github.stefanbirkner</b>
         */
        public ComGithubStefanbirknerVersionAccessors getStefanbirkner() {
            return vaccForComGithubStefanbirknerVersionAccessors;
        }

    }

    public static class ComGithubStefanbirknerVersionAccessors extends VersionFactory  {

        private final ComGithubStefanbirknerSystemVersionAccessors vaccForComGithubStefanbirknerSystemVersionAccessors = new ComGithubStefanbirknerSystemVersionAccessors(providers, config);
        public ComGithubStefanbirknerVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.com.github.stefanbirkner.system</b>
         */
        public ComGithubStefanbirknerSystemVersionAccessors getSystem() {
            return vaccForComGithubStefanbirknerSystemVersionAccessors;
        }

    }

    public static class ComGithubStefanbirknerSystemVersionAccessors extends VersionFactory  {

        public ComGithubStefanbirknerSystemVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>com.github.stefanbirkner.system.rules</b> with value <b>1.16.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getRules() { return getVersion("com.github.stefanbirkner.system.rules"); }

    }

    public static class ComJaywayVersionAccessors extends VersionFactory  {

        private final ComJaywayJsonpathVersionAccessors vaccForComJaywayJsonpathVersionAccessors = new ComJaywayJsonpathVersionAccessors(providers, config);
        public ComJaywayVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.com.jayway.jsonpath</b>
         */
        public ComJaywayJsonpathVersionAccessors getJsonpath() {
            return vaccForComJaywayJsonpathVersionAccessors;
        }

    }

    public static class ComJaywayJsonpathVersionAccessors extends VersionFactory  {

        public ComJaywayJsonpathVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>com.jayway.jsonpath.json</b> with value <b>2.2.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getJson() { return getVersion("com.jayway.jsonpath.json"); }

    }

    public static class ComPuppycrawlVersionAccessors extends VersionFactory  {

        private final ComPuppycrawlToolsVersionAccessors vaccForComPuppycrawlToolsVersionAccessors = new ComPuppycrawlToolsVersionAccessors(providers, config);
        public ComPuppycrawlVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.com.puppycrawl.tools</b>
         */
        public ComPuppycrawlToolsVersionAccessors getTools() {
            return vaccForComPuppycrawlToolsVersionAccessors;
        }

    }

    public static class ComPuppycrawlToolsVersionAccessors extends VersionFactory  {

        public ComPuppycrawlToolsVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>com.puppycrawl.tools.checkstyle</b> with value <b>10.23.1</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getCheckstyle() { return getVersion("com.puppycrawl.tools.checkstyle"); }

    }

    public static class CommonsVersionAccessors extends VersionFactory  {

        private final CommonsIoVersionAccessors vaccForCommonsIoVersionAccessors = new CommonsIoVersionAccessors(providers, config);
        public CommonsVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.commons.io</b>
         */
        public CommonsIoVersionAccessors getIo() {
            return vaccForCommonsIoVersionAccessors;
        }

    }

    public static class CommonsIoVersionAccessors extends VersionFactory  {

        private final CommonsIoCommonsVersionAccessors vaccForCommonsIoCommonsVersionAccessors = new CommonsIoCommonsVersionAccessors(providers, config);
        public CommonsIoVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.commons.io.commons</b>
         */
        public CommonsIoCommonsVersionAccessors getCommons() {
            return vaccForCommonsIoCommonsVersionAccessors;
        }

    }

    public static class CommonsIoCommonsVersionAccessors extends VersionFactory  {

        public CommonsIoCommonsVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>commons.io.commons.io</b> with value <b>2.6</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getIo() { return getVersion("commons.io.commons.io"); }

    }

    public static class OrgVersionAccessors extends VersionFactory  {

        private final OrgApacheVersionAccessors vaccForOrgApacheVersionAccessors = new OrgApacheVersionAccessors(providers, config);
        private final OrgNiisVersionAccessors vaccForOrgNiisVersionAccessors = new OrgNiisVersionAccessors(providers, config);
        private final OrgProjectlombokVersionAccessors vaccForOrgProjectlombokVersionAccessors = new OrgProjectlombokVersionAccessors(providers, config);
        private final OrgSpringframeworkVersionAccessors vaccForOrgSpringframeworkVersionAccessors = new OrgSpringframeworkVersionAccessors(providers, config);
        private final OrgWiremockVersionAccessors vaccForOrgWiremockVersionAccessors = new OrgWiremockVersionAccessors(providers, config);
        private final OrgXmlunitVersionAccessors vaccForOrgXmlunitVersionAccessors = new OrgXmlunitVersionAccessors(providers, config);
        public OrgVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.apache</b>
         */
        public OrgApacheVersionAccessors getApache() {
            return vaccForOrgApacheVersionAccessors;
        }

        /**
         * Group of versions at <b>versions.org.niis</b>
         */
        public OrgNiisVersionAccessors getNiis() {
            return vaccForOrgNiisVersionAccessors;
        }

        /**
         * Group of versions at <b>versions.org.projectlombok</b>
         */
        public OrgProjectlombokVersionAccessors getProjectlombok() {
            return vaccForOrgProjectlombokVersionAccessors;
        }

        /**
         * Group of versions at <b>versions.org.springframework</b>
         */
        public OrgSpringframeworkVersionAccessors getSpringframework() {
            return vaccForOrgSpringframeworkVersionAccessors;
        }

        /**
         * Group of versions at <b>versions.org.wiremock</b>
         */
        public OrgWiremockVersionAccessors getWiremock() {
            return vaccForOrgWiremockVersionAccessors;
        }

        /**
         * Group of versions at <b>versions.org.xmlunit</b>
         */
        public OrgXmlunitVersionAccessors getXmlunit() {
            return vaccForOrgXmlunitVersionAccessors;
        }

    }

    public static class OrgApacheVersionAccessors extends VersionFactory  {

        private final OrgApacheTomcatVersionAccessors vaccForOrgApacheTomcatVersionAccessors = new OrgApacheTomcatVersionAccessors(providers, config);
        public OrgApacheVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.apache.tomcat</b>
         */
        public OrgApacheTomcatVersionAccessors getTomcat() {
            return vaccForOrgApacheTomcatVersionAccessors;
        }

    }

    public static class OrgApacheTomcatVersionAccessors extends VersionFactory  {

        private final OrgApacheTomcatEmbedVersionAccessors vaccForOrgApacheTomcatEmbedVersionAccessors = new OrgApacheTomcatEmbedVersionAccessors(providers, config);
        public OrgApacheTomcatVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.apache.tomcat.embed</b>
         */
        public OrgApacheTomcatEmbedVersionAccessors getEmbed() {
            return vaccForOrgApacheTomcatEmbedVersionAccessors;
        }

    }

    public static class OrgApacheTomcatEmbedVersionAccessors extends VersionFactory  {

        private final OrgApacheTomcatEmbedTomcatVersionAccessors vaccForOrgApacheTomcatEmbedTomcatVersionAccessors = new OrgApacheTomcatEmbedTomcatVersionAccessors(providers, config);
        public OrgApacheTomcatEmbedVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.apache.tomcat.embed.tomcat</b>
         */
        public OrgApacheTomcatEmbedTomcatVersionAccessors getTomcat() {
            return vaccForOrgApacheTomcatEmbedTomcatVersionAccessors;
        }

    }

    public static class OrgApacheTomcatEmbedTomcatVersionAccessors extends VersionFactory  {

        private final OrgApacheTomcatEmbedTomcatEmbedVersionAccessors vaccForOrgApacheTomcatEmbedTomcatEmbedVersionAccessors = new OrgApacheTomcatEmbedTomcatEmbedVersionAccessors(providers, config);
        public OrgApacheTomcatEmbedTomcatVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.apache.tomcat.embed.tomcat.embed</b>
         */
        public OrgApacheTomcatEmbedTomcatEmbedVersionAccessors getEmbed() {
            return vaccForOrgApacheTomcatEmbedTomcatEmbedVersionAccessors;
        }

    }

    public static class OrgApacheTomcatEmbedTomcatEmbedVersionAccessors extends VersionFactory  {

        public OrgApacheTomcatEmbedTomcatEmbedVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>org.apache.tomcat.embed.tomcat.embed.jasper</b> with value <b>10.1.39</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getJasper() { return getVersion("org.apache.tomcat.embed.tomcat.embed.jasper"); }

    }

    public static class OrgNiisVersionAccessors extends VersionFactory  {

        public OrgNiisVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>org.niis.xrd4j</b> with value <b>0.6.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getXrd4j() { return getVersion("org.niis.xrd4j"); }

    }

    public static class OrgProjectlombokVersionAccessors extends VersionFactory  {

        public OrgProjectlombokVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>org.projectlombok.lombok</b> with value <b>1.18.30</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getLombok() { return getVersion("org.projectlombok.lombok"); }

    }

    public static class OrgSpringframeworkVersionAccessors extends VersionFactory  {

        private final OrgSpringframeworkBootVersionAccessors vaccForOrgSpringframeworkBootVersionAccessors = new OrgSpringframeworkBootVersionAccessors(providers, config);
        public OrgSpringframeworkVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.springframework.boot</b>
         */
        public OrgSpringframeworkBootVersionAccessors getBoot() {
            return vaccForOrgSpringframeworkBootVersionAccessors;
        }

    }

    public static class OrgSpringframeworkBootVersionAccessors extends VersionFactory  {

        private final OrgSpringframeworkBootSpringVersionAccessors vaccForOrgSpringframeworkBootSpringVersionAccessors = new OrgSpringframeworkBootSpringVersionAccessors(providers, config);
        public OrgSpringframeworkBootVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.springframework.boot.spring</b>
         */
        public OrgSpringframeworkBootSpringVersionAccessors getSpring() {
            return vaccForOrgSpringframeworkBootSpringVersionAccessors;
        }

    }

    public static class OrgSpringframeworkBootSpringVersionAccessors extends VersionFactory  {

        public OrgSpringframeworkBootSpringVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>org.springframework.boot.spring.boot</b> with value <b>3.4.4</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getBoot() { return getVersion("org.springframework.boot.spring.boot"); }

    }

    public static class OrgWiremockVersionAccessors extends VersionFactory  {

        private final OrgWiremockIntegrationsVersionAccessors vaccForOrgWiremockIntegrationsVersionAccessors = new OrgWiremockIntegrationsVersionAccessors(providers, config);
        public OrgWiremockVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.wiremock.integrations</b>
         */
        public OrgWiremockIntegrationsVersionAccessors getIntegrations() {
            return vaccForOrgWiremockIntegrationsVersionAccessors;
        }

    }

    public static class OrgWiremockIntegrationsVersionAccessors extends VersionFactory  {

        private final OrgWiremockIntegrationsWiremockVersionAccessors vaccForOrgWiremockIntegrationsWiremockVersionAccessors = new OrgWiremockIntegrationsWiremockVersionAccessors(providers, config);
        public OrgWiremockIntegrationsVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.wiremock.integrations.wiremock</b>
         */
        public OrgWiremockIntegrationsWiremockVersionAccessors getWiremock() {
            return vaccForOrgWiremockIntegrationsWiremockVersionAccessors;
        }

    }

    public static class OrgWiremockIntegrationsWiremockVersionAccessors extends VersionFactory  {

        private final OrgWiremockIntegrationsWiremockSpringVersionAccessors vaccForOrgWiremockIntegrationsWiremockSpringVersionAccessors = new OrgWiremockIntegrationsWiremockSpringVersionAccessors(providers, config);
        public OrgWiremockIntegrationsWiremockVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.org.wiremock.integrations.wiremock.spring</b>
         */
        public OrgWiremockIntegrationsWiremockSpringVersionAccessors getSpring() {
            return vaccForOrgWiremockIntegrationsWiremockSpringVersionAccessors;
        }

    }

    public static class OrgWiremockIntegrationsWiremockSpringVersionAccessors extends VersionFactory  {

        public OrgWiremockIntegrationsWiremockSpringVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>org.wiremock.integrations.wiremock.spring.boot</b> with value <b>3.6.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getBoot() { return getVersion("org.wiremock.integrations.wiremock.spring.boot"); }

    }

    public static class OrgXmlunitVersionAccessors extends VersionFactory  {

        public OrgXmlunitVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>org.xmlunit.xmlunit</b> with value <b>2.7.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getXmlunit() { return getVersion("org.xmlunit.xmlunit"); }

    }

    public static class BundleAccessors extends BundleFactory {

        public BundleAccessors(ObjectFactory objects, ProviderFactory providers, DefaultVersionCatalog config, AttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) { super(objects, providers, config, attributesFactory, capabilityNotationParser); }

    }

    public static class PluginAccessors extends PluginFactory {

        public PluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Plugin provider for <b>dependencyManagement</b> with plugin id <b>io.spring.dependency-management</b> and
         * with version reference <b>dependencyManagement</b>
         * <p>
         * This plugin was declared in catalog libs.versions.toml
         */
        public Provider<PluginDependency> getDependencyManagement() { return createPlugin("dependencyManagement"); }

        /**
         * Plugin provider for <b>springBoot</b> with plugin id <b>org.springframework.boot</b> and
         * with version reference <b>springBoot</b>
         * <p>
         * This plugin was declared in catalog libs.versions.toml
         */
        public Provider<PluginDependency> getSpringBoot() { return createPlugin("springBoot"); }

    }

}
