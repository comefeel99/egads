/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads;

import com.yahoo.egads.data.TimeSeries;
import com.yahoo.egads.models.tsmm.*;
import com.yahoo.egads.utilities.FileUtils;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

// Tests the correct generation of the expected values for olympic
// scoring.
public class TestPredictionModel {




  @Test
  public void testNaiveForecastingModel() throws Exception {

    Properties p = new Properties();
//    p.setProperty("NUM_WEEKS", refWindows[w]);
//    p.setProperty("NUM_TO_DROP", drops[d]);
//    p.setProperty("THRESHOLD", "mapee#100,mase#10");

    ArrayList<TimeSeries> actual_metric = FileUtils
            .createTimeSeries("src/test/resources/sample_sales_month_count.csv", p);


    NaiveForecastingModel model = new NaiveForecastingModel(p);
    model.train(actual_metric.get(0).data);

    TimeSeries.DataSequence sequence = new TimeSeries.DataSequence(actual_metric.get(0).startTime(),
            actual_metric.get(0).lastTime(),
            60*60*24*30);
    sequence.setLogicalIndices(actual_metric.get(0).startTime(), 60*60*24*30);


    model.predict(sequence);

  }



  @Test
  public void testRegressionModel() throws Exception {

    Properties p = new Properties();
//    p.setProperty("NUM_WEEKS", refWindows[w]);
//    p.setProperty("NUM_TO_DROP", drops[d]);
//    p.setProperty("THRESHOLD", "mapee#100,mase#10");

    ArrayList<TimeSeries> actual_metric = FileUtils
            .createTimeSeries("src/test/resources/sample_sales_month_count.csv", p);


    RegressionModel model = new RegressionModel(p);
    model.train(actual_metric.get(0).data);

    TimeSeries.DataSequence sequence = new TimeSeries.DataSequence(actual_metric.get(0).startTime(),
            actual_metric.get(0).lastTime(),
            60*60*24*30);
    sequence.setLogicalIndices(actual_metric.get(0).startTime(), 60*60*24*30);


    model.predict(sequence);


    Map<String, Object> modelParams = model.getModelParams();

    TimeSeries.DataSequence observed = actual_metric.get(0).data;
    TimeSeries.DataSequence expected = new TimeSeries.DataSequence(actual_metric.get(0).startTime(),
            actual_metric.get(0).lastTime(),
            60*60*24*30);
    expected.setLogicalIndices(actual_metric.get(0).startTime(), 60*60*24*30);


    model.predict( modelParams, observed, expected);


  }


  @Test
  public void testSimpleExponentialSmoothingModel() throws Exception {

    String configFile = "src/test/resources/sample_sales_month_count_config.ini";
    InputStream is = new FileInputStream(configFile);
    Properties p = new Properties();
    p.load(is);
    ArrayList<TimeSeries> actual_metric = FileUtils
            .createTimeSeries("src/test/resources/sample_sales_month_count.csv", p);

    SimpleExponentialSmoothingModel model = new SimpleExponentialSmoothingModel(p);
    model.train(actual_metric.get(0).data);

    TimeSeries.DataSequence sequence = new TimeSeries.DataSequence(actual_metric.get(0).startTime(),
            actual_metric.get(0).lastTime(),
            60*60*24*30);
    sequence.setLogicalIndices(actual_metric.get(0).startTime(), 60*60*24*30);
    model.predict(sequence);


    Map<String, Object> modelParams = model.getModelParams();

    TimeSeries.DataSequence observed = actual_metric.get(0).data;
    TimeSeries.DataSequence expected = new TimeSeries.DataSequence(actual_metric.get(0).startTime(),
            actual_metric.get(0).lastTime(),
            60*60*24*30);
    sequence.setLogicalIndices(actual_metric.get(0).startTime(), 60*60*24*30);

    model.predict( modelParams, observed, expected);

  }



  @Test
  public void testDoubleExponentialSmoothingModel() throws Exception {

    // not yet
    String configFile = "src/test/resources/sample_sales_month_count_config.ini";
    InputStream is = new FileInputStream(configFile);
    Properties p = new Properties();
    p.load(is);
    ArrayList<TimeSeries> actual_metric = FileUtils
            .createTimeSeries("src/test/resources/sample_sales_month_count.csv", p);

    DoubleExponentialSmoothingModel model = new DoubleExponentialSmoothingModel(p);
    model.train(actual_metric.get(0).data);

    TimeSeries.DataSequence sequence = new TimeSeries.DataSequence(actual_metric.get(0).startTime(),
            actual_metric.get(0).lastTime(),
            60*60*24*30);
    sequence.setLogicalIndices(actual_metric.get(0).startTime(), 60*60*24*30);
    model.predict(sequence);


    Map<String, Object> modelParams = model.getModelParams();

    TimeSeries.DataSequence observed = actual_metric.get(0).data;
    TimeSeries.DataSequence expected = new TimeSeries.DataSequence(actual_metric.get(0).startTime(),
            actual_metric.get(0).lastTime(),
            60*60*24*30);
    sequence.setLogicalIndices(actual_metric.get(0).startTime(), 60*60*24*30);

    model.predict( modelParams, observed, expected);

  }

