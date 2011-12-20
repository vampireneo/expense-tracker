package com.vinsol.expensetracker;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.vinsol.expensetracker.utils.FileDelete;

public class TextEntry extends EditAbstract implements OnClickListener {

	private DatabaseAdapter mDatabaseAdapter;
	private Long userId;
	private Bundle intentExtras;
	private TextView dateBarDateview;
	private String dateViewString;
	private ArrayList<String> mEditList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.edit_page);

		mDatabaseAdapter = new DatabaseAdapter(this);
		findViewById(R.id.edit_date_bar).setBackgroundDrawable(getResources().getDrawable(R.drawable.date_bar_bg));
		dateBarDateview = (TextView) findViewById(R.id.edit_date_bar_dateview);

		// //////********* Get id from intent extras ******** ////////////

		intentExtras = getIntent().getBundleExtra("textEntryBundle");
		editHelper(intentExtras, R.string.text, R.string.finished_textentry, R.string.unfinished_textentry);
		getData();
		
		// ////// ******** Handle Date Bar ********* ////////
		if (intentExtras.containsKey("mDisplayList")) {
			new DateHandler(this, Long.parseLong(mEditList.get(6)));
		} else if (intentExtras.containsKey("timeInMillis")) {
			new DateHandler(this, intentExtras.getLong("timeInMillis"));
		} else {
			new DateHandler(this);
		}
		setClickListeners();
	}
	
	private void getData() {
		userId = getId();
		mEditList = getEditList();
		intentExtras = getIntentExtras();
	}

	@Override
	protected void onResume() {
		super.onResume();
		dateViewString = dateBarDateview.getText().toString();
	}

	private void setClickListeners() {
		// ////// ******* Adding Click Listeners to UI Items ******** //////////

		Button editSaveEntry = (Button) findViewById(R.id.edit_save_entry);
		editSaveEntry.setOnClickListener(this);

		Button editDelete = (Button) findViewById(R.id.edit_delete);
		editDelete.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		// //////******** Adding Action to save entry ********* ///////////

		if (v.getId() == R.id.edit_save_entry) {
			saveEntry();
		}

		// /////// ********* Adding action if delete button ********** /////////

		if (v.getId() == R.id.edit_delete) {
			new FileDelete(userId);

			// //// ******* Delete entry from database ******** /////////
			mDatabaseAdapter.open();
			mDatabaseAdapter.deleteDatabaseEntryID(Long.toString(userId));
			mDatabaseAdapter.close();
			if(intentExtras.containsKey("isFromShowPage")){
				Intent mIntent = new Intent(this, ShowTextActivity.class);
				ArrayList<String> listOnResult = new ArrayList<String>();
				listOnResult.add("");
				Bundle tempBundle = new Bundle();
				tempBundle.putStringArrayList("mDisplayList", listOnResult);
				mEditList = new ArrayList<String>();
				mEditList.addAll(listOnResult);
				mIntent.putExtra("textShowBundle", tempBundle);
				setResult(Activity.RESULT_CANCELED, mIntent);
			}
			finish();
		}
	}

	// /// ****************** Handling back press of key ********** ///////////
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			onBackPressed();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onBackPressed() {
		// This will be called either automatically for you on 2.0
		// or later, or by the code above on earlier versions of the platform.
		saveEntry();
		return;
	}

	private void saveEntry() {
		// ///// ******* Creating HashMap to update info ******* ////////
		HashMap<String, String> list = getSaveEntryData(dateBarDateview,dateViewString);
		// //// ******* Update database if user added additional info *******
		// ///////
		mDatabaseAdapter.open();
		mDatabaseAdapter.editDatabase(list);
		mDatabaseAdapter.close();

		if(!intentExtras.containsKey("isFromShowPage")){
			Intent intentExpenseListing = new Intent(this, ExpenseListing.class);
			Bundle mToHighLight = new Bundle();
			mToHighLight.putString("toHighLight", list.get(DatabaseAdapter.KEY_ID));
			intentExpenseListing.putExtras(mToHighLight);
			intentExpenseListing.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intentExpenseListing);
		} else {
			Intent mIntent = new Intent(this, ShowTextActivity.class);
			Bundle tempBundle = new Bundle();
			ArrayList<String> listOnResult = getListOnResult(list);
			getData();
			tempBundle.putStringArrayList("mDisplayList", listOnResult);
			mIntent.putExtra("textShowBundle", tempBundle);
			mIntent.putExtra("toHighLight", listOnResult.get(0));
			setResult(Activity.RESULT_OK, mIntent);
		}
		finish();
	}

}
