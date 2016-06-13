package com.team2052.frckrawler.fragments.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.team2052.frckrawler.R;
import com.team2052.frckrawler.database.DBManager;
import com.team2052.frckrawler.database.MetricHelper;
import com.team2052.frckrawler.database.MetricHelper.MetricFactory;
import com.team2052.frckrawler.db.Game;
import com.team2052.frckrawler.db.Metric;
import com.team2052.frckrawler.views.ListEditor;

/**
 * @author Adam
 */
public class AddMetricDialogFragment extends DialogFragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private static final String GAME_NAME_EXTRA = "GAME_NAME";
    private static final String METRIC_CATEGORY = "METRIC_CATEGORY";
    private int mMetricCategory;
    private ListEditor list;
    private
    @MetricHelper.MetricType
    int mCurrentSelectedMetricType;
    private Game mGame;
    private Spinner mMetricTypeSpinner;
    private EditText mName, mDescription, mMinimum, mMaximum, mIncrementation;
    private FrameLayout mListEditor;
    private View mListHeader;
    private DBManager mDBManager;

    public static AddMetricDialogFragment newInstance(int metricCategory, long game_id) {
        AddMetricDialogFragment f = new AddMetricDialogFragment();
        Bundle args = new Bundle();
        args.putInt(METRIC_CATEGORY, metricCategory);
        args.putLong(GAME_NAME_EXTRA, game_id);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mMetricCategory = args.getInt(METRIC_CATEGORY, -1);
        mDBManager = DBManager.getInstance(getActivity());
        mGame = mDBManager.getGamesTable().load(args.getLong(GAME_NAME_EXTRA));
        list = new ListEditor(getActivity());
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity(), R.style.AppAlertDialogStyle);
        b.setTitle("Add Metric");
        b.setView(initViews());
        b.setPositiveButton("Add", (dialog, which) -> {
            AddMetricDialogFragment.this.saveMetric();
        });
        b.setNegativeButton("Cancel", null);
        mMetricTypeSpinner.setOnItemSelectedListener(this);
        mMetricTypeSpinner.setSelection(0);
        return b.create();
    }

    private View initViews() {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_add_metric, null);
        mMetricTypeSpinner = (Spinner) view.findViewById(R.id.type);
        mName = (EditText) view.findViewById(R.id.name);
        mDescription = (EditText) view.findViewById(R.id.description);
        mMinimum = (EditText) view.findViewById(R.id.minimum);
        mMaximum = (EditText) view.findViewById(R.id.maximum);
        mIncrementation = (EditText) view.findViewById(R.id.incrementation);
        mListEditor = (FrameLayout) view.findViewById(R.id.list_editor);
        mListHeader = view.findViewById(R.id.list_header);
        return view;
    }

    private void saveMetric() {
        Metric m = null;
        String name = mName.getText().toString();
        String description = mDescription.getText().toString();
        final MetricFactory metricFactory = new MetricFactory(mGame, name);
        metricFactory.setDescription(description);
        metricFactory.setMetricCategory(mMetricCategory);
        metricFactory.setMetricType(mCurrentSelectedMetricType);
        switch (mCurrentSelectedMetricType) {
            case MetricHelper.COUNTER:
                try {
                    metricFactory.setDataMinMaxInc(
                            Integer.parseInt(mMinimum.getText().toString()),
                            Integer.parseInt(mMaximum.getText().toString()),
                            Integer.parseInt(mIncrementation.getText().toString()));
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "Could not create add_button. Make sure you " + "have filled out all of the necessary fields.", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case MetricHelper.SLIDER:
                try {
                    metricFactory.setDataMinMaxInc(
                            Integer.parseInt(mMinimum.getText().toString()),
                            Integer.parseInt(mMaximum.getText().toString()),
                            null);
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "Could not create add_button. Make sure you " + "have filled out all of the necessary fields.", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case MetricHelper.CHOOSER:
            case MetricHelper.CHECK_BOX:
                metricFactory.setDataListIndexValue(list.getValues());
                break;
        }

        mDBManager.getMetricsTable().insert(metricFactory.buildMetric());
        dismiss();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mCurrentSelectedMetricType = position;
        switch (mCurrentSelectedMetricType) {
            case MetricHelper.COUNTER:
                mMinimum.setVisibility(View.VISIBLE);
                mMaximum.setVisibility(View.VISIBLE);
                mIncrementation.setVisibility(View.VISIBLE);
                mListEditor.removeAllViews();
                mListHeader.setVisibility(View.GONE);
                break;
            case MetricHelper.SLIDER:
                mMinimum.setVisibility(View.VISIBLE);
                mMaximum.setVisibility(View.VISIBLE);
                mIncrementation.setVisibility(View.GONE);
                mListEditor.removeAllViews();
                mListHeader.setVisibility(View.GONE);
                break;
            case MetricHelper.CHOOSER:
            case MetricHelper.CHECK_BOX:
                mMinimum.setVisibility(View.GONE);
                mMaximum.setVisibility(View.GONE);
                mIncrementation.setVisibility(View.GONE);
                list = new ListEditor(getActivity());
                mListEditor.removeAllViews();
                mListEditor.addView(list);
                mListHeader.setVisibility(View.VISIBLE);
                break;
            default:
                mMinimum.setVisibility(View.GONE);
                mMaximum.setVisibility(View.GONE);
                mIncrementation.setVisibility(View.GONE);
                mListEditor.removeAllViews();
                mListHeader.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onClick(View v) {
    }

}