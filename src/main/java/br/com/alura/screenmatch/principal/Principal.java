package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private List<DadosSerie> dadosSeries = new ArrayList<>();
    private SerieRepository repository;
    private List<Serie> series;
    private Optional<Serie> serieBuscada;

    public Principal(SerieRepository repository) {
        this.repository = repository;
    }

    public void exibeMenu() {

        var opcao = -1;

        while (opcao != 0) {

            var menu = """
                    
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4 - Buscar série por título
                    5 - Buscar séries por ator
                    6 - Listar as Top 5 séries
                    7 - Buscar séries por gênero
                    8 - Buscar séries por número de temporadas
                    9 - Buscar episódios por título
                    10 - Listar os Top 5 episódios por série
                                    
                    0 - Sair
                                                    
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriesPorAtor();
                    break;
                case 6:
                    listarTop5Series();
                    break;
                case 7:
                    buscarSeriesPorGenero();
                    break;
                case 8:
                    buscarSeriesPorNumeroDeTemporadas();
                    break;
                case 9:
                    buscarEpisodiosPeloTitulo();
                    break;
                case 10:
                    listarTop5EpisodiosPorSerie();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }

        }
    }

    private void listarTop5EpisodiosPorSerie() {
        buscarSeriePorTitulo();
        List<Episodio> topEpisodios = repository.listarTop5EpisodiosPorSerie(serieBuscada);
        topEpisodios.forEach(e -> System.out.printf("Série: %s | Temporada: %s | Episódio %s - %s | Avaliação: %s\n",
                e.getSerie().getTitulo(), e.getTemporada(),
                e.getNumeroEpisodio(), e.getTitulo(), e.getAvaliacao()));
    }

    private void buscarEpisodiosPeloTitulo() {
        System.out.println("Digite o nome do episódio para a busca:");
        var nome = leitura.nextLine();
        List<Episodio> episodios = repository.buscarEpisodiosPorTitulo(nome);
        episodios.forEach(e -> System.out.printf("Série: %s | Temporada: %s | Episódio %s - %s\n",
                e.getSerie().getTitulo(), e.getTemporada(),
                e.getNumeroEpisodio(), e.getTitulo()));
    }

    private void buscarSeriesPorNumeroDeTemporadas() {
        System.out.println("Informe o número máximo de temporadas para a busca: ");
        var totalTemporadas = leitura.nextInt();
        System.out.println("Informe apartir de qual pontuação da avaliação que você deseja buscar as séries: ");
        var avaliacao = leitura.nextDouble();
        List<Serie> seriesBuscadas = repository.buscarSeriesPorTemporadasEAvaliacao(totalTemporadas, avaliacao);
        seriesBuscadas.forEach(System.out::println);
    }

    private void buscarSeriesPorGenero() {
        System.out.println("Informe a categoria/gênero por qual vc deseja buscar a série:");
        var nomeDoGenero = leitura.nextLine();
        Categoria categoria = Categoria.fromEnglish(nomeDoGenero);
        List<Serie> seriesBuscadas = repository.findByGenero(categoria);
        seriesBuscadas.forEach(System.out::println);
    }

    private void listarTop5Series() {
        List<Serie> topSeries = repository.findTop5ByOrderByAvaliacaoDesc();
        topSeries.forEach(s -> System.out.println("Título: " + s.getTitulo() + " | Avaliação: " + s.getAvaliacao()));
    }

    private void buscarSeriesPorAtor() {
        System.out.println("Digite o nome do ator para a busca:");
        var nomeDoAtor = leitura.nextLine();
        System.out.println("Informe apartir de qual pontuação da avaliação que você deseja buscar as séries: ");
        var avaliacao = leitura.nextDouble();
        List<Serie> seriesBuscadas = repository.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeDoAtor, avaliacao);
        seriesBuscadas.forEach(s -> System.out.println("Título: " + s.getTitulo() + " | Avaliação: " + s.getAvaliacao()));
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Informe o nome da série:");
        var nome = leitura.nextLine();

        serieBuscada = repository.findByTituloContainingIgnoreCase(nome);

        if (serieBuscada.isPresent()) {
            System.out.println("Dados da série: " + serieBuscada.get());
        } else {
            System.out.println("Série não encontrada!");
        }
    }

    private void listarSeriesBuscadas() {
        series = repository.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                        .forEach(System.out::println);
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
//        dadosSeries.add(dados);
        repository.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie(){
        listarSeriesBuscadas();
        System.out.println("Informe o nome da série para encontrar seus episódios:");
        var nome = leitura.nextLine();



//        Optional<Serie> serie = series.stream()
//                .filter(s -> s.getTitulo().toLowerCase().contains(nome.toLowerCase()))
//                .findFirst();

        Optional<Serie> serie = repository.findByTituloContainingIgnoreCase(nome);

        if (serie.isPresent()) {
            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repository.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada!");
        }
    }
}