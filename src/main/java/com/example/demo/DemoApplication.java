package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Clase principal de la aplicación Spring Boot Demo.
 * 
 * @author Miguel Angel Machuca Yavita
 * @version 1.0
 */

@SpringBootApplication
@RestController
public class DemoApplication {
    
    /**
     * Método principal que inicia la aplicación Spring Boot.
     *
     * @param args argumentos de línea de comandos (deben ser final)
     */

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    /**
         * Endpoint de health check para verificar el estado de la aplicación.
         * Este método puede ser sobrescrito por subclases para proporcionar
         * implementaciones personalizadas de health check.
         *
         * @return mensaje indicando que la aplicación está funcionando
     */

    @GetMapping("/health")
    public String healthCheck() {
        return "OK, está bien";
    }
}
