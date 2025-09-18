export type BookingStatus = 'NOT_APPROVED' | 'APPROVED' | 'DECLINED';
export type HrRole = 'HR1' | 'HR2';
export type Role = 'DEV' | 'HR1' | 'HR2';

export interface AvailabilitySlotDto {
  id: string;
  startAt: string;
  durationMinutes: number;
}

export interface PublicAvailabilityResponse {
  slots: AvailabilitySlotDto[];
}

export interface PublicBookingResponse {
  id: string;
  startAt: string;
  durationMinutes: number;
  status: BookingStatus;
  eventTypeName: string | null;
  company: string | null;
  hrName: string | null;
  hrEmail: string | null;
  meetingLink: string | null;
  createdByRole: HrRole;
  occupied: boolean;
}

export interface BookingResponse {
  id: string;
  startAt: string;
  durationMinutes: number;
  status: BookingStatus;
  eventTypeName: string;
  company: string;
  hrName: string;
  hrEmail: string;
  meetingLink: string | null;
  createdByRole: HrRole;
}

export interface EventTypeResponse {
  id: string;
  name: string;
}

export interface CreateEventTypeRequest {
  name: string;
}

export interface SingleAvailabilityRequest {
  startAt: string;
}

export interface BulkAvailabilityRequest {
  startDate: string;
  endDate: string;
  dailyStart: string;
  dailyEnd: string;
}

export interface CreatePublicBookingRequest {
  startAt: string;
  durationMinutes: 30 | 60 | 90 | 120;
  eventTypeName: string;
  createdByRole: HrRole;
  company: string;
  hrName: string;
  hrEmail: string;
  meetingLink?: string;
}
