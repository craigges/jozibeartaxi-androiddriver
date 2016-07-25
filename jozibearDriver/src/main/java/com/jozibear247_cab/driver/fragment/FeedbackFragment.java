package com.jozibear247_cab.driver.fragment;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.jozibear247_cab.driver.R;
import com.jozibear247_cab.driver.base.BaseMapFragment;
import com.jozibear247_cab.driver.model.Bill;
import com.jozibear247_cab.driver.model.RequestDetail;
import com.jozibear247_cab.driver.parse.AsyncTaskCompleteListener;
import com.jozibear247_cab.driver.parse.HttpRequester;
import com.jozibear247_cab.driver.utills.AndyConstants;
import com.jozibear247_cab.driver.utills.AndyUtils;
import com.jozibear247_cab.driver.utills.AppLog;
import com.jozibear247_cab.driver.utills.PreferenceHelper;
import com.jozibear247_cab.driver.widget.MyFontEditTextView;
import com.jozibear247_cab.driver.widget.MyFontTextView;

import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * @author Kishan H Dhamat
 * 
 */
public class FeedbackFragment extends BaseMapFragment implements
		AsyncTaskCompleteListener {

	private MyFontEditTextView etFeedbackComment;
	private ImageView ivDriverImage;
	private RatingBar ratingFeedback;
	private MyFontTextView tvTime, tvDistance, tvClientName;

	private final String TAG = "FeedbackFragment";
	private AQuery aQuery;
	private Dialog m_invoiceDlg;
	private boolean m_bNotPaid;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View feedbackFragmentView = inflater.inflate(
				R.layout.fragment_feedback, container, false);

		etFeedbackComment = (MyFontEditTextView) feedbackFragmentView
				.findViewById(R.id.etFeedbackComment);
		tvTime = (MyFontTextView) feedbackFragmentView
				.findViewById(R.id.tvFeedBackTime);
		tvDistance = (MyFontTextView) feedbackFragmentView
				.findViewById(R.id.tvFeedbackDistance);
		ratingFeedback = (RatingBar) feedbackFragmentView
				.findViewById(R.id.ratingFeedback);
		ivDriverImage = (ImageView) feedbackFragmentView
				.findViewById(R.id.ivFeedbackDriverImage);
		tvClientName = (MyFontTextView) feedbackFragmentView
				.findViewById(R.id.tvClientName);

		mapActivity.setActionBarTitle(getResources().getString(
				R.string.text_feedback));

		feedbackFragmentView.findViewById(R.id.tvFeedbackSubmit)
				.setOnClickListener(this);
		feedbackFragmentView.findViewById(R.id.tvFeedbackskip)
				.setOnClickListener(this);

		return feedbackFragmentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		aQuery = new AQuery(mapActivity);
		RequestDetail requestDetail = (RequestDetail) getArguments()
				.getSerializable(AndyConstants.REQUEST_DETAIL);
		Bill bill = (Bill) getArguments().getSerializable("bill");
		showInvoiceDialog(bill);

		if (requestDetail.getClientProfile() != null)
			aQuery.id(ivDriverImage).image(requestDetail.getClientProfile());
		// tvTime.setText(getArguments().getString(AndyConstants.Params.TIME));
		// tvDistance.setText(getArguments().getString(
		// AndyConstants.Params.DISTANCE));

		tvTime.setText((int) (Double.parseDouble(requestDetail.getTime()))
				+ " " + getString(R.string.text_mins));
		tvDistance.setText(new DecimalFormat("0.00").format(Double
				.parseDouble(requestDetail.getDistance()))
				+ " "
				+ requestDetail.getUnit());
		tvClientName.setText(requestDetail.getClientName());
	}

	private void showInvoiceDialog(final Bill bill) {
		m_invoiceDlg = new Dialog(getActivity(),
				android.R.style.Theme_Translucent_NoTitleBar);
		m_invoiceDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

		m_invoiceDlg.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		m_invoiceDlg.setContentView(R.layout.bill_layout);
		DecimalFormat decimalFormat = new DecimalFormat("0.00");
		DecimalFormat perHourFormat = new DecimalFormat("0.0");
		String currency = bill.getCurrency();
		String basePrice = String.valueOf(decimalFormat.format(Double
				.parseDouble(bill.getBasePrice())));
//		String basePricetmp = String.valueOf(decimalFormat.format(Double
//				.parseDouble(basePrice)));
		String totalTmp = String.valueOf(decimalFormat.format(Double
				.parseDouble(bill.getTotal())));
		String distCostTmp = String.valueOf(decimalFormat.format(Double
				.parseDouble(bill.getDistanceCost())));
		String timeCost = String.valueOf(decimalFormat.format(Double
				.parseDouble(bill.getTimeCost())));
		String primary_amount = String.valueOf(decimalFormat.format(Double
				.parseDouble(bill.getPrimary_amount())));
		String secoundry_amount = String.valueOf(decimalFormat.format(Double
				.parseDouble(bill.getSecoundry_amount())));
		String discounts = String.valueOf(decimalFormat.format(Math.abs((Double
				.parseDouble(bill.getPrimary_amount()) + Double
				.parseDouble(bill.getSecoundry_amount()))
				- (Double.parseDouble(bill.getTotal())))));

		((TextView) m_invoiceDlg.findViewById(R.id.tvBasePrice)).setText(currency
				+ " " + basePrice);
		((TextView) m_invoiceDlg.findViewById(R.id.tvBillDistancePerMile))
				.setText(currency
						+ " "
						+ String.valueOf(decimalFormat.format(Double
						.parseDouble(bill.getPricePerUnitDistance())))
						+ " "
						+ getResources().getString(
						R.string.text_cost_per_km));
		((TextView) m_invoiceDlg.findViewById(R.id.tvBillTimePerHour))
				.setText(currency
						+ " "
						+ String.valueOf(decimalFormat.format(Double
						.parseDouble(bill.getPricePerUnitTime())))
						+ " "
						+ getResources().getString(
						R.string.text_cost_per_min));

		((TextView) m_invoiceDlg.findViewById(R.id.adminCost))
				.setText(getResources().getString(R.string.text_cost_for_admin)
						+ " :    " + currency + " " + secoundry_amount);

		((TextView) m_invoiceDlg.findViewById(R.id.providercost))
				.setText(getResources().getString(
						R.string.text_cost_for_provider)
						+ " : " + currency + " " + primary_amount);

		((TextView) m_invoiceDlg.findViewById(R.id.discounts))
				.setText(getResources().getString(R.string.text_discount)
						+ " :     " + currency + " " + discounts);

		((TextView) m_invoiceDlg.findViewById(R.id.tvDis1)).setText(currency + " "+ distCostTmp);
		((TextView) m_invoiceDlg.findViewById(R.id.tvTime1)).setText(currency + " "+ timeCost);
		((TextView) m_invoiceDlg.findViewById(R.id.tvTotal1)).setText(currency + " "+ totalTmp);
		Button btnBillDialogClose = (Button) m_invoiceDlg.findViewById(R.id.btnBillDialogClose);
		btnBillDialogClose.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				m_bNotPaid = false;
				sendFinishPayment(1, bill.getTotal());
				m_invoiceDlg.dismiss();
			}
		});

		Button btnCash = (Button) m_invoiceDlg.findViewById(R.id.btnBillCash);
		btnCash.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				m_bNotPaid = false;
				sendFinishPayment(1, bill.getTotal());
				m_invoiceDlg.dismiss();
			}
		});
