package org.acme.ecommerce.order.service;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.transaction.Transactional;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.format.EventFormat;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.jackson.JsonFormat;
import org.acme.ecommerce.order.model.Order;
import org.acme.ecommerce.order.model.OrderStatus;
import org.acme.ecommerce.order.model.OutboxEvent;

@ApplicationScoped
public class OrderService {

    @Inject
    EntityManager entityManager;

    @Transactional
    public Long create(Order order) {
        order.setStatus(OrderStatus.CREATED);
        entityManager.persist(order);
        OutboxEvent outboxEvent = buildOutBoxEvent(order);
        entityManager.persist(outboxEvent);
        entityManager.remove(outboxEvent);
        return order.getId();
    }

    @Transactional
    public Order get(Long id) {
        return entityManager.find(Order.class, id, LockModeType.OPTIMISTIC);
    }

    OutboxEvent buildOutBoxEvent(Order order) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateType("order-event");
        outboxEvent.setAggregateId(Long.toString(order.getId()));
        outboxEvent.setContentType("application/cloudevents+json; charset=UTF-8");
        outboxEvent.setPayload(toCloudEvent(order));
        return outboxEvent;
    }

    String toCloudEvent(Order order) {
        CloudEvent event = CloudEventBuilder.v1().withType("OrderCreatedEvent").withTime(OffsetDateTime.now()).withSource(URI.create("ecommerce/order-service"))
                .withDataContentType("application/json").withId(UUID.randomUUID().toString()).withData(order.toJson().encode().getBytes()).build();
        EventFormat format = EventFormatProvider.getInstance().resolveFormat(JsonFormat.CONTENT_TYPE);
        return new String(format.serialize(event));
    }

}
