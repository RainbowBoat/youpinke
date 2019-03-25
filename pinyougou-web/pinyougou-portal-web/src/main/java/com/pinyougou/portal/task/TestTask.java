package com.pinyougou.portal.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TestTask {

    @Scheduled(cron = "* * * * * *")
    public void testTask() {
        System.out.println(new Date().getTime());
    }
}
