Depose ici ton fichier .schematic (format WorldEdit/MCEdit classique, NBT gzippe)
sous le nom exact :

    mine_structure.schematic

Il sera charge par MineSchematicRegenHandler.java et colle automatiquement dans
la dimension Mine (nettoyage puis recollage toutes les 30 minutes).

Coordonnees de collage : voir la constante ANCHOR dans
src/main/java/net/mura/rottenflesh/world/MineSchematicRegenHandler.java
- ajuste-la pour qu'elle corresponde a l'emplacement souhaite devant le portail.

Limitations actuelles du loader :
- Blocs avec ID legacy > 255 (tag "AddBlocks") non geres.
- Tile entities / entites du schematic ignores (contenu de coffres, etc. non restaure).
