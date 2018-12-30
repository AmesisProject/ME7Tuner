package openloop.util;

import contract.Me7LogFileContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Me7LogUtil {

    public static List<Map<String, List<Double>>> findMe7Logs(Map<String, List<Double>> me7Log, double minThrottleAngle, double lambdaControlEnabled, double minRpm, int minPointsMe7) {
        ArrayList<Map<String, List<Double>>> logList = new ArrayList<>();

        List<Double> lambdaControl = me7Log.get(Me7LogFileContract.LAMBDA_CONTROL_ACTIVE_HEADER);
        List<Double> throttleAngle = me7Log.get(Me7LogFileContract.THROTTLE_PLATE_ANGLE_HEADER);
        List<Double> rpm = me7Log.get(Me7LogFileContract.RPM_COLUMN_HEADER);

        for (int i = 0; i < throttleAngle.size(); i++) {
            if (throttleAngle.get(i) >= minThrottleAngle && lambdaControl.get(i) == lambdaControlEnabled && rpm.get(i) >= minRpm) {
                if (LogUtil.isValidLogLength(i, minPointsMe7, minThrottleAngle, throttleAngle)) {
                    int endOfLog = LogUtil.findEndOfLog(i, minThrottleAngle, throttleAngle);
                    logList.add(getMe7Log(i, endOfLog, me7Log));
                    i = endOfLog + 1;
                }
            }
        }

        return logList;
    }

    private static Map<String, List<Double>> getMe7Log(int start, int end, Map<String, List<Double>> me7Log) {
        List<Double> voltages = me7Log.get(Me7LogFileContract.MAF_VOLTAGE_HEADER);
        List<Double> requestedLambda = me7Log.get(Me7LogFileContract.REQUESTED_LAMBDA_HEADER);
        List<Double> stft = me7Log.get(Me7LogFileContract.STFT_COLUMN_HEADER);
        List<Double> ltft = me7Log.get(Me7LogFileContract.LTFT_COLUMN_HEADER);
        List<Double> lambdaControl = me7Log.get(Me7LogFileContract.LAMBDA_CONTROL_ACTIVE_HEADER);
        List<Double> throttleAngle = me7Log.get(Me7LogFileContract.THROTTLE_PLATE_ANGLE_HEADER);
        List<Double> rpm = me7Log.get(Me7LogFileContract.RPM_COLUMN_HEADER);

        Map<String, List<Double>> log = new HashMap<>();
        log.put(Me7LogFileContract.MAF_VOLTAGE_HEADER, voltages.subList(start, end));
        log.put(Me7LogFileContract.REQUESTED_LAMBDA_HEADER, requestedLambda.subList(start, end));
        log.put(Me7LogFileContract.STFT_COLUMN_HEADER, stft.subList(start, end));
        log.put(Me7LogFileContract.LTFT_COLUMN_HEADER, ltft.subList(start, end));
        log.put(Me7LogFileContract.LAMBDA_CONTROL_ACTIVE_HEADER, lambdaControl.subList(start, end));
        log.put(Me7LogFileContract.THROTTLE_PLATE_ANGLE_HEADER, throttleAngle.subList(start, end));
        log.put(Me7LogFileContract.RPM_COLUMN_HEADER, rpm.subList(start, end));

        return log;
    }


}
