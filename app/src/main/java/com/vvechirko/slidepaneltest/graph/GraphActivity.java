package com.vvechirko.slidepaneltest.graph;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.Nullable;
import com.vvechirko.slidepaneltest.R;

public class GraphActivity extends Activity {

    private InteractiveLineGraphView mGraphView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        mGraphView = findViewById(R.id.chart);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.graph, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_zoom_in:
                mGraphView.zoomIn();
                return true;
            case R.id.action_zoom_out:
                mGraphView.zoomOut();
                return true;
            case R.id.action_pan_left:
                mGraphView.panLeft();
                return true;
            case R.id.action_pan_right:
                mGraphView.panRight();
                return true;
            case R.id.action_pan_up:
                mGraphView.panUp();
                return true;
            case R.id.action_pan_down:
                mGraphView.panDown();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
