/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.math3.special;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.math3.TestUtils;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

/**
 * @version $Id: BetaTest.java 1546350 2013-11-28 11:41:12Z erans $
 */
public class BetaTest {

    /*
     * Use reflection to test private methods.
     */
    private static final Method LOG_GAMMA_SUM_METHOD;

    private static final Method LOG_GAMMA_MINUS_LOG_GAMMA_SUM_METHOD;

    private static final Method SUM_DELTA_MINUS_DELTA_SUM_METHOD;

    static {
        final Class<Beta> b;
        final Class<Double> d = Double.TYPE;
        b = Beta.class;
        Method m = null;
        try {
            m = b.getDeclaredMethod("logGammaSum", d, d);
        } catch (NoSuchMethodException e) {
            Assert.fail(e.getMessage());
        }
        LOG_GAMMA_SUM_METHOD = m;
        LOG_GAMMA_SUM_METHOD.setAccessible(true);

        m = null;
        try {
            m = b.getDeclaredMethod("logGammaMinusLogGammaSum",d, d);
        } catch (NoSuchMethodException e) {
            Assert.fail(e.getMessage());
        }
        LOG_GAMMA_MINUS_LOG_GAMMA_SUM_METHOD = m;
        LOG_GAMMA_MINUS_LOG_GAMMA_SUM_METHOD.setAccessible(true);

        m = null;
        try {
            m = b.getDeclaredMethod("sumDeltaMinusDeltaSum",d, d);
        } catch (NoSuchMethodException e) {
            Assert.fail(e.getMessage());
        }
        SUM_DELTA_MINUS_DELTA_SUM_METHOD = m;
        SUM_DELTA_MINUS_DELTA_SUM_METHOD.setAccessible(true);
    }

    private void testRegularizedBeta(double expected, double x,
                                     double a, double b) {
        double actual = Beta.regularizedBeta(x, a, b);
        TestUtils.assertEquals(expected, actual, 10e-15);
    }

    private void testLogBeta(double expected, double a, double b) {
        double actual = Beta.logBeta(a, b);
        TestUtils.assertEquals(expected, actual, 10e-15);
    }

    @Test
    public void testRegularizedBetaNanPositivePositive() {
        testRegularizedBeta(Double.NaN, Double.NaN, 1.0, 1.0);
    }

    @Test
    public void testRegularizedBetaPositiveNanPositive() {
        testRegularizedBeta(Double.NaN, 0.5, Double.NaN, 1.0);
    }

    @Test
    public void testRegularizedBetaPositivePositiveNan() {
        testRegularizedBeta(Double.NaN, 0.5, 1.0, Double.NaN);
    }

    @Test
    public void testRegularizedBetaNegativePositivePositive() {
        testRegularizedBeta(Double.NaN, -0.5, 1.0, 2.0);
    }

    @Test
    public void testRegularizedBetaPositiveNegativePositive() {
        testRegularizedBeta(Double.NaN, 0.5, -1.0, 2.0);
    }

    @Test
    public void testRegularizedBetaPositivePositiveNegative() {
        testRegularizedBeta(Double.NaN, 0.5, 1.0, -2.0);
    }

    @Test
    public void testRegularizedBetaZeroPositivePositive() {
        testRegularizedBeta(0.0, 0.0, 1.0, 2.0);
    }

    @Test
    public void testRegularizedBetaPositiveZeroPositive() {
        testRegularizedBeta(Double.NaN, 0.5, 0.0, 2.0);
    }

    @Test
    public void testRegularizedBetaPositivePositiveZero() {
        testRegularizedBeta(Double.NaN, 0.5, 1.0, 0.0);
    }

    @Test
    public void testRegularizedBetaPositivePositivePositive() {
        testRegularizedBeta(0.75, 0.5, 1.0, 2.0);
    }

    @Test
    public void testRegularizedBetaTinyArgument() {
        double actual = Beta.regularizedBeta(1e-17, 1.0, 1e12);
        // This value is from R: pbeta(1e-17,1,1e12)
        TestUtils.assertEquals(9.999950000166648e-6, actual, 1e-16);
    }

    @Test
    public void testMath1067() {
        final double x = 0.22580645161290325;
        final double a = 64.33333333333334;
        final double b = 223;

        try {
            final double r = Beta.regularizedBeta(x, a, b, 1e-14, 10000);
        } catch (StackOverflowError error) {
            Assert.fail("Infinite recursion");
        }
    }

    @Test
    public void testLogBetaNanPositive() {
        testLogBeta(Double.NaN, Double.NaN, 2.0);
    }

    @Test
    public void testLogBetaPositiveNan() {
        testLogBeta(Double.NaN, 1.0, Double.NaN);
    }

    @Test
    public void testLogBetaNegativePositive() {
        testLogBeta(Double.NaN, -1.0, 2.0);
    }

    @Test
    public void testLogBetaPositiveNegative() {
        testLogBeta(Double.NaN, 1.0, -2.0);
    }

    @Test
    public void testLogBetaZeroPositive() {
        testLogBeta(Double.NaN, 0.0, 2.0);
    }

    @Test
    public void testLogBetaPositiveZero() {
        testLogBeta(Double.NaN, 1.0, 0.0);
    }

    @Test
    public void testLogBetaPositivePositive() {
        testLogBeta(-0.693147180559945, 1.0, 2.0);
    }

