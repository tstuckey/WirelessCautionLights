package utilities;

import java.awt.event.KeyAdapter;

import base.Active_LightNodes;
import base.BackgroundFrame;

public class MyKeyAdapter extends KeyAdapter{
	BackgroundFrame bf;
	public MyKeyAdapter(BackgroundFrame bf){
		this.bf=bf;
	}
	
	public void keyTyped(java.awt.event.KeyEvent evt) {
				
		if (evt.getKeyChar()==' '){
			//if it is a space key event{space}
			//invoke the fullCourseButton
			//System.out.println("MyKeyAdapter: invoking full course with "+evt.getKeyChar());
			bf.fullCourseButtonPerformed(null);//invoking the full course caution when the space bar is pressed
		}else{
			//System.out.println("MyKeyAdapter: invoking subset of course with "+evt.getKeyChar());
			Active_LightNodes.invokeLightButtonActionEventAcrossAffectedNodes(evt.getKeyChar()+"");			
		}
	}
}
