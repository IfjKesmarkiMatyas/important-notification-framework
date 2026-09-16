package com.notif.common.domain.decision;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DecisionEngineModeConverter implements AttributeConverter<DecisionEngineMode, String> {

    @Override
    public String convertToDatabaseColumn(DecisionEngineMode attribute) {
        return attribute == null ? DecisionEngineMode.native_.toWire() : attribute.toWire();
    }

    @Override
    public DecisionEngineMode convertToEntityAttribute(String dbData) {
        return DecisionEngineMode.fromWire(dbData);
    }
}