    /**
     * Reference data for the {@link #logGammaSum(double, double)}
     * function. This data was generated with the following
     * <a href="http://maxima.sourceforge.net/">Maxima</a> script.
     *
     * <pre>
     * kill(all);
     *
     * fpprec : 64;
     * gsumln(a, b) := log(gamma(a + b));
     *
     * x : [1.0b0, 1.125b0, 1.25b0, 1.375b0, 1.5b0, 1.625b0, 1.75b0, 1.875b0, 2.0b0];
     *
     * for i : 1 while i <= length(x) do
     *   for j : 1 while j <= length(x) do block(
     *     a : x[i],
     *     b : x[j],
     *     print("{", float(a), ",", float(b), ",", float(gsumln(a, b)), "},")
     *   );
     * </pre>
     */
    private static final double[][] LOG_GAMMA_SUM_REF = {
        { 1.0 , 1.0 , 0.0 },
        { 1.0 , 1.125 , .05775985153034387 },
        { 1.0 , 1.25 , .1248717148923966 },
        { 1.0 , 1.375 , .2006984603774558 },
        { 1.0 , 1.5 , .2846828704729192 },
        { 1.0 , 1.625 , .3763336820249054 },
        { 1.0 , 1.75 , .4752146669149371 },
        { 1.0 , 1.875 , .5809359740231859 },
        { 1.0 , 2.0 , .6931471805599453 },
        { 1.125 , 1.0 , .05775985153034387 },
        { 1.125 , 1.125 , .1248717148923966 },
        { 1.125 , 1.25 , .2006984603774558 },
        { 1.125 , 1.375 , .2846828704729192 },
        { 1.125 , 1.5 , .3763336820249054 },
        { 1.125 , 1.625 , .4752146669149371 },
        { 1.125 , 1.75 , .5809359740231859 },
        { 1.125 , 1.875 , .6931471805599453 },
        { 1.125 , 2.0 , 0.811531653906724 },
        { 1.25 , 1.0 , .1248717148923966 },
        { 1.25 , 1.125 , .2006984603774558 },
        { 1.25 , 1.25 , .2846828704729192 },
        { 1.25 , 1.375 , .3763336820249054 },
        { 1.25 , 1.5 , .4752146669149371 },
        { 1.25 , 1.625 , .5809359740231859 },
        { 1.25 , 1.75 , .6931471805599453 },
        { 1.25 , 1.875 , 0.811531653906724 },
        { 1.25 , 2.0 , .9358019311087253 },
        { 1.375 , 1.0 , .2006984603774558 },
        { 1.375 , 1.125 , .2846828704729192 },
        { 1.375 , 1.25 , .3763336820249054 },
        { 1.375 , 1.375 , .4752146669149371 },
        { 1.375 , 1.5 , .5809359740231859 },
        { 1.375 , 1.625 , .6931471805599453 },
        { 1.375 , 1.75 , 0.811531653906724 },
        { 1.375 , 1.875 , .9358019311087253 },
        { 1.375 , 2.0 , 1.06569589786406 },
        { 1.5 , 1.0 , .2846828704729192 },
        { 1.5 , 1.125 , .3763336820249054 },
        { 1.5 , 1.25 , .4752146669149371 },
        { 1.5 , 1.375 , .5809359740231859 },
        { 1.5 , 1.5 , .6931471805599453 },
        { 1.5 , 1.625 , 0.811531653906724 },
        { 1.5 , 1.75 , .9358019311087253 },
        { 1.5 , 1.875 , 1.06569589786406 },
        { 1.5 , 2.0 , 1.200973602347074 },
        { 1.625 , 1.0 , .3763336820249054 },
        { 1.625 , 1.125 , .4752146669149371 },
        { 1.625 , 1.25 , .5809359740231859 },
        { 1.625 , 1.375 , .6931471805599453 },
        { 1.625 , 1.5 , 0.811531653906724 },
        { 1.625 , 1.625 , .9358019311087253 },
        { 1.625 , 1.75 , 1.06569589786406 },
        { 1.625 , 1.875 , 1.200973602347074 },
        { 1.625 , 2.0 , 1.341414578068493 },
        { 1.75 , 1.0 , .4752146669149371 },
        { 1.75 , 1.125 , .5809359740231859 },
        { 1.75 , 1.25 , .6931471805599453 },
        { 1.75 , 1.375 , 0.811531653906724 },
        { 1.75 , 1.5 , .9358019311087253 },
        { 1.75 , 1.625 , 1.06569589786406 },
        { 1.75 , 1.75 , 1.200973602347074 },
        { 1.75 , 1.875 , 1.341414578068493 },
        { 1.75 , 2.0 , 1.486815578593417 },
        { 1.875 , 1.0 , .5809359740231859 },
        { 1.875 , 1.125 , .6931471805599453 },
        { 1.875 , 1.25 , 0.811531653906724 },
        { 1.875 , 1.375 , .9358019311087253 },
        { 1.875 , 1.5 , 1.06569589786406 },
        { 1.875 , 1.625 , 1.200973602347074 },
        { 1.875 , 1.75 , 1.341414578068493 },
        { 1.875 , 1.875 , 1.486815578593417 },
        { 1.875 , 2.0 , 1.6369886482725 },
        { 2.0 , 1.0 , .6931471805599453 },
        { 2.0 , 1.125 , 0.811531653906724 },
        { 2.0 , 1.25 , .9358019311087253 },
        { 2.0 , 1.375 , 1.06569589786406 },
        { 2.0 , 1.5 , 1.200973602347074 },
        { 2.0 , 1.625 , 1.341414578068493 },
        { 2.0 , 1.75 , 1.486815578593417 },
        { 2.0 , 1.875 , 1.6369886482725 },
        { 2.0 , 2.0 , 1.791759469228055 },
    };

    private static double logGammaSum(final double a, final double b) {

        /*
         * Use reflection to access private method.
         */
        try {
            return ((Double) LOG_GAMMA_SUM_METHOD.invoke(null, a, b)).doubleValue();
        } catch (final IllegalAccessException e) {
            Assert.fail(e.getMessage());
        } catch (final IllegalArgumentException e) {
            Assert.fail(e.getMessage());
        } catch (final InvocationTargetException e) {
            final Throwable te = e.getTargetException();
            if (te instanceof MathIllegalArgumentException) {
                throw (MathIllegalArgumentException) te;
            }
            Assert.fail(e.getMessage());
        }
        return Double.NaN;
    }

    @Test
    public void testLogGammaSum() {
        final int ulps = 2;
        for (int i = 0; i < LOG_GAMMA_SUM_REF.length; i++) {
            final double[] ref = LOG_GAMMA_SUM_REF[i];
            final double a = ref[0];
            final double b = ref[1];
            final double expected = ref[2];
            final double actual = logGammaSum(a, b);
            final double tol = ulps * FastMath.ulp(expected);
            final StringBuilder builder = new StringBuilder();
            builder.append(a).append(", ").append(b);
            Assert.assertEquals(builder.toString(), expected, actual, tol);
        }
    }