//		Button btnCard = (Button) m_invoiceDlg.findViewById(R.id.btnBillCard);
//		btnCard.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				m_bNotPaid = false;
//				sendFinishPayment(2, bill.getTotal());
//				m_invoiceDlg.dismiss();
//			}
//		});
		Button btnNotPaid = (Button) m_invoiceDlg.findViewById(R.id.btnBillNotPaid);
		btnNotPaid.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				m_bNotPaid = true;
				sendFinishPayment(0, bill.getTotal());
				m_invoiceDlg.dismiss();
			}
		});

		m_invoiceDlg.setCancelable(true);
		m_invoiceDlg.show();
	}

	private void sendFinishPayment(int nPayResult, String total) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.FINISH_PAYMENT);
		map.put(AndyConstants.Params.ID, PreferenceHelper.getInstance(mapActivity).getUserId());
		map.put(AndyConstants.Params.TOKEN, PreferenceHelper.getInstance(mapActivity).getSessionToken());
		map.put(AndyConstants.Params.REQUEST_ID,
				String.valueOf(PreferenceHelper.getInstance(mapActivity).getRequestId()));
		map.put(AndyConstants.Params.PAY_RESULT, String.valueOf(nPayResult));
		map.put(AndyConstants.Params.PAYMENT, total);

		new HttpRequester(mapActivity, map, AndyConstants.ServiceCode.FINISH_PAYMENT, this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.tvFeedbackSubmit:

		/*	if (TextUtils.isEmpty(etFeedbackComment.getText().toString())) {
				AndyUtils.showToast(
						mapActivity.getResources().getString(
								R.string.text_empty_feedback), mapActivity);
				return;
			} else {*/
				giveRating();
		//	}
			break;

		case R.id.tvFeedbackskip:
			PreferenceHelper.getInstance(mapActivity).clearRequestData();
			if(m_bNotPaid) {
				blockAccount();
			} else {
				mapActivity.addFragment(new ClientRequestFragment(), false,
						AndyConstants.CLIENT_REQUEST_TAG, true);
			}

		default:
			break;
		}
	}

	// giving feedback for perticular job
	private void giveRating() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}

		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_rating), false);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.RATING);
		map.put(AndyConstants.Params.ID, PreferenceHelper.getInstance(mapActivity).getUserId());
		map.put(AndyConstants.Params.TOKEN, PreferenceHelper.getInstance(mapActivity).getSessionToken());
		map.put(AndyConstants.Params.REQUEST_ID,
				String.valueOf(PreferenceHelper.getInstance(mapActivity).getRequestId()));
		map.put(AndyConstants.Params.RATING,
				String.valueOf(ratingFeedback.getNumStars()));
		map.put(AndyConstants.Params.COMMENT, etFeedbackComment.getText()
				.toString().trim());

		new HttpRequester(mapActivity, map, AndyConstants.ServiceCode.RATING,
				this);
	}

	public void blockAccount() {
		AndyUtils.showToastLong(mapActivity.getResources().getString(
						R.string.text_account_blocked), mapActivity);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.LOGOUT);
		map.put(AndyConstants.Params.ID, PreferenceHelper.getInstance(mapActivity).getUserId());
		map.put(AndyConstants.Params.TOKEN, PreferenceHelper.getInstance(mapActivity).getSessionToken());
		new HttpRequester(mapActivity, map,
				AndyConstants.ServiceCode.LOGOUT, true, this);
	}

	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		AndyUtils.removeCustomProgressDialog();
		switch (serviceCode) {
			case AndyConstants.ServiceCode.RATING:
				AppLog.Log(TAG, "rating response" + response);
				if (parseContent.isSuccess(response)) {
					PreferenceHelper.getInstance(mapActivity).clearRequestData();
					if(m_bNotPaid) {
						blockAccount();
					} else {
						AndyUtils.showToast(
								mapActivity.getResources().getString(
										R.string.toast_feedback_success), mapActivity);
						mapActivity.addFragment(new ClientRequestFragment(), false,
								AndyConstants.CLIENT_REQUEST_TAG, true);
					}
				}

				break;
			case AndyConstants.ServiceCode.FINISH_PAYMENT:
				AppLog.Log(TAG, "Finish payment response" + response);
//				if (parseContent.isSuccess(response)) {
//					PreferenceHelper.getInstance(mapActivity).clearRequestData();
//				}

				break;
			case AndyConstants.ServiceCode.LOGOUT:
				PreferenceHelper.getInstance(mapActivity).Logout();
				mapActivity.goToMainActivity();

				break;

		default:
			break;
		}
	}
}
