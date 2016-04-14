/**
 * 
 */
package com.jozibear247_cab.driver;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hb.views.PinnedSectionListView;
import com.jozibear247_cab.driver.adapter.HistoryAdapter;
import com.jozibear247_cab.driver.base.ActionBarBaseActivitiy;
import com.jozibear247_cab.driver.model.History;
import com.jozibear247_cab.driver.parse.AsyncTaskCompleteListener;
import com.jozibear247_cab.driver.parse.HttpRequester;
import com.jozibear247_cab.driver.parse.ParseContent;
import com.jozibear247_cab.driver.utills.AndyConstants;
import com.jozibear247_cab.driver.utills.AndyUtils;
import com.jozibear247_cab.driver.utills.AppLog;
import com.jozibear247_cab.driver.utills.PreferenceHelper;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * @author Kishan H Dhamat
 * 
 */
public class HistoryActivity extends ActionBarBaseActivitiy implements
		OnItemClickListener, AsyncTaskCompleteListener {

	private HistoryAdapter historyAdapter;
	private ArrayList<History> historyList;
//	private PreferenceHelper preferenceHelper;
	private ParseContent parseContent;
	private ImageView tvEmptyHistory;
	private TreeSet<Integer> mSeparatorsSet = new TreeSet<Integer>();
	private PinnedSectionListView lvHistory;
	private ArrayList<Date> dateList = new ArrayList<Date>();
	private ArrayList<History> historyListOrg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		// getSupportActionBar().setTitle(getString(R.string.text_history));
		// getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// getSupportActionBar().setHomeButtonEnabled(true);
		lvHistory = (PinnedSectionListView) findViewById(R.id.lvHistory);
		tvEmptyHistory = (ImageView) findViewById(R.id.tvHistoryEmpty);
		lvHistory.setOnItemClickListener(this);
		historyList = new ArrayList<History>();
//		preferenceHelper = new PreferenceHelper(this);
		dateList = new ArrayList<Date>();
		parseContent = new ParseContent(this);
		historyListOrg = new ArrayList<History>();
		setActionBarTitle(getString(R.string.text_history));
		setActionBarIcon(R.drawable.ub__nav_history);
		getHistory();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void getHistory() {
		if (!AndyUtils.isNetworkAvailable(this)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet), this);
			return;
		}
		AndyUtils.showCustomProgressDialog(this, "",
				getResources().getString(R.string.progress_getting_history),
				false);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL,
				AndyConstants.ServiceType.HISTORY + AndyConstants.Params.ID
						+ "=" + PreferenceHelper.getInstance(this).getUserId() + "&"
						+ AndyConstants.Params.TOKEN + "="
						+ PreferenceHelper.getInstance(this).getSessionToken());
		new HttpRequester(this, map, AndyConstants.ServiceCode.HISTORY, true,
				this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		if (mSeparatorsSet.contains(position))
			return;
		final Dialog mDialog = new Dialog(this,
				android.R.style.Theme_Translucent_NoTitleBar);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		mDialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		mDialog.setContentView(R.layout.bill_layout);

		DecimalFormat decimalFormat = new DecimalFormat("0.00");
		DecimalFormat perHourFormat = new DecimalFormat("0.0");
		History history = historyListOrg.get(position);
		String currency = history.getCurrency();
		String basePrice = String.valueOf(decimalFormat.format(Double
				.parseDouble(history.getBasePrice())));
//		String basePricetmp = String.valueOf(decimalFormat.format(Double
//				.parseDouble(basePrice)));
		String totalTmp = String.valueOf(decimalFormat.format(Double
				.parseDouble(history.getTotal())));
		String distCostTmp = String.valueOf(decimalFormat.format(Double
				.parseDouble(history.getDistanceCost())));
		String timeCost = String.valueOf(decimalFormat.format(Double
				.parseDouble(history.getTimecost())));
		String primary_amount = String.valueOf(decimalFormat.format(Double
				.parseDouble(history.getPrimary_amount())));
		String secoundry_amount = String.valueOf(decimalFormat.format(Double
				.parseDouble(history.getSecoundry_amount())));
		String discounts = String.valueOf(decimalFormat.format(Math.abs((Double
				.parseDouble(history.getPrimary_amount()) + Double
				.parseDouble(history.getSecoundry_amount()))
				- (Double.parseDouble(history.getTotal())))));

		((TextView) mDialog.findViewById(R.id.tvBasePrice)).setText(currency
				+ " " + basePrice);
		((TextView) mDialog.findViewById(R.id.tvBillDistancePerMile))
				.setText(currency + " "
						+ String.valueOf(decimalFormat.format(Double
						.parseDouble(history.getPricePerUnitDistance())))
						+ " "
						+ getResources().getString(
						R.string.text_cost_per_km));
		((TextView) mDialog.findViewById(R.id.tvBillTimePerHour))
				.setText(currency + " "
						+ String.valueOf(decimalFormat.format(Double
						.parseDouble(history.getPricePerUnitTime())))
						+ " "
						+ getResources().getString(
						R.string.text_cost_per_min));

		((TextView) mDialog.findViewById(R.id.adminCost))
				.setText(getResources().getString(R.string.text_cost_for_admin)
						+ " :    " + currency + " " + secoundry_amount);

		((TextView) mDialog.findViewById(R.id.providercost))
				.setText(getResources().getString(
						R.string.text_cost_for_provider)
						+ " : " + currency + " " + primary_amount);

		((TextView) mDialog.findViewById(R.id.discounts))
				.setText(getResources().getString(R.string.text_discount)
						+ " :     " + currency + " " + discounts);

		((TextView) mDialog.findViewById(R.id.tvDis1)).setText(currency + " " + distCostTmp);
		((TextView) mDialog.findViewById(R.id.tvTime1)).setText(currency + " " + timeCost);
		((TextView) mDialog.findViewById(R.id.tvTotal1)).setText(currency + " " + totalTmp);

//		Button btnCard = (Button) mDialog.findViewById(R.id.btnBillCard);
//		btnCard.setVisibility(View.GONE);
		Button btnNotPaid = (Button) mDialog.findViewById(R.id.btnBillNotPaid);
		btnNotPaid.setVisibility(View.GONE);

		Button btnConfirm = (Button) mDialog.findViewById(R.id.btnBillCash);
		btnConfirm.setText("CLOSE");
		btnConfirm.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mDialog.dismiss();
			}
		});
		mDialog.setCancelable(true);
		mDialog.show();
	}

	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		AndyUtils.removeCustomProgressDialog();
		switch (serviceCode) {
		case AndyConstants.ServiceCode.HISTORY:
			AppLog.Log("TAG", "History Response :" + response);
			Log.d("mahi", "history done" + response);
			if (!parseContent.isSuccess(response)) {
				return;
			}
			historyListOrg.clear();
			historyList.clear();
			dateList.clear();
			parseContent.parseHistory(response, historyList);

			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				final Calendar cal = Calendar.getInstance();

				parseContent.parseHistory(response, historyList);
				Collections.sort(historyList, new Comparator<History>() {
					@Override
					public int compare(History o1, History o2) {

						SimpleDateFormat dateFormat = new SimpleDateFormat(
								"yyyy-MM-dd hh:mm:ss");
						try {
							// date1 = dateFormat.parse(o1.getDate());
							// date2 = dateFormat.parse(o2.getDate());
							String firstStrDate = o1.getDate();
							String secondStrDate = o2.getDate();

							Log.i("firstStrDate 1", "" + firstStrDate);
							Log.i("secondStrDate 2", "" + secondStrDate);
							Date date2 = dateFormat.parse(secondStrDate);
							Date date1 = dateFormat.parse(firstStrDate);
							Log.i("Date 1", "" + date1);
							Log.i("Date 2", "" + date2);
							int value = date2.compareTo(date1);
							Log.i("Value", "" + value);
							return value;
						} catch (ParseException e) {
							e.printStackTrace();
						}
						return 0;
					}
				});
				HashSet<Date> listToSet = new HashSet<Date>();
				for (int i = 0; i < historyList.size(); i++) {
					AppLog.Log("date", historyList.get(i).getDate() + "");
					if (listToSet.add(sdf.parse(historyList.get(i).getDate()))) {
						dateList.add(sdf.parse(historyList.get(i).getDate()));
					}
				}

				for (int i = 0; i < dateList.size(); i++) {
					cal.setTime(dateList.get(i));
					History item = new History();
					item.setDate(sdf.format(dateList.get(i)));
					historyListOrg.add(item);
					mSeparatorsSet.add(historyListOrg.size() - 1);
					for (int j = 0; j < historyList.size(); j++) {
						Calendar messageTime = Calendar.getInstance();
						messageTime.setTime(sdf.parse(historyList.get(j)
								.getDate()));
						if (cal.getTime().compareTo(messageTime.getTime()) == 0) {
							historyListOrg.add(historyList.get(j));
						}
					}
				}
				if (historyList.size() > 0) {
					lvHistory.setVisibility(View.VISIBLE);
					tvEmptyHistory.setVisibility(View.GONE);
				} else {
					lvHistory.setVisibility(View.GONE);
					tvEmptyHistory.setVisibility(View.VISIBLE);
				}
				historyAdapter = new HistoryAdapter(this, historyListOrg,
						mSeparatorsSet);
				lvHistory.setAdapter(historyAdapter);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btnActionNotification:
			onBackPressed();
			overridePendingTransition(R.anim.slide_in_left,
					R.anim.slide_out_right);
			break;

		default:
			break;
		}
	}
}