    @Test(expected = OutOfRangeException.class)
    public void testLogGammaSumPrecondition1() {

        logGammaSum(0.0, 1.0);
    }

    @Test(expected = OutOfRangeException.class)
    public void testLogGammaSumPrecondition2() {

        logGammaSum(3.0, 1.0);
    }

    @Test(expected = OutOfRangeException.class)
    public void testLogGammaSumPrecondition3() {

        logGammaSum(1.0, 0.0);
    }

    @Test(expected = OutOfRangeException.class)
    public void testLogGammaSumPrecondition4() {

        logGammaSum(1.0, 3.0);
    }

    private static final double[][] LOG_GAMMA_MINUS_LOG_GAMMA_SUM_REF = {
//        { 0.0 , 8.0 , 0.0 },
//        { 0.0 , 9.0 , 0.0 },
        { 0.0 , 10.0 , 0.0 },
        { 0.0 , 11.0 , 0.0 },
        { 0.0 , 12.0 , 0.0 },
        { 0.0 , 13.0 , 0.0 },
        { 0.0 , 14.0 , 0.0 },
        { 0.0 , 15.0 , 0.0 },
        { 0.0 , 16.0 , 0.0 },
        { 0.0 , 17.0 , 0.0 },
        { 0.0 , 18.0 , 0.0 },
//        { 1.0 , 8.0 , - 2.079441541679836 },
//        { 1.0 , 9.0 , - 2.19722457733622 },
        { 1.0 , 10.0 , - 2.302585092994046 },
        { 1.0 , 11.0 , - 2.397895272798371 },
        { 1.0 , 12.0 , - 2.484906649788 },
        { 1.0 , 13.0 , - 2.564949357461537 },
        { 1.0 , 14.0 , - 2.639057329615258 },
        { 1.0 , 15.0 , - 2.70805020110221 },
        { 1.0 , 16.0 , - 2.772588722239781 },
        { 1.0 , 17.0 , - 2.833213344056216 },
        { 1.0 , 18.0 , - 2.890371757896165 },
//        { 2.0 , 8.0 , - 4.276666119016055 },
//        { 2.0 , 9.0 , - 4.499809670330265 },
        { 2.0 , 10.0 , - 4.700480365792417 },
        { 2.0 , 11.0 , - 4.882801922586371 },
        { 2.0 , 12.0 , - 5.049856007249537 },
        { 2.0 , 13.0 , - 5.204006687076795 },
        { 2.0 , 14.0 , - 5.347107530717468 },
        { 2.0 , 15.0 , - 5.480638923341991 },
        { 2.0 , 16.0 , - 5.605802066295998 },
        { 2.0 , 17.0 , - 5.723585101952381 },
        { 2.0 , 18.0 , - 5.834810737062605 },
//        { 3.0 , 8.0 , - 6.579251212010101 },
//        { 3.0 , 9.0 , - 6.897704943128636 },
        { 3.0 , 10.0 , - 7.185387015580416 },
        { 3.0 , 11.0 , - 7.447751280047908 },
        { 3.0 , 12.0 , - 7.688913336864796 },
        { 3.0 , 13.0 , - 7.912056888179006 },
        { 3.0 , 14.0 , - 8.11969625295725 },
        { 3.0 , 15.0 , - 8.313852267398207 },
        { 3.0 , 16.0 , - 8.496173824192162 },
        { 3.0 , 17.0 , - 8.668024081118821 },
        { 3.0 , 18.0 , - 8.830543010616596 },
//        { 4.0 , 8.0 , - 8.977146484808472 },
//        { 4.0 , 9.0 , - 9.382611592916636 },
        { 4.0 , 10.0 , - 9.750336373041954 },
        { 4.0 , 11.0 , - 10.08680860966317 },
        { 4.0 , 12.0 , - 10.39696353796701 },
        { 4.0 , 13.0 , - 10.68464561041879 },
        { 4.0 , 14.0 , - 10.95290959701347 },
        { 4.0 , 15.0 , - 11.20422402529437 },
        { 4.0 , 16.0 , - 11.4406128033586 },
        { 4.0 , 17.0 , - 11.66375635467281 },
        { 4.0 , 18.0 , - 11.87506544834002 },
//        { 5.0 , 8.0 , - 11.46205313459647 },
//        { 5.0 , 9.0 , - 11.94756095037817 },
        { 5.0 , 10.0 , - 12.38939370265721 },
        { 5.0 , 11.0 , - 12.79485881076538 },
        { 5.0 , 12.0 , - 13.16955226020679 },
        { 5.0 , 13.0 , - 13.517858954475 },
        { 5.0 , 14.0 , - 13.84328135490963 },
        { 5.0 , 15.0 , - 14.14866300446081 },
        { 5.0 , 16.0 , - 14.43634507691259 },
        { 5.0 , 17.0 , - 14.70827879239624 },
        { 5.0 , 18.0 , - 14.96610790169833 },
//        { 6.0 , 8.0 , - 14.02700249205801 },
//        { 6.0 , 9.0 , - 14.58661827999343 },
        { 6.0 , 10.0 , - 15.09744390375942 },
        { 6.0 , 11.0 , - 15.56744753300516 },
        { 6.0 , 12.0 , - 16.002765604263 },
        { 6.0 , 13.0 , - 16.40823071237117 },
        { 6.0 , 14.0 , - 16.78772033407607 },
        { 6.0 , 15.0 , - 17.14439527801481 },
        { 6.0 , 16.0 , - 17.48086751463602 },
        { 6.0 , 17.0 , - 17.79932124575455 },
        { 6.0 , 18.0 , - 18.10160211762749 },
//        { 7.0 , 8.0 , - 16.66605982167327 },
//        { 7.0 , 9.0 , - 17.29466848109564 },
        { 7.0 , 10.0 , - 17.8700326259992 },
        { 7.0 , 11.0 , - 18.40066087706137 },
        { 7.0 , 12.0 , - 18.89313736215917 },
        { 7.0 , 13.0 , - 19.35266969153761 },
        { 7.0 , 14.0 , - 19.78345260763006 },
        { 7.0 , 15.0 , - 20.18891771573823 },
        { 7.0 , 16.0 , - 20.57190996799433 },
        { 7.0 , 17.0 , - 20.9348154616837 },
        { 7.0 , 18.0 , - 21.27965594797543 },
//        { 8.0 , 8.0 , - 19.37411002277548 },
//        { 8.0 , 9.0 , - 20.06725720333542 },
        { 8.0 , 10.0 , - 20.70324597005542 },
        { 8.0 , 11.0 , - 21.29103263495754 },
        { 8.0 , 12.0 , - 21.83757634132561 },
        { 8.0 , 13.0 , - 22.3484019650916 },
        { 8.0 , 14.0 , - 22.82797504535349 },
        { 8.0 , 15.0 , - 23.27996016909654 },
        { 8.0 , 16.0 , - 23.70740418392348 },
        { 8.0 , 17.0 , - 24.11286929203165 },
        { 8.0 , 18.0 , - 24.49853177284363 },
//        { 9.0 , 8.0 , - 22.14669874501526 },
//        { 9.0 , 9.0 , - 22.90047054739164 },
        { 9.0 , 10.0 , - 23.59361772795159 },
        { 9.0 , 11.0 , - 24.23547161412398 },
        { 9.0 , 12.0 , - 24.8333086148796 },
        { 9.0 , 13.0 , - 25.39292440281502 },
        { 9.0 , 14.0 , - 25.9190174987118 },
        { 9.0 , 15.0 , - 26.41545438502569 },
        { 9.0 , 16.0 , - 26.88545801427143 },
        { 9.0 , 17.0 , - 27.33174511689985 },
        { 9.0 , 18.0 , - 27.75662831086511 },
//        { 10.0 , 8.0 , - 24.97991208907148 },
//        { 10.0 , 9.0 , - 25.7908423052878 },
        { 10.0 , 10.0 , - 26.53805670711802 },
        { 10.0 , 11.0 , - 27.23120388767797 },
        { 10.0 , 12.0 , - 27.87783105260302 },
        { 10.0 , 13.0 , - 28.48396685617334 },
        { 10.0 , 14.0 , - 29.05451171464095 },
        { 10.0 , 15.0 , - 29.59350821537364 },
        { 10.0 , 16.0 , - 30.10433383913963 },
        { 10.0 , 17.0 , - 30.58984165492133 },
        { 10.0 , 18.0 , - 31.05246517686944 },
    };

