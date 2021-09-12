package com.example.demo.envers.audit.book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan.Filter;

import com.example.demo.envers.audit.RepositoryConfiguration;
import com.example.demo.envers.audit.book.Book;
import com.example.demo.envers.audit.book.BookRepository;
import com.example.demo.envers.audit.config.AuditConfiguration;
import com.example.demo.envers.audit.config.AuditorAwareImpl;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;


@DataJpaTest(includeFilters = @Filter(
        type = ASSIGNABLE_TYPE,
        classes = { AuditorAwareImpl.class, AuditConfiguration.class, RepositoryConfiguration.class }
))
class BookRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BookRepository repository;

    private Book book;

    @BeforeEach
    public void save() {
        book = em.persistAndFlush(
        		new Book("Rudyard Kipling", "Jungle Book")
               // Book.builder().author("Rudyard Kipling").title("Jungle Book").build()
        );
    }

    @Test
    void findAllByAuthor() {
        Stream<Book> booksByAuthor = repository.findAllByAuthor("Rudyard Kipling");

        assertThat(booksByAuthor)
                .isNotEmpty()
                .extracting(Book::getAuthor, Book::getTitle)
                .containsExactly(tuple("Rudyard Kipling", "Jungle Book"));
    }

    @Test
    void hasAuditInformation() {
        assertThat(book)
                .extracting(Book::getCreatedBy, Book::getCreatedDate, Book::getLastModifiedBy, Book::getLastModifiedDate, Book::getVersion)
                .isNotNull();
    }
}
