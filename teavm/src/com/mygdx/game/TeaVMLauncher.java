package com.mygdx.game;

import com.github.xpenatan.gdx.backends.teavm.TeaApplicationConfiguration;
import com.github.xpenatan.gdx.backends.teavm.TeaApplication;
import org.teavm.jso.JSBody;

public class TeaVMLauncher {

    @JSBody(script = "return typeof window.__MYGDX_MOBILE_WEB__ !== 'undefined' && window.__MYGDX_MOBILE_WEB__ === true;")
    private static native boolean isMobileWebPlay();

    public static void main(String[] args) {
        TeaApplicationConfiguration config = new TeaApplicationConfiguration("canvas");
        // 0 = let the canvas grow to fill its CSS box (the full browser viewport, see webapp/index.html).
        config.width = 0;
        config.height = 0;
        config.padHorizontal = 0;
        config.padVertical = 0;
        config.usePhysicalPixels = true;
        new TeaApplication(new MyGdxGame(isMobileWebPlay()), config);
    }
}
