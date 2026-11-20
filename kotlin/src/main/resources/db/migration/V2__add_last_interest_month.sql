-- Assumption: interest is applied at most once per calendar month. The month of the last applied interest
-- (first day of that month) is stored per deposit, which makes the balance update idempotent and safe to retry.
ALTER TABLE "timeDeposits" ADD COLUMN "lastInterestMonth" DATE;
