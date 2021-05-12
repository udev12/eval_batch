package com.ipiecoles.eval_batch.repository;

import com.ipiecoles.eval_batch.model.Commune;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Dans notre repository, on définit entre-autres, les méthodes "countDistinctCodePostal()" et "countDistinctNom".
 * On doit hériter de la classe "PagingAndSortingRepository" (au lieu de la classe "JpaRepository"),
 * si on veut lire avec la classe "RepositoryItemReader".
 */
public interface CommuneRepository extends PagingAndSortingRepository<Commune, String> {
    @Query("Select count(distinct c.codePostal) from Commune c")
    long countDistinctCodePostal();

    @Query("select count(distinct c.nom) from Commune c")
    long countDistinctNom();

    @Override
    Page<Commune> findAll(Pageable pageable);

    Page<Commune> findAllByNomLike(String nom, Pageable pageable);

//    @Override
//    default List<Commune> findAll() {
//        return null;
//    }

//    @Override
//    default List<Commune> findAll(Sort sort) {
//        return null;
//    }

//    @Override
//    default List<Commune> findAllById(Iterable<String> iterable) {
//        return null;
//    }
//
//    @Override
//    default <S extends Commune> List<S> saveAll(Iterable<S> iterable) {
//        return null;
//    }
//
//    @Override
//    default void flush() {
//
//    }
//
//    @Override
//    default <S extends Commune> S saveAndFlush(S s) {
//        return null;
//    }
//
//    @Override
//    default void deleteInBatch(Iterable<Commune> iterable) {
//
//    }
//
//    @Override
//    default void deleteAllInBatch() {
//
//    }
//
//    @Override
//    default Commune getOne(String s) {
//        return null;
//    }
//
//    @Override
//    default <S extends Commune> List<S> findAll(Example<S> example) {
//        return null;
//    }
//
//    @Override
//    default <S extends Commune> List<S> findAll(Example<S> example, Sort sort) {
//        return null;
//    }
//
////    @Override
////    default Page<Commune> findAll(Pageable pageable) {
////        return null;
////    }
//
//    @Override
//    default <S extends Commune> S save(S s) {
//        return null;
//    }
//
//    @Override
//    default Optional<Commune> findById(String s) {
//        return Optional.empty();
//    }
//
//    @Override
//    default boolean existsById(String s) {
//        return false;
//    }
//
//    @Override
//    default long count() {
//        return 0;
//    }
//
//    @Override
//    default void deleteById(String s) {
//
//    }
//
//    @Override
//    default void delete(Commune commune) {
//
//    }
//
//    @Override
//    default void deleteAll(Iterable<? extends Commune> iterable) {
//
//    }
//
//    @Override
//    default void deleteAll() {
//
//    }
//
//    @Override
//    default <S extends Commune> Optional<S> findOne(Example<S> example) {
//        return Optional.empty();
//    }
//
//    @Override
//    default <S extends Commune> Page<S> findAll(Example<S> example, Pageable pageable) {
//        return null;
//    }
//
//    @Override
//    default <S extends Commune> long count(Example<S> example) {
//        return 0;
//    }
//
//    @Override
//    default <S extends Commune> boolean exists(Example<S> example) {
//        return false;
//    }
}
