package vn.id.luannv.lutaco.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import vn.id.luannv.lutaco.enumerate.Period;
import vn.id.luannv.lutaco.util.EnumUtils;

@Converter(autoApply = false)
public class PeriodConverter implements AttributeConverter<Period, String> {

    @Override
    public String convertToDatabaseColumn(Period attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public Period convertToEntityAttribute(String dbData) {
        return dbData == null ? null : EnumUtils.from(Period.class, dbData);
    }
}

