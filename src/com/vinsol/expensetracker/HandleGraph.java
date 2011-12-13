package com.vinsol.expensetracker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.vinsol.android.graph.BarGraph;
import com.vinsol.expensetracker.utils.DisplayDate;

public class HandleGraph extends AsyncTask<Void, Void, Void> implements OnClickListener{

	private List<HashMap<String, String>> mDataDateListGraph;
	private ConvertCursorToListString mConvertCursorToListString;
	static private Context mContext;
	private List<HashMap<String, String>> mSubList;
	private ArrayList<ArrayList<ArrayList<String>>> mGraphList;
	private Calendar lastDateCalendar;
	private Activity activity;
	private int j = 0;
	private RelativeLayout main_graph ;
	private ImageView main_graph_previous_arrow ;
	private ImageView main_graph_next_arrow ;
	private RelativeLayout.LayoutParams params ;
	static private BarGraph barGraph;
	static private TextView graph_no_item;
	private TextView main_graph_header_textview;
	
	public HandleGraph(Context _context) {
		mContext = _context;
		activity = (mContext instanceof Activity) ? (Activity) mContext : null;
		lastDateCalendar = Calendar.getInstance();
		lastDateCalendar.setFirstDayOfWeek(Calendar.MONDAY);
		main_graph = (RelativeLayout) activity.findViewById(R.id.main_graph);
		main_graph_previous_arrow = (ImageView) activity.findViewById(R.id.main_graph_previous_arrow);
		main_graph_next_arrow = (ImageView) activity.findViewById(R.id.main_graph_next_arrow);
		params = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,main_graph.getBackground().getIntrinsicHeight());
		main_graph_header_textview = (TextView) activity.findViewById(R.id.main_graph_header_textview);
	}
	
	@Override
	protected void onPreExecute() {
		main_graph_previous_arrow.setVisibility(View.INVISIBLE);
		main_graph_next_arrow.setVisibility(View.INVISIBLE);
		main_graph.removeView(barGraph);
		main_graph.removeView(graph_no_item);
		super.onPreExecute();
	}
	
	@Override
	protected Void doInBackground(Void... arg0) {
		
		mConvertCursorToListString = new ConvertCursorToListString(mContext);
		mDataDateListGraph = mConvertCursorToListString.getDateListStringGraph();
		mSubList = mConvertCursorToListString.getListStringParticularDate();
		if (mDataDateListGraph.size() >= 1) {
			lastDateCalendar.setTimeInMillis(Long.parseLong(mSubList.get(mSubList.size()-1).get(DatabaseAdapter.KEY_DATE_TIME+"Millis")));
			lastDateCalendar.setFirstDayOfWeek(Calendar.MONDAY);
			mGraphList = getGraphList();
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		//view of graph
		// ******start view******//

		main_graph_previous_arrow.setVisibility(View.VISIBLE);
		main_graph_next_arrow.setVisibility(View.VISIBLE);
		if(mGraphList != null) {
			if(mGraphList.size() >= 1 ) {
				barGraph = new BarGraph(mContext,mGraphList.get(j).get(1),mGraphList.get(j).get(2));
				main_graph.addView(barGraph, params);
				if(j == mGraphList.size()-1){
					main_graph_next_arrow.setVisibility(View.INVISIBLE);
				}
				if(j == 0){
					if(!isNotNullAll(mGraphList.get(j).get(0))){
						main_graph.removeView(barGraph);
						graphNoItem();
						main_graph.addView(graph_no_item);
					}
					main_graph_previous_arrow.setVisibility(View.INVISIBLE);
				}
				main_graph_next_arrow.setOnClickListener(this);
				main_graph_previous_arrow.setOnClickListener(this);
				main_graph_header_textview.setText(mGraphList.get(j).get(3).get(0));
			}
			
		} else {
			graphNoItem();
			main_graph.addView(graph_no_item);
			main_graph_next_arrow.setVisibility(View.INVISIBLE);
			main_graph_previous_arrow.setVisibility(View.INVISIBLE);
			main_graph_header_textview.setText("");
		}
		super.onPostExecute(result);
	}
	
	private void graphNoItem(){
		graph_no_item = new TextView(mContext);
		graph_no_item.setGravity(Gravity.CENTER);
		graph_no_item.setText("No Items to Show");
		LayoutParams textParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, main_graph.getBackground().getIntrinsicHeight());
		graph_no_item.setTextColor(Color.BLACK);
		graph_no_item.setPadding(0, 0, 0, 15);
		graph_no_item.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
		graph_no_item.setLayoutParams(textParams);
	}
	
	private ArrayList<ArrayList<ArrayList<String>>> getGraphList(){
		DisplayDate lastDateDisplayDate = new DisplayDate(lastDateCalendar);
		ArrayList<ArrayList<ArrayList<String>>> graphList = new ArrayList<ArrayList<ArrayList<String>>>();
		Calendar mTempCalender = Calendar.getInstance();
		mTempCalender.set(mTempCalender.get(Calendar.YEAR), mTempCalender.get(Calendar.MONTH), mTempCalender.get(Calendar.DAY_OF_MONTH
				),0,0,0);
		mTempCalender.setFirstDayOfWeek(Calendar.MONDAY);
		
		int j = 0;
		ArrayList<ArrayList<String>> subGraphList = new ArrayList<ArrayList<String>>();
		ArrayList<String> mArrayIDList = new ArrayList<String>();
		ArrayList<String> mArrayValues = new ArrayList<String>();
		ArrayList<String> mArrayHorLabels = new ArrayList<String>();
		List<List<String>> mList = getDateIDList();
		while(lastDateCalendar.before(mTempCalender) || lastDateDisplayDate.getDisplayDateGraph().equals(new DisplayDate(mTempCalender).getDisplayDateGraph())){
			DisplayDate mDisplayDate = new DisplayDate(mTempCalender);
			while(mDisplayDate.isCurrentWeek()){
				String toCheckGraphDate = mDisplayDate.getDisplayDateHeaderGraph();
				if(j < mList.size()) {
					if(mList.get(j).get(2).equals(mDisplayDate.getDisplayDateGraph())){
						mArrayIDList.add(mList.get(j).get(0));
						mArrayValues.add(mList.get(j).get(1));
						mArrayHorLabels.add(getWeekDay(mTempCalender.get(Calendar.DAY_OF_WEEK)));
						j++;
					} else {
						mArrayIDList.add(null);
						mArrayValues.add(null);
						mArrayHorLabels.add(getWeekDay(mTempCalender.get(Calendar.DAY_OF_WEEK)));
					}
				} else {
					mArrayIDList.add(null);
					mArrayValues.add(null);
					mArrayHorLabels.add(getWeekDay(mTempCalender.get(Calendar.DAY_OF_WEEK)));
				}
				mTempCalender.add(Calendar.DATE, -1);
				mDisplayDate = new DisplayDate(mTempCalender);
				if(!mDisplayDate.isCurrentWeek()){
					if(mArrayIDList.size() >= 1){
						Collections.reverse(mArrayIDList);
						Collections.reverse(mArrayValues);
						Collections.reverse(mArrayHorLabels);
						subGraphList.add(mArrayIDList);
						subGraphList.add(mArrayValues);
						subGraphList.add(mArrayHorLabels);
						ArrayList<String> displayDate = new ArrayList<String>();
						displayDate.add(toCheckGraphDate);
						subGraphList.add(displayDate);
						graphList.add(subGraphList);
						mArrayIDList = new ArrayList<String>();
						mArrayValues = new ArrayList<String>();
						mArrayHorLabels = new ArrayList<String>();
						subGraphList = new ArrayList<ArrayList<String>>();
					}
				}
			} 

			if(!mDisplayDate.isCurrentWeek() && mDisplayDate.isCurrentMonth()){
				
				String toCheckGraphDate = mDisplayDate.getDisplayDateHeaderGraph();
				while(mDisplayDate.getDisplayDateHeaderGraph().equals(toCheckGraphDate)){
					if(j < mList.size()) {
						if(mList.get(j).get(2).equals(mDisplayDate.getDisplayDateGraph())){
							mArrayIDList.add(mList.get(j).get(0));
							mArrayValues.add(mList.get(j).get(1));
							mArrayHorLabels.add(getWeekDay(mTempCalender.get(Calendar.DAY_OF_WEEK)));
							j++;
						} else {
							mArrayIDList.add(null);
							mArrayValues.add(null);
							mArrayHorLabels.add(getWeekDay(mTempCalender.get(Calendar.DAY_OF_WEEK)));
						}
					} else {
						mArrayIDList.add(null);
						mArrayValues.add(null);
						mArrayHorLabels.add(getWeekDay(mTempCalender.get(Calendar.DAY_OF_WEEK)));
					}
					mTempCalender.add(Calendar.DATE, -1);
					mDisplayDate = new DisplayDate(mTempCalender);
					if(!mDisplayDate.getDisplayDateHeaderGraph().equals(toCheckGraphDate)){
						if(mArrayIDList.size() >= 1){
							Collections.reverse(mArrayIDList);
							Collections.reverse(mArrayValues);
							Collections.reverse(mArrayHorLabels);
							subGraphList.add(mArrayIDList);
							subGraphList.add(mArrayValues);
							subGraphList.add(mArrayHorLabels);
							ArrayList<String> displayDate = new ArrayList<String>();
							displayDate.add(toCheckGraphDate);
							subGraphList.add(displayDate);
							if(isNotNullAll(mArrayIDList))
								graphList.add(subGraphList);
							mArrayIDList = new ArrayList<String>();
							mArrayValues = new ArrayList<String>();
							mArrayHorLabels = new ArrayList<String>();
							subGraphList = new ArrayList<ArrayList<String>>();
						}
					}
				}
			}
			
			
			if(mDisplayDate.isPrevMonths() || mDisplayDate.isPrevYears()){
				
				mTempCalender.set(Calendar.DAY_OF_MONTH, mTempCalender.getActualMaximum(Calendar.DAY_OF_MONTH));
				mTempCalender.setFirstDayOfWeek(Calendar.MONDAY);
				mDisplayDate = new DisplayDate(mTempCalender);
				String toCheckGraphDate = mDisplayDate.getDisplayDateHeaderGraph();
				while(mDisplayDate.getDisplayDateHeaderGraph().equals(toCheckGraphDate)){
					if(j < mList.size()) {
						if(mList.get(j).get(2).equals(mDisplayDate.getDisplayDateGraph())){
							mArrayIDList.add(mList.get(j).get(0));
							mArrayValues.add(mList.get(j).get(1));
							mArrayHorLabels.add("W "+mTempCalender.get(Calendar.WEEK_OF_MONTH));
							j++;
						} else {
							mArrayIDList.add(null);
							mArrayValues.add(null);
							mArrayHorLabels.add("W "+mTempCalender.get(Calendar.WEEK_OF_MONTH));
						}
					} else {
						mArrayIDList.add(null);
						mArrayValues.add(null);
						mArrayHorLabels.add("W "+mTempCalender.get(Calendar.WEEK_OF_MONTH));
					}
					mTempCalender.add(Calendar.WEEK_OF_MONTH, -1);
					mTempCalender.set(Calendar.DAY_OF_WEEK, mTempCalender.getActualMinimum(Calendar.DAY_OF_WEEK));
					mTempCalender.setFirstDayOfWeek(Calendar.MONDAY);
					mDisplayDate = new DisplayDate(mTempCalender);
					if(!mDisplayDate.getDisplayDateHeaderGraph().equals(toCheckGraphDate)){
						if(mArrayIDList.size() >= 1){
							Collections.reverse(mArrayIDList);
							Collections.reverse(mArrayValues);
							Collections.reverse(mArrayHorLabels);
							subGraphList.add(mArrayIDList);
							subGraphList.add(mArrayValues);
							subGraphList.add(mArrayHorLabels);
							ArrayList<String> displayDate = new ArrayList<String>();
							displayDate.add(toCheckGraphDate);
							subGraphList.add(displayDate);
							if(isNotNullAll(mArrayIDList))
								graphList.add(subGraphList);
							mArrayIDList = new ArrayList<String>();
							mArrayValues = new ArrayList<String>();
							mArrayHorLabels = new ArrayList<String>();
							subGraphList = new ArrayList<ArrayList<String>>();
						}
					}
				}
			}
			
		}
		return graphList;
	}

	private boolean isNotNullAll(ArrayList<String> mArrayIDList) {
		for(int i = 0;i<mArrayIDList.size();i++){
			if(mArrayIDList.get(i) != null){
				return true;
			}
		}
		return false;
	}

	private List<List<String>> getDateIDList() {
		List<List<String>> listString = new ArrayList<List<String>>();
		for(int i = 0 ;i < mSubList.size(); ) {
			ArrayList<String> mList = new ArrayList<String>();
			String tempDisplayDate = mSubList.get(i).get(DatabaseAdapter.KEY_DATE_TIME+"Millis");
			Calendar mTCalendar = Calendar.getInstance();
			mTCalendar.setTimeInMillis(Long.parseLong(tempDisplayDate));
			mTCalendar.setFirstDayOfWeek(Calendar.MONDAY);
			String tempDisplayDateGraph = new DisplayDate(mTCalendar).getDisplayDateGraph();
			String idList = "";
			Double temptotalAmount = 0.0;
			String totalAmountString = null;
			boolean isTempAmountNull = false;
			while(new DisplayDate(mTCalendar).getDisplayDateGraph().equals(tempDisplayDateGraph)) {
				String tempAmount = mSubList.get(i).get(DatabaseAdapter.KEY_AMOUNT);
				if (tempAmount != null && !tempAmount.equals("")) {
					try {
						temptotalAmount += Double.parseDouble(tempAmount);
					} catch (NumberFormatException e) {
					}
				} else {
					isTempAmountNull = true;
				}
				idList = idList+mSubList.get(i).get(DatabaseAdapter.KEY_ID)+",";
				i++;
				
				if (i < mSubList.size()) {
					mTCalendar.setTimeInMillis(Long.parseLong(mSubList.get(i).get(DatabaseAdapter.KEY_DATE_TIME+"Millis")));
					mTCalendar.setFirstDayOfWeek(Calendar.MONDAY);
				} else {
					break;
				}
			}
			if (isTempAmountNull) {
				if (temptotalAmount != 0) {
					totalAmountString = temptotalAmount + " ?";
				} else {
					totalAmountString = "?";
				}
			} else {
				totalAmountString = temptotalAmount + "";
			}
			mList.add(idList);
			mList.add(totalAmountString);
			mList.add(tempDisplayDateGraph);
			listString.add(mList);
			
		}
		return listString;
	}

	private String getWeekDay(int i) {
		switch(i){
		case Calendar.MONDAY:
			return "Mon";
		case Calendar.TUESDAY:
			return "Tue";
		case Calendar.WEDNESDAY:
			return "Wed";
		case Calendar.THURSDAY:
			return "Thu";
		case Calendar.FRIDAY:
			return "Fri";
		case Calendar.SATURDAY:
			return "Sat";
		case Calendar.SUNDAY:
			return "Sun";
		}
		return null;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.main_graph_next_arrow:
			j++;
			break;

		case R.id.main_graph_previous_arrow:
			j--;
			break;	
			
		default:
			break;
		}
		
		main_graph.removeView(graph_no_item);
		main_graph.removeView(barGraph);
		if(mGraphList != null) {
			if(mGraphList.size() >= 1 ) {
				barGraph = new BarGraph(mContext,mGraphList.get(j).get(1),mGraphList.get(j).get(2));
				main_graph.addView(barGraph, params);
				main_graph_previous_arrow.setVisibility(View.VISIBLE);
				main_graph_next_arrow.setVisibility(View.VISIBLE);
				
				if(j == mGraphList.size()-1){
					main_graph_next_arrow.setVisibility(View.INVISIBLE);
				}
				if(j == 0){
					if(!isNotNullAll(mGraphList.get(j).get(0))){
						main_graph.removeView(barGraph);
						graphNoItem();
						main_graph.addView(graph_no_item);
					}
					main_graph_previous_arrow.setVisibility(View.INVISIBLE);
				}

				main_graph_next_arrow.setOnClickListener(this);
				main_graph_previous_arrow.setOnClickListener(this);
				TextView main_graph_header_textview = (TextView) activity.findViewById(R.id.main_graph_header_textview);
				main_graph_header_textview.setText(mGraphList.get(j).get(3).get(0));
			}
		} 
	}

}
