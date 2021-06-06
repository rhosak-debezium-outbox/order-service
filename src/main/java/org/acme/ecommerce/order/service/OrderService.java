package org.acme.ecommerce.order.service;

import java.time.OffsetDateTime;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.transaction.Transactional;

import io.smallrye.reactive.messaging.ce.OutgoingCloudEventMetadata;
import io.smallrye.reactive.messaging.ce.OutgoingCloudEventMetadataBuilder;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import io.vertx.core.json.JsonObject;
import org.acme.ecommerce.order.model.Order;
import org.acme.ecommerce.order.model.OrderStatus;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

@ApplicationScoped
public class OrderService {

    @Inject
    EntityManager entityManager;

    @Inject
    @Channel("order-event")
    Emitter<JsonObject> emitter;

    public Long create(Order order) {
        order = doCreate(order);
        emitter.send(toMessage(order.getId().toString(), order));
        return order.getId();
    }

    public Order get(Long orderId) {
        return doGet(orderId);
    }

    @Transactional
    Order doCreate(Order order) {
        order.setStatus(OrderStatus.CREATED);
        entityManager.persist(order);
        return order;
    }

    @Transactional
    Order doGet(Long id) {
        return entityManager.find(Order.class, id, LockModeType.OPTIMISTIC);
    }

    @SuppressWarnings("rawtypes")
    private Message<JsonObject> toMessage(String key, Order order) {
        OutgoingCloudEventMetadataBuilder cloudEventMetadataBuilder = OutgoingCloudEventMetadata.builder().withType("OrderCreatedEvent")
                .withTimestamp(OffsetDateTime.now().toZonedDateTime());
        return KafkaRecord.of(key, order.toJson()).addMetadata(cloudEventMetadataBuilder.build());
    }

}