  @Test
  public void testTripleExponentialSmoothingModel() throws Exception {

    String configFile = "src/test/resources/sample_sales_month_count_config.ini";
    InputStream is = new FileInputStream(configFile);
    Properties p = new Properties();
    p.load(is);
    ArrayList<TimeSeries> actual_metric = FileUtils
            .createTimeSeries("src/test/resources/sample_sales_month_count.csv", p);

    TripleExponentialSmoothingModel model = new TripleExponentialSmoothingModel(p);
    model.train(actual_metric.get(0).data);

    TimeSeries.DataSequence sequence = new TimeSeries.DataSequence(actual_metric.get(0).startTime(),
            actual_metric.get(0).lastTime(),
            60*60*24*30);
    sequence.setLogicalIndices(actual_metric.get(0).startTime(), 60*60*24*30);
    model.predict(sequence);


    Map<String, Object> modelParams = model.getModelParams();
//    modelParams.remove("startTime");

    actual_metric.get(0).data.remove(0);
    TimeSeries.DataSequence observed = actual_metric.get(0).data;
    TimeSeries.DataSequence expected = new TimeSeries.DataSequence(actual_metric.get(0).startTime(),
            actual_metric.get(0).lastTime(),
            60*60*24*30);
    expected.setLogicalIndices(actual_metric.get(0).startTime(), 60*60*24*30);

    model.predict( modelParams, observed, expected);

  }


  @Test
  public void testMeanModel() throws Exception {

    String configFile = "src/test/resources/sample_sales_month_count_config.ini";
    InputStream is = new FileInputStream(configFile);
    Properties p = new Properties();
    p.load(is);
    ArrayList<TimeSeries> actual_metric = FileUtils
            .createTimeSeries("src/test/resources/sample_sales_month_count.csv", p);

    MeanModel model = new MeanModel(p);
    model.train(actual_metric.get(0).data);

    TimeSeries.DataSequence sequence = new TimeSeries.DataSequence(actual_metric.get(0).startTime(),
            actual_metric.get(0).lastTime(),
            60*60*24*30);
    sequence.setLogicalIndices(actual_metric.get(0).startTime(), 60*60*24*30);
    model.predict(sequence);


    Map<String, Object> modelParams = model.getModelParams();

    actual_metric.get(0).data.remove(0);
    TimeSeries.DataSequence observed = actual_metric.get(0).data;
    TimeSeries.DataSequence expected = new TimeSeries.DataSequence(actual_metric.get(0).startTime(),
            actual_metric.get(0).lastTime(),
            60*60*24*30);
    expected.setLogicalIndices(actual_metric.get(0).startTime(), 60*60*24*30);

    model.predict( modelParams, observed, expected);

  }

  @Test
  public void testLongTripleExponentialSmoothingModel() throws Exception {

    String configFile = "src/test/resources/sample_sales_day_sum_config.ini";
    InputStream is = new FileInputStream(configFile);
    Properties p = new Properties();
    p.load(is);
    ArrayList<TimeSeries> actual_metric = FileUtils
            .createTimeSeries("src/test/resources/sample_week_pattern2.csv", p);

    LongTripleExponentialSmoothingModel model = new LongTripleExponentialSmoothingModel(p);
    model.train(actual_metric.get(0).data);

    TimeSeries.DataSequence sequence = new TimeSeries.DataSequence(actual_metric.get(0).startTime(),
            actual_metric.get(0).lastTime(),
            60*60*24*30);
    sequence.setLogicalIndices(actual_metric.get(0).startTime(), 60*60*24*30);
    model.predict(sequence);


    Map<String, Object> modelParams = model.getModelParams();
//    modelParams.remove("startTime");

    actual_metric.get(0).data.remove(0);
    TimeSeries.DataSequence observed = actual_metric.get(0).data;
    TimeSeries.DataSequence expected = new TimeSeries.DataSequence(actual_metric.get(0).startTime(),
            actual_metric.get(0).lastTime(),
            60*60*24*30);
    expected.setLogicalIndices(actual_metric.get(0).startTime(), 60*60*24*30);

    model.predict( modelParams, observed, expected);

  }


  @Test
  public void testSeasonalMedianModel() throws Exception {

    String configFile = "src/test/resources/sample_sales_day_sum_config.ini";
    InputStream is = new FileInputStream(configFile);
    Properties p = new Properties();
    p.load(is);
    ArrayList<TimeSeries> actual_metric = FileUtils
            .createTimeSeries("src/test/resources/sample_week_pattern2.csv", p);

    SeasonalMedianModel model = new SeasonalMedianModel(p);
    model.train(actual_metric.get(0).data);

    TimeSeries.DataSequence sequence = new TimeSeries.DataSequence(actual_metric.get(0).startTime(),
            actual_metric.get(0).lastTime(),
            60*60*24*30);
    sequence.setLogicalIndices(actual_metric.get(0).startTime(), 60*60*24*30);
    model.predict(sequence);
  }


