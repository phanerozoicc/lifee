package com.github.phanerozoicc.base.domain

class EventSourcedAggregateRoot<ID>(
    id: ID
): AggregateRoot<ID>(id) {

    private var version: Long = 0
    private var lastEventVersion: Long = 0


}