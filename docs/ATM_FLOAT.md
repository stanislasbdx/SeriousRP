# ATM network: sATM, cassettes, sBank, sPilot

Guide for operators and staff. Physical cash float is **optional** and controlled by config.

## Quick map

| Sign | Role |
| :--- | :--- |
| `[sATM]` | Player deposit / withdraw (and cash change) |
| Cassette item | Removable cash float cartridge for an ATM |
| `[sBank]` | Staff vault: void / refill cassettes, feed stock |
| `[sPilot]` | Monitoring board for registered ATMs |

Enable the physical float with:

```yaml
Economy:
  Cash:
    Atm:
      PhysicalFloat: true   # false = classic ATM (default)
      CassetteSlots: 2
      CassetteCapacity: 50000
```

When `PhysicalFloat` is `false`, ATMs behave as before (Vault +/− and cash spawn/void). `[sBank]` / `[sPilot]` / cassette craft are inactive.

---

## Permissions

| Permission | Default | Use |
| :--- | :---: | :--- |
| `seriousrp.economy.atm.create` | op | Place an `[sATM]` sign |
| `seriousrp.economy.atm.delete` | op | Break your own ATM |
| `seriousrp.economy.atm.delete.others` | op | Break someone else’s ATM |
| `seriousrp.economy.atm.use` | true | Open / use an ATM |
| `seriousrp.economy.atm.service` | op | Craft cassettes; install / eject on ATM |
| `seriousrp.economy.bank.create` | op | Place an `[sBank]` sign |
| `seriousrp.economy.bank.delete` | op | Break your own sBank |
| `seriousrp.economy.bank.delete.others` | op | Break someone else’s sBank |
| `seriousrp.economy.bank.access` | op | Open an sBank (after PIN) |
| `seriousrp.economy.bank.access.bypass` | op | Open sBank without PIN |
| `seriousrp.economy.atm.pilot.create` | op | Place an `[sPilot]` sign |
| `seriousrp.economy.atm.pilot.others` | op | See every ATM on the pilot board |

---

## sATM

### Create

1. Place a sign.
2. Line 1: `[sATM]` (configurable via `Economy.Cash.Atm.SignHeader`).
3. Requires `atm.create`. Creation may charge Vault (`Atm.CreateCost`, default `150`).

The plugin rewrites the sign. With float on, the ATM is registered in `plugins/SeriousRP/atms.yml` and starts with **empty cassette slots** (no cash inside).

### Use (players)

Right-click the sign (`atm.use`):

- **Deposit** — physical cash → Vault balance (and into installed cassettes if float is on).
- **Withdraw** — Vault → physical cash (from installed cassettes if float is on).
- **Compact / Break** — regroup or split loose cash (same as `/cash` change helpers).

Nearby players see personalized account / pocket amounts on the sign within `Atm.ViewRadius`.

### Float rules (when `PhysicalFloat: true`)

- No cassette installed → ATM is out of service for deposit/withdraw.
- Deposit fills installed cassettes up to `CassetteCapacity` per cassette, then credits the player Vault.
- Withdraw takes from installed cassettes and debits the player Vault.
- Full cassettes refuse further deposit; empty float refuses withdraw.

### Break

Requires `atm.delete` (own) or `atm.delete.others`.  
Break once → confirm message; break again within **10 seconds** to destroy. Installed cassettes drop as items.

---

## Cassettes

Craftable only when float is enabled. Recipe defaults to an iron “frame” (see `Economy.Cash.Cassette.Recipe`). Crafting requires `atm.service`.

### Install / eject (service)

Open the ATM GUI with `atm.service`:

- Bottom row shows cassette slots.
- **Empty slot** + cassette in main hand → click slot → install (consumes one cassette).
- **Filled slot** → click → eject into inventory (needs a free slot).

Cassettes are not wallet cash; they stay as their own item.

---

## sBank

Staff vault for the **physical** float. Only relevant when `PhysicalFloat: true`.

### Create

1. Sign line 1: `[sBank]`.
2. Requires `bank.create`.

### Access + PIN

Right-click with `bank.access`:

1. First open → chat asks for a **4-digit PIN** (set).
2. Later opens → enter that PIN.
3. Type `cancel` to abort.
4. `bank.access.bypass` skips the PIN and opens the GUI immediately.

### GUI actions

Hold the cassette in the **main hand** for empty/fill.

| Action | Effect |
| :--- | :--- |
| **Feed** / **Feed all** | Debit the Vault account named `Bank.VaultPlayer` (default `Banque`) and increase vault stock (up to `VaultCapacity`). |
| **Empty cassette (void)** | Wipe the held cassette’s content. Cash is destroyed (not returned to stock). |
| **Fill cassette** | Move value from vault stock into the held cassette (up to cassette capacity). |

Typical courier loop:

1. Eject cassette(s) from ATM.
2. At sBank: void full cassettes and/or refill empty ones from stock.
3. Feed the vault when the bank demat account should seed physical stock.
4. Reinstall cassettes in ATM slots.

### Break

`bank.delete` / `bank.delete.others` + confirm within 10 seconds. Remaining stock drops as physical cash.

---

## sPilot

Network board for ATM monitoring. Only when float is enabled.

### Create

1. Sign line 1: `[sPilot]`.
2. Requires `atm.pilot.create`.

### Use

Right-click to open the list:

- Without `atm.pilot.others` → only ATMs you own.
- With `atm.pilot.others` → every registered ATM.

Each row shows world / coords and either total float or “no cassette”.

### Break

Owner needs `atm.pilot.create`; others need `atm.pilot.others`.

---

## Config reference

```yaml
Economy:
  Cash:
    Atm:
      SignHeader: "[sATM]"
      ViewRadius: 5
      CreateCost: 150
      PhysicalFloat: false
      CassetteSlots: 2
      CassetteCapacity: 50000
      Presets: [10, 25, 50, 100]
    Cassette:
      Material: CHEST
      CustomModelData: 1
      DisplayName: "&8Cassette"
      Recipe:
        Shape: ["III", "I I", "III"]
        Ingredients:
          I: IRON_INGOT
    Bank:
      SignHeader: "[sBank]"
      VaultPlayer: "Banque"      # Vault offline/player account fed into stock
      VaultCapacity: 500000
    Pilot:
      SignHeader: "[sPilot]"
```

Messages under `Economy.Cash.Cassette.*`, `Bank.*`, and `Pilot.*` are editable in `config.yml`.

---

## Persistence & logs

| File | Content |
| :--- | :--- |
| `plugins/SeriousRP/atms.yml` | Registered ATM locations + owners (for sPilot) |
| `plugins/SeriousRP/logs/atm.log` | Append-only ops: OPEN, DEPOSIT, WITHDRAW, INSTALL, EJECT, BANK_*, BREAK_* |

Cassette contents and sBank stock / PIN live on the sign tile PDC (world data), not in those YAML files.

---

## Suggested staff setup

1. Set `PhysicalFloat: true` and reload / restart.
2. Ensure the Vault account `Bank.VaultPlayer` exists and holds demat funds for feeding.
3. Grant service roles `atm.service`, `bank.access`, and create perms as needed.
4. Place ATMs → craft/install empty cassettes → feed sBank → fill cassettes → install.
5. Place an sPilot for ops visibility.

With float off, you only need normal ATM create/use/delete perms; ignore this guide’s cassette / bank / pilot sections.
