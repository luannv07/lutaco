package vn.id.luannv.lutaco.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.id.luannv.lutaco.dto.request.RecurringTransactionRequest;
import vn.id.luannv.lutaco.dto.response.RecurringTransactionResponse;
import vn.id.luannv.lutaco.entity.RecurringTransaction;

@Mapper(componentModel = "spring")
public interface RecurringTransactionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transaction", ignore = true)
    @Mapping(target = "nextDate", ignore = true)
    RecurringTransaction toEntity(RecurringTransactionRequest request);

    @Mapping(source = "transaction.id", target = "transactionId")
    RecurringTransactionResponse toResponse(RecurringTransaction entity);

    @Mapping(target = "transaction", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "nextDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget RecurringTransaction entity, RecurringTransactionRequest request);
}
