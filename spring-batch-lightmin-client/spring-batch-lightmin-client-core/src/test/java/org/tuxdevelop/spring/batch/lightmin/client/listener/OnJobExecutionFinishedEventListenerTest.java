
package org.tuxdevelop.spring.batch.lightmin.client.listener;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.tuxdevelop.spring.batch.lightmin.api.resource.monitoring.JobExecutionEventInfo;
import org.tuxdevelop.spring.batch.lightmin.client.event.RemoteJobExecutionEventPublisher;
import org.tuxdevelop.spring.batch.lightmin.client.event.RemoteMetricEventPublisher;
import org.tuxdevelop.spring.batch.lightmin.event.JobExecutionEvent;
import org.tuxdevelop.spring.batch.lightmin.service.MetricService;

import static org.mockito.Mockito.any;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest()
public class OnJobExecutionFinishedEventListenerTest {

    private OnJobExecutionFinishedEventListener onJobExecutionFinishedEventListener;

    @Mock
    private RemoteJobExecutionEventPublisher jobExecutionEventPublisher;

    @Mock
    private RemoteMetricEventPublisher remoteMetricEventPublisher;

    @Mock
    private MetricService metricService;

    @Test
    public void testOnApplicationEventJobExecution() {
        final JobInstance instance = new JobInstance(1L, "testJob");
        final JobExecution jobExecution = new JobExecution(1L);
        jobExecution.setJobInstance(instance);
        jobExecution.setExitStatus(ExitStatus.COMPLETED);
        final JobExecutionEvent jobExecutionEvent = new JobExecutionEvent(jobExecution, "testApplication");

        this.onJobExecutionFinishedEventListener.onApplicationEvent(jobExecutionEvent);
        Mockito.verify(this.jobExecutionEventPublisher, Mockito.times(1))
                .publishEvent(any(JobExecutionEventInfo.class));
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        this.onJobExecutionFinishedEventListener = new OnJobExecutionFinishedEventListener(this.jobExecutionEventPublisher, this.remoteMetricEventPublisher, this.metricService);
    }

}

