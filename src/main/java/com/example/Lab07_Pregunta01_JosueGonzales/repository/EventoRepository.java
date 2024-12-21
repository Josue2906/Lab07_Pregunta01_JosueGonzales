package com.example.Lab07_Pregunta01_JosueGonzales.repository;

import com.example.Lab07_Pregunta01_JosueGonzales.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRepository extends JpaRepository<Evento, Long>{
    
}
