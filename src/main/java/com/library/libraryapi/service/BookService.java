package com.library.libraryapi.service;

import com.library.libraryapi.domain.entity.Book;
import com.library.libraryapi.domain.enums.BookStatus;
import com.library.libraryapi.dto.request.BookRequest;
import com.library.libraryapi.dto.response.BookResponse;
import com.library.libraryapi.exception.CustomException;
import com.library.libraryapi.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    public Page<BookResponse> getBooks(String keyword, String searchType, Pageable pageable) {
        Page<Book> books;

        if (!StringUtils.hasText(keyword)) {
            books = bookRepository.findAll(pageable);
        } else {
            books = switch (searchType == null ? "all" : searchType) {
                case "title"  -> bookRepository.findByTitleContainingIgnoreCase(keyword, pageable);
                case "author" -> bookRepository.findByAuthorContainingIgnoreCase(keyword, pageable);
                default       -> bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword, pageable);
            };
        }

        return books.map(BookResponse::from);
    }

    public BookResponse getBook(Long id) {
        return BookResponse.from(findById(id));
    }

    @Transactional
    public BookResponse createBook(BookRequest request) {
        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .publisher(request.getPublisher())
                .isbn(request.getIsbn())
                .build();
        return BookResponse.from(bookRepository.save(book));
    }

    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = findById(id);
        book.update(request.getTitle(), request.getAuthor(), request.getPublisher(), request.getIsbn());
        return BookResponse.from(bookRepository.save(book));
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = findById(id);
        if (book.getStatus() == BookStatus.LOANED) {
            throw new CustomException("대출 중인 도서는 삭제할 수 없습니다.", HttpStatus.CONFLICT);
        }
        bookRepository.delete(book);
    }

    private Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new CustomException("도서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }
}
