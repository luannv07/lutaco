package vn.id.luannv.lutaco.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Base interface service for system.
 * @param <E> FilterRequestDTO
 * @param <R> ResponseDTO
 * @param <T> RequestDTO
 * @param <I> PrimaryKey Datatype
 */
public interface BaseService<E, R, T, I> {
    R create(T request);
    R getDetail(I id);
    Page<R> search(E request, Integer page, Integer size);
    R update(I id, T request);
    void deleteById(I id);
}
