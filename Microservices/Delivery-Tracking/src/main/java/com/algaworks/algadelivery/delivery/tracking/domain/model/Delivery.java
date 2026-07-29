package com.algaworks.algadelivery.delivery.tracking.domain.model;

import com.algaworks.algadelivery.delivery.tracking.domain.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


//Esta @EqualsAndHashCode anotacao utiliza propriedade indicado como indice unico
@Entity
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Setter(AccessLevel.PRIVATE)
@Getter
public class Delivery {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;
    private UUID courierId;

    private DeliveryStatus status;

    private OffsetTime placeAt;
    private OffsetTime assignedAt;
    private OffsetTime expectedDeliveryAt;
    private OffsetTime fulfilledAt;

    private BigDecimal distanceFee;
    private BigDecimal courierPayout;
    private BigDecimal totalCost;

    private Integer totalItems;

    //Estes sao valueObject
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "zipCode", column = @Column(name = "sender_zipCode")),
            @AttributeOverride(name = "street", column = @Column(name = "sender_street")),
            @AttributeOverride(name = "number", column = @Column(name = "sender_number")),
            @AttributeOverride(name = "complement", column = @Column(name = "sender_complement")),
            @AttributeOverride(name = "name", column = @Column(name = "sender_name")),
            @AttributeOverride(name = "phone", column = @Column(name = "sender_zphone"))
    })
    private ContactPoint sender;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "zipCode", column = @Column(name = "recipient_zipCode")),
            @AttributeOverride(name = "street", column = @Column(name = "recipient_street")),
            @AttributeOverride(name = "number", column = @Column(name = "recipient_number")),
            @AttributeOverride(name = "complement", column = @Column(name = "recipient_complement")),
            @AttributeOverride(name = "name", column = @Column(name = "recipient_name")),
            @AttributeOverride(name = "phone", column = @Column(name = "recipient_zphone"))
    })
    private ContactPoint recipient;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "delivery")
    private List<Item> items = new ArrayList<>();

    //padrao factory - static factory(fabrica)
    //draft rascunho
    public static Delivery draft(){
        Delivery delivery = new Delivery();
        delivery.setId(UUID.randomUUID());
        delivery.setStatus(DeliveryStatus.DRAFT);
        delivery.setTotalItems(0);
        delivery.setTotalCost(BigDecimal.ZERO);
        delivery.setCourierPayout(BigDecimal.ZERO);
        delivery.setDistanceFee(BigDecimal.ZERO);
        return delivery;
    }

    //somente o root Delivery do agregatte pode adicionar items
    public UUID addItem(String name, int quantity){
        Item item = Item.brandNew(name, quantity,this);
        items.add(item);
        calculateTotalItems();
        return item.getId();
    }

    //somente o root Delivery do agregatte pode remover item
    public void removeItem(UUID itemId){
        items.removeIf(item -> item.getId().equals(itemId));
        calculateTotalItems();
    }

    public void editPreparationDetails(PreparationDetails details){
        verifyIfCanBeEdited();
        setSender(details.getSender());
        setRecipient(details.getRecipient());
        setDistanceFee(details.getDistanceFee());
        setCourierPayout(details.getCourierPayout());

        setExpectedDeliveryAt(OffsetTime.now().plus(details.getExpectedDeliveryTime()));
        setTotalCost(this.getDistanceFee().add(this.getCourierPayout()));
    }

    public void place(){
        verifyIfCanBePlace();
        this.changeStatusTo(DeliveryStatus.WAITING_FOR_COURIER);
        this.setPlaceAt(OffsetTime.now());
    }

    public void pickUp(UUID courierId){
        this.setCourierId(courierId);
        this.changeStatusTo(DeliveryStatus.IN_TRANSIT);
        this.setAssignedAt(OffsetTime.now());
    }

    public void markAsDelivered(){
        this.changeStatusTo(DeliveryStatus.DELIVERED);
        this.setFulfilledAt(OffsetTime.now());
    }

    public void chageItemQuantity(UUID itemId, int quantity){
        Item item = getItems().stream().filter(i -> i.getId().equals(itemId))
                .findFirst().orElseThrow();

        item.setQuantity(quantity);
        calculateTotalItems();
    }

    public void removeItems(){
        items.clear();
        calculateTotalItems();
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    private void calculateTotalItems(){
        int totalItems = getItems().stream().mapToInt(Item::getQuantity).sum();
        setTotalItems(totalItems);
    }

    private void verifyIfCanBePlace(){
        if (!isFilled()){
            throw new DomainException();
        }
        if (!getStatus().equals(DeliveryStatus.DRAFT)){
            throw new DomainException();
        }
    }

    private void verifyIfCanBeEdited(){
        if (!getStatus().equals(DeliveryStatus.DRAFT)){
            throw new DomainException();
        }
    }

    private boolean isFilled(){
        return getSender() != null
                && getRecipient() != null
                && getTotalCost() != null;
    }

    private void changeStatusTo(DeliveryStatus newStatus){
        if (newStatus != null && this.getStatus().canNotChangeTo(newStatus)){
           throw new DomainException(
             "Invalid status transition from " + this.getStatus() +
                     " to " + newStatus
           );
        }

        this.setStatus(newStatus);
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class PreparationDetails{
        private ContactPoint sender;
        private ContactPoint recipient;
        private BigDecimal distanceFee;
        private BigDecimal courierPayout;
        private Duration expectedDeliveryTime;
    }
}
