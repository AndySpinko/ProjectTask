package org.study.pixelbattleback;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;
import org.study.pixelbattleback.dto.PixelRequest;
import org.study.pixelbattleback.dto.ResultResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.concurrent.*;

//LongSummaryStatistics{count=10000, sum=21094, min=0, average=2,109400, max=263}
//LongSummaryStatistics{count=10000, sum=24636, min=0, average=2,463600, max=363}
//LongSummaryStatistics{count=10000, sum=24147, min=0, average=2,414700, max=207}
//LongSummaryStatistics{count=10000, sum=17122, min=0, average=1,712200, max=202}
//LongSummaryStatistics{count=10000, sum=27574, min=0, average=2,757400, max=315}

//LongSummaryStatistics{count=10000, sum=29122, min=0, average=2,912200, max=251}
//LongSummaryStatistics{count=10000, sum=23903, min=0, average=2,390300, max=191}
//LongSummaryStatistics{count=10000, sum=22439, min=0, average=2,243900, max=200}
//LongSummaryStatistics{count=10000, sum=21506, min=0, average=2,150600, max=228}
//LongSummaryStatistics{count=10000, sum=16424, min=0, average=1,642400, max=237}

public class TestCase {
    private final static Logger logger = LoggerFactory.getLogger(TestCase.class);

    private static final int THREAD_COUNT = 100;

    private static final int REQUEST_COUNT = 100;

    public static final String URL = "http://127.0.0.1:8080/drawPixel";

    @Test
    public void testParallel() {
        ExecutorService pool = Executors.newCachedThreadPool();
        List<Future<?>> futureList = new ArrayList<>();
        for (int threadNumber = 0; threadNumber < THREAD_COUNT; ++threadNumber) {
            int threadColor = ThreadLocalRandom.current().nextInt(1, 255<<17);
            final RestTemplate rest = new RestTemplate();
            futureList.add(pool.submit(() -> {
                PixelRequest pixel = new PixelRequest();
                pixel.setColor(threadColor);
                StopWatch stopWatch = new StopWatch();
                LongSummaryStatistics stats = new LongSummaryStatistics();
                for (int i = 0; i < REQUEST_COUNT; i++) {
                    pixel.setX(ThreadLocalRandom.current().nextInt(0, 100));
                    pixel.setY(ThreadLocalRandom.current().nextInt(0, 100));
                    stopWatch.start();
                    try {
                        ResultResponse res = rest.postForObject(URL, pixel, ResultResponse.class);
                    }catch (Exception e){
                        logger.error(e.getMessage(),e);
                    }
                    stopWatch.stop();
                    stats.accept(stopWatch.getLastTaskTimeMillis());
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1, 100));
                }
                return stats;
            }));
        }
        LongSummaryStatistics stats = new LongSummaryStatistics();
        for (Future<?> voidFuture : futureList) {
            try {
                stats.combine((LongSummaryStatistics) voidFuture.get());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println(stats);
    }
}
