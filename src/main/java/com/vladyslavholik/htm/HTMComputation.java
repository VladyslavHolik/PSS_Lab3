package com.vladyslavholik.htm;

import com.vladyslavholik.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class HTMComputation {
    private final static Logger log = Logger.getLogger(HTMComputation.class);

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();

        for (int numberOfJobs = 50; numberOfJobs < 1000; numberOfJobs += 50) {
            Connection connection = DBUtil.initialize();
            ExecutorService executor = Executors.newFixedThreadPool(2);

            List<Callable<Long>> jobs = new ArrayList<>();
            for (int i = 1; i <= numberOfJobs; i++) {
                jobs.add(() -> updateSoldItems(connection));
            }

            var watch = new StopWatch();
            watch.start();
            List<Future<Long>> metricFutures = executor.invokeAll(jobs);

            List<Long> metrics = new ArrayList<>();
            for (Future<Long> future : metricFutures) {
                metrics.add(future.get());
            }

            watch.stop();
            connection.close();
            executor.shutdown();

            log.info(String.format("Number of jobs: %s, time taken to execute all: %s, average time for one job: %s", numberOfJobs, watch.getTime(), calculateAverage(metrics)));
        }
    }

    private static Long updateSoldItems(Connection connection) {
        var watch = new StopWatch();
        watch.start();
        for (int id = 1; id <= DBUtil.products.size(); id++) {
            updateSoldItemsForProductWithId(id, connection);
        }

        watch.stop();
        return watch.getTime();
    }

    private static void updateSoldItemsForProductWithId(Integer id, Connection connection) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Products SET soldItems = soldItems + 1 WHERE id = ?");
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Double calculateAverage(List<Long> list) {
        var sum = 0d;
        for (Long number : list) {
            sum += number;
        }
        return sum / list.size();
    }

}
