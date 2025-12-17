# Agendapp Architecture

Android application for managing organizational calendars, events, employee schedules, and replacements using **Clean Architecture** + **MVVM** + **Jetpack Compose**.

---

## Layered Architecture

```mermaid
graph TB
    subgraph "Presentation Layer"
        UI[UI Components<br/>Jetpack Compose]
        VM[ViewModels]
    end
    
    subgraph "Domain Layer"
        MODEL[Domain Models<br/>Event, Organization, User]
        REPO_INTERFACE[Repository Interfaces]
    end
    
    subgraph "Data Layer"
        PROVIDER[Repository Providers]
        REPO_IMPL[Repository Implementations]
        FIREBASE[Firebase<br/>Remote Data]
        OBJECTBOX[ObjectBox<br/>Local Data]
        HYBRID[Hybrid<br/>Sync Logic]
    end
    
    UI --> VM
    VM --> REPO_INTERFACE
    REPO_INTERFACE --> PROVIDER
    PROVIDER --> REPO_IMPL
    REPO_IMPL --> FIREBASE
    REPO_IMPL --> OBJECTBOX
    REPO_IMPL --> HYBRID
    HYBRID --> FIREBASE
    HYBRID --> OBJECTBOX
    
    style UI fill:#e1f5ff
    style VM fill:#bbdefb
    style MODEL fill:#c8e6c9
    style REPO_INTERFACE fill:#c8e6c9
    style PROVIDER fill:#fff9c4
    style REPO_IMPL fill:#ffe0b2
    style FIREBASE fill:#ffccbc
    style OBJECTBOX fill:#ffccbc
    style HYBRID fill:#ffccbc
```

---

## Complete Data Flow: Screens → ViewModels → Repositories

````carousel
<!-- slide -->
### Authentication Flow

```mermaid
graph LR
    SIGNIN[SignInScreen] --> SIGNIN_VM[SignInViewModel]
    SIGNIN_VM --> AUTH_REPO[AuthRepository]
    SIGNIN_VM --> USER_REPO[UserRepository]
    
    style SIGNIN fill:#e1f5ff
    style SIGNIN_VM fill:#bbdefb
    style AUTH_REPO fill:#c8e6c9
    style USER_REPO fill:#c8e6c9
```

<!-- slide -->
### Calendar & Events Flow

```mermaid
graph LR
    CAL[CalendarScreen] --> CAL_VM[CalendarViewModel]
    ADD[AddEventScreen] --> ADD_VM[AddEventViewModel]
    EDIT[EditEventScreen] --> EDIT_VM[EditEventViewModel]
    OVERVIEW[EventOverviewScreen] --> EO_VM[EventOverviewViewModel]
    
    CAL_VM --> EVENT_REPO[EventRepository]
    ADD_VM --> EVENT_REPO
    EDIT_VM --> EVENT_REPO
    EO_VM --> EVENT_REPO
    
    ADD_VM --> CAT_REPO[EventCategoryRepository]
    EDIT_VM --> CAT_REPO
    
    style CAL fill:#e1f5ff
    style ADD fill:#e1f5ff
    style EDIT fill:#e1f5ff
    style OVERVIEW fill:#e1f5ff
    style CAL_VM fill:#bbdefb
    style ADD_VM fill:#bbdefb
    style EDIT_VM fill:#bbdefb
    style EO_VM fill:#bbdefb
    style EVENT_REPO fill:#c8e6c9
    style CAT_REPO fill:#c8e6c9
```

<!-- slide -->
### Organization Flow

```mermaid
graph LR
    ORG_LIST[OrganizationListScreen] --> ORG_VM[OrganizationViewModel]
    ADD_ORG[AddOrganizationScreen] --> ADDORG_VM[AddOrganizationViewModel]
    ORG_OV[OrganizationOverviewScreen] --> ORGOV_VM[OrganizationOverviewViewModel]
    
    ORG_VM --> ORG_REPO[OrganizationRepository]
    ORG_VM --> SEL_ORG_REPO[SelectedOrganizationRepository]
    ADDORG_VM --> ORG_REPO
    ORGOV_VM --> ORG_REPO
    ORGOV_VM --> USER_REPO[UserRepository]
    
    style ORG_LIST fill:#e1f5ff
    style ADD_ORG fill:#e1f5ff
    style ORG_OV fill:#e1f5ff
    style ORG_VM fill:#bbdefb
    style ADDORG_VM fill:#bbdefb
    style ORGOV_VM fill:#bbdefb
    style ORG_REPO fill:#c8e6c9
    style USER_REPO fill:#c8e6c9
    style SEL_ORG_REPO fill:#c8e6c9
```

<!-- slide -->
### Replacement Flow

```mermaid
graph LR
    REPL_EMP[ReplacementEmployeeFlow] --> REPL_VM[ReplacementEmployeeViewModel]
    REPL_ORG[ReplacementOrganizeScreen] --> REPLO_VM[ReplacementOrganizeViewModel]
    REPL_PEND[ReplacementPendingListScreen] --> REPL_VM
    REPL_UP[ReplacementUpcomingListScreen] --> REPL_VM
    
    REPL_VM --> REPL_REPO[ReplacementRepository]
    REPLO_VM --> REPL_REPO
    REPL_VM --> EVENT_REPO[EventRepository]
    REPLO_VM --> EVENT_REPO
    
    style REPL_EMP fill:#e1f5ff
    style REPL_ORG fill:#e1f5ff
    style REPL_PEND fill:#e1f5ff
    style REPL_UP fill:#e1f5ff
    style REPL_VM fill:#bbdefb
    style REPLO_VM fill:#bbdefb
    style REPL_REPO fill:#c8e6c9
    style EVENT_REPO fill:#c8e6c9
```

