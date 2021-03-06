package analysisBasic;

import entity.NestedResource;
import entity.SporadicTask;
import utils.AnalysisUtils;

import java.util.ArrayList;

public class RTAWithoutBlockingNested extends RTAWithoutBlocking {

    public long[][] getNestedResponseTime(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<NestedResource> resources, boolean printBebug) {
        long[][] init_Ri = new AnalysisUtils().initResponseTime(tasks);
        long[][] response_time = new long[tasks.size()][];
        boolean isEqual = false, missDeadline = false;
        count = 0;

        for (int i = 0; i < init_Ri.length; i++)
            response_time[i] = new long[init_Ri[i].length];
        new AnalysisUtils().cloneList(init_Ri, response_time);

        /* a huge busy window to get a fixed Ri */
        while (!isEqual) {
            isEqual = true;
            long[][] response_time_plus = busyWindow(tasks, resources, response_time);

            for (int i = 0; i < response_time_plus.length; i++) {
                for (int j = 0; j < response_time_plus[i].length; j++) {
                    if (response_time[i][j] != response_time_plus[i][j])
                        isEqual = false;
                    if (response_time_plus[i][j] > tasks.get(i).get(j).deadline)
                        missDeadline = true;
                }
            }

            count++;
            new AnalysisUtils().cloneList(response_time_plus, response_time);
            if (missDeadline)
                break;
        }

        if (printBebug) {
            if (missDeadline)
                System.out.println("after " + count + " tims of recursion, the tasks miss the deadline.");
            else
                System.out.println("after " + count + " tims of recursion, we got the response time.");
            new AnalysisUtils().printResponseTime(response_time, tasks);
        }

        return response_time;
    }

    private long[][] busyWindow(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<NestedResource> resources, long[][] response_time) {
        long[][] response_time_plus = new long[tasks.size()][];
        for (int i = 0; i < response_time.length; i++)
            response_time_plus[i] = new long[response_time[i].length];

        for (int i = 0; i < tasks.size(); i++) {
            for (int j = 0; j < tasks.get(i).size(); j++) {
                SporadicTask task = tasks.get(i).get(j);
                task.interference = highPriorityInterference(task, tasks, response_time[i][j]);
                response_time_plus[i][j] = task.Ri = task.WCET + task.pure_resource_execution_time + task.interference;
                if (task.Ri > task.deadline)
                    return response_time_plus;
            }
        }
        return response_time_plus;

    }
}
