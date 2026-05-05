package com.trackng.hours.security;

import com.trackng.hours.enums.WorkerRole;

public record WorkerPrincipal(Long id, String name, String phoneNumber, WorkerRole role) {
}
