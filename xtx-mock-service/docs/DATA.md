# Data

## Static mock data

Static JSON fixtures are under `src/main/resources/mock`.

## Runtime data

Runtime JSON files are written under `data/`.

Examples:

- cart runtime snapshots
- order runtime snapshots
- address runtime snapshots
- coupon and gift card runtime snapshots

## Persistence model

- Static fixture files seed the initial state
- Runtime files capture user-visible changes during local demos
- Deleting runtime JSON files resets the mutable demo state
