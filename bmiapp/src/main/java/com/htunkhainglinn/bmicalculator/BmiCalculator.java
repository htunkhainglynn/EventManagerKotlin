package com.htunkhainglinn.bmicalculator;

/** Pure calculation and validation logic for adult BMI. */
public final class BmiCalculator {
    private static final double KILOGRAMS_PER_POUND = 0.45359237;

    public enum WeightUnit { KILOGRAMS, POUNDS }
    public enum HeightUnit { CENTIMETRES, METRES }
    public enum Category { UNDERWEIGHT, HEALTHY, OVERWEIGHT, OBESE }
    public enum FieldError { NONE, MISSING, NOT_A_NUMBER, OUT_OF_RANGE }

    public static final class Outcome {
        public final double bmi;
        public final Category category;
        public final FieldError weightError;
        public final FieldError heightError;

        private Outcome(double bmi, Category category, FieldError weightError, FieldError heightError) {
            this.bmi = bmi;
            this.category = category;
            this.weightError = weightError;
            this.heightError = heightError;
        }

        public boolean isValid() {
            return weightError == FieldError.NONE && heightError == FieldError.NONE;
        }
    }

    private static final class Measurement {
        final double value;
        final FieldError error;

        Measurement(double value, FieldError error) {
            this.value = value;
            this.error = error;
        }
    }

    private BmiCalculator() { }

    public static Outcome calculate(String weightText, WeightUnit weightUnit,
                                    String heightText, HeightUnit heightUnit) {
        if (weightUnit == null || heightUnit == null) {
            throw new IllegalArgumentException("Units are required");
        }
        Measurement weight = readMeasurement(weightText,
                weightUnit == WeightUnit.KILOGRAMS ? 20 : 44,
                weightUnit == WeightUnit.KILOGRAMS ? 300 : 661);
        Measurement height = readMeasurement(heightText,
                heightUnit == HeightUnit.CENTIMETRES ? 50 : 0.5,
                heightUnit == HeightUnit.CENTIMETRES ? 250 : 2.5);
        if (weight.error != FieldError.NONE || height.error != FieldError.NONE) {
            return new Outcome(Double.NaN, null, weight.error, height.error);
        }

        double kilograms = weightUnit == WeightUnit.KILOGRAMS
                ? weight.value : weight.value * KILOGRAMS_PER_POUND;
        double metres = heightUnit == HeightUnit.METRES
                ? height.value : height.value / 100.0;
        double bmi = kilograms / (metres * metres);
        Category category = bmi < 18.5 ? Category.UNDERWEIGHT
                : bmi < 25.0 ? Category.HEALTHY
                : bmi < 30.0 ? Category.OVERWEIGHT : Category.OBESE;
        return new Outcome(bmi, category, FieldError.NONE, FieldError.NONE);
    }

    private static Measurement readMeasurement(String raw, double minimum, double maximum) {
        String text = raw == null ? "" : raw.trim();
        if (text.isEmpty()) {
            return new Measurement(Double.NaN, FieldError.MISSING);
        }
        // A pasted comma decimal is accepted, but signs, exponents and multiple separators are not.
        if (!text.matches("[0-9]+([.,][0-9]+)?")) {
            return new Measurement(Double.NaN, FieldError.NOT_A_NUMBER);
        }
        double value;
        try {
            value = Double.parseDouble(text.replace(',', '.'));
        } catch (NumberFormatException exception) {
            return new Measurement(Double.NaN, FieldError.NOT_A_NUMBER);
        }
        if (!Double.isFinite(value) || value < minimum || value > maximum) {
            return new Measurement(Double.NaN, FieldError.OUT_OF_RANGE);
        }
        return new Measurement(value, FieldError.NONE);
    }
}
