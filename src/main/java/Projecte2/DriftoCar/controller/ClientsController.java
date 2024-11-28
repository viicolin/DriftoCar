/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Projecte2.DriftoCar.controller;

import Projecte2.DriftoCar.entity.MySQL.Client;
import Projecte2.DriftoCar.service.MySQL.ClientService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author Anna
 */
@Controller
@RequestMapping("/clients")
public class ClientsController {

    @Autowired
    private ClientService clientService;
    

    @GetMapping("/llistar")
    public String llistarClients(Model model) {
        
        List<Client> clients = clientService.llistarClients();
        model.addAttribute("clients", clients);
        
        return "client-llistar";
        
    }

    @GetMapping("/esborrar/{dni}")
    public String esborrarClients(@PathVariable("dni") String dni, Client client) {

        clientService.baixaClient(client);

        return "redirect:/clients/llistar";

    }
    
    @GetMapping("/modificar/{dni}")
    public String modificarClients(@PathVariable("dni") String dni, Model model){
        
        Client client = clientService.obtenirClientPerDni(dni);
        if (client == null) {
            throw new RuntimeException("No existeix cap client amb aquest DNI.");
            
        }
        model.addAttribute("client", client);        
        return "client-modificar";
    }
    
    @PostMapping("/modificar")
    public String guardarClientModificat(@ModelAttribute("client") Client client){
        
        clientService.modificarClient(client);
        return "redirect:/clients/llistar";
    }
   
}