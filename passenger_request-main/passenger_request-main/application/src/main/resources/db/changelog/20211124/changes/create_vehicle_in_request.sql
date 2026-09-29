CREATE TABLE REQUEST.VEHICLE (
     ID                 UUID PRIMARY KEY,
     BRAND              VARCHAR(150),
     MODEL              VARCHAR(150),
     STATE_NUMBER       VARCHAR(150),
     COLOR              VARCHAR(150),
     CONTRACTOR_ID      UUID,
     ACTIVE             BOOLEAN DEFAULT TRUE NOT NULL
);