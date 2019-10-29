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
import org.json.JSONObject;
import org.json.JSONStringer;
import java.util.Properties;
import net.sourceforge.openforecast.DataSet;
import net.sourceforge.openforecast.ForecastingModel;
import net.sourceforge.openforecast.DataPoint;
import net.sourceforge.openforecast.Observation;
import java.util.*;

// Double exponential smoothing - also known as Holt exponential smoothing - is a refinement of the popular simple
// exponential smoothing model but adds another component which takes into account any trend in the data.
public class DoubleExponentialSmoothingModel extends TimeSeriesAbstractModel {
    // methods ////////////////////////////////////////////////

    // The model that will be used for forecasting.
    private ForecastingModel forecaster;
    
    // Stores the historical values.
    private TimeSeries.DataSequence data;

    public DoubleExponentialSmoothingModel(Properties config) {
        super(config);
        modelName = "DoubleExponentialSmoothingModel";
    }

    public void reset() {
        // At this point, reset does nothing.
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
        
        // TODO: Make weights configurable.
        forecaster = net.sourceforge.openforecast.models.DoubleExponentialSmoothingModel.getBestFitModel(observedData);
//        forecaster = new net.sourceforge.openforecast.models.DoubleExponentialSmoothingModel(0.75, 0.1);
        forecaster.init(observedData);
        initForecastErrors(forecaster, data);
        
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

    public Map<String, Object> getModelParams(){

        double alpha = ((net.sourceforge.openforecast.models.DoubleExponentialSmoothingModel)forecaster).getAlpha();
        double gamma = ((net.sourceforge.openforecast.models.DoubleExponentialSmoothingModel)forecaster).getGamma();
        Map<String, Object> parameters = ImmutableMap.of(
                "alpha", alpha,
                "gamma", gamma);

        return parameters;
    }

    public void predict( Map<String, Object> params, TimeSeries.DataSequence observed, TimeSeries.DataSequence expected ){

        double alpha = Double.parseDouble(params.get("alpha").toString());
        double gamma = Double.parseDouble(params.get("gamma").toString());

//        int inputSize = observed.size();
//
//        double preObservedValue = 0;
//        double preExpectedValue = 0;
//        double expected_value;
//
//        for( int i = 0 ; i < inputSize ; i++ ){
//
//            if( i == 0 ){
//                preObservedValue = preExpectedValue = observed.get(i).value;
//            }
//
////            slope = this.gamma * (this.forecast(time) - this.forecast(previousTime1)) + (1.0D - this.gamma) * this.getSlope(previousTime1);
////            double forecast = this.alpha * this.getObservedValue(t) + (1.0D - this.alpha) * (this.getForecastValue(previousTime) + iaex);
//
//            expected_value = alpha * preObservedValue + (1.0D - alpha) * preExpectedValue;
//            expected.set(i, (new Entry(observed.get(i).time, (float) expected_value )));
//
//            preObservedValue = observed.get(i).value;
//            preExpectedValue = expected_value;
//        }
    }

    public void toJson(JSONStringer json_out) {

    }

    public void fromJson(JSONObject json_obj) {

    }
}
