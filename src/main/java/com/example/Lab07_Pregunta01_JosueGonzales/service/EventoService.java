package com.example.Lab07_Pregunta01_JosueGonzales.service;

import com.example.Lab07_Pregunta01_JosueGonzales.model.Evento;
import com.example.Lab07_Pregunta01_JosueGonzales.repository.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;

    // Listar todos los eventos
    public List<Evento> listarTodos() {
        return eventoRepository.findAll();
    }
    
    // Guardar un evento (crear o actualizar)
    public Evento guardar(Evento evento) {
        return eventoRepository.save (evento);
    }
    
    // Buscar un evento por su ID
    public Optional<Evento> buscarPorId(Long id) {
        return eventoRepository.findById(id);
    }

    // Eliminar un evento por su ID
    public void eliminar(Long id) {
        eventoRepository.deleteById(id);
    }
}

