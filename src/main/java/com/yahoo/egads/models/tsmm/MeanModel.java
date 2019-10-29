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
import com.yahoo.egads.data.TimeSeries;
import com.yahoo.egads.data.TimeSeries.Entry;
import net.sourceforge.openforecast.DataPoint;
import net.sourceforge.openforecast.DataSet;
import net.sourceforge.openforecast.ForecastingModel;
import net.sourceforge.openforecast.Observation;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.*;

public class MeanModel extends TimeSeriesAbstractModel {
    // methods ////////////////////////////////////////////////

    float MeanValue;

    // Stores the historical values.
    private TimeSeries.DataSequence data;

    public MeanModel(Properties config) {
        super(config);
        modelName = "MeanModel";
    }

    public void reset() {
        // At this point, reset does nothing.
    }
    
    public void train(TimeSeries.DataSequence data) {

        this.data = data;
        int n = data.size();

        if( n == 0 )
            return;

        float[] listValue = new float[n];

        for (int i = 0; i < n; i++) {
            listValue[i] = data.get(i).value;
        }

        Arrays.sort(listValue);

        MeanValue = getMedian(listValue);

        ArrayList<Float> expected = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            expected.add(MeanValue);
        }

        initForecastErrors(expected, data);

        logger.debug(getBias() + "\t" + getMAD() + "\t" + getMAPE() + "\t" + getMSE() + "\t" + getSAE() + "\t" + 0 + "\t" + 0);

    }

    public static float getMedian(float[] array) {

        int center = array.length / 2;

        if (array.length % 2 == 1) {
            return array[center];
        } else {
            return (float)((array[center - 1] + array[center]) / 2.0);
        }
    }

    public void update(TimeSeries.DataSequence data) {

    }

    public String getModelName() {
        return modelName;
    }

    public void predict(TimeSeries.DataSequence sequence) throws Exception {

        int n = data.size();
        for (int i = 0; i < n; i++) {
            sequence.set(i, (new Entry(data.get(i).time, MeanValue)));
            logger.info(data.get(i).time + "," + data.get(i).value + "," + MeanValue);
        }
    }

    public Map<String, Object> getModelParams(){

        double range = getValueRange( data );


        Map<String, Object> parameters = ImmutableMap.of(
                "range", range,
                "mean", MeanValue);

        return parameters;
    }

    public void predict( Map<String, Object> params, TimeSeries.DataSequence observed, TimeSeries.DataSequence expected ){

        double mean = Double.parseDouble(params.get("mean").toString());
        int inputSize = observed.size();

        for( int i = 0 ; i < inputSize ; i++ ){
            expected.set(i, (new Entry(observed.get(i).time, (float) mean )));
        }

    }


    public void toJson(JSONStringer json_out) {

    }

    public void fromJson(JSONObject json_obj) {

    }
}
