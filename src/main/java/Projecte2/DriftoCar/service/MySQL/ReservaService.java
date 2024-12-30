/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Projecte2.DriftoCar.service.MySQL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Projecte2.DriftoCar.entity.MongoDB.HistoricReserves;
import Projecte2.DriftoCar.entity.MySQL.Client;
import Projecte2.DriftoCar.entity.MySQL.Reserva;
import Projecte2.DriftoCar.entity.MySQL.Vehicle;
import Projecte2.DriftoCar.repository.MongoDB.HistoricReservesRepository;
import Projecte2.DriftoCar.repository.MySQL.ClientRepository;
import Projecte2.DriftoCar.repository.MySQL.ReservaRepository;
import Projecte2.DriftoCar.repository.MySQL.VehicleRepository;

import java.time.temporal.ChronoUnit;

/**
 *
 * @author Mario
 */
@Service
public class ReservaService {

    Logger log = LoggerFactory.getLogger(ClientService.class);

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private HistoricReservesRepository historicReservesRepository;

    /**
     * Aquest metode guarda una nova reserva a a BBDD si el client i la
     * matricula existeixen
     *
     * @param reserva
     * @return
     * @throws Exception
     * @author Mario
     */
    public Reserva altaReserva(Reserva reserva) {
        log.info("S'ha entrat al metode d'altaReserva.");

        // Verificar que el vehículo existe
        Optional<Vehicle> vehicle = vehicleRepository.findByMatricula(reserva.getVehicle().getMatricula());

        // Verificar que el cliente existe
        Optional<Client> client = clientRepository.findByDni(reserva.getClient().getDni());

        if (client.isEmpty()) {
            throw new RuntimeException("El client no existeix");
        }
        if (vehicle.isEmpty()) {
            throw new RuntimeException("El vehicle no existeix");
        }

        Vehicle vehicleNou = vehicle.get();
        if (!vehicleNou.isDisponibilitat()) {
            throw new RuntimeException("El vehicle no esta disponible");
        }

        reserva.setClient(client.get());
        reserva.setVehicle(vehicle.get());

        HistoricReserves historic = new HistoricReserves();
        historic.setIdReserva(reserva.getIdReserva().toString());
        historic.setNomClient(reserva.getClient().getNom());
        historic.setCognomClient(reserva.getClient().getCognoms());
        historic.setDNI(reserva.getClient().getDni());
        historic.setMatricula(reserva.getVehicle().getMatricula());
        historic.setDataInici(reserva.getDataInici());
        historic.setDataFi(reserva.getDataFi());
        historic.setTotalCost(calculPreuReserva(reserva));
        historic.setFianca(calculFianca(reserva));
        historic.setEstat(reserva.isEstat());

        historicReservesRepository.save(historic);
        // Guardar la reserva en la base de datos
        return reservaRepository.save(reserva);
    }

    public List<Reserva> llistarReservas() {
        return reservaRepository.findAll();
    }

    public Optional<Reserva> cercaPerId(Long idReserva) {

        return reservaRepository.findById(idReserva);
    }

    public List<Reserva> cercarReserva(String email, Long id_reserva, String matricula) {
        log.debug("cercarReserva() - email: " + email + ", id_reserva: " + id_reserva + ", matricula: " + matricula);
        return reservaRepository.cercarReserves(id_reserva, email, matricula);

    }

    public void modificarReserva(Reserva reserva) {
        log.info("Guardant reserva amb ID: " + reserva.getIdReserva());
        reservaRepository.save(reserva);
        log.info("Reserva guardada correctament.");
    }

    public List<Reserva> obtenirReservesPerMatricula(String matricula) {
        return reservaRepository.findByVehicleMatriculaEstat(matricula);
    }

