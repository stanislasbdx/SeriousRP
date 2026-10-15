SeriousRP does not ship a resource pack. Cash, wallets, cheques, and cassettes are vanilla items with `CustomModelData`. The PNGs in this repo drop into a normal pack or into [Nexo](https://docs.nexomc.com).

The plugin writes the legacy integer and the 1.21.4+ `custom_model_data` floats. Change `config.yml` only if you change the IDs in the pack.

## Files

| Path | Size | Use |
| :--- | :--- | :--- |
| `assets/textures/item/*.png` | 64×64 RGBA | Files the pack must use |
| `assets/textures/item/hi/*.png` | 256×256 RGBA | Sources. Downscale with nearest-neighbour |

Item textures are square PNGs, transparent background, parent `minecraft:item/generated`. Copy the 64×64 files into the pack. Leave `hi/` out.

## What the plugin puts on items

| Item | Material | `CustomModelData` | Texture |
| :--- | :--- | :--- | :--- |
| Cheque | `PAPER` | `1` | `cheque.png` |
| Wallet | `BOOK` | `1` | `wallet.png` |
| Cassette | `CHEST` | `1` | `cassette.png` |
| 1 € | `IRON_NUGGET` | `1` | `coin_1.png` |
| 2 € | `GOLD_NUGGET` | `2` | `coin_2.png` |
| 5 € | `RESIN_BRICK` | `5` | `bill_5.png` |
| 10 € | `RESIN_BRICK` | `10` | `bill_10.png` |
| 20 € | `RESIN_BRICK` | `20` | `bill_20.png` |
| 50 € | `RESIN_BRICK` | `50` | `bill_50.png` |
| 100 € | `RESIN_BRICK` | `100` | `bill_100.png` |
| 200 € | `RESIN_BRICK` | `200` | `bill_200.png` |
| 500 € | `RESIN_BRICK` | `500` | `bill_500.png` |

Config keys, under `Economy.Cheque.CustomModelData`, `Economy.Cash.Wallet.CustomModelData`, and `Economy.Cash.Cassette.CustomModelData`. Denominations accept `custom-model-data`, `CustomModelData`, or `custom_model_data`. A missing denomination key defaults to the face value. `0` or a negative value disables model data for that item.

Wallets and cassettes get an enchantment glint from the plugin only when they hold cash. Coins, bills, and cheques do not glow. The glint is not part of the PNG.

Do not `/nexo give` these as the items players use. SeriousRP items carry PDC (`srp-cash`, `srp-wallet`, `srp-cheque`, `srp-atm-cassette`). The pack only changes how that material and model data look.

## Resource pack (1.21.4+)

Namespace below: `seriousrp`. One generated model per texture:

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "seriousrp:item/cassette"
  }
}
```

Put that file at `assets/seriousrp/models/item/cassette.json`. Same shape for `wallet`, `cheque`, `coin_1`, `coin_2`, and `bill_5` … `bill_500`. Textures go in `assets/seriousrp/textures/item/`.

Vanilla item definitions live in `assets/minecraft/items/`. Several bills share `resin_brick`, so one `range_dispatch` lists every threshold. The highest matching threshold wins.

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
    "fallback": { "type": "minecraft:model", "model": "minecraft:item/resin_brick" }
  }
}
```

The other items have a single threshold. Keep the vanilla item as `fallback`.

| File | Threshold | Model |
| :--- | :--- | :--- |
| `chest.json` | `1` | `seriousrp:item/cassette` |
| `book.json` | `1` | `seriousrp:item/wallet` |
| `paper.json` | `1` | `seriousrp:item/cheque` |
| `iron_nugget.json` | `1` | `seriousrp:item/coin_1` |
| `gold_nugget.json` | `2` | `seriousrp:item/coin_2` |

`assets/minecraft/items/chest.json`:

```json
{
  "model": {
    "type": "minecraft:range_dispatch",
    "property": "minecraft:custom_model_data",
    "entries": [
      { "threshold": 1, "model": { "type": "minecraft:model", "model": "seriousrp:item/cassette" } }
    ],
    "fallback": { "type": "minecraft:model", "model": "minecraft:item/chest" }
  }
}
```

`pack.mcmeta`: set `pack_format` for your Minecraft version ([pack format](https://minecraft.wiki/w/Pack_format)). `46` matches 1.21.4. Zip so `pack.mcmeta` is at the root of the zip.

On 1.21.3 and older, definitions live in `assets/minecraft/models/item/<item>.json` with `overrides` predicates instead of `assets/minecraft/items/`.

## Nexo

Nexo builds and sends the pack. Pick one path.

**Native items.** Copy the 64×64 PNGs to `plugins/Nexo/pack/assets/seriousrp/textures/item/`. In YAML that path is `seriousrp:item/cassette` (no `textures/`, no `.png`). Add `plugins/Nexo/items/seriousrp_cash.yml`:

```yaml
srp_cassette:
  material: CHEST
  excludeFromInventory: true
  Pack:
    texture: seriousrp:item/cassette
    custom_model_data: 1

srp_wallet:
  material: BOOK
  excludeFromInventory: true
  Pack:
    texture: seriousrp:item/wallet
    custom_model_data: 1

srp_cheque:
  material: PAPER
  excludeFromInventory: true
  Pack:
    texture: seriousrp:item/cheque
    custom_model_data: 1

srp_coin_1:
  material: IRON_NUGGET
  excludeFromInventory: true
  Pack:
    texture: seriousrp:item/coin_1
    custom_model_data: 1

srp_bill_5:
  material: RESIN_BRICK
  excludeFromInventory: true
  Pack:
    texture: seriousrp:item/bill_5
    custom_model_data: 5
```

Repeat the bill block for 10, 20, 50, 100, 200, and 500: material `RESIN_BRICK`, `custom_model_data` equal to the face value, texture `seriousrp:item/bill_<value>`. Add `srp_coin_2` the same way (`GOLD_NUGGET`, model data `2`). `Pack.custom_model_data` must match SeriousRP. Then `/nexo pack reload` and reconnect.

**External pack.** Build the zip from the section above and put the zip or the unpacked folder in `plugins/Nexo/pack/external_packs/`. Skip the YAML if you do this, so the same model data is not defined twice.

Docs: [Items](https://docs.nexomc.com/configuration/items), [Resource pack](https://docs.nexomc.com/configuration/resourcepack).
