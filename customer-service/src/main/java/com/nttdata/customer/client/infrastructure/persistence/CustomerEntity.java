package com.nttdata.customer.client.infrastructure.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("cliente")
public class CustomerEntity {

    @Id
    @Column("cliente_id")
    private Long customerId;

    @Column("nombre")
    private String name;

    @Column("genero")
    private String gender;

    @Column("identificacion")
    private String identification;

    @Column("direccion")
    private String address;

    @Column("telefono")
    private String phone;

    @Column("contrasena")
    private String password;

    @Column("estado")
    private Boolean status;

    @Column("fecha_creacion")
    private OffsetDateTime createdAt;

    @Column("fecha_actualizacion")
    private OffsetDateTime updatedAt;
}