    public Reserva desactivarReserva(Long idReserva) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RuntimeException("Reserva " + idReserva + " no trobada: "));
        if (reserva.isEstat()) {
            reserva.setEstat(false); // Marca la reserva como inactiva
            HistoricReserves historic = historicReservesRepository.findById(reserva.getIdReserva().toString())
                    .orElseThrow(() -> new RuntimeException(
                            "Reserva " + idReserva + " no trobada en el historic de reserves: "));
            historic.setEstat(false);
            historicReservesRepository.save(historic);
            return reservaRepository.save(reserva);
        } else {
            throw new RuntimeException("La reserva ja està anul·lada");
        }
    }

    public double calculFianca(Reserva reserva) {

        Client client = reserva.getClient();

        boolean esClientPremium = client != null && client.isReputacio();
        double fiancaBase = reserva.getVehicle().getFianca();
        log.info("La fiança inicial és: " + fiancaBase);
        double fiancaAmbDescompte = esClientPremium ? fiancaBase * 0.75 : fiancaBase;
        log.info("La fiança calculada és: " + fiancaAmbDescompte);
        return fiancaAmbDescompte;
    }

    public double calculPreuTotal(Reserva reserva) {
        if (reserva.getHoraInici() == null || reserva.getHoraLliurar() == null
                || reserva.getHoraFi() == null || reserva.getHoraRetornar() == null
                || reserva.getDataLliurar() == null || reserva.getDataRetornar() == null) {
            throw new IllegalArgumentException("Les dates i hores no poden ser null");
        }

        log.info("Data Lliurar: " + reserva.getDataLliurar());
        log.info("Hora Lliurar: " + reserva.getHoraLliurar());
        log.info("Data Retornar: " + reserva.getDataRetornar());
        log.info("Hora Retornar: " + reserva.getHoraRetornar());

        // Combinar dates i hores
        LocalDateTime iniciLliurament = LocalDateTime.of(reserva.getDataLliurar(), reserva.getHoraLliurar());
        LocalDateTime fiRetorn = LocalDateTime.of(reserva.getDataRetornar(), reserva.getHoraRetornar());

        // Diferència total en hores
        long horesTotals = ChronoUnit.HOURS.between(iniciLliurament, fiRetorn);

        // Penalització per retard
        LocalDateTime fiPrevist = LocalDateTime.of(reserva.getDataFi(), reserva.getHoraFi());
        long horesRetard = ChronoUnit.HOURS.between(fiPrevist, fiRetorn);
        horesRetard = Math.max(0, horesRetard); // Assegurar que no sigui negatiu

        log.info("Hores Totals: " + horesTotals);
        log.info("Hores Retard: " + horesRetard);

        // Cost per hora
        double costHora = reserva.getVehicle().getCostHora();
        log.info("Cost Hora Vehicle: " + costHora);

        double fianca = calculFianca(reserva);
        log.info("Fiança Calculada: " + fianca);

        // Cost total sense penalització
        double costTotalSensePenalitzacio = horesTotals * costHora;

        // Penalització pel retard
        double costPenalitzacio = horesRetard * costHora;

        // Càlcul del preu total
        double preuTotal = costTotalSensePenalitzacio + costPenalitzacio + fianca;

        return Math.max(preuTotal, 0);
    }

    public double calculPreuReserva(Reserva reserva) {

        log.info("Data Inici: " + reserva.getDataInici());
        log.info("Hora Inici: " + reserva.getHoraInici());
        log.info("Data Fi: " + reserva.getDataFi());
        log.info("Hora Fi: " + reserva.getHoraFi());

        // Combinar dates i hores
        LocalDateTime inici = LocalDateTime.of(reserva.getDataInici(), reserva.getHoraInici());
        LocalDateTime fi = LocalDateTime.of(reserva.getDataFi(), reserva.getHoraFi());

        // Diferència total en hores
        long horesTotals = ChronoUnit.HOURS.between(inici, fi);

        // Cost per hora
        double costHora = reserva.getVehicle().getCostHora();
        log.info("Cost Hora Vehicle: " + costHora);

        double fianca = calculFianca(reserva);
        log.info("Fiança Calculada: " + fianca);

        double costTotalSensePenalitzacio = horesTotals * costHora;

        double preuTotal = costTotalSensePenalitzacio + fianca;

        return Math.max(preuTotal, 0);
    }

    public List<Reserva> cercarReservesPerClient(String username, String searchEmail, Long searchId_reserva,
            String searchMatricula) {
        return reservaRepository.findByClientUsuariAndFilters(username, searchEmail, searchId_reserva, searchMatricula);
    }

    public List<Reserva> cercarReservesPerAgent(String username, String searchEmail, Long searchId_reserva,
            String searchMatricula) {
        List<String> matriculesVehicles = vehicleRepository.findMatriculesByAgentUsuari(username);
        return reservaRepository.findByAgentAndFilters(username, matriculesVehicles, searchEmail, searchId_reserva,
                searchMatricula);
    }

    public List<String> obtenerFechasNoDisponibles(String matricula) {
        List<Reserva> reservas = reservaRepository.findByVehicleMatriculaEstat(matricula);
        List<String> fechasNoDisponibles = new ArrayList<>();

        for (Reserva reserva : reservas) {
            LocalDate fechaInicio = reserva.getDataInici();
            LocalDate fechaFin = reserva.getDataFi();

            while (!fechaInicio.isAfter(fechaFin)) {
                fechasNoDisponibles.add(fechaInicio.toString());
                fechaInicio = fechaInicio.plusDays(1);
            }
        }

        return fechasNoDisponibles;
    }
}
