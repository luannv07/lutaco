package vn.id.luannv.lutaco.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BaseFilterRequest {

    @Range(min = 1, max = 1000, message = "{validation.field.size_not_in_range}")
    Integer page = 1;

    @Range(min = 1, max = 1000, message = "{validation.field.size_not_in_range}")
    Integer size = 10;

    String sortBy = "createdAt";

    Sort.Direction sortDirection = Sort.Direction.DESC;

    public Pageable pageable() {

        String validatedSortBy = getAllowedSortFields().contains(sortBy)
                ? sortBy
                : getDefaultSortBy();

        return PageRequest.of(
                page - 1,
                size,
                Sort.by(sortDirection, validatedSortBy)
        );
    }

    protected Set<String> getAllowedSortFields() {
        return Set.of("createdAt", "updatedAt");
    }

    protected String getDefaultSortBy() {
        return "createdAt";
    }
}