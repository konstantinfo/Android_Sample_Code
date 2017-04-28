package com.myelane;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.myelane.utility.UtilityList;
import com.myelane.webServices.RestClient;
import com.myelane.webServices.RestClient.RequestMethod;


/**
 * LinkedAccountFrag Fragment keeps two list with the functionality of add and delete user.  
 * The first list holds the list of linked user and   
 * the second list holds the list of pending user
 * A user can swaps the list items to delete the users.
 */


public class LinkedAccountFrag extends Fragment {

	private View mView;
	private ProgressDialog pDialog;
	private TextView linked_pendingtxt;
	private ImageButton linked_back, addBtn, addAccountBtn;
	private LinearLayout mNoItemLayout, ListItemLayout;
	
	private ListView mLinkedAccountList, mPendingList;
	
	private ArrayList<ArrayList<String>> arrayListAccount;
	private ArrayList<ArrayList<String>> arrayListPending;
	

	private boolean hasData;
	String id = "";
	
	static int x = -1; // x is the position of row in mLinkedAccountList which is used while user swipe the row
	static int y = -1; // y is the position of row in mPendingList which is used while user swipe the row
	
	
	AccountListAdapter accountListAdapter;
	PendingListAdapter pendingListAdapter;
	
	/**
     * Called when the Fragment is first created. Responsible for initializing the UI.
     */

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		mView = inflater.inflate(R.layout.linked_accounts, container, false);

