package com.library.libraryapi.dto.response;

import com.library.libraryapi.domain.entity.Book;
import com.library.libraryapi.domain.enums.BookStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String publisher;
    private String isbn;
    private BookStatus status;
    private LocalDateTime createdAt;

    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getIsbn(),
                book.getStatus(),
                book.getCreatedAt()
        );
    }
}
