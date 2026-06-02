package com.myfit.core.domain.model

enum class BulletColor { GREEN, AMBER, RED, BLUE }

data class BriefingBullet(
    val text: String,
    val color: BulletColor
)