		addBtn = (ImageButton) mView.findViewById(R.id.linked_add);
		linked_back = (ImageButton) mView.findViewById(R.id.linked_setting_btn);
		addAccountBtn = (ImageButton) mView.findViewById(R.id.linked_add_account);
		mLinkedAccountList = (ListView) mView.findViewById(R.id.linked_account_list);
		mPendingList = (ListView) mView.findViewById(R.id.linked_pending_list);
		mNoItemLayout = (LinearLayout) mView.findViewById(R.id.linked_noitem_layout);
		ListItemLayout = (LinearLayout) mView.findViewById(R.id.linked_item_layout);
		linked_pendingtxt = (TextView) mView.findViewById(R.id.linked_pendingtxt);
		
		
		// Handle Back Button 
		linked_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getFragmentManager().popBackStack();
			}
		});

		return mView;
	}

	
	/**
	 * Called when the fragment's activity has been created and this fragment's view hierarchy instantiated.
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Listener to Call NewAccountFrag ( for add new user )
		OnClickListener addAccountListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				NewAccountFrag newAccount = new NewAccountFrag();
				((MyElaneMainFrag) getActivity()).navigateTo(newAccount);
			}
		};

		addBtn.setOnClickListener(addAccountListener);
		addAccountBtn.setOnClickListener(addAccountListener);

		
		// Getting User List from the server 
		new GetAccountList().execute();
	}

	
	
		
	
	
	
	
	
	/**
	 *  Allows to GetAccountList perform (server call) in background
	 *  GetAccountList is responsible for fetching linked and pending user list from the server.	
	 *  Swipe touch shows delete button which is disappear on tap 
	 * @author kipl145
	 * 
	 *
	 */
	private class GetAccountList extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = ProgressDialog.show(getActivity(), "",getString(R.string.loading_data));
			pDialog.setCancelable(false);

			accountListAdapter = new AccountListAdapter(getActivity());
			pendingListAdapter = new PendingListAdapter(getActivity());
		}

		@Override
		protected String doInBackground(Void... params) {
			String message = "failure";
			String response = "";

			RestClient client = new RestClient(Config.newbaseurl+ "users/linkAccounts");
			try {
				client.Execute(RequestMethod.GET);
				response = client.getResponse();
				if (response != null) {
					JSONObject jsonObject = new JSONObject(response);
					message = jsonObject.getString("replyCode");
					// reply = jsonObject.getString("replyMsg");
					if (message.equalsIgnoreCase(Config.SUCCESS)) {
						hasData = true;
						arrayListAccount = new ArrayList<ArrayList<String>>();
						arrayListPending = new ArrayList<ArrayList<String>>();

						JSONObject follower = jsonObject.getJSONObject("Follower");

						try {
							JSONArray array = follower.getJSONArray("accounts");
							for (int i = 0; i < array.length(); i++) {
								JSONObject object = array.getJSONObject(i);
								ArrayList<String> data = new ArrayList<String>();
								data.add(object.getString("id"));
								data.add(object.getString("fullname"));
								data.add(object.getString("email"));
								data.add(object.getString("linkedId"));
								data.add(object.getString("first_name"));
								data.add(object.getString("last_name"));
								data.add(object.getString("userNickname"));
								arrayListAccount.add(data);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						// Getting Pending Request List
						try {
							JSONArray pendingArray = follower.getJSONArray("pending");

							for (int j = 0; j < pendingArray.length(); j++) {
								JSONObject object = pendingArray.getJSONObject(j);
								ArrayList<String> data = new ArrayList<String>();
								data.add(object.getString("id"));
								data.add(object.getString("fullname"));
								data.add(object.getString("email"));
								data.add(object.getString("linkedId"));

								arrayListPending.add(data);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						hasData = false;
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				if (pDialog != null & pDialog.isShowing()) {
					pDialog.dismiss();
				}

			}

			return message;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.e("result", "" + result);

			if (pDialog != null & pDialog.isShowing()) {
				pDialog.dismiss();
			}

			if (result.equalsIgnoreCase(Config.SUCCESS)) {

				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (hasData) {

							if (arrayListAccount != null) {
								if (arrayListAccount.size() > 0) {

									mLinkedAccountList.setAdapter(accountListAdapter);
									mLinkedAccountList.setDivider(null);

									ListItemLayout.setVisibility(View.VISIBLE);
									mNoItemLayout.setVisibility(View.INVISIBLE);
									mLinkedAccountList.setVisibility(View.VISIBLE);

									new Handler().post(new Runnable() {
										@Override
										public void run() {
											((AccountListAdapter) mLinkedAccountList.getAdapter()).notifyDataSetChanged();
											
											// Handle the list view size in scroll view 
											UtilityList.setListViewHeightBasedOnChildren(mLinkedAccountList);
										}
									});

								} else {
									mLinkedAccountList.setVisibility(View.GONE);
								}
							} else {
								mLinkedAccountList.setVisibility(View.GONE);

							}

							if (arrayListPending != null) {
								if (arrayListPending.size() > 0) {
									mPendingList.setAdapter(pendingListAdapter);
									mPendingList.setDivider(null);

									linked_pendingtxt
											.setVisibility(View.VISIBLE);
									ListItemLayout.setVisibility(View.VISIBLE);
									mNoItemLayout.setVisibility(View.INVISIBLE);
									mPendingList.setVisibility(View.VISIBLE);

									new Handler().post(new Runnable() {
										@Override
										public void run() {
											((PendingListAdapter) mPendingList
													.getAdapter())
													.notifyDataSetChanged();

											UtilityList
													.setListViewHeightBasedOnChildren(mPendingList);
										}
									});

								} else {
									linked_pendingtxt.setVisibility(View.GONE);
								}
							} else {
								linked_pendingtxt.setVisibility(View.GONE);
								mPendingList.setVisibility(View.GONE);

							}

						}
						mLinkedAccountList.invalidate();
						mPendingList.invalidate();

					}
				});

				// NoItemLayout appear if no data available on server 
				if (arrayListAccount.size() == 0 && arrayListPending.size() == 0) {
					ListItemLayout.setVisibility(View.INVISIBLE);
					mNoItemLayout.setVisibility(View.VISIBLE);
				}
			} else {
				// NoItemLayout appear if no data available on server 
				ListItemLayout.setVisibility(View.INVISIBLE);
				mNoItemLayout.setVisibility(View.VISIBLE);
			}
		}
	}
	
	
	/**
	 * The AccountListAdapter which implemented with OnGestureListener, OnTouchListener 
	 * which allows the functionality of swipe touch to delete the row (Same like iPhone)
	 * @author kipl145
	 *
	 */
	private class AccountListAdapter extends BaseAdapter implements
			OnGestureListener, OnTouchListener {

		LayoutInflater inflater;
		GestureDetector gDetector;

		public AccountListAdapter(Context c) {
			inflater = LayoutInflater.from(c);
			gDetector = new GestureDetector(c, this);
			mLinkedAccountList.setOnTouchListener(this);
		}

		@Override
		public int getCount() {
			if (arrayListAccount != null) {
				return arrayListAccount.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null || convertView.getTag() == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.linked_account_row,
						null);
				holder.userName = (TextView) convertView
						.findViewById(R.id.link_account_row_name);
				holder.userEmail = (TextView) convertView
						.findViewById(R.id.link_account_row_email);
				holder.linear = (LinearLayout) convertView
						.findViewById(R.id.right_layout);
				holder.listBack = (LinearLayout) convertView
						.findViewById(R.id.linked_row_back);
				holder.deleteBtn = (Button) convertView
						.findViewById(R.id.linked_deled_btn);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.userName.setText(arrayListAccount.get(position).get(1));
			holder.userEmail.setText(arrayListAccount.get(position).get(2));
			holder.listBack.setOnTouchListener(this);

			if (arrayListAccount.size() > 1) {
				if (position == 0) {
					holder.listBack.setBackgroundResource(R.drawable.settingtopbox);
				} else {
					holder.listBack.setBackgroundResource(R.drawable.settingmidbox);
				}
				if (position == arrayListAccount.size() - 1) {
					holder.listBack.setBackgroundResource(R.drawable.settingbotbox);
				}
			} else {
				holder.listBack.setBackgroundResource(R.drawable.settingfullbox);
			}

			if (position == x) {
				holder.deleteBtn.setVisibility(View.VISIBLE);
				holder.linear.setVisibility(View.GONE);
				holder.flag = true;
			} else {
				holder.deleteBtn.setVisibility(View.GONE);
				holder.linear.setVisibility(View.VISIBLE);
				holder.flag = false;
			}

			final int j = position;
			holder.deleteBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (holder.flag) {
						id = arrayListAccount.get(j).get(3);
						// Call Delete Task
						new DeleteTask().execute();
					}
				}
			});

			holder.linear.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					LinkedAccountEditFrag link_edit_frag = new LinkedAccountEditFrag();
					Bundle args = new Bundle();
					args.putString("id", arrayListAccount.get(j).get(0));
					args.putString("first_name", arrayListAccount.get(j).get(4));
					args.putString("last_name", arrayListAccount.get(j).get(5));
					args.putString("nickname", arrayListAccount.get(j).get(6));
					args.putString("email", arrayListAccount.get(j).get(2));

					link_edit_frag.setArguments(args);
					((MyElaneMainFrag) getActivity()).navigateTo(link_edit_frag);
				}
			});

			return convertView;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			try {
				x = (Integer) (accountListAdapter.getItem(mLinkedAccountList.pointToPosition(Math.round(e1.getX()),Math.round(e1.getY()))));
				accountListAdapter.notifyDataSetChanged();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {

		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {

		}

		
		/**
		 *	return the normal state of lists with a single tap
		 */
		
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			y = -1;
			x = -1;
			pendingListAdapter.notifyDataSetChanged();
			accountListAdapter.notifyDataSetChanged();
			return true;
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return gDetector.onTouchEvent(event);
		}
	}

	/**
	 * static class to hold parameters 
	 * @author kipl145
	 *
	 */
	static class ViewHolder {
		TextView userName;
		TextView userEmail;
		Button deleteBtn;
		LinearLayout listBack;
		LinearLayout linear;
		boolean flag;
	}

	/**
	 * class to hold pending list items
	 * @author kipl145
	 *
	 */
	private class PendingListAdapter extends BaseAdapter implements
			OnGestureListener, OnTouchListener {

		LayoutInflater inflater;
		GestureDetector gDetector;

		public PendingListAdapter(Context c) {
			inflater = LayoutInflater.from(c);
			gDetector = new GestureDetector(c, this);
			mPendingList.setOnTouchListener(this);
		}

		@Override
		public int getCount() {
			if (arrayListPending != null) {
				return arrayListPending.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final PendingHolder holder;
			if (convertView == null || convertView.getTag() == null) {
				holder = new PendingHolder();
				convertView = inflater.inflate(R.layout.pending_req_row, null);
				holder.userName = (TextView) convertView
						.findViewById(R.id.pending_account_row_name);
				holder.userEmail = (TextView) convertView
						.findViewById(R.id.pending_account_row_email);
				holder.listBack = (RelativeLayout) convertView
						.findViewById(R.id.pending_account_row_back);
				holder.approveBtn = (Button) convertView
						.findViewById(R.id.pending_approve);
				convertView.setTag(holder);
			} else {
				holder = (PendingHolder) convertView.getTag();
			}

			holder.userName.setText(arrayListPending.get(position).get(1));
			holder.userEmail.setText(arrayListPending.get(position).get(2));

			if (arrayListPending.size() > 1) {
				if (position == 0) {
					holder.listBack
							.setBackgroundResource(R.drawable.settingtopbox);
				} else {
					holder.listBack
							.setBackgroundResource(R.drawable.settingmidbox);
				}
				if (position == arrayListPending.size() - 1) {
					holder.listBack
							.setBackgroundResource(R.drawable.settingbotbox);
				}
			} else {
				holder.listBack
						.setBackgroundResource(R.drawable.settingfullbox);
			}

			final int k = position;
			holder.approveBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// Approve or Deny the Request

					id = arrayListPending.get(k).get(3);
					if (holder.flag) {
						// Deny the Pending Request
						new ApproveDenyReq().execute("deny");
					} else {
						// Approve the Pending Request
						new ApproveDenyReq().execute("approve");
					}
				}
			});

			if (position == y) {
				holder.approveBtn.setBackgroundResource(R.drawable.deny_btn);
				holder.flag = true;
			} else {
				holder.approveBtn.setBackgroundResource(R.drawable.approvebtn);
				holder.flag = false;
			}

			return convertView;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			try {
				y = (Integer) (pendingListAdapter.getItem(mPendingList
						.pointToPosition(Math.round(e1.getX()),
								Math.round(e1.getY()))));
				pendingListAdapter.notifyDataSetChanged();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {

		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {

		}
		
		
		/**
		 *	return the normal state of lists with a single tap
		 */
		
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			y = -1;
			x = -1;
			pendingListAdapter.notifyDataSetChanged();
			accountListAdapter.notifyDataSetChanged();
			return true;
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return gDetector.onTouchEvent(event);
		}
	}

	/**
	 * static class to hold variables for pending list
	 * @author kipl145
	 *
	 */
	static class PendingHolder {
		TextView userName;
		TextView userEmail;
		Button approveBtn;
		RelativeLayout listBack;
		boolean flag;
	}


	
	/**
	 * class to delete linked user
	 * @author kipl145
	 *
	 */
	private class DeleteTask extends AsyncTask<Void, Void, String> {

		String reply;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = ProgressDialog.show(getActivity(), "",
					getString(R.string.loading_data));
			pDialog.setCancelable(false);
		}

		@Override
		protected String doInBackground(Void... params) {
			String message = "";
			String response = "";

			RestClient client = new RestClient(Config.newbaseurl
					+ "users/deleteLinkRequest/" + id);
			try {
				client.Execute(RequestMethod.GET);
				response = client.getResponse();

				if (response != null) {
					JSONObject jsonObject = new JSONObject(response);
					message = jsonObject.getString("replyCode");
					reply = jsonObject.getString("replyMsg");
					if (message.equalsIgnoreCase(Config.SUCCESS)) {
						// Do Next Task
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				if (pDialog != null & pDialog.isShowing()) {
					pDialog.dismiss();
				}
			}

			return message;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (pDialog != null & pDialog.isShowing()) {
				pDialog.dismiss();
			}
			if (result.equalsIgnoreCase(Config.SUCCESS)) {
				x = -1;
				Toast.makeText(getActivity(), reply, Toast.LENGTH_SHORT).show();
				new GetAccountList().execute();
			}
		}

	}

	
	/**
	 *  Approve or Deny request of Pending user based on user id.
	 * @author kipl145
	 *
	 */
	private class ApproveDenyReq extends AsyncTask<String, Void, String> {

		String reply;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = ProgressDialog.show(getActivity(), "",getString(R.string.loading_data));
			pDialog.setCancelable(false);

		}

		@Override
		protected String doInBackground(String... params) {
			String message = "";
			String response = "";
			RestClient client = null;
			if (params[0].equalsIgnoreCase("deny")) {
				client = new RestClient(Config.newbaseurl+ "users/denyLinkRequest/" + id);
			} else {
				client = new RestClient(Config.newbaseurl+ "users/approaveLinkRequest/" + id);
			}

			try {
				client.Execute(RequestMethod.GET);
				response = client.getResponse();

				if (response != null) {
					JSONObject jsonObject = new JSONObject(response);
					message = jsonObject.getString("replyCode");
					reply = jsonObject.getString("replyMsg");
					if (message.equalsIgnoreCase(Config.SUCCESS)) {
						// Do Task
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				if (pDialog != null & pDialog.isShowing()) {
					pDialog.dismiss();
				}
			}

			return message;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (pDialog != null & pDialog.isShowing()) {
				pDialog.dismiss();
			}

			if (result.equalsIgnoreCase(Config.SUCCESS)) {
				y = -1;
				mPendingList.setVisibility(View.GONE);
				Toast.makeText(getActivity(), reply, Toast.LENGTH_SHORT).show();
				new GetAccountList().execute();
			}
		}

	}

}
