package com.multipagos.pagos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class PagosApplication implements WebMvcConfigurer {

    // Ruta temporal para almacenar archivos generados (QRs y recibos)
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    // Directorios para las imágenes de QR y recibos
    private static final String QRS_DIR = "/qrs/";
    private static final String RECEIPTS_DIR = "/receipts/";

    public static void main(String[] args) {
        SpringApplication.run(PagosApplication.class, args);
    }

    /**
     * Configura los manejadores de recursos estáticos para servir archivos generados.
     * 
     * @param registry el registro de recursos para agregar las rutas
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configuración para los archivos QR generados
        registry.addResourceHandler(QRS_DIR + "**")
                .addResourceLocations("file:" + TEMP_DIR + QRS_DIR);

        // Configuración para los recibos generados
        registry.addResourceHandler(RECEIPTS_DIR + "**")
                .addResourceLocations("file:" + TEMP_DIR + RECEIPTS_DIR);
    }
}
