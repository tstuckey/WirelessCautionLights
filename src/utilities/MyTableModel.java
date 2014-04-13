package utilities;

import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import db_info.DB_Calls;
/**
 * MyTableModel extends the DefaultTableModel; this is the place to do customizations
 * to the behavior of the underlying model; for example, making rows, cols, or individual cells
 * editable, etc.
 * @author Tom Stuckey
 *
 */
public class MyTableModel extends DefaultTableModel{

	public MyTableModel(){
		
	}

	@SuppressWarnings("unchecked")
	/**This method overrides the DefaultTableModel method.  When the user
	 * changes a cell value, it prompts the user to see if the user is sure
	 * they want to change the cell.  If they do, it makes a call to update the database
	 * with the new description.
	 * 
	 */
	public void setValueAt(Object aValue, int row, int column){
		String result=verifyDescriptionChange();
		if (result.equals("single yes")){
	        Vector rowVector = (Vector)dataVector.elementAt(row);
	        rowVector.setElementAt(aValue, column);
	        fireTableCellUpdated(row, column);
	        doTrackDescriptionUpdate((String)rowVector.elementAt(0), (String)aValue);
		}else{
			
		}
	}

	public boolean isCellEditable(int row, int col){
		boolean result;
		if (col==0){
			//column 0 contains the database row identifier
			//we don't want the users to be able to modify this value
			result=false;
		}else{
			result=true;	
		}
		return result;
	}

	private void doTrackDescriptionUpdate(String track_id, String description){
		DB_Calls.updateTrackDescription(Integer.valueOf(track_id), description);
	}
	
    private String verifyDescriptionChange() {
		String response="cancel";
    	Object usr_options[] = { "Yes","Cancel" };
		Integer usr_response=JOptionPane.showOptionDialog(null, null, "Change Description? ",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, usr_options,
				usr_options[0]);
		
		if (usr_response==0){
			response="single yes";
		}

		if (usr_response==1){
			response="cancel";
		}

		return response;
    } 
	
	
}
