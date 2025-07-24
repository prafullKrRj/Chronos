package com.prafullkumar.chronos.core

interface Mapper<Data, Domain> {
    fun mapToDomain(data: Data): Domain

    fun mapToData(domain: Domain): Data

    fun mapListToDomain(dataList: List<Data>): List<Domain> {
        return dataList.map { mapToDomain(it) }
    }

    fun mapListToData(domainList: List<Domain>): List<Data> {
        return domainList.map { mapToData(it) }
    }
}