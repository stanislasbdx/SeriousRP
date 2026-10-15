# ATMs, cassettes, and the bank

Practical guide. Permissions and commands live on their own page.

Cash has to be enabled. There are two ways to run the machines.

**Classic.** Players use an ATM like a normal one. Deposit turns the bills in their pocket into account money. Withdraw does the opposite. Nothing has to sit inside the machine.

**Advanced bank.** Turn on `Economy.Cash.AdvancedBank`. The ATM only moves money that is physically inside a cassette. Staff fill and empty those cassettes at a bank vault, and a pilot board shows the network.

If advanced bank is off, `[sBank]` and `[sPilot]` signs do nothing.

## Place an ATM

Put a sign and write `[sATM]` on the first line. The plugin rewrites the sign.

Creating one can cost account money. The price is `CreateCost` in the config (150 by default). Set it to 0 to make them free.

Players standing nearby see their own account and the cash in their pocket on the sign.

## Use an ATM

Right-click the sign.

- **Deposit** moves cash from your pocket to your account.
- **Withdraw** does the reverse and gives you bills.
- **Group** and **Break** only change the bills you are already holding: larger notes, or smaller change.

Amounts are the presets, everything you can move, or a custom number typed in chat. `cancel` aborts that prompt.

### Rename

The owner sees **Rename this ATM** in the menu. Staff who are allowed to rename any machine see it too. Type a name of up to 15 characters. It shows on the sign, in the ATM window, and on the pilot board. An ATM with no name is listed by its place in the world.

### Remove

Break the sign once and you get a confirm message. Break it again within 10 seconds. If cassettes were installed, they drop.

## Cassettes

Only with advanced bank on. Anyone can craft one: an iron frame, empty in the middle. The craft message tells you to lock it.

The item is named like `Cassette #f4de5`. That short id is the one written in the ATM log, so you can follow a cassette when it is installed, removed, filled, emptied, or wiped.

### Lock and open

Right-click the cassette in the air, not on a sign.

The first time, chat asks for a 4-digit code. Later, the same code opens it. `cancel` aborts.

Inside, it works like a wallet. Put bills in, take bills out, close it to save. It cannot hold more than its capacity. If it is very full, take some bills out and open it again to reach the rest.

Three wrong codes destroy the money inside. Chat says how much was lost. The cassette and its code stay, empty.

A cassette glows only when it actually holds cash.

### Load an ATM

Open the ATM as someone allowed to service it. The bottom row is the cassette slots (up to 8, set by `CassetteSlots`). The bottom-right slot is only a total, not a slot.

Drag a cassette from your inventory into an empty slot. Drag it back out to take it. Other players still use the ATM, they just cannot touch that row.

Without a cassette, deposit and withdraw are refused. A deposit fills the cassettes, then credits the player. A withdraw takes cash out of the cassettes, then debits the player. A full machine refuses more deposits. An empty one refuses withdrawals.

## Bank vault

Sign, first line `[sBank]`. Only with advanced bank on.

The first person to open it chooses a 4-digit code for that vault. Everyone else types the code. `cancel` aborts. Staff who are allowed to skip the code walk straight in.

One slot in the middle. Drop a cassette there, then either:

- empty it into the bank, or
- fill it from the bank, for a chosen amount or until the cassette is full.

Closing the vault gives the cassette back. The stock you see is the only cash the vault holds. A button glows when that move can actually happen.

Break works like an ATM: confirm within 10 seconds. Whatever is still in the vault drops as bills.

A usual run: pull the cassette out of the ATM, empty or fill it at the bank, put it back.

## Pilot board

Sign, first line `[sPilot]`. Right-click for the list of ATMs.

You see the machines you own. Staff who can see the whole network see every ATM. Each line is the ATM name, or its place in the world, and how much cash is in its cassettes. A machine with no cassette is marked as empty.

## Turn the advanced bank on

Set `Economy.Cash.AdvancedBank` to `true`, then reload or restart. Place the ATMs, craft and lock cassettes, drag them into the machines, and use a bank vault when they need cash. A pilot board is optional. It is only there so staff can see the network.
