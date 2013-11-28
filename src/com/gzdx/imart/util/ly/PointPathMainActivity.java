package com.gzdx.imart.util.ly;

import java.util.Queue;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

/***
 * 
 * @author liuyin
 * @version 创建时间：Nov 23, 2011 11:10:50 AM
 * 类说明
 */
public class PointPathMainActivity extends Activity implements PointPathListener {
	
	private PointPathView pointPathView;

    private PointPathMainActivity mainWindow = this;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        pointPathView = (PointPathView) findViewById(R.id.pointPathView);
        final PointPathView f_pointPathView = pointPathView;
       
        f_pointPathView.initial(this);
        pointPathView.setConnectCrossingPoint(true);
       	f_pointPathView.invalidate();

    }

    public void onResume()
    {
        super.onResume();
        pointPathView.refreshPath();
        pointPathView.invalidate();
    }

	@Override
	public boolean onPathSelected(Queue<Integer> value) {
		// TODO Auto-generated method stub
		
		String temp = "";
		for (Integer x : value) {
			temp+=x + ",";
		} 
		Toast.makeText(mainWindow, temp, 
				Toast.LENGTH_SHORT).show();		
		
		if(value.size()<4)
		{
			pointPathView.showCorrect(false);
		}else
		{
			pointPathView.showCorrect(true);
		}
		//pointPathView.Enable(false);
		
		//pointPathView.showCorrect(false);
		
		return true;
	}
}