package test.lihongxin.towhere;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import test.lihongxin.towhere.CustomSpanView.CustomSpanView;

public class MainActivity extends AppCompatActivity {
    private CustomSpanView customSpanView;
    private ImageView mStartButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        customSpanView= (CustomSpanView) findViewById(R.id.id_spin);
        mStartButton= (ImageView) findViewById(R.id.id_start_btn);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!customSpanView.isStart()){
                    mStartButton.setImageResource(R.drawable.stop2);
                    customSpanView.spinStart();
                }
                else {
                    if (!customSpanView.isShouldEnd()){
                        mStartButton.setImageResource(R.drawable.start2);
                        customSpanView.spinEnd();

                    }
                }
            }
        });
    }
}
