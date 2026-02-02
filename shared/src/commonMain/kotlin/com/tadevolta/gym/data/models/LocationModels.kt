package com.tadevolta.gym.data.models

import kotlinx.serialization.Serializable

@Serializable
data class State(
    val id: String,
    val name: String,
    val uf: String
)

@Serializable
data class City(
    val id: String,
    val name: String,
    val stateId: String
)
