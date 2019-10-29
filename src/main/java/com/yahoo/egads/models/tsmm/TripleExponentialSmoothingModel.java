//
//  OpenForecast - open source, general-purpose forecasting package.
//  Copyright (C) 2002-2011  Steven R. Gould
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//

// Olympic scoring model considers the average of the last k weeks
// (dropping the b highest and lowest values) as the current prediction.

package com.yahoo.egads.models.tsmm;

import com.google.common.collect.ImmutableMap;
import com.yahoo.egads.data.*;
import com.yahoo.egads.data.TimeSeries.Entry;
import org.apache.commons.lang.ArrayUtils;
import org.json.JSONObject;
import org.json.JSONStringer;
import java.util.Properties;
import net.sourceforge.openforecast.DataSet;
import net.sourceforge.openforecast.ForecastingModel;
import net.sourceforge.openforecast.DataPoint;
import net.sourceforge.openforecast.Observation;
import java.util.*;

// Triple exponential smoothing - also known as the Winters method - is a refinement of the popular double exponential
// smoothing model but adds another component which takes into account any seasonality - or periodicity - in the data.
public class TripleExponentialSmoothingModel extends TimeSeriesAbstractModel {
    // methods ////////////////////////////////////////////////

    // The model that will be used for forecasting.
    private ForecastingModel forecaster;
    
    // Stores the historical values.
    private TimeSeries.DataSequence data;


    private int period;

    public TripleExponentialSmoothingModel(Properties config) {
        super(config);
        modelName = "TripleExponentialSmoothingModel";
    }

