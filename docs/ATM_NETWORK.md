# ATM network: sATM, cassettes, sBank, sPilot

Guide for operators and staff. ATMs can run in two modes, switched in config.

## Modes

| Mode | Config | What players get |
| :--- | :--- | :--- |
| Classic (default) | `PhysicalFloat: false` | Deposit/withdraw talk to Vault only; cash appears or vanishes at the machine |
| Cassettes | `PhysicalFloat: true` | Deposit/withdraw move real cash through removable cassettes inside the ATM |

```yaml
Economy:
  Cash:
    Atm:
      PhysicalFloat: true   # false = classic ATM (default)
      CassetteSlots: 2
      CassetteCapacity: 50000
```

With classic mode, ignore cassettes, `[sBank]`, and `[sPilot]` — they stay inactive.

## Quick map

| Sign / item | Role |
| :--- | :--- |
| `[sATM]` | Player deposit / withdraw (and cash change) |
| Cassette | Removable cash cartridge installed in an ATM |
| `[sBank]` | Staff vault: void / refill cassettes, restock the vault |
| `[sPilot]` | Board listing registered ATMs and how much cash they hold |

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

The plugin rewrites the sign. In cassette mode the ATM is registered in `plugins/SeriousRP/atms.yml` and starts with **empty cassette slots** (no cash inside).

### Use (players)

Right-click the sign (`atm.use`):

- **Deposit** — physical cash → Vault balance (and into installed cassettes in cassette mode).
- **Withdraw** — Vault → physical cash (taken from installed cassettes in cassette mode).
- **Compact / Break** — regroup or split loose cash (same as `/cash` change helpers).

Nearby players see personalized account / pocket amounts on the sign within `Atm.ViewRadius`.

### Cassette mode rules (`PhysicalFloat: true`)

- No cassette installed → deposit/withdraw refused.
- Deposit fills installed cassettes up to `CassetteCapacity` each, then credits the player Vault.
- Withdraw takes cash from installed cassettes and debits the player Vault.
- Full cassettes refuse further deposit; empty cassettes refuse withdraw.

### Break

Requires `atm.delete` (own) or `atm.delete.others`.  
Break once → confirm message; break again within **10 seconds** to destroy. Installed cassettes drop as items.

---

## Cassettes

Craftable only in cassette mode. Recipe defaults to an iron frame (see `Economy.Cash.Cassette.Recipe`). Crafting requires `atm.service`.

### Install / eject (service)

Open the ATM GUI with `atm.service`:

- Bottom row shows cassette slots.
- **Empty slot** + cassette in main hand → click slot → install (consumes one cassette).
- **Filled slot** → click → eject into inventory (needs a free slot).

Cassettes are not wallet cash; they stay as their own item.

---

## sBank

Staff vault used to service ATM cassettes. Only active in cassette mode.

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
3. Feed the vault when the bank Vault account should seed physical stock.
4. Reinstall cassettes in ATM slots.

### Break

`bank.delete` / `bank.delete.others` + confirm within 10 seconds. Remaining stock drops as physical cash.

---

## sPilot

Network board for ATM monitoring. Only active in cassette mode.

### Create

1. Sign line 1: `[sPilot]`.
2. Requires `atm.pilot.create`.

### Use

Right-click to open the list:

- Without `atm.pilot.others` → only ATMs you own.
- With `atm.pilot.others` → every registered ATM.

Each row shows world / coords and either total cash in cassettes or “no cassette”.

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
      VaultPlayer: "Banque"      # Vault account used when feeding stock
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

## Suggested staff setup (cassette mode)

1. Set `PhysicalFloat: true` and reload / restart.
2. Ensure the Vault account `Bank.VaultPlayer` exists and holds demat funds for feeding.
3. Grant service roles `atm.service`, `bank.access`, and create perms as needed.
4. Place ATMs → craft/install empty cassettes → feed sBank → fill cassettes → install.
5. Place an sPilot for ops visibility.

In classic mode you only need normal ATM create/use/delete perms.
