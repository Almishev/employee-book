package com.trackng.hours.dao;

public record LoginResponse(String token, String role, String name, String phoneNumber) {
}
