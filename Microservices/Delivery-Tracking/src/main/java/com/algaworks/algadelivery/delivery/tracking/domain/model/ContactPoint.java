package com.algaworks.algadelivery.delivery.tracking.domain.model;

import lombok.*;

//Este eh um valueObject
//Esta @EqualsAndHashCode anotacao utiliza todas a propriedades como indice unico - padrao onlyExplicitlyIncluded = false
@EqualsAndHashCode
@AllArgsConstructor
@Builder
@Getter
public class ContactPoint {
    private String zipCode;
    private String street;
    private String number;
    private String complement;
    private String name;
    private String phone;
}
