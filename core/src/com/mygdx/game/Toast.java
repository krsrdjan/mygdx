package com.mygdx.game;

public class Toast {
    private String message;
    private float timeRemaining;
    private static final float DURATION = 3.0f; // 3 seconds

    public Toast(String message) {
        this.message = message;
        this.timeRemaining = DURATION;
    }

    public void update(float deltaTime) {
        timeRemaining -= deltaTime;
    }

    public boolean isExpired() {
        return timeRemaining <= 0;
    }

    public String getMessage() {
        return message;
    }

    public float getTimeRemaining() {
        return timeRemaining;
    }

    public float getAlpha() {
        // Fade out in the last 0.5 seconds
        if (timeRemaining < 0.5f) {
            return timeRemaining / 0.5f;
        }
        return 1.0f;
    }
}

