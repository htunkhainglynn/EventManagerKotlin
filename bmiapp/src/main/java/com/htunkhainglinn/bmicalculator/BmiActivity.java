package com.htunkhainglinn.bmicalculator;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public final class BmiActivity extends Activity {
    private EditText weightInput;
    private EditText heightInput;
    private RadioGroup weightUnits;
    private RadioGroup heightUnits;
    private View resultPanel;
    private TextView resultStatus;
    private TextView bmiValue;
    private TextView categoryValue;
    private TextView categoryBand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi);

        View root = findViewById(R.id.root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        weightInput = findViewById(R.id.weight_input);
        heightInput = findViewById(R.id.height_input);
        weightUnits = findViewById(R.id.weight_units);
        heightUnits = findViewById(R.id.height_units);
        resultPanel = findViewById(R.id.result_panel);
        resultStatus = findViewById(R.id.result_status);
        bmiValue = findViewById(R.id.bmi_value);
        categoryValue = findViewById(R.id.category_value);
        categoryBand = findViewById(R.id.category_band);
        Button calculateButton = findViewById(R.id.calculate_button);

        TextWatcher clearOnEdit = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence text, int start, int before, int count) {
                clearResult();
            }
            @Override public void afterTextChanged(Editable text) { }
        };
        weightInput.addTextChangedListener(clearOnEdit);
        heightInput.addTextChangedListener(clearOnEdit);
        weightUnits.setOnCheckedChangeListener((group, checkedId) -> clearResult());
        heightUnits.setOnCheckedChangeListener((group, checkedId) -> clearResult());
        calculateButton.setOnClickListener(view -> calculate());
    }

    private void clearResult() {
        resultPanel.setVisibility(View.GONE);
        resultStatus.setVisibility(View.GONE);
        weightInput.setError(null);
        heightInput.setError(null);
    }

    private void calculate() {
        BmiCalculator.WeightUnit weightUnit = weightUnits.getCheckedRadioButtonId() == R.id.unit_lb
                ? BmiCalculator.WeightUnit.POUNDS : BmiCalculator.WeightUnit.KILOGRAMS;
        BmiCalculator.HeightUnit heightUnit = heightUnits.getCheckedRadioButtonId() == R.id.unit_m
                ? BmiCalculator.HeightUnit.METRES : BmiCalculator.HeightUnit.CENTIMETRES;
        BmiCalculator.Outcome outcome = BmiCalculator.calculate(
                weightInput.getText().toString(), weightUnit,
                heightInput.getText().toString(), heightUnit);

        if (!outcome.isValid()) {
            resultPanel.setVisibility(View.GONE);
            showFieldError(weightInput, outcome.weightError,
                    weightUnit == BmiCalculator.WeightUnit.KILOGRAMS
                            ? R.string.weight_range_kg : R.string.weight_range_lb);
            showFieldError(heightInput, outcome.heightError,
                    heightUnit == BmiCalculator.HeightUnit.CENTIMETRES
                            ? R.string.height_range_cm : R.string.height_range_m);
            resultStatus.setText(R.string.correct_inputs);
            resultStatus.setVisibility(View.VISIBLE);
            return;
        }

        weightInput.setError(null);
        heightInput.setError(null);
        resultStatus.setVisibility(View.GONE);
        bmiValue.setText(String.format(Locale.getDefault(), "%.2f", outcome.bmi));
        int label;
        int band;
        int color;
        switch (outcome.category) {
            case UNDERWEIGHT:
                label = R.string.category_underweight;
                band = R.string.band_underweight;
                color = R.color.category_underweight;
                break;
            case HEALTHY:
                label = R.string.category_healthy;
                band = R.string.band_healthy;
                color = R.color.category_healthy;
                break;
            case OVERWEIGHT:
                label = R.string.category_overweight;
                band = R.string.band_overweight;
                color = R.color.category_overweight;
                break;
            default:
                label = R.string.category_obese;
                band = R.string.band_obese;
                color = R.color.category_obese;
        }
        categoryValue.setText(label);
        categoryValue.setTextColor(getColor(color));
        categoryBand.setText(band);
        resultPanel.setVisibility(View.VISIBLE);
    }

    private void showFieldError(EditText field, BmiCalculator.FieldError error, int rangeMessage) {
        switch (error) {
            case MISSING: field.setError(getString(R.string.required_field)); break;
            case NOT_A_NUMBER: field.setError(getString(R.string.valid_number)); break;
            case OUT_OF_RANGE: field.setError(getString(rangeMessage)); break;
            default: field.setError(null);
        }
    }
}
