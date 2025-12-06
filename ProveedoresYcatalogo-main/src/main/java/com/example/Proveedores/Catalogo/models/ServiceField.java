package com.example.Proveedores.Catalogo.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(
        name = "service_fields",
        indexes = {
                @Index(name = "idx_service_fields_service_id", columnList = "service_id"),
                @Index(name = "idx_service_fields_order_index", columnList = "order_index")
        }
)
public class ServiceField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "service_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_service_fields_service")
    )
    private Servicio service;

    @Column(name = "field_name", nullable = false, length = 50)
    private String fieldName;

    @Column(name = "field_type", nullable = false, length = 20)
    private String fieldType;

    @Column(nullable = false)
    private boolean required = true;

    @Column(name = "order_index")
    private Integer orderIndex;


}