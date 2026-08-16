@{
    BackendPort       = 8080
    FrontendPort      = 4200
    PostgresPort      = 5432

    PostgresContainer = 'datashare-postgres'
    PostgresUser      = 'datashare'
    PostgresDatabase  = 'datashare'

    BackendDirectory  = 'backend'
    FrontendDirectory = 'frontend'

    BackendHealthPath = '/actuator/health'
    StartupTimeoutSec = 60
}
