package com.example.Lab07_Pregunta01_JosueGonzales.controller;

import com.example.Lab07_Pregunta01_JosueGonzales.model.Evento;
import com.example.Lab07_Pregunta01_JosueGonzales.service.EventoService;
import com.itextpdf.io.source.ByteArrayOutputStream;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import jakarta.servlet.http.HttpServletResponse;
import java.time.format.DateTimeFormatter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import static org.thymeleaf.util.temporal.TemporalObjects.temporal;

@Controller
@RequestMapping("/eventos")
public class EventoController {
    
    private final EventoService service;
    
    public EventoController(EventoService service) {
        this.service = service;
    }
    
    @GetMapping
    public String listarEventos(Model model) {
        model.addAttribute("eventos", service.listarTodos());
        return "listarEvento"; // Vista que muestra todos los eventos
    }
    
    // Mostrar el formulario para crear un nuevo evento
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("evento", new Evento());
        return "formularioEvento"; // Vista para el formulario de nuevo evento
    }
    
    // Guardar un nuevo evento
    @PostMapping
    public String guardarEvento(@ModelAttribute Evento evento) {
        service.guardar(evento);
        return "redirect:/eventos"; // Redirigir a la lista de eventos después de guardar
    }
    
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Evento evento = service.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("ID inválido: " + id));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        String fechaFormateada = evento.getFecha().format(formatter);
        model.addAttribute("evento", evento);
        model.addAttribute("fechaFormateada", fechaFormateada);
        return "formularioEvento";
    }
    
    @GetMapping("/eliminar/{id}")
    public String eliminarEvento(@PathVariable Long id) {
        service.eliminar(id);
        return "redirect:/eventos"; // Redirigir a la lista de eventos después de eliminar
    }
    
    @GetMapping("/reporte/pdf")
    public void generarReporte(HttpServletResponse response) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=reporte eventos.pdf");
        
        PdfWriter writer = new PdfWriter(response.getOutputStream());
        
        Document document = new Document(new PdfDocument(writer));
        
        document.add(new Paragraph("Reporte de Eventos").setBold().setFontSize(18));
        
        // Crear una tabla con 5 columnas (ID, Titulo, Fecha y Hora, Lugar)
        Table table = new Table(5); // 5 columnas
        table.addCell("ID");
        table.addCell("Titulo");
        table.addCell("Fecha y Hora");
        table.addCell("Lugar");
        table.addCell("Descripción");
        
        service.listarTodos().forEach(evento -> {
            table.addCell(evento.getId().toString());
            table.addCell(evento.getTitulo());
            table.addCell(evento.getFecha().toString()); // Se asume que 'fecha' es un tipo Date
            table.addCell(evento.getLugar());
            table.addCell(evento.getDescripcion());
        });
        document.add(table);
        document.close();
    }
    
    // Generar reporte Excel
    @GetMapping("/reporte/excel")
    public ResponseEntity<byte[]> generarReporteExcel() throws Exception {
        try {
            // Configuración de cabeceras
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.add("Content-Disposition", "attachment; filename=evento_reporte.xlsx");

            // Crear el archivo Excel
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Eventos");

            // Crear la fila de encabezado
            Row header = sheet.createRow(0);
            String[] headersArray = {"ID", "Titulo", "Fecha y Hora", "Lugar", "Descripcion"};
            for (int i = 0; i < headersArray.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headersArray[i]);

                // Estilo para los encabezados
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                cell.setCellStyle(headerStyle);
            }

            // Obtener los eventos y agregarlos a las filas
            int rowNum = 1;
            for (Evento evento : service.listarTodos()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(evento.getId());
                row.createCell(1).setCellValue(evento.getTitulo());
                row.createCell(2).setCellValue(evento.getFecha().toString());
                row.createCell(3).setCellValue(evento.getLugar());
                row.createCell(4).setCellValue(evento.getDescripcion());
            }

            // Ajustar el tamaño de las columnas
            for (int i = 0; i < headersArray.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Escribir el archivo en un array de bytes
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            workbook.write(byteArrayOutputStream);
            workbook.close();

            // Retornar el archivo como una respuesta HTTP
            byte[] content = byteArrayOutputStream.toByteArray();
            return new ResponseEntity<>(content, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace(); // Loguear el error en el servidor
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
