# DalatS Demo Data

A simulated dataset for screenshots, already loaded into the `dalats` database under the current config. The existing accounts are unchanged.

## Accounts

Shared password for all demo accounts: **`DalatS@Demo2026`**.

| Email | Role / department |
| --- | --- |
| `admin@demo.dalats.test` | Admin – Nguyen Minh Anh |
| `staff1@demo.dalats.test` | Traffic infrastructure |
| `staff2@demo.dalats.test` | Sanitation |
| `staff3@demo.dalats.test` | Urban drainage |
| `staff4@demo.dalats.test` | Parks and trees |
| `staff5@demo.dalats.test` | Public lighting |
| `user1@demo.dalats.test` through `user5@demo.dalats.test` | Citizens, email verified |
| `user6@demo.dalats.test` | Locked citizen account, used to demo account management |

## Content

- 15 incidents, 5 categories, 3 incidents per category.
- 4 pending, 5 in progress, 5 resolved, 1 rejected. 10 incidents are public on the map.
- 5 mock departments, 1 admin, 5 staff, and 6 citizens.
- 8 questions, 5 answers, 10 comments, 16 history records, and 16 notifications.
- Timestamps spread across the month before the seed was run.
- GPS within central Da Lat: latitude 11.9338–11.9663, longitude 108.4248–108.4668. Locations are placed near real streets for demo purposes only, not surveyed on-site. Ward names follow the app's existing list.
- 5 photos are reused across matching incident types: pothole, roadside dumping, clogged drain, fallen tree, damaged light pole. These are stock photos from outside Vietnam, not confirmation photos of real incidents in Da Lat. Photos on resolved reports are the ones submitted at report time.

Photos are stored at `SafeDalat_API/wwwroot/uploads/incidents/demo/`, about 2.5 MB total, and don't require network access to display. Source, author, and license for each photo are listed in [credits.json](SafeDalat_API/wwwroot/uploads/incidents/demo/credits.json). If reusing these photos in public documentation, include the corresponding attribution.

## Re-running the seed

From the `Project_DaLatS` root, with SQL Server running:

```powershell
# Existing database with the schema already created but no matching baseline migration:
dotnet run --project SafeDalat_API/SafeDalat_API/SafeDalat_API.csproj --launch-profile http -- --seed-demo --seed-demo-existing-schema

# New database: set ConnectionStrings:SafeDalatConnection first, then run:
dotnet run --project SafeDalat_API/SafeDalat_API/SafeDalat_API.csproj --launch-profile http -- --seed-demo
```

This only runs in Development, loads the data in a single transaction, and completes before the HTTP server/workers start. If the demo admin's email already exists, the entire seed is skipped, so data isn't duplicated and passwords aren't reset. It never deletes existing data and never auto-seeds on a normal startup. The `--seed-demo-existing-schema` flag only inserts data — it doesn't modify the schema or fake a migration history.

Photos are already included in the project; only run this if photos are missing:

```powershell
powershell -ExecutionPolicy Bypass -File SafeDalat_API/scripts/Get-DemoImages.ps1
```

## Taking screenshots and testing

Run the API as usual, and the web app with `npm start` in `DalatS_Admin`. Log in as Admin to capture the dashboard, incident list, photo detail, departments, citizens, and Q&A. Log in as Staff to see department-scoped work. On Android, use `user1@demo.dalats.test` to capture the home screen, map, personal reports, and notifications.

With the API running:

```powershell
powershell -ExecutionPolicy Bypass -File SafeDalat_API/scripts/Test-Demo.ps1
```

The script checks login, demo record counts, Da Lat GPS coordinates, photo loading over HTTP, incidents on the map, the staff list, Q&A, and the dashboard; it doesn't modify any data. Google Maps on Android still needs a valid API key for the project.