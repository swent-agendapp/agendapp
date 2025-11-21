export interface Organization {
  id: string,
  name: string,
  members: string[],
  admins: string[],
  waiting: string[],
  events: Event[],
  areas: Area[],
  geoCheckEnabled: boolean
}

export interface User {
    id: string,
    displayName?: string,
    email?: string,
    phoneNumber?: string,
    organizations: string[],
}

interface Event{
    id: string,
    title: string,
    description: string,
    startDate: Date,
    endDate: Date,
    personalNotes?: string,
    participants: string[],
    version: bigint,
    recurrenceStatus: RecurrenceStatus,
    hasBeenDeleted: boolean,
    color: number
}

export enum RecurrenceStatus {
  OneTime,
  Weekly,
  Monthly,
  Yearly
}

interface Area {
    id: string,
    label?: string,
    markers: Marker[]
}

interface Marker {
    id: string,
    location: Location
}

interface Location {
    latitude: number,
    longitude: number
}
