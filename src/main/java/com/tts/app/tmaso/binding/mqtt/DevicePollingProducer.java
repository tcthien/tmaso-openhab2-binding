package com.tts.app.tmaso.binding.mqtt;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tts.app.tmaso.binding.BindingConstants;
import com.tts.app.tmaso.binding.ServiceActivator;
import com.tts.app.tmaso.binding.device.TmaDeviceManager;
import com.tts.app.tmaso.binding.type.MqttConstants;

public class DevicePollingProducer extends TmaMqttProducer implements ServiceActivator, Runnable {

    private final Logger logger = LoggerFactory.getLogger(DevicePollingProducer.class);

    static protected final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("discovery");
    private ScheduledFuture<?> schedulerStop;

    private TmaDeviceManager deviceManager;

    private Date pingTime;

    public DevicePollingProducer(TmaDeviceManager deviceManager) {
        super(MqttConstants.TOPIC_PING);
        this.deviceManager = deviceManager;
    }

    @Override
    public void initialize() {
        schedulerStop = scheduler.schedule(this, BindingConstants.TIME_PING, TimeUnit.SECONDS);
    }

    @Override
    public void uninitialize() {
        schedulerStop.cancel(false);
    }

    @Override
    public void run() {
        try {
            this.pingTime = new Date();
            publish(MqttConstants.MSG_PING);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    public Date getPingTime() {
        return pingTime;
    }
}
