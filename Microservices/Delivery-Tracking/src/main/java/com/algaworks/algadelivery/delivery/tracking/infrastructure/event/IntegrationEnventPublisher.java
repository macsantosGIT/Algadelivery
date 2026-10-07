package com.algaworks.algadelivery.delivery.tracking.infrastructure.event;

public interface IntegrationEnventPublisher {
    void publish(Object event, String key, String topic);
}
