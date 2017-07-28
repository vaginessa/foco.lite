package io.github.nfdz.foco.utils;


import android.content.Intent;

public class AnimationUtils {

    public static void addTransitionFlags(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    }

}

