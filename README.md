# spring-boot-envers-audit-demo
Spring Data Jpa provides rough audit information for any CRUD action performed on the entities.


## Enable Entity Audit
By annotating an `@Entity` with `@Audited`, we are informing Spring that we would like respective entity to be audited. 
The following example shows that we want all activities related to Book to be audited:

```java
@Entity
@Audited
public class Book {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    private String author;

    @NotBlank
    private String title;

}
```

Next is to extend a `Repository` class in order to allow us to utilise audit revision features. This can be done by extending
RevisionRepository interface to our `Repository` class. An example can be seen in BookRepository:

```java
public interface BookRepository extends JpaRepository<Book, Long>, RevisionRepository<Book, Long, Integer> {

}
```

## Junit
We will be utilising on `@SpringBootTest` to verify that our implementation works.

### Upon Creation an Initial Revision is Created
```java
@SpringBootTest
class BookRepositoryRevisionsTest {

    @Autowired
    private BookRepository repository;
    
    @Test
    void initialRevision() {
        Book book = repository.save(
                             Book.builder().author("Rudyard Kipling").title("Jungle Book").build()
                     );
        
        Revisions<Integer, Book> revisions = repository.findRevisions(book.getId());

        assertThat(revisions)
                .isNotEmpty()
                .allSatisfy(revision -> assertThat(revision.getEntity())
                        .extracting(Book::getId, Book::getAuthor, Book::getTitle)
                        .containsExactly(book.getId(), book.getAuthor(), book.getTitle())
                );
    }
}
```

### Revision Number Will Be Increase and Latest Revision is Available
```java
@SpringBootTest
class BookRepositoryRevisionsTest {

    @Autowired
    private BookRepository repository;
    
    @Test
    void updateIncreasesRevisionNumber() {
        Book book = repository.save(
                             Book.builder().author("Rudyard Kipling").title("Jungle Book").build()
                     );
    
        book.setTitle("If");

        repository.save(book);

        Optional<Revision<Integer, Book>> revision = repository.findLastChangeRevision(book.getId());

        assertThat(revision)
                .isPresent()
                .hasValueSatisfying(rev ->
                        assertThat(rev.getRevisionNumber()).hasValue(2)
                )
                .hasValueSatisfying(rev ->
                        assertThat(rev.getEntity())
                                .extracting(Book::getTitle)
                                .containsOnly("If")
                );
    }
}
```

### Upon Deletion All Entity Information Will be Removed Except its ID
```java
@SpringBootTest
class BookRepositoryRevisionsTest {

    @Autowired
    private BookRepository repository;
    
    @Test
    void deletedItemWillHaveRevisionRetained() {
        Book book = repository.save(
                             Book.builder().author("Rudyard Kipling").title("Jungle Book").build()
                     );

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
```
## Libraries used
- Spring Boot
- Spring Configuration
- Spring REST Controller
- Spring JPA
- H2
- Development Tools


## Compilation Command
- `mvn clean install` - Plain maven clean and install