  @Test
  public void testCast() throws Exception {

    int a = -10;
    int b = 3;

    int c = a % b;


  }
//    @Test
//    public void testOlympicModel() throws Exception {
//        // Test cases: ref window: 10, 5
//        // Drops: 0, 1
//        String[] refWindows = new String[]{"10", "5"};
//        String[] drops = new String[]{"0", "1"};
//        // Load the true expected values from a file.
//        String configFile = "src/test/resources/sample_config.ini";
//        InputStream is = new FileInputStream(configFile);
//        Properties p = new Properties();
//        p.load(is);
//        ArrayList<TimeSeries> actual_metric = FileUtils
//                .createTimeSeries("src/test/resources/model_input.csv", p);
//
//        for (int w = 0; w < refWindows.length; w++) {
//            for (int d = 0; d < drops.length; d++) {
//                 p.setProperty("NUM_WEEKS", refWindows[w]);
//                 p.setProperty("NUM_TO_DROP", drops[d]);
//                 // Parse the input timeseries.
//                 ArrayList<TimeSeries> metrics = FileUtils
//                            .createTimeSeries("src/test/resources/model_output_" + refWindows[w] + "_" + drops[d] + ".csv", p);
//                 OlympicModel model = new OlympicModel(p);
//                 model.train(actual_metric.get(0).data);
//                 TimeSeries.DataSequence sequence = new TimeSeries.DataSequence(metrics.get(0).startTime(),
//                		                                                        metrics.get(0).lastTime(),
//                		                                                        3600);
//
//
//                 sequence.setLogicalIndices(metrics.get(0).startTime(), 3600);
//                 model.predict(sequence);
//                 Assert.assertEquals(verifyResults(sequence, metrics.get(0).data), true);
//            }
//        }
//    }
//
//    // Verifies that the two time-series are identical.
//    private boolean verifyResults (TimeSeries.DataSequence computed, TimeSeries.DataSequence actual) {
//         int n = computed.size();
//         int n2 = actual.size();
//         if (n != n2) {
//             return false;
//         }
//         float precision = (float) 0.000001;
//         for (int i = 0; i < n; i++) {
//             if (Math.abs(computed.get(i).value - actual.get(i).value) > precision) {
//                 return false;
//             }
//         }
//         return true;
//    }
//
//    @Test
//    public void testForecastErrors() throws Exception {
//        String configFile = "src/test/resources/sample_config.ini";
//        InputStream is = new FileInputStream(configFile);
//        Properties p = new Properties();
//        p.load(is);
//        ArrayList<TimeSeries> actual_metric = FileUtils
//                .createTimeSeries("src/test/resources/model_input.csv", p);
//        OlympicModel olympicModel = new OlympicModel(p);
//        olympicModel.train(actual_metric.get(0).data);
//
//        Assert.assertEquals(olympicModel.getBias(), -26.315675155416635, 1e-10);
//        Assert.assertEquals(olympicModel.getMAD(), 28.81582062080335, 1e-10);
//        Assert.assertEquals(Double.isNaN(olympicModel.getMAPE()), true);
//        Assert.assertEquals(olympicModel.getMSE(), 32616.547275296416, 1e-7);
//        Assert.assertEquals(olympicModel.getSAE(), 41033.72856402397, 1e-7);
//    }
//
//    @Test
//    public void testBetterThan() throws Exception {
//        String configFile = "src/test/resources/sample_config.ini";
//        InputStream is = new FileInputStream(configFile);
//        Properties p = new Properties();
//        p.load(is);
//        ArrayList<TimeSeries> actual_metric = FileUtils
//                .createTimeSeries("src/test/resources/model_input.csv", p);
//        OlympicModel olympicModel = new OlympicModel(p);
//        olympicModel.train(actual_metric.get(0).data);
//
//        MovingAverageModel movingAverageModel = new MovingAverageModel(p);
//        movingAverageModel.train(actual_metric.get(0).data);
//
//        // movingAverageModel is better than olympicModel
//        Assert.assertEquals(TimeSeriesAbstractModel.betterThan(movingAverageModel, olympicModel), true);
//        Assert.assertEquals(TimeSeriesAbstractModel.betterThan(movingAverageModel, movingAverageModel), false);
//        Assert.assertEquals(TimeSeriesAbstractModel.betterThan(olympicModel, movingAverageModel), false);
//        Assert.assertEquals(TimeSeriesAbstractModel.betterThan(olympicModel, olympicModel), false);
//    }
}