    private static double logGammaMinusLogGammaSum(final double a, final double b) {

        /*
         * Use reflection to access private method.
         */
        try {
            final Method m = LOG_GAMMA_MINUS_LOG_GAMMA_SUM_METHOD;
            return ((Double) m.invoke(null, a, b)).doubleValue();
        } catch (final IllegalAccessException e) {
            Assert.fail(e.getMessage());
        } catch (final IllegalArgumentException e) {
            Assert.fail(e.getMessage());
        } catch (final InvocationTargetException e) {
            final Throwable te = e.getTargetException();
            if (te instanceof MathIllegalArgumentException) {
                throw (MathIllegalArgumentException) te;
            }
            Assert.fail(e.getMessage());
        }
        return Double.NaN;
    }

    @Test
    public void testLogGammaMinusLogGammaSum() {
        final int ulps = 4;
        for (int i = 0; i < LOG_GAMMA_MINUS_LOG_GAMMA_SUM_REF.length; i++) {
            final double[] ref = LOG_GAMMA_MINUS_LOG_GAMMA_SUM_REF[i];
            final double a = ref[0];
            final double b = ref[1];
            final double expected = ref[2];
            final double actual = logGammaMinusLogGammaSum(a, b);
            final double tol = ulps * FastMath.ulp(expected);
            final StringBuilder builder = new StringBuilder();
            builder.append(a).append(", ").append(b);
            Assert.assertEquals(builder.toString(), expected, actual, tol);
        }
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testLogGammaMinusLogGammaSumPrecondition1() {
        logGammaMinusLogGammaSum(-1.0, 8.0);
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testLogGammaMinusLogGammaSumPrecondition2() {
        logGammaMinusLogGammaSum(1.0, 7.0);
    }

    private static final double[][] SUM_DELTA_MINUS_DELTA_SUM_REF = {
        { 10.0 , 10.0 , .01249480717472882 },
        { 10.0 , 11.0 , .01193628470267385 },
        { 10.0 , 12.0 , .01148578547212797 },
        { 10.0 , 13.0 , .01111659739668398 },
        { 10.0 , 14.0 , .01080991216314295 },
        { 10.0 , 15.0 , .01055214134859758 },
        { 10.0 , 16.0 , .01033324912491747 },
        { 10.0 , 17.0 , .01014568069918883 },
        { 10.0 , 18.0 , .009983653199146491 },
        { 10.0 , 19.0 , .009842674320242729 },
        { 10.0 , 20.0 , 0.0097192081956071 },
        { 11.0 , 10.0 , .01193628470267385 },
        { 11.0 , 11.0 , .01135973290745925 },
        { 11.0 , 12.0 , .01089355537047828 },
        { 11.0 , 13.0 , .01051064829297728 },
        { 11.0 , 14.0 , 0.0101918899639826 },
        { 11.0 , 15.0 , .009923438811859604 },
        { 11.0 , 16.0 , .009695052724952705 },
        { 11.0 , 17.0 , 0.00949900745283617 },
        { 11.0 , 18.0 , .009329379874933402 },
        { 11.0 , 19.0 , 0.00918156080743147 },
        { 11.0 , 20.0 , 0.00905191635141762 },
        { 12.0 , 10.0 , .01148578547212797 },
        { 12.0 , 11.0 , .01089355537047828 },
        { 12.0 , 12.0 , .01041365883144029 },
        { 12.0 , 13.0 , .01001867865848564 },
        { 12.0 , 14.0 , 0.00968923999191334 },
        { 12.0 , 15.0 , .009411294976563555 },
        { 12.0 , 16.0 , .009174432043268762 },
        { 12.0 , 17.0 , .008970786693291802 },
        { 12.0 , 18.0 , .008794318926790865 },
        { 12.0 , 19.0 , .008640321527910711 },
        { 12.0 , 20.0 , .008505077879954796 },
        { 13.0 , 10.0 , .01111659739668398 },
        { 13.0 , 11.0 , .01051064829297728 },
        { 13.0 , 12.0 , .01001867865848564 },
        { 13.0 , 13.0 , .009613018147953376 },
        { 13.0 , 14.0 , .009274085618154277 },
        { 13.0 , 15.0 , 0.0089876637564166 },
        { 13.0 , 16.0 , .008743200745261382 },
        { 13.0 , 17.0 , .008532715206686251 },
        { 13.0 , 18.0 , .008350069108807093 },
        { 13.0 , 19.0 , .008190472517984874 },
        { 13.0 , 20.0 , .008050138630244345 },
        { 14.0 , 10.0 , .01080991216314295 },
        { 14.0 , 11.0 , 0.0101918899639826 },
        { 14.0 , 12.0 , 0.00968923999191334 },
        { 14.0 , 13.0 , .009274085618154277 },
        { 14.0 , 14.0 , .008926676241967286 },
        { 14.0 , 15.0 , .008632654302369184 },
        { 14.0 , 16.0 , .008381351102615795 },
        { 14.0 , 17.0 , .008164687232662443 },
        { 14.0 , 18.0 , .007976441942841219 },
        { 14.0 , 19.0 , .007811755112234388 },
        { 14.0 , 20.0 , .007666780069317652 },
        { 15.0 , 10.0 , .01055214134859758 },
        { 15.0 , 11.0 , .009923438811859604 },
        { 15.0 , 12.0 , .009411294976563555 },
        { 15.0 , 13.0 , 0.0089876637564166 },
        { 15.0 , 14.0 , .008632654302369184 },
        { 15.0 , 15.0 , 0.00833179217417291 },
        { 15.0 , 16.0 , .008074310643041299 },
        { 15.0 , 17.0 , .007852047581145882 },
        { 15.0 , 18.0 , .007658712051540045 },
        { 15.0 , 19.0 , .007489384065757007 },
        { 15.0 , 20.0 , .007340165635725612 },
        { 16.0 , 10.0 , .01033324912491747 },
        { 16.0 , 11.0 , .009695052724952705 },
        { 16.0 , 12.0 , .009174432043268762 },
        { 16.0 , 13.0 , .008743200745261382 },
        { 16.0 , 14.0 , .008381351102615795 },
        { 16.0 , 15.0 , .008074310643041299 },
        { 16.0 , 16.0 , .007811229919967624 },
        { 16.0 , 17.0 , .007583876618287594 },
        { 16.0 , 18.0 , .007385899933505551 },
        { 16.0 , 19.0 , .007212328560607852 },
        { 16.0 , 20.0 , .007059220321091879 },
        { 17.0 , 10.0 , .01014568069918883 },
        { 17.0 , 11.0 , 0.00949900745283617 },
        { 17.0 , 12.0 , .008970786693291802 },
        { 17.0 , 13.0 , .008532715206686251 },
        { 17.0 , 14.0 , .008164687232662443 },
        { 17.0 , 15.0 , .007852047581145882 },
        { 17.0 , 16.0 , .007583876618287594 },
        { 17.0 , 17.0 , .007351882161431358 },
        { 17.0 , 18.0 , .007149662089534654 },
        { 17.0 , 19.0 , .006972200907152378 },
        { 17.0 , 20.0 , .006815518216094137 },
        { 18.0 , 10.0 , .009983653199146491 },
        { 18.0 , 11.0 , .009329379874933402 },
        { 18.0 , 12.0 , .008794318926790865 },
        { 18.0 , 13.0 , .008350069108807093 },
        { 18.0 , 14.0 , .007976441942841219 },
        { 18.0 , 15.0 , .007658712051540045 },
        { 18.0 , 16.0 , .007385899933505551 },
        { 18.0 , 17.0 , .007149662089534654 },
        { 18.0 , 18.0 , .006943552208153373 },
        { 18.0 , 19.0 , .006762516574228829 },
        { 18.0 , 20.0 , .006602541598043117 },
        { 19.0 , 10.0 , .009842674320242729 },
        { 19.0 , 11.0 , 0.00918156080743147 },
        { 19.0 , 12.0 , .008640321527910711 },
        { 19.0 , 13.0 , .008190472517984874 },
        { 19.0 , 14.0 , .007811755112234388 },
        { 19.0 , 15.0 , .007489384065757007 },
        { 19.0 , 16.0 , .007212328560607852 },
        { 19.0 , 17.0 , .006972200907152378 },
        { 19.0 , 18.0 , .006762516574228829 },
        { 19.0 , 19.0 , .006578188655176814 },
        { 19.0 , 20.0 , .006415174623476747 },
        { 20.0 , 10.0 , 0.0097192081956071 },
        { 20.0 , 11.0 , 0.00905191635141762 },
        { 20.0 , 12.0 , .008505077879954796 },
        { 20.0 , 13.0 , .008050138630244345 },
        { 20.0 , 14.0 , .007666780069317652 },
        { 20.0 , 15.0 , .007340165635725612 },
        { 20.0 , 16.0 , .007059220321091879 },
        { 20.0 , 17.0 , .006815518216094137 },
        { 20.0 , 18.0 , .006602541598043117 },
        { 20.0 , 19.0 , .006415174623476747 },
        { 20.0 , 20.0 , .006249349445691423 },
    };

    private static double sumDeltaMinusDeltaSum(final double a,
                                                final double b) {

        /*
         * Use reflection to access private method.
         */
        try {
            final Method m = SUM_DELTA_MINUS_DELTA_SUM_METHOD;
            return ((Double) m.invoke(null, a, b)).doubleValue();
        } catch (final IllegalAccessException e) {
            Assert.fail(e.getMessage());
        } catch (final IllegalArgumentException e) {
            Assert.fail(e.getMessage());
        } catch (final InvocationTargetException e) {
            final Throwable te = e.getTargetException();
            if (te instanceof MathIllegalArgumentException) {
                throw (MathIllegalArgumentException) te;
            }
            Assert.fail(e.getMessage());
        }
        return Double.NaN;
    }

    @Test
    public void testSumDeltaMinusDeltaSum() {

        final int ulps = 3;
        for (int i = 0; i < SUM_DELTA_MINUS_DELTA_SUM_REF.length; i++) {
            final double[] ref = SUM_DELTA_MINUS_DELTA_SUM_REF[i];
            final double a = ref[0];
            final double b = ref[1];
            final double expected = ref[2];
            final double actual = sumDeltaMinusDeltaSum(a, b);
            final double tol = ulps * FastMath.ulp(expected);
            final StringBuilder builder = new StringBuilder();
            builder.append(a).append(", ").append(b);
            Assert.assertEquals(builder.toString(), expected, actual, tol);
        }
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testSumDeltaMinusDeltaSumPrecondition1() {

        sumDeltaMinusDeltaSum(9.0, 10.0);
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testSumDeltaMinusDeltaSumPrecondition2() {

        sumDeltaMinusDeltaSum(10.0, 9.0);
    }

    private static final double[][] LOG_BETA_REF = {
        { 0.125 , 0.125 , 2.750814190409515 },
        { 0.125 , 0.25 , 2.444366899981226 },
        { 0.125 , 0.5 , 2.230953804989556 },
        { 0.125 , 1.0 , 2.079441541679836 },
        { 0.125 , 2.0 , 1.961658506023452 },
        { 0.125 , 3.0 , 1.901033884207018 },
        { 0.125 , 4.0 , 1.860211889686763 },
        { 0.125 , 5.0 , 1.829440231020009 },
        { 0.125 , 6.0 , 1.804747618429637 },
        { 0.125 , 7.0 , 1.784128331226902 },
        { 0.125 , 8.0 , 1.766428754127501 },
        { 0.125 , 9.0 , 1.750924567591535 },
        { 0.125 , 10.0 , 1.7371312454592 },
        { 0.125 , 1000.0 , 1.156003642015969 },
        { 0.125 , 1001.0 , 1.155878649827818 },
        { 0.125 , 10000.0 , .8681312798751318 },
        { 0.25 , 0.125 , 2.444366899981226 },
        { 0.25 , 0.25 , 2.003680106471455 },
        { 0.25 , 0.5 , 1.657106516191482 },
        { 0.25 , 1.0 , 1.386294361119891 },
        { 0.25 , 2.0 , 1.163150809805681 },
        { 0.25 , 3.0 , 1.045367774149297 },
        { 0.25 , 4.0 , 0.965325066475761 },
        { 0.25 , 5.0 , .9047004446593261 },
        { 0.25 , 6.0 , .8559102804898941 },
        { 0.25 , 7.0 , 0.815088285969639 },
        { 0.25 , 8.0 , .7799969661583689 },
        { 0.25 , 9.0 , .7492253074916152 },
        { 0.25 , 10.0 , .7218263333035008 },
        { 0.25 , 1000.0 , - .4388225372378877 },
        { 0.25 , 1001.0 , - .4390725059930951 },
        { 0.25 , 10000.0 , - 1.014553193217846 },
        { 0.5 , 0.125 , 2.230953804989556 },
        { 0.5 , 0.25 , 1.657106516191482 },
        { 0.5 , 0.5 , 1.1447298858494 },
        { 0.5 , 1.0 , .6931471805599453 },
        { 0.5 , 2.0 , .2876820724517809 },
        { 0.5 , 3.0 , .06453852113757118 },
//        { 0.5 , 4.0 , - .08961215868968714 },
        { 0.5 , 5.0 , - .2073951943460706 },
        { 0.5 , 6.0 , - .3027053741503954 },
        { 0.5 , 7.0 , - .3827480818239319 },
        { 0.5 , 8.0 , - .4517409533108833 },
        { 0.5 , 9.0 , - .5123655751273182 },
        { 0.5 , 10.0 , - .5664327963975939 },
        { 0.5 , 1000.0 , - 2.881387696571577 },
        { 0.5 , 1001.0 , - 2.881887571613228 },
        { 0.5 , 10000.0 , - 4.032792743063396 },
        { 1.0 , 0.125 , 2.079441541679836 },
        { 1.0 , 0.25 , 1.386294361119891 },
        { 1.0 , 0.5 , .6931471805599453 },
        { 1.0 , 1.0 , 0.0 },
        { 1.0 , 2.0 , - .6931471805599453 },
        { 1.0 , 3.0 , - 1.09861228866811 },
        { 1.0 , 4.0 , - 1.386294361119891 },
        { 1.0 , 5.0 , - 1.6094379124341 },
        { 1.0 , 6.0 , - 1.791759469228055 },
        { 1.0 , 7.0 , - 1.945910149055313 },
        { 1.0 , 8.0 , - 2.079441541679836 },
        { 1.0 , 9.0 , - 2.19722457733622 },
        { 1.0 , 10.0 , - 2.302585092994046 },
        { 1.0 , 1000.0 , - 6.907755278982137 },
        { 1.0 , 1001.0 , - 6.90875477931522 },
        { 1.0 , 10000.0 , - 9.210340371976184 },
        { 2.0 , 0.125 , 1.961658506023452 },
        { 2.0 , 0.25 , 1.163150809805681 },
        { 2.0 , 0.5 , .2876820724517809 },
        { 2.0 , 1.0 , - .6931471805599453 },
        { 2.0 , 2.0 , - 1.791759469228055 },
        { 2.0 , 3.0 , - 2.484906649788 },
        { 2.0 , 4.0 , - 2.995732273553991 },
        { 2.0 , 5.0 , - 3.401197381662155 },
        { 2.0 , 6.0 , - 3.737669618283368 },
        { 2.0 , 7.0 , - 4.02535169073515 },
        { 2.0 , 8.0 , - 4.276666119016055 },
        { 2.0 , 9.0 , - 4.499809670330265 },
        { 2.0 , 10.0 , - 4.700480365792417 },
        { 2.0 , 1000.0 , - 13.81651005829736 },
        { 2.0 , 1001.0 , - 13.81850806096003 },
        { 2.0 , 10000.0 , - 18.4207807389527 },
        { 3.0 , 0.125 , 1.901033884207018 },
        { 3.0 , 0.25 , 1.045367774149297 },
        { 3.0 , 0.5 , .06453852113757118 },
        { 3.0 , 1.0 , - 1.09861228866811 },
        { 3.0 , 2.0 , - 2.484906649788 },
        { 3.0 , 3.0 , - 3.401197381662155 },
        { 3.0 , 4.0 , - 4.0943445622221 },
        { 3.0 , 5.0 , - 4.653960350157523 },
        { 3.0 , 6.0 , - 5.123963979403259 },
        { 3.0 , 7.0 , - 5.529429087511423 },
        { 3.0 , 8.0 , - 5.886104031450156 },
        { 3.0 , 9.0 , - 6.20455776256869 },
        { 3.0 , 10.0 , - 6.492239835020471 },
        { 3.0 , 1000.0 , - 20.03311615938222 },
        { 3.0 , 1001.0 , - 20.03611166836202 },
        { 3.0 , 10000.0 , - 26.9381739103716 },
        { 4.0 , 0.125 , 1.860211889686763 },
        { 4.0 , 0.25 , 0.965325066475761 },
//        { 4.0 , 0.5 , - .08961215868968714 },
        { 4.0 , 1.0 , - 1.386294361119891 },
        { 4.0 , 2.0 , - 2.995732273553991 },
        { 4.0 , 3.0 , - 4.0943445622221 },
        { 4.0 , 4.0 , - 4.941642422609304 },
        { 4.0 , 5.0 , - 5.634789603169249 },
        { 4.0 , 6.0 , - 6.222576268071369 },
        { 4.0 , 7.0 , - 6.733401891837359 },
        { 4.0 , 8.0 , - 7.185387015580416 },
        { 4.0 , 9.0 , - 7.590852123688581 },
        { 4.0 , 10.0 , - 7.958576903813898 },
        { 4.0 , 1000.0 , - 25.84525465867605 },
        { 4.0 , 1001.0 , - 25.84924667994559 },
        { 4.0 , 10000.0 , - 35.05020194868867 },
        { 5.0 , 0.125 , 1.829440231020009 },
        { 5.0 , 0.25 , .9047004446593261 },
        { 5.0 , 0.5 , - .2073951943460706 },
        { 5.0 , 1.0 , - 1.6094379124341 },
        { 5.0 , 2.0 , - 3.401197381662155 },
        { 5.0 , 3.0 , - 4.653960350157523 },
        { 5.0 , 4.0 , - 5.634789603169249 },
        { 5.0 , 5.0 , - 6.445719819385578 },
        { 5.0 , 6.0 , - 7.138866999945524 },
        { 5.0 , 7.0 , - 7.745002803515839 },
        { 5.0 , 8.0 , - 8.283999304248526 },
        { 5.0 , 9.0 , - 8.769507120030227 },
        { 5.0 , 10.0 , - 9.211339872309265 },
        { 5.0 , 1000.0 , - 31.37070759780783 },
        { 5.0 , 1001.0 , - 31.37569513931887 },
        { 5.0 , 10000.0 , - 42.87464787956629 },
        { 6.0 , 0.125 , 1.804747618429637 },
        { 6.0 , 0.25 , .8559102804898941 },
        { 6.0 , 0.5 , - .3027053741503954 },
        { 6.0 , 1.0 , - 1.791759469228055 },
        { 6.0 , 2.0 , - 3.737669618283368 },
        { 6.0 , 3.0 , - 5.123963979403259 },
        { 6.0 , 4.0 , - 6.222576268071369 },
        { 6.0 , 5.0 , - 7.138866999945524 },
        { 6.0 , 6.0 , - 7.927324360309794 },
        { 6.0 , 7.0 , - 8.620471540869739 },
        { 6.0 , 8.0 , - 9.239510749275963 },
        { 6.0 , 9.0 , - 9.799126537211386 },
        { 6.0 , 10.0 , - 10.30995216097738 },
        { 6.0 , 1000.0 , - 36.67401250586691 },
        { 6.0 , 1001.0 , - 36.67999457754446 },
        { 6.0 , 10000.0 , - 50.47605021415003 },
        { 7.0 , 0.125 , 1.784128331226902 },
        { 7.0 , 0.25 , 0.815088285969639 },
        { 7.0 , 0.5 , - .3827480818239319 },
        { 7.0 , 1.0 , - 1.945910149055313 },
        { 7.0 , 2.0 , - 4.02535169073515 },
        { 7.0 , 3.0 , - 5.529429087511423 },
        { 7.0 , 4.0 , - 6.733401891837359 },
        { 7.0 , 5.0 , - 7.745002803515839 },
        { 7.0 , 6.0 , - 8.620471540869739 },
        { 7.0 , 7.0 , - 9.39366142910322 },
        { 7.0 , 8.0 , - 10.08680860966317 },
        { 7.0 , 9.0 , - 10.71541726908554 },
        { 7.0 , 10.0 , - 11.2907814139891 },
        { 7.0 , 1000.0 , - 41.79599038729854 },
        { 7.0 , 1001.0 , - 41.80296600103496 },
        { 7.0 , 10000.0 , - 57.89523093697012 },
        { 8.0 , 0.125 , 1.766428754127501 },
        { 8.0 , 0.25 , .7799969661583689 },
        { 8.0 , 0.5 , - .4517409533108833 },
        { 8.0 , 1.0 , - 2.079441541679836 },
        { 8.0 , 2.0 , - 4.276666119016055 },
        { 8.0 , 3.0 , - 5.886104031450156 },
        { 8.0 , 4.0 , - 7.185387015580416 },
        { 8.0 , 5.0 , - 8.283999304248526 },
        { 8.0 , 6.0 , - 9.239510749275963 },
        { 8.0 , 7.0 , - 10.08680860966317 },
        { 8.0 , 8.0 , - 10.84894866171006 },
        { 8.0 , 9.0 , - 11.54209584227001 },
        { 8.0 , 10.0 , - 12.17808460899001 },
        { 8.0 , 1000.0 , - 46.76481113096179 },
        { 8.0 , 1001.0 , - 46.77277930061096 },
        { 8.0 , 10000.0 , - 65.16036091500527 },
        { 9.0 , 0.125 , 1.750924567591535 },
        { 9.0 , 0.25 , .7492253074916152 },
        { 9.0 , 0.5 , - .5123655751273182 },
        { 9.0 , 1.0 , - 2.19722457733622 },
        { 9.0 , 2.0 , - 4.499809670330265 },
        { 9.0 , 3.0 , - 6.20455776256869 },
        { 9.0 , 4.0 , - 7.590852123688581 },
        { 9.0 , 5.0 , - 8.769507120030227 },
        { 9.0 , 6.0 , - 9.799126537211386 },
        { 9.0 , 7.0 , - 10.71541726908554 },
        { 9.0 , 8.0 , - 11.54209584227001 },
        { 9.0 , 9.0 , - 12.29586764464639 },
        { 9.0 , 10.0 , - 12.98901482520633 },
        { 9.0 , 1000.0 , - 51.60109303791327 },
        { 9.0 , 1001.0 , - 51.61005277928474 },
        { 9.0 , 10000.0 , - 72.29205942547217 },
        { 10.0 , 0.125 , 1.7371312454592 },
        { 10.0 , 0.25 , .7218263333035008 },
        { 10.0 , 0.5 , - .5664327963975939 },
        { 10.0 , 1.0 , - 2.302585092994046 },
        { 10.0 , 2.0 , - 4.700480365792417 },
        { 10.0 , 3.0 , - 6.492239835020471 },
        { 10.0 , 4.0 , - 7.958576903813898 },
        { 10.0 , 5.0 , - 9.211339872309265 },
        { 10.0 , 6.0 , - 10.30995216097738 },
        { 10.0 , 7.0 , - 11.2907814139891 },
        { 10.0 , 8.0 , - 12.17808460899001 },
        { 10.0 , 9.0 , - 12.98901482520633 },
        { 10.0 , 10.0 , - 13.73622922703655 },
        { 10.0 , 1000.0 , - 56.32058348093065 },
        { 10.0 , 1001.0 , - 56.33053381178382 },
        { 10.0 , 10000.0 , - 79.30607481535498 },
        { 1000.0 , 0.125 , 1.156003642015969 },
        { 1000.0 , 0.25 , - .4388225372378877 },
        { 1000.0 , 0.5 , - 2.881387696571577 },
        { 1000.0 , 1.0 , - 6.907755278982137 },
        { 1000.0 , 2.0 , - 13.81651005829736 },
        { 1000.0 , 3.0 , - 20.03311615938222 },
        { 1000.0 , 4.0 , - 25.84525465867605 },
        { 1000.0 , 5.0 , - 31.37070759780783 },
        { 1000.0 , 6.0 , - 36.67401250586691 },
        { 1000.0 , 7.0 , - 41.79599038729854 },
        { 1000.0 , 8.0 , - 46.76481113096179 },
        { 1000.0 , 9.0 , - 51.60109303791327 },
        { 1000.0 , 10.0 , - 56.32058348093065 },
        { 1000.0 , 1000.0 , - 1388.482601635902 },
        { 1000.0 , 1001.0 , - 1389.175748816462 },
        { 1000.0 , 10000.0 , - 3353.484270767097 },
        { 1001.0 , 0.125 , 1.155878649827818 },
        { 1001.0 , 0.25 , - .4390725059930951 },
        { 1001.0 , 0.5 , - 2.881887571613228 },
        { 1001.0 , 1.0 , - 6.90875477931522 },
        { 1001.0 , 2.0 , - 13.81850806096003 },
        { 1001.0 , 3.0 , - 20.03611166836202 },
        { 1001.0 , 4.0 , - 25.84924667994559 },
        { 1001.0 , 5.0 , - 31.37569513931887 },
        { 1001.0 , 6.0 , - 36.67999457754446 },
        { 1001.0 , 7.0 , - 41.80296600103496 },
        { 1001.0 , 8.0 , - 46.77277930061096 },
        { 1001.0 , 9.0 , - 51.61005277928474 },
        { 1001.0 , 10.0 , - 56.33053381178382 },
        { 1001.0 , 1000.0 , - 1389.175748816462 },
        { 1001.0 , 1001.0 , - 1389.869395872064 },
        { 1001.0 , 10000.0 , - 3355.882166039895 },
        { 10000.0 , 0.125 , .8681312798751318 },
        { 10000.0 , 0.25 , - 1.014553193217846 },
        { 10000.0 , 0.5 , - 4.032792743063396 },
        { 10000.0 , 1.0 , - 9.210340371976184 },
        { 10000.0 , 2.0 , - 18.4207807389527 },
        { 10000.0 , 3.0 , - 26.9381739103716 },
        { 10000.0 , 4.0 , - 35.05020194868867 },
        { 10000.0 , 5.0 , - 42.87464787956629 },
        { 10000.0 , 6.0 , - 50.47605021415003 },
        { 10000.0 , 7.0 , - 57.89523093697012 },
        { 10000.0 , 8.0 , - 65.16036091500527 },
        { 10000.0 , 9.0 , - 72.29205942547217 },
        { 10000.0 , 10.0 , - 79.30607481535498 },
        { 10000.0 , 1000.0 , - 3353.484270767097 },
        { 10000.0 , 1001.0 , - 3355.882166039895 },
        { 10000.0 , 10000.0 , - 13866.28325676141 },
    };

    @Test
    public void testLogBeta() {
        final int ulps = 3;
        for (int i = 0; i < LOG_BETA_REF.length; i++) {
            final double[] ref = LOG_BETA_REF[i];
            final double a = ref[0];
            final double b = ref[1];
            final double expected = ref[2];
            final double actual = Beta.logBeta(a, b);
            final double tol = ulps * FastMath.ulp(expected);
            final StringBuilder builder = new StringBuilder();
            builder.append(a).append(", ").append(b);
            Assert.assertEquals(builder.toString(), expected, actual, tol);
        }
    }}
