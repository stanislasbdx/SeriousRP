# ATM network: sATM, cassettes, sBank, sPilot

Guide for operators and staff.

## Modes

With `Economy.Cash.Enabled: true`, classic ATMs always work.

| Mode | Config | What you get |
| :--- | :--- | :--- |
| Classic (default) | `AdvancedBank: false` | `[sATM]` deposit/withdraw via Vault; cash appears or vanishes at the machine |
| Advanced bank | `AdvancedBank: true` | Cassettes inside ATMs + `[sBank]` + `[sPilot]` |

```yaml
Economy:
  Cash:
    Enabled: true
    AdvancedBank: false   # true = cassettes, sBank, sPilot
```

If `AdvancedBank` is off, typing `[sBank]` / `[sPilot]` is cancelled with a short message.

## Quick map

| Sign / item | Role |
| :--- | :--- |
| `[sATM]` | Player deposit / withdraw (and cash change) |
| Cassette | Removable cash cartridge installed in an ATM (`AdvancedBank` only) |
| `[sBank]` | Staff vault: move cash between a cassette and the bank stock |
| `[sPilot]` | Board listing registered ATMs and how much cash they hold |

---

## Permissions

| Permission | Default | Use |
| :--- | :---: | :--- |
| `seriousrp.economy.atm.create` | op | Place an `[sATM]` sign |
| `seriousrp.economy.atm.delete` | op | Break your own ATM |
| `seriousrp.economy.atm.delete.others` | op | Break someone else’s ATM |
| `seriousrp.economy.atm.use` | true | Open / use an ATM |
| `seriousrp.economy.atm.service` | op | Drag cassettes into an ATM |
| `seriousrp.economy.atm.rename` | op | Rename any ATM. The owner can rename their own without this |
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

The plugin rewrites the sign. With `AdvancedBank: true` the ATM is registered in `plugins/SeriousRP/atms.yml` and starts with **empty cassette slots**.

### Use (players)

Right-click the sign (`atm.use`):

- **Deposit** — physical cash → Vault balance (and into installed cassettes if advanced bank is on).
- **Withdraw** — Vault → physical cash (from installed cassettes if advanced bank is on).
- **Compact / Break** — regroup or split loose cash.

Nearby players see personalized account / pocket amounts on the sign within `Atm.ViewRadius`.

### Rename

The owner, or anyone with `atm.rename`, sees **Rename this ATM** in the ATM menu. The name is typed in chat (15 characters, `cancel` aborts). It replaces the sign title, the ATM window title, and the sPilot row. Unnamed ATMs stay listed by world and coordinates.

### Advanced bank rules (`AdvancedBank: true`)

- No cassette installed → deposit/withdraw refused.
- Deposit fills installed cassettes up to `CassetteCapacity` each, then credits the player Vault.
- Withdraw takes cash from installed cassettes and debits the player Vault.
- Full cassettes refuse further deposit; empty cassettes refuse withdraw.

### Break

Requires `atm.delete` (own) or `atm.delete.others`.  
Break once → confirm message; break again within **10 seconds** to destroy. Installed cassettes drop as items.

---

## Cassettes

Craftable by anyone while `AdvancedBank` is on. Recipe defaults to an iron frame (see `Economy.Cash.Cassette.Recipe`). A successful craft tells the player to right-click the cassette and lock it. Putting a cassette into an ATM still requires `atm.service`.

### PIN

Right-click a cassette (not a sign):

1. First time → chat asks for a **4-digit code**. That code is saved on the item.
2. Later → enter the code to open a read-only view of the bills inside.
3. Three wrong codes destroy the cash and say how much was destroyed. The item and the code stay. Type `cancel` to abort.

The item name shows a short id, for example `Cassette #f4de5`. The same id is written in `atm.log` for install, eject, bank moves, edits, and wipes.

After the code is accepted, the cassette opens like a wallet: put cash in, take cash out. Closing it saves the contents, up to `CassetteCapacity`. Bills use the same items as loose cash, so the resource-pack texture applies.

### Install / eject (service)

Open the ATM GUI with `atm.service`:

- The bottom row is real cassette slots. How many depends on `Atm.CassetteSlots`, capped at 8. The bottom-right slot is the cash total.
- Drag a cassette from your inventory into an empty slot.
- Drag it back out to eject it.
- Anyone without `atm.service` still uses the ATM, but the cassette row stays locked.

Cassettes are not wallet cash; they stay as their own item.

---

## sBank

Staff vault used to service ATM cassettes. Only when `AdvancedBank: true`.

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

One slot in the middle of the vault GUI. Drag a cassette into it, then:

| Action | Effect |
| :--- | :--- |
| **Empty the cassette into the bank** | Move the cassette cash into the vault stock, up to `VaultCapacity`. |
| **Move N / Fill the cassette** | Move that much stock into the cassette, up to cassette capacity and available stock. |

Closing the GUI gives the cassette back. Stock is the only cash the vault holds. Buttons glow only when that move is actually possible, and the stock icon glows only when the vault holds cash.

Typical courier loop:

1. Drag cassette(s) out of the ATM.
2. At sBank, drop a cassette in the slot and empty it into the bank, or fill it from the bank.
3. Drag the cassette back into an ATM slot.

### Break

`bank.delete` / `bank.delete.others` + confirm within 10 seconds. Remaining stock drops as physical cash.

---

## sPilot

Network board for ATM monitoring. Only when `AdvancedBank: true`.

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
    Enabled: true
    AdvancedBank: false
    Atm:
      SignHeader: "[sATM]"
      ViewRadius: 5
      CreateCost: 150
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
      VaultPlayer: "Banque"
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

## Suggested staff setup (advanced bank)

1. Set `Economy.Cash.AdvancedBank: true` and reload / restart.
2. Grant service roles `atm.service`, `bank.access`, and create perms as needed.
3. Place ATMs → craft cassettes → drag them into the ATM.
4. Use sBank to move cash between a cassette and the vault stock.
5. Place an sPilot for ops visibility.

With `AdvancedBank: false` you only need normal ATM create/use/delete perms.
