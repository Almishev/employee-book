package com.trackng.hours.dao;

import com.trackng.hours.enums.WorkerRole;

public record MeResponse(WorkerRole role, String name, String phoneNumber) {
}
