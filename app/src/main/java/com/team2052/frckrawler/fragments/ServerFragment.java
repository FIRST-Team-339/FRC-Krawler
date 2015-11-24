package com.team2052.frckrawler.fragments;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.team2052.frckrawler.GlobalValues;
import com.team2052.frckrawler.R;
import com.team2052.frckrawler.activities.EventInfoActivity;
import com.team2052.frckrawler.bluetooth.server.events.ServerStateChangeEvent;
import com.team2052.frckrawler.bluetooth.server.events.ServerStateRequestChangeEvent;
import com.team2052.frckrawler.bluetooth.server.events.ServerStateRequestEvent;
import com.team2052.frckrawler.db.Event;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class ServerFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private static final int REQUEST_BT_ENABLED = 1;
    private List<Event> mEvents = new ArrayList<>();

    SwitchCompat mHostToggle;
    Spinner mEventSpinner;
    TextInputLayout mServerSettingCompileWeight;
    View mServerEventContainer, mServerEventsError;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_server, null, false);
    }

    public void onEvent(ServerStateChangeEvent serverStateChangeEvent) {
        mHostToggle.setOnCheckedChangeListener(null);
        mHostToggle.setChecked(serverStateChangeEvent.getState());
        mHostToggle.setOnCheckedChangeListener(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHostToggle = (SwitchCompat) view.findViewById(R.id.host_toggle);
        mEventSpinner = (Spinner) view.findViewById(R.id.event_spinner);
        mServerSettingCompileWeight = (TextInputLayout) view.findViewById(R.id.server_setting_compile_weight);
        mServerEventContainer = view.findViewById(R.id.server_event_container);
        mServerEventsError = view.findViewById(R.id.server_events_error);

        view.findViewById(R.id.view_event).setOnClickListener(this);
        view.findViewById(R.id.excel).setOnClickListener(this);
        view.findViewById(R.id.server_settings_save).setOnClickListener(this);
        view.findViewById(R.id.server_settings_restore_defaults).setOnClickListener(this);

        new GetEventsTask().execute();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(GlobalValues.PREFS_FILE_NAME, 0);
        float compileWeight = sharedPreferences.getFloat(GlobalValues.PREFS_COMPILE_WEIGHT, 1.0f);

        mHostToggle.setOnCheckedChangeListener(this);

        if (mServerSettingCompileWeight.getEditText() != null)
            mServerSettingCompileWeight.getEditText().setText(String.valueOf(compileWeight));

        EventBus.getDefault().post(new ServerStateRequestEvent());
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.excel:
                if (isEventsValid() && getSelectedEvent() != null) {
                    ExportDialogFragment.newInstance(getSelectedEvent()).show(getChildFragmentManager(), "exportDialogProgress");
                } else {
                    Snackbar.make(getView(), "You don't have a selected event", Snackbar.LENGTH_SHORT).show();
                }
                break;
            case R.id.view_event:
                if (isEventsValid() && getSelectedEvent() != null)
                    startActivity(EventInfoActivity.newInstance(getActivity(), getSelectedEvent().getId()));
                break;
            case R.id.server_settings_save:
                onServerSettingSaveButtonClicked();
                break;
            case R.id.server_settings_restore_defaults:
                onRestoreButtonClicked();
                break;
        }
    }

    private Event getSelectedEvent() {
        return mEvents.get(mEventSpinner.getSelectedItemPosition());
    }

    private boolean isEventsValid() {
        return mEvents != null && !mEvents.isEmpty();
    }

    public void onHostToggleClicked(SwitchCompat switchCompat, boolean checked) {
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            Snackbar.make(getView(), "Sorry, your device does not support bluetooth.", Snackbar.LENGTH_LONG).show();
            return;
        } else if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_BT_ENABLED);
            return;
        }
        switchCompat.setChecked(!checked);
        toggleServer();
    }

    public void toggleServer() {
        if (isEventsValid()) {
            Event event = getSelectedEvent();
            EventBus.getDefault().post(new ServerStateRequestChangeEvent(!mHostToggle.isChecked(), event));
        }
    }

    public void onRestoreButtonClicked() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(GlobalValues.PREFS_FILE_NAME, 0);
        sharedPreferences.edit().putFloat(GlobalValues.PREFS_COMPILE_WEIGHT, 1.0f).apply();
        mServerSettingCompileWeight.getEditText().setText(String.valueOf(1.0f));
    }

    public void onServerSettingSaveButtonClicked() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(GlobalValues.PREFS_FILE_NAME, 0);
        float compileWeight = Float.parseFloat(mServerSettingCompileWeight.getEditText().getText().toString());
        mServerSettingCompileWeight.getEditText().setText(String.valueOf(compileWeight));
        sharedPreferences.edit().putFloat(GlobalValues.PREFS_COMPILE_WEIGHT, compileWeight).apply();
    }

    public void showEventError(boolean shown) {
        if (shown) {
            mServerEventContainer.setVisibility(View.GONE);
            mServerEventsError.setVisibility(View.VISIBLE);
        } else {
            mServerEventContainer.setVisibility(View.VISIBLE);
            mServerEventsError.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.host_toggle) {
            onHostToggleClicked((SwitchCompat) buttonView, isChecked);
        }
    }

    private class GetEventsTask extends AsyncTask<Void, Void, List<Event>> {

        @Override
        protected List<Event> doInBackground(Void... params) {
            return mDbManager.getEventsTable().loadAll();
        }

        @Override
        protected void onPostExecute(List<Event> _events) {
            if (getView() != null) {
                mEvents = _events;

                if (isEventsValid()) {
                    List<String> eventNames = new ArrayList<>();

                    for (Event event : _events) {
                        eventNames.add(mDbManager.getEventsTable().getGame(event).getName() + ", " + event.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, eventNames);
                    mEventSpinner.setAdapter(adapter);
                    showEventError(false);
                    return;
                }

                showEventError(true);
            }
        }
    }
}
