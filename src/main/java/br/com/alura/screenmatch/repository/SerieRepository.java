package br.com.alura.screenmatch.repository;

import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie, Long> {

    Optional<Serie> findByTituloContainingIgnoreCase(String nome);

    List<Serie> findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(String nomeDoAtor, Double avaliacao);

    List<Serie> findTop5ByOrderByAvaliacaoDesc();

    List<Serie> findByGenero(Categoria categoria);

    List<Serie> findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(Integer totalTemporadas, Double avaliacao);

    @Query("select s from Serie s where s.totalTemporadas <= :totalTemporadas and s.avaliacao >= :avaliacao")
    List<Serie> buscarSeriesPorTemporadasEAvaliacao(Integer totalTemporadas, Double avaliacao);

    @Query("select e from Serie s join s.episodios e where e.titulo ILIKE %:nome%")
    List<Episodio> buscarEpisodiosPorTitulo(String nome);

    @Query("select e from Serie s join s.episodios e where e.serie = :serieBuscada order by e.avaliacao desc limit 5")
    List<Episodio> listarTop5EpisodiosPorSerie(Optional<Serie> serieBuscada);
}
