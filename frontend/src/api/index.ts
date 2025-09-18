import { apiFetch } from './client';
import type {
  AvailabilitySlotDto,
  BookingResponse,
  BulkAvailabilityRequest,
  CreateEventTypeRequest,
  CreatePublicBookingRequest,
  EventTypeResponse,
  PublicAvailabilityResponse,
  PublicBookingResponse,
  SingleAvailabilityRequest,
  HrRole,
} from './types';

export function getPublicAvailability(token: string, from: string, to: string) {
  const params = new URLSearchParams({ from, to }).toString();
  return apiFetch<PublicAvailabilityResponse>(`/public/${token}/availability?${params}`);
}

export function getPublicBookings(token: string, from: string, to: string, asRole?: HrRole) {
  const params = new URLSearchParams({ from, to });
  if (asRole) params.set('asRole', asRole);
  return apiFetch<PublicBookingResponse[]>(`/public/${token}/bookings?${params.toString()}`);
}

export function createPublicBooking(token: string, payload: CreatePublicBookingRequest) {
  return apiFetch<BookingResponse>(`/public/${token}/bookings`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function getPublicEventTypes(token: string) {
  return apiFetch<EventTypeResponse[]>(`/public/${token}/event-types`);
}

export function getEventTypes(developerId: string, devHeader: string) {
  return apiFetch<EventTypeResponse[]>(`/dev/${developerId}/event-types`, {
    headers: { 'X-Dev-Id': devHeader },
  });
}

export function createEventType(developerId: string, devHeader: string, payload: CreateEventTypeRequest) {
  return apiFetch<EventTypeResponse>(`/dev/${developerId}/event-types`, {
    method: 'POST',
    headers: { 'X-Dev-Id': devHeader },
    body: JSON.stringify(payload),
  });
}

export function getDeveloperAvailability(developerId: string, devHeader: string, from: string, to: string) {
  const params = new URLSearchParams({ from, to }).toString();
  return apiFetch<AvailabilitySlotDto[]>(`/dev/${developerId}/availability?${params}`, {
    headers: { 'X-Dev-Id': devHeader },
  });
}

export function addAvailability(developerId: string, devHeader: string, payload: SingleAvailabilityRequest) {
  return apiFetch<AvailabilitySlotDto>(`/dev/${developerId}/availability`, {
    method: 'POST',
    headers: { 'X-Dev-Id': devHeader },
    body: JSON.stringify(payload),
  });
}

export function deleteAvailability(developerId: string, devHeader: string, payload: SingleAvailabilityRequest) {
  return apiFetch<void>(`/dev/${developerId}/availability`, {
    method: 'DELETE',
    headers: { 'X-Dev-Id': devHeader },
    body: JSON.stringify(payload),
  });
}

export function bulkAddAvailability(developerId: string, devHeader: string, payload: BulkAvailabilityRequest) {
  return apiFetch<AvailabilitySlotDto[]>(`/dev/${developerId}/availability/bulk`, {
    method: 'POST',
    headers: { 'X-Dev-Id': devHeader },
    body: JSON.stringify(payload),
  });
}

export function getDeveloperBookings(developerId: string, devHeader: string, from: string, to: string) {
  const params = new URLSearchParams({ from, to }).toString();
  return apiFetch<BookingResponse[]>(`/dev/${developerId}/bookings?${params}`, {
    headers: { 'X-Dev-Id': devHeader },
  });
}

export function approveBooking(developerId: string, devHeader: string, bookingId: string) {
  return apiFetch<BookingResponse>(`/dev/${developerId}/bookings/${bookingId}/approve`, {
    method: 'POST',
    headers: { 'X-Dev-Id': devHeader },
  });
}

export function unapproveBooking(developerId: string, devHeader: string, bookingId: string) {
  return apiFetch<BookingResponse>(`/dev/${developerId}/bookings/${bookingId}/unapprove`, {
    method: 'POST',
    headers: { 'X-Dev-Id': devHeader },
  });
}

export function declineBooking(developerId: string, devHeader: string, bookingId: string) {
  return apiFetch<BookingResponse>(`/dev/${developerId}/bookings/${bookingId}/decline`, {
    method: 'POST',
    headers: { 'X-Dev-Id': devHeader },
  });
}
