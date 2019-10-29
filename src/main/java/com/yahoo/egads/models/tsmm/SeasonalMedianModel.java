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
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

public class SeasonalMedianModel extends TimeSeriesAbstractModel {
    // methods ////////////////////////////////////////////////

    float[] listSeasonalMedianValue;
    int     period;

    // Stores the historical values.
    private TimeSeries.DataSequence data;

    public SeasonalMedianModel(Properties config) {
        super(config);
        modelName = "SeasonalMedianModel";
    }

    public void reset() {
        // At this point, reset does nothing.
    }
    
    public void train(TimeSeries.DataSequence data) {

        this.data = data;
        int n = data.size();

        if( n == 0 )
            return;


        period = 1;

        if( n > 2 ) {
            long granularity = data.get(1).time - data.get(0).time;

            // second -> minute -> hour
            if( granularity == 1 ){
                period = 60*60;     // hour
                if( n < period * 2 ){
                    period = 60;    // minute
                }
            }
            // minute -> hour -> day
            else if( granularity == 60 ){
                period = 60*24;     // day
                if( n < period * 2 ){
                    period = 60;    // hour
                }
            }
            // hour -> day -> week
            else if( granularity == 3600 ){
                period = 24*7;      // week
                if( n < period * 2 ){
                    period = 24;    // day
                }
            }
            // day -> week -> year
            else if( granularity == 86400 ){
                period = 365;       // year
                if( n < period * 2 ){
                    period = 7;    // week
                }
            }
            // week -> year
            else if( granularity == 604800 ){
                period = 52;
            }
            // month -> year
            else if( granularity >= 2505600 && granularity <= 2678400 ){
                period = 12;
            }
            else{
                throw new IllegalArgumentException("SeasonalMedianModel models don't work other granularity.");
            }

        }
        else {
            throw new IllegalArgumentException("SeasonalMedianModel models don't work 1 period.");
        }


        listSeasonalMedianValue = new float[period+1];
        float[] listValue = new float[(n/period+1)];

        for( int i = 0 ; i < period ; i++ ){

            int count = 0;
            for( int j = i ; j < n ; j = j+period ){
                listValue[count++] = data.get(j).value;
            }

            listSeasonalMedianValue[i] = getMedian(listValue, count);
        }



        ArrayList<Float> expected = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            expected.add( listSeasonalMedianValue[i%period]);
        }

        initForecastErrors(expected, data);

        logger.debug(getBias() + "\t" + getMAD() + "\t" + getMAPE() + "\t" + getMSE() + "\t" + getSAE() + "\t" + 0 + "\t" + 0);

    }



    public static float getMedian(float[] targetArray, int length) {

        float[] array =  Arrays.copyOfRange(targetArray, 0, length);
        Arrays.sort(array);

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
            sequence.set(i, (new Entry(data.get(i).time, listSeasonalMedianValue[i%period])));
            logger.info(data.get(i).time + "," + data.get(i).value + "," + listSeasonalMedianValue[i%period]);
        }
    }

    public Map<String, Object> getModelParams(){

        double range = getValueRange( data );

        Map<String, Object> parameters = ImmutableMap.of(
                "range", range,
                "period", period,
                "startTime", data.get(0).time,
                "seasonal", listSeasonalMedianValue);

        return parameters;
    }

    public void predict( Map<String, Object> params, TimeSeries.DataSequence observed, TimeSeries.DataSequence expected ){

        int period = Integer.parseInt(params.get("period").toString());
        long startTime = Long.parseLong(params.get("startTime").toString());
        ArrayList<Object> listInputSeasonal = ((ArrayList<Object>)params.get("seasonal"));
        double[] listSeasonal = new double[listInputSeasonal.size()];
        for ( int i = 0 ; i < listInputSeasonal.size() ; i++ ){

            Object curValue = listInputSeasonal.get(i);

            if( curValue instanceof Integer ){
                listSeasonal[i] = ((Integer)curValue).doubleValue();
            }
            else if( curValue instanceof Double ){
                listSeasonal[i] = ((Double)curValue).doubleValue();
            }
            else if( curValue instanceof Float ){
                listSeasonal[i] = ((Float)curValue).doubleValue();
            }
            else if( curValue instanceof Long ){
                listSeasonal[i] = ((Long)curValue).doubleValue();
            }
            else{
                throw new IllegalArgumentException("type error : " + curValue.getClass().getName() + " is not usable type.");
            }

        }

        int inputSize = observed.size();

        if( inputSize < 2 ) {
            throw new IllegalArgumentException("SeasonalMedianModel models need more than 2 data");
        }

        long granularity = observed.get(1).time - observed.get(0).time;
        int seasonalPointOffset = ((int)(( observed.get(0).time - startTime )/ granularity )) % period;
        if( seasonalPointOffset < 0 ){
            seasonalPointOffset = seasonalPointOffset + period;
        }


        for( int i = 0 ; i < inputSize ; i++ ){
            expected.set(i, (new Entry(observed.get(i).time, (float)listSeasonal[(i+seasonalPointOffset)%period] )));
        }

    }


    public void toJson(JSONStringer json_out) {

    }

    public void fromJson(JSONObject json_obj) {

    }
}
