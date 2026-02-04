package vn.id.luannv.lutaco.mapper;

import org.mapstruct.*;
import vn.id.luannv.lutaco.dto.request.TransactionRequest;
import vn.id.luannv.lutaco.dto.response.TransactionResponse;
import vn.id.luannv.lutaco.entity.Transaction;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transactionType", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "wallet", ignore = true)
    Transaction toEntity(TransactionRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.categoryName")
    @Mapping(target = "walletId", source = "wallet.id")
    @Mapping(target = "walletName", source = "wallet.walletName")
    TransactionResponse toResponse(Transaction entity);

    @Mapping(target = "transactionType", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "wallet", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Transaction entity, TransactionRequest request);
}
