package tn.esprit.forestfirewatch.ressources.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.forestfirewatch.ressources.entity.Ressource;
import tn.esprit.forestfirewatch.ressources.entity.StatutRessource;
import tn.esprit.forestfirewatch.ressources.entity.TypeRessource;

import java.util.List;

public interface RessourceRepository extends JpaRepository<Ressource, Long> {

    List<Ressource> findByTypeAndStatut(TypeRessource type, StatutRessource statut);

    List<Ressource> findByGouvernoratAndStatut(String gouvernorat, StatutRessource statut);

    List<Ressource> findByGouvernorat(String gouvernorat);

    List<Ressource> findByStatut(StatutRessource statut);

    long countByUniteIdAndTypeAndStatut(Long uniteId, TypeRessource type, StatutRessource statut);
}
