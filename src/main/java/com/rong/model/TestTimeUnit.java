package com.rong.model;

public enum TestTimeUnit {
    SECOND(1e9, " ops/s"), MS(1e6, " ops/ms"), US(1e3, " ops/us");
    private double unit;
    private String unitStr;

    TestTimeUnit(double unit, String unitStr) {
        this.unit = unit;
        this.unitStr = unitStr;
    }

    public double getUnit() {
        return unit;
    }

    public String getUnitStr() {
        return unitStr;
    }


}
