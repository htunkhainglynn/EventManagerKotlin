package com.htunkhainglinn.bmicalculator;

import static com.htunkhainglinn.bmicalculator.BmiCalculator.Category;
import static com.htunkhainglinn.bmicalculator.BmiCalculator.FieldError;
import static com.htunkhainglinn.bmicalculator.BmiCalculator.HeightUnit;
import static com.htunkhainglinn.bmicalculator.BmiCalculator.WeightUnit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BmiCalculatorTest {
    @Test public void kilogramsAndCentimetresCalculateHealthyBmi() {
        BmiCalculator.Outcome result = BmiCalculator.calculate("70", WeightUnit.KILOGRAMS,
                "175", HeightUnit.CENTIMETRES);
        assertTrue(result.isValid());
        assertEquals(22.86, result.bmi, 0.01);
        assertEquals(Category.HEALTHY, result.category);
    }

    @Test public void poundsAndMetresConvertToSameBmi() {
        BmiCalculator.Outcome result = BmiCalculator.calculate("154.32358353", WeightUnit.POUNDS,
                "1.75", HeightUnit.METRES);
        assertTrue(result.isValid());
        assertEquals(22.86, result.bmi, 0.01);
    }

    @Test public void categoryBoundariesBelongToHigherBand() {
        assertEquals(Category.UNDERWEIGHT, calculateAtTwoMetres("73.96").category);
        assertEquals(Category.HEALTHY, calculateAtTwoMetres("74").category);
        assertEquals(Category.OVERWEIGHT, calculateAtTwoMetres("100").category);
        assertEquals(Category.OBESE, calculateAtTwoMetres("120").category);
    }

    @Test public void missingAndInvalidFieldsPreventCalculation() {
        BmiCalculator.Outcome result = BmiCalculator.calculate("abc", WeightUnit.KILOGRAMS,
                "", HeightUnit.CENTIMETRES);
        assertFalse(result.isValid());
        assertEquals(FieldError.NOT_A_NUMBER, result.weightError);
        assertEquals(FieldError.MISSING, result.heightError);
        assertTrue(Double.isNaN(result.bmi));
    }

    @Test public void selectedUnitRangesAreEnforced() {
        BmiCalculator.Outcome result = BmiCalculator.calculate("70", WeightUnit.KILOGRAMS,
                "175", HeightUnit.METRES);
        assertFalse(result.isValid());
        assertEquals(FieldError.OUT_OF_RANGE, result.heightError);
        assertEquals(FieldError.NONE, result.weightError);
    }

    @Test public void commaDecimalIsAccepted() {
        BmiCalculator.Outcome result = BmiCalculator.calculate("70,5", WeightUnit.KILOGRAMS,
                "1,75", HeightUnit.METRES);
        assertTrue(result.isValid());
        assertEquals(23.02, result.bmi, 0.01);
    }

    private static BmiCalculator.Outcome calculateAtTwoMetres(String kilograms) {
        return BmiCalculator.calculate(kilograms, WeightUnit.KILOGRAMS,
                "2", HeightUnit.METRES);
    }
}
