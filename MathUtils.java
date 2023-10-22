package com.example.calculatortest;

import java.text.DecimalFormat;

public class MathUtils {
    // 额外功能：实现可控的小数位选择
    private int decimalPlaces = 6; // 用户选择的小数位数，默认为6位

    // 设置小数位数
    public void setDecimalPlaces(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }
    public String formatResult(double result) {
        // 构造格式化字符串，根据用户选择的小数位数来设置格式
        StringBuilder formatBuilder = new StringBuilder("#.");
        for (int i = 0; i < decimalPlaces; i++) {
            formatBuilder.append("#");
        }
        String formatPattern = formatBuilder.toString();

        // 使用 DecimalFormat 格式化结果
        DecimalFormat decimalFormat = new DecimalFormat(formatPattern);
        return decimalFormat.format(result);
    }
}