    public void reset() {
        // At this point, reset does nothing.
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public ForecastingModel getForecaster() {
        return forecaster;
    }

    public void setForecaster(ForecastingModel forecaster) {
        this.forecaster = forecaster;
    }

    public TimeSeries.DataSequence getData() {
        return data;
    }

    public void setData(TimeSeries.DataSequence data) {
        this.data = data;
    }

    public void train(TimeSeries.DataSequence data) {
        this.data = data;
        int n = data.size();
        DataPoint dp = null;
        DataSet observedData = new DataSet();
        for (int i = 0; i < n; i++) {
            dp = new Observation(data.get(i).value);
            dp.setIndependentValue("x", i);
            observedData.add(dp);
        }
        observedData.setTimeVariable("x");

        period = 12;
        observedData.setPeriodsPerYear(period);

        // TODO: Make weights configurable.

        if( n > 2 ) {
            long granularity = data.get(1).time - data.get(0).time;


            // second
            if( granularity == 1 ) {
                throw new IllegalArgumentException("TripleExponentialSmoothing models don't work second granularity.");
            }
            // minute -> hour
            else if( granularity == 60 ){
                period = 60;
                observedData.setPeriodsPerYear(period);
            }
            // hour -> day
            else if( granularity == 3600 ){
                period = 24;
                observedData.setPeriodsPerYear(period);
            }
            // day -> week
            else if( granularity == 86400 ){
                period = 7;
                observedData.setPeriodsPerYear(period);
            }
            // week -> year
            else if( granularity == 604800 ){
                period = 52;
                observedData.setPeriodsPerYear(period);
            }
            // month -> year
            else if( granularity >= 2505600 && granularity <= 2678400 ){
                period = 12;
                observedData.setPeriodsPerYear(period);
            }
        }

        forecaster = net.sourceforge.openforecast.models.TripleExponentialSmoothingModel.getBestFitModel(observedData);
//        forecaster = new net.sourceforge.openforecast.models.TripleExponentialSmoothingModel(0.75, 0.001, 0.001);
        forecaster.init(observedData);
        initForecastErrors(forecaster, data);


        System.out.println(forecaster.toString());
        logger.debug(getBias() + "\t" + getMAD() + "\t" + getMAPE() + "\t" + getMSE() + "\t" + getSAE() + "\t" + 0 + "\t" + 0);
    }

    public void update(TimeSeries.DataSequence data) {

    }
    
    public String getModelName() {
        return modelName;
    }

    public void predict(TimeSeries.DataSequence sequence) throws Exception {
          int n = data.size();
          DataSet requiredDataPoints = new DataSet();
          DataPoint dp;

          for (int count = 0; count < n; count++) {
              dp = new Observation(0.0);
              dp.setIndependentValue("x", count);
              requiredDataPoints.add(dp);
          }
          forecaster.forecast(requiredDataPoints);

          // Output the results
          Iterator<DataPoint> it = requiredDataPoints.iterator();
          int i = 0;
          while (it.hasNext()) {
              DataPoint pnt = ((DataPoint) it.next());
              logger.info(data.get(i).time + "," + data.get(i).value + "," + pnt.getDependentValue());
              sequence.set(i, (new Entry(data.get(i).time, (float) pnt.getDependentValue())));
              i++;
          }
    }


    private double[] initFirstSeason(double[] values, double[] seasonal, int period)
    {
        final int seasons = values.length / period;
        double[] seasonAverage = new double[seasons];
        double[] averaged = new double[values.length];

        for (int i = 0; i < seasons; i++) {
            for (int j = 0; j < period; j++) {
                seasonAverage[i] += values[(i * period) + j];
            }
            seasonAverage[i] /= period;
        }

        for (int i = 0; i < seasons; i++) {
            for (int j = 0; j < period; j++) {
                averaged[(i * period) + j] = seasonAverage[i] == 0 ? 0 : values[(i * period) + j] / seasonAverage[i];
            }
        }

        for (int i = 0; i < period; i++) {
            for (int j = 0; j < seasons; j++) {
                seasonal[i] += averaged[(j * period) + i];
            }
            seasonal[i] /= seasons;
        }

        return seasonal;
    }

    public Map<String, Object> getModelParams(){

        double alpha = ((net.sourceforge.openforecast.models.TripleExponentialSmoothingModel)forecaster).getAlpha();
        double beta = ((net.sourceforge.openforecast.models.TripleExponentialSmoothingModel)forecaster).getBeta();
        double gamma = ((net.sourceforge.openforecast.models.TripleExponentialSmoothingModel)forecaster).getGamma();
        int period = this.period;

        double[] values = new double[data.size()];
        for(int i = 0 ; i < data.size() ; i++ ){
            values[i] = data.get(i).value;
        }

        double firstPeriodSum  = 0;
        double secondPeriodSum = 0;
        for (int i = 0; i < period; i++) {
            firstPeriodSum += values[i];
            secondPeriodSum += values[i + period] - values[i];
        }
        double startbase = firstPeriodSum / period;
        double starttrend = secondPeriodSum / (period * period);


        double[] startseasonal = initFirstSeason(values, new double[period], period);

        double range = getValueRange( data );

        Map<String, Object> parameters = new HashMap<>();
        parameters.put( "range", range );
        parameters.put( "alpha", alpha );
        parameters.put( "beta", beta );
        parameters.put( "gamma", gamma );
        parameters.put( "startTime", data.get(0).time );
        parameters.put( "period", period );
        parameters.put( "startbase", startbase );
        parameters.put( "starttrend", starttrend );
        parameters.put( "startseasonal", startseasonal );


        return parameters;
    }

    public void predict( Map<String, Object> params, TimeSeries.DataSequence observed, TimeSeries.DataSequence expected ){

        double alpha = Double.parseDouble(params.get("alpha").toString());
        double beta = Double.parseDouble(params.get("beta").toString());
        double gamma = Double.parseDouble(params.get("gamma").toString());

        long startTime = Long.parseLong(params.get("startTime").toString());
        int period = Integer.parseInt(params.get("period").toString());

        double startbase = Double.parseDouble(params.get("startbase").toString());
        double starttrend = Double.parseDouble(params.get("starttrend").toString());

        ArrayList<Double> listSeasonal = ((ArrayList<Double>)params.get("startseasonal"));
        double[] startseasonal = new double[listSeasonal.size()];
        for ( int i = 0 ; i < listSeasonal.size() ; i++ ){
            startseasonal[i] = listSeasonal.get(i).doubleValue();
        }

//        double[] startseasonal = (double[])params.get("startseasonal");

        int inputSize = observed.size();

        if( inputSize < 2 ) {
            throw new IllegalArgumentException("TripleExponentialSmoothing models need more than 2 data");
        }

        long granularity = observed.get(1).time - observed.get(0).time;
        int seasonalPointOffset = ((int)(( observed.get(0).time - startTime )/ granularity )) % period;

        if( seasonalPointOffset < 0  ){
            throw new IllegalArgumentException("TripleExponentialSmoothing models can't predict before training time");
        }


        double preObservedValue = 0;
        double preExpectedValue = 0;
        double expected_value;

        double[] newSeasonal = new double[inputSize+period];

        double prePeriodSeasonalIndex;
        double preBaseValue = 0;
        double preTrendValue = 0;

        for( int i = 0 ; i < inputSize ; i++ ){

            if( i == 0 ){
                preBaseValue = startbase;
                preTrendValue = starttrend;
            }

            if( i < period ){
                prePeriodSeasonalIndex = startseasonal[(i + seasonalPointOffset)%period];
            }
            else {
                prePeriodSeasonalIndex = newSeasonal[i-period];
            }

            expected_value = ( preBaseValue + preTrendValue ) * prePeriodSeasonalIndex;

            expected.set(i, (new Entry(observed.get(i).time, (float) expected_value )));


            double curObservedValue = observed.get(i).value;
            double curBaseValue = alpha * (curObservedValue / prePeriodSeasonalIndex) + (1.0D - alpha) * (preBaseValue + preTrendValue);
            double curTrendValue = beta * (curBaseValue - preBaseValue) + (1.0D - beta) * preTrendValue;
            newSeasonal[i] = gamma * (curObservedValue / curBaseValue ) + (1.0D - gamma) * prePeriodSeasonalIndex;

            preBaseValue = curBaseValue;
            preTrendValue = curTrendValue;


//            double curBaseValue = alpha * (preObservedValue / prePeriodSeasonalIndex ) + (1.0D - alpha) * ( preBaseValue + preTrendValue );
//            double curTrendValue = beta * (curBaseValue - preBaseValue) + (1.0D - beta) * preTrendValue;
//            double curSeasonIndex = gamma * (preObservedValue / curBaseValue) + (1.0D - gamma) * prePeriodSeasonalIndex;
//
//            expected_value = (curBaseValue + curTrendValue) * curSeasonIndex;
//
//            expected.set(i, (new Entry(observed.get(i).time, (float) expected_value )));
//
//            preObservedValue = observed.get(i).value;
//            preExpectedValue = expected_value;
        }
    }

    public void toJson(JSONStringer json_out) {

    }

    public void fromJson(JSONObject json_obj) {

    }
}
