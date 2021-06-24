package com.path.test;

import android.app.Activity;
import android.os.Bundle;

import com.path.androipathview.LinePathAnimView;
import com.path.anim.AnimatorBuilder;
import com.path.anim.AnimatorSetBuilder;
import com.path.anim.LineAnimHelper;


public class SecondActivity extends Activity {

    private LinePathAnimView pathAnimView;
    private LinePathAnimView mainPathAnimView;
    private LinePathAnimView rectPathAnimView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        pathAnimView = findViewById(R.id.pathView);
        mainPathAnimView = findViewById(R.id.mianPathView);
        rectPathAnimView = findViewById(R.id.rect);

        pathAnimView.setAnimatorBuilder(new AnimatorBuilder(pathAnimView));
        //pathAnimView.setAnimatorBuilder(new AnimatorSetBuilder(pathAnimView));
        //pathAnimView.setPathAnimHelper(new LineAnimHelper());
        //pathAnimView.setPathAnimHelper(new SequentAnimHelper());
        mainPathAnimView.setAnimatorBuilder(new AnimatorSetBuilder(pathAnimView));
        mainPathAnimView.setPathAnimHelper(new LineAnimHelper());


        rectPathAnimView.setAnimatorBuilder(new AnimatorSetBuilder(pathAnimView));
        //rectPathAnimView.setPathAnimHelper(new SequentAnimHelper());
    }
}
