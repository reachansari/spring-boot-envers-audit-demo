package com.example.demo.envers.audit.book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.envers.repository.support.DefaultRevisionMetadata;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;

import com.example.demo.envers.audit.book.Book;
import com.example.demo.envers.audit.book.BookRepository;
import com.example.demo.envers.audit.config.AuditRevisionEntity;

import java.util.Iterator;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class BookRepositoryRevisionsTest {

    @Autowired
    private BookRepository repository;

    private Book book;

    @BeforeEach
    public void save() {
        repository.deleteAll();

        book = repository.save(
        		new Book("Rudyard Kipling","Jungle Book")
                //Book.builder().author("Rudyard Kipling").title("Jungle Book").build()
        );
    }

    @Test
    void initialRevision() {
        Revisions<Integer, Book> revisions = repository.findRevisions(book.getId());

        assertThat(revisions)
                .isNotEmpty()
                .allSatisfy(revision -> assertThat(revision.getEntity())
                        .extracting(Book::getId, Book::getAuthor, Book::getTitle)
                        .containsExactly(book.getId(), book.getAuthor(), book.getTitle())
                )
                .allSatisfy(revision -> {
                            DefaultRevisionMetadata metadata = (DefaultRevisionMetadata) revision.getMetadata();
                            AuditRevisionEntity revisionEntity = metadata.getDelegate();

                            assertThat(revisionEntity.getUsername()).isEqualTo("wade.wilson");
                        }
                );
    }

    @Test
    void updateIncreasesRevisionNumber() {
        book.setTitle("If");

        repository.save(book);

        Optional<Revision<Integer, Book>> revision = repository.findLastChangeRevision(book.getId());

        assertThat(revision)
                .isPresent()
                .hasValueSatisfying(rev ->
                        assertThat(rev.getRevisionNumber()).hasValue(3)
                )
                .hasValueSatisfying(rev ->
                        assertThat(rev.getEntity())
                                .extracting(Book::getTitle)
                                .isEqualTo("If")
                );
    }

    @Test
    void deletedItemWillHaveRevisionRetained() {
        repository.delete(book);

        Revisions<Integer, Book> revisions = repository.findRevisions(book.getId());

        assertThat(revisions).hasSize(2);

        Iterator<Revision<Integer, Book>> iterator = revisions.iterator();

        Revision<Integer, Book> initialRevision = iterator.next();
        Revision<Integer, Book> finalRevision = iterator.next();

        assertThat(initialRevision)
                .satisfies(rev ->
                        assertThat(rev.getEntity())
                                .extracting(Book::getId, Book::getAuthor, Book::getTitle)
                                .containsExactly(book.getId(), book.getAuthor(), book.getTitle())
                );

        assertThat(finalRevision)
                .satisfies(rev -> assertThat(rev.getEntity())
                        .extracting(Book::getId, Book::getTitle, Book::getAuthor)
                        .containsExactly(book.getId(), null, null)
                );
    }
}
