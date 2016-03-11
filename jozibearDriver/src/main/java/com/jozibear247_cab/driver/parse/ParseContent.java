package com.jozibear247_cab.driver.parse;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.jozibear247_cab.driver.R;
import com.jozibear247_cab.driver.db.DBHelper;
import com.jozibear247_cab.driver.maputills.PolyLineUtils;
import com.jozibear247_cab.driver.model.ApplicationPages;
import com.jozibear247_cab.driver.model.BeanRoute;
import com.jozibear247_cab.driver.model.BeanStep;
import com.jozibear247_cab.driver.model.Bill;
import com.jozibear247_cab.driver.model.History;
import com.jozibear247_cab.driver.model.RequestDetail;
import com.jozibear247_cab.driver.model.User;
import com.jozibear247_cab.driver.model.VehicalType;
import com.jozibear247_cab.driver.utills.AndyConstants;
import com.jozibear247_cab.driver.utills.AndyUtils;
import com.jozibear247_cab.driver.utills.AppLog;
import com.jozibear247_cab.driver.utills.PreferenceHelper;
import com.jozibear247_cab.driver.utills.ReadFiles;

public class ParseContent {
	private Activity activity;
//	private PreferenceHelper preferenceHelper;
	private final String KEY_SUCCESS = "success";
	private final String KEY_ERROR = "error";
	private final String IS_WALKER_STARTED = "is_walker_started";
	private final String IS_WALKER_ARRIVED = "is_walker_arrived";
	private final String IS_WALK_STARTED = "is_started";
	private final String IS_DOG_RATED = "is_dog_rated";
	private final String IS_WALK_COMPLETED = "is_completed";
	private final String KEY_ERROR_CODE = "error_code";
	private final String CURRENCY = "currency";

	private final String BASE_PRICE = "base_price";
	private final String TYPES = "types";
	private final String ID = "id";
	private final String ICON = "icon";
	private final String IS_DEFAULT = "is_default";
	private final String PRICE_PER_UNIT_TIME = "price_per_unit_time";
	private final String PRICE_PER_UNIT_DISTANCE = "price_per_unit_distance";

	public ParseContent(Activity activity) {
		this.activity = activity;
//		preferenceHelper = new PreferenceHelper(activity);
	}

	/**
	 * @param applicationContext
	 */

