/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Projecte2.DriftoCar.entity.MySQL;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Anna
 */
@Entity
@Table(name = "reserva")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "id_reserva")
    private Long idReserva;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client", referencedColumnName = "dni", nullable = false) 
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) 
    @JoinColumn(name = "vehicle", referencedColumnName = "matricula", nullable = false) 
    private Vehicle vehicle;

    @Column(name = "data_inici", nullable = false)
    private LocalDate dataInici;

    @Column(name = "data_fi", nullable = false)
    private LocalDate dataFi;

    //TODO Calcular precio total i fiança al crear una reserva teniendo en cuenta el tipo de cliente
    @Column(name = "cost_total", nullable = false)
    private Double costTotal; 

    @Column(name = "fianca", nullable = false)
    private Double fianca;
    
    @Column(name = "estat", nullable = false)
    private boolean estat;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "data_lliurar")
    private LocalDate dataLliurar;

    @Column(name = "descripcio_estat_lliurar")
    private String descripcioEstatLliurar;

    @Column(name = "data_retornar")
    private LocalDate dataRetornar;

    @Column(name =  "hora_inici")
    private LocalTime horaInici;

    @Column(name =  "hora_lliurar")
    private LocalTime horaLliurar;

    @Column(name =  "hora_fi")
    private LocalTime horaFi;

    @Column(name =  "hora_retornar")
    private LocalTime horaRetornar;

}
