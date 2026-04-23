package com.mygdx.game;

import java.io.File;
import com.github.xpenatan.gdx.backends.teavm.config.AssetFileHandle;
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuildConfiguration;
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuilder;
import org.teavm.tooling.TeaVMTargetType;
import org.teavm.tooling.TeaVMTool;
import org.teavm.vm.TeaVMOptimizationLevel;

public class TeaVMBuilder {

    private static final boolean DEBUG = false;

    public static void main(String[] arguments) {
        TeaBuildConfiguration teaBuildConfiguration = new TeaBuildConfiguration();
        teaBuildConfiguration.assetsPath.add(new AssetFileHandle("../assets"));
        teaBuildConfiguration.webappPath = new File("build/dist").getAbsolutePath();
        teaBuildConfiguration.targetType = TeaVMTargetType.JAVASCRIPT;
        // Use the index.html shipped under teavm/webapp/ instead of the gdx-teavm default
        // so we can opt into a mobile-friendly viewport and a canvas that fills the screen.
        teaBuildConfiguration.useDefaultHtmlIndex = false;

        TeaBuilder.config(teaBuildConfiguration);
        TeaVMTool tool = new TeaVMTool();

        tool.setMainClass("com.mygdx.game.TeaVMLauncher");
        tool.setOptimizationLevel(DEBUG ? TeaVMOptimizationLevel.SIMPLE : TeaVMOptimizationLevel.ADVANCED);
        tool.setObfuscated(!DEBUG);

        if (DEBUG) {
            tool.setDebugInformationGenerated(true);
            tool.setSourceMapsFileGenerated(true);
        }

        TeaBuilder.build(tool);
    }
}
