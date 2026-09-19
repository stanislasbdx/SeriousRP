# Cash item textures

SeriousRP does **not** ship a resource pack. Cash and wallets are vanilla items with `CustomModelData`. The PNGs in this repo are ready to drop into a normal pack or into [Nexo](https://docs.nexomc.com).

The plugin already writes the matching model data (legacy integer **and** the 1.21.4+ `custom_model_data` floats component). Change `config.yml` only if you change the IDs in the pack.

## Files

| Path | Size | Use |
| :--- | :--- | :--- |
| `assets/textures/item/*.png` | 64×64 RGBA | Files the pack must use |
| `assets/textures/item/hi/*.png` | 256×256 RGBA | Sources. Downscale with nearest-neighbour, never bilinear |

Item textures must be **square PNG**, transparent background, `minecraft:item/generated` (flat sprite). Bills are front-facing 2D notes; the bottom-right denomination is the `sRP` watermark.

A wallet PNG is not in this folder yet. Add `wallet.png` the same way (64×64, then a 256×256 copy under `hi/` if you keep sources).

## What the plugin puts on items

| Item | Material | `CustomModelData` | Texture |
| :--- | :--- | :--- | :--- |
| Wallet | `BOOK` | `1` | `wallet.png` |
| 1 € | `IRON_NUGGET` | `1` | `coin_1.png` |
| 2 € | `GOLD_NUGGET` | `2` | `coin_2.png` |
| 5 € | `RESIN_BRICK` | `5` | `bill_5.png` |
| 10 € | `RESIN_BRICK` | `10` | `bill_10.png` |
| 20 € | `RESIN_BRICK` | `20` | `bill_20.png` |
| 50 € | `RESIN_BRICK` | `50` | `bill_50.png` |
| 100 € | `RESIN_BRICK` | `100` | `bill_100.png` |
| 200 € | `RESIN_BRICK` | `200` | `bill_200.png` |
| 500 € | `RESIN_BRICK` | `500` | `bill_500.png` |

Config keys (all three names work on denominations):

```yaml
Economy:
  Cash:
    Wallet:
      Material: BOOK
      CustomModelData: 1          # 0 disables the model data
    Denominations:
      - value: 5
        material: RESIN_BRICK
        custom-model-data: 5      # or CustomModelData / custom_model_data
```

Missing denomination keys default to the face value. `0` or a negative value disables model data for that item.

Cash and wallets also get an enchantment glint from the plugin (`enchantment_glint_override`). That is not part of the PNG.

Do **not** `/nexo give` these items as the cash players use. SeriousRP items carry PDC (`srp-cash` / `srp-wallet`). The pack only changes how the vanilla material + model data **look**.

---

## 1. Normal resource pack (1.21.4+)

Namespace used below: `seriousrp`. Folder layout:

```
seriousrp-cash/
├── pack.mcmeta
└── assets/
    ├── minecraft/items/
    │   ├── book.json
    │   ├── iron_nugget.json
    │   ├── gold_nugget.json
    │   └── resin_brick.json
    └── seriousrp/
        ├── models/item/
        │   ├── wallet.json
        │   ├── coin_1.json
        │   ├── coin_2.json
        │   └── bill_5.json   # plus bill_10 … bill_500
        └── textures/item/
            ├── wallet.png    # when you have it
            ├── coin_1.png
            ├── coin_2.png
            └── bill_5.png    # plus bill_10 … bill_500
```

Copy the 64×64 files from `assets/textures/item/` into `assets/seriousrp/textures/item/`. Leave `hi/` out of the pack.

`pack.mcmeta` — set `pack_format` for your Minecraft version ([wiki](https://minecraft.wiki/w/Pack_format)):

```json
{
  "pack": {
    "pack_format": 46,
    "description": "SeriousRP cash"
  }
}
```

Each custom model (`assets/seriousrp/models/item/bill_5.json`, same idea for coins and wallet):

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "seriousrp:item/bill_5"
  }
}
```

Vanilla item definition — several bills share `resin_brick`, so one `range_dispatch` lists every threshold. Highest matching threshold wins.

`assets/minecraft/items/resin_brick.json`:

```json
{
  "model": {
    "type": "minecraft:range_dispatch",
    "property": "minecraft:custom_model_data",
    "entries": [
      { "threshold": 5,   "model": { "type": "minecraft:model", "model": "seriousrp:item/bill_5" } },
      { "threshold": 10,  "model": { "type": "minecraft:model", "model": "seriousrp:item/bill_10" } },
      { "threshold": 20,  "model": { "type": "minecraft:model", "model": "seriousrp:item/bill_20" } },
      { "threshold": 50,  "model": { "type": "minecraft:model", "model": "seriousrp:item/bill_50" } },
      { "threshold": 100, "model": { "type": "minecraft:model", "model": "seriousrp:item/bill_100" } },
      { "threshold": 200, "model": { "type": "minecraft:model", "model": "seriousrp:item/bill_200" } },
      { "threshold": 500, "model": { "type": "minecraft:model", "model": "seriousrp:item/bill_500" } }
    ],
    "fallback": {
      "type": "minecraft:model",
      "model": "minecraft:item/resin_brick"
    }
  }
}
```

`assets/minecraft/items/iron_nugget.json`:

```json
{
  "model": {
    "type": "minecraft:range_dispatch",
    "property": "minecraft:custom_model_data",
    "entries": [
      { "threshold": 1, "model": { "type": "minecraft:model", "model": "seriousrp:item/coin_1" } }
    ],
    "fallback": {
      "type": "minecraft:model",
      "model": "minecraft:item/iron_nugget"
    }
  }
}
```

Same pattern for `gold_nugget.json` (`threshold` 2 → `seriousrp:item/coin_2`) and `book.json` (`threshold` 1 → `seriousrp:item/wallet`). Always keep the vanilla fallback so normal books / nuggets / resin bricks stay vanilla.

Zip the folder (zip **the contents**, so `pack.mcmeta` is at the root of the zip), then either:

- put it in `server.properties` `resource-pack=`, or
- serve it however you already send a pack to players.

On 1.21.3 and older, item definitions live under `assets/minecraft/models/item/<item>.json` with `overrides` / `custom_model_data` predicates instead of `assets/minecraft/items/`. Prefer the `items/` format above on current Paper.

---

## 2. Nexo

Nexo builds and sends the pack. Two ways; pick one.

### A. Native Nexo items (pack generation)

1. Copy the 64×64 PNGs to:

   `plugins/Nexo/pack/assets/seriousrp/textures/item/`

   Example: `plugins/Nexo/pack/assets/seriousrp/textures/item/bill_5.png`  
   In YAML that path is `seriousrp:item/bill_5` (no `textures/`, no `.png`). See [Nexo FAQ](https://docs.nexomc.com/general-usage/faq).

2. Add `plugins/Nexo/items/seriousrp_cash.yml` (Nexo merges every file in `items/`). IDs are only so Nexo generates the pack; hide them from the Nexo catalog.

```yaml
srp_wallet:
  material: BOOK
  excludeFromInventory: true
  Pack:
    texture: seriousrp:item/wallet
    custom_model_data: 1

srp_coin_1:
  material: IRON_NUGGET
  excludeFromInventory: true
  Pack:
    texture: seriousrp:item/coin_1
    custom_model_data: 1

srp_coin_2:
  material: GOLD_NUGGET
  excludeFromInventory: true
  Pack:
    texture: seriousrp:item/coin_2
    custom_model_data: 2

srp_bill_5:
  material: RESIN_BRICK
  excludeFromInventory: true
  Pack:
    texture: seriousrp:item/bill_5
    custom_model_data: 5

srp_bill_10:
  material: RESIN_BRICK
  excludeFromInventory: true
  Pack:
    texture: seriousrp:item/bill_10
    custom_model_data: 10

srp_bill_20:
  material: RESIN_BRICK
  excludeFromInventory: true
  Pack:
    texture: seriousrp:item/bill_20
    custom_model_data: 20

srp_bill_50:
  material: RESIN_BRICK
  excludeFromInventory: true
  Pack:
    texture: seriousrp:item/bill_50
    custom_model_data: 50

srp_bill_100:
  material: RESIN_BRICK
  excludeFromInventory: true
  Pack:
    texture: seriousrp:item/bill_100
    custom_model_data: 100

srp_bill_200:
  material: RESIN_BRICK
  excludeFromInventory: true
  Pack:
    texture: seriousrp:item/bill_200
    custom_model_data: 200

srp_bill_500:
  material: RESIN_BRICK
  excludeFromInventory: true
  Pack:
    texture: seriousrp:item/bill_500
    custom_model_data: 500
```

`Pack.custom_model_data` must equal `Economy.Cash` in SeriousRP. Nexo then injects a `custom_model_data` range into the vanilla item model, so a SeriousRP `RESIN_BRICK` with model data `50` shows `bill_50.png`.

3. Reload the pack (`/nexo pack reload` or a restart) and reconnect so the client downloads it.

Docs: [Items](https://docs.nexomc.com/configuration/items), [Resource pack](https://docs.nexomc.com/configuration/resourcepack).

### B. Drop the vanilla pack into Nexo

Build the zip from section 1, then put the zip **or** the unpacked folder in:

`plugins/Nexo/pack/external_packs/`

Nexo merges it into the pack it sends. Skip the YAML in A if you do this, so you do not define the same model data twice.

---

## Checklist

- PNG is 64×64, RGBA, transparent around the sprite.
- Texture path / Nexo `Pack.texture` uses namespace `seriousrp` and the file name without `.png`.
- Material + `CustomModelData` in the pack match `config.yml`.
- Vanilla fallback is kept for each overridden item.
- Players still receive cash from SeriousRP (crafted wallet, ATM, `/cash transform`), not from Nexo give.
- After a pack change, reconnect (or force the pack to redistribute) and check one coin, one bill, and a wallet.
