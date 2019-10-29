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

import com.yahoo.egads.data.*;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.Map;
import java.util.Properties;

// Picks the best model from the available EGADS models.
public class AutoForecastModel extends TimeSeriesAbstractModel {
    // Stores the properties file to init other models.
    private Properties p;
    
    // Stores the model.
    private TimeSeriesAbstractModel myModel = null;

    public AutoForecastModel(Properties config) {
        super(config);
        modelName = "AutoForecastModel";
        this.p = config;
    }

    public void reset() {
        // At this point, reset does nothing.
    }
    
    public void train(TimeSeries.DataSequence data) throws IllegalArgumentException  {


        // Init all.
//        OlympicModel olympModel = new OlympicModel(p);
//        MovingAverageModel movingAvg = new MovingAverageModel(p);
//        MultipleLinearRegressionModel mlReg = new MultipleLinearRegressionModel(p);
        NaiveForecastingModel naive = new NaiveForecastingModel(p);
//        PolynomialRegressionModel poly = new PolynomialRegressionModel(p);
        RegressionModel regr = new RegressionModel(p);
        SimpleExponentialSmoothingModel simpleExp = new SimpleExponentialSmoothingModel(p);
        TripleExponentialSmoothingModel tripleExp = new TripleExponentialSmoothingModel(p);
//        WeightedMovingAverageModel weightAvg = new WeightedMovingAverageModel(p);
//        DoubleExponentialSmoothingModel doubleExp = new DoubleExponentialSmoothingModel(p);
        
        // Train all.
//        olympModel.train(data);

//        try {
//            movingAvg.train(data);
//        }catch (IllegalArgumentException e) {
//            movingAvg = null;
//        }
//
//        try {
//            mlReg.train(data);
//        }catch (IllegalArgumentException e) {
//            mlReg = null;
//        }

        try {
            naive.train(data);
        }catch (IllegalArgumentException e) {
            naive = null;
        }

//        try {
//            poly.train(data);
//        }catch (IllegalArgumentException e) {
//            poly = null;
//        }

        try {
            regr.train(data);
        }catch (IllegalArgumentException e) {
            regr = null;
        }

        try {
            simpleExp.train(data);
        }catch (IllegalArgumentException e) {
            simpleExp = null;
        }

        try {
            tripleExp.train(data);
        }catch (IllegalArgumentException e) {
            tripleExp = null;
        }

//        try {
//            weightAvg.train(data);
//        }catch (IllegalArgumentException e) {
//            weightAvg = null;
//        }

//        try {
//            doubleExp.train(data);
//        }catch (IllegalArgumentException e) {
//            doubleExp = null;
//        }


        // Pick best.
//        if (betterThan(olympModel, myModel)) {
//            myModel = olympModel;
//        }
//        if (movingAvg != null && betterThan(movingAvg, myModel)) {
//            myModel = movingAvg;
//        }
//        if (mlReg != null && betterThan(mlReg, myModel)) {
//            myModel = mlReg;
//        }
        if (naive != null && betterThan(naive, myModel)) {
            myModel = naive;
        }
//        if (poly != null && betterThan(poly, myModel)) {
//            myModel = poly;
//        }
        if (regr != null && betterThan(regr, myModel)) {
            myModel = regr;
        }
        if (simpleExp != null && betterThan(simpleExp, myModel)) {
            myModel = simpleExp;
        }
        if (tripleExp != null && betterThan(tripleExp, myModel)) {
            myModel = tripleExp;
        }
//        if (weightAvg != null && betterThan(weightAvg, myModel)) {
//            myModel = weightAvg;
//        }
//        if (doubleExp != null && betterThan(doubleExp, myModel)) {
//            myModel = doubleExp;
//        }
        
        initForecastErrors(myModel, data);
       
        logger.debug(getBias() + "\t" + getMAD() + "\t" + getMAPE() + "\t" + getMSE() + "\t" + getSAE() + "\t" + 0 + "\t" + 0);
    }

    public void update(TimeSeries.DataSequence data) {

    }

    public String getModelName() {
        if( myModel != null ){
            return myModel.getModelName();
        }

        return modelName;
    }

    public void predict(TimeSeries.DataSequence sequence) throws Exception {
        myModel.predict(sequence);        
    }

    public Map<String, Object> getModelParams() throws Exception {

        if( myModel != null ){
            return myModel.getModelParams();
        }

        return null;
    }

    public void predict( Map<String, Object> params, TimeSeries.DataSequence observed, TimeSeries.DataSequence expected ) {
    }



    public void toJson(JSONStringer json_out) {

    }

    public void fromJson(JSONObject json_obj) {

    }

    public TimeSeriesAbstractModel getBestModel(){
        return myModel;
    }
}
