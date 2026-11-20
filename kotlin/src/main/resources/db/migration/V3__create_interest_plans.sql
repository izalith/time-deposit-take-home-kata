-- Assumption: "store all time deposit plans in a database" covers the plan rules, so rates and day limits
-- live here instead of in code. No interest while days <= interestStartsAfterDays, nor after interestEndsAfterDays.
CREATE TABLE "interestPlans"
(
    "planType"                VARCHAR(50)   PRIMARY KEY,
    "annualRate"              DECIMAL(7, 4) NOT NULL,
    "interestStartsAfterDays" INTEGER       NOT NULL,
    "interestEndsAfterDays"   INTEGER
);

INSERT INTO "interestPlans" ("planType", "annualRate", "interestStartsAfterDays", "interestEndsAfterDays")
VALUES ('basic', 0.0100, 30, NULL),
       ('student', 0.0300, 30, 365),
       ('premium', 0.0500, 45, NULL);
