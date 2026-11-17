# TechNova — mod tech inspiré de Mekanism

Mod **NeoForge pour Minecraft 1.21.11** (Java 21). Énergie, machines, chaîne de traitement
des minerais jusqu'à **x5** et système de **chimie** (gaz, électrolyse, acide sulfurique).

> ⚠️ **Statut : v0.1, non compilée ni testée en jeu.** Le projet a été écrit sans accès réseau
> (impossible de télécharger Gradle/NeoForge). La cohérence des ressources et la syntaxe Java ont
> été vérifiées, mais pas la compilation contre l'API NeoForge. Attends-toi à corriger quelques
> imports ou signatures au premier build (voir « Premier build »).

## Compiler

Le plus fiable : prends le **MDK officiel 1.21.11 (ModDevGradle)** de NeoForge, puis copie dans
ce projet son `gradlew`, `gradlew.bat` et `gradle/wrapper/gradle-wrapper.jar` (le jar n'est pas
fourni ici). Ensuite :

```
./gradlew build        # produit build/libs/technova-0.1.0.jar
./gradlew runClient    # lance Minecraft avec le mod
```

Dans `gradle.properties`, `neo_version=21.11.+` prend le dernier NeoForge 21.11 ; remplace-le par
le numéro exact du MDK si tu veux un build reproductible.

## Contenu

**Ressources** : minerais d'étain, de plomb, de soufre, de sel (génération dans le monde),
lingots, blocs, circuits, châssis, tablettes d'énergie.

**Énergie (FE)** : générateur à combustion, solaire, à hydrogène ; cube d'énergie (2 M FE) ;
câble d'énergie.

**Traitement des minerais** (fer, or, cuivre, étain, plomb) :

| Étape | Machine | Gaz | Rendement |
|---|---|---|---|
| Minerai → poussière | Chambre d'enrichissement | — | x2 |
| Minerai → agglomérats | Chambre de purification | Oxygène | x3 |
| Minerai → éclats | Chambre d'injection chimique | Chlorure d'hydrogène | x4 |
| Minerai → cristaux | Chambre de dissolution chimique | Acide sulfurique | x5 |

Chaîne complète : minerai → 5 cristaux → (injection) 5 éclats → (purification) 5 agglomérats →
(broyeur) 5 poussières → (four électrique) 5 lingots.

**Chimie** : séparateur électrolytique (eau → hydrogène + oxygène), oxydeur chimique (soufre →
SO₂, sel → chlore), infuseur chimique (H₂+Cl₂→HCl, SO₂+O₂→SO₃, SO₃+H₂O→H₂SO₄), réservoir
chimique, tube chimique.

## Utilisation

- **Clic droit** sur une machine : GUI. Passe la souris sur une jauge pour voir les quantités.
- **Entonnoirs** : fonctionnent sur toutes les machines (entrées par le haut/côtés, sorties en bas).
- **Câbles** : relient générateurs, batteries et machines (ils s'équilibrent entre eux).
- **Tubes chimiques** : un seul produit par réseau. Les machines poussent leurs sorties vers les
  tubes/machines adjacents.
- **Cube d'énergie et réservoir** : entrée par toutes les faces sauf le dessous, **sortie par le
  dessous uniquement**.
- Le séparateur accepte l'eau en **seaux** (slot du haut) ou par tube.

## Nouveautés de cette version

- **Upgrades** : slots d'upgrade sur les machines de traitement (vitesse : +1 étape/tick par upgrade,
  jusqu'à 4, mais consomme plus ; énergie : réduit la consommation).
- **Configurateur** : **Maj + clic droit** sur une face d'une machine, d'un cube d'énergie ou d'un
  réservoir fait tourner le mode : tout / entrée / sortie / désactivée (énergie, objets, produits chimiques).
- **Compatibilité tuyaux d'objets d'autres mods** : les machines exposent la capacité d'objets de NeoForge.
- **Outils en étain** (pioche, hache, pelle, houe, épée) et **combinaison anti-radiation** (4 pièces, réparable au plomb).
- **Uranium** (minerai, chaîne de traitement complète) et **réacteur à fission** multibloc :
  cube 3x3x3 de carters du réacteur, un noyau au centre, le contrôleur remplace le carter au
  centre d'une face. Il brûle un lingot d'uranium (3000 ticks) + 1 mB d'eau/tick pour 300 FE/t.
- **Radiation** : uranium (en inventaire ou posé à proximité) et réacteurs en marche donnent faim,
  faiblesse, poison, wither selon la dose. La combinaison complète protège entièrement.
- **Recettes de machines en datapack** : `data/<namespace>/machine_recipes/*.json`,
  `machine_infusing/*.json`, `machine_oxidizing/*.json` (voir les exemples dans le mod ;
  `machine` vaut crusher, enrichment, smelter, purification, injection ou dissolution).
- Textures : plus travaillées (reliefs, biseaux, minerais à pépites), mais toujours générées par script.

## Limites connues / prochaines étapes

- Pas encore : tuyaux de fluides vanilla, turbine, JEI/EMI.
- Les produits chimiques sont un `enum` : ajouter un gaz = ajouter une ligne + une traduction.
- Les objets d'une machine tombent quand un joueur la casse, mais pas lors d'une explosion.
- Textures générées par script (pas dessinées à la main).
- **Rien de tout cela n'a été compilé ni testé en jeu** : prévois un ou deux tours de corrections.

## Premier build : points à surveiller

L'API énergie/transfert de NeoForge a été refaite en 21.9. Si le compilateur proteste, ce sont
probablement ces fichiers :

- `util/EnergyUtil.java`, `util/SideEnergy.java`, `blockentity/AbstractMachineBE.java` : imports
  `net.neoforged.neoforge.transfer.transaction.*` et `...transfer.energy.*`.
- `registry/ModBlockEntities.java` : constructeur de `BlockEntityType`.
- `client/MachineScreen.java` : `renderBg` / `renderLabels` / `renderTooltip`.
- `blockentity/AbstractMachineBE.java` : `WorldlyContainerWrapper(this, side)` (capacité d'objets).
- `recipe/MachineRecipes.java` : `AddServerReloadListenersEvent` et `SimpleJsonResourceReloadListener`.
- `radiation/RadiationHandler.java` : `PlayerTickEvent.Post`.
- `registry/ModMaterials.java`, `registry/ModItems.java` : constructeurs `ToolMaterial` / `ArmorMaterial`, `Item.Properties#pickaxe/sword/humanoidArmor`.
