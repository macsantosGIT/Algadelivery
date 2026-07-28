package com.algaworks.algadelivery.delivery.tracking.domain.model;

import com.algaworks.algadelivery.delivery.tracking.domain.exception.DomainException;
import org.hibernate.loader.internal.IdentifierLoadAccessImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryTest {

    @Test
    public void shouldChangeToPlaced(){
        Delivery delivery = Delivery.draft();
        delivery.editPreparationDetails(createdValidPreparationDetails());
        delivery.place();

        assertEquals(DeliveryStatus.WAITING_FOR_COURIER, delivery.getStatus());
        assertNotNull(delivery.getPlaceAt());
    }

    @Test
    public void shouldNotPlaced(){
        Delivery delivery = Delivery.draft();

        assertThrows(DomainException.class, () ->{
            delivery.place();
        });

        assertEquals(DeliveryStatus.DRAFT, delivery.getStatus());
        assertNull(delivery.getPlaceAt());
    }

    private Delivery.PreparationDetails createdValidPreparationDetails(){
        ContactPoint sender = ContactPoint.builder()
                .zipCode("00000-000")
                .street("Rua Sao Paulo")
                .number("100")
                .complement("Sala 10")
                .name("Jose da Silva")
                .phone("(11) 96548-7652")
                .build();

        ContactPoint recipient = ContactPoint.builder()
                .zipCode("37524-000")
                .street("Rua Brasilia")
                .number("120")
                .complement("")
                .name("Maria Joaquina")
                .phone("(11) 989749-7652")
                .build();

        return Delivery.PreparationDetails.builder()
                .sender(sender)
                .recipient(recipient)
                .distanceFee(new BigDecimal("15.00"))
                .courierPayout(new BigDecimal("5.00"))
                .expectedDeliveryTime(Duration.ofHours(5))
                .build();
    }
}