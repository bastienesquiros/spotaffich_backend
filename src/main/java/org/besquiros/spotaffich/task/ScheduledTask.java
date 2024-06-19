package org.besquiros.spotaffich.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.besquiros.spotaffich.service.GeoPointService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ScheduledTask {

    private static final long THREE_MONTHS_IN_MILLISECONDS = 90L * 24L * 60L * 60L * 1000L;
    private static final Logger logger = LogManager.getLogger(ScheduledTask.class);
    private GeoPointService geoPointService;

    public ScheduledTask(GeoPointService geoPointService) {
        this.geoPointService = geoPointService;
    }

    @Scheduled(fixedRate = THREE_MONTHS_IN_MILLISECONDS)
    public void performTask() {
        logger.info("Task started at " + LocalDateTime.now());
        geoPointService.fetchAllGeoPoint();
        logger.info("Task ended at " + LocalDateTime.now());
    }
}
