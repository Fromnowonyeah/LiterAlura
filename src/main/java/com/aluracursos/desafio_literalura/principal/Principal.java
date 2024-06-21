package com.aluracursos.desafio_literalura.principal;

import com.aluracursos.desafio_literalura.models.*;
import com.aluracursos.desafio_literalura.repositorio.IAutoresRepository;
import com.aluracursos.desafio_literalura.repositorio.ILibrosRepository;
import com.aluracursos.desafio_literalura.service.ConsumoApi;
import com.aluracursos.desafio_literalura.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoApi consumoApi = new ConsumoApi();
    private ConvierteDatos conversor = new ConvierteDatos();
    private final static String URL_BASE = "https://gutendex.com/books/?search=";

    private IAutoresRepository autoresRepository;
    private ILibrosRepository librosRepository;

    public Principal(IAutoresRepository autoresRepository, ILibrosRepository librosRepository) {
        this.autoresRepository = autoresRepository;
        this.librosRepository = librosRepository;
    }

    public void muestraElMenu () {
        var opcion = -1;
        System.out.println("\n*** Bienvenido, por favor selecciona una opción ***\n");
        while (opcion != 0) {
            var menu = """
                    1 - Buscar libros por título
                    2 - Buscar libros registrados
                    3 - Buscar autores registrados
                    4 - Buscar autores vivos en un determinado año
                    5 - Buscar libros por idioma
                    0 - Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    agregarLibros();
                    break;
                case 2:
                    librosRegistrados();
                    break;
                case 3:
                    autoresRegistrados();
                    break;
                case 4:
                    autoresPorAño();
                    break;
                case 5:
                    listarPorIdioma();
                    break;
                case 0:
                    System.out.println("Saliendo de la aplicación...");
                    break;

                default:
                    System.out.println("Opción inválida, intenta de nuevo");
            }

        }
    }

    private Datos getDatosLibros() {
        var nombreLibro = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreLibro.replace(" ", "+"));
        Datos datosLibros = conversor.obtenerDatos(json, Datos.class);
        return datosLibros;
    }

    private Libros crearLibro(DatosLibros datosLibros, Autores autor) {
        if (autor != null) {
            return new Libros(datosLibros, autor);
        } else {
            System.out.println("\nEl autor no existe, no se puede generar el libro\n");
            return null;
        }
    }

    private  void agregarLibros() {
        System.out.println("\nEscribe el título del libro que deseas buscar:\n");
        Datos datos = getDatosLibros();
        if (!datos.resultados().isEmpty()) {
            DatosLibros datosLibro = datos.resultados().get(0);
            DatosAutores datosAutores = datosLibro.autor().get(0);
            Libros libro = null;
            Libros libroRepositorio = librosRepository.findByTitulo(datosLibro.titulo());
            if (libroRepositorio != null) {
                System.out.println("\nEste libro ya está registrado en la base de datos\n");
                System.out.println(libroRepositorio.toString());
            } else {
                Autores autorRepositorio = autoresRepository.findByNameIgnoreCase(datosLibro.autor().get(0).nombreAutor());
                if (autorRepositorio != null) {
                    libro = crearLibro(datosLibro, autorRepositorio);
                    librosRepository.save(libro);
                    System.out.println("\n**** Libro agregado ****\n");
                    System.out.println(libro);
                } else {
                    Autores autor = new Autores(datosAutores);
                    autor = autoresRepository.save(autor);
                    libro = crearLibro(datosLibro, autor);
                    librosRepository.save(libro);
                    System.out.println("\n**** Libro agregado ****\n");
                    System.out.println(libro);
                }
            }
        } else {
            System.out.println("\nEl libro no se encuentra en la API Gutendex, intenta con otro por favor\n");
        }
    }

    private void librosRegistrados() {
        List<Libros> libros = librosRepository.findAll();
        if (libros.isEmpty()) {
            System.out.println("\nNo hay libros en la base de datos\n");
            return;
        }
        System.out.println("\n**** Los libros registrados en la base de datos son: ****\n");
        libros.stream()
                .sorted(Comparator.comparing(Libros::getTitulo))
                .forEach(System.out::println);
    }

    private void autoresRegistrados() {
        List<Autores> autores = autoresRepository.findAll();
        if (autores.isEmpty()) {
            System.out.println("\nNo hay autores en la base de datos\n");
            return;
        }
        System.out.println("\n**** Los autores registrados en la base de datos son: ****\n");
        autores.stream()
                .sorted(Comparator.comparing(Autores::getName))
                .forEach(System.out::println);
    }

    private void autoresPorAño() {
        System.out.println("\nEscribe el año en el cual deseas buscar:\n");
        var año = teclado.nextInt();
        teclado.nextLine();
        if(año < 0) {
            System.out.println("\nEl año tiene que se después de Cristo, intente de nuevo\n");
            return;
        }
        List<Autores> autoresPorAño = autoresRepository.findByAñoNacimientoLessThanEqualAndAñoMuerteGreaterThanEqual(año, año);
        if (autoresPorAño.isEmpty()) {
            System.out.println("\nNo existen autores en ese año\n");
            return;
        }
        System.out.println("\n**** Los autores registrados en el año " + año + " son: ****\n");
        autoresPorAño.stream()
                .sorted(Comparator.comparing(Autores::getName))
                .forEach(System.out::println);
    }

    private void listarPorIdioma() {
        System.out.println("\nIndica el idioma por el cual deseas buscar:\n");
        String menu = """
                en - Inglés
                es - Español
                pt - Portugués
                fr - Francés
                """;
        System.out.println(menu);
        var idioma = teclado.nextLine();
        if (!idioma.equals("es") && !idioma.equals("en") && !idioma.equals("fr") && !idioma.equals("pt")) {
            System.out.println("\nIdioma no encontrado, por favor intenta con otro\n");
            return;
        }
        List<Libros> librosPorIdioma = librosRepository.findByLenguajesContaining(idioma);
        if (librosPorIdioma.isEmpty()) {
            System.out.println("\nNo existen libros en la base de datos en ese idioma\n");
            return;
        }
        System.out.println("\n**** Los libros escritos en el idioma que seleccionaste son: ****\n");
        librosPorIdioma.stream()
                .sorted(Comparator.comparing(Libros::getTitulo))
                .forEach(System.out::println);
    }
}
