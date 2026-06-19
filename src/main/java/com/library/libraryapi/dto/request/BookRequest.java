package com.library.libraryapi.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookRequest {
    private String title;
    private String author;
    private String publisher;
    private String isbn;
}
