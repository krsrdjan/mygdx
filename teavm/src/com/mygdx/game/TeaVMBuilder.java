package com.mygdx.game;

import java.io.File;
import com.github.xpenatan.gdx.teavm.backends.shared.config.AssetFileHandle;
import com.github.xpenatan.gdx.teavm.backends.shared.config.builder.TeaBuilder;
import com.github.xpenatan.gdx.teavm.backends.web.config.backend.WebBackend;
import org.teavm.vm.TeaVMOptimizationLevel;

public class TeaVMBuilder {

    private static final boolean DEBUG = false;

    public static void main(String[] arguments) {
        WebBackend backend = new WebBackend()
                .setGenerateIndexHtml(false);

        TeaBuilder builder = new TeaBuilder(backend)
                .addAssets(new AssetFileHandle("../assets"))
                .setMainClass("com.mygdx.game.TeaVMLauncher")
                .setOutputName("app")
                .setOptimizationLevel(DEBUG ? TeaVMOptimizationLevel.SIMPLE : TeaVMOptimizationLevel.ADVANCED)
                .setObfuscated(!DEBUG);

        if (DEBUG) {
            builder.setDebugInformationGenerated(true);
            builder.setSourceMapsFileGenerated(true);
        }

        // Outputs to build/dist/webapp/ (app.js + assets); custom index.html comes from teavm/webapp/.
        builder.build(new File("build/dist"));
    }
}
