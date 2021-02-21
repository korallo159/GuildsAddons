package koral.guildsaddons.listeners;

import koral.sectorserver.SectorServer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class PrepareSmithingListener implements Listener {
    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent ev) {
        SectorServer.doForNonNull(ev.getResult(), result -> {
            ItemMeta meta = result.getItemMeta();
            if (meta.hasAttributeModifiers())
                return;

            int prot;
            EquipmentSlot slot;
            switch (result.getType()) {
                case NETHERITE_HELMET:      prot = 3; slot = EquipmentSlot.HEAD;    break;
                case NETHERITE_CHESTPLATE:  prot = 8; slot = EquipmentSlot.CHEST;   break;
                case NETHERITE_LEGGINGS:    prot = 6; slot = EquipmentSlot.LEGS;    break;
                case NETHERITE_BOOTS:       prot = 3; slot = EquipmentSlot.FEET;    break;
                default:
                    return;
            }
            meta.addAttributeModifier(Attribute.GENERIC_ARMOR,                new AttributeModifier(UUID.randomUUID(), "prot", prot,       AttributeModifier.Operation.ADD_NUMBER, slot));
            meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS,      new AttributeModifier(UUID.randomUUID(), "prott", 3,  AttributeModifier.Operation.ADD_NUMBER, slot));
            meta.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.randomUUID(), "knock", .05, AttributeModifier.Operation.ADD_NUMBER, slot));

            result.setItemMeta(meta);
            ev.setResult(result);
        });
    }
}
