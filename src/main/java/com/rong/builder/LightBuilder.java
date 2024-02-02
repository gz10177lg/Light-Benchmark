package com.rong.builder;

import com.rong.worker.LightWorkerModel;
import com.rong.worker.impl.LightWorkerOpsModel;
import com.rong.worker.impl.LightWorkerTimesModel;

public class LightBuilder {

    public static LightBuilder build() {
        return new LightBuilder();
    }

    public LightWorkerModel model(int model) {
        switch (model) {
            case 1:
                return new LightWorkerOpsModel();
            case 2:
                return new LightWorkerTimesModel();
            default:
                return null;
        }
    }
}
