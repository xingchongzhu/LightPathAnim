package com.path.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.animation.LinearInterpolator;

import com.path.androipathview.LinePathAnimView;
import com.path.anim.AnimatorBuilder;
import com.path.anim.AnimatorSetBuilder;
import com.path.anim.LineAnimHelper;
import com.path.anim.MarqueeAnimHelper;
import com.path.anim.SequentAnimHelper;


public class SecondActivity extends Activity {

    private LinePathAnimView pathAnimView;
    private LinePathAnimView mainPathAnimView;
    private LinePathAnimView rectPathAnimView;
    private LinePathAnimView fingerprintPathAnimView;
    private LinePathAnimView buildPathAnimView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        pathAnimView = findViewById(R.id.pathView);
        mainPathAnimView = findViewById(R.id.mianPathView);
        rectPathAnimView = findViewById(R.id.rect);
        fingerprintPathAnimView = findViewById(R.id.fingerprint);
        buildPathAnimView = findViewById(R.id.build);

        pathAnimView.setAnimatorBuilder(new AnimatorBuilder(pathAnimView));

        mainPathAnimView.setAnimatorBuilder(new AnimatorSetBuilder(mainPathAnimView));
        mainPathAnimView.setPathAnimHelper(new LineAnimHelper());

        rectPathAnimView.setPathAnimHelper(new MarqueeAnimHelper().interpolator(new LinearInterpolator()));

        //fingerprintPathAnimView.setAnimatorBuilder(new AnimatorBuilder(fingerprintPathAnimView));

        buildPathAnimView.setAnimatorBuilder(new AnimatorSetBuilder(buildPathAnimView));

        buildPathAnimView.setPathAnimHelper(new LineAnimHelper());
    }
}
