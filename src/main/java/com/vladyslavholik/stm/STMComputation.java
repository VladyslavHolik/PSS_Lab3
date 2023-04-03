package com.vladyslavholik.stm;

import com.vladyslavholik.DBUtil;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.multiverse.api.StmUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class STMComputation {
    private final static Logger log = Logger.getLogger(STMComputation.class);

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();

        for (int numberOfJobs = 50; numberOfJobs < 1000; numberOfJobs += 50) {
            Connection connection = DBUtil.initialize();
            ExecutorService executor = Executors.newFixedThreadPool(4);

            List<Product> products = getProducts();
            List<Callable<Long>> jobs = new ArrayList<>();
            for (int i = 1; i <= 50; i++) {
                jobs.add(() -> updateSoldItems(products, connection));
            }

            var watch = new StopWatch();
            watch.start();
            List<Future<Long>> metricFutures = executor.invokeAll(jobs);

            List<Long> metrics = new ArrayList<>();
            for (Future<Long> future : metricFutures) {
                metrics.add(future.get());
            }

            watch.stop();

            log.info(String.format("Number of jobs: %s, time taken to execute all: %s, average time for one job: %s", numberOfJobs, watch.getTime(), calculateAverage(metrics)));
        }
    }

    private static List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        for (int id : DBUtil.products.keySet()) {
            products.add(new Product(id, 0));
        }
        return products;
    }

    private static Long updateSoldItems(List<Product> products, Connection connection) {
        var watch = new StopWatch();

        StmUtils.atomic(() -> {
            for (Product product : products) {
                updateSoldItemsForProduct(product, connection);
            }
        });

        watch.stop();
        return watch.getTime();
    }

    private static void updateSoldItemsForProduct(Product product, Connection connection) {
        try {
            product.getSoldItems().increment();
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Products SET soldItems = ? WHERE id = ?");
            preparedStatement.setInt(1, product.getSoldItems().get());
            preparedStatement.setInt(1, product.getId());
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