<!-- slide -->
### Invitation Flow

```mermaid
graph LR
    INV_OV[InvitationOverviewScreen] --> INV_VM[InvitationOverviewViewModel]
    CREATE_INV[CreateInvitationScreen] --> CINV_VM[CreateInvitationViewModel]
    USE_INV[UseInvitationScreen] --> UINV_VM[UseInvitationViewModel]
    
    INV_VM --> INV_REPO[InvitationRepository]
    CINV_VM --> INV_REPO
    UINV_VM --> INV_REPO
    UINV_VM --> ORG_REPO[OrganizationRepository]
    
    style INV_OV fill:#e1f5ff
    style CREATE_INV fill:#e1f5ff
    style USE_INV fill:#e1f5ff
    style INV_VM fill:#bbdefb
    style CINV_VM fill:#bbdefb
    style UINV_VM fill:#bbdefb
    style INV_REPO fill:#c8e6c9
    style ORG_REPO fill:#c8e6c9
```

<!-- slide -->
### Profile, Settings & Utilities Flow

```mermaid
graph LR
    PROF[ProfileScreen] --> PROF_VM[ProfileViewModel]
    HOUR[HourRecapScreen] --> HOUR_VM[HourRecapViewModel]
    MAP[MapScreen] --> MAP_VM[MapViewModel]
    CAT[EditCategoryScreen] --> CAT_VM[EditCategoryViewModel]
    
    PROF_VM --> AUTH_REPO[AuthRepository]
    PROF_VM --> USER_REPO[UserRepository]
    
    HOUR_VM --> EVENT_REPO[EventRepository]
    HOUR_VM --> USER_REPO
    HOUR_VM --> ORG_REPO[OrganizationRepository]
    
    MAP_VM --> MAP_REPO[MapRepository]
    MAP_VM --> LOC_REPO[LocationRepository]
    
    CAT_VM --> CAT_REPO[EventCategoryRepository]
    
    style PROF fill:#e1f5ff
    style HOUR fill:#e1f5ff
    style MAP fill:#e1f5ff
    style CAT fill:#e1f5ff
    style PROF_VM fill:#bbdefb
    style HOUR_VM fill:#bbdefb
    style MAP_VM fill:#bbdefb
    style CAT_VM fill:#bbdefb
    style AUTH_REPO fill:#c8e6c9
    style USER_REPO fill:#c8e6c9
    style EVENT_REPO fill:#c8e6c9
    style ORG_REPO fill:#c8e6c9
    style MAP_REPO fill:#c8e6c9
    style LOC_REPO fill:#c8e6c9
    style CAT_REPO fill:#c8e6c9
```
````

---

## Data Layer Architecture

### Event Repository Pattern

```mermaid
graph TB
    VM[ViewModels] --> INTERFACE[EventRepository Interface]
    
    INTERFACE --> PROVIDER[EventRepositoryProvider<br/>Singleton]
    
    PROVIDER --> HYBRID[EventRepositoryHybrid<br/>Default Implementation]
    
    HYBRID --> FIREBASE[EventRepositoryFirebase<br/>Remote Storage]
    HYBRID --> LOCAL[EventRepositoryLocal<br/>ObjectBox Storage]
    
    FIREBASE --> FIRESTORE[(Cloud Firestore)]
    LOCAL --> OBJECTBOX[(ObjectBox Database)]
    
    style VM fill:#bbdefb
    style INTERFACE fill:#c8e6c9
    style PROVIDER fill:#fff9c4
    style HYBRID fill:#ffe0b2
    style FIREBASE fill:#ffccbc
    style LOCAL fill:#ffccbc
    style FIRESTORE fill:#ef9a9a
    style OBJECTBOX fill:#ef9a9a
```

### Storage Strategies by Domain

```mermaid
graph LR
    subgraph "Domain Areas"
        EVENT[Event]
        ORG[Organization]
        USER[User]
        REPL[Replacement]
        INV[Invitation]
        CAT[Category]
        MAP[Map]
    end
    
    subgraph "Storage Strategies"
        HYB[Hybrid<br/>Sync]
        FB[Firebase<br/>Remote]
        LOC[Local<br/>ObjectBox]
    end
    
    EVENT --> HYB
    ORG -.-> FB
    ORG -.-> LOC
    USER -.-> FB
    USER -.-> LOC
    REPL -.-> FB
    REPL -.-> LOC
    INV -.-> FB
    INV -.-> LOC
    CAT -.-> FB
    CAT -.-> LOC
    MAP -.-> FB
    MAP -.-> LOC
    
    style EVENT fill:#c8e6c9
    style ORG fill:#c8e6c9
    style USER fill:#c8e6c9
    style REPL fill:#c8e6c9
    style INV fill:#c8e6c9
    style CAT fill:#c8e6c9
    style MAP fill:#c8e6c9
    style HYB fill:#ffe0b2
    style FB fill:#ffccbc
    style LOC fill:#ffccbc
```


