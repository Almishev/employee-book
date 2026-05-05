package com.trackng.hours.dao;

import com.trackng.hours.enums.WorkerRole;

public record WorkerResponse(Long id, String name, String phoneNumber, WorkerRole role) {
}
