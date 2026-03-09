package hackhub.repository;

import java.util.Optional;

/**
 * Interfaccia base per tutti i repository del dominio.
 * Definisce l'operazione di ricerca per identificatore, comune a ogni repository.
 * <p>
 * Estende questa interfaccia per aggiungere operazioni specifiche per ogni aggregato
 * (es. query di dominio, metodi di scrittura) rispettando il principio ISP.
 *
 * @param <T>  tipo dell'entità gestita dal repository
 * @param <ID> tipo dell'identificatore univoco dell'entità
 */
public interface GenericRepository<T, ID> {

    /**
     * Cerca un'entità tramite il suo identificatore.
     *
     * @param id l'identificatore dell'entità
     * @return Optional contenente l'entità se trovata, Optional.empty() altrimenti
     */
    Optional<T> findById(ID id);
}
