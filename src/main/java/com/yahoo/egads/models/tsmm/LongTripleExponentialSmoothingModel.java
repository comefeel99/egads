package com.yahoo.egads.models.tsmm;

import com.yahoo.egads.data.TimeSeries;
import net.sourceforge.openforecast.DataPoint;
import net.sourceforge.openforecast.DataSet;
import net.sourceforge.openforecast.Observation;
import net.sourceforge.openforecast.ForecastingModel;

import java.util.Properties;

/**
 * Created by hsp on 2019. 7. 19..
 */
public class LongTripleExponentialSmoothingModel extends TripleExponentialSmoothingModel {
  public LongTripleExponentialSmoothingModel(Properties config) {
    super(config);
    modelName = "LongTripleExponentialSmoothingModel";
  }

  public void train(TimeSeries.DataSequence data) {
    setData(data);
    int n = data.size();
    DataPoint dp = null;
    DataSet observedData = new DataSet();
    for (int i = 0; i < n; i++) {
      dp = new Observation(data.get(i).value);
      dp.setIndependentValue("x", i);
      observedData.add(dp);
    }
    observedData.setTimeVariable("x");



    int period = 12;
    observedData.setPeriodsPerYear(period);

    // TODO: Make weights configurable.

    if( n > 2 ) {
      long granularity = data.get(1).time - data.get(0).time;

      // second
      if( granularity == 1 ) {
        throw new IllegalArgumentException("LongTripleExponentialSmoothing models don't work second granularity.");
      }
      // minute -> day
      else if( granularity == 60 ){
        period = 60*24;
      }
      // hour -> week
      else if( granularity == 3600 ){
        period = 24*7;
      }
      // day -> year
      else if( granularity == 86400 ){
        period = 365;
      }
      else{
        throw new IllegalArgumentException("LongTripleExponentialSmoothing models don't work other granularity.");
      }
      setPeriod(period);
      observedData.setPeriodsPerYear(period);

    }
    else {
      throw new IllegalArgumentException("LongTripleExponentialSmoothing models don't work 1 period.");
    }

    ForecastingModel forecaster = net.sourceforge.openforecast.models.TripleExponentialSmoothingModel.getBestFitModel(observedData);
    setForecaster(forecaster);
    forecaster.init(observedData);
    initForecastErrors(forecaster, data);


    System.out.println(forecaster.toString());
    logger.debug(getBias() + "\t" + getMAD() + "\t" + getMAPE() + "\t" + getMSE() + "\t" + getSAE() + "\t" + 0 + "\t" + 0);
  }


}
