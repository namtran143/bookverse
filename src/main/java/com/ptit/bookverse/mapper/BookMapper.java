package com.ptit.bookverse.mapper;

import com.ptit.bookverse.dto.BookRequest;
import com.ptit.bookverse.dto.BookResponse;
import com.ptit.bookverse.entity.Book;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BookMapper {
    BookResponse toResponse(Book book);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "coverPath", ignore = true)
    Book toEntity(BookRequest request);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "coverPath", ignore = true)
    void update(BookRequest request, @MappingTarget Book book);
}