	public BeanRoute parseRoute(String response, BeanRoute routeBean) {

		try {
			BeanStep stepBean;
			JSONObject jObject = new JSONObject(response);
			JSONArray jArray = jObject.getJSONArray("routes");
			for (int i = 0; i < jArray.length(); i++) {
				JSONObject innerjObject = jArray.getJSONObject(i);
				if (innerjObject != null) {
					JSONArray innerJarry = innerjObject.getJSONArray("legs");
					for (int j = 0; j < innerJarry.length(); j++) {

						JSONObject jObjectLegs = innerJarry.getJSONObject(j);
						routeBean.setDistanceText(jObjectLegs.getJSONObject(
								"distance").getString("text"));
						routeBean.setDistanceValue(jObjectLegs.getJSONObject(
								"distance").getInt("value"));

						routeBean.setDurationText(jObjectLegs.getJSONObject(
								"duration").getString("text"));
						routeBean.setDurationValue(jObjectLegs.getJSONObject(
								"duration").getInt("value"));

						routeBean.setStartAddress(jObjectLegs
								.getString("start_address"));
						routeBean.setEndAddress(jObjectLegs
								.getString("end_address"));

						routeBean.setStartLat(jObjectLegs.getJSONObject(
								"start_location").getDouble("lat"));
						routeBean.setStartLon(jObjectLegs.getJSONObject(
								"start_location").getDouble("lng"));
						routeBean.setEndLat(jObjectLegs.getJSONObject(
								"end_location").getDouble("lat"));
						routeBean.setEndLon(jObjectLegs.getJSONObject(
								"end_location").getDouble("lng"));

						JSONArray jstepArray = jObjectLegs
								.getJSONArray("steps");
						if (jstepArray != null) {
							for (int k = 0; k < jstepArray.length(); k++) {
								stepBean = new BeanStep();
								JSONObject jStepObject = jstepArray
										.getJSONObject(k);
								if (jStepObject != null) {

									stepBean.setHtml_instructions(jStepObject
											.getString("html_instructions"));
									stepBean.setStrPoint(jStepObject
											.getJSONObject("polyline")
											.getString("points"));
									stepBean.setStartLat(jStepObject
											.getJSONObject("start_location")
											.getDouble("lat"));
									stepBean.setStartLon(jStepObject
											.getJSONObject("start_location")
											.getDouble("lng"));
									stepBean.setEndLat(jStepObject
											.getJSONObject("end_location")
											.getDouble("lat"));
									stepBean.setEndLong(jStepObject
											.getJSONObject("end_location")
											.getDouble("lng"));

									stepBean.setListPoints(new PolyLineUtils()
											.decodePoly(stepBean.getStrPoint()));
									routeBean.getListStep().add(stepBean);
								}

							}
						}
					}

				}

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return routeBean;
	}

	public boolean isSuccessWithId(String response) {
		if (TextUtils.isEmpty(response)) {
			return false;
		}
		try {
			JSONObject jsonObject = new JSONObject(response);
			boolean result = true;
			try {
				result = jsonObject.getInt(KEY_SUCCESS) == 1 ? true : false;
			} catch (Exception e) {
				result = jsonObject.getBoolean(KEY_SUCCESS);
			}
			if (result) {
				PreferenceHelper.getInstance(activity).putUserId(jsonObject
						.getString(AndyConstants.Params.ID));
				PreferenceHelper.getInstance(activity).putSessionToken(jsonObject
						.getString(AndyConstants.Params.TOKEN));
				PreferenceHelper.getInstance(activity).putEmail(jsonObject
						.getString(AndyConstants.Params.EMAIL));
				PreferenceHelper.getInstance(activity).putEmailActivation(jsonObject.getInt(AndyConstants.Params.EMAIL_ACTIVATION));
				PreferenceHelper.getInstance(activity).putLoginBy(jsonObject
						.getString(AndyConstants.Params.LOGIN_BY));
				if (!PreferenceHelper.getInstance(activity).getLoginBy().equalsIgnoreCase(
						AndyConstants.MANUAL)) {
					PreferenceHelper.getInstance(activity).putSocialId(jsonObject
							.getString(AndyConstants.Params.SOCIAL_UNIQUE_ID));
				}
				return true;
			} else {
				AndyUtils.showErrorToast(jsonObject.getInt(KEY_ERROR_CODE),
						activity);
				return false;
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean isSuccess(String response) {
		if (TextUtils.isEmpty(response))
			return false;
		JSONObject jsonObject = null;
		boolean result = true;
		try {
			jsonObject = new JSONObject(response);
			try {
				result = jsonObject.getInt(KEY_SUCCESS) == 1 ? true : false;
			} catch (Exception e) {
				result = jsonObject.getBoolean(KEY_SUCCESS);
			}
			if (!result) {
				AndyUtils.showToast(jsonObject.getString(KEY_ERROR), activity);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			result = false;
		}

		return result;
	}

	public Bill parseBillWhenWalkComplete(String response) {
		Bill bill = new Bill();

		if (TextUtils.isEmpty(response)) {
			return null;
		}

		try {
			int index = response.indexOf("}{");
			if(index > 0) {
				response = response.substring(index + 1);
			}
			JSONObject jsonObject = new JSONObject(response);
			boolean result = true;
			try {
				result = jsonObject.getInt(KEY_SUCCESS) == 1 ? true : false;
			} catch (Exception e) {
				result = jsonObject.getBoolean(KEY_SUCCESS);
			}
			if (result) {
				JSONObject billObject = jsonObject.getJSONObject("bill");
				bill.setBasePrice(billObject.getString(BASE_PRICE));
				bill.setDistance(billObject
						.getString(AndyConstants.Params.DISTANCE));
				bill.setUnit(billObject.getString("unit"));
				bill.setTime(billObject.getString(AndyConstants.Params.TIME));
				bill.setDistanceCost(billObject
						.getString(AndyConstants.Params.DISTANCE_COST));
				bill.setTimeCost(billObject
						.getString(AndyConstants.Params.TIME_COST));
				bill.setCurrency(billObject.getString(CURRENCY));
				bill.setTotal(billObject.getString("actual_total"));

				JSONObject walkerObject = billObject.getJSONObject("walker");
				bill.setPrimary_amount(walkerObject.getString("amount"));

				JSONObject adminObject = billObject.getJSONObject("admin");
				bill.setSecoundry_amount(adminObject.getString("amount"));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bill;
	}

	public ArrayList<String> parseCountryCodes() {
		String response = "";
		ArrayList<String> list = new ArrayList<String>();
		try {
			response = ReadFiles.readRawFileAsString(activity,
					R.raw.countrycodes);

			JSONArray array = new JSONArray(response);
			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				list.add(object.getString("phone-code") + " "
						+ object.getString("name"));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	public int getErrorCode(String response) {
		if (TextUtils.isEmpty(response))
			return 0;
		try {
			AppLog.Log("TAG", response);
			JSONObject jsonObject = new JSONObject(response);
			return jsonObject.getInt(KEY_ERROR_CODE);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int parseRequestInProgress(String response) {
		if (TextUtils.isEmpty(response)) {
			return AndyConstants.NO_REQUEST;
		}
		try {
			JSONObject jsonObject = new JSONObject(response);
			boolean result = true;
			try {
				result = jsonObject.getInt(KEY_SUCCESS) == 1 ? true : false;
			} catch (Exception e) {
				result = jsonObject.getBoolean(KEY_SUCCESS);
			}
			if (result) {
				int requestId = jsonObject
						.getInt(AndyConstants.Params.REQUEST_ID);
				PreferenceHelper.getInstance(activity).putRequestId(requestId);
				return requestId;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return AndyConstants.NO_REQUEST;
	}

	public RequestDetail parseRequestStatus(String response) {
		if (TextUtils.isEmpty(response)) {
			return null;
		}
		RequestDetail requestDetail = null;
		try {
			JSONObject jsonObject = new JSONObject(response);
			boolean result = true;
			try {
				result = jsonObject.getInt(KEY_SUCCESS) == 1 ? true : false;
			} catch (Exception e) {
				result = jsonObject.getBoolean(KEY_SUCCESS);
			}
			if (result) {
				requestDetail = new RequestDetail();
				requestDetail.setJobStatus(AndyConstants.IS_ASSIGNED);
				JSONObject object = jsonObject
						.getJSONObject(AndyConstants.Params.REQUEST);

				if (object.getInt(IS_WALKER_STARTED) == 0) {
					requestDetail.setJobStatus(AndyConstants.IS_WALKER_STARTED);
					// status = AndyConstants.IS_WALKER_STARTED;
				} else if (object.getInt(IS_WALKER_ARRIVED) == 0) {
					requestDetail.setJobStatus(AndyConstants.IS_WALKER_ARRIVED);
					// status = AndyConstants.IS_WALKER_ARRIVED;
				} else if (object.getInt(IS_WALK_STARTED) == 0) {
					requestDetail.setJobStatus(AndyConstants.IS_WALK_STARTED);
					// status = AndyConstants.IS_WALK_STARTED;
				} else if (object.getInt(IS_WALK_COMPLETED) == 0) {
					PreferenceHelper.getInstance(activity).putIsTripStart(true);
					requestDetail.setJobStatus(AndyConstants.IS_WALK_COMPLETED);
					// status = AndyConstants.IS_WALK_COMPLETED;
				} else if (object.getInt(IS_DOG_RATED) == 0) {
					requestDetail.setJobStatus(AndyConstants.IS_DOG_RATED);
					// status = AndyConstants.IS_DOG_RATED;
				}

				String time = object.optString(AndyConstants.Params.START_TIME);
				// "start_time": "2014-11-20 03:27:37"
				if (!TextUtils.isEmpty(time)) {
					try {
						TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
						Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
								Locale.ENGLISH).parse(time);
						AppLog.Log("TAG", "START DATE---->" + date.toString()
								+ " month:" + date.getMonth());
						PreferenceHelper.getInstance(activity).putRequestTime(date.getTime());
						requestDetail.setStartTime(date.getTime());
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				JSONObject ownerDetailObject = object
						.getJSONObject(AndyConstants.Params.OWNER);
				requestDetail.setClientName(ownerDetailObject
						.getString(AndyConstants.Params.NAME));
				requestDetail.setClientProfile(ownerDetailObject
						.getString(AndyConstants.Params.PICTURE));
				requestDetail.setClientPhoneNumber(ownerDetailObject
						.getString(AndyConstants.Params.PHONE));
				requestDetail.setClientRating((float) ownerDetailObject
						.optDouble(AndyConstants.Params.RATING));
				requestDetail.setClientLatitude(ownerDetailObject
						.getString(AndyConstants.Params.LATITUDE));
				requestDetail.setClientLongitude(ownerDetailObject
						.getString(AndyConstants.Params.LONGITUDE));
				if(object.has("unit")) {
					requestDetail.setUnit(object.getString("unit"));
				} else {
					requestDetail.setUnit("kms");
				}
				JSONObject jsonObjectBill = jsonObject.optJSONObject("bill");
				if (jsonObjectBill != null) {
					requestDetail.setTime(jsonObjectBill.getString("time"));
					requestDetail.setDistance(jsonObjectBill
							.getString("distance"));
				}

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return requestDetail;
	}

	public RequestDetail parseAllRequests(String response) {

		Log.d("xxx", "from parse all request  " + response);
		if (TextUtils.isEmpty(response)) {
			return null;
		}
		try {
			JSONObject jsonObject = new JSONObject(response);
			boolean result = true;
			try {
				result = jsonObject.getInt(KEY_SUCCESS) == 1 ? true : false;
			} catch (Exception e) {
				result = jsonObject.getBoolean(KEY_SUCCESS);
			}
			if (result) {
				JSONArray jsonArray = jsonObject
						.getJSONArray(AndyConstants.Params.INCOMING_REQUESTS);
				if (jsonArray.length() > 0) {
					JSONObject object = jsonArray.getJSONObject(0);

					if (object.getInt(AndyConstants.Params.REQUEST_ID) != AndyConstants.NO_REQUEST) {
						int timeto_respond = object
								.getInt(AndyConstants.Params.TIME_LEFT_TO_RESPOND);
						if (timeto_respond < 0) {
							return null;
						}
						RequestDetail requestDetail = new RequestDetail();
						requestDetail.setRequestId(object
								.getInt(AndyConstants.Params.REQUEST_ID));
						requestDetail.setTimeLeft(timeto_respond);

						JSONObject requestData = object
								.getJSONObject(AndyConstants.Params.REQUEST_DATA);
						JSONObject ownerDetailObject = requestData
								.getJSONObject(AndyConstants.Params.OWNER);
						requestDetail.setClientName(ownerDetailObject
								.getString(AndyConstants.Params.NAME));
						requestDetail.setClientProfile(ownerDetailObject
								.getString(AndyConstants.Params.PICTURE));
						requestDetail.setClientPhoneNumber(ownerDetailObject
								.getString(AndyConstants.Params.PHONE));
						if (!TextUtils.isEmpty(ownerDetailObject
								.getString(AndyConstants.Params.RATING))) {
							requestDetail
									.setClientRating((float) ownerDetailObject
											.getDouble(AndyConstants.Params.RATING));
						} else {
							requestDetail.setClientRating(0);
						}
						requestDetail.setClientLatitude(ownerDetailObject
								.getString(AndyConstants.Params.LATITUDE));
						requestDetail.setClientLongitude(ownerDetailObject
								.getString(AndyConstants.Params.LONGITUDE));
						try {
							requestDetail
									.setClient_d_latitude(ownerDetailObject
											.getString(AndyConstants.Params.DEST_LATITUDE));
							requestDetail
									.setClient_d_longitude(ownerDetailObject
											.getString(AndyConstants.Params.DEST_LONGITUDE));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return requestDetail;
					}

				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;

	}
	public RequestDetail parseNotification(String response) {
		Log.d("xxx", "from parsenotification  " + response);

		if (TextUtils.isEmpty(response)) {
			return null;
		}
		try {
			JSONObject object = new JSONObject(response);
			if (object.getInt(AndyConstants.Params.REQUEST_ID) != AndyConstants.NO_REQUEST) {
				int timeto_respond = object
						.getInt(AndyConstants.Params.TIME_LEFT_TO_RESPOND);
				if (timeto_respond < 0) {
					return null;
				}
				RequestDetail requestDetail = new RequestDetail();
				requestDetail.setRequestId(object
						.getInt(AndyConstants.Params.REQUEST_ID));
				requestDetail.setTimeLeft(timeto_respond);

				JSONObject requestData = object
						.getJSONObject(AndyConstants.Params.REQUEST_DATA);
				JSONObject ownerDetailObject = requestData
						.getJSONObject(AndyConstants.Params.OWNER);

				requestDetail.setClientName(ownerDetailObject
						.getString(AndyConstants.Params.NAME));
				requestDetail.setClientProfile(ownerDetailObject
						.getString(AndyConstants.Params.PICTURE));
				requestDetail.setClientPhoneNumber(ownerDetailObject
						.getString(AndyConstants.Params.PHONE));
				if (!TextUtils.isEmpty(ownerDetailObject
						.getString(AndyConstants.Params.RATING))) {
					requestDetail
							.setClientRating((float) ownerDetailObject
									.getDouble(AndyConstants.Params.RATING));
				} else {
					requestDetail.setClientRating(0);
				}
				requestDetail.setClientLatitude(ownerDetailObject
						.getString(AndyConstants.Params.LATITUDE));
				requestDetail.setClientLongitude(ownerDetailObject
						.getString(AndyConstants.Params.LONGITUDE));
				try {
					requestDetail.setClient_d_latitude(ownerDetailObject
							.getString(AndyConstants.Params.DEST_LATITUDE));
					requestDetail.setClient_d_longitude(ownerDetailObject
							.getString(AndyConstants.Params.DEST_LONGITUDE));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return requestDetail;

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;

	}

	public User parseUserAndStoreToDb(String response) {
		User user = null;
		try {
			JSONObject jsonObject = new JSONObject(response);
			boolean result = true;
			try {
				result = jsonObject.getInt(KEY_SUCCESS) == 1 ? true : false;
			} catch (Exception e) {
				result = jsonObject.getBoolean(KEY_SUCCESS);
			}
			if (result) {
				user = new User();
//				DBHelper dbHelper = new DBHelper(activity);
				user.setUserId(jsonObject.getInt(AndyConstants.Params.ID));
				user.setEmail(jsonObject.optString(AndyConstants.Params.EMAIL));
				user.setFname(jsonObject
						.getString(AndyConstants.Params.FIRSTNAME));
				user.setLname(jsonObject
						.getString(AndyConstants.Params.LAST_NAME));

				user.setAddress(jsonObject
						.getString(AndyConstants.Params.ADDRESS));
				user.setBio(jsonObject.getString(AndyConstants.Params.BIO));
				user.setZipcode(jsonObject
						.getString(AndyConstants.Params.ZIPCODE));
				user.setPicture(jsonObject
						.getString(AndyConstants.Params.PICTURE));
				user.setContact(jsonObject
						.getString(AndyConstants.Params.PHONE));
				user.setTimezone(jsonObject
						.getString(AndyConstants.Params.TIMEZONE));
				user.setEmailActivation(jsonObject.getInt(AndyConstants.Params.EMAIL_ACTIVATION));
				DBHelper.getInstance(activity).createUser(user);

			} else {
				// AndyUtils.showToast(jsonObject.getString(KEY_ERROR),
				// activity);

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return user;
	}

	// public boolean isSuccess(String response) {
	// if (TextUtils.isEmpty(response)) {
	// return false;
	// }
	// try {
	// JSONObject jsonObject = new JSONObject(response);
	// if (jsonObject.getBoolean(KEY_SUCCESS)) {
	// return true;
	// }
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	// return false;
	//
	// }

	public ArrayList<ApplicationPages> parsePages(
			ArrayList<ApplicationPages> list, String response) {
		list.clear();
		ApplicationPages applicationPages = new ApplicationPages();
		applicationPages.setId(-1);
		applicationPages.setTitle(activity.getResources().getString(
				R.string.text_profile));
		applicationPages.setData("");
		applicationPages.setIcon("");
		list.add(applicationPages);
		applicationPages = new ApplicationPages();
		applicationPages.setId(-2);
		applicationPages.setTitle(activity.getResources().getString(
				R.string.text_history));
		applicationPages.setData("");
		applicationPages.setIcon("");
		list.add(applicationPages);
		applicationPages = new ApplicationPages();
		applicationPages.setId(-3);
		applicationPages.setTitle(activity.getResources().getString(
				R.string.text_setting));
		applicationPages.setData("");
		applicationPages.setIcon("");
		list.add(applicationPages);
		if (TextUtils.isEmpty(response)) {
			return list;
		}
		try {
			JSONObject jsonObject = new JSONObject(response);
			boolean result = true;
			try {
				result = jsonObject.getInt(KEY_SUCCESS) == 1 ? true : false;
			} catch (Exception e) {
				result = jsonObject.getBoolean(KEY_SUCCESS);
			}
			if (result) {
				JSONArray jsonArray = jsonObject
						.getJSONArray(AndyConstants.Params.INFORMATIONS);
				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						applicationPages = new ApplicationPages();
						JSONObject object = jsonArray.getJSONObject(i);
						applicationPages.setId(object
								.getInt(AndyConstants.Params.ID));
						applicationPages.setTitle(object
								.getString(AndyConstants.Params.TITLE));
						applicationPages.setData(object
								.getString(AndyConstants.Params.CONTENT));
						applicationPages.setIcon(object
								.getString(AndyConstants.Params.ICON));
						list.add(applicationPages);
					}
				}

			}
			// else {
			// AndyUtils.showToast(jsonObject.getString(KEY_ERROR), activity);
			// }
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	public boolean checkDriverStatus(String response) {
		if (TextUtils.isEmpty(response))
			return false;
		try {
			JSONObject jsonObject = new JSONObject(response);
			boolean result = true;
			try {
				result = jsonObject.getInt(KEY_SUCCESS) == 1 ? true : false;
			} catch (Exception e) {
				result = jsonObject.getBoolean(KEY_SUCCESS);
			}
			if (result) {
				if (jsonObject.getInt(AndyConstants.Params.IS_ACTIVE) == 0) {
					return false;
				} else {
					return true;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	public ArrayList<VehicalType> parseTypes(String response,
			ArrayList<VehicalType> list) {
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(response);
			boolean result = true;
			try {
				result = jsonObject.getInt(KEY_SUCCESS) == 1 ? true : false;
			} catch (Exception e) {
				result = jsonObject.getBoolean(KEY_SUCCESS);
			}
			if (result) {
				JSONArray jsonArray = jsonObject.getJSONArray(TYPES);
				for (int i = 0; i < jsonArray.length(); i++) {
					VehicalType type = new VehicalType();
					JSONObject typeJson = jsonArray.getJSONObject(i);
					type.setBasePrice(typeJson.getString(BASE_PRICE));
					type.setIcon(typeJson.getString(ICON));
					type.setId(typeJson.getInt(ID));
					type.setName(typeJson.getString(AndyConstants.Params.NAME));
					type.setPricePerUnitDistance(typeJson
							.getString(PRICE_PER_UNIT_DISTANCE));
					type.setPricePerUnitTime(typeJson
							.getString(PRICE_PER_UNIT_TIME));
					list.add(type);

				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;

	}

	public ArrayList<History> parseHistory(String response,
			ArrayList<History> list) {

		list.clear();

		if (TextUtils.isEmpty(response)) {
			return list;
		}
		try {
			JSONObject jsonObject = new JSONObject(response);
			boolean result = true;
			try {
				result = jsonObject.getInt(KEY_SUCCESS) == 1 ? true : false;
			} catch (Exception e) {
				result = jsonObject.getBoolean(KEY_SUCCESS);
			}
			if (result) {
				JSONArray jsonArray = jsonObject
						.getJSONArray(AndyConstants.Params.REQUESTS);
				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject object = jsonArray.getJSONObject(i);
						History history = new History();
						history.setId(object.getInt(AndyConstants.Params.ID));
						history.setDate(object
								.getString(AndyConstants.Params.DATE));
						history.setDistance(object
								.getString(AndyConstants.Params.DISTANCE));
						history.setTime(object
								.getString(AndyConstants.Params.TIME));
						history.setBasePrice(object.getString(BASE_PRICE));
						history.setDistanceCost(object
								.getString(AndyConstants.Params.DISTANCE_COST));
						history.setCurrency(object.getString(CURRENCY));
						history.setTimecost(object
								.getString(AndyConstants.Params.TIME_COST));
						history.setTotal(new DecimalFormat("0.00").format(Double
								.parseDouble(object
										.getString(AndyConstants.Params.TOTAL))));
						JSONObject ja_primary = object.getJSONObject("walker");

						history.setPrimary_amount(ja_primary
								.getString("amount"));

						JSONObject ja_secoundry = object.getJSONObject("admin");
						history.setSecoundry_amount(ja_secoundry
								.getString("amount"));
						JSONObject userObject = object
								.getJSONObject(AndyConstants.Params.OWNER);
						history.setFirstName(userObject
								.getString(AndyConstants.Params.FIRSTNAME));
						history.setLastName(userObject
								.getString(AndyConstants.Params.LAST_NAME));
						history.setPhone(userObject
								.getString(AndyConstants.Params.PHONE));
						history.setPicture(userObject
								.getString(AndyConstants.Params.PICTURE));
						history.setEmail(userObject
								.getString(AndyConstants.Params.EMAIL));
						history.setBio(userObject
								.getString(AndyConstants.Params.BIO));
						list.add(history);
					}
				}

			}
			// else {
			// AndyUtils.showToast(jsonObject.getString(KEY_ERROR), activity);
			// }
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * @param response
	 * @return
	 */
	public boolean parseAvaibilty(String response) {
		if (TextUtils.isEmpty(response))
			return false;
		try {
			JSONObject jsonObject = new JSONObject(response);
			boolean result = true;
			try {
				result = jsonObject.getInt(KEY_SUCCESS) == 1 ? true : false;
			} catch (Exception e) {
				result = jsonObject.getBoolean(KEY_SUCCESS);
			}
			if (result) {
				if (jsonObject.getInt(AndyConstants.Params.IS_ACTIVE) == 1) {
					return true;
				}
			}

			// else {
			// AndyUtils.showToast(jsonObject.getString(KEY_ERROR), activity);
			// }
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @param response
	 * @param points
	 */
	public ArrayList<LatLng> parsePathRequest(String response,
			ArrayList<LatLng> points) {
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(response);
			boolean result = true;
			try {
				result = jsonObject.getInt(KEY_SUCCESS) == 1 ? true : false;
			} catch (Exception e) {
				result = jsonObject.getBoolean(KEY_SUCCESS);
			}
			if (result) {
				JSONArray jsonArray = jsonObject
						.getJSONArray(AndyConstants.Params.LOCATION_DATA);
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject json = jsonArray.getJSONObject(i);
					points.add(new LatLng(Double.parseDouble(json
							.getString(AndyConstants.Params.LATITUDE)), Double
							.parseDouble(json
									.getString(AndyConstants.Params.LONGITUDE))));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return points;
	}
}
