package com.team2052.frckrawler.views.metric.impl;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.google.gson.JsonElement;
import com.team2052.frckrawler.R;
import com.team2052.frckrawler.database.MetricHelper;
import com.team2052.frckrawler.database.MetricValue;
import com.team2052.frckrawler.util.Tuple2;
import com.team2052.frckrawler.views.metric.MetricWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adam on 3/28/15.
 */
public class CheckBoxMetricWidget extends MetricWidget {
    private final LinearLayout values;

    public CheckBoxMetricWidget(Context context, MetricValue m) {
        super(context, m);
        inflater.inflate(R.layout.widget_metric_checkbox, this);
        this.values = (LinearLayout) findViewById(R.id.values);
        TextView name = (TextView) findViewById(R.id.name);
        name.setText(m.getMetric().getName());

        final Optional<List<String>> optionalValues = MetricHelper.getListItemIndexRange(m.getMetric());
        if (!optionalValues.isPresent())
            throw new IllegalStateException("Couldn't parse range values, cannot proceed");

        final List<String> rangeValues = optionalValues.get();
        final Tuple2<List<Integer>, MetricHelper.ReturnResult> preLoadedValuesResult = MetricHelper.getListIndexMetricValue(m);

        //Add checkboxes and preloaded values
        for (int i = 0; i < rangeValues.size(); i++) {
            String value = rangeValues.get(i);
            AppCompatCheckBox checkbox = new AppCompatCheckBox(getContext());
            checkbox.setText(value);
            for (Integer integer : preLoadedValuesResult.t1)
                if (i == integer) checkbox.setChecked(true);
            this.values.addView(checkbox);
        }
    }


    @Override
    public JsonElement getData() {
        return MetricHelper.buildListIndexValue(getIndexValues());
    }

    public List<Integer> getIndexValues() {
        ArrayList<Integer> index_values = new ArrayList<>();

        for (int i = 0; i < this.values.getChildCount(); i++) {
            AppCompatCheckBox check_box = (AppCompatCheckBox) this.values.getChildAt(i);
            if (check_box.isChecked()) {
                index_values.add(i);
            }
        }

        return index_values;
    }
}
