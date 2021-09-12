package com.example.demo.envers.audit.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.stream.Stream;


public interface BookRepository extends JpaRepository<Book, Long>, RevisionRepository<Book, Long, Integer> {

    Stream<Book> findAllByAuthor(String author);

}
