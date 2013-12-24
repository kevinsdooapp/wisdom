package org.wisdom.maven.mojos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wisdom.maven.Constants;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.node.NPM;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;

/**
 * Compiles coffeescript files.
 */
@Mojo(name = "compile-coffeescript", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class CoffeeScriptCompilerMojo extends AbstractWisdomWatcherMojo implements Constants {

    public static final String COFFEE_SCRIPT_NPM_NAME = "coffee-script";
    public static final String COFFEE_SCRIPT_NPM_VERSION = "1.6.3";
    public static final String COFFEE_SCRIPT_COMMAND = "coffee";
    private File internalSources;
    private File destinationForInternals;
    private File externalSources;
    private File destinationForExternals;

    @Override
    public void execute() throws MojoExecutionException {
        this.internalSources = new File(basedir, MAIN_RESOURCES_DIR);
        this.destinationForInternals = new File(buildDirectory, "classes");

        this.externalSources = new File(basedir, ASSETS_SRC_DIR);
        this.destinationForExternals = new File(getWisdomRootDirectory(), ASSETS_DIR);

        NPM.Install install = new NPM.Install(this);
        install.install(COFFEE_SCRIPT_NPM_NAME, COFFEE_SCRIPT_NPM_VERSION);

        if (internalSources.isDirectory()) {
            getLog().info("Compiling CoffeeScript files from " + internalSources.getAbsolutePath());
            invokeCoffeeScriptCompiler(internalSources, destinationForInternals);
        }

        if (externalSources.isDirectory()) {
            getLog().info("Compiling CoffeeScript files from " + externalSources.getAbsolutePath());
            invokeCoffeeScriptCompiler(externalSources, destinationForExternals);
        }
    }

    @Override
    public boolean accept(File file) {
        return
                (WatcherUtils.isInDirectory(file, WatcherUtils.getInternalAssetsSource(basedir))
                        || (WatcherUtils.isInDirectory(file, WatcherUtils.getExternalAssetsSource(basedir)))
                )
                && WatcherUtils
                        .hasExtension(file, "coffee");
    }

    private File getOutputJSFile(File input) {
        File source;
        File destination;
        if (input.getAbsolutePath().startsWith(internalSources.getAbsolutePath())) {
            source = internalSources;
            destination = destinationForInternals;
        } else if (input.getAbsolutePath().startsWith(externalSources.getAbsolutePath())) {
            source = externalSources;
            destination = destinationForExternals;
        } else {
            return null;
        }

        String jsFileName = input.getName().substring(0, input.getName().length() - ".coffee".length()) + ".js";
        String path = input.getParentFile().getAbsolutePath().substring(source.getAbsolutePath().length());
        return new File(destination, path + "/" + jsFileName);
    }

    private void compile(File file) throws WatchingException {
        if (file == null) {
            return;
        }
        File out = getOutputJSFile(file);
        getLog().info("Compiling CoffeeScript " + file.getAbsolutePath() + " to " + out.getAbsolutePath());

        try {
            invokeCoffeeScriptCompiler(file, out.getParentFile());
        } catch (MojoExecutionException e) { //NOSONAR
            throw new WatchingException("Error during the compilation of " + file.getName() + " : " + e.getMessage());
        }
    }

    private void invokeCoffeeScriptCompiler(File input, File out) throws MojoExecutionException {
        new NPM.Execution(this).npm(COFFEE_SCRIPT_NPM_NAME).command(COFFEE_SCRIPT_COMMAND).withoutQuoting()
                .args("--compile", "--map", "--output", out.getAbsolutePath(),
                        input.getAbsolutePath()).execute();
    }

    @Override
    public boolean fileCreated(File file) throws WatchingException {
        compile(file);
        return true;
    }

    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        compile(file);
        return true;
    }

    @Override
    public boolean fileDeleted(File file) {
        File theFile = getOutputJSFile(file);
        if (theFile.exists()) {
            theFile.delete();
        }
        return true;
    }

}