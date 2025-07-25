package com.prafullkumar.chronos.data.mappers

import com.google.firebase.Timestamp
import com.prafullkumar.chronos.core.Mapper
import com.prafullkumar.chronos.data.dtos.ReminderDto
import com.prafullkumar.chronos.domain.model.Reminder

class ReminderMapper : Mapper<ReminderDto, Reminder> {
    override fun mapToDomain(data: ReminderDto): Reminder {
        return Reminder(
            data.uid,
            data.title,
            data.dateTime.toDate().time,
            data.description,
            data.emoji,
            data.type,
            imageUrl = data.imageUrl
        )
    }

    override fun mapToData(domain: Reminder): ReminderDto {
        return ReminderDto(
            uid = domain.id,
            title = domain.title,
            description = domain.description,
            emoji = domain.emoji,
            type = domain.type,
            dateTime = Timestamp(
                domain.dateTime / 1000,
                ((domain.dateTime % 1000) * 1000000).toInt()
            ),
            imageUrl = domain.imageUrl
        )
    }
}