# Contributing

## Branching
- Base off `main`. Keep `main` clean; no direct commits.
- Name branches `feature/<area>-<short-desc>` where area is `admin|user|provider`.

## Workflow
- Note: This project uses pnpm. Do not commit package-lock.json (npm lockfile).
1) Sync: `git checkout main` then `git pull origin main`.
2) Branch: `git checkout -b feature/<area>-<short-desc>`.
3) Set correct author (see Identity below) before committing.
4) Develop + tests.
5) Stay current: `git fetch origin` then `git merge origin/main` on your feature branch (no force push needed).
6) Push: `git push -u origin feature/...`.
7) Open PR to `main` using the template. Request a reviewer.
8) Merge via squash or fast-forward after approval; delete the branch.

## Identity
- Person A: `git config user.name "Dinuxd"` and `git config user.email "dwmddevinda@gmail.com"` (or `git commit --author="Dinuxd <dwmddevinda@gmail.com>"`).
- Person B: `git config user.name "pamithabandara"` and `git config user.email "pamithabandara123@gmail.com"` (or `git commit --author="pamithabandara <pamithabandara123@gmail.com>"`).
- Verify before committing: `git config user.name`, `git config user.email`.

## Reviews
- Admin-focused changes: prefer review by `@Dinuxd`.
- User-focused changes: prefer review by `@pamithabandara`.
- Provider/shared changes: either can review; aim for at least one review.

## Testing
- Note what you ran in the PR (manual/automated). Keep it honest and concise.
