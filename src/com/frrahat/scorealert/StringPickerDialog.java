package com.frrahat.scorealert;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class StringPickerDialog extends DialogFragment {
	
	private EditText stringEditText;
	private TextInputListener inputListener;
	private String prevText;
	
	@SuppressLint("InflateParams")
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View pickerView=inflater.inflate(R.layout.dialog_string_picker, null);
        stringEditText=(EditText) pickerView.findViewById(R.id.stringEditText);
        
        if(prevText!=null){
        	//setting fileName name for quick input
	        stringEditText.setText(prevText);
			
			/*int cursorIndex=prevText.lastIndexOf(".");
			if(cursorIndex>=0)
				stringEditText.setSelection(cursorIndex);*/
        }
        /*else{
        	stringEditText.setText(".txt");
        }*/
        
        builder.setView(pickerView)
        // Add action buttons
               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                	   if(inputListener!=null)
                	   inputListener.inputGiven(stringEditText.getText().toString());
                   }
               })
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       StringPickerDialog.this.getDialog().cancel();
                   }
               });   
              
        return builder.create();
    }
	
	public void setPrevText(String prevText)
	{
		this.prevText=prevText;
	}
	
	public EditText getFocusingEditText(){
		return stringEditText;
	}
	
	public void setInputListener(TextInputListener listener)
	{
		this.inputListener=listener;
	}
}
